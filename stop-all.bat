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

echo Stopping Gamification Service...
taskkill /FI "WINDOWTITLE eq GamificationService*" /T /F > nul 2>&1

echo Stopping Feed Service...
taskkill /FI "WINDOWTITLE eq FeedService*" /T /F > nul 2>&1

echo Stopping Notification Service...
taskkill /FI "WINDOWTITLE eq NotificationService*" /T /F > nul 2>&1

echo Stopping Relationship Service...
taskkill /FI "WINDOWTITLE eq RelationshipService*" /T /F > nul 2>&1

echo Stopping Media Service...
taskkill /FI "WINDOWTITLE eq MediaService*" /T /F > nul 2>&1

echo Stopping Moderation Service...
taskkill /FI "WINDOWTITLE eq ModerationService*" /T /F > nul 2>&1

echo Stopping API Gateway...
taskkill /FI "WINDOWTITLE eq ApiGateway*" /T /F > nul 2>&1

echo Stopping Wefit Web...
taskkill /FI "WINDOWTITLE eq WefitWeb*" /T /F > nul 2>&1

echo Stopping Admin Dashboard...
taskkill /FI "WINDOWTITLE eq AdminDashboard*" /T /F > nul 2>&1

echo Stopping Mobile App...
taskkill /FI "WINDOWTITLE eq MobileApp*" /T /F > nul 2>&1

echo Stopping Docker dependencies...
docker-compose stop

echo ========================================
echo   All services have been stopped!
echo ========================================
pause
