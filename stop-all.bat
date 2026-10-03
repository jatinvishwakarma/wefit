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

echo Stopping Relationship Service...
taskkill /FI "WINDOWTITLE eq RelationshipService*" /T /F > nul 2>&1

echo Stopping Media Service...
taskkill /FI "WINDOWTITLE eq MediaService*" /T /F > nul 2>&1

echo Stopping Moderation Service...
taskkill /FI "WINDOWTITLE eq ModerationService*" /T /F > nul 2>&1

echo Stopping API Gateway...
taskkill /FI "WINDOWTITLE eq ApiGateway*" /T /F > nul 2>&1

echo Stopping Frontend...
taskkill /FI "WINDOWTITLE eq Frontend*" /T /F > nul 2>&1

echo Stopping Docker dependencies...
docker-compose stop

echo ========================================
echo   All services have been stopped!
echo ========================================
pause
