@echo off
javac -cp "lib\jade.jar" -d out src\*.java
if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b %errorlevel%
)
java -cp "lib\jade.jar;out" Main