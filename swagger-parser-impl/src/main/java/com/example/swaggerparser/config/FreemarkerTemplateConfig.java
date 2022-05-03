package com.example.swaggerparser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

public class FreemarkerTemplateConfig {

    @Bean
    public FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean() {
        FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean = new FreeMarkerConfigurationFactoryBean();
        freeMarkerConfigurationFactoryBean.setPreferFileSystemAccess(false);
        freeMarkerConfigurationFactoryBean.setTemplateLoaderPath("/templates");
        return freeMarkerConfigurationFactoryBean;
    }
}
