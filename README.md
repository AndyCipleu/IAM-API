# 🔐 IAM API - Identity and Access Management

Sistema completo de **autenticación y autorización** construido con **Spring Boot 3** y **arquitectura hexagonal**, implementando JWT tokens, gestión de usuarios, roles y permisos con control de acceso basado en roles (RBAC).

---

## 🚀 Tecnologías Utilizadas

### Backend
- **Java 17**
- **Spring Boot 3.x**
  - Spring Security (JWT, BCrypt)
  - Spring Data JPA
  - Spring Validation
- **Arquitectura Hexagonal** (Ports & Adapters)
- **Domain-Driven Design (DDD)**

### Base de Datos
- **PostgreSQL** (datos relacionales)
- **Redis** (blacklist de tokens)
- **Flyway** (migraciones de BD)

### Infraestructura
- **Docker & Docker Compose**
- **Logback** (logging y auditoría)

### Documentación
- **Swagger/OpenAPI 3.0** (documentación interactiva de la API)

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────┐
│                    REST API Layer                       │
│  AuthController | UserController | RoleController       │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              Application Layer (Use Cases)              │
│  RegisterUser | AuthenticateUser | UpdateUser | ...     │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                  Domain Layer                           │
│  User | Role | Permission (Entities)                    │
│  Ports (Interfaces): UserRepository, TokenService       │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│             Infrastructure Layer                        │
│  JPA Adapters | JWT Service | Redis Blacklist          │
│  PostgreSQL | BCrypt Encoder | Audit Logger             │
└─────────────────────────────────────────────────────────┘
```

### Flujo de Autenticación

```
1. Usuario → POST /api/auth/login (email + password)
2. AuthenticateUserService verifica credenciales con BCrypt
3. TokenService genera Access Token (1h) + Refresh Token (7d)
4. Cliente guarda tokens y los usa en requests:
   Authorization: Bearer {accessToken}
5. JwtAuthenticationFilter valida token en cada request
6. SecurityContext establece usuario autenticado
7. Controller accede al usuario y ejecuta lógica
```

---

## 📐 Características Principales

### 🔑 Autenticación JWT
- **Access Tokens** (1 hora) para requests normales
- **Refresh Tokens** (7 días) para renovar access tokens
- **Blacklist en Redis** para revocación inmediata de tokens
- **Validación automática** mediante filtros de Spring Security

### 👥 Gestión de Usuarios
- Registro de usuarios con validación de email y password
- Actualización de perfil (nombre, apellido, email)
- Cambio de contraseña con verificación de password actual
- Eliminación de usuarios (soft/hard delete)
- Asignación y revocación de roles

### 🛡️ Control de Acceso (RBAC)
- **Roles predefinidos**: ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR
- **Permisos**: Granularidad a nivel de recurso + acción
- **Autorización a nivel de método**: `@PreAuthorize`
- **Expresiones SpEL**: Admin o mismo usuario

### 🔒 Seguridad
- **BCrypt** para hashing de passwords (resistente a timing attacks)
- **Validación de passwords**: mínimo 8 caracteres, mayúsculas, minúsculas, dígitos
- **Auditoría**: Logs de login, logout, registro, cambios de password
- **CSRF deshabilitado** (API stateless con JWT)
- **Sesiones stateless** (sin HttpSession)

### 📊 Base de Datos
- **PostgreSQL**: Usuarios, roles, permisos
- **Redis**: Blacklist de tokens revocados con TTL automático
- **Flyway**: Migraciones versionadas con datos iniciales

---

## 📁 Estructura del Proyecto

```
src/main/java/com/andy/iamapi/
├── domain/                          # Capa de dominio (lógica de negocio)
│   ├── model/                       # Entidades del dominio
│   ├── port/                        # Puertos (interfaces)
│   │   ├── input/                   # Casos de uso
│   │   └── output/                  # Puertos de salida
│   ├── exception/                   # Excepciones de dominio
│   └── util/                        # Utilidades
│
├── application/                     # Capa de aplicación (orquestación)
│   └── service/                     # Servicios de casos de uso
│
└── infrastructure/                  # Capa de infraestructura
    ├── adapter/
    │   ├── persistence/             # Adaptadores de BD
    │   ├── security/                # Adaptadores de seguridad
    │   └── rest/                    # Adaptadores REST
    └── config/                      # Configuración
