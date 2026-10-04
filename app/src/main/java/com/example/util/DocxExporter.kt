package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.WordExportConfig
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Native OpenXML (.docx) Document Generator.
 * Creates 100% compliant Microsoft Word (.docx) packages adhering strictly to the ECMA-376 standard.
 * Features:
 * - Pure Android/JVM implementation with zero external library bloat or java.awt dependencies.
 * - Full Arabic RTL (Right-to-Left) bidi support.
 * - Ministry of Justice 3.0cm right margin for court registry archives.
 * - Traditional Arabic and Amiri typography support.
 * - Native OpenXML tables, headings, bullet lists, page breaks, and judicial metadata headers.
 */
object DocxExporter {

    private const val MIME_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

    /**
     * Generates a valid .docx ZIP archive byte array.
     */
    fun generateDocxBytes(
        title: String,
        markdownContent: String,
        config: WordExportConfig = WordExportConfig()
    ): ByteArray {
        val documentXml = generateDocumentXml(title, markdownContent, config)
        val stylesXml = generateStylesXml(config)
        val settingsXml = generateSettingsXml()
        val contentTypesXml = generateContentTypesXml()
        val rootRelsXml = generateRootRelsXml()
        val documentRelsXml = generateDocumentRelsXml()

        val baos = java.io.ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            // 1. [Content_Types].xml
            addZipFile(zos, "[Content_Types].xml", contentTypesXml)

            // 2. _rels/.rels
            addZipFile(zos, "_rels/.rels", rootRelsXml)

            // 3. word/_rels/document.xml.rels
            addZipFile(zos, "word/_rels/document.xml.rels", documentRelsXml)

            // 4. word/settings.xml
            addZipFile(zos, "word/settings.xml", settingsXml)

            // 5. word/styles.xml
            addZipFile(zos, "word/styles.xml", stylesXml)

            // 6. word/document.xml
            addZipFile(zos, "word/document.xml", documentXml)
        }
        return baos.toByteArray()
    }

    /**
     * Generates a valid .docx ZIP archive and writes it to the app cache.
     */
    fun createDocxFile(
        context: Context,
        title: String,
        markdownContent: String,
        config: WordExportConfig = WordExportConfig()
    ): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanTitle = title.replace("[^\\w\\s\\u0600-\\u06FF-]".toRegex(), "_").take(40)
            val docxFile = File(exportDir, "${cleanTitle}.docx")

            val bytes = generateDocxBytes(title, markdownContent, config)
            FileOutputStream(docxFile).use { it.write(bytes) }

            docxFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Exports and shares as a true .docx document.
     */
    fun shareDocumentAsDocx(
        context: Context,
        title: String,
        markdownContent: String,
        config: WordExportConfig = WordExportConfig()
    ) {
        val file = createDocxFile(context, title, markdownContent, config)
        if (file != null && file.exists()) {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE_DOCX
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "مستند رسمي بصيغة Microsoft Word (.docx) تم إنشاؤه عبر تطبيق الناسخ الذكي")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة ملف Word (.docx) إلى الحاسوب أو التطبيقات"))
        } else {
            Toast.makeText(context, "فشل إنشاء مستند Word (.docx)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addZipFile(zos: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        val bytes = content.toByteArray(Charsets.UTF_8)
        zos.write(bytes, 0, bytes.size)
        zos.closeEntry()
    }

    private fun generateContentTypesXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/word/settings.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/>
</Types>"""
    }

    private fun generateRootRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""
    }

    private fun generateDocumentRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings" Target="settings.xml"/>
</Relationships>"""
    }

    private fun generateSettingsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:settings xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:defaultTabStop w:val="720"/>
  <w:characterSpacingControl w:val="doNotCompress"/>
  <w:compat>
    <w:compatSetting w:name="compatibilityMode" w:uri="http://schemas.microsoft.com/office/word" w:val="15"/>
  </w:compat>
