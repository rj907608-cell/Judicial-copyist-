package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_documents")
data class ScannedDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val extractedText: String,
    val originalImageUri: String? = null,
    val localImagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "محاضر جلسات",
    val isFavorite: Boolean = false,
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val tags: String = "",
    val pageCount: Int = 1,
    val caseNumber: String = "",
    val courtName: String = "وزارة العدل",
    val templateType: String = "محضر جلسة قضائية"
)
