#!/usr/bin/env pwsh
# Run this from c:\Wefit in PowerShell to stage, commit, push and open a PR

Set-Location "c:\Wefit"

# ─── 1. Create feature branch ─────────────────────────────────────────────────
git checkout -b feature/SCRUM-42-47-ai-coach-nutrition-analytics-challenges

# ─── 2. Remove deleted junk that was previously tracked ───────────────────────
git rm --cached gh-cli/LICENSE gh-cli/bin/gh.exe gh.zip powershell.bat 2>$null

# ─── 3. Stage everything ──────────────────────────────────────────────────────
git add -A

# ─── 4. Commit ────────────────────────────────────────────────────────────────
git commit -m "feat: SCRUM-42/43/44/45/47 - AI Coach, Workout Plans, Nutrition, Analytics, Challenges

SCRUM-42: AI Personal Coach
- ChatSession MongoDB entity with conversation history (last 10 msgs context window)
- AiCoachService integrating Gemini with SafetyEvaluationService
- POST /api/v1/ai/coach/{userId}/chat

SCRUM-43: Smart Workout Plan Generator
- WorkoutPlan MongoDB entity with structured daily schedules & exercises
- WorkoutPlanService using structured JSON Gemini prompting
- POST /api/v1/ai/workout-plans/{userId}/generate
- GET  /api/v1/ai/workout-plans/{userId}

SCRUM-44: Nutrition Recommendations
- NutritionPlan MongoDB entity
- BMR/TDEE calculation via Mifflin-St Jeor equation
- Gemini-generated meal suggestions for 4 daily meals
- POST /api/v1/ai/nutrition-plans/{userId}/generate

SCRUM-45: Progress Analytics & Burnout Risk Detection
- AnalyticsReport MongoDB entity
- Burnout risk score (0-10), progress analysis, predictive insights
- POST /api/v1/ai/analytics/{userId}/generate

SCRUM-47: Challenge Engine
- Challenge + UserChallenge entities in activityService
- Full lifecycle: create, join (duplicate guard), progress update
- Auto-completion detection + Kafka publish to challenge-completed topic
- POST /api/v1/challenges, GET /active, join, progress, GET /user/{id}

Also:
- Updated .gitignore: certs, IDE configs, agent state, scratch excluded
- Added /api/v1/challenges/** to api-gateway activity-service route
- SafetyEvaluationService integrated across all AI endpoints

Closes SCRUM-42, SCRUM-43, SCRUM-44, SCRUM-45, SCRUM-47"

# ─── 5. Push ──────────────────────────────────────────────────────────────────
git push -u origin feature/SCRUM-42-47-ai-coach-nutrition-analytics-challenges

Write-Host ""
Write-Host "✅ Pushed! Now open a PR at:"
Write-Host "   https://github.com/jatinvishwakarma/wefit/compare/feature/SCRUM-42-47-ai-coach-nutrition-analytics-challenges"
