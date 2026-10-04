package com.example

import com.example.data.model.WordExportConfig
import com.example.util.DocExporter
import com.example.util.DocxExporter
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

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

    @Test
    fun testDocxPackageGeneration() {
        val markdown = """
# قرار قضائي مسبب
## أولاً: الوقائع
حضر المدعي وطلب إلزام المدعى عليه بدفع مبلغ **215,000** ريال.
| البند | البيان | المبلغ |
|---|---|---|
| 1 | أصل المطالبة | 200,000 |
| 2 | أتعاب التقاضي | 15,000 |
- قررت الدائرة إمهال الخصوم.
- [كلمة غير واضحة]
---page-break---
# الصفحة الثانية من القرار
لذلك حكمت الدائرة بما هو آت.
        """.trimIndent()

        val config = WordExportConfig(
            fontName = "Traditional Arabic",
            fontSizePt = 16,
            courtName = "المحكمة التجارية",
            circuitName = "الدائرة الأولى",
            caseNumber = "46291044",
            judgeName = "فضيلة القاضي",
            clerkName = "الناسخ القضائي"
        )

        val docxBytes = DocxExporter.generateDocxBytes("قرار قضائي", markdown, config)
        assertTrue("Docx byte array should not be empty", docxBytes.isNotEmpty())

        // Read and verify zip structure and contents
        val entries = mutableListOf<String>()
        var documentXmlContent = ""

        ZipInputStream(ByteArrayInputStream(docxBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entries.add(entry.name)
                if (entry.name == "word/document.xml") {
                    documentXmlContent = zis.bufferedReader(Charsets.UTF_8).readText()
                }
                entry = zis.nextEntry
            }
        }

        assertTrue("Must contain [Content_Types].xml", entries.contains("[Content_Types].xml"))
        assertTrue("Must contain _rels/.rels", entries.contains("_rels/.rels"))
        assertTrue("Must contain word/_rels/document.xml.rels", entries.contains("word/_rels/document.xml.rels"))
        assertTrue("Must contain word/document.xml", entries.contains("word/document.xml"))
        assertTrue("Must contain word/styles.xml", entries.contains("word/styles.xml"))
        assertTrue("Must contain word/settings.xml", entries.contains("word/settings.xml"))

        // Check document XML contents
        assertTrue("Document must contain case number", documentXmlContent.contains("46291044"))
        assertTrue("Document must contain court name", documentXmlContent.contains("المحكمة التجارية"))
        assertTrue("Document must contain RTL bidi tag", documentXmlContent.contains("<w:bidi/>"))
        assertTrue("Document must contain court right margin 1701 dxa (3.0 cm)", documentXmlContent.contains("w:right=\"1701\""))
        assertTrue("Document must contain tables", documentXmlContent.contains("<w:tbl>"))
        assertTrue("Document must contain page break", documentXmlContent.contains("<w:br w:type=\"page\"/>"))
        assertTrue("Document must contain yellow highlight for illegible words", documentXmlContent.contains("<w:highlight w:val=\"yellow\"/>"))
        assertTrue("Document must contain clerk signature", documentXmlContent.contains("الناسخ القضائي"))
    }
}
