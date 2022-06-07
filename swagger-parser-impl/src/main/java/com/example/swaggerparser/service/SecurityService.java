package com.example.swaggerparser.service;

import com.example.swaggerparser.dto.SecurityInfo;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.Map;

public interface SecurityService {
    Map<String, SecurityInfo> getSecurityInfo(Map<String, SecurityScheme> security);

}
