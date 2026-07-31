package com.example.data.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiChatService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """
        You are CRRSA AI, the official virtual assistant for the Civil Registration and Residency Service Agency (CRRSA) in Ethiopia.
        Your goal is to assist citizens with inquiries regarding:
        - Residency ID applications, renewals, and replacements
        - Birth registration and certificate issuance (within 90 days rule)
        - Legal Marriage solemnization and registration (single status requirement, witnesses)
        - Death registration and legal inheritance certificates
        - Divorce record registration
        - Document fees (in ETB), processing times, sub-city Woreda office hours (Mon-Fri 8:30AM-5:30PM)
        - Document authentication and copies
        
        Maintain a polite, professional, helpful tone. Support English, Amharic, and Afaan Oromoo. Keep responses clear and well-structured with bullet points where appropriate.
    """.trimIndent()

    suspend fun getAiResponse(userPrompt: String, currentLanguage: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                
                val jsonPayload = JSONObject().apply {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", systemPrompt + " User preferred language code: $currentLanguage")))
                    })
                    put("contents", JSONArray().put(
                        JSONObject().apply {
                            put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                        }
                    ))
                }

                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val jsonObj = JSONObject(responseBody)
                    val candidates = jsonObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCand = candidates.getJSONObject(0)
                        val content = firstCand.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text")
                            if (text.isNotBlank()) {
                                return@withContext text
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to intelligent local rule engine below
            }
        }

        // Offline / Fallback Intelligent CRRSA Knowledge Engine
        return@withContext generateLocalCrrsaResponse(userPrompt, currentLanguage)
    }

    private fun generateLocalCrrsaResponse(prompt: String, lang: String): String {
        val lower = prompt.lowercase()

        val isAmharic = lang == "AM" || prompt.any { it in '\u1200'..'\u137F' }
        val isOromo = lang == "OM" || lower.contains("akham") || lower.contains("waraqaa")

        if (lower.contains("residency") || lower.contains("id card") || lower.contains("መታወቂያ") || lower.contains("waraqaa einnoo")) {
            return if (isAmharic) {
                "ለአዲስ የነዋሪነት መታወቂያ የሚከተሉት ማስረጃዎች ያስፈልጋሉ:\n" +
                        "1. የቀበሌ የድጋፍ ደብዳቤ ወይም የዝውውር ማስረጃ\n" +
                        "2. የቤት ኪራይ ውል ወይም የቤት ባለቤትነት ማረጋገጫ ደብተር\n" +
                        "3. 2 የቅርብ ጊዜ ጉርድ ፎቶግራፍ\n" +
                        "• ክፍያ: 100 ETB\n" +
                        "• የማጠናቀቂያ ጊዜ: 3 የሥራ ቀናት"
            } else if (isOromo) {
                "Waraqaa Eenyummaa Haarawaa Argachuuf:\n" +
                        "1. Xalayaa deeggarsa Gandaa\n" +
                        "2. Waraqaa Abbaa Qabeenyummaa Manaa ykn Waliigaltee Kireeffannaa\n" +
                        "3. Suuraa Paaspoortii 2\n" +
                        "• Kafaltii: 100 ETB\n" +
                        "• Sa'aatii Hojii: Guyaa 3"
            } else {
                "Requirements for New Residency ID Card:\n" +
                        "• Kebele Recommendation Letter or Official Transfer Slip\n" +
                        "• House Lease Agreement or Property Title Deed\n" +
                        "• 2 Recent Passport-size Photographs\n" +
                        "• Original Previous Identification Card (if renewing/replacing)\n\n" +
                        "Fee: 100 ETB | Standard Processing Time: 3 Business Days."
            }
        }

        if (lower.contains("birth") || lower.contains("born") || lower.contains("ልደት") || lower.contains("daa'imman")) {
            return if (isAmharic) {
                "የልደት ምዝገባና ምስክር ወረቀት አሰጣጥ:\n" +
                        "• ሕፃኑ በተወለደ በ 90 ቀናት ውስጥ መመዝገብ አለበት (ከቅጣት ነፃ ነው)\n" +
                        "• የሚያስፈልጉ ማስረጃዎች: የህክምና ተቋም የልደት ማረጋገጫ ወረቀት፣ የወላጆች የፀና መታወቂያ\n" +
                        "• ክፍያ: 50 ETB | የሚፈጀው ጊዜ: 1 ቀን"
            } else {
                "Birth Registration & Legal Certificate Guidelines:\n" +
                        "• Mandatory Registration Period: Within 90 days of child birth (Free of late penalty)\n" +
                        "• Required Documents: Hospital/Health Center Birth Notification Slip, Parents' valid residency IDs\n" +
                        "• Administrative Fee: 50 ETB\n" +
                        "• Delivery: Same day or 1 business day."
            }
        }

        if (lower.contains("marriage") || lower.contains("marry") || lower.contains("ጋብቻ") || lower.contains("fuudha")) {
            return if (isAmharic) {
                "የጋብቻ ምዝገባ ቅድመ ሁኔታዎች:\n" +
                        "1. የሁለቱም ተጋቢዎች በቦታው መገኘትና የፀና መታወቂያ ማቅረብ\n" +
                        "2. ከተወለዱበት/ከሚኖሩበት ወረዳ የተሰጠ ያላገባች/ያላገባ ማረጋገጫ ደብዳቤ\n" +
                        "3. 4 የፀና መታወቂያ ያላቸው ምስክሮች (2 ከሙሽራው፣ 2 ከሙሽሪት)\n" +
                        "4. 2 ጉርድ ፎቶግራፍ ከሁለቱም\n" +
                        "• ክፍያ: 200 ETB"
            } else {
                "Civil Marriage Registration Requirements:\n" +
                        "1. Physical presence of both spouses with valid Residency IDs\n" +
                        "2. Official Single Status Certificate from respective Sub-city / Woreda\n" +
                        "3. 4 Witnesses with valid Residency IDs\n" +
                        "4. 2 Passport photos for each spouse\n\n" +
                        "Fee: 200 ETB | Processing Time: 2 Business Days."
            }
        }

        if (lower.contains("fee") || lower.contains("cost") || lower.contains("ክፍያ") || lower.contains("price")) {
            return if (isAmharic) {
                "የአገልግሎት ክፍያዎች ሰንጠረዥ (በኢትዮጵያ ብር):\n" +
                        "• አዲስ መታወቂያ: 100 ETB\n" +
                        "• መታወቂያ እድሳት/ምትክ: 150 ETB\n" +
                        "• የልደት ምስክር ወረቀት: 50 ETB\n" +
                        "• የጋብቻ ምስክር ወረቀት: 200 ETB\n" +
                        "• የሞት ምስክር ወረቀት: 50 ETB\n" +
                        "• የሰነድ ማረጋገጫና ግልባጭ: 80 ETB"
            } else {
                "CRRSA Official Service Fees (in ETB):\n" +
                        "• New Residency ID Card: 100 ETB\n" +
                        "• Residency ID Renewal / Replacement: 150 ETB\n" +
                        "• Birth Certificate: 50 ETB\n" +
                        "• Legal Marriage Certificate: 200 ETB\n" +
                        "• Death Certificate: 50 ETB\n" +
                        "• Authenticated Document Copy: 80 ETB"
            }
        }

        if (lower.contains("hour") || lower.contains("time") || lower.contains("location") || lower.contains("ሰዓት") || lower.contains("አድራሻ")) {
            return if (isAmharic) {
                "የኤጀንሲው የሥራ ሰዓትና አገልግሎት ሰጪ ማዕከላት:\n" +
                        "• ሰኞ - አርብ: ጠዋት 2:30 - 6:30 | ከሰዓት 7:30 - 11:30\n" +
                        "• ቅዳሜ: ጠዋት 2:30 - 6:00 (ለአስቸኳይ ምስክር ወረቀት ማውጫ ብቻ)\n" +
                        "• ማዕከላዊ ቢሮ: አዲስ አበባ፣ የካ ክፍለ ከተማ ወረዳ 06"
            } else {
                "CRRSA Office Locations & Working Hours:\n" +
                        "• Working Days: Monday to Friday (8:30 AM – 12:30 PM & 1:30 PM – 5:30 PM)\n" +
                        "• Saturday Morning: Urgent certificate collection (8:30 AM – 12:00 PM)\n" +
                        "• Main Headquarters: Yeka Sub-City, Woreda 06, Addis Ababa, Ethiopia.\n" +
                        "• Support Hotline: +251 11 123 4567"
            }
        }

        return if (isAmharic) {
            "የሲቪል ምዝገባና የነዋሪነት አገልግሎት ኤጀንሲ (CRRSA) ረዳት ነኝ። በነዋሪነት መታወቂያ፣ በልደት፣ በጋብቻ፣ በሞት ምዝገባ ወይም በአገልግሎት ክፍያዎች ዙሪያ ጥያቄ ካለዎት መጠየቅ ይችላሉ።"
        } else if (isOromo) {
            "Baga nagaaan gara Tajaajila Galmeessaa Fi Eenyummaa Hawaasaa (CRRSA) dhufte. Waraqaa eenyummaa, galmee dhalootaa fi fuudha irratti gorsa argachuuf gaafachuu dandeessu."
        } else {
            "Welcome to CRRSA AI Assistance! I can help you with questions regarding Residency IDs, Birth Certificates, Marriage solemnization, Death records, service fees, or document submission guidelines. Feel free to ask or pick a suggestion below!"
        }
    }
}
