package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.EnumObject;
import com.example.swaggerparser.dto.ImportObject;
import io.swagger.v3.oas.models.Operation;

import java.util.List;
import java.util.Set;

public interface ParametersService {
    List<String> getParameters(Operation operation, List<ImportObject> objects, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects);
}
