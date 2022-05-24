package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.EnumObject;
import com.example.swaggerparser.dto.ImportObject;
import io.swagger.v3.oas.models.Operation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ReturnTypeService {
    String getReturnType(Operation operation, List<ImportObject> objects, Set<EnumObject> enums, List<EnumObject> enumObjects);
}
