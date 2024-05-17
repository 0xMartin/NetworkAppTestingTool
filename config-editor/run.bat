@echo off

python main.py
if %ERRORLEVEL% neq 0 (
    echo Python not found. Make sure you have Python installed and set correctly in PATH.
    pause
)