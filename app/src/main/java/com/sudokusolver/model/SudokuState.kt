package com.sudokusolver.model

/**
 * Represents a single cell in the Sudoku grid.
 *
 * @param value The digit in the cell (0 = empty, 1–9 = filled)
 * @param isUserEntered True if the user entered this digit, false if the solver did
 */
data class Cell(
    val value: Int = 0,
    val isUserEntered: Boolean = false
) {
    init {
        require(value in 0..9) { "Cell value must be 0–9, got $value" }
    }

    val isEmpty: Boolean get() = value == 0
    val isFilled: Boolean get() = value != 0
}

/**
 * Represents the entire Sudoku board state.
 *
 * @param cells 9×9 grid of Cells
 * @param selectedRow Currently selected row (null if no selection)
 * @param selectedCol Currently selected column (null if no selection)
 * @param isSolved Whether the board has been solved by the solver
 * @param errorMessage Error to display to the user, or null
 */
data class SudokuState(
    val cells: List<List<Cell>> = List(9) { List(9) { Cell() } },
    val selectedRow: Int? = null,
    val selectedCol: Int? = null,
    val isSolved: Boolean = false,
    val errorMessage: String? = null
) {
    init {
        require(cells.size == 9) { "Grid must have 9 rows" }
        require(cells.all { it.size == 9 }) { "Each row must have 9 columns" }
    }

    companion object {
        fun empty(): SudokuState = SudokuState()
    }
}

/**
 * Converts the SudokuState cells to a simple 2D Int array for the solver.
 */
fun SudokuState.toIntArray(): Array<IntArray> {
    return Array(9) { row ->
        IntArray(9) { col ->
            cells[row][col].value
        }
    }
}

/**
 * Creates a copy of this SudokuState with values from a solved Int array.
 * User-entered cells that were non-zero remain marked as user-entered;
 * newly filled cells are marked as solver-entered.
 */
fun SudokuState.withSolvedGrid(solved: Array<IntArray>): SudokuState {
    val originalValues = toIntArray()
    return copy(
        cells = List(9) { row ->
            List(9) { col ->
                Cell(
                    value = solved[row][col],
                    isUserEntered = originalValues[row][col] != 0
                )
            }
        },
        isSolved = true,
        errorMessage = null,
        selectedRow = null,
        selectedCol = null
    )
}