</w:settings>"""
    }

    private fun generateStylesXml(config: WordExportConfig): String {
        val font = escapeXml(config.fontName)
        val sz = config.fontSizePt * 2
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="$font" w:hAnsi="$font" w:cs="$font"/>
        <w:sz w:val="$sz"/>
        <w:szCs w:val="$sz"/>
        <w:lang w:val="ar-SA" w:bidi="ar-SA"/>
      </w:rPr>
    </w:rPrDefault>
    <w:pPrDefault>
      <w:pPr>
        <w:bidi/>
        <w:jc w:val="both"/>
        <w:spacing w:line="280" w:lineRule="auto" w:after="160"/>
      </w:pPr>
    </w:pPrDefault>
  </w:docDefaults>
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
    <w:name w:val="Normal"/>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:pPr>
      <w:bidi/>
      <w:jc w:val="center"/>
      <w:spacing w:before="240" w:after="180"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="$font" w:hAnsi="$font" w:cs="$font"/>
      <w:b/>
      <w:bCs/>
      <w:color w:val="0A4D40"/>
      <w:sz w:val="${sz + 8}"/>
      <w:szCs w:val="${sz + 8}"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/>
    <w:pPr>
      <w:bidi/>
      <w:spacing w:before="200" w:after="120"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="$font" w:hAnsi="$font" w:cs="$font"/>
      <w:b/>
      <w:bCs/>
      <w:color w:val="12574A"/>
      <w:sz w:val="${sz + 4}"/>
      <w:szCs w:val="${sz + 4}"/>
    </w:rPr>
  </w:style>
</w:styles>"""
    }

    private fun generateDocumentXml(title: String, markdown: String, config: WordExportConfig): String {
        val sb = StringBuilder()
        val font = escapeXml(config.fontName)
        val sz = config.fontSizePt * 2
        val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.forLanguageTag("ar")).format(Date())

        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
""")

        // Official Ministry of Justice Header Block
        if (config.isJudicialFormat) {
            sb.append("""
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:bidi/><w:spacing w:after="60"/></w:pPr>
      <w:r>
        <w:rPr><w:b/><w:bCs/><w:sz w:val="${sz + 4}"/><w:szCs w:val="${sz + 4}"/><w:color w:val="0A4D40"/></w:rPr>
        <w:t>${escapeXml(config.ministryName)}</w:t>
      </w:r>
    </w:p>
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:bidi/><w:spacing w:after="60"/></w:pPr>
      <w:r>
        <w:rPr><w:b/><w:bCs/><w:sz w:val="${sz + 2}"/><w:szCs w:val="${sz + 2}"/><w:color w:val="1B3831"/></w:rPr>
        <w:t>${escapeXml(config.courtName)} - ${escapeXml(config.circuitName)}</w:t>
      </w:r>
    </w:p>
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:bidi/><w:spacing w:after="180"/></w:pPr>
      <w:r>
        <w:rPr><w:sz w:val="${sz - 4}"/><w:szCs w:val="${sz - 4}"/><w:color w:val="555555"/></w:rPr>
        <w:t>العام: ${escapeXml(config.caseYear)} • تاريخ الطباعة: $dateStr</w:t>
      </w:r>
    </w:p>
""")

            // Case Info Summary Table
            if (config.caseNumber.isNotBlank() || config.judgeName.isNotBlank() || config.clerkName.isNotBlank()) {
                sb.append(generateCaseSummaryTableXml(config))
            }
        }

        // Parse markdown body into OOXML elements
        sb.append(parseMarkdownToDocxXml(markdown, config))

        // Judicial Signatures Block
        if (config.isJudicialFormat) {
            sb.append(generateSignaturesTableXml(config))
        }

        // Section Properties: A4 with 3.0cm right margin for court punching
        sb.append("""
    <w:sectPr>
      <w:pgSz w:w="11906" w:h="16838" w:code="9"/>
      <!-- Right margin: 1701 dxa = 3.0 cm for court registry archive hole punch -->
      <w:pgMar w:top="1440" w:right="1701" w:bottom="1440" w:left="1134" w:header="720" w:footer="720" w:gutter="0"/>
      <w:bidi/>
    </w:sectPr>
  </w:body>
