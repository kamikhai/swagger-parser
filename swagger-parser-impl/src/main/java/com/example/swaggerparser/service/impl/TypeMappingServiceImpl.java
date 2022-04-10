package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.entity.TypeMapping;
import com.example.swaggerparser.repository.TypeMappingRepository;
import com.example.swaggerparser.service.TypeMappingService;
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

    @Override
    public String getObjectArrayType(Schema schema) {
        return String.format("List<%s>", getObjectName(((ArraySchema) schema).getItems().get$ref()));
    }

    @Override
    public String getArrayType(Schema schema) {
        if (Objects.nonNull(((ArraySchema)schema).getItems().getType())) {
            return getSimpleArrayType(schema);
        } else {
            return getObjectArrayType(schema);
        }
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
        return getObjectName(schema.get$ref()).toLowerCase();
    }

    @Override
    public String getObjectArrayName(Schema schema) {
        return getObjectName(((ArraySchema) schema).getItems().get$ref()).toLowerCase();
    }

    private String getObjectName(String ref) {
        return ref.replace(OBJECTS_PATH, "");
    }
}
