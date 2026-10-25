# حالة فرع إصلاح الحزمة

**الفرع:** `fix/package-align-com-baqiyat-azkar`
**التاريخ:** 25 أكتوبر 2026

## ما تم رفعه على الفرع

- `IMPROVEMENTS.md`
- `app/google-services.json` منظّف من عميل `com.example`
- `app/src/main/AndroidManifest.xml` (أسماء الحزمة الجديدة)
- `AzkarApplication.kt`
- مثبّتي App Check (debug + release)
- `MainActivity.kt` (مع `FieldValue.arrayUnion` لتوكنات FCM)
- بعض ملفات data الأولية
- السكربت: `scripts/complete-package-rename.sh`

## الطريقة الموصى بها لإكمال النقل (موثوقة وسريعة)

لأن نقل 72 ملفًا عبر واجهة البرمجة بطيء ومعرّض للأخطاء، نفّذ هذا محليًا:

```bash
git fetch origin
git checkout fix/package-align-com-baqiyat-azkar
bash scripts/complete-package-rename.sh
git add -A
git status   # راجع التغييرات
git commit -m "fix: complete package rename to com.baqiyat.azkar"
git push
```

السكربت يقوم بـ:
1. نقل كل المجلدات من `com/example` إلى `com/baqiyat/azkar`
2. تحديث كل `package` و `import`
3. تطبيق تحسين FCM (`arrayUnion`)

## بعد الإكمال

```bash
./gradlew :app:assembleDebug
```

ثم أنشئ Pull Request من الفرع إلى `main`.
