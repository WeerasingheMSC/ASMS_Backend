@echo off
echo ========================================
echo PostgreSQL Connection Cleanup Script
echo ========================================
echo.

echo Step 1: Stopping any running Java processes...
taskkill /F /IM java.exe 2>nul
if %errorlevel% equ 0 (
    echo    - Java processes stopped
    timeout /t 2 /nobreak >nul
) else (
    echo    - No Java processes found
)
echo.

echo Step 2: Clearing PostgreSQL connections...
echo    - Connecting to PostgreSQL...
psql -U postgres -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'demo' AND pid <> pg_backend_pid();" 2>nul
if %errorlevel% equ 0 (
    echo    - Connections cleared successfully
) else (
    echo    - Warning: Could not clear connections (may not be needed)
)
echo.

echo Step 3: Checking remaining connections...
psql -U postgres -d demo -c "SELECT COUNT(*) as active_connections FROM pg_stat_activity WHERE datname = 'demo';" 2>nul
echo.

echo ========================================
echo Cleanup Complete!
echo ========================================
echo.
echo You can now start your Spring Boot application:
echo    mvnw.cmd spring-boot:run
echo.
pause

