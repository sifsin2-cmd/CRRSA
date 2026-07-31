package com.example.data.repository

import android.content.Context
import com.example.data.api.GeminiChatService
import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.File

class CrrsaRepository(private val context: Context) {

    private val db = CrrsaDatabase.getDatabase(context)
    private val chatMessageDao = db.chatMessageDao()
    private val faqDao = db.faqDao()
    private val serviceMenuDao = db.serviceMenuDao()
    private val appSettingDao = db.appSettingDao()

    private val aiService = GeminiChatService()

    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()
    val allFaqs: Flow<List<FaqItem>> = faqDao.getAllFaqs()
    val allServices: Flow<List<ServiceMenuItem>> = serviceMenuDao.getAllServices()
    val activeServices: Flow<List<ServiceMenuItem>> = serviceMenuDao.getActiveServices()
    val allSettings: Flow<List<AppSetting>> = appSettingDao.getAllSettings()

    // 1. Send User Message & Receive AI Reply
    suspend fun sendMessage(
        text: String,
        attachmentPath: String? = null,
        attachmentType: String? = null
    ) {
        val currentLang = appSettingDao.getSettingValue("app_language") ?: "EN"

        // Save User Message
        val userMsg = ChatMessage(
            sender = "USER",
            text = text,
            language = currentLang,
            attachmentPath = attachmentPath,
            attachmentType = attachmentType
        )
        chatMessageDao.insertMessage(userMsg)

        // Get AI Response
        val aiReply = aiService.getAiResponse(text, currentLang)

        // Save Bot Message
        val botMsg = ChatMessage(
            sender = "BOT",
            text = aiReply,
            language = currentLang
        )
        chatMessageDao.insertMessage(botMsg)
    }

    // 2. Clear Chat History
    suspend fun clearChatHistory() {
        chatMessageDao.clearHistory()
        // Re-insert welcoming message
        chatMessageDao.insertMessage(
            ChatMessage(
                sender = "BOT",
                text = "Chat history cleared. How else can CRRSA AI assist you today?\nየንግግር ታሪክ ተደምስሷል። በምን ልርዳዎት?",
                language = "EN"
            )
        )
    }

    // 3. Delete Single Message
    suspend fun deleteMessage(id: Long) {
        chatMessageDao.deleteMessage(id)
    }

    // 4. FAQ Operations
    fun searchFaqs(query: String): Flow<List<FaqItem>> = faqDao.searchFaqs(query)

    suspend fun addFaq(faq: FaqItem): Long = faqDao.insertFaq(faq)

    suspend fun updateFaq(faq: FaqItem) = faqDao.updateFaq(faq)

    suspend fun deleteFaq(id: Long) = faqDao.deleteFaq(id)

    suspend fun recordFaqView(id: Long) = faqDao.incrementViewCount(id)

    // 5. Service Operations
    suspend fun addService(item: ServiceMenuItem): Long = serviceMenuDao.insertService(item)

    suspend fun updateService(item: ServiceMenuItem) = serviceMenuDao.updateService(item)

    suspend fun deleteService(id: Long) = serviceMenuDao.deleteService(id)

    // 6. Settings Operations
    suspend fun getSetting(key: String): String? = appSettingDao.getSettingValue(key)

    suspend fun updateSetting(key: String, value: String) {
        appSettingDao.saveSetting(AppSetting(key, value))
    }

    // 7. Ensure Upload Directory Permissions (755 simulation & filesystem check)
    fun ensureUploadsDirectory(): File {
        val uploadsDir = File(context.filesDir, "uploads")
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs()
        }
        // Set permissions 755 (Readable & Executable for all, Writable for owner)
        uploadsDir.setReadable(true, false)
        uploadsDir.setExecutable(true, false)
        uploadsDir.setWritable(true, true)
        return uploadsDir
    }

    fun getUploadDirStatus(): String {
        val dir = ensureUploadsDirectory()
        val r = if (dir.canRead()) "r" else "-"
        val w = if (dir.canWrite()) "w" else "-"
        val x = if (dir.canExecute()) "x" else "-"
        val fileCount = dir.listFiles()?.size ?: 0
        return "Directory: ${dir.absolutePath} [755 Mode: $r$w$x] ($fileCount files stored)"
    }
}
