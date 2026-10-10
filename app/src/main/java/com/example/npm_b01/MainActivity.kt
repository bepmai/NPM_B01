package com.example.npm_b01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.npm_b01.ui.BlockPuzzleGame
import com.example.npm_b01.ui.theme.NPM_B01Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NPM_B01Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        BlockPuzzleGame()
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
        BlockPuzzleGame()
    }
}
