package com.example.swaggerparser.service.impl;

import com.example.swaggerparser.dto.SecurityInfo;
import com.example.swaggerparser.service.SecurityService;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Override
    public Map<String, SecurityInfo> getSecurityInfo(Map<String, SecurityScheme> security) {
        Map<String, SecurityInfo> securityInfo = new HashMap<>();
        if (Objects.nonNull(security)) {
            security.forEach((key, value) -> {
                if (value.getType().equals(SecurityScheme.Type.APIKEY)) {
                    securityInfo.put(key, SecurityInfo.builder()
                            .in(value.getIn().toString())
                            .paramName(value.getName())
                            .build());
                }
            });
        }
        return securityInfo;
    }
}
