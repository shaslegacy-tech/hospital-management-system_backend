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
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Preflight ─────────────────────────────
                        .requestMatchers(
                                HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // ── Public ────────────────────────────────
                        .requestMatchers(
                                "/api/auth/**")
                        .permitAll()

                        // ── Swagger ───────────────────────────────
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs")
                        .permitAll()

                        // ── File Upload ───────────────────────────
                        .requestMatchers("/api/files/**")
                        .hasAnyRole(
                                "ADMIN", "DOCTOR",
                                "PATIENT", "RECEPTIONIST")

                        // ── Admin only ────────────────────────────
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // ── Dashboard ─────────────────────────────
                        .requestMatchers("/api/dashboard/**")
                        .hasRole("ADMIN")

                        // ── Audit logs ────────────────────────────
                        .requestMatchers("/api/audit-logs/**")
                        .hasRole("ADMIN")

                        // ── Departments ───────────────────────────
                        .requestMatchers("/api/departments/**")
                        .hasAnyRole(
                                "ADMIN", "DOCTOR",
                                "PATIENT", "RECEPTIONIST")

                        // ── Doctors ───────────────────────────────
                        .requestMatchers("/api/doctors/**")
                        .hasAnyRole(
                                "ADMIN", "DOCTOR",
                                "PATIENT", "RECEPTIONIST")

                        // ── Patients ──────────────────────────────
                        .requestMatchers("/api/patients/**")
                        .hasAnyRole(
                                "ADMIN", "DOCTOR",
                                "PATIENT", "RECEPTIONIST")

                        // ── Users (search + approvals) ────────────
                        // ✅ Added — was completely missing
                        .requestMatchers("/api/users/**")
                        .hasAnyRole(
                                "ADMIN", "RECEPTIONIST")

                        // ── Appointments ──────────────────────────
                        .requestMatchers("/api/appointments/**")
                        .hasAnyRole(
                                "ADMIN", "DOCTOR",
                                "PATIENT", "RECEPTIONIST")

                        // ── Medical Records ───────────────────────
                        // ✅ Added — was completely missing
                        .requestMatchers("/api/records/**")
                        .hasAnyRole(
                                "ADMIN", "DOCTOR", "PATIENT")

                        // ── Prescriptions ─────────────────────────
                        // ✅ Added — was completely missing
                        .requestMatchers("/api/prescriptions/**")
                        .hasAnyRole(
                                "ADMIN", "DOCTOR", "PATIENT")

                        // ── Bills ─────────────────────────────────
                        // ✅ Added — was completely missing
                        .requestMatchers("/api/bills/**")
                        .hasAnyRole(
                                "ADMIN", "RECEPTIONIST", "PATIENT")

                        // ── Payments (Razorpay) ───────────────────
                        // ✅ Added — was completely missing
                        .requestMatchers("/api/payments/**")
                        .hasAnyRole(
                                "ADMIN", "RECEPTIONIST", "PATIENT")

                        // ── Notifications ─────────────────────────
                        // ✅ Added — was completely missing
                        .requestMatchers("/api/notifications/**")
                        .hasAnyRole(
                                "ADMIN", "DOCTOR",
                                "PATIENT", "RECEPTIONIST")

                        .requestMatchers("/api/caregivers/**")
                        .hasAnyRole(
                                "ADMIN", "DOCTOR",
                                "PATIENT", "RECEPTIONIST")

                        // ── Fallback ──────────────────────────────
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((
                                request,
                                response,
                                accessDeniedException) -> {
                            response.setStatus(
                                    HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(
                                    "application/json");
                            response.getWriter().write(
                                    "{\"status\":403," +
                                            "\"error\":\"Forbidden\"," +
                                            "\"message\":\"Access denied\"," +
                                            "\"timestamp\":\"" +
                                            LocalDateTime.now() + "\"}");
                        })
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://aarogyaai-ojasya.vercel.app"
        ));
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT",
                "DELETE", "PATCH", "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",
                configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
