package com.dlp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String BEARER_KEY = "bearerAuth";

    @Bean
    public OpenAPI dlpOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Library Platform API")
                        .version("1.0.0")
                        .description("REST API for the digital library platform with DRM, subscriptions and content delivery"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_KEY))
                .components(new Components().addSecuritySchemes(BEARER_KEY,
                        new SecurityScheme()
                                .name(BEARER_KEY)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

