package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.ApiMethod;
import io.swagger.v3.oas.models.Paths;

import java.util.List;
import java.util.Map;

public interface MethodService {
    Map<String, List<ApiMethod>> getTagsAndMethods(Paths paths, List<ApiMethod> endpointsToCreate);
}
