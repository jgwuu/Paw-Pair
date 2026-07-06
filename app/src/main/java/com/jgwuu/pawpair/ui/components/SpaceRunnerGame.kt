package com.jgwuu.pawpair.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

data class Obstacle(val id: Long, val lane: Int, var y: Float, val isReward: Boolean, val symbol: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceRunnerGame(
    onGameFinished: (coinsEarned: Int, xpEarned: Int) -> Unit,
    onBack: () -> Unit
) {
    var petLane by remember { mutableStateOf(1) } // 0: Left, 1: Center, 2: Right
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var gameOver by remember { mutableStateOf(false) }
    val obstacles = remember { mutableStateListOf<Obstacle>() }
    var gameSpeed by remember { mutableStateOf(0.02f) }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            var tick = 0L
            while (!gameOver && lives > 0) {
                delay(30L)
                tick++

                // Spawn new obstacles or stars
                if (tick % 25 == 0L) {
                    val lane = Random.nextInt(3)
                    val isReward = Random.nextFloat() > 0.6f
                    val symbol = if (isReward) "⭐" else "☄️"
                    obstacles.add(Obstacle(System.currentTimeMillis() + tick, lane, 0f, isReward, symbol))
                }

                // Move obstacles down
                val toRemove = mutableListOf<Obstacle>()
                for (obs in obstacles) {
                    obs.y += gameSpeed

                    // Collision check with player at y ~ 0.8f
                    if (obs.y in 0.75f..0.88f && obs.lane == petLane) {
                        if (obs.isReward) {
                            score += 15
                        } else {
                            lives--
                            if (lives <= 0) {
                                gameOver = true
                            }
                        }
                        toRemove.add(obs)
                    } else if (obs.y > 1.1f) {
                        if (!obs.isReward) score += 5 // dodged successfully
                        toRemove.add(obs)
                    }
                }
                obstacles.removeAll(toRemove)
                gameSpeed = (0.02f + (score / 300f) * 0.015f).coerceAtMost(0.06f)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrera Espacial 🚀", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Puntuación: $score ⭐", color = Color(0xFF38BDF8), fontWeight = FontWeight.Black, fontSize = 18.sp)
                Row {
                    repeat(lives) {
                        Text("❤️", fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Track
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF020617))
                    .border(2.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            ) {
                // Draw lanes and obstacles
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val laneWidth = size.width / 3
                    // Draw vertical lane dividers
                    drawLine(Color(0xFF1E293B), Offset(laneWidth, 0f), Offset(laneWidth, size.height), strokeWidth = 2f)
                    drawLine(Color(0xFF1E293B), Offset(laneWidth * 2, 0f), Offset(laneWidth * 2, size.height), strokeWidth = 2f)
                }

                // Render falling items
                obstacles.forEach { obs ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp)
                    ) {
                        val xFraction = when (obs.lane) {
                            0 -> 0.16f
                            1 -> 0.5f
                            else -> 0.84f
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = (300.dp * xFraction) - 16.dp,
                                    y = (500.dp * obs.y)
                                )
                        ) {
                            Text(obs.symbol, fontSize = 28.sp)
                        }
                    }
                }

                // Render Pet at lane
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    val alignX = when (petLane) {
                        0 -> Alignment.BottomStart
                        1 -> Alignment.BottomCenter
                        else -> Alignment.BottomEnd
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        contentAlignment = alignX
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF38BDF8))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐶🚀", fontSize = 26.sp)
                        }
                    }
                }

                if (gameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text("💥 ¡FIN DE CARRERA! 🚀", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Puntuación Final: $score ⭐", fontSize = 18.sp, color = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    val coins = (score / 2).coerceAtLeast(15)
                                    val xp = (score / 3).coerceAtLeast(10)
                                    onGameFinished(coins, xp)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Reclamar Recompensas 🎁", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { if (petLane > 0) petLane-- },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Text("⬅️ Izquierda", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { if (petLane < 2) petLane++ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Text("Derecha ➡️", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
