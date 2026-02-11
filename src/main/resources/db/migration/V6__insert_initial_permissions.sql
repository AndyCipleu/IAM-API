-- Insertar permisos iniciales del sistema

-- Permisos de USUARIOS
INSERT INTO permissions (id, name, resource, action, description, created_at, updated_at) VALUES
                                                                                              ('11111111-1111-1111-1111-111111111111', 'READ_USERS', 'USER', 'READ', 'Permite leer información de usuarios', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                              ('22222222-2222-2222-2222-222222222222', 'UPDATE_USERS', 'USER', 'UPDATE', 'Permite actualizar usuarios', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                              ('33333333-3333-3333-3333-333333333333', 'DELETE_USERS', 'USER', 'DELETE', 'Permite eliminar usuarios', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                              ('44444444-4444-4444-4444-444444444444', 'CREATE_USERS', 'USER', 'CREATE', 'Permite crear usuarios', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Permisos de ROLES
INSERT INTO permissions (id, name, resource, action, description, created_at, updated_at) VALUES
                                                                                              ('55555555-5555-5555-5555-555555555555', 'READ_ROLES', 'ROLE', 'READ', 'Permite leer roles', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                              ('66666666-6666-6666-6666-666666666666', 'ASSIGN_ROLES', 'ROLE', 'ASSIGN', 'Permite asignar roles a usuarios', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                              ('77777777-7777-7777-7777-777777777777', 'REVOKE_ROLES', 'ROLE', 'REVOKE', 'Permite revocar roles de usuarios', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Permisos de PERMISOS (meta-permisos)
INSERT INTO permissions (id, name, resource, action, description, created_at, updated_at) VALUES
    ('88888888-8888-8888-8888-888888888888', 'READ_PERMISSIONS', 'PERMISSION', 'READ', 'Permite leer permisos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);