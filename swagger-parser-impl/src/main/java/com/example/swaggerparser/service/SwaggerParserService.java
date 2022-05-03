package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.ApiMethod;

import java.util.List;
import java.util.Map;

public interface SwaggerParserService {
    void parse(List<ApiMethod> endpointsToCreate);

    Map<String, List<ApiMethod>> parseSchema();
}
