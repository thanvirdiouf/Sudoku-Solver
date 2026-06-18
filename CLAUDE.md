# CLAUDE.md — Sudoku Solver

## Project overview

Android app for solving Sudoku puzzles. User enters a puzzle on a 9×9 grid, taps Solve, and the backtracking solver fills in the solution. Solved boards can be exported as PNG images.

- **Package:** `com.sudokusolver`
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM (ViewModel + StateFlow)

## Build commands

```bash
# Build
export ANDROID_HOME=/home/spectre/Android
./gradlew assembleDebug

# Install
adb install app/build/outputs/apk/debug/app-debug.apk

# Clean rebuild
./gradlew clean assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Key files and their roles

### Model layer
- `app/src/main/java/com/sudokusolver/model/SudokuState.kt`
  - `Cell(value: Int, isUserEntered: Boolean)` — 0 = empty, 1–9 = filled
  - `SudokuState(cells, selectedRow, selectedCol, isSolved, errorMessage)`
  - Extension functions: `toIntArray()`, `withSolvedGrid(solved)`

### Solver (pure Kotlin, zero Android dependencies)
- `app/src/main/java/com/sudokusolver/solver/SudokuSolver.kt`
  - Singleton `object SudokuSolver`
  - `solve(board: Array<IntArray>): Boolean` — mutates board in place, returns success
  - `isValid(board, row, col, digit): Boolean` — public, used for validation too
  - Uses MRV heuristic (minimum remaining values) to prune the search tree
  - Validates input board for conflicts before attempting solve

### ViewModel
- `app/src/main/java/com/sudokusolver/viewmodel/SudokuViewModel.kt`
  - `state: StateFlow<SudokuState>` — single source of truth
  - Actions: `selectCell()`, `placeDigit()`, `eraseSelected()`, `solve()`, `reset()`, `clearError()`
  - `solve()` validates the board for row/column/box conflicts before invoking the solver
  - User-entered cells preserve their `isUserEntered = true` flag after solving

### UI layer (Compose)
- `app/src/main/java/com/sudokusolver/ui/SudokuScreen.kt`
  - Top-level: Scaffold + TopAppBar + error dialog + solve/reset/save buttons
  - Handles WRITE_EXTERNAL_STORAGE permission request for API < 29
  - Save button is green, only visible when `isSolved == true`

- `app/src/main/java/com/sudokusolver/ui/SudokuGrid.kt`
  - Renders 9×9 grid of `SudokuCell` composables
  - Selected cell → blue background; same-value cells → light blue background
  - All borders are uniform 1.5dp; outer border wraps the entire grid

- `app/src/main/java/com/sudokusolver/ui/NumberPad.kt`
  - Two rows: 1–5 and 6–9 + erase (backspace icon)
  - Buttons are 68dp circles, 28sp bold digits

- `app/src/main/java/com/sudokusolver/ui/theme/`
  - `Color.kt` — all app colors (user/solver digit colors, selection highlights, grid lines)
  - `Theme.kt` — light & dark color schemes
  - `Type.kt` — Material 3 typography

### Utilities
- `app/src/main/java/com/sudokusolver/util/SudokuImageExporter.kt`
  - `saveToGallery(context, cells): Boolean` — renders grid to bitmap, saves to Pictures
  - Renders at 1080×1080 with crisp grid lines and thick box borders (3px/8px)
  - Uses MediaStore on API 29+, direct file + MediaScanner on API 24–28
  - Shows Toast on success/failure

## Dependencies

```kotlin
// Compose BOM 2024.02.00
androidx.compose.ui:ui
androidx.compose.material3:material3
androidx.compose.material:material-icons-extended

// Architecture
androidx.activity:activity-compose:1.8.2
androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0
androidx.lifecycle:lifecycle-runtime-compose:2.7.0
androidx.core:core-ktx:1.12.0
```

## State flow

```
SudokuViewModel.state (StateFlow<SudokuState>)
  │
  ├── selectCell(row, col)   → toggles selectedRow/selectedCol
  ├── placeDigit(digit)      → sets value in selected cell (userEntered=true)
  ├── eraseSelected()        → clears selected cell
  ├── solve()                → validates → calls SudokuSolver.solve() → withSolvedGrid()
  ├── reset()                → replaces state with SudokuState.empty()
  └── clearError()           → sets errorMessage = null
```

## Solver design notes

- The solver mutates the board array in place — `solve()` in ViewModel passes a `.copyOf()` to avoid corrupting the displayed board on failure
- MRV heuristic picks the cell with fewest valid candidates first, which dramatically reduces the search space
- `countCandidates()` returns 0 for dead-end cells, triggering immediate backtrack
- The initial validation pass (`isBoardValid`) temporarily clears each cell to check it against `isValid()`, avoiding false positives from the cell matching itself

## Image export design notes

- Images are rendered programmatically to a Canvas-backed Bitmap (not screenshotted from Compose)
- Grid: thin lines (3px) inside boxes, thick lines (8px) at box boundaries
- Digits: bold typeface for user-entered, regular for solver-entered
- API 29+ uses `MediaStore.Images` with `IS_PENDING` flag (atomic writes)
- API 24–28 uses `Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)` + `MediaScannerConnection`

## Permissions

- `WRITE_EXTERNAL_STORAGE` — `maxSdkVersion="28"`, only requested on API < 29
- Runtime permission request handled via `rememberLauncherForActivityResult` in SudokuScreen

## Warnings to maintain

- The build should produce **zero warnings** — the deprecation of `Icons.Default.Backspace` was fixed by switching to `Icons.AutoMirrored.Filled.Backspace`
- The deprecated `ACTION_MEDIA_SCANNER_SCAN_FILE` intent was replaced with `MediaScannerConnection.scanFile()`
