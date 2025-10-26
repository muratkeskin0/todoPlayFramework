#!/bin/bash

# Migration runner script
echo "🚀 Starting database migrations..."

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
until pg_isready -h postgres -p 5432 -U postgres; do
  echo "PostgreSQL is unavailable - sleeping"
  sleep 2
done

echo "✅ PostgreSQL is ready!"

# Run migrations in order
echo "📝 Running migration: Todos Table"
psql -h postgres -U postgres -d todo_db -f /migrations/2025.10.14_Todos_Table.sql

echo "📝 Running migration: Users Table"
psql -h postgres -U postgres -d todo_db -f /migrations/2025.10.21_Users_Table.sql

echo "📝 Running migration: Todos Add User FK"
psql -h postgres -U postgres -d todo_db -f /migrations/2025.10.22_Todos_Add_User_FK.sql

echo "🎉 All migrations completed successfully!"
