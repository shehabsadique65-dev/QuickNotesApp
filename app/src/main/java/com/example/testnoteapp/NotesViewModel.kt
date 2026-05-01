package com.example.testnoteapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

class NotesViewModel : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val notes: StateFlow<List<Note>> = combine(_notes, _searchQuery) { notes, query ->
        if (query.isBlank()) notes
        else notes.filter { it.title.contains(query, ignoreCase = true) || it.body.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(title: String, body: String, color: Int) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val newNote = Note(title = title, body = body, color = color, date = currentDate)
        _notes.value = listOf(newNote) + _notes.value
    }

    fun updateNote(id: Long, title: String, body: String, color: Int) {
        _notes.value = _notes.value.map {
            if (it.id == id) it.copy(title = title, body = body, color = color) else it
        }
    }

    fun deleteNote(note: Note) {
        _notes.value = _notes.value.filter { it.id != note.id }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}