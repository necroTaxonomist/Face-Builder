@echo off
echo ==============================
echo BUILDING
echo ==============================
@echo on
javac -d ./bin -Xlint:unchecked -cp "..\XML-Parse\bin\XMLParse.jar" ./src/*.java