package com.andy.iamapi.infrastructure.adapter.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Servicio de Rate Limiting usando Bucket4j con Redis.
 *
 * Gestiona los límites de peticiones por IP para endpoints sensibles.
 * Cada combinación de (endpoint + IP) tiene su propio bucket en Redis.
 *
 * Algoritmo Token Bucket:
 * - Cada bucket tiene una capacidad máxima de tokens
 * - Cada request consume 1 token
 * - Los tokens se recargan automáticamente con el tiempo
 * - Si no hay tokens disponibles → 429 Too Many Requests
 */
@Service
public class RateLimitService {
    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    // Prefijo para las claves en Redis
    // Ejemplo de clave: "rate_limit:login:192.168.1.1"
    private static final String KEY_PREFIX = "rate_limit";

    private final ProxyManager<String> proxyManager;

    public RateLimitService(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    /**
     * Verifica si una IP puede hacer una petición al endpoint de login.
     *
     * Límite: 5 intentos por minuto por IP.
     * Pensado para prevenir ataques de fuerza bruta.
     *
     * @param ipAddress IP del cliente
     * @return true si el request está permitido, false si se excedió el límite
     */
    public boolean isLoginAllowed(String ipAddress) {
        return isAllowed("login", ipAddress, loginBucketConfiguration());
    }

    /**
     * Verifica si una IP puede hacer una petición al endpoint de registro.
     *
     * Límite: 3 registros por hora por IP.
     * Pensado para prevenir creación masiva de cuentas.
     *
     * @param ipAddress IP del cliente
     * @return true si el request está permitido, false si se excedió el límite
     */
    public boolean isRegisterAllowed(String ipAddress) {
        return isAllowed("register", ipAddress, registerBucketConfiguration());
    }

    /**
     * Verifica si una IP puede hacer una petición al endpoint de refresh.
     *
     * Límite: 10 intentos por minuto por IP.
     *
     * @param ipAddress IP del cliente
     * @return true si el request está permitido, false si se excedió el límite
     */
    public boolean isRefreshAllowed(String ipAddress) {
        return isAllowed("refresh", ipAddress, refreshBucketConfiguration());
    }

    /**
     * Verifica si una IP puede hacer una petición a endpoints generales.
     *
     * Límite: 30 requests por minuto por IP.
     * Pensado para endpoints de listado (GET /api/users, etc.)
     *
     * @param ipAddress IP del cliente
     * @return true si el request está permitido, false si se excedió el límite
     */
    public boolean isGeneralAllowed(String ipAddress) {
        return isAllowed("general", ipAddress, generalBucketConfiguration());
    }

//    ----------------------------------------

    /**
     * Lógica central de verificación de rate limit.
     *
     * Construye la clave única para el bucket (endpoint + IP),
     * obtiene o crea el bucket en Redis, y verifica si hay tokens disponibles.
     *
     * ConsumptionProbe contiene:
     * - isConsumed(): si se pudo consumir el token (request permitido)
     * - getNanosToWaitForRefill(): tiempo hasta que haya tokens disponibles
     * - getRemainingTokens(): tokens restantes después del consumo
     *
     * @param endpoint Nombre del endpoint (para diferenciar buckets)
     * @param ipAddress IP del cliente
     * @param configSupplier Configuración del bucket (capacidad y velocidad de recarga)
     * @return true si el request está permitido
     */
    private boolean isAllowed(
            String endpoint,
            String ipAddress,
            Supplier<BucketConfiguration> configSupplier
    ) {

        // Clave única: "rate_limit:login:192.168.1.1"\
        String key = String.format("%s:%s:%s", KEY_PREFIX, endpoint, ipAddress);

        // Obtiene el bucket existente o crea uno nuevo con la configuración dada
        // Si la IP ya tiene un bucket en Redis, lo reutiliza
        // Si es la primera petición, crea un bucket nuevo
        ConsumptionProbe probe = proxyManager
                .builder()
                .build(key,configSupplier)
                .tryConsumeAndReturnRemaining(1); // Intenta consumir 1 token

        if (probe.isConsumed()) {
            // No hay tokens disponibles
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            log.warn("Rate limit exceeded for IP: {} on endpoint: {}. Wait {} seconds",
                    ipAddress, endpoint, waitSeconds);
            return false;
        }

        log.debug("Rate limit check passed for IP: {} on endpoint: {}. Remaining tokens: {}",
                ipAddress, endpoint, probe.getRemainingTokens());

        return true;
    }

    // ===== Configuraciones de cada endpoint =====

    /**
     * Configuración para endpoint de login.
     *
     * 5 tokens máximo, se recarga 1 token cada 12 segundos
     * (equivale a 5 por minuto en total).
     *
     * @return Supplier con la configuración del bucket
     */
    private Supplier<BucketConfiguration> loginBucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)  // Máximo 5 tokens
                        .refillGreedy(5, Duration.ofMinutes(1)) // Recarga 5 tokens
                        .build())
                .build();
    }

    /**
     * Configuración para endpoint de registro.
     *
     * 3 tokens máximo, se recargan cada hora.
     *
     * @return Supplier con la configuración del bucket
     */
    private Supplier<BucketConfiguration> registerBucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillGreedy(3, Duration.ofHours(1))
                        .build())
                .build();
    }

    /**
     * Configuración para endpoint de refresh token.
     *
     * 10 tokens máximo, se recargan cada minuto.
     *
     * @return Supplier con la configuración del bucket
     */
    private Supplier<BucketConfiguration> refreshBucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillGreedy(10, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    /**
     * Configuración para endpoints generales.
     *
     * 30 tokens máximo, se recargan cada minuto.
     *
     * @return Supplier con la configuración del bucket
     */
    private Supplier<BucketConfiguration> generalBucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(30)
                        .refillGreedy(30, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}
