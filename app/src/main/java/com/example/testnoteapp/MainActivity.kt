package com.example.testnoteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testnoteapp.ui.theme.TestnoteappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = NoteDatabase.getDatabase(applicationContext)
        val repo = NoteRepository(db.noteDao())
        val vm: NotesViewModel by viewModels { NotesViewModelFactory(repo) }
        
        setContent {
            TestnoteappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF08080A)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Subtle liquid-like background blobs
                        Box(
                            modifier = Modifier
                                .offset(x = (-100).dp, y = (-100).dp)
                                .size(400.dp)
                                .background(Brush.radialGradient(listOf(Color(0xFF0A84FF).copy(0.15f), Color.Transparent)))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 100.dp, y = 100.dp)
                                .size(500.dp)
                                .background(Brush.radialGradient(listOf(Color(0xFFBF5AF2).copy(0.1f), Color.Transparent)))
                        )
                        
                        NotesScreen(vm)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(vm: NotesViewModel) {
    val notes by vm.notes.collectAsState()
    val search by vm.searchQuery.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<NoteEntity?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    noteToEdit = null
                    showSheet = true 
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp),
                containerColor = Color(0xFF0A84FF),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        }
    ) { p ->
        Column(modifier = Modifier.padding(p).fillMaxSize()) {
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Quick Notes",
                        fontSize = 34.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        "${notes.size} total notes", 
                        fontSize = 15.sp, 
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sleek iOS-style Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(40.dp)
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .clickable { /* Handle focus */ }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = search,
                        onValueChange = { vm.onSearchQueryChanged(it) },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 17.sp),
                        cursorBrush = Brush.verticalGradient(listOf(Color(0xFF0A84FF), Color(0xFF0A84FF))),
                        decorationBox = { innerTextField ->
                            if (search.isEmpty()) {
                                Text("Search", color = Color.Gray, fontSize = 17.sp)
                            }
                            innerTextField()
                        }
                    )
                    if (search.isNotEmpty()) {
                        Icon(
                            Icons.Rounded.Cancel, 
                            contentDescription = "Clear", 
                            tint = Color.Gray, 
                            modifier = Modifier.size(18.dp).clickable { vm.onSearchQueryChanged("") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.NoteAdd, 
                            contentDescription = null, 
                            tint = Color(0x33FFFFFF), 
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Your collection is empty", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteItem(
                            note = note,
                            onClick = {
                                noteToEdit = note
                                showSheet = true
                            },
                            onDelete = {
                                showDeleteDialog = note
                            }
                        )
                    }
                }
            }
        }

        if (showSheet) {
            AddEditSheet(
                note = noteToEdit,
                sheetState = sheetState,
                onDismiss = { showSheet = false },
                onSave = { t, c, clr ->
                    if (noteToEdit == null) vm.addNote(t, c, clr)
                    else vm.updateNote(noteToEdit!!.copy(title = t, content = c, color = clr))
                    showSheet = false
                }
            )
        }

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                containerColor = Color(0xFF1C1C1E),
                shape = RoundedCornerShape(14.dp),
                title = { 
                    Text(
                        "Delete Note?", 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 17.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = { 
                    Text(
                        "This note will be permanently removed.", 
                        color = Color.White, 
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.deleteNote(showDeleteDialog!!)
                            showDeleteDialog = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = Color(0xFF0A84FF), fontSize = 17.sp)
                    }
                }
            )
        }
    }
}

@Composable
fun NoteItem(note: NoteEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f)
    val cardColor = Color(android.graphics.Color.parseColor(note.color))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1AFFFFFF)) // Glassy translucent background
            .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { 
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Color indicator pill
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .clip(CircleShape)
                    .background(cardColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        note.title, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp, 
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "#${note.id}", 
                        fontSize = 12.sp, 
                        color = Color.Gray,
                        fontWeight = FontWeight.Light
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    note.content, 
                    fontSize = 14.sp, 
                    color = Color(0xFFEBEBF5).copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    note.createdAt, 
                    fontSize = 12.sp, 
                    color = Color.Gray,
                    fontWeight = FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color(0xFF0A84FF), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFFF453A), modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSheet(
    note: NoteEntity?, 
    sheetState: SheetState,
    onDismiss: () -> Unit, 
    onSave: (String, String, String) -> Unit
) {
    var t by remember { mutableStateOf(note?.title ?: "") }
    var c by remember { mutableStateOf(note?.content ?: "") }
    val colors = listOf("#0A84FF", "#30D158", "#FF9F0A", "#64D2FF", "#BF5AF2", "#FF375F")
    var selClr by remember { mutableStateOf(note?.color ?: colors[0]) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF0A84FF), fontSize = 17.sp) }
                Text(if (note == null) "New Note" else "Edit Note", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                TextButton(onClick = { if (t.isNotBlank()) onSave(t, c, selClr) }) { 
                    Text("Done", color = Color(0xFF0A84FF), fontSize = 17.sp, fontWeight = FontWeight.Bold) 
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // High Quality Glassy Input Group
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1AFFFFFF)) // Glassy effect
                    .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
            ) {
                // Title Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    BasicTextField(
                        value = t,
                        onValueChange = { t = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 22.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF0A84FF)),
                        decorationBox = { innerTextField ->
                            if (t.isEmpty()) Text("Note Title", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0x33FFFFFF))
                            innerTextField()
                        }
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color(0x1AFFFFFF),
                    thickness = 0.5.dp
                )
                
                // Content Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .heightIn(min = 200.dp)
                ) {
                    BasicTextField(
                        value = c,
                        onValueChange = { c = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 17.sp, 
                            color = Color(0xFFEBEBF5).copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF0A84FF)),
                        decorationBox = { innerTextField ->
                            if (c.isEmpty()) Text("Write your thoughts...", fontSize = 17.sp, color = Color(0x33FFFFFF))
                            innerTextField()
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("TAG COLOR", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 4.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1AFFFFFF))
                    .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                colors.forEach { colorStr ->
                    val isSelected = selClr == colorStr
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(colorStr)))
                            .border(if (isSelected) 3.dp else 0.dp, Color.White, CircleShape)
                            .clickable { selClr = colorStr }
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
