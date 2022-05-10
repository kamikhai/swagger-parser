package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.ImportObject;
import com.example.swaggerparser.entity.TypeMapping;
import com.example.swaggerparser.mapper.ImportObjectMapper;
import com.example.swaggerparser.service.ReturnTypeService;
import com.example.swaggerparser.service.TypeMappingService;
import com.example.swaggerparser.util.ParameterizedClassesUtil;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.swaggerparser.constant.SwaggerConstant.*;

@Service
@RequiredArgsConstructor
public class ReturnTypeServiceImpl implements ReturnTypeService {
    private final TypeMappingService typeMappingService;

    @Override
    public String getReturnType(Operation operation, List<ImportObject> objects) {
        String type;
        if (operation.getResponses().containsKey("200") && operation.getResponses().get("200").getContent().size() > 0) {
            Schema applicationJson = getFirstNonNullSchema(operation);
            if (Objects.nonNull(applicationJson.getType())) {
                type = getSimpleType(objects, applicationJson);
            } else {
                type = getObjectType(objects, applicationJson);
            }
        } else {
            type = "void";
        }
        return String.format(FUTURE_TYPE, type);
    }

    private String getSimpleType(List<ImportObject> objects, Schema applicationJson) {
        String type;
        if (applicationJson.getType().equals(TYPE_ARRAY)) {
            type = typeMappingService.getArrayType(applicationJson);
            typeMappingService.getArrayClass(applicationJson).ifPresent(s -> objects.add(ImportObject.builder().name(s).build()));
        } else if (applicationJson.getType().equals(TYPE_OBJECT)) {
            type = String.format(MAP_TYPE, typeMappingService.getType((Schema) applicationJson.getAdditionalProperties()));
        } else {
            type = typeMappingService.getType(applicationJson);
        }
        return type;
    }

    private String getObjectType(List<ImportObject> objects, Schema applicationJson) {
        String type = typeMappingService.getObjectType(applicationJson);
        if (ParameterizedClassesUtil.isParameterizedClass(type)) {
            String cl = ParameterizedClassesUtil.getParameterizedClass(type);
            type = typeMappingService.getParameterizedClassType(cl, type, objects);
        } else {
            objects.add(ImportObject.builder().name(type).build());
        }
        return type;
    }

    private Schema getFirstNonNullSchema(Operation operation) {
        Optional<Schema> optionalSchema = operation.getResponses().get("200").getContent().entrySet().stream().map(entry -> entry.getValue().getSchema())
                .filter(schema -> Objects.nonNull(schema)).findFirst();
        if (optionalSchema.isPresent()) {
            return optionalSchema.get();
        }
        throw new IllegalArgumentException("Can't find response schema");
    }
}
