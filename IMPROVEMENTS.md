# تحسينات تم تطبيقها على المستودع

**التاريخ:** 25 أكتوبر 2026

## 1. إصلاح عدم تطابق الحزمة (Package Mismatch)

- نقل كل المصدر من `com.example` إلى `com.baqiyat.azkar`
- تحديث package / import / AndroidManifest / docs
- تنظيف google-services.json من عميل com.example الزائد

## 2. تحسين تخزين توكنات FCM

استخدام `FieldValue.arrayUnion(token)` بدل استبدال القائمة كاملة.

## الخطوات التالية

1. `./gradlew :app:assembleDebug`
2. اختبار Google Sign-In + FCM على جهاز
3. إنشاء `admins/{uid}` بأول مشرف
4. `firebase deploy --only firestore:rules,firestore:indexes,storage`
