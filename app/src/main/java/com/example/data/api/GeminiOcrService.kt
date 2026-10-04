package com.example.data.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.JudicialTemplate
import com.example.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiOcrService(private val context: Context) {

    companion object {
        private const val TAG = "GeminiOcrService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
        const val DEFAULT_MODEL = "gemini-2.5-flash"
        const val PREF_KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
        const val PREF_KEY_SELECTED_MODEL = "selected_gemini_model"

        val JUDICIAL_SYSTEM_INSTRUCTION = """
You are an expert Judicial Typist & Legal Scribe AI Assistant for the Ministry of Justice (وزارة العدل) and Court Chambers.
Your critical mission is to analyze handwritten court session minutes (محاضر الجلسات), judicial rulings and decisions (قرارات قضائية وأحكام), and witness testimonies (ضبط أقوال وشهادات).

### Strict Judicial Transcription Rules:
1. **Verbatim Judicial Precision:** Transcribe all court statements with 100% accuracy. Never invent, alter, or summarize testimony, judge notes, or rulings.
2. **Judicial Abbreviation Expansion:** Accurately expand standard court handwriting abbreviations:
   - (م.ع) -> المدعى عليه
   - (م.ي / م.أ) -> المدعي
   - (د.ق) -> الدائرة القضائية
   - (ص.ح) -> صك حكم
   - (ق.س) -> قرار الجلسة
   - (ج.م) -> جلسة مرافعة
   - (ش.أ / ش.1) -> الشاهد الأول
3. **Court Document Structure:**
   - For Session Minutes: Main Title (# محضر جلسة قضائية), followed by section headers (## الحضور والافتتاح, ## أقوال المدعي وطلباته, ## جواب المدعى عليه ودفوعه, ## قرار الدائرة وتوجيهاتها).
   - For Judicial Decisions: Main Title (# قرار قضائي / صك حكم), followed by (## الديباجة والوقائع, ## أسباب القرار وتسبيبه, ## منطوق الحكم والقرار الصادر).
   - For Witness Testimony: Main Title (# محضر سماع أقوال وضبط شهادة), followed by (## بيانات الشاهد وحلف اليمين, ## الأسئلة والأقوال المضبوطة, ## المصادقة والتوقيع).
4. **Tables & Accounting:** If the handwritten sheet contains ledgers, fee tables, or accounting figures, format them cleanly into Markdown tables.
5. **Doubt & Illegibility Protection:** If a word, legal term, name, or number in the handwriting is illegible, strictly write [كلمة غير واضحة] so the court scribe can inspect it directly. Never guess in judicial records.
6. **Multi-page Continuity:** If multiple pages are provided in the request, separate the content of each page with "---page-break---" on its own line.
7. **Word Document Ready:** Output ONLY the clean, structured Arabic document in Markdown ready for direct Word (.doc) export, without conversational filler.
        """.trimIndent()
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences("qalam_ocr_prefs", Context.MODE_PRIVATE)
    }

    // MANDATED 60-second timeouts by gemini-api skill
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getEffectiveApiKey(): String {
        val customKey = sharedPreferences.getString(PREF_KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (customKey.isNotEmpty()) return customKey

        return try {
            val buildConfigKey = BuildConfig.GEMINI_API_KEY
            if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
                buildConfigKey
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun setCustomApiKey(key: String) {
        sharedPreferences.edit().putString(PREF_KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun getSelectedModel(): String {
        return sharedPreferences.getString(PREF_KEY_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    fun setSelectedModel(model: String) {
        sharedPreferences.edit().putString(PREF_KEY_SELECTED_MODEL, model).apply()
    }

    /**
     * Extracts and structurally formats handwritten Arabic documents/pages using Gemini Multimodal Vision API.
     * Supports single or multi-page session minutes and judicial decisions.
     */
    suspend fun performHandwritingOcr(
        bitmaps: List<Bitmap>,
        customInstructionAddition: String? = null,
        template: JudicialTemplate = JudicialTemplate.SESSION_MINUTES
    ): Result<String> = withContext(Dispatchers.IO) {
        if (bitmaps.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("لم يتم تزويد أي صور للمستند"))
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Result.failure(
                IllegalStateException("لا يتوفر اتصال بالإنترنت حالياً. يعمل التطبيق بدون إنترنت في التصفح والتعديل والتصدير، ويلزم الاتصال بالإنترنت فقط عند إرسال الصور للتعرف الذكي.")
            )
        }

        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("لم يتم ضبط مفتاح Gemini API. يرجى إدخال مفتاحك في الإعدادات للمتابعة.")
            )
        }

        try {
            val model = getSelectedModel()
            val url = "$BASE_URL$model:generateContent?key=$apiKey"

            // Construct JSON request matching Gemini REST spec
            val requestJson = JSONObject().apply {
                // systemInstruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", JUDICIAL_SYSTEM_INSTRUCTION)
                        })
                    })
                })

                // contents
                val promptText = buildString {
                    append("قم بتحليل ونسخ هذه الأوراق المكتوبة بخط اليد لوزارة العدل (${template.titleArabic}). ")
                    append("طبق القواعد القضائية الرسمية بدقة متناهية 100%. ")
                    if (bitmaps.size > 1) {
                        append("المستند يتكون من ${bitmaps.size} صفحات متتالية. افصل بين كل صفحة وأخرى برمز '---page-break---' على سطر منفرد. ")
                    }
                    if (!customInstructionAddition.isNullOrBlank()) {
                        append("\n\nملاحظات وتوجيهات إضافية من كاتب الضبط: $customInstructionAddition")
                    }
                }

                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            // Text prompt
                            put(JSONObject().apply {
                                put("text", promptText)
                            })

                            // Image parts for each page
                            for ((index, bitmap) in bitmaps.withIndex()) {
                                val processed = prepareBitmap(bitmap)
                                val base64 = bitmapToBase64(processed)

                                put(JSONObject().apply {
                                    put("text", "--- صورة الصفحة ${index + 1} من ${bitmaps.size} ---")
                                })
                                put(JSONObject().apply {
                                    put("inlineData", JSONObject().apply {
                                        put("mimeType", "image/jpeg")
                                        put("data", base64)
                                    })
                                })
                            }
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                // High precision temperature for court transcription
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1)
                    put("topP", 0.95)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error code: ${response.code}, body: $responseBody")
                val errorMsg = parseErrorMessage(responseBody, response.code)
                return@withContext Result.failure(Exception(errorMsg))
            }

            val extractedText = parseCandidateText(responseBody)
            if (extractedText.isBlank()) {
                return@withContext Result.failure(Exception("لم يتمكن النموذج من استخراج أي نص من المستند."))
            }

            Result.success(cleanOutputText(extractedText))
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Judicial OCR", e)
            Result.failure(e)
        }
    }

    // Convenience single bitmap overload
    suspend fun performHandwritingOcr(
        bitmap: Bitmap,
        customInstructionAddition: String? = null
    ): Result<String> {
        return performHandwritingOcr(listOf(bitmap), customInstructionAddition, JudicialTemplate.SESSION_MINUTES)
    }

    private fun prepareBitmap(original: Bitmap): Bitmap {
        val maxDimension = 2048
        if (original.width <= maxDimension && original.height <= maxDimension) {
            return original
        }
        val ratio = original.width.toFloat() / original.height.toFloat()
        val targetWidth: Int
        val targetHeight: Int
        if (ratio > 1) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt()
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 88, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun parseCandidateText(jsonString: String): String {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            val stringBuilder = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val text = part.optString("text", "")
                if (text.isNotEmpty()) {
                    stringBuilder.append(text)
                }
            }
            stringBuilder.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing candidate text", e)
            ""
        }
    }

    private fun parseErrorMessage(errorBody: String, httpCode: Int): String {
        return try {
            val root = JSONObject(errorBody)
            val errorObj = root.optJSONObject("error")
            val message = errorObj?.optString("message", "") ?: ""
            if (message.isNotEmpty()) {
                when {
                    httpCode == 400 && message.contains("API key not valid", ignoreCase = true) ->
                        "مفتاح Gemini API غير صالح. يرجى التحقق من المفتاح في الإعدادات."
                    httpCode == 429 || message.contains("Resource has been exhausted", ignoreCase = true) ->
                        "تم تجاوز حد الاستخدام المتاح (Quota limit). يرجى المحاولة بعد قليل أو استخدام مفتاحك الخاص."
                    else -> "خطأ من خادم الذكاء الاصطناعي: $message"
                }
            } else {
                "فشل الاتصال بالخادم (رمز الخطأ: $httpCode)"
            }
        } catch (e: Exception) {
            "فشل معالجة الطلب (رمز الخطأ: $httpCode)"
        }
    }

    private fun cleanOutputText(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```markdown") && text.endsWith("```")) {
            text = text.removePrefix("```markdown").removeSuffix("```").trim()
        } else if (text.startsWith("```") && text.endsWith("```")) {
            text = text.removePrefix("```").removeSuffix("```").trim()
        }
        return text
    }
}
