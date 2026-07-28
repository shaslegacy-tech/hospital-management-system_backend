// FILE: com/hospital/hms/config/CorsConfig.java
// Add this file to your Spring Boot config package

package com.hospital.hms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // Allow requests from these origins
                        .allowedOrigins(
                                "http://localhost:3000",      // Local Next.js dev
                                "http://localhost:3001",      // Alternate port
                                "http://127.0.0.1:3000",
                                "http://127.0.0.1:3001",
                                // Add your Vercel deployment URL once deployed:
                                 "https://hms-frontend-kohl.vercel.app"
                        )
                        // Allow all HTTP methods
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        // Allow all headers (Authorization, Content-Type, etc)
                        .allowedHeaders("*")
                        // Allow credentials (JWT tokens in headers)
                        .allowCredentials(true)
                        // Cache CORS preflight response for 1 hour
                        .maxAge(3600);
            }
        };
    }
}