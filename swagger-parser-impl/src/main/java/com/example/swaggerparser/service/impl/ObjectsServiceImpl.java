package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.FlutterObject;
import com.example.swaggerparser.dto.ObjectField;
import com.example.swaggerparser.service.ObjectsService;
import com.example.swaggerparser.service.TypeMappingService;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import static com.example.swaggerparser.constant.SwaggerConstant.TYPE_ARRAY;

@Service
@RequiredArgsConstructor
public class ObjectsServiceImpl implements ObjectsService {
    private final TypeMappingService typeMappingService;

    @Override
    public List<FlutterObject> getObjects(Components components) {
        List<FlutterObject> objects = new ArrayList<>();
        components.getSchemas().forEach((name, object) -> {
            List<ObjectField> fields = new ArrayList<>();
            object.getProperties().forEach((BiConsumer<String, Schema>) (s, schema) -> {
                String type;
                if (Objects.isNull(schema.getType())) {
                    type = typeMappingService.getObjectType(schema);
                } else if (schema.getType().equals(TYPE_ARRAY)) {
                    type = typeMappingService.getArrayType(schema);
                } else {
                    type = typeMappingService.getType(schema);
                }
                fields.add(new ObjectField(s, type));
            });
            objects.add(new FlutterObject(name, fields));
        });
        return objects;
    }
}
