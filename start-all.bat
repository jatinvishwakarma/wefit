@echo off
echo ========================================
echo   Starting all Wefit services...
echo ========================================

:: Fix broken system PATH for Maven Wrapper and Node.js
set PATH=C:\Windows\System32;C:\Windows;C:\Windows\System32\Wbem;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Program Files\nodejs\;C:\Users\jatin\AppData\Roaming\npm;%PATH%

:: Trust the self-signed certificate globally for all JVMs
set JAVA_TOOL_OPTIONS=-Djavax.net.ssl.trustStore=../certs/keystore.p12 -Djavax.net.ssl.trustStorePassword=changeit


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
docker-compose up -d postgres mongodb zookeeper kafka keycloak redis

echo Creating logs directory...
if not exist logs mkdir logs

echo Starting Eureka Server on port 8761...
start "Eureka" powershell -Command "cd eureka; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\eureka.log"
echo Waiting for Eureka to start...
ping 127.0.0.1 -n 31 > nul

echo Starting Config Server on port 8888...
start "ConfigServer" powershell -Command "cd configServer; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\configServer.log"
echo Waiting for Config Server to start...
ping 127.0.0.1 -n 21 > nul

echo Starting UserService on port 8081...
start "UserService" powershell -Command "cd userService; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\userService.log"
echo Waiting for UserService to start...
ping 127.0.0.1 -n 11 > nul

echo Starting ActivityService on port 8082...
start "ActivityService" powershell -Command "cd activityService; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\activityService.log"

echo Starting AiService on port 8083...
start "AiService" powershell -Command "cd aiService; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\aiService.log"

echo Starting GamificationService on port 8089...
start "GamificationService" powershell -Command "cd gamificationService; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\gamificationService.log"

echo Starting FeedService on port 8084...
start "FeedService" powershell -Command "cd feedService; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\feedService.log"

echo Starting NotificationService on port 8086...
start "NotificationService" powershell -Command "cd notificationService; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\notificationService.log"

echo Starting RelationshipService on port 8085...
start "RelationshipService" powershell -Command "cd relationshipService; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\relationshipService.log"

echo Starting MediaService on port 8087...
start "MediaService" powershell -Command "cd mediaService; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\mediaService.log"

echo Starting ModerationService on port 8088...
start "ModerationService" powershell -Command "cd moderationService; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\moderationService.log"

echo Starting API Gateway on port 8443...
start "ApiGateway" powershell -Command "cd apiGateway; cmd /c '.\mvnw.cmd spring-boot:run 2>&1' | Tee-Object -FilePath ..\logs\apiGateway.log"

echo Starting Wefit Web Frontend...
start "WefitWeb" powershell -Command "cd wefit-web; npm install; cmd /c 'npm run dev 2>&1' | Tee-Object -FilePath ..\logs\wefitWeb.log"

echo Starting Admin Dashboard...
start "AdminDashboard" powershell -Command "cd admin-dashboard; npm install; cmd /c 'npm run dev 2>&1' | Tee-Object -FilePath ..\logs\adminDashboard.log"

echo Starting Mobile App (Expo)...
start "MobileApp" powershell -Command "cd mobile-app; npm install; cmd /c 'npx expo start --port 8091 --web 2>&1' | Tee-Object -FilePath ..\logs\mobileApp.log"

echo.
echo ========================================
echo   All services have been launched!
echo ========================================
echo   Eureka:              http://localhost:8761
echo   Config Server:       http://localhost:8888
echo   API Gateway:         https://localhost:8443
echo   UserService:         http://localhost:8081
echo   ActivityService:     http://localhost:8082
echo   AiService:           http://localhost:8083
echo   FeedService:         http://localhost:8084
echo   RelationshipService: http://localhost:8085
echo   NotificationService: http://localhost:8086
echo   MediaService:        http://localhost:8087
echo   ModerationService:   http://localhost:8088
echo   GamificationService: http://localhost:8089
echo   Wefit Web:           http://localhost:5173
echo   Admin Dashboard:     http://localhost:3000
echo   Mobile App:          http://localhost:8081 (Expo)
echo   Keycloak:            http://localhost:8090
echo ========================================
pause
