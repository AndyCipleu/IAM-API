CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_resource_action UNIQUE (resource, action)
);


CREATE INDEX idx_permissions_name ON permissions(name);
CREATE INDEX idx_permissions_resource ON permissions(resource);


INSERT INTO permissions (id, name, resource, action, description) VALUES
                                                                      ('650e8400-e29b-41d4-a716-446655440001', 'USER_READ', 'USER', 'READ', 'Leer información de usuarios'),
                                                                      ('650e8400-e29b-41d4-a716-446655440002', 'USER_WRITE', 'USER', 'WRITE', 'Crear y actualizar usuarios'),
                                                                      ('650e8400-e29b-41d4-a716-446655440003', 'USER_DELETE', 'USER', 'DELETE', 'Eliminar usuarios'),


                                                                      ('650e8400-e29b-41d4-a716-446655440004', 'ROLE_READ', 'ROLE', 'READ', 'Leer roles'),
                                                                      ('650e8400-e29b-41d4-a716-446655440005', 'ROLE_WRITE', 'ROLE', 'WRITE', 'Crear y actualizar roles'),
                                                                      ('650e8400-e29b-41d4-a716-446655440006', 'ROLE_DELETE', 'ROLE', 'DELETE', 'Eliminar roles'),
                                                                      ('650e8400-e29b-41d4-a716-446655440007', 'ROLE_ASSIGN', 'ROLE', 'ASSIGN', 'Asignar roles a usuarios'),


                                                                      ('650e8400-e29b-41d4-a716-446655440008', 'PERMISSION_READ', 'PERMISSION', 'READ', 'Leer permisos'),
                                                                      ('650e8400-e29b-41d4-a716-446655440009', 'PERMISSION_WRITE', 'PERMISSION', 'WRITE', 'Crear y actualizar permisos'),
                                                                      ('650e8400-e29b-41d4-a716-446655440010', 'PERMISSION_ASSIGN', 'PERMISSION', 'ASSIGN', 'Asignar permisos a roles'),


                                                                      ('650e8400-e29b-41d4-a716-446655440011', 'AUDIT_READ', 'AUDIT', 'READ', 'Leer logs de auditoría');


COMMENT ON TABLE permissions IS 'Permisos granulares del sistema';
COMMENT ON COLUMN permissions.name IS 'Nombre único del permiso (formato: RESOURCE_ACTION)';
COMMENT ON COLUMN permissions.resource IS 'Recurso sobre el que aplica (USER, ROLE, etc.)';
COMMENT ON COLUMN permissions.action IS 'Acción permitida (READ, WRITE, DELETE, etc.)';
COMMENT ON CONSTRAINT uk_resource_action ON permissions IS 'Un recurso solo puede tener una acción específica';