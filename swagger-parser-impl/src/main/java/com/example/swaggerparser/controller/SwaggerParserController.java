package com.example.swaggerparser.controller;

import com.example.swaggerparser.api.SwaggerParserApi;
import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.service.SwaggerParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SwaggerParserController implements SwaggerParserApi {
    private final SwaggerParserService swaggerParserService;

    @Override
    public void parse(List<ApiMethod> endpointsToCreate) {
        swaggerParserService.parse(endpointsToCreate);
    }

    @Override
    public Map<String, List<ApiMethod>> parseSchema() {
        return swaggerParserService.parseSchema();
    }
}
