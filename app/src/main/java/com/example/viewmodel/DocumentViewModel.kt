package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiOcrService
import com.example.data.db.AppDatabase
import com.example.data.db.DocumentRepository
import com.example.data.model.*
import com.example.util.ImageUtils
import com.example.util.SampleDataHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DocumentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository
    val geminiService: GeminiOcrService

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DocumentRepository(db.scannedDocumentDao())
        geminiService = GeminiOcrService(application)

        viewModelScope.launch {
            repository.allDocuments.firstOrNull()?.let { docs ->
                if (docs.isEmpty()) {
                    preloadInitialSamples()
                }
            }
        }
    }

    // Active Category Filter
    private val _selectedCategory = MutableStateFlow(DocumentCategory.ALL)
    val selectedCategory: StateFlow<DocumentCategory> = _selectedCategory.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Documents Flow combining category and search
    val documents: StateFlow<List<ScannedDocument>> = combine(
        repository.allDocuments,
        _selectedCategory,
        _searchQuery
    ) { allDocs, category, query ->
        allDocs.filter { doc ->
            val matchesCategory = when (category) {
                DocumentCategory.ALL -> true
                else -> doc.category == category.titleArabic
            }
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                doc.title.contains(query, ignoreCase = true) ||
                doc.extractedText.contains(query, ignoreCase = true) ||
                doc.caseNumber.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // OCR Processing State
    private val _ocrState = MutableStateFlow<OcrProcessingState>(OcrProcessingState.Idle)
    val ocrState: StateFlow<OcrProcessingState> = _ocrState.asStateFlow()

    // Multi-page scans support (Pages 1, 2, 3...)
    private val _scannedPages = MutableStateFlow<List<Bitmap>>(emptyList())
    val scannedPages: StateFlow<List<Bitmap>> = _scannedPages.asStateFlow()

    // Active Case Metadata (Court, Circuit, Case Number, Judge, Typist)
    private val _caseMetadata = MutableStateFlow(CaseMetadata())
    val caseMetadata: StateFlow<CaseMetadata> = _caseMetadata.asStateFlow()

    // Active Judicial Template
    private val _selectedTemplate = MutableStateFlow(JudicialTemplate.SESSION_MINUTES)
    val selectedTemplate: StateFlow<JudicialTemplate> = _selectedTemplate.asStateFlow()

    private val _currentDocument = MutableStateFlow<ScannedDocument?>(null)
    val currentDocument: StateFlow<ScannedDocument?> = _currentDocument.asStateFlow()

    fun setSelectedCategory(category: DocumentCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setJudicialTemplate(template: JudicialTemplate) {
        _selectedTemplate.value = template
    }

    fun updateCaseMetadata(metadata: CaseMetadata) {
        _caseMetadata.value = metadata
    }

    // Page Management for Multi-page sessions
    fun addPage(bitmap: Bitmap) {
        _scannedPages.value = _scannedPages.value + bitmap
        _ocrState.value = OcrProcessingState.Idle
    }

    fun setSinglePage(bitmap: Bitmap) {
        _scannedPages.value = listOf(bitmap)
        _ocrState.value = OcrProcessingState.Idle
    }

    fun removePageAt(index: Int) {
        val current = _scannedPages.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _scannedPages.value = current
        }
    }

    fun clearPages() {
        _scannedPages.value = emptyList()
        _ocrState.value = OcrProcessingState.Idle
    }

    fun resetOcrState() {
        _ocrState.value = OcrProcessingState.Idle
    }

    fun selectDocument(document: ScannedDocument) {
        _currentDocument.value = document
    }

    fun selectDocumentById(id: Long) {
        viewModelScope.launch {
            repository.getDocumentById(id).collect { doc ->
                _currentDocument.value = doc
            }
        }
    }

    /**
     * Executes specialized Judicial OCR analysis on one or multiple pages.
     */
    fun analyzeJudicialPages(
        pages: List<Bitmap>,
        template: JudicialTemplate,
        customInstruction: String? = null,
        metadata: CaseMetadata = _caseMetadata.value
    ) {
        if (pages.isEmpty()) return

        viewModelScope.launch {
            val pageCountText = if (pages.size > 1) " (${pages.size} صفحات)" else ""
            _ocrState.value = OcrProcessingState.Processing(
                "جاري فحص وتفريغ خط اليد للمحضر القضائي$pageCountText...",
                0.3f
            )

            // Save first page as primary preview
            val firstPagePath = ImageUtils.saveBitmapToInternalStorage(getApplication(), pages.first(), "court_p1")

            _ocrState.value = OcrProcessingState.Processing(
                "فك الاختصارات القضائية، وتنسيق الأقوال والجداول...",
                0.7f
            )

            val result = geminiService.performHandwritingOcr(pages, customInstruction, template)

            result.onSuccess { formattedText ->
                _ocrState.value = OcrProcessingState.Processing(
                    "تجهيز صك المحضر للتصدير إلى Word بالمعايير الرسمية...",
                    0.95f
                )

                val detectedTitle = extractTitleFromMarkdown(formattedText).ifBlank {
                    "${template.defaultTitle} - قضية ${metadata.caseNumber.ifBlank { "رقمية" }}"
                }
                val words = formattedText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                val chars = formattedText.length

                val categoryName = when (template) {
                    JudicialTemplate.SESSION_MINUTES -> DocumentCategory.COURT_SESSIONS.titleArabic
                    JudicialTemplate.JUDICIAL_DECISION -> DocumentCategory.COURT_DECISIONS.titleArabic
                    JudicialTemplate.WITNESS_TESTIMONY -> DocumentCategory.STATEMENTS.titleArabic
                    JudicialTemplate.GENERAL_JUDICIAL_NOTE -> DocumentCategory.LEGAL_MEMOS.titleArabic
                }

                val newDoc = ScannedDocument(
                    title = detectedTitle,
                    extractedText = formattedText,
                    localImagePath = firstPagePath,
                    category = categoryName,
                    wordCount = words,
                    charCount = chars,
                    pageCount = pages.size,
                    caseNumber = metadata.caseNumber,
                    courtName = "${metadata.courtName} - ${metadata.circuitName}",
                    templateType = template.titleArabic
                )

                val newId = repository.insertDocument(newDoc)
                val savedDoc = newDoc.copy(id = newId)
                _currentDocument.value = savedDoc
                _ocrState.value = OcrProcessingState.Success(formattedText, detectedTitle)

            }.onFailure { error ->
                _ocrState.value = OcrProcessingState.Error(
                    error.message ?: "حدث خطأ غير متوقع أثناء معالجة المستند القضائي."
                )
            }
        }
    }

    /**
     * Loads a sample judicial document.
     */
    fun loadSample(sample: SampleDocument) {
        val sampleBitmap = SampleDataHelper.generateSampleBitmap(sample)
        _scannedPages.value = listOf(sampleBitmap)
        _ocrState.value = OcrProcessingState.Idle

        viewModelScope.launch {
            val localPath = ImageUtils.saveBitmapToInternalStorage(getApplication(), sampleBitmap, "sample_court")
            val words = sample.formattedResult.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            val doc = ScannedDocument(
                title = sample.title,
                extractedText = sample.formattedResult,
                localImagePath = localPath,
                category = sample.category.titleArabic,
                wordCount = words,
                charCount = sample.formattedResult.length,
                pageCount = 1,
                courtName = "وزارة العدل - المحكمة العامة",
                templateType = "محضر جلسة قضائية"
            )
            val newId = repository.insertDocument(doc)
            _currentDocument.value = doc.copy(id = newId)
            _ocrState.value = OcrProcessingState.Success(sample.formattedResult, sample.title)
        }
    }

    fun updateCurrentDocumentText(newText: String) {
        val doc = _currentDocument.value ?: return
        viewModelScope.launch {
            repository.updateText(doc.id, newText)
            _currentDocument.value = doc.copy(
                extractedText = newText,
                wordCount = newText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size,
                charCount = newText.length
            )
        }
    }

    fun toggleFavorite(document: ScannedDocument) {
        viewModelScope.launch {
            repository.toggleFavorite(document.id, document.isFavorite)
        }
    }

    fun deleteDocument(document: ScannedDocument) {
        viewModelScope.launch {
            repository.deleteDocument(document)
            if (_currentDocument.value?.id == document.id) {
                _currentDocument.value = null
            }
        }
    }

    private fun extractTitleFromMarkdown(markdown: String): String {
        for (line in markdown.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("# ")) {
                return trimmed.removePrefix("# ").trim().take(60)
            } else if (trimmed.startsWith("## ")) {
                return trimmed.removePrefix("## ").trim().take(60)
            }
        }
        return "محضر جلسة قضائية - " + java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.forLanguageTag("ar")).format(java.util.Date())
    }

    private suspend fun preloadInitialSamples() {
        for (sample in SampleDataHelper.SAMPLES.take(2)) {
            val bitmap = SampleDataHelper.generateSampleBitmap(sample)
            val path = ImageUtils.saveBitmapToInternalStorage(getApplication(), bitmap, "initial_${sample.id}")
            val words = sample.formattedResult.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            repository.insertDocument(
                ScannedDocument(
                    title = sample.title,
                    extractedText = sample.formattedResult,
                    localImagePath = path,
                    category = sample.category.titleArabic,
                    wordCount = words,
                    charCount = sample.formattedResult.length,
                    pageCount = 1,
                    courtName = "المحكمة العامة - الدائرة الأولى",
                    templateType = "محضر جلسة قضائية"
                )
            )
        }
    }
}
