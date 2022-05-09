package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.dto.FlutterObject;

import java.util.List;
import java.util.Map;

public interface FileGeneratorService {

    void generateFiles(Map<String, List<ApiMethod>> tags, String baseUrl, List<FlutterObject> objects, Map<String, List<String>> enums);
}
