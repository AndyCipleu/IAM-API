CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
    FOREIGN KEY (role_id)
    REFERENCES roles(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
    FOREIGN KEY (permission_id)
    REFERENCES permissions(id)
    ON DELETE CASCADE
);


CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);




INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001');


INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          -- Permisos de usuarios
                                                          ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001'),  -- USER_READ
                                                          ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440002'),  -- USER_WRITE
                                                          ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440003'),  -- USER_DELETE

                                                          -- Permisos de roles
                                                          ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440004'),  -- ROLE_READ
                                                          ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440005'),  -- ROLE_WRITE
                                                          ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440006'),  -- ROLE_DELETE
                                                          ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440007'),  -- ROLE_ASSIGN

                                                          -- Permisos de permisos
                                                          ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440008'),  -- PERMISSION_READ
                                                          ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440009'),  -- PERMISSION_WRITE
                                                          ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440010'),  -- PERMISSION_ASSIGN

                                                          -- Permisos de auditoría
                                                          ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440011');  -- AUDIT_READ


INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          ('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001'),  -- USER_READ
                                                          ('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440002'),  -- USER_WRITE
                                                          ('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440004'),  -- ROLE_READ
                                                          ('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440011');  -- AUDIT_READ


COMMENT ON TABLE role_permissions IS 'Tabla de unión para asignar permisos a roles (many-to-many)';