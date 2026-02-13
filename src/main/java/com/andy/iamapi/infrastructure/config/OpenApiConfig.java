package com.andy.iamapi.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación interactiva de la API.
 *
 * La documentación estará disponible en:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 *
 * Esta configuración define:
 * - Información general de la API (título, descripción, versión)
 * - Esquema de seguridad JWT (Bearer Token)
 * - Servidores disponibles (local, producción)
 * - Información de contacto y licencia
 */
@Configuration
public class OpenApiConfig {
    /**
     * Configura la documentación OpenAPI de la API.
     *
     * Este bean es detectado automáticamente por SpringDoc y se usa
     * para generar la especificación OpenAPI y la interfaz Swagger UI.
     *
     * @return Configuración OpenAPI personalizada
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // Nombre del esquema de seguridad (debe coincidir en @SecurityRequirement)
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                // ===== INFORMACIÓN GENERAL DE LA API =====
                .info(new Info()
                        .title("IAM API - Identity and Access Management")
                        .description("""
                                ## 🔐 Sistema de Autenticación y Autorización
                                
                                API REST completa para gestión de identidad y control de acceso (IAM) 
                                construida con Spring Boot 3 y arquitectura hexagonal.
                                
                                ### 🎯 Características principales:
                                - ✅ **Autenticación JWT** con access y refresh tokens
                                - ✅ **Control de acceso basado en roles** (RBAC)
                                - ✅ **Gestión completa de usuarios** (CRUD)
                                - ✅ **Sistema de roles y permisos** granular
                                - ✅ **Blacklist de tokens** en Redis
                                - ✅ **Arquitectura hexagonal** (Domain, Application, Infrastructure)
                                - ✅ **Seguridad con BCrypt** para passwords
                                
                                ### 🔑 Flujo de autenticación:
                                1. Registra un usuario con `POST /api/auth/register`
                                2. Obtén tokens con `POST /api/auth/login`
                                3. Usa el **access token** en el header `Authorization: Bearer {token}`
                                4. Renueva el access token con `POST /api/auth/refresh`
                                
                                ### 🛡️ Roles disponibles:
                                - **ROLE_USER**: Usuario estándar (puede ver y editar su perfil)
                                - **ROLE_ADMIN**: Administrador (acceso completo al sistema)
                                - **ROLE_MODERATOR**: Moderador (permisos intermedios)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Andy Cipleu")
                                .email("andycipleu@gmail.com")
                                .url("https://www.linkedin.com/in/andrei-claudiu-cipleu-30a625300/"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // ===== SERVIDORES DISPONIBLES =====
                // Define los entornos donde la API puede ejecutarse
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo local")
                ))

                // ===== CONFIGURACIÓN DE SEGURIDAD JWT =====
                // Añade un requisito de seguridad global (todos los endpoints lo necesitan por defecto)
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))

                // Define el esquema de seguridad (cómo autenticarse)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        ### 🔑 Autenticación JWT
                                        
                                        Para usar endpoints protegidos:
                                        
                                        1. **Registrarse**: `POST /api/auth/register`
                                        2. **Iniciar sesión**: `POST /api/auth/login`
                                        3. El endpoint de login te devolverá un `accessToken`
                                        4. Copia el token completo
                                        5. Haz clic en el botón **Authorize** (🔒) arriba a la derecha
                                        6. Pega el token en el campo de valor
                                        7. Haz clic en **Authorize** de nuevo
                                        
                                        El token se enviará automáticamente en todas las peticiones:
```
                                        Authorization: Bearer {tu-access-token}
```
                                        
                                        **Nota**: Los tokens expiran en 1 hora. 
                                        Usa `/api/auth/refresh` para renovarlos.
                                        """)));


    }
}
