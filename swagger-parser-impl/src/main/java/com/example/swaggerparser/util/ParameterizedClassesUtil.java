package com.example.swaggerparser.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

import static com.example.swaggerparser.constant.SwaggerConstant.PARAMETERIZATION_SYMBOL_1;
import static com.example.swaggerparser.constant.SwaggerConstant.PARAMETERIZATION_SYMBOL_2;

@UtilityClass
public class ParameterizedClassesUtil {

    public static String getParameterizationType(String name) {
        return name.substring(name.indexOf(PARAMETERIZATION_SYMBOL_1) + 1, (name.indexOf(PARAMETERIZATION_SYMBOL_2)));
    }

    public static Pattern getParameterizedClassPattern(String name) {
        return Pattern.compile("^" + name + "«.*»");
    }

    public static boolean isParameterizedClass(String type) {
        return type.contains(PARAMETERIZATION_SYMBOL_1);
    }

    public static String getParameterizedClass(String type) {
        return type.substring(0, type.indexOf(PARAMETERIZATION_SYMBOL_1));
    }
}
