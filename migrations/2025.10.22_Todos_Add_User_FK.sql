-- Add user_id foreign key to todos table

-- Add user_id column to todos table
ALTER TABLE todos ADD COLUMN user_id BIGINT;

-- Add foreign key constraint
ALTER TABLE todos ADD CONSTRAINT fk_todos_user_id 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_todos_user_id ON todos(user_id);

-- Add comment for documentation
COMMENT ON COLUMN todos.user_id IS 'Foreign key reference to users table';
