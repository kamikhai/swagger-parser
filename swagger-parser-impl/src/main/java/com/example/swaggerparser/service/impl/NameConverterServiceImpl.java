package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.service.NameConverterService;
import com.google.common.base.CaseFormat;
import org.springframework.stereotype.Service;

@Service
public class NameConverterServiceImpl implements NameConverterService {

    @Override
    public String toLowerCamel(String name) {
        if (name.contains("-")) {
            return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, name.replace(" ", "-"));
        } else if (name.contains("_")) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.replace(" ", "_"));
        } else {
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name.replace(" ", ""));
        }
    }

    @Override
    public String toUpperCamel(String name) {
        if (name.contains("-")) {
            return CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name.replace(" ", "-"));
        } else if (name.contains("_")) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name.replace(" ", "_"));
        } else {
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name.replace(" ", ""));
        }
    }

    @Override
    public String toLowerUnderscore(String name) {
        if (name.contains("-")) {
            return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, name.replace(" ", "-"));
        } else if (name.contains("_")) {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, name.replace(" ", "_"));
        } else {
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name.replace(" ", ""));
        }
    }
}
