package com.hospital.hms.config;

import com.hospital.hms.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})   // ✅ enable CORS
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ allow preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public
                        .requestMatchers("/api/auth/**").permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()

                        // File Upload
                        .requestMatchers("/api/files/**")
                        .hasAnyRole("ADMIN","DOCTOR","PATIENT","RECEPTIONIST")

                        // Admin only
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Doctor routes
                        .requestMatchers("/api/doctors/**")
                        .hasAnyRole("ADMIN","DOCTOR","PATIENT","RECEPTIONIST")

                        // Patient routes
                        .requestMatchers("/api/patients/**")
                        .hasAnyRole("ADMIN","DOCTOR","RECEPTIONIST","PATIENT")

                        // Appointments
                        .requestMatchers("/api/appointments/**")
                        .hasAnyRole("ADMIN","DOCTOR","PATIENT","RECEPTIONIST")

                        // Dashboard
                        .requestMatchers("/api/dashboard/**")
                        .hasRole("ADMIN")

                        // Department
                        .requestMatchers("/api/departments/**")
                        .hasAnyRole("ADMIN","DOCTOR","PATIENT","RECEPTIONIST")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":403," +
                                            "\"error\":\"Forbidden\"," +
                                            "\"message\":\"Access denied\"," +
                                            "\"timestamp\":\"" +
                                            LocalDateTime.now() + "\"}");
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000","https://hms-frontend-kohl.vercel.app"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
