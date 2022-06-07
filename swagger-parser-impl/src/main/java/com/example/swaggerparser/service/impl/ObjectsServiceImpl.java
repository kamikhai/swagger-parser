package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.*;
import com.example.swaggerparser.entity.TypeMapping;
import com.example.swaggerparser.mapper.ImportObjectMapper;
import com.example.swaggerparser.service.ObjectsService;
import com.example.swaggerparser.service.TypeMappingService;
import com.example.swaggerparser.util.ParameterizedClassesUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.example.swaggerparser.constant.SwaggerConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectsServiceImpl implements ObjectsService {
    private final TypeMappingService typeMappingService;
    private final ImportObjectMapper importObjectMapper;

    @Override
    public Set<FlutterObject> getObjects(Components components, List<ImportObject> objectToCreate, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects) {
        Set<FlutterObject> objects = new HashSet<>();
        Map<String, Schema> all = components.getSchemas();

        while (!objectToCreate.isEmpty()) {
            ImportObject o = objectToCreate.stream().findFirst().orElseThrow();
            if (Objects.isNull(o.getImportClass())) {
                String name = o.getName();
                ParameterizationInfo parameterizationInfo = new ParameterizationInfo();
                Schema objectSchema = getObjectSchema(name, parameterizationInfo, all);

                Set<ImportObject> relatedObjects = new HashSet<>();
                List<ObjectField> fields = new ArrayList<>();
                objectSchema = checkToComposedObjectSchema(objectSchema);
                if (Objects.nonNull(objectSchema) && Objects.nonNull(objectSchema.getProperties())) {
                    List<String> required = Objects.nonNull(objectSchema.getRequired()) ? objectSchema.getRequired() : List.of();
                    objectSchema.getProperties().forEach((BiConsumer<String, Schema>) (s, schema) -> {
                        String type = getFieldType(schema, relatedObjects, objectToCreate, parameterizationInfo, s, enumsToCreate, enumObjects);
                        fields.add(new ObjectField(s, type, required.contains(s)));
                    });
                }
                objects.add(new FlutterObject(name.replace(" ", ""), fields, relatedObjects, parameterizationInfo.isParameterized()));
                removeCreated(all, name, parameterizationInfo);
            }
            objectToCreate.remove(o);
        }
        return objects;
    }

    private String getFieldType(Schema schema, Set<ImportObject> relatedObjects, List<ImportObject> objectToCreate,
                                ParameterizationInfo parameterizationInfo, String s, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects) {
        String type;
        if (Objects.isNull(schema.getType())) {
            type = getObjectType(schema, relatedObjects, objectToCreate, parameterizationInfo);
        } else if (schema.getType().equals(TYPE_ARRAY)) {
            type = getArrayType(schema, relatedObjects, objectToCreate, parameterizationInfo, s, enumsToCreate, enumObjects);
        } else if (schema.getType().equals(TYPE_OBJECT)) {
            type = String.format(MAP_TYPE, typeMappingService.getType((Schema) schema.getAdditionalProperties()));
        } else {
            type = getSimpleType(schema, relatedObjects, enumsToCreate, s, enumObjects);
        }
        return type;
    }

    private String getSimpleType(Schema schema, Set<ImportObject> relatedObjects, Set<EnumObject> enumsToCreate, String s, List<EnumObject> enumObjects) {
        String type;
        if (typeMappingService.isEnum(schema)) {
            type = typeMappingService.getEnum(relatedObjects, schema, enumsToCreate, enumObjects);
        } else {
            Optional<TypeMapping> typeMappingOptional = typeMappingService.getTypeMapping(schema.getType());
            if (typeMappingOptional.isPresent()) {
                TypeMapping typeMapping = typeMappingOptional.get();
                type = typeMapping.getFlutterType();
                if (Objects.nonNull(typeMapping.getImportClass()) && !typeMapping.getImportClass().isBlank()) {
                    relatedObjects.add(importObjectMapper.toDto(typeMapping));
                }
            } else {
                type = schema.getType();
                log.error("Can't find type " + type);
            }
        }
        return type;
    }

    private String getArrayType(Schema schema, Set<ImportObject> relatedObjects, List<ImportObject> objectToCreate,
                                ParameterizationInfo parameterizationInfo, String s, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects) {
        String type;
        if (parameterizationInfo.isParameterized() && typeMappingService.getArrayClass(schema).isPresent()
                && typeMappingService.getArrayClass(schema).orElseThrow().equals(parameterizationInfo.getParameterizationType())) {
            type = "List<T>";
        } else {
            type = typeMappingService.getArrayTypeOrEnum(schema, relatedObjects, enumsToCreate, enumObjects);
            typeMappingService.getArrayClass(schema).ifPresent(cl -> {
                relatedObjects.add(ImportObject.builder().name(cl).build());
                objectToCreate.add(ImportObject.builder().name(cl).build());
            });
        }
        return type;
    }

    private String getObjectType(Schema schema, Set<ImportObject> relatedObjects, List<ImportObject> objectToCreate,
                                 ParameterizationInfo parameterizationInfo) {
        String type = typeMappingService.getObjectType(schema);
        if (parameterizationInfo.isParameterized() && type.equals(parameterizationInfo.getParameterizationType())) {
            type = "T";
        } else {
            relatedObjects.add(ImportObject.builder().name(type).build());
            objectToCreate.add(ImportObject.builder().name(type).build());
        }
        return type;
    }

    private Schema checkToComposedObjectSchema(Schema objectSchema) {
        if (objectSchema instanceof ComposedSchema) {
            objectSchema = ((ComposedSchema) objectSchema).getAllOf().stream().filter(ObjectSchema.class::isInstance)
                    .findFirst().orElse(null);
        }
        return objectSchema;
    }

    private void removeCreated(Map<String, Schema> all, String name, ParameterizationInfo parameterizationInfo) {
        if (parameterizationInfo.isParameterized()) {
            List<String> toRemove = all.keySet().stream().filter(key ->
                    ParameterizedClassesUtil.getParameterizedClassPattern(name).matcher(key).matches()
            ).collect(Collectors.toList());
            toRemove.forEach(all::remove);
        } else {
            all.remove(name);
        }
    }

    private Schema getObjectSchema(String name, ParameterizationInfo parameterizationInfo, Map<String, Schema> all) {
        Schema objectSchema = null;

        Optional<Map.Entry<String, Schema>> first = findClassByPattern(name, all);
        if (first.isPresent()) {
            parameterizationInfo.setParameterized(true);
            parameterizationInfo.setParameterizationType(ParameterizedClassesUtil.getParameterizationType(first.get().getKey()));
            objectSchema = first.get().getValue();
        } else if (all.containsKey(name)) {
            parameterizationInfo.setParameterized(false);
            parameterizationInfo.setParameterizationType("");
            objectSchema = all.get(name);
        }
        return objectSchema;
    }

    private Optional<Map.Entry<String, Schema>> findClassByPattern(String name, Map<String, Schema> all) {
        return all.entrySet().stream().filter(entry -> {
            Matcher matcher = ParameterizedClassesUtil.getParameterizedClassPattern(name).matcher(entry.getKey());
            return matcher.matches();
        }).findFirst();
    }

}
