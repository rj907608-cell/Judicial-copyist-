package com.example.data.model

sealed class OcrProcessingState {
    object Idle : OcrProcessingState()
    data class Processing(val stageMessage: String, val progressFraction: Float = 0f) : OcrProcessingState()
    data class Success(val extractedText: String, val detectedTitle: String) : OcrProcessingState()
    data class Error(val errorMessage: String, val canRetry: Boolean = true) : OcrProcessingState()
}
