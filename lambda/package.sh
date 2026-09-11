#!/usr/bin/env bash
# Builds lambda/function.zip from app.py (+ any deps in requirements.txt).
# Used by both local manual deploys and the GitHub Actions workflows, so the
# packaging logic only lives in one place.
set -euo pipefail
cd "$(dirname "$0")"

rm -rf package function.zip
mkdir package

if grep -vqE '^\s*#|^\s*$' requirements.txt; then
  pip install -r requirements.txt -t package/
fi

cp app.py package/
(cd package && zip -r ../function.zip .)
rm -rf package
