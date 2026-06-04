package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

// Define the extracted JSON payload format
@JsonClass(generateAdapter = true)
data class ExtractedBooking(
    val customerName: String?,
    val customerContact: String?,
    val customerIdCard: String?,
    val customerAddress: String?,
    val serviceType: String?, // "ห้องพักรายวัน", "ห้องพักรายเดือน", "ห้องประชุม"
    val roomNumber: String?,
    val checkInDate: String?, // YYYY-MM-DD
    val checkOutDate: String?, // YYYY-MM-DD
    val numberOfNights: Int?,
    val pricePerNight: Double?,
    val totalAmount: Double?,
    val paymentMode: String?, // "เงินโอน", "เงินสด"
    val notes: String?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Parse unstructured text message into parsed Reservation data.
     */
    suspend fun parseBookingText(promptText: String): ExtractedBooking? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey.startsWith("MY_")) {
            Log.e(TAG, "API Key is missing or default placeholder")
            return@withContext null
        }

        val systemPrompt = """
            You are a lead front-desk booking coordinator for Wipat Hotel ("วิพัฒน์โฮเทล"). 
            Your goal is to parse unstructured room bookings, check-in alerts, or hotel reservations 
            and extract them into a single clean JSON object matching the requested schema.
            
            Schema details:
            - customerName: Full name of user.
            - customerContact: Phone number, format e.g. "083-148-2070", "098-964-4936".
            - customerIdCard: 13 digit number if present, else empty/null.
            - customerAddress: If present, else empty/null.
            - serviceType: Must be either "ห้องพักรายวัน", "ห้องพักรายเดือน", or "ห้องประชุม".
            - roomNumber: Room key, e.g., "A101", "B106", "N2", "N3".
            - checkInDate: ISO format "YYYY-MM-DD". If they say e.g., "3 ต.ค. 2568" or "3-7 ตุลาคม 2568", extract check-in as "2025-10-03". 
              Note: Thai Buddhist Era year 2568 is Christian Era 2025 (2568 - 543 = 2025). 2569 is 2026. Use current year 2026/2025 if no year is mentioned.
            - checkOutDate: ISO format "YYYY-MM-DD". For "3-7 ตุลาคม 2568", check-out is "2025-10-07".
            - numberOfNights: Number of nights stay. (e.g. for "3-7 ตุลาคม 2568", it's 4 nights).
            - pricePerNight: Room price per night as double.
            - totalAmount: Total room price (pricePerNight * numberOfNights).
            - paymentMode: Payment transaction type. Must be "เงินโอน" (transfer), "เงินสด" (cash), or "-" (uncalculated/unpaid).
            - notes: Any additional requirements, extra beds, comments, e.g. "พัก 3 คน", "พักต่อ".
            
            Return ONLY the raw JSON object, without markdown wraps, and do NOT wrap it in ```json. Just raw text starting with { and ending with }.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = "Input text to extract:\n$promptText")))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.1f
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                Log.d(TAG, "Gemini Response: $jsonText")
                val adapter = moshi.adapter(ExtractedBooking::class.java)
                return@withContext adapter.fromJson(jsonText)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
        }
        return@withContext null
    }
}
