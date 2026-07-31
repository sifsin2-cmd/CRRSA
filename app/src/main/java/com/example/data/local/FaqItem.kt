package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faq_items")
data class FaqItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "Residency", "Birth", "Death", "Marriage", "Divorce", "Fees & Offices"
    val questionEn: String,
    val questionAm: String,
    val answerEn: String,
    val answerAm: String,
    val viewsCount: Int = 0,
    val isPopular: Boolean = false,
    val updatedDate: Long = System.currentTimeMillis()
)
