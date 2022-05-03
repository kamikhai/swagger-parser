package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.service.MethodService;
import com.example.swaggerparser.service.ParametersService;
import com.example.swaggerparser.service.ReturnTypeService;
import com.google.common.base.CaseFormat;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class MethodServiceImpl implements MethodService {

    private final ParametersService parametersService;
    private final ReturnTypeService returnTypeService;

    private static final Map<String, Function<PathItem, Operation>> operations = Map.of(
            "GET", PathItem::getGet,
            "POST", PathItem::getPost,
            "PUT", PathItem::getPut,
            "DELETE", PathItem::getDelete,
            "PATCH", PathItem::getPatch
    );

    @Override
    public Map<String, List<ApiMethod>> getTagsAndMethods(Paths paths, List<ApiMethod> endpointsToCreate) {
        Map<String, List<ApiMethod>> tags = new HashMap<>();
        paths.forEach((path, pathItem) ->
                operations.forEach((operation, func) -> {
                    Operation o = func.apply(pathItem);
                    if (Objects.nonNull(o) && (Objects.isNull(endpointsToCreate)
                            || endpointsToCreate.contains(ApiMethod.builder().operation(operation).path(path).build()))) {
                        saveToTags(createMethod(o, operation, path), tags);
                    }
                })
        );
        return tags;
    }

    private void saveToTags(ApiMethod method, Map<String, List<ApiMethod>> tags) {
        for (String tag : method.getTags()) {
            tag = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, tag.replace(" ", "-"));
            tags.computeIfAbsent(tag, s -> new ArrayList<>());
            tags.get(tag).add(method);
        }
    }

    private ApiMethod createMethod(Operation o, String operation, String path) {
        ApiMethod method = new ApiMethod();
        method.setOperation(operation);
        method.setPath(path);
        method.setTags(o.getTags());
        List<String> objects = new ArrayList<>();
        method.setReturnType(returnTypeService.getReturnType(o, objects));
        method.setMethodName(CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, o.getOperationId()));
        method.setParameters(parametersService.getParameters(o, objects));
        method.setObjects(objects);
        method.setDescription(o.getSummary());
        return method;
    }
}
