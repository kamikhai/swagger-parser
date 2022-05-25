package com.example.swaggerparser.api;

import com.example.swaggerparser.dto.ApiMethod;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/v1")
public interface SwaggerParserApi {

    @PostMapping("/parse")
    ResponseEntity<Resource> parse(@RequestPart("data") String endpointsToCreate,
                                   @RequestPart(value = "file") MultipartFile file);

    @PostMapping("/parse/url")
    ResponseEntity<Resource> parse(@RequestBody List<ApiMethod> endpointsToCreate,
               @RequestParam(value = "url") String url);

    @JsonView(ApiMethod.Short.class)
    @PostMapping("/parse/schema")
    Map<String, List<ApiMethod>> parseSchema(@RequestParam(value = "file") MultipartFile file);

    @JsonView(ApiMethod.Short.class)
    @PostMapping("/parse/url/schema")
    Map<String, List<ApiMethod>> parseSchema(@RequestParam(value = "url") String url);
}
