package com.example.npm_b01

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.npm_b01.ui.AnimatedSplashScreen
import com.example.npm_b01.ui.BlockPuzzleGame
import com.example.npm_b01.ui.HomeScreen
import com.example.npm_b01.ui.OnboardingScreen
import com.example.npm_b01.ui.theme.NPM_B01Theme
import com.example.npm_b01.viewmodel.BlockPuzzleViewModel

enum class ScreenState {
    SPLASH,
    ONBOARDING,
    HOME,
    GAME
}

class MainActivity : ComponentActivity() {
    private val viewModel: BlockPuzzleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("block_puzzle_prefs", Context.MODE_PRIVATE)

        setContent {
            NPM_B01Theme {
                var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }
                val state by viewModel.uiState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Crossfade(
                            targetState = currentScreen,
                            animationSpec = tween(500),
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                // 1. Splash Screen
                                ScreenState.SPLASH -> AnimatedSplashScreen(
                                    onSplashFinished = {
                                        val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
                                        currentScreen = if (!hasSeenOnboarding) {
                                            ScreenState.ONBOARDING
                                        } else {
                                            ScreenState.HOME
                                        }
                                    }
                                )
                                // 2. Onboarding Screen
                                ScreenState.ONBOARDING -> OnboardingScreen(
                                    onOnboardingFinished = {
                                        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                                        currentScreen = ScreenState.HOME
                                    }
                                )
                                // 3. Home Screen (Mode Selection)
                                ScreenState.HOME -> HomeScreen(
                                    highScore = state.highScore,
                                    totalGames = state.totalGamesPlayed,
                                    totalLines = state.totalLinesCleared,
                                    onSelectFreePlay = {
                                        viewModel.startFreePlay()
                                        currentScreen = ScreenState.GAME
                                    },
                                    onSelectPresetChallenge = {
                                        viewModel.startPresetChallenge()
                                        currentScreen = ScreenState.GAME
                                    }
                                )
                                // 4. Main Game Screen
                                ScreenState.GAME -> BlockPuzzleGame(
                                    viewModel = viewModel,
                                    onGoHome = { currentScreen = ScreenState.HOME }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    NPM_B01Theme {
        AnimatedSplashScreen(onSplashFinished = {})
    }
}
