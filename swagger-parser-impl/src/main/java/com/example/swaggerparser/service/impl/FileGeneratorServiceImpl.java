package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.dto.FlutterObject;
import com.example.swaggerparser.service.FileGeneratorService;
import com.example.swaggerparser.service.NameConverterService;
import com.example.swaggerparser.service.TemplateProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileGeneratorServiceImpl implements FileGeneratorService {
    private final TemplateProcessorService templateProcessorService;
    private final NameConverterService nameConverterService;

    @Override
    public void generateFiles(Map<String, List<ApiMethod>> tags, String baseUrl, List<FlutterObject> objects) {
        generateClients(tags, baseUrl);
        generateObjectFiles(objects);
    }

    private void generateClients(Map<String, List<ApiMethod>> tags, String baseUrl) {
        tags.entrySet().forEach(tag -> {
            String filename = nameConverterService.toLowerUnderscore(tag.getKey()) + "_client";
            List<String> objects = tag.getValue().stream().map(ApiMethod::getObjects)
                    .flatMap(List::stream).distinct()
                    .filter(importObject -> Objects.isNull(importObject.getImportClass()) || !importObject.getImportClass().isBlank())
                    .map(importObject -> {
                        if (Objects.isNull(importObject.getImportClass())) {
                            return String.format("model/%s.dart", nameConverterService.toLowerUnderscore(importObject.getName()));
                        } else return importObject.getImportClass();
                    })
                    .collect(Collectors.toList());

            Map<String, Object> params = Map.of(
                    "file_name", filename,
                    "client_name", tag.getKey(),
                    "base_url", baseUrl,
                    "methods", tag.getValue(),
                    "objects", objects
            );
            String content = templateProcessorService.processTemplate(params, "client_template.ftlh");
            writeToFile(content, "result/", filename);
        });
    }

    private void writeToFile(String content, String folder, String filename) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(folder + filename + ".dart"));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void generateObjectFiles(List<FlutterObject> objects) {
        objects.forEach(object -> {
            String filename = nameConverterService.toLowerUnderscore(object.getName());
            List<String> relatedObjects = object.getRelatedObjects().stream()
                    .filter(importObject -> Objects.isNull(importObject.getImportClass()) || !importObject.getImportClass().isBlank())
                    .map(importObject -> {
                        if (Objects.isNull(importObject.getImportClass())) {
                            return String.format("%s.dart", nameConverterService.toLowerUnderscore(importObject.getName()));
                        } else return importObject.getImportClass();
                    })
                    .collect(Collectors.toList());
            Map<String, Object> params = Map.of(
                    "file_name", filename,
                    "class_name", object.getName(),
                    "fields", object.getFields(),
                    "objects", relatedObjects
            );
            String content;
            if (object.isParameterized()) {
                content = templateProcessorService.processTemplate(params, "parameterized_object_template.ftlh");
            } else {
                content = templateProcessorService.processTemplate(params, "object_template.ftlh");
            }
            writeToFile(content, "result/model/", filename);
        });
    }
}
