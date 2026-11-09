# معمارية Forgeprom Code Studio

## نظرة عامة

تطبيق Android أصلي: Kotlin 2.0.20، Jetpack Compose (BOM 2024.09.03)، Material 3، Room 2.6.1 (KSP)، DataStore، OkHttp 4.12.0، security-crypto. Gradle 8.9 / AGP 8.5.2، compileSdk 35، minSdk 26، JDK 17.

## الطبقات

```
ui/          Compose screens + navigation (AppNavHost، 5 تبويبات)
core/
  AppContainer    حقن يدوي: db + settings + apiKeys + engines + session
  ai/             AiEngine (واجهة) + LocalRulesEngine + GeminiEngine + OpenAiCompatEngine + AiEngineManager
  data/           Room: 6 كيانات (Project, Prompt, Analysis, Activity, EditorFile, ImageAnalysis)
  settings/       DataStore: ثيم، لغة الموجه، موافقة السحابة، محرك كل مهمة
  security/       ApiKeyStore — EncryptedSharedPreferences
  registry/       FeatureRegistry — سجل ميزات صادق
  util/           Connectivity، Exporter (FileProvider + ACTION_SEND)
domain/            IdeaAnalyzer، PromptBuilder، QualityChecker، Critique — Kotlin نقي
```

## تدفق التوليد

```
CreateScreen (فكرة) → IdeaAnalyzer.analyze → SessionHolder.draft
  → AnalysisScreen (تحرير/اعتماد) → AiEngineManager.defaultForTask("general").generate
  → PromptBuilder (12 قسماً) → QualityChecker (14 عنصراً) → Critique
  → ResultScreen (6 تبويبات: برومبت/فهم/نقد/افتراضات/جودة/تصدير)
  → حفظ في مشروع (Room) أو تصدير (MD/TXT/JSON)
```

## سياسة المحركات

- **local.rules** (افتراضي): محرك قاعدي قواعد عربي — دون إنترنت.
- **cloud.gemini**: Gemini 2.0 Flash (+Vision) — خلف بوابة `cloudGate()` ومفتاح مشفّر.
- **cloud.openai_compat**: أي مزود متوافق (baseUrl + model قابلان للضبط) — نفس البوابة.
- `SessionHolder` يمرر حالة الجلسة بين الشاشات دون تمرير نصوص ضخمة في المسارات.

## الخصوصية

- موافقة السحابة مرفوضة افتراضياً — لا استدعاء سحابي دونها.
- المفاتيح في EncryptedSharedPreferences (بديل InMemory عند فشل التهيئة).
- تحليل الصور يرسل الصورة إلى Google فقط بعد الموافقة وإقرار «استلهام فقط».

## الاختبار وCI

- اختبارات JUnit4 لطبقة المجال (`app/src/test/.../DomainTests.kt`) — 8 اختبارات.
- GitHub Actions: `testDebugUnitTest` ثم `assembleDebug` ثم رفع APK كـ artifact.
