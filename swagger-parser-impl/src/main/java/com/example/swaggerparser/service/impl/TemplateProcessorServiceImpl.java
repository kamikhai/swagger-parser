package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.service.TemplateProcessorService;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateProcessorServiceImpl implements TemplateProcessorService {
    private final Configuration freeMarkerConfigurationFactoryBean;

    public String processTemplate(Map<String, Object> params, String templateName) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(
                    freeMarkerConfigurationFactoryBean.getTemplate(templateName, "UTF-8"), params);
        } catch (IOException | TemplateException e) {
            throw new IllegalArgumentException(e);
        }

    }
}
