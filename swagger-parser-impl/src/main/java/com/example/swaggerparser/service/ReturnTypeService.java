package com.example.swaggerparser.service;

import io.swagger.v3.oas.models.Operation;

import java.util.List;

public interface ReturnTypeService {
    String getReturnType(Operation operation, List<String> objects);
}
