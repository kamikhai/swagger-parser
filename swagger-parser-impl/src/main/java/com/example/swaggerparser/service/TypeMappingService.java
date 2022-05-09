package com.example.swaggerparser.service;

import com.example.swaggerparser.entity.TypeMapping;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Optional;

public interface TypeMappingService {
    String findFlutterTypeBySwaggerType(String type);

    String getSimpleArrayType(Schema schema);

    String getArrayType(Schema schema);

    Optional<String> getArrayClass(Schema schema);

    String getType(Schema schema);

    String getObjectType(Schema schema);

    String getObjectName(Schema schema);

    String getArrayName(Schema schema);

    Optional<TypeMapping> getTypeMapping(String type);
}
