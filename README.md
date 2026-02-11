# 🔐 IAM API - Identity and Access Management

Sistema completo de **autenticación y autorización** construido con **Spring Boot 3** y **arquitectura hexagonal**, implementando JWT tokens, gestión de usuarios, roles y permisos con control de acceso basado en roles (RBAC).

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

### 🛡️ Control de Acceso (RBAC)
- **Roles**: ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR
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

## 📁 Estructura del Proyecto
```
src/main/java/com/andy/iamapi/
├── domain/                          # Capa de dominio (lógica de negocio)
│   ├── model/                       # Entidades del dominio
│   │   ├── User.java
│   │   ├── Role.java
│   │   └── Permission.java
│   ├── port/                        # Puertos (interfaces)
│   │   ├── input/                   # Casos de uso
│   │   │   ├── RegisterUserUseCase.java
│   │   │   ├── AuthenticateUserUseCase.java
│   │   │   └── ...
│   │   └── output/                  # Puertos de salida
│   │       ├── UserRepository.java
│   │       ├── TokenService.java
│   │       └── ...
│   ├── exception/                   # Excepciones de dominio
│   └── util/                        # Utilidades (PasswordValidator)
│
├── application/                     # Capa de aplicación (orquestación)
│   └── service/
│       ├── RegisterUserService.java
│       ├── AuthenticateUserService.java
│       └── ...
│
└── infrastructure/                  # Capa de infraestructura
    ├── adapter/
    │   ├── persistence/             # Adaptadores de BD
    │   │   ├── entity/              # JPA Entities
    │   │   ├── repository/          # JPA Repositories
    │   │   ├── mapper/              # Mappers Entity ↔ Domain
    │   │   └── *RepositoryAdapter.java
    │   ├── security/                # Adaptadores de seguridad
    │   │   ├── JwtTokenService.java
    │   │   ├── BCryptPasswordEncoderAdapter.java
    │   │   ├── RedisTokenBlacklist.java
    │   │   └── LoggerAuditAdapter.java
    │   └── rest/                    # Adaptadores REST
    │       ├── controller/
    │       ├── dto/
    │       └── exception/
    └── config/                      # Configuración
        ├── SecurityConfig.java
        ├── RedisConfig.java
        └── security/
            └── JwtAuthenticationFilter.java
```

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

## 🔧 API Endpoints

### Autenticación (Públicos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registrar nuevo usuario |
| POST | `/api/auth/login` | Autenticar y obtener tokens |
| POST | `/api/auth/refresh` | Renovar access token |

### Autenticación (Protegidos)

| Método | Endpoint | Descripción | Requiere |
|--------|----------|-------------|----------|
| POST | `/api/auth/logout` | Cerrar sesión (revocar tokens) | Token válido |

### Gestión de Usuarios

| Método | Endpoint | Descripción | Requiere |
|--------|----------|-------------|----------|
| GET | `/api/users/me` | Obtener mi perfil | Token válido |
| GET | `/api/users` | Listar todos los usuarios | ROLE_ADMIN |
| GET | `/api/users/{id}` | Obtener usuario por ID | ROLE_ADMIN |
| PUT | `/api/users/{id}` | Actualizar usuario | ROLE_ADMIN o mismo usuario |
| PUT | `/api/users/{id}/password` | Cambiar contraseña | Mismo usuario |
| DELETE | `/api/users/{id}` | Eliminar usuario | ROLE_ADMIN |

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

## 🧪 Ejemplo de Uso

### 1. Registrar Usuario
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**
```json
{
  "id": "a3c7ef12-9b4d-4f8a-b123-456789abcdef",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "enabled": true,
  "roles": ["ROLE_USER"],
  "createdAt": "2026-02-11T10:30:45"
}
```

### 2. Login
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecurePass123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

### 3. Obtener Mi Perfil
```bash
GET http://localhost:8080/api/users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "id": "a3c7ef12-9b4d-4f8a-b123-456789abcdef",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "enabled": true,
  "roles": ["ROLE_USER"],
  "createdAt": "2026-02-11T10:30:45"
}
```

## 🔮 Futuras Mejoras

- [ ] Gestión completa de roles y permisos (asignar/revocar)
- [ ] Paginación y filtrado en listado de usuarios
- [ ] Rate limiting para prevenir ataques de fuerza bruta
- [ ] Two-Factor Authentication (2FA)
- [ ] OAuth2 con providers externos (Google, GitHub)
- [ ] Tests unitarios e integración completos
- [ ] Documentación OpenAPI/Swagger
- [ ] CI/CD con GitHub Actions
- [ ] Despliegue con Kubernetes
- [ ] Colección de Postman/Insomnia

## 📚 Patrones y Buenas Prácticas Aplicadas

- **Arquitectura Hexagonal**: Separación clara entre dominio, aplicación e infraestructura
- **DDD (Domain-Driven Design)**: Entidades, Value Objects, Aggregate Roots
- **SOLID Principles**: Single Responsibility, Dependency Inversion, Open/Closed
- **Factory Pattern**: Creación de entidades de dominio
- **Repository Pattern**: Abstracción de acceso a datos
- **Strategy Pattern**: PasswordEncoder, TokenService
- **Immutability**: Objetos de dominio inmutables con `reconstitute()`
- **DTO Pattern**: Separación entre modelos de dominio y API
- **Validation**: Bean Validation en DTOs, validaciones de negocio en dominio

## ✉️ Autor / Contacto

**Nombre**: Andy Cipleu

**LinkedIn**: [linkedin.com/in/andy-cipleu](https://www.linkedin.com/in/andrei-claudiu-cipleu-30a625300/)

**Email**: andycipleu@gmail.com

**GitHub**: [github.com/AndyCipleu](https://github.com/AndyCipleu)

## 📄 Licencia y Derechos de Autor

Copyright © 2026 **Andy Cipleu**. Todos los derechos reservados.

Este proyecto está disponible públicamente con fines educativos y de demostración.
El código puede ser visualizado y utilizado como referencia de aprendizaje, pero
**no está permitido** su uso comercial, distribución o reproducción sin autorización
expresa del autor.

Para consultas sobre uso comercial, contactar a: andycipleu@gmail.com
