package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.dto.FlutterObject;
import com.example.swaggerparser.dto.ImportObject;
import com.example.swaggerparser.service.FileGeneratorService;
import com.example.swaggerparser.service.MethodService;
import com.example.swaggerparser.service.ObjectsService;
import com.example.swaggerparser.service.SwaggerParserService;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SwaggerParserServiceImpl implements SwaggerParserService {

    private final MethodService methodService;
    private final ObjectsService objectsService;
    private final FileGeneratorService fileGeneratorService;

    @SneakyThrows
    @Override
    public void parse(List<ApiMethod> endpointsToCreate) {
        OpenAPI openAPI = getOpenAPi();
        if (openAPI != null) {
            Map<String, List<String>> enums = new HashMap<>();
            Map<String, List<ApiMethod>> tags = methodService.getTagsAndMethods(openAPI.getPaths(), endpointsToCreate, enums);
            String baseUrl = openAPI.getServers().get(0).getUrl();
            List<ImportObject> objectToCreate = tags.entrySet().stream().map(stringListEntry -> stringListEntry.getValue().stream().map(ApiMethod::getObjects)
                            .flatMap(List::stream)
                            .collect(Collectors.toList())).flatMap(List::stream).distinct()
                    .collect(Collectors.toList());
            List<FlutterObject> objects = objectsService.getObjects(openAPI.getComponents(), new HashSet<>(objectToCreate), enums);
            fileGeneratorService.generateFiles(tags, baseUrl, objects, enums);
        }
    }

    @Override
    public Map<String, List<ApiMethod>> parseSchema() {
        OpenAPI openAPI = getOpenAPi();
        if (openAPI != null) {
            return methodService.getTagsAndMethods(openAPI.getPaths(), null, new HashMap<>());
        }
        return Map.of();
    }

    private OpenAPI getOpenAPi() {
        SwaggerParseResult result = new OpenAPIParser().readLocation("swagger_work_1.json", null, null);

        OpenAPI openAPI = result.getOpenAPI();

        if (result.getMessages() != null)
            result.getMessages().forEach(System.err::println);
        return openAPI;
    }
}
