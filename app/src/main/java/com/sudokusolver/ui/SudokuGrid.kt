package com.sudokusolver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudokusolver.model.Cell
import com.sudokusolver.ui.theme.CellBackground
import com.sudokusolver.ui.theme.CellBackgroundSameValue
import com.sudokusolver.ui.theme.CellBackgroundSelected
import com.sudokusolver.ui.theme.GridLine
import com.sudokusolver.ui.theme.SolverDigitColor
import com.sudokusolver.ui.theme.UserDigitColor

@Composable
fun SudokuGrid(
    cells: List<List<Cell>>,
    selectedRow: Int?,
    selectedCol: Int?,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, GridLine)
    ) {
        for (row in 0 until 9) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 9) {
                    val cell = cells[row][col]
                    val isSelected = selectedRow == row && selectedCol == col
                    val selectedValue = if (selectedRow != null && selectedCol != null) {
                        cells[selectedRow][selectedCol].value
                    } else null

                    val isSameValue = selectedValue != null &&
                            selectedValue != 0 &&
                            cell.value == selectedValue &&
                            !isSelected

                    SudokuCell(
                        cell = cell,
                        isSelected = isSelected,
                        isSameValue = isSameValue,
                        onClick = { onCellClick(row, col) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SudokuCell(
    cell: Cell,
    isSelected: Boolean,
    isSameValue: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> CellBackgroundSelected
        isSameValue -> CellBackgroundSameValue
        else -> CellBackground
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .background(backgroundColor)
            .border(1.5.dp, GridLine)
            .clickable { onClick() }
    ) {
        if (cell.isFilled) {
            Text(
                text = cell.value.toString(),
                fontSize = if (isSelected) 22.sp else 20.sp,
                fontWeight = if (cell.isUserEntered) FontWeight.Bold else FontWeight.Normal,
                color = if (cell.isUserEntered) UserDigitColor else SolverDigitColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
