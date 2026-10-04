package com.example.data.db

import com.example.data.model.ScannedDocument
import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val dao: ScannedDocumentDao) {
    val allDocuments: Flow<List<ScannedDocument>> = dao.getAllDocuments()
    val favoriteDocuments: Flow<List<ScannedDocument>> = dao.getFavoriteDocuments()

    fun getDocumentById(id: Long): Flow<ScannedDocument?> = dao.getDocumentById(id)

    fun getDocumentsByCategory(category: String): Flow<List<ScannedDocument>> = dao.getDocumentsByCategory(category)

    fun searchDocuments(query: String): Flow<List<ScannedDocument>> = dao.searchDocuments(query)

    suspend fun insertDocument(document: ScannedDocument): Long = dao.insertDocument(document)

    suspend fun updateDocument(document: ScannedDocument) = dao.updateDocument(document)

    suspend fun deleteDocument(document: ScannedDocument) = dao.deleteDocument(document)

    suspend fun deleteDocumentById(id: Long) = dao.deleteDocumentById(id)

    suspend fun toggleFavorite(id: Long, current: Boolean) = dao.updateFavorite(id, !current)

    suspend fun updateText(id: Long, newText: String) {
        val words = newText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val chars = newText.length
        dao.updateExtractedText(id, newText, words, chars)
    }
}
