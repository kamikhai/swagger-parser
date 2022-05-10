package com.example.swaggerparser.mapper;

import com.example.swaggerparser.dto.ImportObject;
import com.example.swaggerparser.entity.TypeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ImportObjectMapper {

    @Mapping(target = "name", source = "flutterType")
    ImportObject toDto(TypeMapping typeMapping);
}
