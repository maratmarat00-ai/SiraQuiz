package com.siraquiz.app

data class QuizQuestion(
    val category: String,
    val question: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String
)
