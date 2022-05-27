package com.example.swaggerparser.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SwaggerConstant {
    public static final String APPLICATION_JSON = "application/json";
    public static final String ANY_MEDIA_TYPE = "*/*";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String TYPE_ARRAY = "array";
    public static final String TYPE_OBJECT = "object";
    public static final String FILE_PARAM = "@Part() File file";
    public static final String QUERY_PARAM = "@Query('%s')";
    public static final String PATH_PARAM = "@Path()";
    public static final String BODY_PARAM = "@Body()";
    public static final String PARAMS = "%s %s %s";
    public static final String FUTURE_TYPE = "Future<%s>";
    public static final String MAP_TYPE = "Map<String, %s>";
    public static final String LIST_TYPE = "List<%s>";
    public static final String PARAMETERIZED_CLASS_TYPE = "%s<%s>";
    public static final String MAP_PARAMS = "Map<String, String>";
    public static final String DART_EXTENSION = ".dart";
    public static final String PARAMETERIZATION_SYMBOL_1 = "«";
    public static final String PARAMETERIZATION_SYMBOL_2 = "»";
    public static final String UNDEFINED_ENUM = "UndefinedEnum";

    // Templates
    public static final String CLIENT_TEMPLATE = "client_template.ftlh";
    public static final String ENUM_TEMPLATE = "enum_template.ftlh";
    public static final String OBJECT_TEMPLATE = "object_template.ftlh";
    public static final String PARAMETERIZED_OBJECT_TEMPLATE = "parameterized_object_template.ftlh";

}