```

---

## 🗄️ Diagrama de Base de Datos

```sql
┌─────────────┐       ┌──────────────┐       ┌─────────────────┐
│   users     │       │  user_roles  │       │     roles       │
├─────────────┤       ├──────────────┤       ├─────────────────┤
│ id (PK)     │───┐   │ user_id (FK) │   ┌───│ id (PK)         │
│ email       │   └──→│ role_id (FK) │←──┘   │ name (UNIQUE)   │
│ password    │       └──────────────┘       │ description     │
│ first_name  │                              └─────────────────┘
│ last_name   │                                      │
│ enabled     │                                      │
│ locked      │       ┌──────────────────┐          │
│ created_at  │       │ role_permissions │          │
│ updated_at  │       ├──────────────────┤          │
└─────────────┘       │ role_id (FK)     │←─────────┘
                      │ permission_id    │
                      └──────────────────┘
                               │
                               ▼
                      ┌─────────────────┐
                      │   permissions   │
                      ├─────────────────┤
                      │ id (PK)         │
                      │ name (UNIQUE)   │
                      │ resource        │
                      │ action          │
                      │ description     │
                      └─────────────────┘
```

---

## 📚 Documentación de la API

La API está completamente documentada con **Swagger/OpenAPI 3.0**.

### 🔗 Acceder a Swagger UI

Una vez la aplicación esté corriendo, accede a:

```
http://localhost:8080/swagger-ui.html
```

### 📖 Swagger incluye:

- ✅ **Listado completo de endpoints** organizados por categorías
- ✅ **Descripción detallada** de cada operación
- ✅ **Esquemas de request/response** con ejemplos
- ✅ **Códigos de estado HTTP** y sus significados
- ✅ **Autenticación JWT integrada** (botón Authorize 🔒)
- ✅ **Interfaz interactiva** para probar la API directamente

### 🔑 Autenticación en Swagger:

1. Ejecutar `POST /api/auth/register` para crear un usuario
2. Ejecutar `POST /api/auth/login` para obtener tokens
3. Copiar el `accessToken` de la respuesta
4. Hacer clic en el botón **Authorize** 🔒
5. Pegar el token y hacer clic en **Authorize**
6. ¡Listo! Ya puedes probar todos los endpoints protegidos

### 📥 Especificación OpenAPI (JSON)

También puedes obtener la especificación OpenAPI en formato JSON:

```
http://localhost:8080/v3/api-docs
```

Este JSON se puede importar en otras herramientas como Postman, Insomnia, o generadores de clientes API.

---

## ⚙️ Instalación y Ejecución

### Requisitos Previos
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### 1. Clonar el repositorio

```bash
git clone https://github.com/tuusuario/iam-api.git
cd iam-api
```

### 2. Configuración

**Archivo `application.yml`:**

```yaml
jwt:
  secret: tu-clave-secreta-de-256-bits-minimo-para-jwt-tokens
  expiration: 3600000        # 1 hora
  refresh-expiration: 604800000  # 7 días
```

> ⚠️ **IMPORTANTE**: Cambia el `jwt.secret` en producción por una clave segura.

### 3. Levantar infraestructura con Docker

```bash
# Levantar PostgreSQL y Redis
docker-compose up -d

# Verificar contenedores
docker ps
```

### 4. Ejecutar aplicación

```bash
# Compilar
./mvnw clean install

