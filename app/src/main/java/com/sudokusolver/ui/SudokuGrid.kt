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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudokusolver.model.Cell
import com.sudokusolver.ui.theme.CellBackground
import com.sudokusolver.ui.theme.CellBackgroundSameValue
import com.sudokusolver.ui.theme.CellBackgroundSelected
import com.sudokusolver.ui.theme.GridLine

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
            .border(0.5.dp, GridLine)
            .drawWithContent {
                drawContent()
                // Draw thick box boundary lines on top
                val boxLineColor = Color(0xFF1976D2)
                val boxLineWidth = 4.dp.toPx()
                val w = size.width
                val h = size.height
                val cx1 = w / 3f
                val cx2 = w * 2f / 3f
                val cy1 = h / 3f
                val cy2 = h * 2f / 3f
                // Vertical box lines (between cols 3-4 and 6-7)
                drawLine(boxLineColor, Offset(cx1, 0f), Offset(cx1, h), boxLineWidth)
                drawLine(boxLineColor, Offset(cx2, 0f), Offset(cx2, h), boxLineWidth)
                // Horizontal box lines (between rows 3-4 and 6-7)
                drawLine(boxLineColor, Offset(0f, cy1), Offset(w, cy1), boxLineWidth)
                drawLine(boxLineColor, Offset(0f, cy2), Offset(w, cy2), boxLineWidth)
                // Outer border drawn on top
                drawLine(boxLineColor, Offset(0f, 0f), Offset(w, 0f), boxLineWidth)
                drawLine(boxLineColor, Offset(0f, h), Offset(w, h), boxLineWidth)
                drawLine(boxLineColor, Offset(0f, 0f), Offset(0f, h), boxLineWidth)
                drawLine(boxLineColor, Offset(w, 0f), Offset(w, h), boxLineWidth)
            }
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
            .border(0.5.dp, GridLine)
            .clickable { onClick() }
    ) {
        if (cell.isFilled) {
            Text(
                text = cell.value.toString(),
                fontSize = if (isSelected) 22.sp else 20.sp,
                fontWeight = if (cell.isUserEntered) FontWeight.Bold else FontWeight.Normal,
                color = if (cell.isUserEntered) com.sudokusolver.ui.theme.UserDigitColor else com.sudokusolver.ui.theme.SolverDigitColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
