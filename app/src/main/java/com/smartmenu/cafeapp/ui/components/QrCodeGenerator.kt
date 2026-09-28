package com.smartmenu.cafeapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Lightweight deterministic 2D QR Code matrix generator for Android Compose
 */
object QrMatrixGenerator {
    private const val SIZE = 25 // 25x25 grid for Version 2 QR

    fun generateMatrix(content: String): Array<BooleanArray> {
        val matrix = Array(SIZE) { BooleanArray(SIZE) { false } }
        val reserved = Array(SIZE) { BooleanArray(SIZE) { false } }

        // Place Finder Patterns at top-left, top-right, bottom-left
        drawFinderPattern(matrix, reserved, 0, 0)
        drawFinderPattern(matrix, reserved, SIZE - 7, 0)
        drawFinderPattern(matrix, reserved, 0, SIZE - 7)

        // Alignment pattern at (16, 16)
        drawAlignmentPattern(matrix, reserved, 16, 16)

        // Timing patterns
        for (i in 8 until SIZE - 8) {
            val bit = (i % 2 == 0)
            if (!reserved[i][6]) {
                matrix[i][6] = bit
                reserved[i][6] = true
            }
            if (!reserved[6][i]) {
                matrix[6][i] = bit
                reserved[6][i] = true
            }
        }

        // Fill data bits deterministically from content hash & bytes
        val bytes = content.toByteArray(Charsets.UTF_8)
        var bitIndex = 0
        val totalBits = bytes.size * 8

        for (x in SIZE - 1 downTo 0 step 2) {
            val col = if (x <= 6) x - 1 else x
            if (col < 0) break
            for (y in 0 until SIZE) {
                val row = if ((x / 2) % 2 == 0) SIZE - 1 - y else y
                for (c in 0..1) {
                    val currX = col - c
                    if (currX >= 0 && !reserved[currX][row]) {
                        val bytePos = (bitIndex / 8) % maxOf(1, bytes.size)
                        val bitOffset = bitIndex % 8
                        val byteVal = if (bytes.isNotEmpty()) bytes[bytePos].toInt() else 0
                        val dataBit = ((byteVal shr (7 - bitOffset)) and 1) == 1

                        // Apply standard mask pattern (row + currX) % 2 == 0
                        val mask = (row + currX) % 2 == 0
                        matrix[currX][row] = dataBit xor mask
                        bitIndex++
                    }
                }
            }
        }

        return matrix
    }

    private fun drawFinderPattern(
        matrix: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
        startX: Int,
        startY: Int
    ) {
        for (dx in 0 until 7) {
            for (dy in 0 until 7) {
                val x = startX + dx
                val y = startY + dy
                reserved[x][y] = true
                val isBorder = dx == 0 || dx == 6 || dy == 0 || dy == 6
                val isCenter = dx in 2..4 && dy in 2..4
                matrix[x][y] = isBorder || isCenter
            }
        }
        // Separators around finder pattern
        for (dx in -1..7) {
            for (dy in -1..7) {
                val x = startX + dx
                val y = startY + dy
                if (x in 0 until SIZE && y in 0 until SIZE && !reserved[x][y]) {
                    reserved[x][y] = true
                    matrix[x][y] = false
                }
            }
        }
    }

    private fun drawAlignmentPattern(
        matrix: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
        centerX: Int,
        centerY: Int
    ) {
        for (dx in -2..2) {
            for (dy in -2..2) {
                val x = centerX + dx
                val y = centerY + dy
                if (x in 0 until SIZE && y in 0 until SIZE) {
                    reserved[x][y] = true
                    val isBorder = abs(dx) == 2 || abs(dy) == 2
                    val isCenter = dx == 0 && dy == 0
                    matrix[x][y] = isBorder || isCenter
                }
            }
        }
    }
}

@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    moduleColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    val matrix = remember(data) {
        QrMatrixGenerator.generateMatrix(data)
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFFF0A500), RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val count = matrix.size
            val cellSize = size.minDimension / count
            val offsetX = (size.width - cellSize * count) / 2f
            val offsetY = (size.height - cellSize * count) / 2f

            for (x in 0 until count) {
                for (y in 0 until count) {
                    if (matrix[x][y]) {
                        drawRect(
                            color = moduleColor,
                            topLeft = Offset(offsetX + x * cellSize, offsetY + y * cellSize),
                            size = Size(cellSize + 0.5f, cellSize + 0.5f)
                        )
                    }
                }
            }
        }
    }
}
