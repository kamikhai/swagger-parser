package com.example.swaggerparser.api;

import com.example.swaggerparser.dto.ApiMethod;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/v1")
public interface SwaggerParserApi {

    @GetMapping("/parse")
    void parse(@JsonView(ApiMethod.Short.class) @RequestBody List<ApiMethod> endpointsToCreate);

    @JsonView(ApiMethod.Short.class)
    @GetMapping("/parse/schema")
    Map<String, List<ApiMethod>> parseSchema();
}
