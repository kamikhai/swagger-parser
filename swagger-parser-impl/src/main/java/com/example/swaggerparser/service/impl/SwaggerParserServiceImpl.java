package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.dto.EnumObject;
import com.example.swaggerparser.dto.FlutterObject;
import com.example.swaggerparser.dto.ImportObject;
import com.example.swaggerparser.service.*;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SwaggerParserServiceImpl implements SwaggerParserService {

    private final MethodService methodService;
    private final ObjectsService objectsService;
    private final FileGeneratorService fileGeneratorService;
    private final EnumParserService enumParserService;

    @SneakyThrows
    @Override
    public void parse(List<ApiMethod> endpointsToCreate, MultipartFile file, ByteArrayOutputStream out) {
        String json = getJsonFromFile(file);
        OpenAPI openAPI = getOpenAPi(json);
        generateCode(openAPI, endpointsToCreate, out, json);
    }

    @SneakyThrows
    @Override
    public void parse(List<ApiMethod> endpointsToCreate, String url, ByteArrayOutputStream out) {
        String json = getJsonFromUrl(url);
        OpenAPI openAPI = getOpenAPi(json);
        generateCode(openAPI, endpointsToCreate, out, json);
    }

    private void generateCode(OpenAPI openAPI, List<ApiMethod> endpointsToCreate, ByteArrayOutputStream out, String json) {
        if (openAPI != null) {
            List<EnumObject> enumObjects = enumParserService.parseEnums(json);
            Set<EnumObject> enumsToCreate = new HashSet<>();
            Map<String, List<ApiMethod>> tags = methodService.getTagsAndMethods(openAPI.getPaths(), endpointsToCreate, enumsToCreate, enumObjects);
            String baseUrl = openAPI.getServers().get(0).getUrl();
            List<ImportObject> objectToCreate = tags.entrySet().stream().map(stringListEntry -> stringListEntry.getValue().stream().map(ApiMethod::getObjects)
                            .flatMap(List::stream)
                            .collect(Collectors.toList())).flatMap(List::stream).distinct()
                    .collect(Collectors.toList());
            Set<FlutterObject> objects = objectsService.getObjects(openAPI.getComponents(), new HashSet<>(objectToCreate), enumsToCreate, enumObjects);
            fileGeneratorService.generateFiles(tags, baseUrl, objects, enumsToCreate, out);
        }
    }

    @Override
    public Map<String, List<ApiMethod>> parseSchema(MultipartFile file) {
        String json = getJsonFromFile(file);
        OpenAPI openAPI = getOpenAPi(json);
        if (openAPI != null) {
            List<EnumObject> enumObjects = enumParserService.parseEnums(json);
            return methodService.getTagsAndMethods(openAPI.getPaths(), null, new HashSet<>(), enumObjects);
        }
        return Map.of();
    }

    @Override
    public Map<String, List<ApiMethod>> parseSchema(String url) {
        String json = getJsonFromUrl(url);
        OpenAPI openAPI = getOpenAPi(json);
        if (openAPI != null) {
            List<EnumObject> enumObjects = enumParserService.parseEnums(json);
            return methodService.getTagsAndMethods(openAPI.getPaths(), null, new HashSet<>(), enumObjects);
        }
        return Map.of();
    }

    private String getJsonFromFile(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return StreamUtils.copyToString(is, Charset.defaultCharset());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String getJsonFromUrl(String url) {
        try (BufferedInputStream is = new BufferedInputStream(new URL(url).openStream())) {
            return StreamUtils.copyToString(is, Charset.defaultCharset());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private OpenAPI getOpenAPi(String json) {
        return new OpenAPIParser()
                .readContents(json, null, new ParseOptions())
                .getOpenAPI();

    }
}
