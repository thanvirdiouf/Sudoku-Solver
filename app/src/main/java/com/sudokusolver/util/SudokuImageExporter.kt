package com.sudokusolver.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.sudokusolver.model.Cell
import java.io.File
import java.io.FileOutputStream

/**
 * Renders the Sudoku grid to a PNG image and saves it to the device.
 */
object SudokuImageExporter {

    private const val IMAGE_SIZE = 1080
    private const val GRID_SIZE = 9
    private const val CELL_SIZE = IMAGE_SIZE / GRID_SIZE // 120px

    // Colors (ARGB)
    private const val COLOR_BG = 0xFFFFFFFF.toInt()
    private const val COLOR_GRID_LINE = 0xFF333333.toInt()
    private const val COLOR_USER_DIGIT = 0xFF1565C0.toInt()
    private const val COLOR_SOLVER_DIGIT = 0xFF212121.toInt()
    private const val COLOR_BOX_BORDER = 0xFF000000.toInt()

    /**
     * Save the current board as a PNG image. Returns true on success.
     */
    fun saveToGallery(context: Context, cells: List<List<Cell>>): Boolean {
        val bitmap = renderGrid(cells)
        return try {
            val uri = saveBitmap(context, bitmap)
            bitmap.recycle()
            if (uri != null) {
                Toast.makeText(context, "Saved to Pictures", Toast.LENGTH_SHORT).show()
                true
            } else {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            bitmap.recycle()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun renderGrid(cells: List<List<Cell>>): Bitmap {
        val bitmap = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(COLOR_BG)

        // Draw cells
        val cellPaint = Paint().apply {
            color = COLOR_BG
            style = Paint.Style.FILL
        }

        // Cell backgrounds (we just fill everything white, no highlights in export)
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val x = col * CELL_SIZE
                val y = row * CELL_SIZE
                canvas.drawRect(
                    x.toFloat(), y.toFloat(),
                    (x + CELL_SIZE).toFloat(), (y + CELL_SIZE).toFloat(),
                    cellPaint
                )
            }
        }

        // Draw digits
        val userTextPaint = Paint().apply {
            color = COLOR_USER_DIGIT
            textSize = CELL_SIZE * 0.55f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
        val solverTextPaint = Paint().apply {
            color = COLOR_SOLVER_DIGIT
            textSize = CELL_SIZE * 0.55f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val cell = cells[row][col]
                if (cell.isFilled) {
                    val paint = if (cell.isUserEntered) userTextPaint else solverTextPaint
                    val cx = col * CELL_SIZE + CELL_SIZE / 2f
                    val cy = row * CELL_SIZE + CELL_SIZE / 2f - (paint.descent() + paint.ascent()) / 2f
                    canvas.drawText(cell.value.toString(), cx, cy, paint)
                }
            }
        }

        // Draw grid lines (thin inner lines first, then thick box borders)
        val thinLine = Paint().apply {
            color = COLOR_GRID_LINE
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val thickLine = Paint().apply {
            color = COLOR_BOX_BORDER
            strokeWidth = 8f
            style = Paint.Style.STROKE
        }

        // Horizontal lines
        for (row in 0..GRID_SIZE) {
            val paint = if (row % 3 == 0) thickLine else thinLine
            val y = row * CELL_SIZE.toFloat()
            canvas.drawLine(0f, y, IMAGE_SIZE.toFloat(), y, paint)
        }

        // Vertical lines
        for (col in 0..GRID_SIZE) {
            val paint = if (col % 3 == 0) thickLine else thinLine
            val x = col * CELL_SIZE.toFloat()
            canvas.drawLine(x, 0f, x, IMAGE_SIZE.toFloat(), paint)
        }

        return bitmap
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap): Uri? {
        val filename = "Sudoku_${System.currentTimeMillis()}.png"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // MediaStore (API 29+)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(it, values, null, null)
            }
            uri
        } else {
            // Direct file (API 24-28)
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(dir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            // Notify media scanner
            MediaScannerConnection.scanFile(
                context, arrayOf(file.absolutePath), null, null
            )
            Uri.fromFile(file)
        }
    }
}
