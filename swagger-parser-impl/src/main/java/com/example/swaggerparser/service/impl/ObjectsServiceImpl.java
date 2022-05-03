package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.FlutterObject;
import com.example.swaggerparser.dto.ObjectField;
import com.example.swaggerparser.service.ObjectsService;
import com.example.swaggerparser.service.TypeMappingService;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;

import static com.example.swaggerparser.constant.SwaggerConstant.TYPE_ARRAY;

@Service
@RequiredArgsConstructor
public class ObjectsServiceImpl implements ObjectsService {
    private final TypeMappingService typeMappingService;

    @Override
    public List<FlutterObject> getObjects(Components components, Set<String> objectToCreate) {
        List<FlutterObject> objects = new ArrayList<>();
        Map<String, Schema> all = components.getSchemas();

        while (!objectToCreate.isEmpty()) {
            String name = objectToCreate.stream().findFirst().get();
            if (all.containsKey(name)) {
                Schema object = all.get(name);
                Set<String> relatedObjects = new HashSet<>();
                List<ObjectField> fields = new ArrayList<>();
                if (object instanceof ComposedSchema) {
                    object = ((ComposedSchema) object).getAllOf().stream().filter(ObjectSchema.class::isInstance)
                            .findFirst().orElse(null);
                }
                if (Objects.nonNull(object)) {
                    List<String> required = Objects.nonNull(object.getRequired()) ? object.getRequired() : List.of();
                    object.getProperties().forEach((BiConsumer<String, Schema>) (s, schema) -> {
                        String type;
                        if (Objects.isNull(schema.getType())) {
                            type = typeMappingService.getObjectType(schema);
                            relatedObjects.add(type);
                            objectToCreate.add(type);
                        } else if (schema.getType().equals(TYPE_ARRAY)) {
                            type = typeMappingService.getArrayType(schema);
                            typeMappingService.getArrayClass(schema).ifPresent(cl -> {
                                relatedObjects.add(cl);
                                objectToCreate.add(cl);
                            });
                        } else {
                            type = typeMappingService.getType(schema);
                        }
                        fields.add(new ObjectField(s, type, required.contains(s)));
                    });
                }
                objects.add(new FlutterObject(name.replace(" ", ""), fields, relatedObjects));
                all.remove(name);
            }
            objectToCreate.remove(name);
        }
        return objects;
    }
}
