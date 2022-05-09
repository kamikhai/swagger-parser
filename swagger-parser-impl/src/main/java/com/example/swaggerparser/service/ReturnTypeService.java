package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.ImportObject;
import io.swagger.v3.oas.models.Operation;

import java.util.List;

public interface ReturnTypeService {
    String getReturnType(Operation operation, List<ImportObject> objects);
}
