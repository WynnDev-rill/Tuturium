#!/usr/bin/env bash
# One-time private release setup. Never commit, print, or publish secret values.
set -euo pipefail

backup="${1:-}"
if [[ -z "$backup" || ! -f "$backup" ]]; then
  echo "Usage: bash scripts/setup-signing-secrets.sh /path/to/Tuturium-signing-key.zip" >&2
  exit 1
fi
for command in gh unzip base64; do
  command -v "$command" >/dev/null || {
    echo "Missing dependency: $command" >&2
    exit 1
  }
done

gh auth status >/dev/null
unzip -tq "$backup" >/dev/null
unzip -p "$backup" Tuturium.p12 | base64 | tr -d '\r\n' |
  gh secret set TUTURIUM_KEYSTORE_BASE64 -R WynnDev-rill/Tuturium
unzip -p "$backup" password.txt | tr -d '\r\n' |
  gh secret set TUTURIUM_STORE_PASSWORD -R WynnDev-rill/Tuturium

echo "Signing credentials stored as private GitHub Actions secrets."
echo "Starting signed release workflow for current main branch..."
gh workflow run release.yml -R WynnDev-rill/Tuturium --ref main
echo "See https://github.com/WynnDev-rill/Tuturium/actions"
