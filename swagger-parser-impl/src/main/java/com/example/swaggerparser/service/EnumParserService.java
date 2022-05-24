package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.EnumObject;

import java.util.List;

public interface EnumParserService {
    List<EnumObject> parseEnums(String json);

    EnumObject getEnumObject(List<String> arrayEnums, List<EnumObject> enumObjects);
}
