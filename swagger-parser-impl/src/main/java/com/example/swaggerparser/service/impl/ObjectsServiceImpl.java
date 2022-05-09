package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.FlutterObject;
import com.example.swaggerparser.dto.ImportObject;
import com.example.swaggerparser.dto.ObjectField;
import com.example.swaggerparser.entity.TypeMapping;
import com.example.swaggerparser.service.NameConverterService;
import com.example.swaggerparser.service.ObjectsService;
import com.example.swaggerparser.service.TypeMappingService;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.swaggerparser.constant.SwaggerConstant.TYPE_ARRAY;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectsServiceImpl implements ObjectsService {
    private final TypeMappingService typeMappingService;
    private final NameConverterService nameConverterService;

    @Override
    public List<FlutterObject> getObjects(Components components, Set<ImportObject> objectToCreate, Map<String, List<String>> enums) {
        List<FlutterObject> objects = new ArrayList<>();
        Map<String, Schema> all = components.getSchemas();

        while (!objectToCreate.isEmpty()) {

            ImportObject o = objectToCreate.stream().findFirst().get();
            if (Objects.isNull(o.getImportClass())) {
                String name = o.getName();
                var lambdaContext = new Object() {
                    boolean isParameterized;
                    String parameterizationType;
                };
                Schema object = null;
                Pattern pattern = Pattern.compile("^" + name + "«.*»");
                Optional<Map.Entry<String, Schema>> first = all.entrySet().stream().filter(entry -> {
                    Matcher matcher = pattern.matcher(entry.getKey());
                    return matcher.matches();
                }).findFirst();
                if (first.isPresent()) {
                    lambdaContext.isParameterized = true;
                    String key = first.get().getKey();
                    lambdaContext.parameterizationType = key.substring(key.indexOf("«") + 1, (key.indexOf("»")));
                    object = first.get().getValue();
                } else if (all.containsKey(name)) {
                    lambdaContext.isParameterized = false;
                    lambdaContext.parameterizationType = "";
                    object = all.get(name);
                }

                if (Objects.nonNull(object)) {
                    Set<ImportObject> relatedObjects = new HashSet<>();
                    List<ObjectField> fields = new ArrayList<>();
                    if (object instanceof ComposedSchema) {
                        object = ((ComposedSchema) object).getAllOf().stream().filter(ObjectSchema.class::isInstance)
                                .findFirst().orElse(null);
                    }
                    if (Objects.nonNull(object)) {
                        List<String> required = Objects.nonNull(object.getRequired()) ? object.getRequired() : List.of();
                        if (Objects.nonNull(object.getProperties())) {
                            object.getProperties().forEach((BiConsumer<String, Schema>) (s, schema) -> {
                                String type;
                                if (Objects.isNull(schema.getType())) {
                                    type = typeMappingService.getObjectType(schema);
                                    if (lambdaContext.isParameterized && type.equals(lambdaContext.parameterizationType)) {
                                        type = "T";
                                    } else {
                                        relatedObjects.add(ImportObject.builder().name(type).build());
                                        objectToCreate.add(ImportObject.builder().name(type).build());
                                    }
                                } else if (schema.getType().equals(TYPE_ARRAY)) {
                                    if (lambdaContext.isParameterized && typeMappingService.getArrayClass(schema).isPresent()
                                            && typeMappingService.getArrayClass(schema).get().equals(lambdaContext.parameterizationType)) {
                                        type = "List<T>";
                                    } else {
                                        type = typeMappingService.getArrayTypeOrEnum(s, schema, relatedObjects, enums);
                                        typeMappingService.getArrayClass(schema).ifPresent(cl -> {
                                            relatedObjects.add(ImportObject.builder().name(cl).build());
                                            objectToCreate.add(ImportObject.builder().name(cl).build());
                                        });
                                    }
                                } else {
                                    if (typeMappingService.isEnum(schema)) {
                                        type = nameConverterService.toUpperCamel(s);
                                        relatedObjects.add(ImportObject.builder().name(type).build());
                                        enums.put(type, schema.getEnum());
                                    } else {
                                        Optional<TypeMapping> typeMappingOptional = typeMappingService.getTypeMapping(schema.getType());
                                        if (typeMappingOptional.isPresent()) {
                                            TypeMapping typeMapping = typeMappingOptional.get();
                                            type = typeMapping.getFlutterType();
                                            if (Objects.nonNull(typeMapping.getImportClass())) {
                                                relatedObjects.add(ImportObject.builder()
                                                        .name(typeMapping.getFlutterType())
                                                        .importClass(typeMapping.getImportClass())
                                                        .build());
                                            }
                                        } else {
                                            type = schema.getType();
                                            log.error("Can't find type " + type);
                                        }
                                    }
                                }
                                fields.add(new ObjectField(s, type, required.contains(s)));
                            });
                        }
                    }
                    objects.add(new FlutterObject(name.replace(" ", ""), fields, relatedObjects, lambdaContext.isParameterized));
                    if (lambdaContext.isParameterized) {
                        List<String> toRemove = all.entrySet().stream().map(Map.Entry::getKey).filter(key -> {
                            Matcher matcher = pattern.matcher(key);
                            return matcher.matches();
                        }).collect(Collectors.toList());
                        toRemove.forEach(all::remove);
                    } else {
                        all.remove(name);
                    }
                }
            }
            objectToCreate.remove(o);
        }
        return objects;
    }
}
