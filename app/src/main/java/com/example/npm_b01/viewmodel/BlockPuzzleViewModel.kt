package com.example.npm_b01.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.npm_b01.model.BlockPiece
import com.example.npm_b01.model.GameTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ScoreEffect(
    val id: Long = System.currentTimeMillis(),
    val text: String
)

data class BlockPuzzleState(
    val board: List<List<Int>> = List(GRID_SIZE) { List(GRID_SIZE) { 0 } },
    val upcomingPieces: List<BlockPiece?> = listOf(
        BlockPiece.getRandomPiece(),
        BlockPiece.getRandomPiece(),
        BlockPiece.getRandomPiece()
    ),
    val score: Int = 0,
    val highScore: Int = 0,
    val combo: Int = 1,
    val isGameOver: Boolean = false,
    val currentTheme: GameTheme = GameTheme.NEON,
    val rotationCharges: Int = 3, // Power-up charges for piece rotation
    val totalGamesPlayed: Int = 0,
    val totalLinesCleared: Int = 0,
    val activeEffect: ScoreEffect? = null
) {
    companion object {
        const val GRID_SIZE = 8
    }
}

class BlockPuzzleViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = getApplication<Application>().getSharedPreferences("block_puzzle_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        BlockPuzzleState(
            highScore = prefs.getInt("high_score", 0),
            totalGamesPlayed = prefs.getInt("total_games", 0),
            totalLinesCleared = prefs.getInt("total_lines", 0)
        )
    )
    val uiState: StateFlow<BlockPuzzleState> = _uiState.asStateFlow()

    fun restartGame() {
        val currentState = _uiState.value
        val newHigh = maxOf(currentState.score, currentState.highScore)
        val newGames = currentState.totalGamesPlayed + 1

        // Save persistent statistics
        prefs.edit()
            .putInt("high_score", newHigh)
            .putInt("total_games", newGames)
            .putInt("total_lines", currentState.totalLinesCleared)
            .apply()

        _uiState.update {
            BlockPuzzleState(
                highScore = newHigh,
                totalGamesPlayed = newGames,
                totalLinesCleared = currentState.totalLinesCleared,
                currentTheme = currentState.currentTheme,
                rotationCharges = 3,
                upcomingPieces = listOf(
                    BlockPiece.getRandomPiece(),
                    BlockPiece.getRandomPiece(),
                    BlockPiece.getRandomPiece()
                )
            )
        }
    }

    fun switchTheme(theme: GameTheme) {
        _uiState.update { it.copy(currentTheme = theme) }
    }

    /**
     * Power-up: Rotates the piece at [pieceIndex] in the dock.
     */
    fun rotatePiece(pieceIndex: Int): Boolean {
        val currentState = _uiState.value
        if (currentState.rotationCharges <= 0) return false

        val piece = currentState.upcomingPieces.getOrNull(pieceIndex) ?: return false

        val rotatedPiece = piece.rotate()
        val updatedPieces = currentState.upcomingPieces.toMutableList()
        updatedPieces[pieceIndex] = rotatedPiece

        _uiState.update {
            it.copy(
                upcomingPieces = updatedPieces,
                rotationCharges = it.rotationCharges - 1
            )
        }
        return true
    }

    /**
     * Checks if a piece can be placed at (startRow, startCol).
     */
    fun canPlacePiece(
        board: List<List<Int>>,
        piece: BlockPiece,
        startRow: Int,
        startCol: Int
    ): Boolean {
        for (r in piece.shape.indices) {
            for (c in piece.shape[r].indices) {
                if (piece.shape[r][c]) {
                    val targetRow = startRow + r
                    val targetCol = startCol + c

                    // Check bounds
                    if (targetRow !in 0 until BlockPuzzleState.GRID_SIZE ||
                        targetCol !in 0 until BlockPuzzleState.GRID_SIZE
                    ) {
                        return false
                    }

                    // Check overlap
                    if (board[targetRow][targetCol] != 0) {
                        return false
                    }
                }
            }
        }
        return true
    }

    /**
     * Attempts to place the piece at index [pieceIndex] into (startRow, startCol).
     */
    fun placePiece(pieceIndex: Int, startRow: Int, startCol: Int): Boolean {
        val currentState = _uiState.value
        if (currentState.isGameOver) return false

        val piece = currentState.upcomingPieces.getOrNull(pieceIndex) ?: return false

        if (!canPlacePiece(currentState.board, piece, startRow, startCol)) {
            return false
        }

        // 1. Write piece blocks to board
        val newBoard = currentState.board.map { it.toMutableList() }
        for (r in piece.shape.indices) {
            for (c in piece.shape[r].indices) {
                if (piece.shape[r][c]) {
                    newBoard[startRow + r][startCol + c] = piece.colorId
                }
            }
        }

        var addedScore = piece.blockCount

        // 2. Check for completed rows and columns
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        for (r in 0 until BlockPuzzleState.GRID_SIZE) {
            if (newBoard[r].all { it != 0 }) {
                rowsToClear.add(r)
            }
        }

        for (c in 0 until BlockPuzzleState.GRID_SIZE) {
            if ((0 until BlockPuzzleState.GRID_SIZE).all { r -> newBoard[r][c] != 0 }) {
                colsToClear.add(c)
            }
        }

        val totalLinesCleared = rowsToClear.size + colsToClear.size
        var newCombo = currentState.combo
        var effect: ScoreEffect? = null

        if (totalLinesCleared > 0) {
            val linePoints = totalLinesCleared * 10 * newCombo
            addedScore += linePoints

            val effectText = if (newCombo > 1) {
                "+$linePoints! COMBO x$newCombo🔥"
            } else {
                "+$linePoints!"
            }
            effect = ScoreEffect(text = effectText)

            newCombo += 1
        } else {
            newCombo = 1 // Reset combo
        }

        // Clear cells
        for (r in rowsToClear) {
            for (c in 0 until BlockPuzzleState.GRID_SIZE) {
                newBoard[r][c] = 0
            }
        }
        for (c in colsToClear) {
            for (r in 0 until BlockPuzzleState.GRID_SIZE) {
                newBoard[r][c] = 0
            }
        }

        val updatedBoard = newBoard.map { it.toList() }

        // 3. Update upcoming pieces
        val updatedUpcomingPieces = currentState.upcomingPieces.toMutableList()
        updatedUpcomingPieces[pieceIndex] = null

        // Bonus charge for clearing lines
        val bonusCharges = if (totalLinesCleared >= 2) 1 else 0

        // If all 3 pieces used, spawn 3 new pieces
        if (updatedUpcomingPieces.all { it == null }) {
            updatedUpcomingPieces[0] = BlockPiece.getRandomPiece()
            updatedUpcomingPieces[1] = BlockPiece.getRandomPiece()
            updatedUpcomingPieces[2] = BlockPiece.getRandomPiece()
        }

        val newScore = currentState.score + addedScore
        val newHighScore = maxOf(newScore, currentState.highScore)
        val newTotalLines = currentState.totalLinesCleared + totalLinesCleared

        // Persist high score and total lines
        prefs.edit()
            .putInt("high_score", newHighScore)
            .putInt("total_lines", newTotalLines)
            .apply()

        // 4. Check for Game Over condition
        val isGameOverNow = checkGameOver(updatedBoard, updatedUpcomingPieces)

        _uiState.update {
            it.copy(
                board = updatedBoard,
                upcomingPieces = updatedUpcomingPieces,
                score = newScore,
                highScore = newHighScore,
                combo = newCombo,
                rotationCharges = (it.rotationCharges + bonusCharges).coerceAtMost(5),
                totalLinesCleared = newTotalLines,
                isGameOver = isGameOverNow,
                activeEffect = effect
            )
        }

        return true
    }

    private fun checkGameOver(
        board: List<List<Int>>,
        pieces: List<BlockPiece?>
    ): Boolean {
        val remainingPieces = pieces.filterNotNull()
        if (remainingPieces.isEmpty()) return false

        for (piece in remainingPieces) {
            for (r in 0 until BlockPuzzleState.GRID_SIZE) {
                for (c in 0 until BlockPuzzleState.GRID_SIZE) {
                    if (canPlacePiece(board, piece, r, c)) {
                        return false
                    }
                }
            }
        }
        return true
    }
}
