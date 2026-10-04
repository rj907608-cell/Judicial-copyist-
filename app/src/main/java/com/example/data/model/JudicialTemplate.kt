package com.example.data.model

enum class JudicialTemplate(
    val titleArabic: String,
    val description: String,
    val iconName: String,
    val defaultTitle: String
) {
    SESSION_MINUTES(
        titleArabic = "محضر جلسة قضائية",
        description = "محضر وقائع ومجريات الجلسة وأقوال الخصوم والطلبات",
        iconName = "gavel",
        defaultTitle = "محضر جلسة قضائية"
    ),
    JUDICIAL_DECISION(
        titleArabic = "قرار قضائي / صك حكم",
        description = "ديباجة وحيثيات وتسبيب ومنطوق قرار أو صك حكم",
        iconName = "balance",
        defaultTitle = "قرار قضائي صادر عن الدائرة"
    ),
    WITNESS_TESTIMONY(
        titleArabic = "محضر ضبط أقوال وسماع شهادة",
        description = "ضبط أقوال شاهد أو إفادة خصم مع حلف اليمين",
        iconName = "record_voice_over",
        defaultTitle = "محضر سماع شهادة وضبط أقوال"
    ),
    GENERAL_JUDICIAL_NOTE(
        titleArabic = "مذكرة وملاحظات الدائرة",
        description = "ملاحظات وتوجيهات أصحاب الفضيلة القضاة ومسودات القضايا",
        iconName = "description",
        defaultTitle = "مسودة وملاحظات الدائرة القضائية"
    );

    companion object {
        fun fromArabic(arabic: String): JudicialTemplate {
            return entries.find { it.titleArabic == arabic } ?: SESSION_MINUTES
        }
    }
}

data class CaseMetadata(
    val ministryName: String = "وزارة العدل",
    val courtName: String = "المحكمة العامة",
    val circuitName: String = "الدائرة الحقوقية الأولى",
    val caseNumber: String = "",
    val caseYear: String = "1446هـ",
    val sessionDateHijri: String = "",
    val judgeName: String = "",
    val clerkName: String = ""
)
