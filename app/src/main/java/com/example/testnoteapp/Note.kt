package com.example.testnoteapp

data class Note(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val body: String,
    val color: Int,
    val date: String
)