-- Insertar roles iniciales del sistema

-- ROLE_USER: Usuario estándar
INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ROLE_USER', 'Usuario estándar del sistema', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ROLE_ADMIN: Administrador con todos los permisos
INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'ROLE_ADMIN', 'Administrador del sistema con acceso completo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ROLE_MODERATOR: Moderador con permisos limitados
INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'ROLE_MODERATOR', 'Moderador con permisos de lectura y actualización', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- ============================================
-- Asignar permisos a ROLE_USER
-- ============================================
-- Solo puede leer su propio perfil
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111');  -- READ_USERS


-- ============================================
-- Asignar permisos a ROLE_MODERATOR
-- ============================================
-- Puede leer y actualizar usuarios, pero no eliminar
INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          ('cccccccc-cccc-cccc-cccc-cccccccccccc', '11111111-1111-1111-1111-111111111111'),  -- READ_USERS
                                                          ('cccccccc-cccc-cccc-cccc-cccccccccccc', '22222222-2222-2222-2222-222222222222'),  -- UPDATE_USERS
                                                          ('cccccccc-cccc-cccc-cccc-cccccccccccc', '55555555-5555-5555-5555-555555555555');  -- READ_ROLES


-- ============================================
-- Asignar permisos a ROLE_ADMIN
-- ============================================
-- Tiene TODOS los permisos
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- Permisos de usuarios
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111'),  -- READ_USERS
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222'),  -- UPDATE_USERS
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333'),  -- DELETE_USERS
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '44444444-4444-4444-4444-444444444444'),  -- CREATE_USERS

-- Permisos de roles
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '55555555-5555-5555-5555-555555555555'),  -- READ_ROLES
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '66666666-6666-6666-6666-666666666666'),  -- ASSIGN_ROLES
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '77777777-7777-7777-7777-777777777777'),  -- REVOKE_ROLES

-- Permisos de permisos
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '88888888-8888-8888-8888-888888888888');  -- READ_PERMISSIONS