package com.example.swaggerparser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlutterObject {

    String name;
    List<ObjectField> fields;
    Set<String> relatedObjects;
}
