package com.example.npm_b01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.npm_b01.ui.AnimatedSplashScreen
import com.example.npm_b01.ui.BlockPuzzleGame
import com.example.npm_b01.ui.theme.NPM_B01Theme

enum class ScreenState {
    SPLASH,
    GAME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NPM_B01Theme {
                var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Crossfade(
                            targetState = currentScreen,
                            animationSpec = tween(500),
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                ScreenState.SPLASH -> AnimatedSplashScreen(
                                    onSplashFinished = { currentScreen = ScreenState.GAME }
                                )
                                ScreenState.GAME -> BlockPuzzleGame()
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
