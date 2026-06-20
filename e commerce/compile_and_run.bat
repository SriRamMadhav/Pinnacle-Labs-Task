@echo off
title E-Commerce Shopping Cart System Build Script
color 0B
cls

echo ========================================================
echo        E-COMMERCE SHOPPING CART SYSTEM BUILDER
echo ========================================================
echo.

:: Setup build directory
if not exist "bin" (
    echo Creating build folder [bin]...
    mkdir bin
)

echo Compiling Java source files...
:: Compile all java files under src package structures
javac -d bin -sourcepath src src/com/ecommerce/Main.java src/com/ecommerce/CoreTests.java

if %errorlevel% neq 0 (
    color 0C
    echo.
    echo [ERROR] Compilation failed! Please inspect compilation errors.
    pause
    exit /b %errorlevel%
)

echo [SUCCESS] Compilation completed successfully.
echo.

:menu
echo --------------------------------------------------------
echo Choose an option to execute:
echo 1) Run E-Commerce Shopping Cart GUI Application
echo 2) Run Automated Unit Tests (CLI)
echo 3) Recompile Only
echo 4) Exit
echo --------------------------------------------------------
set /p opt="Select choice (1-4): "

if "%opt%"=="1" (
    echo.
    echo Launching GUI App in background...
    start javaw -cp bin com.ecommerce.Main
    goto menu
)
if "%opt%"=="2" (
    echo.
    echo Running automated unit tests...
    java -cp bin com.ecommerce.CoreTests
    echo.
    goto menu
)
if "%opt%"=="3" (
    echo.
    echo Re-compiling...
    javac -d bin -sourcepath src src/com/ecommerce/Main.java src/com/ecommerce/CoreTests.java
    if %errorlevel% equ 0 (
        echo [SUCCESS] Re-compiled successfully.
    ) else (
        echo [ERROR] Re-compilation failed.
    )
    echo.
    goto menu
)
if "%opt%"=="4" (
    echo Goodbye!
    exit /b 0
)

echo Invalid choice. Please pick 1, 2, 3 or 4.
echo.
goto menu
