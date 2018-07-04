@echo off
echo ==============================
echo BUILDING
echo ==============================
@echo on
javac -d ./bin -Xlint:unchecked -cp "..\XML-Parse\bin\XMLParse.jar" ./src/*.java

@echo off
if %ERRORLEVEL% == 0 (
 @echo on
 run
) else (
 echo Build failed.
)