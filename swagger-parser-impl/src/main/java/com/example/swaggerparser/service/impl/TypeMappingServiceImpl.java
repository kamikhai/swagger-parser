package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.entity.TypeMapping;
import com.example.swaggerparser.repository.TypeMappingRepository;
import com.example.swaggerparser.service.TypeMappingService;
import com.google.common.base.CaseFormat;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static com.example.swaggerparser.constant.SwaggerConstant.OBJECTS_PATH;

@Slf4j
@Service
@RequiredArgsConstructor
public class TypeMappingServiceImpl implements TypeMappingService {

    private final TypeMappingRepository typeMappingRepository;

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
        return String.format("List<%s>", findFlutterTypeBySwaggerType(((ArraySchema) schema).getItems().getType()));
    }

    private String getObjectArrayType(Schema schema) {
        return String.format("List<%s>", getObjectArrayClass(schema));
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
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, getObjectName(schema.get$ref()));
    }

    @Override
    public String getObjectArrayName(Schema schema) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, getObjectName(((ArraySchema) schema).getItems().get$ref()));
    }

    private String getObjectName(String ref) {
        return ref.replace(OBJECTS_PATH, "");
    }
}
