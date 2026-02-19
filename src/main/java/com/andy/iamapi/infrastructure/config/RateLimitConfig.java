package com.andy.iamapi.infrastructure.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Configuración de Rate Limiting con Bucket4j y Redis.
 *
 * Conecta Bucket4j con Redis usando Lettuce, el cliente Redis
 * que ya usa Spring Data Redis internamente.
 *
 * Cada IP tendrá su propio "bucket" (contador) en Redis,
 * identificado por una clave única por endpoint + IP.
 */
@Configuration
public class RateLimitConfig {
    /**
     * Crea el ProxyManager de Bucket4j usando la conexión Lettuce de Spring.
     *
     * Reutilizamos la LettuceConnectionFactory que ya configura Spring Boot
     * automáticamente con los datos del application.yml (host, port, etc.)
     *
     * El codec String/ByteArray permite:
     * - String: claves legibles en Redis (ej: "rate_limit:login:192.168.1.1")
     * - ByteArray: valores binarios serializados del estado del bucket
     *
     * @param connectionFactory Factory de conexiones Redis de Spring Boot
     * @return ProxyManager listo para gestionar buckets en Redis
     */
    @Bean
    public ProxyManager<String> lettuceBasedProxyManager(
            LettuceConnectionFactory connectionFactory
    ) {

        // Creamos una conexión con codec String/ByteArray
        // String: para las claves (ej: "rate_limit:login:192.168.1.1")
        // ByteArray: para los valores (estado del bucket serializado)
        StatefulRedisConnection<String, byte[]> connection = connectionFactory.getNativeClient() instanceof RedisClient redisClient
                ? redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE))
                : null;

        if (connection == null) {
            throw new IllegalStateException(
                    "No se pudo obtener el cliente Lettuce nativo. " +
                            "Verifica que estás usando Lettuce como cliente Redis."
            );
        }

        // Creamos el ProxyManager que gestiona los buckets en Redis
        return LettuceBasedProxyManager
                .builderFor(connection)
                .build();
    }
}
