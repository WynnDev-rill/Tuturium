#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
uv tool run --from graphifyy==0.9.118 graphify update .
