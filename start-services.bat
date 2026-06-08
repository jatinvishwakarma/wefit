@echo off
echo Starting Eureka Server...
start "Eureka" cmd /k "cd eureka && mvnw.cmd spring-boot:run"
echo Waiting for Eureka to start...
timeout /t 30 /nobreak > nul

echo Starting UserService on port 8081...
start "UserService" cmd /k "cd userService && mvnw.cmd spring-boot:run"
echo Waiting for UserService to start...
timeout /t 15 /nobreak > nul

echo Starting ActivityService on port 8082...
start "ActivityService" cmd /k "cd activityService && mvnw.cmd spring-boot:run"

echo All services started!
echo Eureka: http://localhost:8761
echo UserService: http://localhost:8081
echo ActivityService: http://localhost:8082
pause