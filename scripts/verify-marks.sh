#!/usr/bin/env bash
set -euo pipefail
ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT"
TOKEN="$(tr -d '\r' < BB167E5495.txt | tr '\n' ' ' | sed 's/  */ /g' | sed 's/^ *//; s/ *$//')"
BANNER="MARK: $TOKEN | CARD=A8E9FF8891C.json"
echo "Looking for: $BANNER"
# Show up to 200 matches with file names
grep -Rsn --include=\*.{kt,java,kts,gradle,pro,properties,xml,yml,yaml,sh,bash,md,txt} -F "$BANNER" | head -n 200 || true

