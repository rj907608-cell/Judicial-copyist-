package com.example.data.model

data class WordExportConfig(
    val fontName: String = "Traditional Arabic",
    val fontSizePt: Int = 16,
    val lineSpacing: Float = 1.25f,
    val includeHeaderLogo: Boolean = true,
    val documentTitle: String = "محضر جلسة قضائية",
    val isRtl: Boolean = true,
    val includeTimestamp: Boolean = true,
    val isJudicialFormat: Boolean = true,
    val ministryName: String = "المملكة العربية السعودية • وزارة العدل",
    val courtName: String = "المحكمة العامة",
    val circuitName: String = "الدائرة القضائية الأولى",
    val caseNumber: String = "",
    val caseYear: String = "1446هـ",
    val judgeName: String = "",
    val clerkName: String = ""
) {
    companion object {
        val AVAILABLE_FONTS = listOf(
            "Traditional Arabic",
            "Amiri",
            "Simplified Arabic",
            "Arial",
            "Times New Roman"
        )
    }
}
