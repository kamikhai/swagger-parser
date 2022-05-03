package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.dto.FlutterObject;
import com.example.swaggerparser.service.FileGeneratorService;
import com.example.swaggerparser.service.TemplateProcessorService;
import com.google.common.base.CaseFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileGeneratorServiceImpl implements FileGeneratorService {
    private final TemplateProcessorService templateProcessorService;

    @Override
    public void generateFiles(Map<String, List<ApiMethod>> tags, String baseUrl, List<FlutterObject> objects) {
        generateClients(tags, baseUrl);
        generateObjectFiles(objects);
    }

    private void generateClients(Map<String, List<ApiMethod>> tags, String baseUrl) {
        tags.entrySet().forEach(tag -> {
            String filename = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, tag.getKey());
            List<String> objects = tag.getValue().stream().map(ApiMethod::getObjects)
                    .flatMap(List::stream).distinct().map(s -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, s))
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
            String filename = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, object.getName());
            List<String> relatedObjects = object.getRelatedObjects().stream().map(s -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, s))
                    .collect(Collectors.toList());
            Map<String, Object> params = Map.of(
                    "file_name", filename,
                    "class_name", object.getName(),
                    "fields", object.getFields(),
                    "objects", relatedObjects
            );
            String content = templateProcessorService.processTemplate(params, "object_template.ftlh");
            writeToFile(content, "result/model/", filename);
        });
    }
}
