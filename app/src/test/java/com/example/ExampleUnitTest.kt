package com.example

import com.example.data.model.WordExportConfig
import com.example.util.DocExporter
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testWordHtmlExportJudicialFormatting() {
        val markdown = """
# محضر جلسة قضائية
## أقوال المدعي
- **المطالبة:** إلزام المدعى عليه بالسداد
| الدفعة | القيمة |
|---|---|
| 1 | 50000 |
ملاحظة بكلمة [كلمة غير واضحة] هنا.
        """.trimIndent()

        val config = WordExportConfig(
            fontName = "Traditional Arabic",
            fontSizePt = 16,
            courtName = "المحكمة العامة",
            circuitName = "الدائرة الأولى",
            caseNumber = "461028392",
            judgeName = "فضيلة الشيخ القاضي",
            clerkName = "الناسخ القضائي"
        )
        val html = DocExporter.createWordHtml("محضر جلسة", markdown, config)

        assertTrue("Should contain Word doc declaration", html.contains("xmlns:w='urn:schemas-microsoft-com:office:word'"))
        assertTrue("Should contain RTL direction", html.contains("direction: rtl"))
        assertTrue("Should contain 3.0cm court right margin", html.contains("3.0cm"))
        assertTrue("Should contain Ministry of Justice header", html.contains("وزارة العدل"))
        assertTrue("Should contain Court name", html.contains("المحكمة العامة"))
        assertTrue("Should contain Case number", html.contains("461028392"))
        assertTrue("Should contain H1 header", html.contains("<h1>محضر جلسة قضائية</h1>"))
        assertTrue("Should contain table", html.contains("<table class=\"data-table\">"))
        assertTrue("Should contain illegible word badge", html.contains("illegible-word"))
        assertTrue("Should contain clerk signature title", html.contains("كاتب الضبط والناسخ القضائي"))
    }
}
