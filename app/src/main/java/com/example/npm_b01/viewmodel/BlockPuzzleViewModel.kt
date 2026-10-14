package com.example.npm_b01.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.npm_b01.audio.SoundManager
import com.example.npm_b01.i18n.AppLanguage
import com.example.npm_b01.model.BlockPiece
import com.example.npm_b01.model.GameMode
import com.example.npm_b01.model.GameTheme
import com.example.npm_b01.model.PlayType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Random

data class ScoreEffect(
    val id: Long = System.currentTimeMillis(),
    val text: String
)

data class BlockPuzzleState(
    val board: List<List<Int>> = List(6) { List(6) { 0 } },
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
    val currentLanguage: AppLanguage = AppLanguage.VI,
    val rotationCharges: Int = 3,
    val totalGamesPlayed: Int = 0,
    val totalLinesCleared: Int = 0,
    val activeEffect: ScoreEffect? = null,
    val gameMode: GameMode = GameMode.LEVELS,
    val playType: PlayType = PlayType.FREE,
    val currentLevel: Int = 1,
    val levelTargetScore: Int = 150,
    val isLevelCompleted: Boolean = false,

    // Gravity Drop Mode State
    val gravityBoard: List<List<Int>> = List(GRAVITY_ROWS) { List(GRAVITY_COLS) { 0 } },
    val fallingPiece: BlockPiece? = null,
    val fallingCol: Int = 3,
    val fallingRow: Int = 0
) {
    val gridSize: Int get() = if (board.isNotEmpty()) board.size else 8

    companion object {
        const val GRAVITY_COLS = 8
        const val GRAVITY_ROWS = 12
    }
}

class BlockPuzzleViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = getApplication<Application>().getSharedPreferences("block_puzzle_prefs", Context.MODE_PRIVATE)

    private val savedLevel = prefs.getInt("current_level", 1)
    private val savedLangCode = prefs.getString("app_language", "vi") ?: "vi"
    private val initialLang = if (savedLangCode == "en") AppLanguage.EN else AppLanguage.VI

    private fun getGridSizeForLevel(level: Int, mode: GameMode): Int {
        if (mode == GameMode.CLASSIC) return 8
        return when (level) {
            1, 2 -> 6
            3, 4, 5 -> 7
            else -> 8
        }
    }

    private fun getTargetScoreForLevel(level: Int): Int {
        return when (level) {
            1 -> 150
            2 -> 250
            3 -> 400
            4 -> 550
            5 -> 700
            else -> level * 200
        }
    }

    private val _uiState = MutableStateFlow(
        run {
            val initialGridSize = getGridSizeForLevel(savedLevel, GameMode.LEVELS)
            BlockPuzzleState(
                highScore = prefs.getInt("high_score", 0),
                totalGamesPlayed = prefs.getInt("total_games", 0),
                totalLinesCleared = prefs.getInt("total_lines", 0),
                currentLevel = savedLevel,
                currentLanguage = initialLang,
                levelTargetScore = getTargetScoreForLevel(savedLevel),
                board = List(initialGridSize) { List(initialGridSize) { 0 } }
            )
        }
    )
    val uiState: StateFlow<BlockPuzzleState> = _uiState.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.code).apply()
        _uiState.update { it.copy(currentLanguage = language) }
    }

    fun startFreePlay() {
        _uiState.update { currentState ->
            val gridSize = getGridSizeForLevel(currentState.currentLevel, currentState.gameMode)
            currentState.copy(
                playType = PlayType.FREE,
                score = 0,
                combo = 1,
                isGameOver = false,
                isLevelCompleted = false,
                rotationCharges = 3,
                board = List(gridSize) { List(gridSize) { 0 } },
                upcomingPieces = listOf(
                    BlockPiece.getRandomPiece(),
                    BlockPiece.getRandomPiece(),
                    BlockPiece.getRandomPiece()
                )
            )
        }
    }

    fun startPresetChallenge() {
        val currentState = _uiState.value
        val gridSize = getGridSizeForLevel(currentState.currentLevel, currentState.gameMode)
        val presetBoard = generatePresetBoard(gridSize)
        _uiState.update {
            it.copy(
                playType = PlayType.PRESET,
                score = 0,
                combo = 1,
                isGameOver = false,
                isLevelCompleted = false,
                rotationCharges = 3,
                board = presetBoard,
                upcomingPieces = listOf(
                    BlockPiece.getRandomPiece(),
                    BlockPiece.getRandomPiece(),
                    BlockPiece.getRandomPiece()
                )
            )
        }
    }

    fun startGravityDrop() {
        val firstPiece = BlockPiece.getRandomPiece()
        val initialCol = (BlockPuzzleState.GRAVITY_COLS - firstPiece.width) / 2
        _uiState.update { currentState ->
            currentState.copy(
                playType = PlayType.GRAVITY,
                score = 0,
                combo = 1,
                isGameOver = false,
                isLevelCompleted = false,
                gravityBoard = List(BlockPuzzleState.GRAVITY_ROWS) { List(BlockPuzzleState.GRAVITY_COLS) { 0 } },
                fallingPiece = firstPiece,
                fallingCol = initialCol,
                fallingRow = 0
            )
        }
    }

    fun stepGravityDown() {
        val currentState = _uiState.value
        val piece = currentState.fallingPiece ?: return
        if (currentState.isGameOver || currentState.playType != PlayType.GRAVITY) return

        val nextRow = currentState.fallingRow + 1
        if (nextRow + piece.height <= BlockPuzzleState.GRAVITY_ROWS &&
            canPlaceGravityPiece(currentState.gravityBoard, piece, nextRow, currentState.fallingCol)
        ) {
            _uiState.update { it.copy(fallingRow = nextRow) }
        } else {
            dropFallingPiece()
        }
    }

    fun moveFallingLeft() {
        val currentState = _uiState.value
        val piece = currentState.fallingPiece ?: return
        if (currentState.fallingCol > 0) {
            val newCol = currentState.fallingCol - 1
            if (canPlaceGravityPiece(currentState.gravityBoard, piece, currentState.fallingRow, newCol)) {
                _uiState.update { it.copy(fallingCol = newCol) }
            }
        }
    }

    fun moveFallingRight() {
        val currentState = _uiState.value
        val piece = currentState.fallingPiece ?: return
        if (currentState.fallingCol + piece.width < BlockPuzzleState.GRAVITY_COLS) {
            val newCol = currentState.fallingCol + 1
            if (canPlaceGravityPiece(currentState.gravityBoard, piece, currentState.fallingRow, newCol)) {
                _uiState.update { it.copy(fallingCol = newCol) }
            }
        }
    }

    fun rotateFallingPiece() {
        val currentState = _uiState.value
        val piece = currentState.fallingPiece ?: return
        val rotated = piece.rotate()

        var targetCol = currentState.fallingCol
        if (targetCol + rotated.width > BlockPuzzleState.GRAVITY_COLS) {
            targetCol = BlockPuzzleState.GRAVITY_COLS - rotated.width
        }

        if (canPlaceGravityPiece(currentState.gravityBoard, rotated, currentState.fallingRow, targetCol)) {
            _uiState.update {
                it.copy(
                    fallingPiece = rotated,
                    fallingCol = targetCol
                )
            }
        }
    }

    fun dropFallingPiece() {
        val currentState = _uiState.value
        val piece = currentState.fallingPiece ?: return
        if (currentState.isGameOver) return

        var landRow = currentState.fallingRow
        while (landRow + 1 + piece.height <= BlockPuzzleState.GRAVITY_ROWS &&
            canPlaceGravityPiece(currentState.gravityBoard, piece, landRow + 1, currentState.fallingCol)
        ) {
            landRow++
        }

        val newBoard = currentState.gravityBoard.map { it.toMutableList() }
        for (r in piece.shape.indices) {
            for (c in piece.shape[r].indices) {
                if (piece.shape[r][c]) {
                    newBoard[landRow + r][currentState.fallingCol + c] = piece.colorId
                }
            }
        }

        var addedScore = piece.blockCount

        val rowsToClear = mutableListOf<Int>()
        for (r in 0 until BlockPuzzleState.GRAVITY_ROWS) {
            if (newBoard[r].all { it != 0 }) {
                rowsToClear.add(r)
            }
        }

        val totalLines = rowsToClear.size
        var newCombo = currentState.combo
        var effect: ScoreEffect? = null

        if (totalLines > 0) {
            val linePoints = totalLines * 20 * newCombo
            addedScore += linePoints
            effect = ScoreEffect(text = "+$linePoints! COMBO x$newCombo🔥")
            newCombo++
            SoundManager.playLineClearSound(newCombo)

            for (r in rowsToClear) {
                for (above in r downTo 1) {
                    for (c in 0 until BlockPuzzleState.GRAVITY_COLS) {
                        newBoard[above][c] = newBoard[above - 1][c]
                    }
                }
                for (c in 0 until BlockPuzzleState.GRAVITY_COLS) {
                    newBoard[0][c] = 0
                }
            }
        } else {
            newCombo = 1
            SoundManager.playPlaceSound()
        }

        val updatedBoard = newBoard.map { it.toList() }

        val nextPiece = BlockPiece.getRandomPiece()
        val nextCol = (BlockPuzzleState.GRAVITY_COLS - nextPiece.width) / 2
        val isGameOver = !canPlaceGravityPiece(updatedBoard, nextPiece, 0, nextCol)

        val newScore = currentState.score + addedScore
        val newHigh = maxOf(newScore, currentState.highScore)
        val newTotalLines = currentState.totalLinesCleared + totalLines

        prefs.edit()
            .putInt("high_score", newHigh)
            .putInt("total_lines", newTotalLines)
            .apply()

        _uiState.update {
            it.copy(
                gravityBoard = updatedBoard,
                fallingPiece = nextPiece,
                fallingCol = nextCol,
                fallingRow = 0,
                score = newScore,
                highScore = newHigh,
                combo = newCombo,
                totalLinesCleared = newTotalLines,
                isGameOver = isGameOver,
                activeEffect = effect
            )
        }
    }

    private fun canPlaceGravityPiece(
        board: List<List<Int>>,
        piece: BlockPiece,
        startRow: Int,
        startCol: Int
    ): Boolean {
        for (r in piece.shape.indices) {
            for (c in piece.shape[r].indices) {
                if (piece.shape[r][c]) {
                    val tr = startRow + r
                    val tc = startCol + c

                    if (tr !in 0 until BlockPuzzleState.GRAVITY_ROWS ||
                        tc !in 0 until BlockPuzzleState.GRAVITY_COLS
                    ) {
                        return false
                    }

                    if (board[tr][tc] != 0) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun generatePresetBoard(gridSize: Int): List<List<Int>> {
        val board = MutableList(gridSize) { MutableList(gridSize) { 0 } }
        val random = Random()
        var placedCount = 0
        val targetBlocks = if (gridSize == 6) 6 else if (gridSize == 7) 10 else 14

        while (placedCount < targetBlocks) {
            val r = random.nextInt(gridSize)
            val c = random.nextInt(gridSize)
            if (board[r][c] == 0) {
                board[r][c] = random.nextInt(6) + 1
                val rowFull = board[r].all { it != 0 }
                val colFull = (0 until gridSize).all { board[it][c] != 0 }
                if (rowFull || colFull) {
                    board[r][c] = 0
                } else {
                    placedCount++
                }
            }
        }
        return board.map { it.toList() }
    }

    fun switchGameMode(mode: GameMode) {
        _uiState.update { currentState ->
            val gridSize = getGridSizeForLevel(currentState.currentLevel, mode)
            val newBoard = if (currentState.playType == PlayType.PRESET) generatePresetBoard(gridSize) else List(gridSize) { List(gridSize) { 0 } }
            currentState.copy(
                gameMode = mode,
                score = 0,
                combo = 1,
                isGameOver = false,
                isLevelCompleted = false,
                board = newBoard,
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
        val newTarget = getTargetScoreForLevel(nextLvl)
        val gridSize = getGridSizeForLevel(nextLvl, currentState.gameMode)

        prefs.edit().putInt("current_level", nextLvl).apply()

        val newBoard = if (currentState.playType == PlayType.PRESET) generatePresetBoard(gridSize) else List(gridSize) { List(gridSize) { 0 } }

        _uiState.update {
            it.copy(
                currentLevel = nextLvl,
                levelTargetScore = newTarget,
                isLevelCompleted = false,
                score = 0,
                combo = 1,
                rotationCharges = (it.rotationCharges + 2).coerceAtMost(5),
                board = newBoard,
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

        if (currentState.playType == PlayType.GRAVITY) {
            startGravityDrop()
            return
        }

        val gridSize = getGridSizeForLevel(currentState.currentLevel, currentState.gameMode)
        val newBoard = if (currentState.playType == PlayType.PRESET) generatePresetBoard(gridSize) else List(gridSize) { List(gridSize) { 0 } }

        _uiState.update {
            BlockPuzzleState(
                highScore = newHigh,
                totalGamesPlayed = newGames,
                totalLinesCleared = currentState.totalLinesCleared,
                currentTheme = currentState.currentTheme,
                currentLanguage = currentState.currentLanguage,
                gameMode = currentState.gameMode,
                playType = currentState.playType,
                currentLevel = currentState.currentLevel,
                levelTargetScore = getTargetScoreForLevel(currentState.currentLevel),
                rotationCharges = 3,
                board = newBoard,
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
        val gridSize = board.size
        for (r in piece.shape.indices) {
            for (c in piece.shape[r].indices) {
                if (piece.shape[r][c]) {
                    val targetRow = startRow + r
                    val targetCol = startCol + c

                    if (targetRow !in 0 until gridSize ||
                        targetCol !in 0 until gridSize
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

        val gridSize = currentState.gridSize

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

        for (r in 0 until gridSize) {
            if (newBoard[r].all { it != 0 }) {
                rowsToClear.add(r)
            }
        }

        for (c in 0 until gridSize) {
            if ((0 until gridSize).all { r -> newBoard[r][c] != 0 }) {
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
            SoundManager.playLineClearSound(newCombo)
        } else {
            newCombo = 1
            SoundManager.playPlaceSound()
        }

        // Clear cells
        for (r in rowsToClear) {
            for (c in 0 until gridSize) {
                newBoard[r][c] = 0
            }
        }
        for (c in colsToClear) {
            for (r in 0 until gridSize) {
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

        val gridSize = board.size
        for (piece in remainingPieces) {
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    if (canPlacePiece(board, piece, r, c)) {
                        return false
                    }
                }
            }
        }
        return true
    }
}
