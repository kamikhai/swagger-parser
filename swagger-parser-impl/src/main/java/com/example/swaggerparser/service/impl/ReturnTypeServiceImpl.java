package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.ImportObject;
import com.example.swaggerparser.entity.TypeMapping;
import com.example.swaggerparser.service.ReturnTypeService;
import com.example.swaggerparser.service.TypeMappingService;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.swaggerparser.constant.SwaggerConstant.*;

@Service
@RequiredArgsConstructor
public class ReturnTypeServiceImpl implements ReturnTypeService {
    private final TypeMappingService typeMappingService;

    @Override
    public String getReturnType(Operation operation, List<ImportObject> objects) {
        String type;
        if (operation.getResponses().containsKey("200") && operation.getResponses().get("200").getContent().size() > 0) {
            Schema applicationJson = getFirstNonNullSchema(operation);
            if (Objects.nonNull(applicationJson.getType())) {
                if (applicationJson.getType().equals(TYPE_ARRAY)) {
                    type = typeMappingService.getArrayType(applicationJson);
                    typeMappingService.getArrayClass(applicationJson).ifPresent(s -> objects.add(ImportObject.builder().name(s).build()));
                } else if (applicationJson.getType().equals(TYPE_OBJECT)) {
                    type = String.format(MAP_TYPE, typeMappingService.getType((Schema) applicationJson.getAdditionalProperties()));
                } else {
                    type = typeMappingService.getType(applicationJson);
                }
            } else {
                type = typeMappingService.getObjectType(applicationJson);
                if (type.contains("«")) {
                    String cl = type.substring(0, type.indexOf("«"));
                    objects.add(ImportObject.builder().name(cl).build());
                    String subtype = type.substring(type.indexOf("«") + 1, (type.indexOf("»")));
                    Optional<TypeMapping> typeMappingOptional = typeMappingService.getTypeMapping(subtype);
                    if (typeMappingOptional.isPresent()) {
                        TypeMapping typeMapping = typeMappingOptional.get();
                        subtype = typeMapping.getFlutterType();
                        objects.add(ImportObject.builder()
                                .name(typeMapping.getFlutterType())
                                .importClass(typeMapping.getImportClass())
                                .build());
                    } else {
                        objects.add(ImportObject.builder()
                                .name(subtype)
                                .build());
                    }
                    type = String.format("%s<%s>", cl, subtype);
                } else {
                    objects.add(ImportObject.builder().name(type).build());
                }
            }
        } else {
            type = "void";
        }
        return String.format(FUTURE_TYPE, type);
    }

    private Schema getFirstNonNullSchema(Operation operation) {
        Optional<Schema> optionalSchema = operation.getResponses().get("200").getContent().entrySet().stream().map(entry -> entry.getValue().getSchema())
                .filter(schema -> Objects.nonNull(schema)).findFirst();
        if (optionalSchema.isPresent()) {
            return optionalSchema.get();
        }
        throw new IllegalArgumentException("Can't find response schema");
    }
}
