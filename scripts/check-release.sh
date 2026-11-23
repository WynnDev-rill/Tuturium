#!/usr/bin/env bash
set -euo pipefail
tag="${1:?release tag required}"
[[ "$tag" =~ ^v(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]
version=$(sed -nE 's/^[[:space:]]*versionName = "([^"]+)"$/\1/p' app/build.gradle.kts)
code=$(sed -nE 's/^[[:space:]]*versionCode = ([0-9]+)$/\1/p' app/build.gradle.kts)
[[ "$tag" == "v$version" && "$code" =~ ^[1-9][0-9]*$ ]]
if gh release view "$tag" >/dev/null 2>&1; then
    echo "Release $tag already exists." >&2
    exit 1
fi
latest=$(gh api 'repos/WynnDev-rill/Tuturium/releases?per_page=100' --jq     '[.[] | select((.draft == false) and (.prerelease == false))][0].tag_name // empty')
if [[ -n "$latest" ]]; then
    previous=$(git show "$latest:app/build.gradle.kts" | sed -nE 's/^[[:space:]]*versionCode = ([0-9]+)$/\1/p')
    newest=$(printf '%s\n' "$latest" "$tag" | sort -V | tail -1)
    [[ "$code" -gt "$previous" && "$newest" == "$tag" ]]
fi
