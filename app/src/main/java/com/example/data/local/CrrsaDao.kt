package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)
}

@Dao
interface FaqDao {
    @Query("SELECT * FROM faq_items ORDER BY isPopular DESC, category ASC")
    fun getAllFaqs(): Flow<List<FaqItem>>

    @Query("SELECT * FROM faq_items WHERE category = :category")
    fun getFaqsByCategory(category: String): Flow<List<FaqItem>>

    @Query("SELECT * FROM faq_items WHERE questionEn LIKE '%' || :query || '%' OR questionAm LIKE '%' || :query || '%' OR answerEn LIKE '%' || :query || '%' OR answerAm LIKE '%' || :query || '%'")
    fun searchFaqs(query: String): Flow<List<FaqItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaq(faq: FaqItem): Long

    @Update
    suspend fun updateFaq(faq: FaqItem)

    @Query("DELETE FROM faq_items WHERE id = :id")
    suspend fun deleteFaq(id: Long)

    @Query("UPDATE faq_items SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementViewCount(id: Long)
}

@Dao
interface ServiceMenuDao {
    @Query("SELECT * FROM service_menu_items ORDER BY id ASC")
    fun getAllServices(): Flow<List<ServiceMenuItem>>

    @Query("SELECT * FROM service_menu_items WHERE isActive = 1")
    fun getActiveServices(): Flow<List<ServiceMenuItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(item: ServiceMenuItem): Long

    @Update
    suspend fun updateService(item: ServiceMenuItem)

    @Query("DELETE FROM service_menu_items WHERE id = :id")
    suspend fun deleteService(id: Long)
}

@Dao
interface AppSettingDao {
    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSetting>>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSetting)
}
