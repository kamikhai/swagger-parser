package com.example.swaggerparser.controller;

import com.example.swaggerparser.api.SwaggerParserApi;
import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.service.SwaggerParserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class SwaggerParserController implements SwaggerParserApi {
    private final SwaggerParserService swaggerParserService;

    public static HttpHeaders createHttpHeaders(String name, String contentType, Integer size) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + URLEncoder.encode(name, StandardCharsets.UTF_8));
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(size));
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");
        return headers;
    }

    @Override
    public ResponseEntity<Resource> parse(String endpointsToCreate, MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<ApiMethod> apiMethods = null;
        List<Map<String, String>> objects = null;
        try {
            objects = objectMapper.readValue(endpointsToCreate, List.class);
            apiMethods = objects.stream().map(m -> ApiMethod.builder()
                    .path(m.get("path"))
                    .operation(m.get("operation"))
                    .build()).collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            swaggerParserService.parse(apiMethods, file, out);
            return ResponseEntity.ok()
                    .headers(createHttpHeaders("generated_clients.zip", MediaType.APPLICATION_OCTET_STREAM_VALUE, out.size()))
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                    .body(new ByteArrayResource(out.toByteArray()));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public ResponseEntity<Resource> parse(List<ApiMethod> apiMethods, String url) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            swaggerParserService.parse(apiMethods, url, out);
            return ResponseEntity.ok()
                    .headers(createHttpHeaders("generated_clients.zip", MediaType.APPLICATION_OCTET_STREAM_VALUE, out.size()))
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                    .body(new ByteArrayResource(out.toByteArray()));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Map<String, List<ApiMethod>> parseSchema(MultipartFile file) {
        return swaggerParserService.parseSchema(file);
    }

    @Override
    public Map<String, List<ApiMethod>> parseSchema(String url) {
        return swaggerParserService.parseSchema(url);
    }
}
