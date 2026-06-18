# Sudoku Solver

An Android app that solves Sudoku puzzles. Enter a puzzle manually, tap **Solve**, and get the solution instantly. You can also save the solved board as a PNG image.

## Features

- **9×9 interactive grid** — tap a cell, then tap a number (1–9) to fill it
- **Instant solving** — backtracking algorithm with MRV heuristic solves puzzles in microseconds
- **Smart input** — selected cell highlights in blue; cells with the same digit highlight in light blue
- **Visual distinction** — user-entered digits appear in bold blue; solver-filled digits in regular dark gray
- **Input validation** — detects invalid puzzles (duplicate digits in row/column/box) and unsolvable puzzles
- **Save as PNG** — after solving, export the board as a 1080×1080 image to your Pictures folder
- **Reset** — clears the entire board with one tap

## Screenshots

```
┌──────────────────────────────┐
│       SUDOKU SOLVER          │
├──────────────────────────────┤
│   ┌───┬───┬───┬───┬───┬───┐  │
│   │ 5 │ 3 │   │   │ 7 │   │  │
│   ├───┼───┼───┼───┼───┼───┤  │
│   │ 6 │   │   │ 1 │ 9 │ 5 │  │
│   ├───┼───┼───┼───┼───┼───┤  │
│   │   │ 9 │ 8 │   │   │   │  │
│   ├───┼───┼───┼───┼───┼───┤  │
│   │ 8 │   │   │   │ 6 │   │  │
│   ├───┼───┼───┼───┼───┼───┤  │
│   │ 4 │   │   │ 8 │   │ 3 │  │
│   ├───┼───┼───┼───┼───┼───┤  │
│   │ 7 │   │   │   │ 2 │   │  │
│   └───┴───┴───┴───┴───┴───┘  │
├──────────────────────────────┤
│   1   2   3   4   5           │
│   6   7   8   9   ⌫           │
├──────────────────────────────┤
│  [ SOLVE ]    [ RESET ]      │
│  [       SAVE AS PNG       ] │
└──────────────────────────────┘
```

## Build

### Prerequisites

- **Android SDK** (API 34) — install via Android Studio or [command-line tools](https://developer.android.com/studio#command-line-tools-only)
- **Java 17+** (OpenJDK 21 recommended)

### Build from command line

```bash
# Set Android SDK location
export ANDROID_HOME=/path/to/Android/Sdk

# Build debug APK
./gradlew assembleDebug

# APK output at: app/build/outputs/apk/debug/app-debug.apk
```

### Install on device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Open in Android Studio

Open this project folder in Android Studio. It will handle SDK setup and Gradle sync automatically.

## Tech Stack

| Component | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Build | Gradle 8.5 (Kotlin DSL) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |

## Project Structure

```
app/src/main/java/com/sudokusolver/
├── MainActivity.kt              # Single activity, edge-to-edge display
├── model/
│   └── SudokuState.kt           # Cell & SudokuState data classes
├── solver/
│   └── SudokuSolver.kt          # Backtracking solver (pure Kotlin, no Android deps)
├── viewmodel/
│   └── SudokuViewModel.kt       # State management, user actions, validation
├── ui/
│   ├── SudokuScreen.kt          # Top-level Scaffold + solve/reset/save buttons
│   ├── SudokuGrid.kt            # 9×9 interactive grid composable
│   ├── NumberPad.kt             # 1–9 circular buttons + erase
│   └── theme/
│       ├── Color.kt             # App color palette
│       ├── Theme.kt             # Light/dark Material3 theme
│       └── Type.kt              # Typography definitions
└── util/
    └── SudokuImageExporter.kt   # Renders board to PNG bitmap, saves via MediaStore
```

## Architecture

```
User tap → SudokuGrid (Composable)
         → SudokuViewModel.selectCell/placeDigit/eraseSelected
         → StateFlow<SudokuState> update
         → Compose recomposition

Solve tap → SudokuViewModel.solve()
         → SudokuState.toIntArray()
         → SudokuSolver.solve()        ← backtracking with MRV heuristic
         → SudokuState.withSolvedGrid()
         → Compose recomposition

Save tap → SudokuImageExporter.saveToGallery()
         → Canvas-backed Bitmap render
         → MediaStore insert / save to Pictures
```

### Solver Algorithm

The solver in `SudokuSolver.kt` uses **backtracking with the Minimum Remaining Values (MRV) heuristic**:

1. Find the empty cell with the fewest valid candidates
2. Try each valid digit (1–9)
3. Recursively solve the rest of the board
4. Backtrack on dead ends

The initial board is validated for conflicts before solving. Typical puzzles solve in **microseconds**.

## License

This project is free to use and modify.
