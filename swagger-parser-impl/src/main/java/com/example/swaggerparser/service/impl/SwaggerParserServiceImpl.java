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

    @SneakyThrows
    @Override
    public void parse(List<ApiMethod> endpointsToCreate, MultipartFile file, ByteArrayOutputStream out) {
        OpenAPI openAPI = getOpenAPi(file);
        generateCode(openAPI, endpointsToCreate, out);
    }

    private void generateCode(OpenAPI openAPI, List<ApiMethod> endpointsToCreate, ByteArrayOutputStream out) {
        if (openAPI != null) {
            Map<String, List<String>> enums = new HashMap<>();
            Map<String, List<ApiMethod>> tags = methodService.getTagsAndMethods(openAPI.getPaths(), endpointsToCreate, enums);
            String baseUrl = openAPI.getServers().get(0).getUrl();
            List<ImportObject> objectToCreate = tags.entrySet().stream().map(stringListEntry -> stringListEntry.getValue().stream().map(ApiMethod::getObjects)
                            .flatMap(List::stream)
                            .collect(Collectors.toList())).flatMap(List::stream).distinct()
                    .collect(Collectors.toList());
            Set<FlutterObject> objects = objectsService.getObjects(openAPI.getComponents(), new HashSet<>(objectToCreate), enums);
            fileGeneratorService.generateFiles(tags, baseUrl, objects, enums, out);
        }
    }

    @SneakyThrows
    @Override
    public void parse(List<ApiMethod> endpointsToCreate, String url, ByteArrayOutputStream out) {
        OpenAPI openAPI = getOpenAPi(url);
        generateCode(openAPI, endpointsToCreate, out);
    }

    @Override
    public Map<String, List<ApiMethod>> parseSchema(MultipartFile file) {
        OpenAPI openAPI = getOpenAPi(file);
        if (openAPI != null) {
            return methodService.getTagsAndMethods(openAPI.getPaths(), null, new HashMap<>());
        }
        return Map.of();
    }

    @Override
    public Map<String, List<ApiMethod>> parseSchema(String url) {
        OpenAPI openAPI = getOpenAPi(url);
        if (openAPI != null) {
            return methodService.getTagsAndMethods(openAPI.getPaths(), null, new HashMap<>());
        }
        return Map.of();
    }

    private OpenAPI getOpenAPi(String url) {
        try (BufferedInputStream is = new BufferedInputStream(new URL(url).openStream())) {
            return new OpenAPIParser()
                    .readContents(StreamUtils.copyToString(is, Charset.defaultCharset()), null, new ParseOptions())
                    .getOpenAPI();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private OpenAPI getOpenAPi(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return new OpenAPIParser()
                    .readContents(StreamUtils.copyToString(is, Charset.defaultCharset()), null, new ParseOptions())
                    .getOpenAPI();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
