package com.jgwuu.pawpair.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SimonSaysGame(
    onGameFinished: (Int, Int) -> Unit,
    onClose: () -> Unit
) {
    val colors = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFBC02D))
    val icons = listOf("🔴", "🔵", "🟢", "🟡")
    
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var playerIndex by remember { mutableStateOf(0) }
    var isShowingSequence by remember { mutableStateOf(false) }
    var activeFlashIndex by remember { mutableStateOf(-1) }
    var score by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    // Start next level
    LaunchedEffect(sequence.size) {
        if (sequence.isNotEmpty()) {
            isShowingSequence = true
            playerIndex = 0
            delay(500)
            for (index in sequence) {
                activeFlashIndex = index
                delay(600)
                activeFlashIndex = -1
                delay(250)
            }
            isShowingSequence = false
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
                Text("🎵 Simón Dice PawPair", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Text("Repite la secuencia de colores en orden", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Nivel: ${sequence.size} | Puntos: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))

            if (sequence.isEmpty() && !gameOver) {
                Button(
                    onClick = { sequence = listOf((0..3).random()) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🚀 Comenzar Juego", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else if (gameOver) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💥 ¡Incorrecto! Fin del juego.", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val coins = (score * 5).coerceAtMost(100)
                            onGameFinished(coins, score)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🎁 Reclamar ${score * 5} Monedas", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text(
                    text = if (isShowingSequence) "👀 Observa la secuencia..." else "👉 ¡Tu turno! Toca en orden",
                    fontWeight = FontWeight.Bold,
                    color = if (isShowingSequence) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp)
                ) {
                    itemsIndexed(colors) { index, color ->
                        val isFlashing = activeFlashIndex == index
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isFlashing) color else color.copy(alpha = 0.35f))
                                .border(
                                    width = if (isFlashing) 4.dp else 1.dp,
                                    color = if (isFlashing) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable(enabled = !isShowingSequence && !gameOver) {
                                    activeFlashIndex = index
                                    if (sequence[playerIndex] == index) {
                                        playerIndex++
                                        if (playerIndex >= sequence.size) {
                                            score++
                                            sequence = sequence + (0..3).random()
                                        }
                                    } else {
                                        gameOver = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(icons[index], fontSize = 36.sp)
                        }
                    }
                }
            }
        }
    }
}
