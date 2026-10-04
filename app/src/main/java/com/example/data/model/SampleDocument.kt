package com.example.data.model

data class SampleDocument(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: DocumentCategory,
    val description: String,
    val formattedResult: String,
    val visualType: SampleVisualType
)

enum class SampleVisualType {
    LECTURE_NOTES,
    ACCOUNTING_LEDGER,
    HISTORICAL_MANUSCRIPT,
    OFFICIAL_LETTER
}
