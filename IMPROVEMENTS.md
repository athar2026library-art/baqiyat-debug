# تحسينات الحزمة وFCM

**التاريخ:** 25 أكتوبر 2026  
**الفرع:** `fix/package-align-com-baqiyat-azkar`

## ما تم رفعه على الفرع

- `IMPROVEMENTS.md`
- `app/google-services.json` منظّف من عميل `com.example`
- `app/src/main/AndroidManifest.xml` محدّث بأسماء الحزمة الجديدة
- `AzkarApplication.kt` + مثبّتي App Check (debug/release)
- سكربت `scripts/complete-package-rename.sh`

## لإكمال النقل الكامل (مرة واحدة)

```bash
git fetch origin
git checkout fix/package-align-com-baqiyat-azkar
bash scripts/complete-package-rename.sh
git add -A
git commit -m "fix: complete package rename to com.baqiyat.azkar"
git push
```

أو إذا كنت على main:

```bash
git checkout -b fix/package-align-com-baqiyat-azkar
bash scripts/complete-package-rename.sh
git add -A
git commit -m "fix: complete package rename to com.baqiyat.azkar"
git push -u origin fix/package-align-com-baqiyat-azkar
```

## بعد الدمج

```bash
./gradlew :app:assembleDebug
```
