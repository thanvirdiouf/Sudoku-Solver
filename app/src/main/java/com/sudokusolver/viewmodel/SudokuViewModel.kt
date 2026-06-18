package com.sudokusolver.viewmodel

import androidx.lifecycle.ViewModel
import com.sudokusolver.model.Cell
import com.sudokusolver.model.SudokuState
import com.sudokusolver.model.toIntArray
import com.sudokusolver.model.withSolvedGrid
import com.sudokusolver.solver.SudokuSolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SudokuViewModel : ViewModel() {

    private val _state = MutableStateFlow(SudokuState.empty())
    val state: StateFlow<SudokuState> = _state.asStateFlow()

    /**
     * Select a cell on the grid. If already selected, deselects it.
     */
    fun selectCell(row: Int, col: Int) {
        _state.update { current ->
            if (current.selectedRow == row && current.selectedCol == col) {
                current.copy(selectedRow = null, selectedCol = null)
            } else {
                current.copy(
                    selectedRow = row,
                    selectedCol = col,
                    errorMessage = null
                )
            }
        }
    }

    /**
     * Place a digit in the currently selected cell.
     */
    fun placeDigit(digit: Int) {
        val current = _state.value
        val row = current.selectedRow ?: return
        val col = current.selectedCol ?: return

        // Don't overwrite solver-entered cells
        if (current.cells[row][col].isFilled && !current.cells[row][col].isUserEntered) {
            return
        }

        _state.update { s ->
            val newCells = s.cells.mapIndexed { r, rowList ->
                rowList.mapIndexed { c, cell ->
                    if (r == row && c == col) {
                        Cell(value = digit, isUserEntered = true)
                    } else {
                        cell
                    }
                }
            }
            s.copy(
                cells = newCells,
                isSolved = false,
                errorMessage = null
            )
        }
    }

    /**
     * Erase the digit from the currently selected cell.
     */
    fun eraseSelected() {
        val current = _state.value
        val row = current.selectedRow ?: return
        val col = current.selectedCol ?: return

        _state.update { s ->
            val newCells = s.cells.mapIndexed { r, rowList ->
                rowList.mapIndexed { c, _ ->
                    if (r == row && c == col) {
                        Cell(value = 0, isUserEntered = false)
                    } else {
                        s.cells[r][c]
                    }
                }
            }
            s.copy(
                cells = newCells,
                isSolved = false,
                errorMessage = null
            )
        }
    }

    /**
     * Solve the current puzzle. Runs the backtracking solver on a copy
     * of the board and updates the state with the solution.
     */
    fun solve() {
        val current = _state.value

        // Check if the board is completely empty
        val hasAnyEntry = current.cells.any { row -> row.any { it.value != 0 } }
        if (!hasAnyEntry) {
            _state.update { it.copy(errorMessage = "Enter a puzzle first") }
            return
        }

        val board = current.toIntArray()

        // Check for conflicts before solving
        if (!isInputValid(board)) {
            _state.update { it.copy(errorMessage = "Invalid puzzle: check for duplicate digits") }
            return
        }

        val solved = board.map { it.copyOf() }.toTypedArray()

        if (SudokuSolver.solve(solved)) {
            _state.update { it.withSolvedGrid(solved) }
        } else {
            _state.update { it.copy(errorMessage = "No solution exists for this puzzle") }
        }
    }

    /**
     * Clear the entire board.
     */
    fun reset() {
        _state.value = SudokuState.empty()
    }

    /**
     * Dismiss the error message.
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * Check whether the user-entered board is internally consistent.
     */
    private fun isInputValid(board: Array<IntArray>): Boolean {
        // Check rows
        for (row in 0 until 9) {
            val seen = mutableSetOf<Int>()
            for (col in 0 until 9) {
                val v = board[row][col]
                if (v != 0 && !seen.add(v)) return false
            }
        }
        // Check columns
        for (col in 0 until 9) {
            val seen = mutableSetOf<Int>()
            for (row in 0 until 9) {
                val v = board[row][col]
                if (v != 0 && !seen.add(v)) return false
            }
        }
        // Check 3×3 boxes
        for (boxRow in 0 until 3) {
            for (boxCol in 0 until 3) {
                val seen = mutableSetOf<Int>()
                for (r in 0 until 3) {
                    for (c in 0 until 3) {
                        val v = board[boxRow * 3 + r][boxCol * 3 + c]
                        if (v != 0 && !seen.add(v)) return false
                    }
                }
            }
        }
        return true
    }
}
