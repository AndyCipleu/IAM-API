CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    account_non_locked BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);

--Comentarios informativos para tabla y columna
COMMENT ON TABLE users IS 'Tabla principal de usuarios del sistema IAM';
COMMENT ON COLUMN users.id IS 'Identificador único UUID del usuario';
COMMENT ON COLUMN users.email IS 'Email del usuario (usado como username para login)';
COMMENT ON COLUMN users.password IS 'Contraseña hasheada con BCrypt (60 caracteres)';
COMMENT ON COLUMN users.enabled IS 'Indica si el usuario puede autenticarse';
COMMENT ON COLUMN users.account_non_locked IS 'Indica si la cuenta NO está bloqueada';