package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.ImportObject;
import com.example.swaggerparser.entity.TypeMapping;
import com.example.swaggerparser.service.NameConverterService;
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
import java.util.Optional;

import static com.example.swaggerparser.constant.SwaggerConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParametersServiceImpl implements ParametersService {
    private final TypeMappingService typeMappingService;
    private final NameConverterService nameConverterService;

    @Override
    public List<String> getParameters(Operation operation, List<ImportObject> objects) {
        List<String> params = new ArrayList<>();
        getPathAndQueryParams(operation, params);
        getRequestBody(operation, params, objects);
        return params;
    }

    private void getRequestBody(Operation operation, List<String> params, List<ImportObject> objects) {
        if (Objects.nonNull(operation.getRequestBody())) {
            if (Objects.nonNull(operation.getRequestBody().getContent().get(APPLICATION_OCTET_STREAM)) ||
                    Objects.nonNull(operation.getRequestBody().getContent().get(MULTIPART_FORM_DATA))) {
                params.add(FILE_PARAM);
                typeMappingService.getTypeMapping("file").ifPresent(typeMapping -> objects.add(ImportObject.builder()
                                .name(typeMapping.getFlutterType())
                                .importClass(typeMapping.getImportClass())
                        .build()));
            } else {
                Schema applicationJson = operation.getRequestBody().getContent().entrySet().iterator().next().getValue().getSchema();
                String type, name;
                if (Objects.nonNull(applicationJson.getType()) && applicationJson.getType().equals(TYPE_ARRAY)) {
                    type = typeMappingService.getArrayType(applicationJson);
                    name = typeMappingService.getArrayName(applicationJson);
                    typeMappingService.getArrayClass(applicationJson).ifPresent(s -> objects.add(ImportObject.builder().name(s).build()));
                } else {
                    type = typeMappingService.getObjectType(applicationJson);
                    if (type.contains("«")) {
                        String cl = type.substring(0, type.indexOf("«"));
                        objects.add(ImportObject.builder().name(cl).build());
                        String subClass = type.substring(type.indexOf("«") + 1, (type.indexOf("»")));
                        Optional<TypeMapping> typeMappingOptional = typeMappingService.getTypeMapping(subClass);
                        if (typeMappingOptional.isPresent()) {
                            TypeMapping typeMapping = typeMappingOptional.get();
                            subClass = typeMapping.getFlutterType();
                            objects.add(ImportObject.builder()
                                    .name(typeMapping.getFlutterType())
                                    .importClass(typeMapping.getImportClass())
                                    .build());
                        } else {
                            objects.add(ImportObject.builder()
                                    .name(subClass)
                                    .build());
                        }
                        type = String.format("%s<%s>", cl, subClass);
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
                String name = parameter.getName();
                if (name.contains(".")) {
                    String[] strings = name.split("\\.");
                    name = strings[strings.length - 1];
                }
                params.add(String.format(PARAMS, annotation, type, nameConverterService.toLowerCamel(name)));
            });
        }
    }
}
