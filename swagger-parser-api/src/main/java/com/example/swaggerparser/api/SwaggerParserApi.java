package com.example.swaggerparser.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1")
public interface SwaggerParserApi {

    @GetMapping("/parse")
    void parse();
}
