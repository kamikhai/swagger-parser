package com.example.swaggerparser.controller;

import com.example.swaggerparser.api.SwaggerParserApi;
import com.example.swaggerparser.service.SwaggerParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SwaggerParserController implements SwaggerParserApi {
    private final SwaggerParserService swaggerParserService;

    @Override
    public void parse() {
        swaggerParserService.parse();
    }
}
