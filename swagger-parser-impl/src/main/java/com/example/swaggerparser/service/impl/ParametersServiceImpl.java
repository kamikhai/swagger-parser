package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.service.ParametersService;
import com.example.swaggerparser.service.TypeMappingService;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.swaggerparser.constant.SwaggerConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParametersServiceImpl implements ParametersService {
    private final TypeMappingService typeMappingService;

    @Override
    public List<String> getParameters(Operation operation, List<String> objects) {
        List<String> params = new ArrayList<>();
        getPathAndQueryParams(operation, params);
        getRequestBody(operation, params, objects);
        return params;
    }

    private void getRequestBody(Operation operation, List<String> params, List<String> objects) {
        if (Objects.nonNull(operation.getRequestBody())) {
            if (Objects.nonNull(operation.getRequestBody().getContent().get(APPLICATION_OCTET_STREAM)) ||
                    Objects.nonNull(operation.getRequestBody().getContent().get(MULTIPART_FORM_DATA))) {
                params.add(FILE_PARAM);
            } else if (Objects.nonNull(operation.getRequestBody().getContent().get(APPLICATION_JSON))) {
                Schema applicationJson = operation.getRequestBody().getContent().get(APPLICATION_JSON).getSchema();
                String type, name;
                if (Objects.nonNull(applicationJson.getType()) && applicationJson.getType().equals(TYPE_ARRAY)) {
                    type = typeMappingService.getArrayType(applicationJson);
                    name = typeMappingService.getObjectArrayName(applicationJson);
                    typeMappingService.getArrayClass(applicationJson).ifPresent(objects::add);
                } else {
                    type = typeMappingService.getObjectType(applicationJson);
                    name = typeMappingService.getObjectName(applicationJson);
                    objects.add(type);
                }
                params.add(String.format(PARAMS, BODY_PARAM, type, name));
            } else {
                log.error("MIME type application/json not found");
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
                    annotation = String.format(QUERY_PARAM, parameter.getName());
                    if (Objects.isNull(parameter.getSchema().getType())) {
                        type = MAP_PARAMS;
                    } else if (parameter.getSchema().getType().equals(TYPE_ARRAY)) {
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
