package com.example.swaggerparser.service;

import java.util.Map;

public interface TemplateProcessorService {
    String processTemplate(Map<String, Object> params, String templateName);
}
