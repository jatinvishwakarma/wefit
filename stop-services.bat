@echo off
echo ========================================
echo   Stopping all Wefit services...
echo ========================================

echo Stopping Eureka...
taskkill /FI "WINDOWTITLE eq Eureka*" /T /F > nul 2>&1

echo Stopping Config Server...
taskkill /FI "WINDOWTITLE eq ConfigServer*" /T /F > nul 2>&1

echo Stopping User Service...
taskkill /FI "WINDOWTITLE eq UserService*" /T /F > nul 2>&1

echo Stopping Activity Service...
taskkill /FI "WINDOWTITLE eq ActivityService*" /T /F > nul 2>&1

echo Stopping AI Service...
taskkill /FI "WINDOWTITLE eq AiService*" /T /F > nul 2>&1

echo Stopping API Gateway...
taskkill /FI "WINDOWTITLE eq ApiGateway*" /T /F > nul 2>&1

echo ========================================
echo   All services have been stopped!
echo ========================================
pause
