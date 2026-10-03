@echo off
echo ========================================
echo   Starting all Wefit services...
echo ========================================

:: Fix broken system PATH for Maven Wrapper and Node.js
set PATH=C:\Windows\System32;C:\Windows;C:\Windows\System32\Wbem;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Program Files\nodejs\;C:\Users\jatin\AppData\Roaming\npm;%PATH%


:: Load environment variables from .env if it exists
if exist .env (
    echo Loading environment variables from .env...
    for /f "usebackq tokens=* eol=#" %%i in (".env") do (
        set "%%i"
    )
)

echo Checking for certificates...
if exist generate-certs.bat call generate-certs.bat

echo Starting Docker dependencies (Postgres, MongoDB, Kafka, Keycloak, etc.)...
docker-compose up -d postgres mongodb zookeeper kafka keycloak redis minio

echo Starting Eureka Server on port 8761...
start "Eureka" cmd /k "cd eureka && mvnw.cmd spring-boot:run"
echo Waiting for Eureka to start...
ping 127.0.0.1 -n 31 > nul

echo Starting Config Server on port 8888...
start "ConfigServer" cmd /k "cd configServer && mvnw.cmd spring-boot:run"
echo Waiting for Config Server to start...
ping 127.0.0.1 -n 21 > nul

echo Starting UserService on port 8081...
start "UserService" cmd /k "cd userService && mvnw.cmd spring-boot:run"
echo Waiting for UserService to start...
ping 127.0.0.1 -n 11 > nul

echo Starting ActivityService on port 8082...
start "ActivityService" cmd /k "cd activityService && mvnw.cmd spring-boot:run"

echo Starting AiService on port 8083...
start "AiService" cmd /k "cd aiService && mvnw.cmd spring-boot:run"

echo Starting RelationshipService on port 8085...
start "RelationshipService" cmd /k "cd relationshipService && mvnw.cmd spring-boot:run"

echo Starting MediaService on port 8087...
start "MediaService" cmd /k "cd mediaService && mvnw.cmd spring-boot:run"

echo Starting ModerationService on port 8088...
start "ModerationService" cmd /k "cd moderationService && mvnw.cmd spring-boot:run"

echo Starting API Gateway on port 8443...
start "ApiGateway" cmd /k "cd apiGateway && mvnw.cmd spring-boot:run"

echo Starting Frontend...
start "Frontend" cmd /k "cd wefit-web && npm install && npm run dev"

echo.
echo ========================================
echo   All services have been launched!
echo ========================================
echo   Eureka:              http://localhost:8761
echo   Config Server:       http://localhost:8888
echo   API Gateway:         http://localhost:8443
echo   UserService:         http://localhost:8081
echo   ActivityService:     http://localhost:8082
echo   AiService:           http://localhost:8083
echo   RelationshipService: http://localhost:8085
echo   MediaService:        http://localhost:8087
echo   ModerationService:   http://localhost:8088
echo   Frontend:            http://localhost:5173
echo   Keycloak:            http://localhost:8084
echo ========================================
pause
