package com.example.kingmaker.service

data class NextAction(
    val id: Int,
    val title: String,
    val who: String,
    val context: String,
    val lastInteraction: String,
    val whyNow: String,
    val message: String
)
