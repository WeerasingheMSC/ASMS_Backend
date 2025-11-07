@echo off
echo ===============================================
echo  ASMS Backend - IDE Cache Refresh Script
echo ===============================================
echo.
echo This script will help resolve "cannot find symbol" errors in IntelliJ IDEA
echo.

echo Step 1: Cleaning Maven build...
call mvnw.cmd clean
echo.

echo Step 2: Compiling project...
call mvnw.cmd compile -DskipTests
echo.

echo ===============================================
echo  Build Complete!
echo ===============================================
echo.
echo Now in IntelliJ IDEA, please do the following:
echo.
echo 1. File ^> Invalidate Caches / Restart...
echo 2. Select "Invalidate and Restart"
echo 3. Wait for IntelliJ to restart and re-index
echo.
echo OR try these alternatives:
echo.
echo A. Right-click on project ^> Maven ^> Reload Project
echo B. File ^> Project Structure ^> Modules ^> Mark src/main/java as Sources
echo C. Build ^> Rebuild Project
echo.
echo ===============================================
pause

