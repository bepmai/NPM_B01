package com.example.npm_b01.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.npm_b01.i18n.AppLanguage
import com.example.npm_b01.i18n.AppStrings
import com.example.npm_b01.model.GameTheme

@Composable
fun HomeScreen(
    highScore: Int,
    totalGames: Int,
    totalLines: Int,
    currentLanguage: AppLanguage,
    currentTheme: GameTheme,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (GameTheme) -> Unit,
    onSelectFreePlay: () -> Unit,
    onSelectPresetChallenge: () -> Unit,
    onSelectGravityDrop: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
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
                            text = AppStrings.get("app_title", currentLanguage),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        IconButton(
                            onClick = { showSettings = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Text(text = "⚙️", fontSize = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Best Score Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF1E293B), Color(0xFF334155))
                                )
                            )
                            .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = AppStrings.get("best_score", currentLanguage),
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$highScore",
                                fontSize = 20.sp,
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mode Cards - Clean, Visual & Action-Oriented
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mode 1: Free Play
                    GameCardBanner(
                        title = AppStrings.get("mode_free_title", currentLanguage),
                        badge = AppStrings.get("mode_free_badge", currentLanguage),
                        icon = "🎮",
                        gradient = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
                        onClick = onSelectFreePlay
                    )

                    // Mode 2: Preset Puzzle Challenge
                    GameCardBanner(
                        title = AppStrings.get("mode_preset_title", currentLanguage),
                        badge = AppStrings.get("mode_preset_badge", currentLanguage),
                        icon = "🧩",
                        gradient = listOf(Color(0xFFD97706), Color(0xFFB45309)),
                        onClick = onSelectPresetChallenge
                    )

                    // Mode 3: Gravity Drop
                    GameCardBanner(
                        title = AppStrings.get("mode_gravity_title", currentLanguage),
                        badge = AppStrings.get("mode_gravity_badge", currentLanguage),
                        icon = "⏬",
                        gradient = listOf(Color(0xFF16A34A), Color(0xFF15803D)),
                        onClick = onSelectGravityDrop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Compact Footer Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎯 ", fontSize = 16.sp)
                        Column {
                            Text(
                                text = AppStrings.get("played", currentLanguage),
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$totalGames ${AppStrings.get("games_count", currentLanguage)}",
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF334155)))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔥 ", fontSize = 16.sp)
                        Column {
                            Text(
                                text = AppStrings.get("cleared", currentLanguage),
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$totalLines ${AppStrings.get("lines_count", currentLanguage)}",
                                fontSize = 13.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Settings Dialog
            AnimatedVisibility(
                visible = showSettings,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                SettingsDialog(
                    currentLanguage = currentLanguage,
                    currentTheme = currentTheme,
                    onLanguageChange = onLanguageChange,
                    onThemeChange = onThemeChange,
                    onClose = { showSettings = false }
                )
            }
        }
    }
}

@Composable
private fun GameCardBanner(
    title: String,
    badge: String,
    icon: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(95.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradient))
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 24.sp)
                    }

                    Column {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Play Arrow
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "➔", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
