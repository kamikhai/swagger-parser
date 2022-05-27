package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.dto.EnumObject;
import com.example.swaggerparser.dto.FlutterObject;
import com.example.swaggerparser.service.FileGeneratorService;
import com.example.swaggerparser.service.NameConverterService;
import com.example.swaggerparser.service.TemplateProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.example.swaggerparser.constant.SwaggerConstant.*;

@Service
@RequiredArgsConstructor
public class FileGeneratorServiceImpl implements FileGeneratorService {
    private final TemplateProcessorService templateProcessorService;
    private final NameConverterService nameConverterService;

    @Override
    public void generateFiles(Map<String, List<ApiMethod>> tags, String baseUrl, Set<FlutterObject> objects,
                              Set<EnumObject> enumsToCreate, ByteArrayOutputStream out) {
        ZipOutputStream zipOut = new ZipOutputStream(out);
        generateClients(tags, baseUrl, zipOut);
        generateObjectFiles(objects, zipOut);
        generateEnums(enumsToCreate, zipOut);
        try {
            zipOut.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void generateClients(Map<String, List<ApiMethod>> tags, String baseUrl, ZipOutputStream zipOut) {
        tags.entrySet().forEach(tag -> {
            String filename = nameConverterService.toLowerUnderscore(tag.getKey()) + "_client";
            List<String> objects = tag.getValue().stream().map(ApiMethod::getObjects)
                    .flatMap(List::stream).distinct()
                    .map(importObject -> {
                        if (Objects.isNull(importObject.getImportClass()) || importObject.getImportClass().isBlank()) {
                            return String.format("../model/%s.dart", nameConverterService.toLowerUnderscore(importObject.getName()));
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
            String content = templateProcessorService.processTemplate(params, CLIENT_TEMPLATE);
            writeToFile(content, "result/api/", filename, zipOut);
        });
    }

    private void writeToFile(String content, String folder, String filename, ZipOutputStream zipOut) {
        try {
            ZipEntry zipEntry = new ZipEntry(folder + filename + DART_EXTENSION);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = content.getBytes();
            int length = bytes.length;
            zipOut.write(bytes, 0, length);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void generateObjectFiles(Set<FlutterObject> objects, ZipOutputStream zipOut) {
        objects.forEach(object -> {
            String filename = nameConverterService.toLowerUnderscore(object.getName());
            List<String> relatedObjects = object.getRelatedObjects().stream()
                    .map(importObject -> {
                        if (Objects.isNull(importObject.getImportClass()) || importObject.getImportClass().isBlank()) {
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
            String templateName = object.isParameterized() ? PARAMETERIZED_OBJECT_TEMPLATE : OBJECT_TEMPLATE;
            String content = templateProcessorService.processTemplate(params, templateName);
            writeToFile(content, "result/model/", filename, zipOut);
        });
    }

    private void generateEnums(Set<EnumObject> enumsToCreate, ZipOutputStream zipOut) {
        enumsToCreate.forEach(enumObject -> {
            String filename = nameConverterService.toLowerUnderscore(enumObject.getName());
            Map<String, Object> params = Map.of(
                    "file_name", filename,
                    "class_name", enumObject.getName(),
                    "values", enumObject.getEnums()
            );
            String content = templateProcessorService.processTemplate(params, ENUM_TEMPLATE);
            writeToFile(content, "result/model/", filename, zipOut);
        });
    }
}
