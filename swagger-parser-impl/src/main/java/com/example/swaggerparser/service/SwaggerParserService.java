package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.ApiMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public interface SwaggerParserService {

    void parse(List<ApiMethod> endpointsToCreate, MultipartFile file, ByteArrayOutputStream out);

    void parse(List<ApiMethod> endpointsToCreate, String url, ByteArrayOutputStream out);

    Map<String, List<ApiMethod>> parseSchema(MultipartFile file);

    Map<String, List<ApiMethod>> parseSchema(String url);
}
