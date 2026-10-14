package com.example.npm_b01.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    highScore: Int,
    totalGames: Int,
    totalLines: Int,
    onSelectFreePlay: () -> Unit,
    onSelectPresetChallenge: () -> Unit,
    onSelectGravityDrop: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "BLOCK PUZZLE 🧩",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                // High Score Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆 BEST HIGH SCORE: ", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text(text = "$highScore", fontSize = 16.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Options Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Mode 1: Free Play Card
                ModeSelectionCard(
                    title = "🎮 CHƠI TỰ DO",
                    badge = "Kinh Điển",
                    badgeColor = Color(0xFF00BCD4),
                    description = "Bàn cờ trống hoàn toàn. Kéo thả tự do, tích combo & thử thách điểm cao không giới hạn.",
                    gradient = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
                    onClick = onSelectFreePlay
                )

                // Mode 2: Preset Puzzle Card
                ModeSelectionCard(
                    title = "🧩 XẾP HÌNH BÀN CỜ",
                    badge = "Thử Thách",
                    badgeColor = Color(0xFFFF9800),
                    description = "Bàn cờ có sẵn một số khối gạch vật cản. Lựa chọn và tính toán các khối bên dưới để giải đố.",
                    gradient = listOf(Color(0xFFD97706), Color(0xFFB45309)),
                    onClick = onSelectPresetChallenge
                )

                // Mode 3: Gravity Drop Mode Card
//                ModeSelectionCard(
//                    title = "⏬ THẢ KHỐI RƠI (GRAVITY)",
//                    badge = "Rơi Tự Do",
//                    badgeColor = Color(0xFF4CAF50),
//                    description = "Khối gạch rơi từ trên xuống. Di chuyển trái/phải và thả rơi để lấp đầy các ô bên dưới.",
//                    gradient = listOf(Color(0xFF16A34A), Color(0xFF15803D)),
//                    onClick = onSelectGravityDrop
//                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Quick Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "ĐÃ CHƠI", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = "$totalGames ván", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFF334155)))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "ĐÃ XÓA", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = "$totalLines hàng", fontSize = 14.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ModeSelectionCard(
    title: String,
    badge: String,
    badgeColor: Color,
    description: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(gradient), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VÀO CHƠI ➔",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
