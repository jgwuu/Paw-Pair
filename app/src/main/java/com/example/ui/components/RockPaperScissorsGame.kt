package com.example.ui.components

import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

enum class Element(val label: String, val emoji: String, val color: Color) {
    FIRE("Fuego", "🔥", Color(0xFFEF4444)),
    WATER("Agua", "💧", Color(0xFF3B82F6)),
    PLANT("Planta", "🌿", Color(0xFF10B981))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RockPaperScissorsGame(
    onGameFinished: (coinsEarned: Int, xpEarned: Int) -> Unit,
    onBack: () -> Unit
) {
    var playerScore by remember { mutableStateOf(0) }
    var petScore by remember { mutableStateOf(0) }
    var roundResult by remember { mutableStateOf("¡Elige un elemento mágico para batallar contra tu mascota!") }
    var playerChoice by remember { mutableStateOf<Element?>(null) }
    var petChoice by remember { mutableStateOf<Element?>(null) }
    var gameOver by remember { mutableStateOf(false) }

    fun playRound(choice: Element) {
        if (gameOver) return
        playerChoice = choice
        val aiChoice = Element.values()[Random.nextInt(Element.values().size)]
        petChoice = aiChoice

        if (choice == aiChoice) {
            roundResult = "¡Empate! Ambos eligieron ${choice.emoji} ${choice.label}."
        } else if (
            (choice == Element.FIRE && aiChoice == Element.PLANT) ||
            (choice == Element.WATER && aiChoice == Element.FIRE) ||
            (choice == Element.PLANT && aiChoice == Element.WATER)
        ) {
            playerScore++
            roundResult = "¡Ganaste la ronda! ${choice.emoji} vence a ${aiChoice.emoji}."
            if (playerScore >= 3) {
                gameOver = true
                roundResult = "🏆 ¡VICTORIA TOTAL! Venciste a tu mascota en el duelo elemental."
            }
        } else {
            petScore++
            roundResult = "¡Perdiste la ronda! ${aiChoice.emoji} vence a ${choice.emoji}."
            if (petScore >= 3) {
                gameOver = true
                roundResult = "💥 Tu mascota ganó el duelo elemental. ¡Sigue practicando!"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Duelo Elemental 🔥💧🌿", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF111827),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111827))
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scoreboard Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TÚ 👤", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                        Text("$playerScore", color = Color(0xFF10B981), fontSize = 32.sp, fontWeight = FontWeight.Black)
                    }
                    Text("VS", color = Color(0xFFF59E0B), fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MASCOTA 🐶", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                        Text("$petScore", color = Color(0xFFEF4444), fontSize = 32.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Arena Display
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF374151)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(playerChoice?.color?.copy(alpha = 0.2f) ?: Color(0xFF4B5563))
                                .border(2.dp, playerChoice?.color ?: Color.Transparent, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(playerChoice?.emoji ?: "❓", fontSize = 44.sp)
                        }
                        Text("⚡", fontSize = 32.sp)
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(petChoice?.color?.copy(alpha = 0.2f) ?: Color(0xFF4B5563))
                                .border(2.dp, petChoice?.color ?: Color.Transparent, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(petChoice?.emoji ?: "🐶", fontSize = 44.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        roundResult,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!gameOver) {
                Text("Elige tu Elemento:", color = Color(0xFFD1D5DB), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Element.values().forEach { elem ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = elem.color),
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .clickable { playRound(elem) }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(elem.emoji, fontSize = 28.sp)
                                Text(elem.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (playerScore >= 3) Color(0xFF059669) else Color(0xFFDC2626)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val coins = if (playerScore >= 3) 65 else 20
                        val xp = if (playerScore >= 3) 50 else 15
                        Text("Recompensa del Duelo: +$coins 🪙 | +$xp XP ✨", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onGameFinished(coins, xp) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Reclamar Recompensas 🎁", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
