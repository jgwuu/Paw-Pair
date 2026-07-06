package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun BubblePopGame(
    onGameFinished: (Int, Int) -> Unit,
    onClose: () -> Unit
) {
    var bubbles by remember { mutableStateOf((0..15).map { (0..3).random() }) }
    val icons = listOf("🫧", "🧼", "💎", "⭐")
    val colors = listOf(Color(0xFFE1F5FE), Color(0xFFF3E5F5), Color(0xFFE8F5E9), Color(0xFFFFF9C4))
    
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(20) }
    var gameOver by remember { mutableStateOf(false) }

    LaunchedEffect(timeLeft) {
        while (timeLeft > 0 && !gameOver) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft <= 0) gameOver = true
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
                Text("🫧 Explota Burbujas PawPair", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Text("¡Explota tantas burbujas de jabón como puedas en 20 segundos!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Text("⏱️ Tiempo: ${timeLeft}s", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (timeLeft < 5) Color.Red else Color.Unspecified)
                Text("⭐ Puntos: $score", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (!gameOver) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(260.dp)
                ) {
                    itemsIndexed(bubbles) { index, type ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors[type])
                                .clickable {
                                    score += 5
                                    val current = bubbles.toMutableList()
                                    current[index] = (0..3).random()
                                    bubbles = current
                                }
                        ) {
                            Text(icons[type], fontSize = 28.sp)
                        }
                    }
                }
            } else {
                Text("⏰ ¡Tiempo Agotado!", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Explotaste burbujas para $score puntos", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val coins = (score * 2).coerceAtMost(100)
                        onGameFinished(coins, score)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎁 Reclamar Recompensa", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
