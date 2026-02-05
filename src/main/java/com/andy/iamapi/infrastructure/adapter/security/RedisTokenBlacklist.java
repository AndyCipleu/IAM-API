package com.andy.iamapi.infrastructure.adapter.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Blacklist de tokens usando Redis.
 *
 * Redis ventajas sobre In-Memory:
 * - Persiste entre reinicios de la app
 * - Compartido entre múltiples instancias
 * - TTL automático (Redis borra tokens expirados)
 * - Altísima performance (todo en RAM)
 *
 * Estructura en Redis:
 * Key: "blacklist:token:{token}"
 * Value: timestamp de revocación
 * TTL: tiempo hasta que el token expire naturalmente
 *
 * Ejemplo:
 * Key: "blacklist:token:eyJhbGciOiJIUzI1NiIs..."
 * Value: "2026-02-02T15:30:45"
 * TTL: 3600 segundos (1 hora para access token)
 */
@Component
public class RedisTokenBlacklist {
    private static final Logger log = LoggerFactory.getLogger(RedisTokenBlacklist.class);

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private final RedisTemplate<String, String> redisTemplate;

    public RedisTokenBlacklist(RedisTemplate<String,String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Agrega un token a la blacklist con TTL.
     *
     * El token se almacena con un TTL (Time To Live).
     * Después del TTL, Redis automáticamente borra el token.
     *
     * ¿Por qué TTL?
     * - Un token expirado naturalmente no necesita estar en blacklist
     * - Redis limpia automáticamente (no crece indefinidamente)
     *
     * @param token Token JWT a revocar
     * @param ttl Tiempo de vida (cuánto falta para que expire el token)
     */
    public void add(String token, Duration ttl) {
        String key = BLACKLIST_PREFIX + token;
        String value = String.valueOf(System.currentTimeMillis());

        redisTemplate.opsForValue().set(key, value, ttl);

        log.info("Token added to Redis blacklist with TLL: {}", ttl.getSeconds());
    }

    /**
     * Verifica si un token está en la blacklist.
     *
     * @param token Token JWT a verificar
     * @return true si está revocado, false si no
     */
    public boolean contains(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);

        return exists != null && exists;
    }

    /**
     * Remueve un token de la blacklist manualmente.
     *
     * Normalmente no es necesario (Redis lo borra por TTL).
     * Útil para testing o casos especiales.
     *
     * @param token Token a remover
     * @return true si se removió, false si no existía
     */
    public boolean remove(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean deleted = redisTemplate.delete(key);

        if (deleted != null && deleted) {
            log.debug("Token removed from Redis blacklist");
            return true;
        }

        return false;
    }

    /**
     * Limpia toda la blacklist.
     *
     * CUIDADO: Esto borra TODOS los tokens revocados.
     * Útil para testing, NO usar en producción.
     */
    public void clear() {
        var keys = redisTemplate.keys(BLACKLIST_PREFIX + "*");

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared {} tokens from Redis blacklist", keys.size());
        }
    }

    /**
     * Obtiene el número de tokens en la blacklist.
     *
     * Útil para monitoreo y debugging.
     *
     * @return Cantidad de tokens revocados
     */
    public long count() {
        var keys = redisTemplate.keys(BLACKLIST_PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }
}
