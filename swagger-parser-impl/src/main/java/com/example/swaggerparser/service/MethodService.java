package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.dto.EnumObject;
import io.swagger.v3.oas.models.Paths;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MethodService {
    Map<String, List<ApiMethod>> getTagsAndMethodsExtended(Paths paths, List<ApiMethod> endpointsToCreate, Set<EnumObject> enums, List<EnumObject> enumObjects);

    Map<String, List<ApiMethod>> getTagsAndMethods(Paths paths);
}