</w:document>""")

        return sb.toString()
    }

    private fun generateCaseSummaryTableXml(config: WordExportConfig): String {
        val caseNo = if (config.caseNumber.isNotBlank()) config.caseNumber else "غير محدد"
        val judge = if (config.judgeName.isNotBlank()) config.judgeName else "فضيلة القاضي ناظر القضية"
        val clerk = if (config.clerkName.isNotBlank()) config.clerkName else "الناسخ القضائي"

        return """
    <w:tbl>
      <w:tblPr>
        <w:tblW w:w="5000" w:type="pct"/>
        <w:jc w:val="center"/>
        <w:bidiVisual/>
        <w:tblBorders>
          <w:top w:val="single" w:sz="8" w:space="0" w:color="0A4D40"/>
          <w:left w:val="single" w:sz="8" w:space="0" w:color="0A4D40"/>
          <w:bottom w:val="single" w:sz="8" w:space="0" w:color="0A4D40"/>
          <w:right w:val="single" w:sz="8" w:space="0" w:color="0A4D40"/>
          <w:insideH w:val="single" w:sz="4" w:space="0" w:color="99BCB4"/>
          <w:insideV w:val="single" w:sz="4" w:space="0" w:color="99BCB4"/>
        </w:tblBorders>
      </w:tblPr>
      <w:tr>
        <w:tc>
          <w:tcPr><w:shd w:val="clear" w:color="auto" w:fill="F8FBFA"/></w:tcPr>
          <w:p><w:pPr><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:bCs/><w:color w:val="0A4D40"/></w:rPr><w:t>رقم القضية: </w:t></w:r><w:r><w:t>${escapeXml(caseNo)}</w:t></w:r></w:p>
        </w:tc>
        <w:tc>
          <w:tcPr><w:shd w:val="clear" w:color="auto" w:fill="F8FBFA"/></w:tcPr>
          <w:p><w:pPr><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:bCs/><w:color w:val="0A4D40"/></w:rPr><w:t>الدائرة: </w:t></w:r><w:r><w:t>${escapeXml(config.circuitName)}</w:t></w:r></w:p>
        </w:tc>
      </w:tr>
      <w:tr>
        <w:tc>
          <w:tcPr><w:shd w:val="clear" w:color="auto" w:fill="F8FBFA"/></w:tcPr>
          <w:p><w:pPr><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:bCs/><w:color w:val="0A4D40"/></w:rPr><w:t>ناظر القضية: </w:t></w:r><w:r><w:t>${escapeXml(judge)}</w:t></w:r></w:p>
        </w:tc>
        <w:tc>
          <w:tcPr><w:shd w:val="clear" w:color="auto" w:fill="F8FBFA"/></w:tcPr>
          <w:p><w:pPr><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:bCs/><w:color w:val="0A4D40"/></w:rPr><w:t>الناسخ القضائي: </w:t></w:r><w:r><w:t>${escapeXml(clerk)}</w:t></w:r></w:p>
        </w:tc>
      </w:tr>
    </w:tbl>
    <w:p><w:pPr><w:bidi/><w:spacing w:after="160"/></w:pPr></w:p>
"""
    }

    private fun generateSignaturesTableXml(config: WordExportConfig): String {
        val judge = if (config.judgeName.isNotBlank()) config.judgeName else "فضيلة رئيس الدائرة"
        val clerk = if (config.clerkName.isNotBlank()) config.clerkName else "كاتب الضبط والناسخ"

        return """
    <w:p><w:pPr><w:bidi/><w:spacing w:before="360" w:after="120"/></w:pPr></w:p>
    <w:tbl>
      <w:tblPr>
        <w:tblW w:w="5000" w:type="pct"/>
        <w:jc w:val="center"/>
        <w:bidiVisual/>
        <w:tblBorders>
          <w:top w:val="none"/>
          <w:left w:val="none"/>
          <w:bottom w:val="none"/>
          <w:right w:val="none"/>
          <w:insideH w:val="none"/>
          <w:insideV w:val="none"/>
        </w:tblBorders>
      </w:tblPr>
      <w:tr>
        <w:tc>
          <w:tcPr><w:tcW w:w="2500" w:type="pct"/></w:tcPr>
          <w:p>
            <w:pPr><w:jc w:val="center"/><w:bidi/><w:spacing w:after="240"/></w:pPr>
            <w:r><w:rPr><w:b/><w:bCs/><w:color w:val="0A4D40"/></w:rPr><w:t>كاتب الضبط والناسخ القضائي</w:t></w:r>
          </w:p>
          <w:p>
            <w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr>
            <w:r><w:t>${escapeXml(clerk)}</w:t></w:r>
          </w:p>
        </w:tc>
        <w:tc>
          <w:tcPr><w:tcW w:w="2500" w:type="pct"/></w:tcPr>
          <w:p>
            <w:pPr><w:jc w:val="center"/><w:bidi/><w:spacing w:after="240"/></w:pPr>
            <w:r><w:rPr><w:b/><w:bCs/><w:color w:val="0A4D40"/></w:rPr><w:t>فضيلة ناظر القضية / رئيس الدائرة</w:t></w:r>
          </w:p>
          <w:p>
            <w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr>
            <w:r><w:t>${escapeXml(judge)}</w:t></w:r>
          </w:p>
        </w:tc>
      </w:tr>
    </w:tbl>
