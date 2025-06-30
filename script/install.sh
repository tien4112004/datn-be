#!/usr/bin/env bash
set -euo pipefail

# 1. Ensure .env exists and source it
if [[ ! -f .env ]]; then
  echo "Error: .env file not found in $(pwd)"
  exit 1
fi

# shellcheck disable=SC1091
source .env
echo ".env loaded."

# 2. Install npm dependencies and add commitlint hook

echo "Installing npm dependencies and setting up commitlint hook..."
npm install 
