CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id,role_id),

    CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
    FOREIGN KEY (role_id)
    REFERENCES roles(id)
    ON DELETE RESTRICT
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

COMMENT ON TABLE user_roles IS 'Tabla de unión para asignar roles a usuarios (many-to-many)';
COMMENT ON COLUMN user_roles.user_id IS 'ID del usuario';
COMMENT ON COLUMN user_roles.role_id IS 'ID del rol asignado';
COMMENT ON COLUMN user_roles.assigned_at IS 'Timestamp de cuándo se asignó el rol';