"""
    }

    private fun parseMarkdownToDocxXml(markdown: String, config: WordExportConfig): String {
        val sb = StringBuilder()
        val lines = markdown.lines()
        var inTable = false
        val tableRows = mutableListOf<List<String>>()

        fun flushTable() {
            if (tableRows.isNotEmpty()) {
                sb.append(generateDocxTableXml(tableRows))
                tableRows.clear()
            }
            inTable = false
        }

        for (line in lines) {
            val trimmed = line.trim()

            // Page Break
            if (trimmed == "---page-break---" || trimmed == "---" || trimmed == "***") {
                flushTable()
                sb.append("""
    <w:p>
      <w:pPr><w:bidi/></w:pPr>
      <w:r><w:br w:type="page"/></w:r>
    </w:p>
""")
                continue
            }

            // Table row
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                // Skip separator row (|---|---|)
                if (trimmed.replace("|", "").replace("-", "").replace(":", "").replace(" ", "").isEmpty()) {
                    continue
                }
                inTable = true
                val cells = trimmed.split("|")
                    .filterIndexed { index, _ -> index > 0 && index < trimmed.split("|").size - 1 }
                    .map { it.trim() }
                tableRows.add(cells)
                continue
            } else if (inTable) {
                flushTable()
            }

            // Headings
            if (trimmed.startsWith("# ")) {
                val text = trimmed.removePrefix("# ").trim()
                sb.append("""
    <w:p>
      <w:pPr><w:pStyle w:val="Heading1"/><w:jc w:val="center"/><w:bidi/></w:pPr>
      <w:r><w:t>${escapeXml(text)}</w:t></w:r>
    </w:p>
""")
            } else if (trimmed.startsWith("## ")) {
                val text = trimmed.removePrefix("## ").trim()
                sb.append("""
    <w:p>
      <w:pPr><w:pStyle w:val="Heading2"/><w:bidi/></w:pPr>
      <w:r><w:t>${escapeXml(text)}</w:t></w:r>
    </w:p>
""")
            } else if (trimmed.startsWith("### ")) {
                val text = trimmed.removePrefix("### ").trim()
                sb.append("""
    <w:p>
      <w:pPr><w:bidi/><w:spacing w:before="140" w:after="80"/></w:pPr>
      <w:r><w:rPr><w:b/><w:bCs/></w:rPr><w:t>${escapeXml(text)}</w:t></w:r>
    </w:p>
""")
            }
            // Bullet List
            else if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
                val text = trimmed.substring(2).trim()
                sb.append("""
    <w:p>
      <w:pPr><w:bidi/><w:ind w:right="360"/><w:spacing w:after="80"/></w:pPr>
      <w:r><w:rPr><w:color w:val="0A4D40"/><w:b/><w:bCs/></w:rPr><w:t>• </w:t></w:r>
      ${generateFormattedRunsXml(text)}
    </w:p>
""")
            }
            // Numbered List
            else if (trimmed.matches("^\\d+\\.\\s+.*".toRegex())) {
                val number = trimmed.substringBefore(".")
                val text = trimmed.replaceFirst("^\\d+\\.\\s+".toRegex(), "")
                sb.append("""
    <w:p>
      <w:pPr><w:bidi/><w:ind w:right="360"/><w:spacing w:after="80"/></w:pPr>
      <w:r><w:rPr><w:b/><w:bCs/></w:rPr><w:t>$number. </w:t></w:r>
      ${generateFormattedRunsXml(text)}
    </w:p>
""")
            }
            // Empty Line
            else if (trimmed.isEmpty()) {
                sb.append("<w:p><w:pPr><w:bidi/><w:spacing w:after=\"80\"/></w:pPr></w:p>\n")
            }
            // Normal paragraph
            else {
                sb.append("""
    <w:p>
      <w:pPr><w:bidi/><w:jc w:val="both"/></w:pPr>
      ${generateFormattedRunsXml(trimmed)}
    </w:p>
