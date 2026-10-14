package com.example.npm_b01.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.npm_b01.audio.SoundManager
import com.example.npm_b01.model.PlayType
import com.example.npm_b01.viewmodel.BlockPuzzleState
import com.example.npm_b01.viewmodel.BlockPuzzleViewModel
import kotlinx.coroutines.delay

@Composable
fun GravityDropGame(
    viewModel: BlockPuzzleViewModel = viewModel(),
    onGoHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val theme = state.currentTheme

    // Automatic Free Fall Gravity Loop Timer
    LaunchedEffect(state.playType, state.isGameOver, state.fallingPiece) {
        if (state.playType == PlayType.GRAVITY && !state.isGameOver) {
            while (true) {
                val speedMs = maxOf(200L, 650L - (state.score / 100) * 30L)
                delay(speedMs)
                viewModel.stepGravityDown()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1E293B)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RƠI TỰ DO ⏬",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Score: ${state.score}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accent
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onGoHome,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(theme.surfaceContainer)
                        ) {
                            Text(text = "🏠", fontSize = 16.sp)
                        }

                        IconButton(
                            onClick = { viewModel.restartGame() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(theme.surfaceContainer)
                        ) {
                            Text(text = "↺", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Gravity Board (12 Rows x 8 Columns) - Tap anywhere drops piece immediately!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                // Tapping anywhere on the board drops the falling piece immediately!
                                viewModel.dropFallingPiece()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (r in 0 until BlockPuzzleState.GRAVITY_ROWS) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (c in 0 until BlockPuzzleState.GRAVITY_COLS) {
                                    var colorId = state.gravityBoard[r][c]

                                    // Render falling piece overlay
                                    val piece = state.fallingPiece
                                    if (piece != null) {
                                        val pr = r - state.fallingRow
                                        val pc = c - state.fallingCol
                                        if (pr in 0 until piece.height && pc in 0 until piece.width) {
                                            if (piece.shape[pr][pc]) {
                                                colorId = piece.colorId
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(1.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (colorId != 0) theme.getPieceColor(colorId) else Color(0xFF1E293B)
                                            )
                                            .then(
                                                if (colorId != 0) {
                                                    Modifier.border(
                                                        width = 1.dp,
                                                        color = Color.White.copy(alpha = 0.35f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                } else {
                                                    Modifier
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Controls Area (Left, Down, Rotate, Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left
                    Button(
                        onClick = {
                            SoundManager.playPickUpSound()
                            viewModel.moveFallingLeft()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "⬅️ Trái", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Soft Drop (Down 1 step)
                    Button(
                        onClick = {
                            viewModel.stepGravityDown()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "⬇️ Cho Rơi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Rotate
                    Button(
                        onClick = {
                            SoundManager.playPickUpSound()
                            viewModel.rotateFallingPiece()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "🔄 Xoay", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Right
                    Button(
                        onClick = {
                            SoundManager.playPickUpSound()
                            viewModel.moveFallingRight()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "➡️ Phải", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Floating Score Effect Banner
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = state.activeEffect != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -40 })
                ) {
                    state.activeEffect?.let { effect ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(theme.accent)
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

            // Game Over Dialog
            Column(modifier = Modifier.align(Alignment.Center)) {
                AnimatedVisibility(
                    visible = state.isGameOver,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    GameOverDialog(
                        score = state.score,
                        highScore = state.highScore,
                        theme = theme,
                        onRestart = { viewModel.restartGame() }
                    )
                }
            }
        }
    }
}
