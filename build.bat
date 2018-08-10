@echo off
echo ==============================
echo BUILDING
echo ==============================

@echo on
javac -d ./bin -Xlint:unchecked -cp "..\XML-Parse\bin\XMLParse.jar";"..\Exp-Parse\bin\ExpParse.jar" ./src/*.java

@echo off
exit /b %ERRORLEVEL%