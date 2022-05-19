package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.FlutterObject;
import com.example.swaggerparser.dto.ImportObject;
import io.swagger.v3.oas.models.Components;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ObjectsService {
    Set<FlutterObject> getObjects(Components components, Set<ImportObject> objectToCreate, Map<String, List<String>> enums);
}
