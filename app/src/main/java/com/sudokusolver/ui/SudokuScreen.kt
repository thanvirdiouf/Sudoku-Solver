package com.sudokusolver.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudokusolver.model.Cell
import com.sudokusolver.ui.theme.Blue700
import com.sudokusolver.util.SudokuImageExporter
import com.sudokusolver.viewmodel.SudokuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuScreen(
    viewModel: SudokuViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Permission launcher for API < 29
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            SudokuImageExporter.saveToGallery(context, state.cells)
        } else {
            Toast.makeText(context, "Storage permission needed to save images", Toast.LENGTH_SHORT).show()
        }
    }

    // Show error dialog
    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sudoku Solver",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue700,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Solved badge
            if (state.isSolved) {
                Text(
                    text = "✓ Solved!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // The 9×9 grid
            SudokuGrid(
                cells = state.cells,
                selectedRow = state.selectedRow,
                selectedCol = state.selectedCol,
                onCellClick = { row, col -> viewModel.selectCell(row, col) }
            )

            // Number pad
            NumberPad(
                cells = state.cells,
                onDigitClick = { viewModel.placeDigit(it) },
                onEraseClick = { viewModel.eraseSelected() }
            )

            // Status panel
            StatusPanel(cells = state.cells, isSolved = state.isSolved)

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = { viewModel.solve() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "SOLVE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.reset() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "RESET",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Save PNG button (only after solving)
            if (state.isSolved) {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // MediaStore — no permission needed
                            SudokuImageExporter.saveToGallery(context, state.cells)
                        } else {
                            // Check/request permission
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                SudokuImageExporter.saveToGallery(context, state.cells)
                            } else {
                                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "SAVE AS PNG",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatusPanel(cells: List<List<Cell>>, isSolved: Boolean) {
    val emptyCount = cells.sumOf { row -> row.count { it.isEmpty } }
    val filledCount = 81 - emptyCount
    val hasConflicts = !isSolved && hasBoardConflicts(cells)

    val difficulty = when {
        filledCount == 0 -> null
        filledCount < 19 -> "Expert"
        filledCount < 27 -> "Hard"
        filledCount < 36 -> "Medium"
        else -> "Easy"
    }

    val borderColor = if (hasConflicts) Color(0xFFEF9A9A) else Color(0xFFA5D6A7)
    val bgColor = if (hasConflicts) Color(0xFFFFF3F0) else Color(0xFFF1F8E9)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: cells remaining + difficulty
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$filledCount filled · $emptyCount empty",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
            if (difficulty != null) {
                Text(
                    text = difficulty,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (difficulty) {
                        "Easy" -> Color(0xFF2E7D32)
                        "Medium" -> Color(0xFFF57F17)
                        "Hard" -> Color(0xFFE65100)
                        "Expert" -> Color(0xFFB71C1C)
                        else -> Color.Gray
                    }
                )
            }
        }

        // Row 2: conflict status
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (filledCount == 0) "— Start entering a puzzle"
                else if (isSolved) "✓ Puzzle solved"
                else if (hasConflicts) "⚠ Conflicts detected"
                else "✓ No conflicts detected",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (hasConflicts) Color(0xFFC62828)
                    else if (isSolved || filledCount > 0) Color(0xFF2E7D32)
                    else Color(0xFF757575)
            )
        }
    }
}

private fun hasBoardConflicts(cells: List<List<Cell>>): Boolean {
    // Check rows
    for (row in 0 until 9) {
        val seen = mutableSetOf<Int>()
        for (col in 0 until 9) {
            val v = cells[row][col].value
            if (v != 0 && !seen.add(v)) return true
        }
    }
    // Check columns
    for (col in 0 until 9) {
        val seen = mutableSetOf<Int>()
        for (row in 0 until 9) {
            val v = cells[row][col].value
            if (v != 0 && !seen.add(v)) return true
        }
    }
    // Check 3×3 boxes
    for (boxRow in 0 until 3) {
        for (boxCol in 0 until 3) {
            val seen = mutableSetOf<Int>()
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    val v = cells[boxRow * 3 + r][boxCol * 3 + c].value
                    if (v != 0 && !seen.add(v)) return true
                }
            }
        }
    }
    return false
}
