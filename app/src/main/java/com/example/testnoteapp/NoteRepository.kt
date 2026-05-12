package com.example.testnoteapp

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    suspend fun insert(note: NoteEntity) = noteDao.insertNote(note)

    suspend fun delete(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun update(note: NoteEntity) = noteDao.updateNote(note)
}