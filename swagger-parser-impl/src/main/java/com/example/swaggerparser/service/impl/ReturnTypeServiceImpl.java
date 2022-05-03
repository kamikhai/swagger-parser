package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.service.ReturnTypeService;
import com.example.swaggerparser.service.TypeMappingService;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.example.swaggerparser.constant.SwaggerConstant.*;

@Service
@RequiredArgsConstructor
public class ReturnTypeServiceImpl implements ReturnTypeService {
    private final TypeMappingService typeMappingService;

    @Override
    public String getReturnType(Operation operation, List<String> objects) {
        String type;
        if (operation.getResponses().containsKey("200") && operation.getResponses().get("200").getContent().size() > 0) {
            Schema applicationJson;
            if (Objects.nonNull(operation.getResponses().get("200").getContent().get(APPLICATION_JSON))) {
                applicationJson = operation.getResponses().get("200").getContent().get(APPLICATION_JSON).getSchema();
            } else {
                applicationJson = operation.getResponses().get("200").getContent().get(ANY_MEDIA_TYPE).getSchema();
            }
            if (Objects.nonNull(applicationJson.getType())) {
                if (applicationJson.getType().equals(TYPE_ARRAY)) {
                    type = typeMappingService.getArrayType(applicationJson);
                    typeMappingService.getArrayClass(applicationJson).ifPresent(objects::add);
                } else if (applicationJson.getType().equals(TYPE_OBJECT)) {
                    type = String.format(MAP_TYPE, typeMappingService.getType((Schema) applicationJson.getAdditionalProperties()));
                } else {
                    type = typeMappingService.getType(applicationJson);
                }
            } else {
                type = typeMappingService.getObjectType(applicationJson);
                objects.add(type);
            }
        } else {
            type = "void";
        }
        return String.format(FUTURE_TYPE, type);
    }
}
