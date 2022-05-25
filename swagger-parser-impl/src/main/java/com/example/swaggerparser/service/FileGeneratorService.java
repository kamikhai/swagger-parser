package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.dto.EnumObject;
import com.example.swaggerparser.dto.FlutterObject;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FileGeneratorService {

    void generateFiles(Map<String, List<ApiMethod>> tags, String baseUrl, Set<FlutterObject> objects,
                       Set<EnumObject> enumsToCreate, ByteArrayOutputStream out);
}
