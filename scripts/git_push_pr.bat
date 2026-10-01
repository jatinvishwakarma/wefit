@echo off
setlocal EnableDelayedExpansion

cd /d c:\Wefit

echo.
echo ========================================
echo  Wefit - Git Push and PR Setup
echo ========================================
echo.

:: Check current branch
for /f "tokens=*" %%i in ('git branch --show-current') do set CURRENT_BRANCH=%%i
echo [INFO] Current branch: %CURRENT_BRANCH%

:: Create or switch to feature branch
set BRANCH=feature/SCRUM-42-47-ai-coach-nutrition-analytics-challenges
git checkout -b %BRANCH% 2>nul
if errorlevel 1 (
    echo [INFO] Branch exists, switching...
    git checkout %BRANCH%
)

echo.
echo [STEP 1] Removing deleted junk files from git tracking...
git rm --cached "gh-cli/LICENSE" 2>nul
git rm --cached "gh-cli/bin/gh.exe" 2>nul
git rm --cached "gh.zip" 2>nul
git rm --cached "powershell.bat" 2>nul
echo Done.

echo.
echo [STEP 2] Staging all changes...
git add -A
echo Done.

echo.
echo [STEP 3] Git status (staged files):
git status --short

echo.
echo [STEP 4] Committing...
git commit -m "feat: SCRUM-42/43/44/45/47 - AI Coach, Workout Plans, Nutrition, Analytics, Challenges" -m "SCRUM-42: AI Personal Coach with ChatSession (MongoDB) + Gemini multi-turn chat" -m "SCRUM-43: Workout Plan Generator with structured JSON Gemini prompting" -m "SCRUM-44: Nutrition Plans with BMR/TDEE calculation (Mifflin-St Jeor)" -m "SCRUM-45: Progress Analytics + Burnout Risk detection via Gemini" -m "SCRUM-47: Challenge Engine with join/progress/completion + Kafka events" -m "Updated .gitignore: exclude certs, IDE, agent state, scratch files" -m "Removed duplicate pom.xml test dependency in activityService" -m "API Gateway: added /api/v1/challenges/** to activity-service route" -m "Closes SCRUM-42, Closes SCRUM-43, Closes SCRUM-44, Closes SCRUM-45, Closes SCRUM-47"

if errorlevel 1 (
    echo [WARN] Nothing to commit or commit failed.
) else (
    echo [OK] Committed successfully.
)

echo.
echo [STEP 5] Pushing to GitHub...
git push -u origin %BRANCH%

if errorlevel 1 (
    echo [ERROR] Push failed. You may need to authenticate.
    echo Try running: git push -u origin %BRANCH%
) else (
    echo.
    echo ========================================
    echo  SUCCESS! Now create your PR at:
    echo.
    echo  https://github.com/jatinvishwakarma/wefit/compare/%BRANCH%
    echo.
    echo  Click "Compare ^& pull request"
    echo ========================================
    
    :: Open PR page in browser automatically
    start "" "https://github.com/jatinvishwakarma/wefit/compare/%BRANCH%"
)

echo.
pause
