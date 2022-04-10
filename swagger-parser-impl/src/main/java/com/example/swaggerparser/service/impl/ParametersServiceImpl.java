package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.service.ParametersService;
import com.example.swaggerparser.service.TypeMappingService;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.swaggerparser.constant.SwaggerConstant.*;

@Service
@RequiredArgsConstructor
public class ParametersServiceImpl implements ParametersService {
    private final TypeMappingService typeMappingService;

    @Override
    public List<String> getParameters(Operation operation) {
        List<String> params = new ArrayList<>();
        getPathAndQueryParams(operation, params);
        getRequestBody(operation, params);
        return params;
    }

    private void getRequestBody(Operation operation, List<String> params) {
        if (Objects.nonNull(operation.getRequestBody())) {
            if (Objects.nonNull(operation.getRequestBody().getContent().get(APPLICATION_OCTET_STREAM))) {
                params.add(FILE_PARAM);
            } else {
                Schema applicationJson =  operation.getRequestBody().getContent().get(APPLICATION_JSON).getSchema();
                String type, name;
                if (Objects.nonNull(applicationJson.getType()) && applicationJson.getType().equals(TYPE_ARRAY)) {
                    type = typeMappingService.getObjectArrayType(applicationJson);
                    name = typeMappingService.getObjectArrayName(applicationJson);
                } else {
                    type = typeMappingService.getObjectType(applicationJson);
                    name = typeMappingService.getObjectName(applicationJson);
                }
                params.add(String.format(PARAMS, BODY_PARAM, type, name));
            }
        }
    }

    private void getPathAndQueryParams(Operation operation, List<String> params) {
        if (Objects.nonNull(operation.getParameters())) {
            operation.getParameters().forEach(parameter -> {
                String annotation = "";
                String type = "";
                if (parameter.getIn().equals("path")) {
                    annotation = PATH_PARAM;
                    type = typeMappingService.getType(parameter.getSchema());
                } else if (parameter.getIn().equals("query")) {
                    annotation = QUERY_PARAM;
                    if (parameter.getSchema().getType().equals(TYPE_ARRAY)) {
                        type = typeMappingService.getSimpleArrayType(parameter.getSchema());
                    } else {
                        type = typeMappingService.getType(parameter.getSchema());
                    }
                } else if (parameter.getIn().equals("header")) {
                    return;
                }
                params.add(String.format(PARAMS, annotation, type, parameter.getName()));
            });
        }
    }
}