""")
            }
        }

        if (inTable) {
            flushTable()
        }

        return sb.toString()
    }

    private fun generateDocxTableXml(rows: List<List<String>>): String {
        val sb = StringBuilder()
        sb.append("""
    <w:tbl>
      <w:tblPr>
        <w:tblW w:w="5000" w:type="pct"/>
        <w:jc w:val="center"/>
        <w:bidiVisual/>
        <w:tblBorders>
          <w:top w:val="single" w:sz="6" w:space="0" w:color="777777"/>
          <w:left w:val="single" w:sz="6" w:space="0" w:color="777777"/>
          <w:bottom w:val="single" w:sz="6" w:space="0" w:color="777777"/>
          <w:right w:val="single" w:sz="6" w:space="0" w:color="777777"/>
          <w:insideH w:val="single" w:sz="4" w:space="0" w:color="BBBBBB"/>
          <w:insideV w:val="single" w:sz="4" w:space="0" w:color="BBBBBB"/>
        </w:tblBorders>
      </w:tblPr>
""")

        for ((rIdx, row) in rows.withIndex()) {
            val isHeader = rIdx == 0
            sb.append("      <w:tr>\n")
            for (cell in row) {
                val bg = if (isHeader) "E6F2EE" else if (rIdx % 2 == 1) "FAF8F4" else "FFFFFF"
                sb.append("""
        <w:tc>
          <w:tcPr>
            <w:shd w:val="clear" w:color="auto" w:fill="$bg"/>
            <w:tcMar><w:top w:w="120"/><w:bottom w:w="120"/><w:left w:w="180"/><w:right w:w="180"/></w:tcMar>
          </w:tcPr>
          <w:p>
            <w:pPr><w:bidi/><w:jc w:val="${if (isHeader) "center" else "right"}"/></w:pPr>
            ${if (isHeader) "<w:r><w:rPr><w:b/><w:bCs/><w:color w:val=\"0A4D40\"/></w:rPr><w:t>${escapeXml(cell)}</w:t></w:r>" else generateFormattedRunsXml(cell)}
          </w:p>
        </w:tc>
""")
            }
            sb.append("      </w:tr>\n")
        }
        sb.append("    </w:tbl>\n")
        sb.append("    <w:p><w:pPr><w:bidi/><w:spacing w:after=\"120\"/></w:pPr></w:p>\n")
        return sb.toString()
    }

    private fun generateFormattedRunsXml(text: String): String {
        // Regex pattern to extract tokens: bold, italic, illegible tag
        val pattern = Regex("(\\*\\*.*?\\*\\*|\\*.*?\\*|\\[كلمة غير واضحة\\])")
        val sb = StringBuilder()
        var lastIdx = 0

        pattern.findAll(text).forEach { match ->
            if (match.range.first > lastIdx) {
                val plain = text.substring(lastIdx, match.range.first)
                sb.append(createRunXml(plain, isBold = false, isItalic = false, isHighlight = false))
            }
            val token = match.value
            when {
                token.startsWith("**") && token.endsWith("**") -> {
                    val inner = token.removeSurrounding("**")
                    sb.append(createRunXml(inner, isBold = true, isItalic = false, isHighlight = false))
                }
                token.startsWith("*") && token.endsWith("*") -> {
                    val inner = token.removeSurrounding("*")
                    sb.append(createRunXml(inner, isBold = false, isItalic = true, isHighlight = false))
                }
                token == "[كلمة غير واضحة]" -> {
                    sb.append(createRunXml("[كلمة غير واضحة]", isBold = true, isItalic = false, isHighlight = true))
                }
            }
            lastIdx = match.range.last + 1
        }

        if (lastIdx < text.length) {
            val remaining = text.substring(lastIdx)
            sb.append(createRunXml(remaining, isBold = false, isItalic = false, isHighlight = false))
        }

        return sb.toString()
    }

    private fun createRunXml(text: String, isBold: Boolean, isItalic: Boolean, isHighlight: Boolean): String {
        val rPr = StringBuilder()
        if (isBold) rPr.append("<w:b/><w:bCs/>")
        if (isItalic) rPr.append("<w:i/><w:iCs/>")
        if (isHighlight) rPr.append("<w:highlight w:val=\"yellow\"/><w:color w:val=\"92400E\"/>")

        val rPrXml = if (rPr.isNotEmpty()) "<w:rPr>$rPr</w:rPr>" else ""
        return "<w:r>$rPrXml<w:t xml:space=\"preserve\">${escapeXml(text)}</w:t></w:r>"
    }

    private fun escapeXml(str: String): String {
        return str
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
