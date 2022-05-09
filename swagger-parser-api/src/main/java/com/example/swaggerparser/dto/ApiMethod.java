package com.example.swaggerparser.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiMethod {

    public interface Short {
    }

    @JsonView(Short.class)
    private String operation;

    @JsonView(Short.class)
    private String path;
    private String returnType;
    private String methodName;
    private List<String> parameters;
    private List<String> tags;

    @JsonView(Short.class)
    private String description;

    private List<ImportObject> objects;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiMethod apiMethod = (ApiMethod) o;
        return operation.equals(apiMethod.operation) && path.equals(apiMethod.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, path);
    }
}
