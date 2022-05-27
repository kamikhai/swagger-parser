package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.EnumObject;
import com.example.swaggerparser.dto.ImportObject;
import com.example.swaggerparser.entity.TypeMapping;
import com.example.swaggerparser.mapper.ImportObjectMapper;
import com.example.swaggerparser.repository.TypeMappingRepository;
import com.example.swaggerparser.service.EnumParserService;
import com.example.swaggerparser.service.NameConverterService;
import com.example.swaggerparser.service.TypeMappingService;
import com.example.swaggerparser.util.ParameterizedClassesUtil;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.swaggerparser.constant.SwaggerConstant.*;
import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

@Slf4j
@Service
@RequiredArgsConstructor
public class TypeMappingServiceImpl implements TypeMappingService {

    private final TypeMappingRepository typeMappingRepository;
    private final NameConverterService nameConverterService;
    private final ImportObjectMapper importObjectMapper;
    private final EnumParserService enumParserService;

    @Override
    public String findFlutterTypeBySwaggerType(String type) {
        Optional<TypeMapping> typeMappingOptional = typeMappingRepository.findBySwaggerType(type);
        if (typeMappingOptional.isPresent()) {
            return typeMappingOptional.get().getFlutterType();
        } else {
            log.error(String.format("Type %s not found", type));
            return type;
        }
    }

    @Override
    public String getSimpleArrayType(Schema schema) {
        return String.format(LIST_TYPE, findFlutterTypeBySwaggerType(((ArraySchema) schema).getItems().getType()));
    }

    private String getObjectArrayType(Schema schema) {
        return String.format(LIST_TYPE, getObjectArrayClass(schema));
    }

    private String getObjectArrayClass(Schema schema) {
        return getObjectName(((ArraySchema) schema).getItems().get$ref());
    }

    @Override
    public String getArrayType(Schema schema) {
        if (Objects.nonNull(((ArraySchema) schema).getItems().getType())) {
            return getSimpleArrayType(schema);
        } else {
            return getObjectArrayType(schema);
        }
    }

    @Override
    public Optional<String> getArrayClass(Schema schema) {
        if (Objects.isNull(((ArraySchema) schema).getItems().getType())) {
            return Optional.of(getObjectArrayClass(schema));
        }
        return Optional.empty();
    }

    @Override
    public String getType(Schema schema) {
        return findFlutterTypeBySwaggerType(schema.getType());
    }

    @Override
    public String getObjectType(Schema schema) {
        return getObjectName(schema.get$ref());
    }

    @Override
    public String getObjectName(Schema schema) {
        return nameConverterService.toLowerCamel(getObjectName(schema.get$ref()));
    }

    @Override
    public String getArrayName(Schema schema) {
        String type = ((ArraySchema) schema).getItems().getType();
        if (Objects.nonNull(type)) {
            return nameConverterService.toLowerCamel(type) + "List";
        } else {
            return nameConverterService.toLowerCamel(getObjectName(((ArraySchema) schema).getItems().get$ref()));
        }
    }

    @Override
    public Optional<TypeMapping> getTypeMapping(String type) {
        return typeMappingRepository.findBySwaggerType(type);
    }

    @Override
    public boolean isEnumArray(Schema schema) {
        return Objects.nonNull(((ArraySchema) schema).getItems().getEnum());
    }

    @Override
    public List getArrayEnums(Schema schema) {
        return ((ArraySchema) schema).getItems().getEnum();
    }

    @Override
    public boolean isEnum(Schema schema) {
        return schema.getType().equals("string") && Objects.nonNull(schema.getEnum());
    }

    @Override
    public String getTypeOrEnum(String name, Schema schema, List<ImportObject> objects, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects) {
        String type;
        if (isEnum(schema)) {
            type = getEnum(objects, schema, enumsToCreate, enumObjects);
        } else {
            type = getType(schema);
        }
        return type;
    }

    @Override
    public String getEnum(Collection<ImportObject> objects, Schema schema, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects) {
        EnumObject enumObject = enumParserService.getEnumObject(schema.getEnum(), enumObjects);
        objects.add(ImportObject.builder().name(enumObject.getName()).importClass("").build());
        enumsToCreate.add(enumObject);
        return enumObject.getName();
    }

    @Override
    public String getArrayTypeOrEnum(Schema schema, Collection<ImportObject> objects, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects) {
        String type;
        if (isEnumArray(schema)) {
            EnumObject enumObject = enumParserService.getEnumObject(getArrayEnums(schema), enumObjects);
            type = enumObject.getName();
            objects.add(ImportObject.builder().name(type).importClass("").build());
            enumsToCreate.add(enumObject);
            type = String.format(LIST_TYPE, type);
        } else {
            type = getArrayType(schema);
        }
        return type;
    }

    @Override
    public String getParameterizedClassType(String cl, String type, List<ImportObject> objects) {
        objects.add(ImportObject.builder().name(cl).build());
        String subClass = ParameterizedClassesUtil.getParameterizationType(type);
        Optional<TypeMapping> typeMappingOptional = getTypeMapping(subClass);
        if (typeMappingOptional.isPresent()) {
            TypeMapping typeMapping = typeMappingOptional.get();
            subClass = typeMapping.getFlutterType();
            if (!typeMapping.getImportClass().isBlank()) {
                objects.add(importObjectMapper.toDto(typeMapping));
            }
        } else {
            objects.add(ImportObject.builder()
                    .name(subClass)
                    .build());
        }
        return String.format(PARAMETERIZED_CLASS_TYPE, cl, subClass);
    }

    private String getObjectName(String ref) {
        return ref.replace(COMPONENTS_SCHEMAS_REF, "");
    }
}
