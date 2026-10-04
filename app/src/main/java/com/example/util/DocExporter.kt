package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
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

object DocExporter {

    /**
     * Converts markdown text into official Ministry of Justice / Court Word-compatible HTML format (.doc).
     * Microsoft Word and LibreOffice natively open this with full RTL, court margins, and official styling.
     */
    fun createWordHtml(title: String, markdownContent: String, config: WordExportConfig): String {
        val htmlBody = markdownToStyledHtml(markdownContent)
        val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.forLanguageTag("ar")).format(Date())

        return """
<!DOCTYPE html>
<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
<head>
    <meta charset="utf-8">
    <title>$title</title>
    <!--[if gte mso 9]>
    <xml>
        <w:WordDocument>
            <w:View>Print</w:View>
            <w:Zoom>100</w:Zoom>
            <w:DoNotOptimizeForBrowser/>
        </w:WordDocument>
    </xml>
    <![endif]-->
    <style>
        @page {
            size: A4;
            /* 3.0cm right margin for court binder punching and official filing */
            margin: 2.5cm 2.0cm 2.5cm 3.0cm;
            mso-page-orientation: portrait;
        }
        body {
            font-family: '${config.fontName}', 'Traditional Arabic', 'Amiri', 'Simplified Arabic', serif;
            font-size: ${config.fontSizePt}pt;
            line-height: ${config.lineSpacing};
            direction: rtl;
            text-align: right;
            color: #111111;
            background-color: #FFFFFF;
        }
        .court-header {
            text-align: center;
            border-bottom: 2pt solid #0A4D40;
            padding-bottom: 8pt;
            margin-bottom: 14pt;
        }
        .kingdom-title {
            font-size: 16pt;
            font-weight: bold;
            color: #0A4D40;
            margin-bottom: 2pt;
        }
        .ministry-title {
            font-size: 14pt;
            font-weight: bold;
            color: #1B3831;
            margin-bottom: 2pt;
        }
        .court-sub {
            font-size: 12pt;
            color: #333333;
        }
        .case-info-table {
            border-collapse: collapse;
            width: 100%;
            margin-top: 10pt;
            margin-bottom: 16pt;
            direction: rtl;
            border: 1.5pt solid #0A4D40;
        }
        .case-info-table td {
            border: 1pt solid #99BCB4;
            padding: 5pt 10pt;
            font-size: 11pt;
            background-color: #F8FBFA;
        }
        .case-info-table td strong {
            color: #0A4D40;
        }
        h1 {
            color: #0A4D40;
            font-size: ${config.fontSizePt + 4}pt;
            text-align: center;
            border-bottom: 1.5pt solid #C89528;
            padding-bottom: 5pt;
            margin-top: 14pt;
            margin-bottom: 12pt;
            font-weight: bold;
        }
        h2 {
            color: #12574A;
            font-size: ${config.fontSizePt + 2}pt;
            margin-top: 12pt;
            margin-bottom: 6pt;
            font-weight: bold;
        }
        h3 {
            color: #222222;
            font-size: ${config.fontSizePt + 1}pt;
            margin-top: 10pt;
            margin-bottom: 4pt;
        }
        p {
            margin-top: 0;
            margin-bottom: 8pt;
            text-align: justify;
            text-justify: inter-word;
        }
        ul, ol {
            margin-top: 4pt;
            margin-bottom: 10pt;
            padding-right: 24pt;
        }
        li {
            margin-bottom: 4pt;
        }
        table.data-table {
            border-collapse: collapse;
            width: 100%;
            margin-top: 12pt;
            margin-bottom: 16pt;
            direction: rtl;
        }
        table.data-table th, table.data-table td {
            border: 1pt solid #777777;
            padding: 6pt 10pt;
            text-align: right;
        }
        table.data-table th {
            background-color: #E6F2EE;
            color: #0A4D40;
            font-weight: bold;
        }
        table.data-table tr:nth-child(even) {
            background-color: #FAF8F4;
        }
        .illegible-word {
            color: #B45309;
            background-color: #FEF3C7;
            padding: 1pt 5pt;
            border-radius: 3pt;
            font-weight: bold;
            border: 0.5pt solid #F59E0B;
        }
        .judicial-highlight {
            font-weight: bold;
            color: #0A4D40;
        }
        .page-break {
            page-break-before: always;
            clear: both;
            margin-top: 20pt;
            padding-top: 10pt;
            border-top: 1pt dashed #CCCCCC;
        }
        .signature-section {
            margin-top: 36pt;
            width: 100%;
            page-break-inside: avoid;
        }
        .signature-table {
            width: 100%;
            border: none;
            direction: rtl;
        }
        .signature-table td {
            border: none;
            text-align: center;
            vertical-align: top;
            width: 50%;
            padding: 10pt;
        }
        .signature-title {
            font-weight: bold;
            font-size: 13pt;
            color: #0A4D40;
            margin-bottom: 30pt;
        }
        .doc-footer {
            margin-top: 24pt;
            padding-top: 8pt;
            border-top: 1pt solid #DEDEDE;
            font-size: 9pt;
            color: #777777;
            text-align: center;
        }
    </style>
</head>
<body>
    ${if (config.isJudicialFormat) """
    <div class="court-header">
        <div class="kingdom-title">${config.ministryName}</div>
        <div class="ministry-title">${config.courtName} - ${config.circuitName}</div>
        <div class="court-sub">التاريخ: ${config.caseYear} • تاريخ الطباعة: $dateStr</div>
    </div>

