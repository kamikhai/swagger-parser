package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.EnumObject;
import com.example.swaggerparser.dto.FlutterObject;
import com.example.swaggerparser.dto.ImportObject;
import io.swagger.v3.oas.models.Components;

import java.util.List;
import java.util.Set;

public interface ObjectsService {
    Set<FlutterObject> getObjects(Components components, List<ImportObject> objectToCreate, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects);
}
