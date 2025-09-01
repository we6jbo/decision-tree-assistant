#!/usr/bin/env bash
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT"

TOKEN_FILE="$ROOT/BB167E5495.txt"
CARD_FILE="$ROOT/A8E9FF8891C.json"

if [[ ! -f "$TOKEN_FILE" || ! -f "$CARD_FILE" ]]; then
  echo "Missing $TOKEN_FILE or $CARD_FILE. Aborting." >&2
  exit 1
fi

# Read single-line token (collapse whitespace)
TOKEN_LINE="$(tr -d '\r' < "$TOKEN_FILE" | tr '\n' ' ' | sed 's/  */ /g' | sed 's/^ *//; s/ *$//')"

# Banner text we’ll prepend (keep it one line to avoid noisy diffs)
BANNER="MARK: $TOKEN_LINE | CARD=A8E9FF8891C.json"

# File globs to include (source/resources) and exclude (build/binaries)
INCLUDE_EXTS=(
  "kt" "java" "kts" "gradle" "pro" "properties"
  "xml" "yml" "yaml" "sh" "bash" "md" "txt"
)
EXCLUDE_DIRS=("build/" ".git/" ".gradle/" ".idea/")

# Comment styles by extension
comment_open() {
  case "$1" in
    xml|md) echo "<!--";;
    sh|bash|yml|yaml|properties) echo "#";;
    *) echo "//";; # kt, java, gradle, kts, pro, txt
  esac
}
comment_close() {
  case "$1" in
    xml|md) echo " -->";;
    *) echo "";;
  esac
}

# Build a regex to skip excluded dirs
EXCLUDE_ARGS=()
for d in "${EXCLUDE_DIRS[@]}"; do
  EXCLUDE_ARGS+=(-path "$d" -prune -o)
done

# Build extension predicates
EXT_PRED=()
for ext in "${INCLUDE_EXTS[@]}"; do
  EXT_PRED+=(-name "*.${ext}" -o)
done
unset 'EXT_PRED[${#EXT_PRED[@]}-1]'  # drop trailing -o

# Use git ls-files so we only touch tracked files
mapfile -t CANDIDATES < <(
  git ls-files |
  while IFS= read -r f; do
    skip=0
    for d in "${EXCLUDE_DIRS[@]}"; do
      [[ "$f" == $d* ]] && { skip=1; break; }
    done
    [[ $skip -eq 1 ]] && continue

    ext="${f##*.}"
    for e in "${INCLUDE_EXTS[@]}"; do
      if [[ "$ext" == "$e" ]]; then
        echo "$f"
        break
      fi
    done
  done
)

# Stamp each file if not already stamped
for f in "${CANDIDATES[@]}"; do
  ext="${f##*.}"
  open="$(comment_open "$ext")"
  close="$(comment_close "$ext")"

  # Grep quickly to see if already stamped (idempotent)
  if grep -Fq "$BANNER" "$f"; then
    echo "skip: $f (already stamped)"
    continue
  fi

  # Prepend banner safely
  tmp="$f.__stamp__"
  {
    if [[ "$open" == "<!--" ]]; then
      echo "${open} ${BANNER}${close}"
    else
      echo "${open} ${BANNER}${close}"
    fi
    cat "$f"
  } > "$tmp"
  mv "$tmp" "$f"
  echo "stamped: $f"
done

echo "Done. Banner:"
echo "$BANNER"

