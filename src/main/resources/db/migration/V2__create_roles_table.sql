CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roles_name ON roles(name);

INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
                                                                      -- Rol básico para usuarios registrados
                                                                      ('550e8400-e29b-41d4-a716-446655440001', 'ROLE_USER', 'Usuario estándar con permisos básicos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

                                                                      -- Rol de administrador con todos los permisos
                                                                      ('550e8400-e29b-41d4-a716-446655440002', 'ROLE_ADMIN', 'Administrador del sistema con acceso total', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

                                                                      -- Rol de moderador (ejemplo futuro)
                                                                      ('550e8400-e29b-41d4-a716-446655440003', 'ROLE_MODERATOR', 'Moderador con permisos intermedios', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

COMMENT ON TABLE roles IS 'Roles del sistema que agrupan permisos';
COMMENT ON COLUMN roles.name IS 'Nombre único del rol (prefijo ROLE_ por convención Spring Security)';
COMMENT ON COLUMN roles.description IS 'Descripción legible del propósito del rol';