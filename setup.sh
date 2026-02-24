#!/bin/bash

# Configuration
if [ ! -f .env ]; then
    cp .env.example .env
    echo "Created .env, please update secrets."
else
    echo ".env exists."
fi

# Pre-flight checks
command -v docker >/dev/null 2>&1 || { echo >&2 "Docker not found. Please install it."; exit 1; }
command -v docker-compose >/dev/null 2>&1 || { echo >&2 "Docker-compose not found. Please install it."; exit 1; }

# Setup execution
chmod +x setup.sh
echo "Setup ready. Run: docker-compose up -d --build"
