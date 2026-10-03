@echo off
set "DB_PASSWORD=Jatin1307@"
set PATH=C:\Windows\System32;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Program Files\Java\jdk-21\bin;C:\Zeotap_Assesment\apache-maven-3.9.9\bin;%PATH%
cd c:\Wefit\relationshipService
mvn.cmd spring-boot:run > c:\Wefit\scratch\logs\relationship.log 2>&1
