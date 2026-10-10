package com.example.npm_b01.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.npm_b01.model.BlockPiece
import com.example.npm_b01.model.GameTheme
import com.example.npm_b01.viewmodel.BlockPuzzleState
import com.example.npm_b01.viewmodel.BlockPuzzleViewModel
import kotlin.math.roundToInt

data class DraggingState(
    val pieceIndex: Int,
    val piece: BlockPiece,
    val dragPosition: Offset
)

data class PlacementPreview(
    val piece: BlockPiece,
    val startRow: Int,
    val startCol: Int,
    val isValid: Boolean
)

@Composable
fun BlockPuzzleGame(
    viewModel: BlockPuzzleViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val currentTheme = state.currentTheme

    var boardBoundsInRoot by remember { mutableStateOf(Rect.Zero) }
    var draggingState by remember { mutableStateOf<DraggingState?>(null) }
    var showStatsDialog by remember { mutableStateOf(false) }

    val cellSizePx = if (boardBoundsInRoot.width > 0f) {
        boardBoundsInRoot.width / BlockPuzzleState.GRID_SIZE
    } else {
        0f
    }

    val cellSizeDp = with(density) { cellSizePx.toDp() }
    val maxLiftPx = with(density) { 50.dp.toPx() }

    fun calculateTargetGrid(drag: DraggingState): Pair<Int, Int>? {
        if (cellSizePx <= 0f) return null

        val isNearBoard = drag.dragPosition.x >= (boardBoundsInRoot.left - cellSizePx * 1.5f) &&
                drag.dragPosition.x <= (boardBoundsInRoot.right + cellSizePx * 1.5f) &&
                drag.dragPosition.y >= (boardBoundsInRoot.top - cellSizePx * 1.5f) &&
                drag.dragPosition.y <= (boardBoundsInRoot.bottom + cellSizePx * 1.5f)

        if (!isNearBoard) return null

        val pieceWidthPx = drag.piece.width * cellSizePx
        val pieceHeightPx = drag.piece.height * cellSizePx

        val distToTop = drag.dragPosition.y - boardBoundsInRoot.top
        val currentLiftPx = maxLiftPx.coerceAtMost(maxOf(0f, distToTop - (pieceHeightPx / 2f)))

        val pieceTopLeftX = drag.dragPosition.x - (pieceWidthPx / 2f)
        val pieceTopLeftY = drag.dragPosition.y - (pieceHeightPx / 2f) - currentLiftPx

        val relX = pieceTopLeftX - boardBoundsInRoot.left
        val relY = pieceTopLeftY - boardBoundsInRoot.top

        val rawCol = (relX / cellSizePx).roundToInt()
        val rawRow = (relY / cellSizePx).roundToInt()

        val startCol = rawCol.coerceIn(0, (BlockPuzzleState.GRID_SIZE - drag.piece.width).coerceAtLeast(0))
        val startRow = rawRow.coerceIn(0, (BlockPuzzleState.GRID_SIZE - drag.piece.height).coerceAtLeast(0))

        return Pair(startRow, startCol)
    }

    val placementPreview: PlacementPreview? = remember(
        draggingState,
        boardBoundsInRoot,
        state.board
    ) {
        val drag = draggingState ?: return@remember null
        val target = calculateTargetGrid(drag) ?: return@remember null

        val isValid = viewModel.canPlacePiece(state.board, drag.piece, target.first, target.second)
        PlacementPreview(drag.piece, target.first, target.second, isValid)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = currentTheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                GameHeader(
                    score = state.score,
                    highScore = state.highScore,
                    combo = state.combo,
                    theme = currentTheme,
                    onThemeChange = { viewModel.switchTheme(it) },
                    onOpenStats = { showStatsDialog = true },
                    onRestart = { viewModel.restartGame() }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Board
                Box(contentAlignment = Alignment.Center) {
                    GameBoard(
                        board = state.board,
                        theme = currentTheme,
                        placementPreview = placementPreview,
                        onBoardPositioned = { rect -> boardBoundsInRoot = rect }
                    )

                    // Floating Score Effect Banner
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedVisibility(
                            visible = state.activeEffect != null,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { -40 })
                        ) {
                            state.activeEffect?.let { effect ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(currentTheme.accent)
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = effect.text,
                                        color = Color.Black,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Piece Dock with Rotation Power-up
                PieceDock(
                    upcomingPieces = state.upcomingPieces,
                    theme = currentTheme,
                    rotationCharges = state.rotationCharges,
                    onRotatePiece = { index -> viewModel.rotatePiece(index) },
                    onDragStart = { index, piece, position ->
                        draggingState = DraggingState(index, piece, position)
                    },
                    onDrag = { position ->
                        draggingState = draggingState?.copy(dragPosition = position)
                    },
                    onDragEnd = {
                        val drag = draggingState
                        if (drag != null) {
                            val target = calculateTargetGrid(drag)
                            if (target != null) {
                                val (startRow, startCol) = target
                                if (viewModel.canPlacePiece(state.board, drag.piece, startRow, startCol)) {
                                    viewModel.placePiece(drag.pieceIndex, startRow, startCol)
                                }
                            }
                        }
                        draggingState = null
                    }
                )
            }

            // Floating Dragging Overlay
            draggingState?.let { drag ->
                if (cellSizePx > 0f) {
                    val pieceWidthPx = drag.piece.width * cellSizePx
                    val pieceHeightPx = drag.piece.height * cellSizePx

                    val distToTop = drag.dragPosition.y - boardBoundsInRoot.top
                    val currentLiftPx = maxLiftPx.coerceAtMost(maxOf(0f, distToTop - (pieceHeightPx / 2f)))

                    val pieceTopLeftX = drag.dragPosition.x - (pieceWidthPx / 2f)
                    val pieceTopLeftY = drag.dragPosition.y - (pieceHeightPx / 2f) - currentLiftPx

                    val floatX = pieceTopLeftX.roundToInt()
                    val floatY = pieceTopLeftY.roundToInt()

                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier.offset { IntOffset(floatX, floatY) }
                        ) {
                            PieceShapeView(
                                piece = drag.piece,
                                theme = currentTheme,
                                cellSizeDp = cellSizeDp
                            )
                        }
                    }
                }
            }

            // Game Over Dialog
            Column {
                AnimatedVisibility(
                    visible = state.isGameOver,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    GameOverDialog(
                        score = state.score,
                        highScore = state.highScore,
                        theme = currentTheme,
                        onRestart = { viewModel.restartGame() }
                    )
                }
            }

            // Statistics Sheet
            Column {
                AnimatedVisibility(
                    visible = showStatsDialog,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    StatsDialog(
                        highScore = state.highScore,
                        totalGames = state.totalGamesPlayed,
                        totalLines = state.totalLinesCleared,
                        theme = currentTheme,
                        onClose = { showStatsDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
fun GameHeader(
    score: Int,
    highScore: Int,
    combo: Int,
    theme: GameTheme,
    onThemeChange: (GameTheme) -> Unit,
    onOpenStats: () -> Unit,
    onRestart: () -> Unit
) {
    var showThemeMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BLOCK PUZZLE",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Theme Selector
                Box {
                    IconButton(
                        onClick = { showThemeMenu = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(theme.surfaceContainer)
                    ) {
                        Text(text = "🎨", fontSize = 16.sp)
                    }

                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false },
                        modifier = Modifier.background(theme.surfaceContainer)
                    ) {
                        GameTheme.entries.forEach { themeItem ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = themeItem.displayName,
                                        color = if (themeItem == theme) theme.accent else Color.White,
                                        fontWeight = if (themeItem == theme) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onThemeChange(themeItem)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }
                }

                // Stats Button
                IconButton(
                    onClick = onOpenStats,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(theme.surfaceContainer)
                ) {
                    Text(text = "🏆", fontSize = 16.sp)
                }

                // Restart Button
                IconButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(theme.surfaceContainer)
                ) {
                    Text(
                        text = "↺",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Score Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SCORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "$score",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.accent
                    )
                }
            }

            // High Score Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "★",
                            color = Color(0xFFFFB300),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BEST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = "$highScore",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        // Combo Badge
        AnimatedVisibility(visible = combo > 1) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF5252), Color(0xFFFF9800))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "$combo x COMBO! 🔥",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun GameBoard(
    board: List<List<Int>>,
    theme: GameTheme,
    placementPreview: PlacementPreview?,
    onBoardPositioned: (Rect) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .onGloballyPositioned { coordinates ->
                    onBoardPositioned(coordinates.boundsInRoot())
                },
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (r in 0 until BlockPuzzleState.GRID_SIZE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (c in 0 until BlockPuzzleState.GRID_SIZE) {
                        val colorId = board[r][c]

                        var isPreviewCell = false
                        var isPreviewValid = false

                        if (placementPreview != null) {
                            val pr = r - placementPreview.startRow
                            val pc = c - placementPreview.startCol
                            val piece = placementPreview.piece
                            if (pr in 0 until piece.height && pc in 0 until piece.width) {
                                if (piece.shape[pr][pc]) {
                                    isPreviewCell = true
                                    isPreviewValid = placementPreview.isValid
                                }
                            }
                        }

                        GridCell(
                            colorId = colorId,
                            theme = theme,
                            isPreview = isPreviewCell,
                            isPreviewValid = isPreviewValid,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridCell(
    colorId: Int,
    theme: GameTheme,
    isPreview: Boolean,
    isPreviewValid: Boolean,
    modifier: Modifier = Modifier
) {
    val cellColor = when {
        colorId != 0 -> theme.getPieceColor(colorId)
        isPreview && isPreviewValid -> Color(0x804CAF50)
        isPreview && !isPreviewValid -> Color(0x80FF5252)
        else -> theme.emptyCell
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(cellColor)
            .then(
                if (colorId != 0) {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(6.dp)
                    )
                } else {
                    Modifier
                }
            )
    )
}

@Composable
fun PieceDock(
    upcomingPieces: List<BlockPiece?>,
    theme: GameTheme,
    rotationCharges: Int,
    onRotatePiece: (index: Int) -> Unit,
    onDragStart: (index: Int, piece: BlockPiece, position: Offset) -> Unit,
    onDrag: (position: Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Power-up Charges Banner
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UPCOMING BLOCKS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Text(
                    text = "⚡ Rotation Charges: $rotationCharges",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.accent
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                upcomingPieces.forEachIndexed { index, piece ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (piece != null) {
                            var cardPositionInRoot by remember { mutableStateOf(Offset.Zero) }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .onGloballyPositioned { coordinates ->
                                            cardPositionInRoot = coordinates.boundsInRoot().topLeft
                                        }
                                        .pointerInput(piece) {
                                            detectDragGestures(
                                                onDragStart = { localOffset ->
                                                    val rootOffset = cardPositionInRoot + localOffset
                                                    onDragStart(index, piece, rootOffset)
                                                },
                                                onDrag = { change, _ ->
                                                    change.consume()
                                                    onDrag(change.position + cardPositionInRoot)
                                                },
                                                onDragEnd = { onDragEnd() },
                                                onDragCancel = { onDragEnd() }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    PieceShapeView(piece = piece, theme = theme, cellSizeDp = 16.dp)
                                }

                                // Rotate Button for Piece
                                if (rotationCharges > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.emptyCell)
                                            .clickable { onRotatePiece(index) }
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "🔄 Rotate",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieceShapeView(
    piece: BlockPiece,
    theme: GameTheme,
    cellSizeDp: Dp
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        for (r in 0 until piece.height) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                for (c in 0 until piece.width) {
                    val isBlock = piece.shape[r][c]
                    if (isBlock) {
                        Box(
                            modifier = Modifier
                                .size(cellSizeDp)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.getPieceColor(piece.colorId))
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.size(cellSizeDp))
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    score: Int,
    highScore: Int,
    theme: GameTheme,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceContainer),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NO MORE MOVES!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF5252),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "FINAL SCORE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = "$score",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = theme.accent
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (score >= highScore && score > 0) {
                    Text(
                        text = "🎉 NEW BEST SCORE! 🎉",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                } else {
                    Text(
                        text = "Best: $highScore",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.accent
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "PLAY AGAIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun StatsDialog(
    highScore: Int,
    totalGames: Int,
    totalLines: Int,
    theme: GameTheme,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceContainer),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 STATISTICS 🏆",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                StatRow("Best High Score", "$highScore", theme.accent)
                StatRow("Total Games Played", "$totalGames", Color.White)
                StatRow("Total Lines Cleared", "$totalLines", Color.White)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.emptyCell
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "CLOSE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 18.sp, color = valueColor, fontWeight = FontWeight.Bold)
    }
}
