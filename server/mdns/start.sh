#!/bin/bash

set -e

SOURCE_DIR=$(dirname "$0")
pushd $SOURCE_DIR

if [ ! -d "venv" ]; then
    echo "[mdns] Creating virtual environment..."
    python -m venv venv
fi

echo "[mdns] Activating virtual environment..."
source venv/bin/activate

echo "[mdns] Installing dependencies..."
pip install -r requirements.txt

echo "[mdns] Start broadcasting..."
python -u main.py "$@"