package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.EnumObject;
import com.example.swaggerparser.dto.ImportObject;
import com.example.swaggerparser.entity.TypeMapping;
import io.swagger.v3.oas.models.media.Schema;

import java.util.*;

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

    boolean isEnumArray(Schema schema);

    List<String> getArrayEnums(Schema schema);

    boolean isEnum(Schema schema);

    String getTypeOrEnum(String name, Schema schema, List<ImportObject> objects, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects);

    String getEnum(Collection<ImportObject> objects, Schema schema, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects);

    String getArrayTypeOrEnum(Schema schema, Collection<ImportObject> objects, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects);

    String getParameterizedClassType(String cl, String type, List<ImportObject> objects);
}
