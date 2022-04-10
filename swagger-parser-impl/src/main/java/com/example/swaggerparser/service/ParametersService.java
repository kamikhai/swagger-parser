package com.example.swaggerparser.service;

import io.swagger.v3.oas.models.Operation;

import java.util.List;

public interface ParametersService {
    List<String> getParameters(Operation operation);
}
