#!/usr/bin/env bash
# Complete the package rename from com.example to com.baqiyat.azkar
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Moving source directories..."
for srcset in main androidTest test debug release; do
  if [ -d "app/src/$srcset/java/com/example" ]; then
    mkdir -p "app/src/$srcset/java/com/baqiyat/azkar"
    mv app/src/$srcset/java/com/example/* "app/src/$srcset/java/com/baqiyat/azkar/" 2>/dev/null || true
    rmdir app/src/$srcset/java/com/example 2>/dev/null || true
    rmdir app/src/$srcset/java/com 2>/dev/null || true
    echo "  moved $srcset"
  fi
done

echo "Updating package / import statements..."
find app/src -type f \( -name "*.kt" -o -name "*.xml" \) -print0 | xargs -0 sed -i \
  -e 's/package com\.example/package com.baqiyat.azkar/g' \
  -e 's/import com\.example\./import com.baqiyat.azkar./g' \
  -e 's/com\.example\./com.baqiyat.azkar./g'

# FCM improvement
python3 - <<'PY'
from pathlib import Path
for p in [
    "app/src/main/java/com/baqiyat/azkar/MainActivity.kt",
    "app/src/main/java/com/baqiyat/azkar/data/FirestoreRepository.kt",
]:
    path = Path(p)
    if path.exists():
        text = path.read_text()
        old = '"fcmTokens" to listOf(token),'
        new = '"fcmTokens" to FieldValue.arrayUnion(token),'
        if old in text:
            path.write_text(text.replace(old, new, 1))
            print(f"  FCM arrayUnion applied in {p}")
PY

sed -i 's/com\.example\./com.baqiyat.azkar./g' docs/PHASE3_APPLY.md 2>/dev/null || true

echo "Done. Review with: git status"
echo "Then: git add -A && git commit -m 'fix: complete package rename to com.baqiyat.azkar'"
