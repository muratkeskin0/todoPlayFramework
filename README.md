# Todo Application

A Scala Play Framework based todo application with user management, authentication, and email notifications.

## Features

- **User Management**: Registration, login, user roles (admin/basic)
- **Todo Management**: Create, edit, delete, and mark todos as completed
- **Authentication**: Secure login with password hashing
- **Email Notifications**: Welcome emails sent via actor-based system
- **Structured Logging**: JSON format request/response logging
- **Docker Support**: Full containerization with PostgreSQL database
- **Database Migrations**: Automatic database schema setup

## Tech Stack

- **Backend**: Scala 2.13, Play Framework 3.0
- **Database**: PostgreSQL 15
- **Actors**: Pekko (Apache Pekko)
- **Email**: Play Mailer with Gmail SMTP
- **Containerization**: Docker, Docker Compose
- **Password Hashing**: BCrypt

## Quick Start with Docker

### Prerequisites

- Docker and Docker Compose installed
- Gmail App Password for email functionality

### 1. Environment Setup

Create a `.env` file in the project root:

```bash
# Email Configuration
MAIL_PASSWORD=your-gmail-app-password

# Optional: Override database configuration (defaults work for Docker Compose)
# DB_HOST=postgres
# DB_PORT=5432
# DB_NAME=todo_db
# DB_USER=postgres
# DB_PASS=macig
```

See [ENV_CONFIG.md](ENV_CONFIG.md) for all available environment variables.

### 2. Build and Run

```bash
# Build and start all services
docker-compose up --build

# Or run in background
docker-compose up --build -d
```

### 3. Access the Application

- **Application**: http://localhost:9000
- **Database**: localhost:5432 (postgres/macig)

### 4. Default Admin User

The application automatically creates an admin user on startup:

- **Email**: admin@todoapp.com
- **Password**: admin123
- **Role**: Admin

## Development Setup

### Prerequisites

- Java 17+
- sbt 1.8+
- PostgreSQL 15+

### 1. Database Setup

```bash
# Start PostgreSQL
docker-compose up postgres -d

# Or use local PostgreSQL
createdb todo_db
```

### 2. Run Migrations

```bash
# Migrations run automatically in Docker
# For local development, run manually:
psql -U postgres -d todo_db -f migrations/2025.10.14_Todos_Table.sql
psql -U postgres -d todo_db -f migrations/2025.10.21_Users_Table.sql
psql -U postgres -d todo_db -f migrations/2025.10.22_Todos_Add_User_FK.sql
```

### 3. Configuration

The application uses **environment-based configuration** with sensible defaults for local development. See [ENV_CONFIG.md](ENV_CONFIG.md) for detailed configuration options.

For local development, you can use the defaults. For production, configure environment variables:

```bash
# Database Configuration
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=todo_db
export DB_USER=postgres
export DB_PASS=macig

# Email Configuration
export MAIL_PASSWORD=your-gmail-app-password
```

### 4. Run Application

```bash
# Development mode
sbt run

# Production build
sbt dist
```

## Docker Services

### todo-app
- **Port**: 9000
- **Dependencies**: postgres, migration
- **Features**: Main application with startup hooks

### postgres
- **Port**: 5432
- **Database**: todo_db
- **Health Check**: Automatic readiness check

### migration
- **Purpose**: Runs database migrations
- **Dependencies**: postgres (healthy)
- **Runs**: Before todo-app starts

## Project Structure

```
app/
├── actors/           # Email actor system
├── controllers/      # HTTP controllers
├── filters/          # Request/response logging
├── models/           # Data models
├── repositories/     # Data access layer
├── security/         # Password hashing
├── services/         # Business logic
├── startup/          # Application startup hooks
└── views/            # Twirl templates

conf/
├── application.conf  # Application configuration
├── logback.xml       # Logging configuration
└── routes            # URL routing

migrations/           # Database migration scripts
public/              # Static assets
```

## Configuration

The application uses **environment-based configuration** with sensible defaults. All configuration is managed through `conf/application.conf` with environment variable overrides.

**📖 See [ENV_CONFIG.md](ENV_CONFIG.md) for complete configuration documentation.**

### Email Settings

```conf
play.mailer {
  host = "smtp.gmail.com"
  port = 587
  user = "your-email@gmail.com"
  password = ${?MAIL_PASSWORD}
  ssl = false
  tls = true
  mock = false
}
```

### Admin User Settings

```conf
admin {
  email = "admin@todoapp.com"
  firstName = "Admin"
  lastName = "User"
  password = "admin123"
}
```

## Logging

The application uses structured JSON logging:

```json
{
  "type": "REQUEST",
  "requestId": "abc123",
  "timestamp": "2025-10-25T20:00:00Z",
  "method": "POST",
  "uri": "/auth/login",
  "user": {
    "id": "12",
    "email": "user@example.com",
    "authenticated": true
  }
}
```

## API Endpoints

### Authentication
- `POST /auth/login` - User login
- `POST /auth/register` - User registration
- `GET /auth/logout` - User logout

### Todos
- `GET /` - Todo list
- `POST /todos` - Create todo
- `PUT /todos/:id` - Update todo
- `DELETE /todos/:id` - Delete todo

### Users (Admin)
- `GET /users` - User list
- `PUT /users/:id` - Update user

## Troubleshooting

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# Check logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

### Email Issues

```bash
# Check email configuration
docker-compose logs todo-app | grep -i mail

# Verify Gmail App Password
# Make sure MAIL_PASSWORD is set in .env file
```

### Application Issues

```bash
# Check application logs
docker-compose logs todo-app

# Restart application
docker-compose restart todo-app

# Rebuild and restart
docker-compose up --build --force-recreate
```

## Development Commands

```bash
# Compile
sbt compile

# Run tests
sbt test

# Build distribution
sbt dist

# Clean build
sbt clean compile

# Docker commands
docker-compose up --build
docker-compose down
docker-compose logs -f todo-app
docker-compose exec postgres psql -U postgres -d todo_db
```

## License

This project is licensed under the MIT License.