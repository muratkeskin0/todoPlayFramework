# Todo App Development Helper
param(
    [Parameter(Position=0)]
    [string]$Command = "help"
)

# Todo App Development Helper Script

# Main execution
switch ($Command.ToLower()) {
    "build" { 
        Write-Host "Building application..." -ForegroundColor Blue
        sbt dist
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Build failed!" -ForegroundColor Red
            exit 1
        }
        Write-Host "Building Docker image..." -ForegroundColor Blue
        docker-compose build --no-cache
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Docker build failed!" -ForegroundColor Red
            exit 1
        }
        Write-Host "Build completed!" -ForegroundColor Green
    }
    "run" { 
        Write-Host "Starting containers..." -ForegroundColor Blue
        docker-compose up -d
        Write-Host "Containers started!" -ForegroundColor Green
    }
    "clean" { 
        Write-Host "Cleaning up..." -ForegroundColor Blue
        docker-compose down -v
        Write-Host "Cleanup completed!" -ForegroundColor Green
    }
    "restart" { 
        Write-Host "Cleaning up..." -ForegroundColor Blue
        docker-compose down -v
        Write-Host "Cleanup completed!" -ForegroundColor Green
        
        Write-Host "Building application..." -ForegroundColor Blue
        sbt dist
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Build failed!" -ForegroundColor Red
            exit 1
        }
        Write-Host "Building Docker image..." -ForegroundColor Blue
        docker-compose build --no-cache
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Docker build failed!" -ForegroundColor Red
            exit 1
        }
        Write-Host "Build completed!" -ForegroundColor Green
        
        Write-Host "Starting containers..." -ForegroundColor Blue
        docker-compose up -d
        Write-Host "Containers started!" -ForegroundColor Green
        Write-Host "Restart completed!" -ForegroundColor Green
    }
    "dev" { 
        Write-Host "Building application..." -ForegroundColor Blue
        sbt dist
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Build failed!" -ForegroundColor Red
            exit 1
        }
        Write-Host "Building Docker image..." -ForegroundColor Blue
        docker-compose build --no-cache
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Docker build failed!" -ForegroundColor Red
            exit 1
        }
        Write-Host "Build completed!" -ForegroundColor Green
        
        Write-Host "Starting containers..." -ForegroundColor Blue
        docker-compose up -d
        Write-Host "Containers started!" -ForegroundColor Green
        
        Write-Host ""
        Write-Host "Development environment ready!" -ForegroundColor Green
        Write-Host "Open: http://localhost:9000" -ForegroundColor Cyan
        Write-Host "View logs: .\dev.ps1 logs" -ForegroundColor Cyan
    }
    "logs" { 
        Write-Host "Viewing application logs..." -ForegroundColor Blue
        docker-compose logs -f todo-app
    }
    "status" { 
        Write-Host "Container status:" -ForegroundColor Blue
        docker-compose ps
    }
    "test" { 
        Write-Host "Testing application..." -ForegroundColor Blue
        $response = Invoke-WebRequest -Uri "http://localhost:9000" -UseBasicParsing -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "Application is responding!" -ForegroundColor Green
        } else {
            Write-Host "Application not responding!" -ForegroundColor Red
        }
    }
    "help" { 
        Write-Host "Todo App Development Helper" -ForegroundColor Green
        Write-Host ""
        Write-Host "Usage: .\dev.ps1 [command]"
        Write-Host ""
        Write-Host "Commands:" -ForegroundColor Yellow
        Write-Host "  build    - Build application and Docker image"
        Write-Host "  run      - Start containers"
        Write-Host "  clean    - Stop and remove everything (including volumes)"
        Write-Host "  restart  - Clean + Build + Run"
        Write-Host "  dev      - Build + Run (development workflow)"
        Write-Host "  logs     - View application logs"
        Write-Host "  status   - Check container status"
        Write-Host "  test     - Quick HTTP test"
        Write-Host "  help     - Show this help"
    }
    default { 
        Write-Host "Todo App Development Helper" -ForegroundColor Green
        Write-Host ""
        Write-Host "Usage: .\dev.ps1 [command]"
        Write-Host ""
        Write-Host "Commands:" -ForegroundColor Yellow
        Write-Host "  build    - Build application and Docker image"
        Write-Host "  run      - Start containers"
        Write-Host "  clean    - Stop and remove everything (including volumes)"
        Write-Host "  restart  - Clean + Build + Run"
        Write-Host "  dev      - Build + Run (development workflow)"
        Write-Host "  logs     - View application logs"
        Write-Host "  status   - Check container status"
        Write-Host "  test     - Quick HTTP test"
        Write-Host "  help     - Show this help"
    }
}
