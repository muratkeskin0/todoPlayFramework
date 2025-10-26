-- Users table migration (Updated - Added role field)

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'basic' CHECK (role IN ('admin', 'basic')),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    password VARCHAR(255) NOT NULL DEFAULT ''
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- Add comments for documentation
COMMENT ON TABLE users IS 'User management table';
COMMENT ON COLUMN users.id IS 'Primary key, auto-increment';
COMMENT ON COLUMN users.email IS 'Unique email address (used as username)';
COMMENT ON COLUMN users.first_name IS 'Optional first name, max 100 characters';
COMMENT ON COLUMN users.last_name IS 'Optional last name, max 100 characters';
COMMENT ON COLUMN users.role IS 'User role: admin or basic, default basic';
COMMENT ON COLUMN users.is_active IS 'User active status, default true';
COMMENT ON COLUMN users.created_at IS 'User creation timestamp';
COMMENT ON COLUMN users.updated_at IS 'Last update timestamp';