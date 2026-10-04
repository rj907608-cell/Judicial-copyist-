package com.example.data.model

enum class DocumentCategory(val titleArabic: String, val titleEnglish: String, val iconName: String) {
    ALL("الكل", "All", "all"),
    COURT_SESSIONS("محاضر جلسات", "Court Sessions", "gavel"),
    COURT_DECISIONS("قرارات وأحكام", "Rulings & Decisions", "balance"),
    STATEMENTS("ضبط أقوال وشهادات", "Statements & Testimony", "record_voice_over"),
    LEGAL_MEMOS("لوائح ومذكرات", "Legal Memos", "folder"),
    GENERAL("عام", "General", "description");

    companion object {
        fun fromArabic(arabic: String): DocumentCategory {
            return entries.find { it.titleArabic == arabic } ?: GENERAL
        }
    }
}
