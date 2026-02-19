package com.andy.iamapi.infrastructure.adapter.security;

import com.andy.iamapi.infrastructure.adapter.rest.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.andy.iamapi.domain.util.IpAddressUtil.getClientIp;

/**
 * Filtro de Rate Limiting para endpoints sensibles.
 *
 * Se ejecuta una vez por request (OncePerRequestFilter) antes de
 * Spring Security y los controllers.
 *
 * Si el cliente excede el límite, retorna 429 Too Many Requests
 * inmediatamente sin procesar el request.
 *
 * Endpoints protegidos:
 * - POST /api/auth/login → 5 req/min por IP
 * - POST /api/auth/register → 3 req/hora por IP
 * - POST /api/auth/refresh → 10 req/min por IP
 * - GET /api/users → 30 req/min por IP
 */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(
            RateLimitService rateLimitService,
            ObjectMapper objectMapper
    ) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    /**
     * Lógica principal del filtro.
     *
     * Para cada request:
     * 1. Extrae la IP del cliente
     * 2. Identifica qué endpoint se está llamando
     * 3. Verifica el rate limit correspondiente
     * 4. Si está permitido → continúa la cadena de filtros
     * 5. Si está bloqueado → retorna 429 inmediatamente
     *
     * @param request Request HTTP entrante
     * @param response Response HTTP
     * @param filterChain Cadena de filtros de Spring
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String ipAddress = getClientIp(request);


        // Verificar rate limit según el endpoint
        boolean allowed = checkRateLimit(path,method,ipAddress);

        if (!allowed) {
            // Bloquear el request con 429 Too Many Requests
            sendRateLimitExceededResponse(response, ipAddress, path);
            return;  // No continúa la cadena de filtros
        }

        // Request permitido → continuar con el siguiente filtro
        filterChain.doFilter(request, response);
    }

    /**
     * Determina qué límite aplicar según el endpoint y método HTTP.
     *
     * @param path URI del request (ej: "/api/auth/login")
     * @param method Método HTTP (GET, POST, etc.)
     * @param ipAddress IP del cliente
     * @return true si el request está permitido, false si está bloqueado
     */
    private boolean checkRateLimit(String path, String method, String ipAddress) {
        // Login: POST /api/auth/login
        if ("POST".equals(method) && path.equals("/api/auth/login")) {
            return rateLimitService.isLoginAllowed(ipAddress);
        }
        // Register: POST /api/auth/register
        if ("POST".equals(method) && path.equals("/api/auth/register")) {
            return rateLimitService.isRegisterAllowed(ipAddress);
        }

        // Refresh: POST /api/auth/refresh
        if ("POST".equals(method) && path.equals("/api/auth/refresh")) {
            return rateLimitService.isRefreshAllowed(ipAddress);
        }

        // Listado de usuarios: GET /api/users
        if ("GET".equals(method) && path.equals("/api/users")) {
            return rateLimitService.isGeneralAllowed(ipAddress);
        }

        // El resto de endpoints no tienen rate limit (por ahora)
        return true;
    }

    /**
     * Envía una respuesta 429 Too Many Requests al cliente.
     *
     * Retorna un JSON con el error para que el cliente
     * pueda manejarlo correctamente.
     *
     * @param response HttpServletResponse para escribir la respuesta
     * @param ipAddress IP del cliente (para el log)
     * @param path Endpoint que se estaba llamando
     */
    private void sendRateLimitExceededResponse(
            HttpServletResponse response,
            String ipAddress,
            String path
    )throws IOException {
        log.warn("Rate limit exceeded - IP: {}, Path: {}", ipAddress, path);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Reutilizamos ErrorResponse que ya tienes en el proyecto
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too many requests",
                "Too many requests. Please try again later.",
                path
        );

        // Escribir el JSON en la respuesta
        response.getWriter().write(
                objectMapper.writeValueAsString(errorResponse)
        );
    }
}
