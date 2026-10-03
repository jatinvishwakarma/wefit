@echo off
set PATH=C:\Users\jatin\AppData\Roaming\npm;C:\Program Files\nodejs\;C:\Windows\System32;C:\Windows;
cd c:\Wefit\wefit-web
echo Running npm install...
call npm install
echo Starting frontend...
call npm run dev
