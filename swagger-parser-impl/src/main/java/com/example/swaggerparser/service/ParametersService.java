package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.ImportObject;
import io.swagger.v3.oas.models.Operation;

import java.util.List;
import java.util.Map;

public interface ParametersService {
    List<String> getParameters(Operation operation, List<ImportObject> objects, Map<String, List<String>> enums);
}
