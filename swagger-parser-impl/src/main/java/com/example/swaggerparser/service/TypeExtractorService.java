package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.ImportObject;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

public interface TypeExtractorService {
    String getTypeFromSchema(Schema firstNonNullSchema, List<ImportObject> objects);
}
