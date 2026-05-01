package com.example.testnoteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testnoteapp.ui.theme.TestnoteappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestnoteappTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF282a36)) {
                    NotesApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesApp(viewModel: NotesViewModel = viewModel()) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    
    val myColors = listOf(
        Color(0xFFff5555), Color(0xFF50fa7b), Color(0xFFf1fa8c), 
        Color(0xFFbd93f9), Color(0xFFff79c6), Color(0xFF8be9fd), 
        Color(0xFFffb86c), Color(0xFF6272a4)
    )
    var selectedColor by remember { mutableStateOf(myColors[0]) }
    var editingNote by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        containerColor = Color(0xFF282a36),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF44475a))) {
                TopAppBar(
                    title = { Text("Quick Notes", fontWeight = FontWeight.ExtraBold, color = Color(0xFFf8f8f2)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF44475a))
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search...", color = Color(0xFF6272a4)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8be9fd)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFf8f8f2),
                        unfocusedTextColor = Color(0xFFf8f8f2),
                        focusedContainerColor = Color(0xFF282a36),
                        unfocusedContainerColor = Color(0xFF282a36),
                        focusedIndicatorColor = Color(0xFFff79c6)
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editingNote = null
                    title = ""
                    body = ""
                    selectedColor = myColors[0]
                    showDialog = true 
                },
                containerColor = Color(0xFFbd93f9),
                contentColor = Color(0xFF282a36)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (notes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No notes found", color = Color(0xFF6272a4))
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(notes) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable {
                                    editingNote = note
                                    title = note.title
                                    body = note.body
                                    selectedColor = Color(note.color)
                                    showDialog = true
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(note.color))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        note.title, 
                                        fontWeight = FontWeight.Bold, 
                                        fontSize = 18.sp,
                                        color = Color(0xFF282a36),
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(onClick = { viewModel.deleteNote(note) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF282a36).copy(0.7f))
                                    }
                                }
                                Text(note.body, fontSize = 14.sp, color = Color(0xFF282a36), maxLines = 4)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    note.date, 
                                    fontSize = 10.sp, 
                                    color = Color(0xFF282a36).copy(0.5f),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = Color(0xFF44475a),
                title = { Text(if(editingNote == null) "New Note" else "Edit Note", color = Color(0xFFf8f8f2)) },
                text = {
                    Column {
                        TextField(
                            value = title, 
                            onValueChange = { title = it }, 
                            placeholder = { Text("Title", color = Color(0xFF6272a4)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color(0xFFf8f8f2),
                                unfocusedTextColor = Color(0xFFf8f8f2),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextField(
                            value = body, 
                            onValueChange = { body = it }, 
                            placeholder = { Text("Write something...", color = Color(0xFF6272a4)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color(0xFFf8f8f2),
                                unfocusedTextColor = Color(0xFFf8f8f2),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            myColors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { selectedColor = color }
                                        .then(if (selectedColor == color) Modifier.background(Color.White.copy(0.3f)) else Modifier)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (title.isNotBlank()) {
                            if (editingNote == null) {
                                viewModel.addNote(title, body, selectedColor.toArgb())
                            } else {
                                viewModel.updateNote(editingNote!!.id, title, body, selectedColor.toArgb())
                            }
                            showDialog = false
                        }
                    }) {
                        Text("SAVE", color = Color(0xFF50fa7b), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("CANCEL", color = Color(0xFFff5555))
                    }
                }
            )
        }
    }
}