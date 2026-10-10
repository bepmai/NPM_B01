package com.example.npm_b01.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.npm_b01.model.BlockPiece
import com.example.npm_b01.model.GameMode
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
    val rotationCharges: Int = 3,
    val totalGamesPlayed: Int = 0,
    val totalLinesCleared: Int = 0,
    val activeEffect: ScoreEffect? = null,
    val gameMode: GameMode = GameMode.LEVELS,
    val currentLevel: Int = 1,
    val levelTargetScore: Int = 300,
    val isLevelCompleted: Boolean = false
) {
    companion object {
        const val GRID_SIZE = 8
    }
}

class BlockPuzzleViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = getApplication<Application>().getSharedPreferences("block_puzzle_prefs", Context.MODE_PRIVATE)

    private val savedLevel = prefs.getInt("current_level", 1)

    private val _uiState = MutableStateFlow(
        BlockPuzzleState(
            highScore = prefs.getInt("high_score", 0),
            totalGamesPlayed = prefs.getInt("total_games", 0),
            totalLinesCleared = prefs.getInt("total_lines", 0),
            currentLevel = savedLevel,
            levelTargetScore = savedLevel * 300
        )
    )
    val uiState: StateFlow<BlockPuzzleState> = _uiState.asStateFlow()

    fun switchGameMode(mode: GameMode) {
        _uiState.update { currentState ->
            currentState.copy(
                gameMode = mode,
                score = 0,
                combo = 1,
                isGameOver = false,
                isLevelCompleted = false,
                board = List(BlockPuzzleState.GRID_SIZE) { List(BlockPuzzleState.GRID_SIZE) { 0 } },
                upcomingPieces = listOf(
                    BlockPiece.getRandomPiece(),
                    BlockPiece.getRandomPiece(),
                    BlockPiece.getRandomPiece()
                )
            )
        }
    }

    fun nextLevel() {
        val currentState = _uiState.value
        val nextLvl = currentState.currentLevel + 1
        val newTarget = nextLvl * 350

        prefs.edit().putInt("current_level", nextLvl).apply()

        _uiState.update {
            it.copy(
                currentLevel = nextLvl,
                levelTargetScore = newTarget,
                isLevelCompleted = false,
                score = 0,
                combo = 1,
                rotationCharges = (it.rotationCharges + 2).coerceAtMost(5),
                board = List(BlockPuzzleState.GRID_SIZE) { List(BlockPuzzleState.GRID_SIZE) { 0 } },
                upcomingPieces = listOf(
                    BlockPiece.getRandomPiece(),
                    BlockPiece.getRandomPiece(),
                    BlockPiece.getRandomPiece()
                )
            )
        }
    }

    fun restartGame() {
        val currentState = _uiState.value
        val newHigh = maxOf(currentState.score, currentState.highScore)
        val newGames = currentState.totalGamesPlayed + 1

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
                gameMode = currentState.gameMode,
                currentLevel = currentState.currentLevel,
                levelTargetScore = currentState.currentLevel * 300,
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

                    if (targetRow !in 0 until BlockPuzzleState.GRID_SIZE ||
                        targetCol !in 0 until BlockPuzzleState.GRID_SIZE
                    ) {
                        return false
                    }

                    if (board[targetRow][targetCol] != 0) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun placePiece(pieceIndex: Int, startRow: Int, startCol: Int): Boolean {
        val currentState = _uiState.value
        if (currentState.isGameOver || currentState.isLevelCompleted) return false

        val piece = currentState.upcomingPieces.getOrNull(pieceIndex) ?: return false

        if (!canPlacePiece(currentState.board, piece, startRow, startCol)) {
            return false
        }

        // 1. Write piece blocks
        val newBoard = currentState.board.map { it.toMutableList() }
        for (r in piece.shape.indices) {
            for (c in piece.shape[r].indices) {
                if (piece.shape[r][c]) {
                    newBoard[startRow + r][startCol + c] = piece.colorId
                }
            }
        }

        var addedScore = piece.blockCount

        // 2. Check for completed lines
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
            newCombo = 1
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

        val bonusCharges = if (totalLinesCleared >= 2) 1 else 0

        if (updatedUpcomingPieces.all { it == null }) {
            updatedUpcomingPieces[0] = BlockPiece.getRandomPiece()
            updatedUpcomingPieces[1] = BlockPiece.getRandomPiece()
            updatedUpcomingPieces[2] = BlockPiece.getRandomPiece()
        }

        val newScore = currentState.score + addedScore
        val newHighScore = maxOf(newScore, currentState.highScore)
        val newTotalLines = currentState.totalLinesCleared + totalLinesCleared

        // Check if Level Target Score is Reached!
        val isCompleted = currentState.gameMode == GameMode.LEVELS && newScore >= currentState.levelTargetScore

        prefs.edit()
            .putInt("high_score", newHighScore)
            .putInt("total_lines", newTotalLines)
            .apply()

        val isGameOverNow = if (isCompleted) false else checkGameOver(updatedBoard, updatedUpcomingPieces)

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
                isLevelCompleted = isCompleted,
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
