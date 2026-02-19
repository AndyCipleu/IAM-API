package com.andy.iamapi.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Jackson para serialización/deserialización JSON.
 *
 * Registra el ObjectMapper como bean de Spring para que pueda
 * ser inyectado en otros componentes (ej: RateLimitFilter).
 *
 * Configuración aplicada:
 * - JavaTimeModule: Soporte para tipos de fecha/hora de Java 8+
 *   (LocalDateTime, LocalDate, etc.) que usamos en ErrorResponse
 * - WRITE_DATES_AS_TIMESTAMPS deshabilitado: Las fechas se serializan
 *   como strings ISO 8601 ("2026-02-17T14:10:00") en vez de números
 */
@Configuration
public class JacksonConfig {
    /**
     * Configura y registra el ObjectMapper como bean de Spring.
     *
     * @return ObjectMapper configurado para la aplicación
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Soporte para LocalDateTime, LocalDate, etc.
        mapper.registerModule(new JavaTimeModule());

        // Fechas como "2026-02-17T14:10:00" en vez de timestamp numérico
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
