package com.hospital.hms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hospitalOpenAPI() {

        // Define JWT Bearer token security scheme
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("Hospital Management System API")
                        .description(
                                "Production-grade REST API for " +
                                        "managing hospital operations - " +
                                        "patients, doctors, appointments, " +
                                        "billing, and medical records.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Shashi")
                                .email("shashi@hospital.com"))
                        .license(new License()
                                .name("MIT License")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development")
                ))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes(
                                "Bearer Authentication",
                                bearerScheme));
    }
}