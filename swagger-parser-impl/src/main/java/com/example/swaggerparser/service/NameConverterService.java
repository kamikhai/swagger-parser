package com.example.swaggerparser.service;

public interface NameConverterService {

    String toLowerCamel(String name);

    String toUpperCamel(String name);

    String toLowerUnderscore(String name);
}
