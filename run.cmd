@echo off
setlocal
pushd "%~dp0"
set "inventory_jdk=%JAVA_HOME%"
if not exist "%inventory_jdk%\bin\javac.exe" (
    for /d %%J in ("%ProgramFiles%\Java\jdk-*") do (
        if exist "%%~fJ\bin\javac.exe" set "inventory_jdk=%%~fJ"
    )
)
set "inventory_java=java"
set "inventory_javac=javac"
if exist "%inventory_jdk%\bin\javac.exe" (
    set "inventory_java=%inventory_jdk%\bin\java.exe"
    set "inventory_javac=%inventory_jdk%\bin\javac.exe"
)
if not exist out mkdir out
(for %%F in (src\model\*.java src\service\*.java src\storage\*.java src\exception\*.java src\web\*.java) do @echo %%F) > out\sources.txt
"%inventory_javac%" --release 17 -encoding UTF-8 -d out @out\sources.txt
if errorlevel 1 (
    popd
    exit /b 1
)
"%inventory_java%" -cp out web.WarehouseServer %*
set "inventory_exit=%errorlevel%"
popd
exit /b %inventory_exit%
