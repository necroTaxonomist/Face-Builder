@echo off

call build
if %ERRORLEVEL% == 0 (
    call run
) else (
    echo Build failed.
)