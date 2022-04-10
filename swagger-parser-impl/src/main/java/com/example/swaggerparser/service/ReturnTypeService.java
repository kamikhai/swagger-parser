package com.example.swaggerparser.service;

import io.swagger.v3.oas.models.Operation;

public interface ReturnTypeService {
    String getReturnType(Operation operation);
}
