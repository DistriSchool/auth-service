-- V2__create_users_table.sql

CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')),
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    reset_password_token VARCHAR(255),
    reset_password_expires TIMESTAMP,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
    );

-- Índices para melhor performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_verification_token ON users(verification_token);
CREATE INDEX idx_users_reset_password_token ON users(reset_password_token);
CREATE INDEX idx_users_role ON users(role);

-- Comentários
COMMENT ON TABLE users IS 'Tabela de usuários do sistema escolar';
COMMENT ON COLUMN users.role IS 'Perfil do usuário: ADMIN, TEACHER, STUDENT, PARENT';
COMMENT ON COLUMN users.email_verified IS 'Indica se o email foi verificado';
COMMENT ON COLUMN users.verification_token IS 'Token para verificação de email';
COMMENT ON COLUMN users.reset_password_token IS 'Token para reset de senha';
COMMENT ON COLUMN users.reset_password_expires IS 'Data de expiração do token de reset';