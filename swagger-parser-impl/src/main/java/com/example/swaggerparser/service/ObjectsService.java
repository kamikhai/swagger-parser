package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.FlutterObject;
import io.swagger.v3.oas.models.Components;

import java.util.List;

public interface ObjectsService {
    List<FlutterObject> getObjects(Components components);
}
