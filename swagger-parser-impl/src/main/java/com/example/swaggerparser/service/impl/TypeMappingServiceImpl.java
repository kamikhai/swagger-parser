package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.ImportObject;
import com.example.swaggerparser.entity.TypeMapping;
import com.example.swaggerparser.mapper.ImportObjectMapper;
import com.example.swaggerparser.repository.TypeMappingRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TypeMappingServiceImpl implements TypeMappingService {

    private final TypeMappingRepository typeMappingRepository;
    private final NameConverterService nameConverterService;
    private final ImportObjectMapper importObjectMapper;

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
    public String getTypeOrEnum(String name, Schema schema, List<ImportObject> objects, Map<String, List<String>> enums) {
        String type;
        if (isEnum(schema)) {
            type = getEnum(name, objects, schema, enums);
        } else {
            type = getType(schema);
        }
        return type;
    }

    @Override
    public String getEnum(String name, Collection<ImportObject> objects, Schema schema, Map<String, List<String>> enums) {
        String type = nameConverterService.toUpperCamel(name);
        objects.add(ImportObject.builder().name(type).build());
        enums.put(type, schema.getEnum());
        return type;
    }

    @Override
    public String getArrayTypeOrEnum(String name, Schema schema, Collection<ImportObject> objects, Map<String, List<String>> enums) {
        String type;
        if (isEnumArray(schema)) {
            type = nameConverterService.toUpperCamel(name);
            objects.add(ImportObject.builder().name(type).build());
            enums.put(type, getArrayEnums(schema));
            type = String.format(LIST_TYPE, type);
        } else {
            type = getSimpleArrayType(schema);
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
            objects.add(importObjectMapper.toDto(typeMapping));
        } else {
            objects.add(ImportObject.builder()
                    .name(subClass)
                    .build());
        }
        return String.format(PARAMETERIZED_CLASS_TYPE, cl, subClass);
    }

    private String getObjectName(String ref) {
        return ref.replace(OBJECTS_PATH, "");
    }
}
