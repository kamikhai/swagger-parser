package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.FlutterObject;
import io.swagger.v3.oas.models.Components;

import java.util.List;
import java.util.Set;

public interface ObjectsService {
    List<FlutterObject> getObjects(Components components, Set<String> objectToCreate);
}
