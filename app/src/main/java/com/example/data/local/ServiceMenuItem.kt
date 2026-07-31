package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_menu_items")
data class ServiceMenuItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceCode: String, // e.g., "BIRTH_REG", "RESIDENCY_ID", "MARRIAGE_REG", "DEATH_REG", "DIVORCE_REG", "CERT_COPY", "ID_RENEWAL"
    val category: String, // "Civil Registration" or "Residency & Verification"
    val titleEn: String,
    val titleAm: String,
    val descriptionEn: String,
    val descriptionAm: String,
    val requiredDocumentsEn: String,
    val requiredDocumentsAm: String,
    val feeEtb: Double,
    val processingTimeDays: Int,
    val isActive: Boolean = true
)
