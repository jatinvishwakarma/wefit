@echo off
:: Load environment variables from .env if it exists
if exist .env (
    echo Loading environment variables from .env...
    for /f "usebackq tokens=* eol=#" %%i in (".env") do (
        set "%%i"
    )
)

echo Starting Eureka Server on port 8761...
start "Eureka" cmd /k "cd eureka && mvnw.cmd spring-boot:run"
echo Waiting for Eureka to start...
timeout /t 30 /nobreak > nul

echo Starting Config Server on port 8888...
start "ConfigServer" cmd /k "cd configServer && mvnw.cmd spring-boot:run"
echo Waiting for Config Server to start...
timeout /t 20 /nobreak > nul

echo Starting UserService on port 8081...
start "UserService" cmd /k "cd userService && mvnw.cmd spring-boot:run"
echo Waiting for UserService to start...
timeout /t 15 /nobreak > nul

echo Starting ActivityService on port 8082...
start "ActivityService" cmd /k "cd activityService && mvnw.cmd spring-boot:run"
echo Waiting for ActivityService to start...
timeout /t 15 /nobreak > nul

echo Starting AiService on port 8083...
start "AiService" cmd /k "cd aiService && mvnw.cmd spring-boot:run"
echo Waiting for AiService to start...
timeout /t 15 /nobreak > nul

echo Starting API Gateway on port 8080...
start "ApiGateway" cmd /k "cd apiGateway && mvnw.cmd spring-boot:run"

echo.
echo ========================================
echo   All services started!
echo ========================================
echo   Eureka:           http://localhost:8761
echo   Config Server:    http://localhost:8888
echo   API Gateway:      http://localhost:8080
echo   UserService:      http://localhost:8081
echo   ActivityService:  http://localhost:8082
echo   AiService:        http://localhost:8083
echo ========================================
pause