# Ejecutar
./mvnw spring-boot:run
```

La API estará disponible en: **http://localhost:8080**

### 5. Acceder a Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

## 🧪 Probar la API

### Opción 1: Swagger UI (Recomendado)

La forma más fácil es usar la **interfaz Swagger** incluida:

1. Accede a http://localhost:8080/swagger-ui.html
2. Sigue el flujo de autenticación explicado arriba
3. Prueba todos los endpoints interactivamente

### Opción 2: Postman

Si prefieres Postman, hay una colección lista para usar:

1. Importar archivos:
   - `Postman/IAM API.postman_collection.json`
   - `Postman/IAM API - Local.postman_environment.json`
2. Seleccionar el environment `IAM API - Local`
3. Seguir el flujo: Register → Login → Usar endpoints

### Opción 3: cURL

```bash
# 1. Registrar usuario
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123",
    "firstName": "Test",
    "lastName": "User"
  }'

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123"
  }'

# 3. Usar token en endpoints protegidos
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer {tu-access-token-aquí}"
```

---

## 🔮 Roadmap / Próximas Mejoras

- [ ] Tests unitarios e integración completos
- [ ] Paginación y filtrado en listado de usuarios
- [ ] Rate limiting para prevenir ataques de fuerza bruta
- [ ] Two-Factor Authentication (2FA)
- [ ] OAuth2 con providers externos (Google, GitHub)
- [ ] Email verification al registrarse
- [ ] Password reset flow
- [ ] Account locking después de intentos fallidos
- [ ] Auditoría persistente en base de datos
- [ ] CI/CD con GitHub Actions
- [ ] Despliegue con Kubernetes

---

## 📚 Patrones y Buenas Prácticas Aplicadas

- **Arquitectura Hexagonal**: Separación clara entre dominio, aplicación e infraestructura
- **DDD (Domain-Driven Design)**: Entidades, Value Objects, Aggregate Roots
- **SOLID Principles**: Single Responsibility, Dependency Inversion, Open/Closed
- **Factory Pattern**: Creación de entidades de dominio (`User.create()`, `User.reconstitute()`)
- **Repository Pattern**: Abstracción de acceso a datos
- **Strategy Pattern**: PasswordEncoder, TokenService
- **Immutability**: Objetos de dominio inmutables con `reconstitute()`
- **DTO Pattern**: Separación entre modelos de dominio y API
- **Validation**: Bean Validation en DTOs, validaciones de negocio en dominio

---

## 🛠️ Tecnologías y Dependencias Principales

| Dependencia | Versión | Propósito |
|-------------|---------|-----------|
| Spring Boot | 3.4.x | Framework base |
| Spring Security | 6.x | Autenticación y autorización |
| Spring Data JPA | 3.x | Acceso a datos |
| PostgreSQL | 16 | Base de datos relacional |
| Redis | 7 | Cache y blacklist de tokens |
| Flyway | 10.x | Migraciones de BD |
| jjwt | 0.12.6 | Generación y validación de JWT |
| Lombok | - | Reducción de boilerplate |
| SpringDoc OpenAPI | 2.6.0 | Documentación Swagger |

---

## ✉️ Autor / Contacto

**Nombre**: Andy Cipleu

**LinkedIn**: [linkedin.com/in/andy-cipleu](https://www.linkedin.com/in/andrei-claudiu-cipleu-30a625300/)

**Email**: andycipleu@gmail.com

**GitHub**: [github.com/AndyCipleu](https://github.com/AndyCipleu)

---

## 📄 Licencia

Este proyecto es de código abierto desarrollado por **Andy Cipleu** y está disponible bajo la [Licencia MIT](LICENSE).

Copyright © 2026 Andy Cipleu. Todos los derechos reservados.

---

## 🙏 Agradecimientos

Gracias por revisar este proyecto. Si tienes sugerencias o encuentras algún problema, no dudes en abrir un issue o contactarme directamente.

---

**⭐ Si te gusta este proyecto, considera darle una estrella en GitHub!**
