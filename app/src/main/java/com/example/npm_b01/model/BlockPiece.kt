package com.example.npm_b01.model

import androidx.compose.ui.graphics.Color
import java.util.UUID
import kotlin.random.Random

enum class GameMode(val displayName: String) {
    CLASSIC("Classic Endless"),
    LEVELS("Level Campaign")
}

/**
 * Represents a single block piece in the game.
 * [shape] is a 2D boolean array representing the occupied blocks of the piece.
 * [colorId] is an index pointing to a color palette in the current theme.
 */
data class BlockPiece(
    val id: String = UUID.randomUUID().toString(),
    val shape: List<List<Boolean>>,
    val colorId: Int
) {
    val height: Int get() = shape.size
    val width: Int get() = if (shape.isNotEmpty()) shape[0].size else 0
    val blockCount: Int get() = shape.sumOf { row -> row.count { it } }

    /**
     * Rotates the piece shape 90 degrees clockwise.
     */
    fun rotate(): BlockPiece {
        if (shape.isEmpty() || shape[0].isEmpty()) return this
        val rows = shape.size
        val cols = shape[0].size
        val rotatedShape = List(cols) { c ->
            List(rows) { r ->
                shape[rows - 1 - r][c]
            }
        }
        return copy(shape = rotatedShape)
    }

    companion object {
        private val TEMPLATES = listOf(
            // 1x1 Dot
            listOf(listOf(true)),

            // 1x2, 1x3, 1x4, 1x5 Horizontal Lines
            listOf(listOf(true, true)),
            listOf(listOf(true, true, true)),
            listOf(listOf(true, true, true, true)),

            // 2x1, 3x1, 4x1 Vertical Lines
            listOf(listOf(true), listOf(true)),
            listOf(listOf(true), listOf(true), listOf(true)),
            listOf(listOf(true), listOf(true), listOf(true), listOf(true)),

            // 2x2 Square
            listOf(
                listOf(true, true),
                listOf(true, true)
            ),

            // 3x3 Square
            listOf(
                listOf(true, true, true),
                listOf(true, true, true),
                listOf(true, true, true)
            ),

            // L-Shapes 2x2
            listOf(
                listOf(true, false),
                listOf(true, true)
            ),
            listOf(
                listOf(false, true),
                listOf(true, true)
            ),
            listOf(
                listOf(true, true),
                listOf(true, false)
            ),
            listOf(
                listOf(true, true),
                listOf(false, true)
            ),

            // L-Shapes 3x3
            listOf(
                listOf(true, false, false),
                listOf(true, false, false),
                listOf(true, true, true)
            ),
            listOf(
                listOf(false, false, true),
                listOf(false, false, true),
                listOf(true, true, true)
            ),

            // T-Shapes
            listOf(
                listOf(true, true, true),
                listOf(false, true, false)
            ),
            listOf(
                listOf(false, true, false),
                listOf(true, true, true)
            ),

            // Small 2x2 Corner
            listOf(
                listOf(true, true),
                listOf(true, false)
            ),

            // Z & S Shapes
            listOf(
                listOf(true, true, false),
                listOf(false, true, true)
            ),
            listOf(
                listOf(false, true, true),
                listOf(true, true, false)
            )
        )

        fun getRandomPiece(): BlockPiece {
            val template = TEMPLATES.random()
            val colorId = Random.nextInt(1, 7) // 1 to 6
            return BlockPiece(shape = template, colorId = colorId)
        }
    }
}

/**
 * Themes for customization
 */
enum class GameTheme(
    val displayName: String,
    val background: Color,
    val surfaceContainer: Color,
    val emptyCell: Color,
    val gridBorder: Color,
    val accent: Color,
    val pieceColors: List<Color>
) {
    NEON(
        displayName = "Neon Dark",
        background = Color(0xFF131822),
        surfaceContainer = Color(0xFF181E2A),
        emptyCell = Color(0xFF1E2430),
        gridBorder = Color(0xFF2A324B),
        accent = Color(0xFFFFD54F),
        pieceColors = listOf(
            Color(0xFFFF5252), // Red
            Color(0xFFFF9800), // Orange
            Color(0xFFFFEB3B), // Yellow
            Color(0xFF4CAF50), // Green
            Color(0xFF00BCD4), // Cyan
            Color(0xFF9C27B0)  // Purple
        )
    ),
    WOOD(
        displayName = "Classic Wood",
        background = Color(0xFF2C1D11),
        surfaceContainer = Color(0xFF3E2A1E),
        emptyCell = Color(0xFF4E3627),
        gridBorder = Color(0xFF5C4030),
        accent = Color(0xFFFFB74D),
        pieceColors = listOf(
            Color(0xFFD84315), // Deep Rust
            Color(0xFFE65100), // Burnt Orange
            Color(0xFFF57F17), // Amber
            Color(0xFF8D6E63), // Light Wood
            Color(0xFF6D4C41), // Medium Wood
            Color(0xFF4E342E)  // Dark Walnut
        )
    ),
    OCEAN(
        displayName = "Ocean Ice",
        background = Color(0xFF0A192F),
        surfaceContainer = Color(0xFF112240),
        emptyCell = Color(0xFF1D3557),
        gridBorder = Color(0xFF233554),
        accent = Color(0xFF64FFDA),
        pieceColors = listOf(
            Color(0xFF00E676), // Spring Green
            Color(0xFF00B0FF), // Deep Sky Blue
            Color(0xFF1DE9B6), // Aquamarine
            Color(0xFF651FFF), // Electric Violet
            Color(0xFF00E5FF), // Cyan Accent
            Color(0xFF2979FF)  // Bright Blue
        )
    );

    fun getPieceColor(colorId: Int): Color {
        val index = (colorId - 1).coerceIn(0, pieceColors.lastIndex)
        return pieceColors[index]
    }
}
