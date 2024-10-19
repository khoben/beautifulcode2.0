@echo off

pushd %~dp0

if not exist .\venv\ (
    echo [mdns] Creating virtual environment...
    python -m venv venv
)

echo [mdns] Installing dependencies...
.\venv\Scripts\pip install -r requirements.txt

echo [mdns] Start broadcasting...
.\venv\Scripts\python -u main.py %*