    ${if (config.caseNumber.isNotBlank() || config.judgeName.isNotBlank() || config.clerkName.isNotBlank()) """
    <table class="case-info-table">
        <tr>
            ${if (config.caseNumber.isNotBlank()) "<td><strong>رقم القضية:</strong> ${config.caseNumber}</td>" else ""}
            <td><strong>الدائرة:</strong> ${config.circuitName}</td>
            <td><strong>العام:</strong> ${config.caseYear}</td>
        </tr>
        <tr>
            <td><strong>القاضي ناظر القضية:</strong> ${config.judgeName.ifBlank { "فضيلة القاضي" }}</td>
            <td colspan="2"><strong>كاتب الضبط والناسخ:</strong> ${config.clerkName.ifBlank { "الناسخ القضائي" }}</td>
        </tr>
    </table>
    """ else ""}
    """ else if (config.includeHeaderLogo) """
    <div class="court-header">
        <div class="kingdom-title">مستند قضائي مستخرج ومنسق عبر قلم OCR</div>
        <div class="court-sub">تاريخ الاستخراج: $dateStr</div>
    </div>
    """ else ""}

    <div class="content">
        $htmlBody
    </div>

    ${if (config.isJudicialFormat) """
    <div class="signature-section">
        <table class="signature-table">
            <tr>
                <td>
                    <div class="signature-title">كاتب الضبط والناسخ القضائي</div>
                    <div>${config.clerkName.ifBlank { "............................" }}</div>
                </td>
                <td>
                    <div class="signature-title">فضيلة رئيس الدائرة / ناظر القضية</div>
                    <div>${config.judgeName.ifBlank { "............................" }}</div>
                </td>
            </tr>
        </table>
    </div>
    """ else ""}

    <div class="doc-footer">
        تمت الرقمنة والتحرير بواسطة تطبيق قلم OCR المخصص للنسخ والمحاضر القضائية
    </div>
</body>
</html>
        """.trimIndent()
    }

    /**
     * Converts markdown headings, bullets, tables, and bold tags into HTML.
     */
    private fun markdownToStyledHtml(markdown: String): String {
        val lines = markdown.lines()
        val result = StringBuilder()
        var inTable = false
        var isHeaderRow = true
        var inList = false

        for (line in lines) {
            val trimmed = line.trim()

            // Page Break indicator from multi-page scans
            if (trimmed == "---page-break---" || trimmed == "---" || trimmed == "***") {
                if (inList) { result.append("</ul>\n"); inList = false }
                if (inTable) { result.append("</table>\n"); inTable = false }
                result.append("<div class=\"page-break\"><hr style=\"display:none;\"/></div>\n")
                continue
            }

            // Table row detection
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                if (inList) {
                    result.append("</ul>\n")
                    inList = false
                }
                // Separator row (|---|---|)
                if (trimmed.replace("|", "").replace("-", "").replace(":", "").replace(" ", "").isEmpty()) {
                    isHeaderRow = false
                    continue
                }

                if (!inTable) {
                    result.append("<table class=\"data-table\">\n")
                    inTable = true
                    isHeaderRow = true
                }

                result.append("  <tr>\n")
                val cells = trimmed.split("|").filterIndexed { index, _ -> index > 0 && index < trimmed.split("|").size - 1 }
                for (cell in cells) {
                    val tag = if (isHeaderRow) "th" else "td"
                    result.append("    <$tag>${formatInlineText(cell.trim())}</$tag>\n")
                }
                result.append("  </tr>\n")
                continue
            } else if (inTable) {
                result.append("</table>\n")
                inTable = false
            }

            // Headers
            if (trimmed.startsWith("### ")) {
                if (inList) { result.append("</ul>\n"); inList = false }
                result.append("<h3>${formatInlineText(trimmed.removePrefix("### "))}</h3>\n")
            } else if (trimmed.startsWith("## ")) {
                if (inList) { result.append("</ul>\n"); inList = false }
                result.append("<h2>${formatInlineText(trimmed.removePrefix("## "))}</h2>\n")
            } else if (trimmed.startsWith("# ")) {
                if (inList) { result.append("</ul>\n"); inList = false }
                result.append("<h1>${formatInlineText(trimmed.removePrefix("# "))}</h1>\n")
            }
            // Bullet list items
            else if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
                if (!inList) {
                    result.append("<ul>\n")
                    inList = true
                }
                val content = trimmed.substring(2).trim()
                result.append("  <li>${formatInlineText(content)}</li>\n")
            }
            // Numbered list items
            else if (trimmed.matches("^\\d+\\.\\s+.*".toRegex())) {
                if (inList) { result.append("</ul>\n"); inList = false }
                val content = trimmed.replaceFirst("^\\d+\\.\\s+".toRegex(), "")
                result.append("<p><strong>${trimmed.substringBefore(".")}</strong>. ${formatInlineText(content)}</p>\n")
            }
            // Empty line
            else if (trimmed.isEmpty()) {
                if (inList) { result.append("</ul>\n"); inList = false }
            }
            // Normal paragraph
            else {
                if (inList) { result.append("</ul>\n"); inList = false }
                result.append("<p>${formatInlineText(trimmed)}</p>\n")
            }
        }

        if (inList) {
            result.append("</ul>\n")
        }
        if (inTable) {
            result.append("</table>\n")
        }

        return result.toString()
    }

    private fun formatInlineText(text: String): String {
        var formatted = text
            // Bold **text**
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<strong>$1</strong>")
            // Italic *text*
            .replace(Regex("\\*(.*?)\\*"), "<em>$1</em>")
            // Illegible word tag
            .replace("[كلمة غير واضحة]", "<span class=\"illegible-word\">[كلمة غير واضحة]</span>")

        // Auto-emphasize core judicial decision phrases
        val judicialKeywords = listOf(
            "حكمت الدائرة بما يلي",
            "حكمت الدائرة بما هو آت",
            "قررت المحكمة",
            "وبسؤال المدعي",
            "وبمواجهة المدعى عليه",
            "رفعت الجلسة",
            "لذلك كله حكمت المحكمة"
        )
        for (kw in judicialKeywords) {
            if (formatted.contains(kw) && !formatted.contains("<strong>$kw</strong>")) {
                formatted = formatted.replace(kw, "<strong>$kw</strong>")
            }
        }

        return formatted
    }

    /**
     * Writes Word .doc file to app cache and returns shareable FileProvider Uri.
     */
    fun exportWordFile(context: Context, title: String, markdownContent: String, config: WordExportConfig): Uri? {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanTitle = title.replace("[^\\w\\s\\u0600-\\u06FF-]".toRegex(), "_").take(40)
            val file = File(exportDir, "${cleanTitle}_Word.doc")

            val htmlContent = createWordHtml(title, markdownContent, config)
            FileOutputStream(file).use { it.write(htmlContent.toByteArray(Charsets.UTF_8)) }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Shares document to external apps (Microsoft Word, Google Drive, WhatsApp, etc.).
     */
    fun shareDocumentAsWord(context: Context, title: String, markdownContent: String, config: WordExportConfig = WordExportConfig()) {
        val uri = exportWordFile(context, title, markdownContent, config)
        if (uri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/msword"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "مستند قضائي منسق عبر تطبيق قلم OCR")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة المحضر أو القرار إلى Word"))
        } else {
            Toast.makeText(context, "فشل إنشاء ملف Word", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareAsPlainText(context: Context, title: String, content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة النص"))
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Qalam OCR Judicial Text") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ النص القضائي المنسق للحافظة بنجاح", Toast.LENGTH_SHORT).show()
    }
}
