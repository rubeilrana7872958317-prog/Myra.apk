package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.FileEntity
import com.example.data.local.entities.MemoryEntity
import com.example.ui.MyraViewModel
import com.example.ui.theme.MyraAlertRed
import com.example.ui.theme.MyraBorder
import com.example.ui.theme.MyraCardBg
import com.example.ui.theme.MyraCardBgElevated
import com.example.ui.theme.MyraCyberCyan
import com.example.ui.theme.MyraMatrixGreen
import com.example.ui.theme.MyraNeonGreen
import com.example.ui.theme.MyraTextPrimary
import com.example.ui.theme.MyraTextSecondary

@Composable
fun MemoryFilesScreen(
    viewModel: MyraViewModel,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.memories.collectAsState()
    val files by viewModel.files.collectAsState()

    var showAddMemoryDialog by remember { mutableStateOf(false) }
    var showAddFileDialog by remember { mutableStateOf(false) }
    var selectedFileToView by remember { mutableStateOf<FileEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: AI Memory & Context
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = MyraNeonGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🧠 NEURAL MEMORY BANK",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MyraNeonGreen
                        )
                    )
                }

                Button(
                    onClick = { showAddMemoryDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MyraNeonGreen, contentColor = Color(0xFF021B0C)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_memory_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Fact", fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Facts and context MYRA uses to personalize interactions across conversations.",
                style = MaterialTheme.typography.bodySmall.copy(color = MyraTextSecondary, fontSize = 11.sp)
            )
        }

        if (memories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MyraCardBg)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Neural memory is clear. Tell MYRA to 'remember...' or add a fact!", color = MyraTextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            items(memories) { memory ->
                MemoryItemCard(
                    memory = memory,
                    onDelete = { viewModel.deleteMemory(memory.id) }
                )
            }
        }

        // Section 2: Local File Manager & Notes Vault
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = MyraCyberCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📂 FILE VAULT & LOGS",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MyraCyberCyan
                        )
                    )
                }

                Button(
                    onClick = { showAddFileDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MyraCyberCyan, contentColor = Color(0xFF003039)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("create_file_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New File", fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Secure on-device text notes, voice logs, and configuration files.",
                style = MaterialTheme.typography.bodySmall.copy(color = MyraTextSecondary, fontSize = 11.sp)
            )
        }

        if (files.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MyraCardBg)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No files in local vault. Create a new file or log!", color = MyraTextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            items(files) { file ->
                FileItemCard(
                    file = file,
                    onView = { selectedFileToView = file },
                    onDelete = { viewModel.deleteFile(file.id) }
                )
            }
        }
    }

    // Add Memory Fact Dialog
    if (showAddMemoryDialog) {
        var keyInput by remember { mutableStateOf("") }
        var valueInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("USER_PREFERENCE") }

        AlertDialog(
            onDismissRequest = { showAddMemoryDialog = false },
            title = {
                Text("Store Neural Fact", color = MyraNeonGreen, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Fact Key (e.g. USER_NAME)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        label = { Text("Details (e.g. Rubeil Rana)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyInput.isNotBlank() && valueInput.isNotBlank()) {
                            viewModel.saveMemory(keyInput.trim(), valueInput.trim(), categoryInput)
                            showAddMemoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyraNeonGreen, contentColor = Color(0xFF021B0C))
                ) {
                    Text("Save Fact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemoryDialog = false }) {
                    Text("Cancel", color = MyraTextSecondary)
                }
            },
            containerColor = MyraCardBgElevated
        )
    }

    // Add File Dialog
    if (showAddFileDialog) {
        var fileNameInput by remember { mutableStateOf("") }
        var fileContentInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddFileDialog = false },
            title = {
                Text("Create New File", color = MyraCyberCyan, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { fileNameInput = it },
                        label = { Text("File Name (e.g. notes.txt)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = fileContentInput,
                        onValueChange = { fileContentInput = it },
                        label = { Text("File Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fileNameInput.isNotBlank()) {
                            viewModel.saveFile(fileNameInput.trim(), fileContentInput)
                            showAddFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyraCyberCyan, contentColor = Color(0xFF003039))
                ) {
                    Text("Save File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFileDialog = false }) {
                    Text("Cancel", color = MyraTextSecondary)
                }
            },
            containerColor = MyraCardBgElevated
        )
    }

    // File Preview Dialog
    selectedFileToView?.let { file ->
        AlertDialog(
            onDismissRequest = { selectedFileToView = null },
            title = {
                Text(file.fileName, color = MyraCyberCyan, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Size: ${file.sizeBytes} bytes • Type: ${file.fileType}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MyraTextSecondary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MyraCardBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = file.content.ifEmpty { "(Empty file)" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MyraTextPrimary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedFileToView = null }) {
                    Text("Close")
                }
            },
            containerColor = MyraCardBgElevated
        )
    }
}

@Composable
fun MemoryItemCard(
    memory: MemoryEntity,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MyraCardBg),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MyraBorder, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = memory.key,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MyraNeonGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0F2618))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = memory.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MyraMatrixGreen)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = memory.value,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MyraTextPrimary)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Memory", tint = MyraAlertRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun FileItemCard(
    file: FileEntity,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MyraCardBg),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MyraBorder, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = MyraCyberCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = file.fileName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MyraTextPrimary
                        )
                    )
                    Text(
                        text = "${file.sizeBytes} B • ${file.fileType}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MyraTextSecondary, fontSize = 11.sp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onView, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Visibility, contentDescription = "View File", tint = MyraCyberCyan, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete File", tint = MyraAlertRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
