package com.example.testnoteapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotesViewModel(private val repo: NoteRepository) : ViewModel() {
    private val _search = MutableStateFlow("")
    val searchQuery = _search.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes = _search.flatMapLatest { q ->
        if (q.isEmpty()) repo.allNotes
        else repo.searchNotes(q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(t: String, c: String, clr: String) {
        viewModelScope.launch {
            val time = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date())
            repo.insert(NoteEntity(title = t, content = c, color = clr, createdAt = time))
        }
    }

    fun deleteNote(n: NoteEntity) {
        viewModelScope.launch {
            repo.delete(n)
        }
    }

    fun updateNote(n: NoteEntity) {
        viewModelScope.launch {
            repo.update(n)
        }
    }

    fun onSearchQueryChanged(q: String) {
        _search.value = q
    }
}