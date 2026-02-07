package com.andy.iamapi.infrastructure.adapter.security;

import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.output.TokenService;
import com.andy.iamapi.domain.port.output.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Filtro de autenticación JWT que se ejecuta en cada request.
 *
 * Responsabilidades:
 * 1. Extraer el token JWT del header Authorization
 * 2. Validar el token (firma, expiración, blacklist)
 * 3. Extraer el email del usuario del token
 * 4. Cargar el usuario completo de la BD
 * 5. Establecer la autenticación en el SecurityContext
 *
 * OncePerRequestFilter garantiza que se ejecuta UNA SOLA VEZ por request.
 *
 * Flujo:
 * Request → JwtAuthenticationFilter → Controller
 *           ↓
 *           Valida token y carga usuario
 *           ↓
 *           SecurityContext tiene usuario autenticado
 *           ↓
 *           Controller puede acceder a usuario actual
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer";

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter (
            TokenService tokenService,
            UserRepository userRepository
    ) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    /**
     * Método principal del filtro.
     *
     * Se ejecuta automáticamente en cada request HTTP.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Cadena de filtros de Spring Security
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
            ) throws ServletException, IOException {

        try {
            // PASO 1: Extraer token del header Authorization
            String token = extractTokenFromRequest(request);

            if (token == null) {
                log.debug("No JWT token found in request to: {}", request.getRequestURI());
                filterChain.doFilter(request,response);
                return;
            }

            //Paso 2: Validar token y extraer email
            Optional<String> emailOpt = tokenService.validateTokenAndGetEmail(token);

            if (emailOpt.isEmpty()) {
                log.warn("Invalid or expired JWT token for request to: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            String email = emailOpt.get();

            //Paso 3: Verificar que no haya autenticación previa
            //(evitar procesar el token múltiples veces)
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.debug("User already authenticated: {}", email);
                filterChain.doFilter(request, response);
                return;
            }

            //Paso 4: Cargar usuario completo de la BD
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                log.warn("Valid token but user not found: {}", email);
                filterChain.doFilter(request, response);
                return;
            }

            User user = userOpt.get();

            //Paso 5: Verificar que la cuenta esté habilitada
            if (!user.isEnabled()) {
                log.warn("User account disabled: {}", email);
                filterChain.doFilter(request, response);
                return;
            }

            // PASO 6: Verificar que la cuenta no esté bloqueada
            if (!user.isAccountNonLocked()) {
                log.warn("User account locked: {}", email);
                filterChain.doFilter(request, response);
                return;
            }

            //Paso 7: Convertir roles del usuario a GrantedAutorities
            List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .toList();

            //Paso 8: Crear Authentication object
            //UsernamePasswordAuthenticationToken es un objeto que representa una autenticatión en Spring Security
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            //PASO 9: Establecer autenticación en SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication); //Contenedor que va a guardar la autenticatión

            log.debug("User authenticated successfully: {} with roles: {}", email, authorities);
        } catch (Exception e) {
            // Cualquier error en el filtro → loguear pero no fallar el request
            log.error("Error processing JWT authentication", e);
            // NO lanzar excepción, dejar que continúe sin autenticación
        }

        // PASO 10: Continuar con la cadena de filtros
        filterChain.doFilter(request, response);


    }



    /**
     * Extrae el token JWT del header Authorization.
     *
     * Header esperado:
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
     *
     * @param request HTTP request
     * @return Token JWT sin el prefijo "Bearer ", o null si no hay token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Authorization header does not start with 'Bearer ': {}", authHeader);
            return null;
        }

        //Extraer token(después de "Bearer ")
        return authHeader.substring(BEARER_PREFIX.length());
    }


}
