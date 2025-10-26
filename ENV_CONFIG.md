# Environment Configuration

This application uses an environment-based configuration pattern that allows seamless deployment across different environments (local, Docker, production).

## Configuration Pattern

The configuration follows a **defaults + overrides** pattern:

```hocon
# ---- defaults (local) ----
app.db {
  host = "localhost"        # <— local default
  port = 5432
  name = "todo_db"
  user = "postgres"
  pass = "macig"
}

# ---- env overrides (Docker/prod) ----
app.db.host = ${?DB_HOST}
app.db.port = ${?DB_PORT}
app.db.name = ${?DB_NAME}
app.db.user = ${?DB_USER}
app.db.pass = ${?DB_PASS}
```

The `=${?VARIABLE_NAME}` syntax means: "use the environment variable if it exists, otherwise use the default value."

## Environment Variables

### Database Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | Database hostname |
| `DB_PORT` | 5432 | Database port |
| `DB_NAME` | todo_db | Database name |
| `DB_USER` | postgres | Database username |
| `DB_PASS` | macig | Database password |

### Email Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `MAIL_PASSWORD` | (required in prod) | SMTP password |
| `MAIL_USER` | softwareeng.murat.keskin@gmail.com | SMTP username |
| `MAIL_HOST` | smtp.gmail.com | SMTP host |
| `MAIL_PORT` | 587 | SMTP port |

### Application Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `APP_BASE_URL` | http://localhost:9000 | Base URL for the application |
| `ADMIN_PASSWORD` | admin123 | Admin user password |

## Usage Examples

### Local Development
No environment variables needed - uses defaults from `application.conf`:
```bash
sbt run
```

### Docker Deployment
Environment variables are set in `docker-compose.yml`:
```bash
docker-compose up
```

### Production Deployment
Set environment variables in your production environment:
```bash
export DB_HOST=production-db.example.com
export DB_NAME=prod_todo_db
export DB_USER=todo_user
export DB_PASS=secure_password
export MAIL_PASSWORD=your_smtp_password

# Run the application
./bin/todo
```

## Benefits

1. **Single Configuration File**: All configuration logic in one place
2. **Environment-Specific Overrides**: Easy to override for different environments
3. **Security**: Sensitive data (passwords) can be provided via environment variables
4. **Flexibility**: Works seamlessly in local development and Docker/production
5. **Clear Documentation**: Configuration structure is self-documenting

## Connection Pool Configuration

The Slick configuration includes optimized connection pool settings:

```hocon
slick.dbs.default.db {
  connectionTimeout = 30000      # 30 seconds
  maximumPoolSize   = 10         # Max connections
  idleTimeout       = 600000     # 10 minutes
  keepAliveConnection = true     # Keep connections alive
}
```
