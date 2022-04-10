package com.example.swaggerparser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiMethod {
    private String operation;
    private String path;
    private String returnType;
    private String methodName;
    private List<String> parameters;
    private List<String> tags;
}
