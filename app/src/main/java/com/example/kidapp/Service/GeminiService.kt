package com.example.kidapp.Service

import android.content.Context
import android.util.Log
import com.example.kidapp.BuildConfig
import com.example.kidapp.models.StoryModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiService(private val context: Context) {
    // Use the appropriate model name for Gemini 1.5
    private val modelName = "gemini-1.5-flash"
    private val generativeModel: GenerativeModel
    private val TAG = "GeminiServiceKt"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // fal.ai API configuration
    private val FAL_API_URL = "https://fal.run/fal-ai/flux/dev"
    private val FAL_API_KEY = BuildConfig.FAL_AI_API_KEY

    // Thư mục lưu ảnh
    private val imageDir by lazy {
        File(context.filesDir, "story_images").apply {
            if (!exists()) mkdirs()
        }
    }

    private var storyCharacterDescriptions: Map<String, String> = mutableMapOf()
    private var storyStylePrompt: String = ""

    init {
        // Create generationConfig using the factory function
        val config = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 800
        }

        // Sử dụng API key từ BuildConfig
        val apiKey = BuildConfig.GEMINI_API_KEY
        generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = config
        )
    }

    interface StoryCallback {
        fun onSuccess(story: StoryModel)
        fun onError(throwable: Throwable)
    }

    // Updated to support multiple characters and items
    suspend fun generateStory(characters: List<String>, setting: String, items: List<String>): Result<StoryModel> {
        // Reset character descriptions for new story
        storyCharacterDescriptions = mutableMapOf()

        // Generate detailed character descriptions first
        try {
            generateCharacterDescriptions(characters)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating character descriptions: ${e.message}")
        }

        // Create style prompt for the story
        storyStylePrompt = createStoryStylePrompt(setting)

        // Create appropriate prompt for children
        val prompt = buildPrompt(characters, setting, items)

        // Use Kotlin Extension to easily create Content
        val contentInput = content {
            text(prompt)
        }

        return withContext(Dispatchers.IO) { // Switch to IO thread for network calls
            try {
                // Call generateContent() which is a suspend function
                val response = generativeModel.generateContent(contentInput)
                val storyText = response.text

                if (!storyText.isNullOrEmpty()) {
                    try {
                        // Phân tích phản hồi thành các phần
                        val parts = parseResponse(storyText)
                        val title = parts[0]
                        val content = parts[1]

                        // Tạo đối tượng StoryModel
                        val storyModel = StoryModel(
                            title,
                            content,
                            "", // Không cần imageUrl chính nữa vì sẽ có ảnh cho từng cảnh
                            characters.firstOrNull() ?: "", // character (for backward compatibility)
                            setting,
                            items.firstOrNull() ?: "" // primary item for backward compatibility
                        )

                        // Thiết lập danh sách nhân vật và vật phẩm
                        val charactersList = ArrayList<String>()
                        charactersList.addAll(characters)
                        storyModel.charactersList = charactersList

                        val itemsList = ArrayList<String>()
                        itemsList.addAll(items)
                        storyModel.itemsList = itemsList

                        // Phân tích nội dung thành các cảnh và tạo ảnh cho từng cảnh
                        val scenes = parseScenes(content)
                        for (sceneContent in scenes) {
                            val imageUrl = generateImageForScene(sceneContent, characters, setting)
                            val scene = StoryModel.SceneModel(sceneContent, imageUrl)
                            storyModel.addScene(scene)
                        }

                        // Nếu không có cảnh nào, tạo một cảnh mặc định với toàn bộ nội dung
                        if (storyModel.scenes.isEmpty()) {
                            val defaultImageUrl = generateImageForScene(content, characters, setting)
                            val defaultScene = StoryModel.SceneModel(content, defaultImageUrl)
                            storyModel.addScene(defaultScene)
                        }

                        Result.success(storyModel)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing response: ${e.message}")
                        Result.failure(Exception("Lỗi khi phân tích phản hồi từ Gemini.", e))
                    }
                } else {
                    Log.e(TAG, "Không thể tạo truyện. Phản hồi Gemini không chứa văn bản.")
                    Result.failure(Exception("Không thể tạo truyện. Phản hồi Gemini không chứa văn bản."))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating content: ${e.message}")
                Result.failure(Exception("Lỗi khi gọi Gemini API.", e))
            }
        }
    }

    // Hàm này có thể được gọi từ coroutine
    fun generateStoryAsync(characters: List<String>, setting: String, items: List<String>, callback: StoryCallback) {
        // Sử dụng một CoroutineScope phù hợp (ví dụ: lifecycleScope trong Activity/Fragment)
        // Ở đây dùng GlobalScope chỉ để minh họa, nên tránh dùng trong ứng dụng thực tế
        // Bạn nên truyền CoroutineScope từ nơi gọi hàm này
        GlobalScope.launch {
            val result = generateStory(characters, setting, items)
            withContext(Dispatchers.Main) { // Chuyển về luồng chính để gọi callback
                result.onSuccess { story ->
                    callback.onSuccess(story)
                }.onFailure { throwable ->
                    callback.onError(throwable)
                }
            }
        }
    }

    // Backward compatibility method for single character and item
    suspend fun generateStory(character: String, setting: String, item: String): Result<StoryModel> {
        return generateStory(listOf(character), setting, listOf(item))
    }

    // Backward compatibility method for async version
    fun generateStoryAsync(characters: ArrayList<String>, setting: String, items: ArrayList<String>, callback: StoryCallback) {
        generateStoryAsync(characters.toList(), setting, items.toList(), callback)
    }

    // Backward compatibility method for single character and item
    fun generateStoryAsync(character: String, setting: String, item: String, callback: StoryCallback) {
        generateStoryAsync(listOf(character), setting, listOf(item), callback)
    }

    private fun buildPrompt(characters: List<String>, setting: String, items: List<String>): String {
        val charactersText = if (characters.size == 1) {
            "- Nhân vật chính: ${characters[0]}\n"
        } else {
            val mainCharacter = characters[0]
            val supportingCharacters = characters.subList(1, characters.size)
            "- Nhân vật chính: $mainCharacter\n" +
            "- Nhân vật phụ: ${supportingCharacters.joinToString(", ")}\n"
        }

        val itemsText = if (items.size == 1) {
            "- Vật phẩm quan trọng: ${items[0]}\n"
        } else {
            "- Vật phẩm quan trọng: ${items.joinToString(", ")}\n"
        }

        return """
            Hãy tạo một câu chuyện ngắn dành cho trẻ em với các yếu tố sau:
            $charactersText
            - Bối cảnh: $setting
            $itemsText
            
            Yêu cầu:
            1. Tạo một câu chuyện ngắn gọn, hấp dẫn và có ý nghĩa giáo dục
            2. Chia câu chuyện thành 3-5 đoạn ngắn, mỗi đoạn là một phần của câu chuyện
            3. Không đánh số thứ tự các đoạn, không ghi "Cảnh 1", "Cảnh 2",...
            4. Định dạng phản hồi:
            
            TIÊU ĐỀ TIẾNG VIỆT
            ---
            ENGLISH TITLE
            ===
            [Đoạn 1 tiếng Việt]
            ---
            [Scene 1 in English]
            ===
            [Đoạn 2 tiếng Việt]
            ---
            [Scene 2 in English]
            ===
            [Tiếp tục với các đoạn còn lại]
            
            Lưu ý:
            - Mỗi đoạn nên dài khoảng 2-3 câu
            - Nội dung tiếng Anh phải là bản dịch chính xác của nội dung tiếng Việt
            - Sử dụng ngôn ngữ đơn giản, phù hợp với trẻ em
            - Tạo những tình huống thú vị và bất ngờ
            - Kết thúc câu chuyện với một bài học ý nghĩa
        """.trimIndent()
    }

    private fun parseResponse(response: String): List<String> {
        val parts = response.split("===")
        if (parts.isEmpty()) return listOf("", "")

        // Tách tiêu đề
        val titleParts = parts[0].split("---")
        val vietnameseTitle = titleParts[0].trim()
        val englishTitle = if (titleParts.size > 1) titleParts[1].trim() else ""

        // Tách nội dung các cảnh
        val vietnameseScenes = mutableListOf<String>()
        val englishScenes = mutableListOf<String>()

        for (i in 1 until parts.size) {
            val sceneParts = parts[i].split("---")
            if (sceneParts.size >= 2) {
                vietnameseScenes.add(sceneParts[0].trim())
                englishScenes.add(sceneParts[1].trim())
            }
        }

        return listOf(
            vietnameseTitle,
            vietnameseScenes.joinToString("\n\n"),
            englishTitle,
            englishScenes.joinToString("\n\n")
        )
    }

    private fun parseScenes(content: String): List<String> {
        // Phân tích nội dung thành các cảnh dựa trên đoạn văn
        val paragraphs = content.split("\n\n")
        val scenes = mutableListOf<String>()

        // Nếu chỉ có một đoạn dài, chia thành các đoạn nhỏ hơn
        if (paragraphs.size <= 1) {
            val sentences = content.split(". ")
            val sceneSize = 3  // Số câu trong một cảnh
            var currentScene = StringBuilder()

            for ((index, sentence) in sentences.withIndex()) {
                if (index > 0 && index % sceneSize == 0) {
                    scenes.add(currentScene.toString().trim())
                    currentScene = StringBuilder()
                }

                if (currentScene.isNotEmpty()) {
                    currentScene.append(". ")
                }
                currentScene.append(sentence)
            }

            // Thêm cảnh cuối cùng nếu còn
            if (currentScene.isNotEmpty()) {
                scenes.add(currentScene.toString().trim())
            }
        } else {
            // Mỗi đoạn văn là một cảnh
            scenes.addAll(paragraphs)
        }

        return scenes
    }

    private suspend fun generateImageForScene(sceneContent: String, characters: List<String>, setting: String): String {
        // Sử dụng nội dung tiếng Anh để tạo prompt cho việc tạo ảnh
        val imagePrompt = """
            Create a children's story illustration:
            Characters: ${characters.joinToString(", ")}
            Setting: $setting
            Scene: $sceneContent
            Style: Cute, colorful, child-friendly digital art
        """.trimIndent()

        return try {
            val imageDescription = getImageDescriptionFromGemini(imagePrompt)
            generateImageWithFalAi(imageDescription)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating image for scene: ${e.message}")
            "https://picsum.photos/600/400?random=${System.currentTimeMillis()}"
        }
    }

    private suspend fun getImageDescriptionFromGemini(prompt: String): String {
        val contentInput = content { text(prompt) }
        val response = generativeModel.generateContent(contentInput)
        return response.text?.trim() ?: ""
    }

    private suspend fun generateImageWithFalAi(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val jsonMediaType = "application/json".toMediaType()
            val requestBody = JSONObject().apply {
                put("prompt", prompt)
                put("image_size", "square")
                put("num_inference_steps", 50)  // Increased for better quality
                put("guidance_scale", 8.5)      // Increased for better prompt adherence
                put("num_images", 1)
                put("enable_safety_checker", true)
                // Add seed to maintain consistency
                put("seed", System.currentTimeMillis() % 1000000)  // Use same seed base for similar style
            }.toString()

            val request = Request.Builder()
                .url(FAL_API_URL)
                .addHeader("Authorization", "Key $FAL_API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Log.e(TAG, "API Error - Code: ${response.code}, Body: $errorBody")
                    throw IOException("Unexpected response: ${response.code}")
                }

                // Parse the response to get image URL
                val responseBody = response.body?.string()
                Log.d(TAG, "fal.ai response: $responseBody")

                try {
                    val jsonResponse = JSONObject(responseBody ?: "")
                    // Kiểm tra xem có phải response queue hay không
                    if (jsonResponse.has("request_id")) {
                        // Đây là response queue, cần đợi và lấy kết quả
                        val requestId = jsonResponse.getString("request_id")
                        return@use waitForImageResult(requestId)
                    } else {
                        // Response trực tiếp - Parse đúng format JSON mới
                        val imagesArray = jsonResponse.getJSONArray("images")
                        if (imagesArray.length() > 0) {
                            val imageObject = imagesArray.getJSONObject(0)
                            val imageUrl = imageObject.getString("url")
                            if (imageUrl.isNotEmpty()) {
                                return@use downloadAndSaveImage(imageUrl)
                            } else {
                                throw IOException("Empty image URL in response")
                            }
                        } else {
                            throw IOException("No images in response")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing response: ${e.message}")
                    throw e
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling fal.ai API: ${e.message}")
            e.printStackTrace()
            "https://picsum.photos/600/400?random=${System.currentTimeMillis()}"
        }
    }

    private suspend fun waitForImageResult(requestId: String): String = withContext(Dispatchers.IO) {
        var attempts = 0
        val maxAttempts = 30 // Tối đa 30 lần thử
        val delayMs = 2000L // Đợi 2 giây giữa các lần thử

        while (attempts < maxAttempts) {
            val statusRequest = Request.Builder()
                .url("$FAL_API_URL/status/$requestId")
                .addHeader("Authorization", "Key $FAL_API_KEY")
                .build()

            client.newCall(statusRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val statusBody = response.body?.string()
                    val statusJson = JSONObject(statusBody ?: "")

                    when (statusJson.optString("status")) {
                        "COMPLETED" -> {
                            val result = statusJson.getJSONObject("result")
                            val imagesArray = result.getJSONArray("images")
                            if (imagesArray.length() > 0) {
                                val imageObject = imagesArray.getJSONObject(0)
                                val imageUrl = imageObject.getString("url")
                                if (imageUrl.isNotEmpty()) {
                                    return@withContext downloadAndSaveImage(imageUrl)
                                } else {
                                    throw IOException("Empty image URL in completed result")
                                }
                            } else {
                                throw IOException("No images in completed result")
                            }
                        }
                        "FAILED" -> throw IOException("Image generation failed")
                        else -> {
                            // Still processing, wait and try again
                            delay(delayMs)
                            attempts++
                        }
                    }
                }
            }
        }
        throw IOException("Timeout waiting for image generation")
    }

    private suspend fun downloadAndSaveImage(imageUrl: String): String = withContext(Dispatchers.IO) {
        val imageRequest = Request.Builder()
            .url(imageUrl)
            .build()

        client.newCall(imageRequest).execute().use { imageResponse ->
            if (!imageResponse.isSuccessful) {
                throw IOException("Failed to download image")
            }

            val fileName = "scene_${System.currentTimeMillis()}.jpg"
            val imageFile = File(imageDir, fileName)

            imageResponse.body?.let { responseBody ->
                imageFile.outputStream().use { fileOut ->
                    responseBody.byteStream().copyTo(fileOut)
                }
            }

            "file://${imageFile.absolutePath}"
        }
    }

    private fun buildImagePrompt(sceneContent: String, characters: List<String>, setting: String): String {
        val characterDescriptions = characters.joinToString("\n") { character ->
            val description = storyCharacterDescriptions[character] ?: character
            "Character '$character': $description"
        }

        return """
            Create a cohesive scene for a children's story series:
            
            Scene context: $sceneContent
            
            Setting: A consistent $setting environment
            
            Characters in scene:
            $characterDescriptions
            
            Style guide:
            $storyStylePrompt
            
            Additional requirements:
            - Maintain EXACT same character designs, colors, and proportions as previous scenes
            - Use same art style and color palette throughout
            - Ensure background elements match the setting consistently
            - Create clear visual continuity with previous scenes
            - Keep character positions and scale consistent
            - No text or watermarks
        """.trimIndent()
    }

    private suspend fun generateCharacterDescriptions(characters: List<String>) {
        val characterPrompt = """
            Describe the physical appearance of a character for a children's story in a consistent art style.
            Character: %s
            Include:
            1. Specific colors
            2. Size and shape
            3. Distinctive features
            4. Consistent details that should appear in every scene
            Keep it under 50 words and focus on visual elements only.
        """.trimIndent()

        for (character in characters) {
            val prompt = characterPrompt.format(character)
            val contentInput = content { text(prompt) }
            try {
                val response = generativeModel.generateContent(contentInput)
                val description = response.text?.trim() ?: ""
                if (description.isNotEmpty()) {
                    (storyCharacterDescriptions as MutableMap)[character] = description
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating description for $character: ${e.message}")
            }
        }
    }

    private fun createStoryStylePrompt(setting: String): String {
        return """
            Art style specifications:
            - Consistent kawaii/chibi art style throughout all scenes
            - Soft, warm color palette with pastel highlights
            - Simple, clean backgrounds focused on $setting elements
            - Characters maintain exact same design, colors, and proportions
            - Lighting and shading consistent across all scenes
            - Same level of detail and art style maintained throughout
        """.trimIndent()
    }

    // Hàm dọn dẹp ảnh cũ
    fun cleanupOldImages() {
        try {
            // Xóa các ảnh cũ hơn 24 giờ
            val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            imageDir.listFiles()?.forEach { file ->
                if (file.lastModified() < twentyFourHoursAgo) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old images: ${e.message}")
        }
    }
}