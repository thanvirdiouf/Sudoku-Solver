package com.sudokusolver.solver

/**
 * A backtracking Sudoku solver.
 *
 * Solves standard 9×9 Sudoku puzzles. The algorithm:
 * 1. Finds the next empty cell (using the minimum-remaining-values heuristic
 *    for efficiency — picks the cell with the fewest valid candidates).
 * 2. Tries each valid digit 1–9.
 * 3. Recursively attempts to solve the rest of the board.
 * 4. Backtracks if a dead end is reached.
 *
 * Worst-case complexity is O(9^n) for n empty cells, but with constraint
 * propagation, typical puzzles solve in microseconds.
 */
object SudokuSolver {

    private const val SIZE = 9
    private const val BOX_SIZE = 3
    private const val EMPTY = 0

    /**
     * Attempts to solve the given Sudoku board in-place.
     *
     * @param board A 9×9 grid where 0 represents an empty cell.
     * @return true if a solution was found, false if the puzzle is unsolvable.
     */
    fun solve(board: Array<IntArray>): Boolean {
        validateBoardShape(board)

        // First, verify the initial board has no conflicts
        if (!isBoardValid(board)) {
            return false
        }

        return solveBacktrack(board)
    }

    /**
     * Checks whether placing [digit] at [row],[col] is valid according to
     * Sudoku rules (no duplicate in row, column, or 3×3 box).
     */
    fun isValid(board: Array<IntArray>, row: Int, col: Int, digit: Int): Boolean {
        // Check row
        for (c in 0 until SIZE) {
            if (board[row][c] == digit) return false
        }

        // Check column
        for (r in 0 until SIZE) {
            if (board[r][col] == digit) return false
        }

        // Check 3×3 box
        val boxStartRow = (row / BOX_SIZE) * BOX_SIZE
        val boxStartCol = (col / BOX_SIZE) * BOX_SIZE
        for (r in boxStartRow until boxStartRow + BOX_SIZE) {
            for (c in boxStartCol until boxStartCol + BOX_SIZE) {
                if (board[r][c] == digit) return false
            }
        }

        return true
    }

    // --- private implementation ---

    private fun validateBoardShape(board: Array<IntArray>) {
        require(board.size == SIZE) { "Board must have 9 rows, got ${board.size}" }
        for ((i, row) in board.withIndex()) {
            require(row.size == SIZE) { "Row $i must have 9 columns, got ${row.size}" }
        }
    }

    /**
     * Checks the initial board for obvious conflicts (duplicate digits
     * in any row, column, or box).
     */
    private fun isBoardValid(board: Array<IntArray>): Boolean {
        for (row in 0 until SIZE) {
            for (col in 0 until SIZE) {
                val digit = board[row][col]
                if (digit == EMPTY) continue
                // Temporarily clear the cell so isValid doesn't see a
                // false positive from the cell itself
                board[row][col] = EMPTY
                val valid = isValid(board, row, col, digit)
                board[row][col] = digit
                if (!valid) return false
            }
        }
        return true
    }

    /**
     * Recursive backtracking solver. Finds the most constrained empty cell
     * (fewest valid candidates) and tries each candidate.
     */
    private fun solveBacktrack(board: Array<IntArray>): Boolean {
        // Find the empty cell with the fewest candidates (MRV heuristic)
        var bestRow = -1
        var bestCol = -1
        var bestCandidates = SIZE + 1

        for (row in 0 until SIZE) {
            for (col in 0 until SIZE) {
                if (board[row][col] != EMPTY) continue

                val candidates = countCandidates(board, row, col)
                if (candidates == 0) return false // dead end
                if (candidates < bestCandidates) {
                    bestCandidates = candidates
                    bestRow = row
                    bestCol = col
                    // Optimization: if only 1 candidate, take it immediately
                    if (candidates == 1) break
                }
            }
        }

        // No empty cell found — board is solved
        if (bestRow == -1) return true

        val row = bestRow
        val col = bestCol

        for (digit in 1..SIZE) {
            if (isValid(board, row, col, digit)) {
                board[row][col] = digit
                if (solveBacktrack(board)) return true
                board[row][col] = EMPTY // backtrack
            }
        }

        return false
    }

    private fun countCandidates(board: Array<IntArray>, row: Int, col: Int): Int {
        var count = 0
        for (digit in 1..SIZE) {
            if (isValid(board, row, col, digit)) count++
        }
        return count
    }
}
