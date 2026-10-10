package com.example.npm_b01.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animation states for the 4 puzzle logo blocks
    val block1Scale = remember { Animatable(0f) }
    val block2Scale = remember { Animatable(0f) }
    val block3Scale = remember { Animatable(0f) }
    val block4Scale = remember { Animatable(0f) }

    val titleAlpha = remember { Animatable(0f) }
    val titleScale = remember { Animatable(0.8f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Pop in the 4 blocks sequentially with spring physics
        launch {
            block1Scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f))
        }
        delay(120)
        launch {
            block2Scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f))
        }
        delay(120)
        launch {
            block3Scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f))
        }
        delay(120)
        launch {
            block4Scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f))
        }

        // Fade & Scale in Title and Subtitle
        delay(200)
        launch {
            titleAlpha.animateTo(1f, animationSpec = tween(400))
        }
        launch {
            titleScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.5f))
        }
        delay(200)
        launch {
            subtitleAlpha.animateTo(1f, animationSpec = tween(400))
        }

        // Infinite pulse for loading text
        launch {
            pulseScale.animateTo(
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }

        // Wait before transitioning to the game
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated 2x2 Grid Logo
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Block 1 - Coral Red
                    SplashBlock(
                        color = Color(0xFFFF5252),
                        scale = block1Scale.value
                    )
                    // Block 2 - Amber Yellow
                    SplashBlock(
                        color = Color(0xFFFFB300),
                        scale = block2Scale.value
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Block 3 - Cyan Aqua
                    SplashBlock(
                        color = Color(0xFF00BCD4),
                        scale = block3Scale.value
                    )
                    // Block 4 - Emerald Green
                    SplashBlock(
                        color = Color(0xFF4CAF50),
                        scale = block4Scale.value
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Animated Game Title
            Text(
                text = "BLOCK PUZZLE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .scale(titleScale.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Subtitle
            Text(
                text = "Fit The Blocks 🧩 Clear The Lines",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Pulse Loading Indicator
            Text(
                text = "LOADING...",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8),
                modifier = Modifier
                    .alpha(subtitleAlpha.value)
                    .scale(pulseScale.value)
            )
        }
    }
}

@Composable
private fun SplashBlock(
    color: Color,
    scale: Float
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
    )
}
