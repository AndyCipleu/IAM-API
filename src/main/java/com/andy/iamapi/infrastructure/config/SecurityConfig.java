package com.andy.iamapi.infrastructure.config;

import com.andy.iamapi.infrastructure.adapter.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad de Spring Security.
 *
 * Configura:
 * - Autenticación stateless con JWT
 * - Endpoints públicos vs protegidos
 * - Filtro JWT personalizado
 * - Manejo de CORS y CSRF
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig (JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configura la cadena de filtros de seguridad.
     *
     * Configuración:
     * 1. CSRF deshabilitado (APIs REST stateless no lo necesitan)
     * 2. Sesiones deshabilitadas (stateless con JWT)
     * 3. Endpoints públicos: /api/auth/** (register, login)
     * 4. Endpoints protegidos: todo lo demás
     * 5. Filtro JWT antes del filtro de autenticación de Spring
     *
     * @param http HttpSecurity builder
     * @return SecurityFilterChain configurado
     */
    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        http
                // CSRF: Deshabilitado para APIs REST
                // CSRF protege contra ataques desde formularios HTML
                // APIs REST usan tokens en headers (no cookies) → CSRF no aplica
                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())

                // CORS: Permitir requests desde otros orígenes
                // Necesario si frontend está en dominio diferente
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.disable())

                // Sesiones: STATELESS (no crear sesiones HTTP)
                // JWT es stateless → no necesitamos sesiones en servidor
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Autorización de requests
                .authorizeHttpRequests(auth -> auth
                        // Endpoints PÚBLICOS (sin token)
                        .requestMatchers("/api/auth/**").permitAll() //register, login, logout y refresh

                        // Swagger/OpenAPI endpoints (públicos para documentación)
                        .requestMatchers(
                                "/v3/api-docs/**",      // OpenAPI JSON
                                "/swagger-ui/**",        // Recursos de Swagger UI (CSS, JS)
                                "/swagger-ui.html"       // Página principal de Swagger
                        ).permitAll()

                        // Todos los demás endpoints requieren AUTENTICACIÓN
                        .anyRequest().authenticated())

                // Agregar filtro JWT ANTES del filtro de autenticación de Spring
                // Orden importante: JWT debe ejecutarse primero para establecer autenticación
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
