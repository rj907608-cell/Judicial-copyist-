package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScannedDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedDocumentDao {
    @Query("SELECT * FROM scanned_documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<ScannedDocument>>

    @Query("SELECT * FROM scanned_documents WHERE id = :id")
    fun getDocumentById(id: Long): Flow<ScannedDocument?>

    @Query("SELECT * FROM scanned_documents WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteDocuments(): Flow<List<ScannedDocument>>

    @Query("SELECT * FROM scanned_documents WHERE category = :category ORDER BY timestamp DESC")
    fun getDocumentsByCategory(category: String): Flow<List<ScannedDocument>>

    @Query("SELECT * FROM scanned_documents WHERE title LIKE '%' || :query || '%' OR extractedText LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchDocuments(query: String): Flow<List<ScannedDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: ScannedDocument): Long

    @Update
    suspend fun updateDocument(document: ScannedDocument)

    @Delete
    suspend fun deleteDocument(document: ScannedDocument)

    @Query("DELETE FROM scanned_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("UPDATE scanned_documents SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE scanned_documents SET extractedText = :newText, wordCount = :wordCount, charCount = :charCount WHERE id = :id")
    suspend fun updateExtractedText(id: Long, newText: String, wordCount: Int, charCount: Int)
}
