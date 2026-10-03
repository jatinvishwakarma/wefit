@echo off
set PATH=C:\Users\jatin\AppData\Local\Programs\Python\Python312\Scripts\;C:\Users\jatin\AppData\Local\Programs\Python\Python312\;C:\Program Files\Java\jdk-21\bin;C:\ProgramData\chocolatey\bin;C:\Program Files\Git\cmd;C:\Program Files\Microsoft VS Code\bin;C:\Program Files\Docker\Docker\resources\bin;C:\Program Files\dotnet\;C:\Program Files\nodejs\;C:\Zeotap_Assesment\apache-maven-3.9.9\bin;C:\Windows\System32;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Windows;

echo Running all Maven tests to verify fixes...

cd userService
call mvnw.cmd clean test
if %errorlevel% neq 0 exit /b %errorlevel%
cd ..

cd activityService
call mvnw.cmd clean test
if %errorlevel% neq 0 exit /b %errorlevel%
cd ..

cd aiService
call mvnw.cmd clean test
if %errorlevel% neq 0 exit /b %errorlevel%
cd ..

echo.
echo Core backend tests passed successfully!
