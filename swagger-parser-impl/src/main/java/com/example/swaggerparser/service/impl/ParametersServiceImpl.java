package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.EnumObject;
import com.example.swaggerparser.dto.ImportObject;
import com.example.swaggerparser.service.NameConverterService;
import com.example.swaggerparser.service.ParametersService;
import com.example.swaggerparser.service.TypeMappingService;
import com.example.swaggerparser.util.ParameterizedClassesUtil;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.example.swaggerparser.constant.SwaggerConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParametersServiceImpl implements ParametersService {
    private final TypeMappingService typeMappingService;
    private final NameConverterService nameConverterService;

    @Override
    public List<String> getParameters(Operation operation, List<ImportObject> objects, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects) {
        List<String> params = new ArrayList<>();
        getPathAndQueryParams(operation, params, enumsToCreate, objects, enumObjects);
        getRequestBody(operation, params, objects);
        return params;
    }

    private void getRequestBody(Operation operation, List<String> params, List<ImportObject> objects) {
        if (Objects.nonNull(operation.getRequestBody())) {
            if (Objects.nonNull(operation.getRequestBody().getContent().get(APPLICATION_OCTET_STREAM)) ||
                    Objects.nonNull(operation.getRequestBody().getContent().get(MULTIPART_FORM_DATA))) {
                addFileParam(params, objects);
            } else {
                Schema applicationJson = operation.getRequestBody().getContent().entrySet().iterator().next().getValue().getSchema();
                String type, name;
                if (Objects.nonNull(applicationJson.getType()) && applicationJson.getType().equals(TYPE_ARRAY)) {
                    name = typeMappingService.getArrayName(applicationJson);
                    type = typeMappingService.getArrayType(applicationJson);
                    typeMappingService.getArrayClass(applicationJson).ifPresent(s -> objects.add(ImportObject.builder().name(s).build()));
                } else {
                    type = typeMappingService.getObjectType(applicationJson);
                    if (ParameterizedClassesUtil.isParameterizedClass(type)) {
                        String cl = ParameterizedClassesUtil.getParameterizedClass(type);
                        type = typeMappingService.getParameterizedClassType(cl, type, objects);
                        name = nameConverterService.toLowerCamel(cl);
                    } else {
                        objects.add(ImportObject.builder().name(type).build());
                        name = typeMappingService.getObjectName(applicationJson);
                    }
                }
                params.add(String.format(PARAMS, BODY_PARAM, type, name));
            }
        }
    }

    private void addFileParam(List<String> params, List<ImportObject> objects) {
        params.add(FILE_PARAM);
        typeMappingService.getTypeMapping("file").ifPresent(typeMapping -> objects.add(ImportObject.builder()
                .name(typeMapping.getFlutterType())
                .importClass(typeMapping.getImportClass())
                .build()));
    }

    private void getPathAndQueryParams(Operation operation, List<String> params, Set<EnumObject> enumsToCreate, List<ImportObject> objects, List<EnumObject> enumObjects) {
        if (Objects.nonNull(operation.getParameters())) {
            operation.getParameters().forEach(parameter -> {
                String annotation = "";
                String type = "";
                if (parameter.getIn().equals("path")) {
                    annotation = PATH_PARAM;
                    type = typeMappingService.getTypeOrEnum(parameter.getName(), parameter.getSchema(), objects, enumsToCreate, enumObjects);
                } else if (parameter.getIn().equals("query")) {
                    annotation = String.format(QUERY_PARAM, parameter.getName());
                    type = getQueryParamType(parameter, objects, enumsToCreate, enumObjects);
                } else if (parameter.getIn().equals("header")) {
                    return;
                }
                params.add(String.format(PARAMS, annotation, type, getParamName(parameter)));
            });
        }
    }

    private String getParamName(Parameter parameter) {
        String name = parameter.getName();
        if (name.contains(".")) {
            String[] strings = name.split("\\.");
            name = strings[strings.length - 1];
        }
        return nameConverterService.toLowerCamel(name);
    }

    private String getQueryParamType(Parameter parameter, List<ImportObject> objects, Set<EnumObject> enumsToCreate, List<EnumObject> enumObjects) {
        String type;
        if (Objects.isNull(parameter.getSchema().getType())) {
            type = MAP_PARAMS;
        } else if (parameter.getSchema().getType().equals(TYPE_ARRAY)) {
            type = typeMappingService.getArrayTypeOrEnum(parameter.getSchema(), objects, enumsToCreate, enumObjects);
        } else {
            type = typeMappingService.getTypeOrEnum(parameter.getName(), parameter.getSchema(), objects, enumsToCreate, enumObjects);
        }
        return type;
    }
}
