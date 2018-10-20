@echo off
gradle build-jar
if %errorlevel% neq 0 exit /b %errorlevel%
java -jar echoClient.jar
