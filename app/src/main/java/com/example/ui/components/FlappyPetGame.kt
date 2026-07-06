package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun FlappyPetGame(
    onGameFinished: (Int, Int) -> Unit,
    onClose: () -> Unit
) {
    var petY by remember { mutableFloatStateOf(0.5f) }
    var velocity by remember { mutableFloatStateOf(0f) }
    var obstacleX by remember { mutableFloatStateOf(1.0f) }
    var obstacleGapY by remember { mutableFloatStateOf(0.5f) }
    var isRunning by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRunning) {
        while (isRunning && !gameOver) {
            delay(30)
            velocity += 0.006f // gravity
            petY += velocity

            obstacleX -= 0.025f
            if (obstacleX < -0.2f) {
                obstacleX = 1.0f
                obstacleGapY = (20..70).random() / 100f
                score++
            }

            // Collision checks
            if (petY < 0f || petY > 1.0f) {
                gameOver = true
                isRunning = false
            }
            if (obstacleX in 0.15f..0.35f) {
                if (petY < obstacleGapY - 0.15f || petY > obstacleGapY + 0.15f) {
                    gameOver = true
                    isRunning = false
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🕊️ Flappy Pet PawPair", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Text("¡Toca la pantalla para volar y esquivar los obstáculos!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Puntuación: $score", fontWeight = FontWeight.Black, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF81D4FA))
                    .clickable {
                        if (!isRunning && !gameOver) {
                            isRunning = true
                        } else if (isRunning) {
                            velocity = -0.04f // jump
                        }
                    }
            ) {
                // Obstacle Top
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (obstacleX * 300).dp)
                        .width(44.dp)
                        .fillMaxHeight((obstacleGapY - 0.15f).coerceAtLeast(0f))
                        .background(Color(0xFF388E3C), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                )
                // Obstacle Bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (obstacleX * 300).dp)
                        .width(44.dp)
                        .fillMaxHeight((1f - obstacleGapY - 0.15f).coerceAtLeast(0f))
                        .background(Color(0xFF388E3C), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                )

                // Pet character
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 60.dp, y = (petY * 220).dp)
                        .size(40.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐶", fontSize = 24.sp)
                }

                if (!isRunning && !gameOver) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.9f)) {
                            Text("👆 Toca aquí para volar", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (gameOver) {
                Text("💥 ¡Chocaste!", color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val coins = (score * 6).coerceAtMost(100)
                        onGameFinished(coins, score)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🎁 Reclamar ${score * 6} Monedas", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
