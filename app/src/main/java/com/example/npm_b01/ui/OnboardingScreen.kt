package com.example.npm_b01.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val buttonText: String
)

private val ONBOARDING_PAGES = listOf(
    OnboardingPageData(
        title = "Xếp Khối Thông Minh 🧩",
        subtitle = "Kéo thả các khối gạch nhiều hình dạng vào bàn cờ 8x8 để lấp đầy khoảng trống và ghi điểm cực cao.",
        buttonText = "Tiếp tục"
    ),
    OnboardingPageData(
        title = "Bùng Nổ Điểm Số 🔥",
        subtitle = "Xóa nhiều hàng và cột cùng lúc để kích hoạt hiệu ứng Combo nhân đôi điểm số cùng hiệu ứng rực rỡ.",
        buttonText = "Tiếp tục"
    ),
    OnboardingPageData(
        title = "Vượt Cấp & Đổi Theme 🎨",
        subtitle = "Chinh phục hàng trăm Cấp độ thử thách và tùy chỉnh giao diện Neon, Wood, Ocean độc đáo.",
        buttonText = "Bắt đầu ngay 🚀"
    )
)

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header (Skip Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage < ONBOARDING_PAGES.lastIndex) {
                        TextButton(onClick = onOnboardingFinished) {
                            Text(
                                text = "Bỏ qua",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }

                // Page Animated Graphic & Content
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    },
                    label = "OnboardingPageTransition"
                ) { page ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Graphic Area based on page index
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0xFF1E293B))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF334155),
                                    shape = RoundedCornerShape(28.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when (page) {
                                0 -> Page1Animation()
                                1 -> Page2Animation()
                                2 -> Page3Animation()
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Text Content
                        Text(
                            text = ONBOARDING_PAGES[page].title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = ONBOARDING_PAGES[page].subtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                // Bottom Navigation (Dots & Action Button)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ONBOARDING_PAGES.indices.forEach { index ->
                            val isActive = index == currentPage
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(if (isActive) 24.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isActive) Color(0xFF38BDF8) else Color(0xFF334155)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Next / Start Button
                    Button(
                        onClick = {
                            if (currentPage < ONBOARDING_PAGES.lastIndex) {
                                currentPage += 1
                            } else {
                                onOnboardingFinished()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF38BDF8)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = ONBOARDING_PAGES[currentPage].buttonText,
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated Graphic for Page 1: Dropping Blocks into Grid
 */
@Composable
private fun Page1Animation() {
    val block1Anim = remember { Animatable(0f) }
    val block2Anim = remember { Animatable(0f) }
    val block3Anim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            block1Anim.snapTo(0f)
            block2Anim.snapTo(0f)
            block3Anim.snapTo(0f)

            launch { block1Anim.animateTo(1f, spring(dampingRatio = 0.6f)) }
            delay(250)
            launch { block2Anim.animateTo(1f, spring(dampingRatio = 0.6f)) }
            delay(250)
            launch { block3Anim.animateTo(1f, spring(dampingRatio = 0.6f)) }

            delay(1500)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(block1Anim.value)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF5252))
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(block2Anim.value)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFB300))
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(block3Anim.value)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF00BCD4))
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF334155))
            )
        }
    }
}

/**
 * Animated Graphic for Page 2: Glowing Line Clear & Combo
 */
@Composable
private fun Page2Animation() {
    val rowAlpha = remember { Animatable(1f) }
    val comboScale = remember { Animatable(0.6f) }

    LaunchedEffect(Unit) {
        while (true) {
            rowAlpha.snapTo(1f)
            comboScale.snapTo(0.6f)

            delay(600)
            // Flash and fade line
            launch {
                rowAlpha.animateTo(0.2f, tween(300))
            }
            launch {
                comboScale.animateTo(1.2f, spring(dampingRatio = 0.5f))
            }
            delay(1200)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Full glowing line
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .alpha(rowAlpha.value)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF4CAF50))
                )
            }
        }

        // Combo Badge
        Box(
            modifier = Modifier
                .scale(comboScale.value)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFF5252), Color(0xFFFF9800))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "2x COMBO! 🔥",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Animated Graphic for Page 3: Progress Bar & Themes
 */
@Composable
private fun Page3Animation() {
    val progress = remember { Animatable(0.2f) }
    val themePulse = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            progress.snapTo(0.2f)
            launch {
                progress.animateTo(1.0f, tween(1200, easing = FastOutSlowInEasing))
            }
            launch {
                themePulse.animateTo(
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            delay(2000)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "LEVEL 5 TARGET",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress.value },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = Color(0xFF38BDF8),
            trackColor = Color(0xFF334155),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Themes palette icons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Neon Theme
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .scale(themePulse.value)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD54F))
            )
            // Wood Theme
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8D6E63))
            )
            // Ocean Theme
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF64FFDA))
            )
        }
    }
}
