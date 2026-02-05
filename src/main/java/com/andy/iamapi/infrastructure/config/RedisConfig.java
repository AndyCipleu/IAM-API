package com.andy.iamapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuración de Redis para la aplicación.
 *
 * Redis se usa para:
 * - Blacklist de tokens revocados
 * - Cache (futuro)
 * - Sesiones (futuro)
 *
 * RedisTemplate es la abstracción de Spring para interactuar con Redis.
 * Similar a JdbcTemplate para BD relacionales.
 */
@Configuration
public class RedisConfig {

/**
 * Configura RedisTemplate para almacenar String → String.
 *
 * RedisTemplate<K, V>:
 * - K = tipo de la clave (String en nuestro caso: token)
 * - V = tipo del valor (String en nuestro caso: "revoked" o timestamp)
 *
 * Serializers:
 * - StringRedisSerializer: convierte String a bytes para Redis
 * - Por defecto usa JdkSerializationRedisSerializer (menos eficiente)
 *
 * @param connectionFactory Factory de conexiones (inyectado automáticamente)
 * @return RedisTemplate configurado
 */
@Bean
RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();

        //Establecer la factory de conexiones
        template.setConnectionFactory(connectionFactory);
        //Usar StringRedisSerializer para claves y valores
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        //Inicializar el template
        template.afterPropertiesSet();

        return template;
    }




}

