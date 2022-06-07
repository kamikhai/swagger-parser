package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.EnumObject;
import com.example.swaggerparser.service.EnumParserService;
import com.example.swaggerparser.service.NameConverterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.swaggerparser.constant.SwaggerConstant.UNDEFINED_ENUM;

@Service
@RequiredArgsConstructor
public class EnumParserServiceImpl implements EnumParserService {

    private final ObjectMapper mapper;
    private final NameConverterService nameConverterService;

    @Override
    public List<EnumObject> parseEnums(String json) {
        Queue<Pair<String, JsonNode>> nodesQueue = new ArrayDeque<>();
        try {
            JsonNode root = mapper.readTree(json);
            nodesQueue.add(Pair.of("", root));

            Map<String, Set<List<String>>> enumsNames = startBFS(nodesQueue);
            return findBestNamesCombination(enumsNames);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Map<String, Set<List<String>>> startBFS(Queue<Pair<String, JsonNode>> nodesQueue) {
        Map<String, Set<List<String>>> enumsNames = new HashMap<>();
        while (!nodesQueue.isEmpty()) {
            Pair<String, JsonNode> pair = nodesQueue.poll();
            JsonNode node = pair.getSecond();
            if (node.get("enum") != null) {
                addEnum(node, pair.getFirst(), enumsNames);
            } else {
                addChildrenToQueue(node, nodesQueue);
            }
        }
        return enumsNames;
    }

    @Override
    public EnumObject getEnumObject(List<String> arrayEnums, List<EnumObject> enumObjects) {
        return enumObjects.stream().filter(enumObject -> enumObject.getEnums().equals(arrayEnums)).findFirst().orElseThrow();
    }

    private List<EnumObject> findBestNamesCombination(Map<String, Set<List<String>>> enumsNames) {
        List<EnumObject> enumObjects = new ArrayList<>();
        saveReadyEnums(enumsNames, enumObjects);
        while (!enumsNames.isEmpty()) {
            Map.Entry<String, Set<List<String>>> entry = getFirst(enumsNames);
            int size = entry.getValue().size();
            for (int i = 1; i <= size; i++) {
                List<String> enums = entry.getValue().stream().findFirst().orElseThrow();
                enumObjects.add(EnumObject.builder()
                        .name(nameConverterService.toUpperCamel(entry.getKey() + i))
                        .enums(enums)
                        .build());
                removeEnum(enumsNames, enums);
            }
            enumsNames.remove(entry.getKey());
            saveReadyEnums(enumsNames, enumObjects);
        }
        return enumObjects;
    }

    private void saveReadyEnums(Map<String, Set<List<String>>> enumsNames, List<EnumObject> enumObjects) {
        while (containsOneList(enumsNames)) {
            Map.Entry<String, Set<List<String>>> entry = getFirstOneList(enumsNames);
            List<String> enums = entry.getValue().stream().findFirst().orElseThrow();
            enumObjects.add(EnumObject.builder()
                    .name(nameConverterService.toUpperCamel(entry.getKey()))
                    .enums(enums)
                    .build());
            removeEnum(enumsNames, enums);
            enumsNames.remove(entry.getKey());
        }
    }

    private void removeEnum(Map<String, Set<List<String>>> enumsNames, List<String> enums) {
        List<String> toDelete = new ArrayList<>();
        enumsNames.forEach((key, value) -> {
            if (value.contains(enums)) {
                value.remove(enums);
                if (value.isEmpty()) {
                    toDelete.add(key);
                }
            }
        });
        toDelete.forEach(enumsNames::remove);
    }

    private boolean containsOneList(Map<String, Set<List<String>>> enumsNames) {
        return enumsNames.entrySet().stream().anyMatch(entry -> entry.getValue().size() == 1);
    }

    private Map.Entry<String, Set<List<String>>> getFirstOneList(Map<String, Set<List<String>>> enumsNames) {
        return enumsNames.entrySet().stream().filter(entry -> entry.getValue().size() == 1).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can't find list with size one"));
    }

    private Map.Entry<String, Set<List<String>>> getFirst(Map<String, Set<List<String>>> enumsNames) {
        return enumsNames.entrySet().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no elements"));
    }

    private void addEnum(JsonNode node, String nodeName, Map<String, Set<List<String>>> enumsNames) {
        String name = UNDEFINED_ENUM;
        if (nodeName.isBlank()) {
            name = node.get("name").asText();
        } else if (!List.of("schema", "items").contains(nodeName)) {
            name = nodeName;
        }
        List<String> enums = getEnums(node);
        enumsNames.putIfAbsent(name, new HashSet<>());
        enumsNames.get(name).add(enums);
    }

    private void addChildrenToQueue(JsonNode node, Queue<Pair<String, JsonNode>> nodesQueue) {
        Iterator<String> fieldNames = node.fieldNames();
        if (fieldNames.hasNext()) {
            fieldNames.forEachRemaining(s -> nodesQueue.add(Pair.of(s, node.get(s))));
        } else {
            Iterator<JsonNode> jsonNodeIterator = node.elements();
            while (jsonNodeIterator.hasNext()) {
                nodesQueue.add(Pair.of("", jsonNodeIterator.next()));
            }
        }
    }

    private List<String> getEnums(JsonNode node) {
        List<String> enums = new ArrayList<>();
        node.get("enum").forEach(enumNode -> enums.add(enumNode.asText()));
        return enums;
    }
}