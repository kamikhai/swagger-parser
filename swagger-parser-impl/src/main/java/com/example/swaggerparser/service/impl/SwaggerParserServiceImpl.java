package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.FlutterObject;
import com.example.swaggerparser.dto.ApiMethod;
import com.example.swaggerparser.service.MethodService;
import com.example.swaggerparser.service.ObjectsService;
import com.example.swaggerparser.service.SwaggerParserService;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SwaggerParserServiceImpl implements SwaggerParserService {

    private final MethodService methodService;
    private final ObjectsService objectsService;

    @Override
    public void parse() {
        OpenAPI openAPI = getOpenAPi();
        if (openAPI != null) {
            Map<String, List<ApiMethod>> tags = methodService.getTagsAndMethods(openAPI.getPaths());
            String baseUrl = openAPI.getServers().get(0).getUrl();

            tags.entrySet().forEach(tag -> {
                List<ApiMethod> methods = tag.getValue();
                System.out.println("@RestApi(baseUrl: \"" + baseUrl + "\")");
                System.out.println(String.format("abstract class %sClient {%n", tag.getKey()));
                methods.forEach(method -> {
                    System.out.println(String.format("     @%s(\"%s\")", method.getOperation(), method.getPath()));
                    System.out.println(String.format("     %s %s(%s);", method.getReturnType(), method.getMethodName(), method.getParameters().isEmpty() ? "" : String.join(", ", method.getParameters())));
                    System.out.println();
                });
                System.out.println("}");
                System.out.println();
            });

            List<FlutterObject> objects = objectsService.getObjects(openAPI.getComponents());

            objects.forEach(object -> {
                System.out.println("@JsonSerializable()");
                System.out.println(String.format("class %s {", object.getName()));
                System.out.println(String.format("  %s({", object.getName()));
                object.getFields().forEach(field -> {
                    System.out.println(String.format("    required this.%s,", field.getName()));
                });
                System.out.println("  });");
                System.out.println();
                object.getFields().forEach(field -> {
                    System.out.println(String.format("  %s %s;", field.getType(), field.getName()));
                });
                System.out.println();
                System.out.println(String.format("  factory %s.fromJson(Map<String, dynamic> json) => _$%sFromJson(json);",
                        object.getName(), object.getName()));
                System.out.println();
                System.out.println(String.format("  Map<String, dynamic> toJson() => _$%sToJson(this);", object.getName()));
                System.out.println("}");
                System.out.println();
            });
        }
    }

    private OpenAPI getOpenAPi() {
        SwaggerParseResult result = new OpenAPIParser().readLocation("swagger_pet.json", null, null);

        OpenAPI openAPI = result.getOpenAPI();

        if (result.getMessages() != null)
            result.getMessages().forEach(System.err::println);
        return openAPI;
    }
}
