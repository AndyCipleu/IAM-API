package com.andy.iamapi.domain.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilidad para obtener la IP real del cliente HTTP.
 *
 * Centraliza la lógica de extracción de IP para evitar duplicación
 * en diferentes partes de la aplicación (filtros, controllers, etc.)
 *
 * Problema que resuelve:
 * Cuando la aplicación está detrás de un proxy o load balancer,
 * request.getRemoteAddr() devuelve la IP del proxy, no la del cliente.
 * El header X-Forwarded-For contiene la IP original del cliente.
 */
public class IpAddressUtil {
    // Clase de utilidad, no debe instanciarse
    private IpAddressUtil() {}

    /**
     * Extrae la IP real del cliente del request HTTP.
     *
     * Orden de prioridad para obtener la IP:
     * 1. Header X-Forwarded-For (si hay proxy/load balancer)
     * 2. Header X-Real-IP (alternativa usada por Nginx)
     * 3. remoteAddr directo (si no hay proxy)
     *
     * X-Forwarded-For puede contener múltiples IPs:
     * "clientIP, proxy1IP, proxy2IP"
     * Siempre tomamos la primera (IP original del cliente).
     *
     * @param request HttpServletRequest del que extraer la IP
     * @return IP del cliente como String
     */
    public static String getClientIp(HttpServletRequest request) {
        // Caso 1: Detrás de proxy/load balancer (AWS ELB, Nginx, etc.)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // Tomamos solo la primera IP (la del cliente real)
            return xForwardedFor.split(",")[0].trim();
        }

        // Caso 2: Nginx como reverse proxy (alternativa a X-Forwarded-For)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }

        // Caso 3: Conexión directa sin proxy
        return request.getRemoteAddr();
    }
}
