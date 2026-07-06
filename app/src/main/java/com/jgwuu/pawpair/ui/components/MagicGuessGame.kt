package com.jgwuu.pawpair.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagicGuessGame(
    onGameFinished: (coinsEarned: Int, xpEarned: Int) -> Unit,
    onBack: () -> Unit
) {
    var targetNumber by remember { mutableStateOf(Random.nextInt(1, 101)) }
    var userGuess by remember { mutableStateOf("") }
    var hintMessage by remember { mutableStateOf("Estoy pensando un número secreto entre 1 y 100. ¡Adivina cuál es! 🤔🔮") }
    var attempts by remember { mutableStateOf(0) }
    val maxAttempts = 7
    var gameWon by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    val guessHistory = remember { mutableStateListOf<Pair<Int, String>>() }

    fun checkGuess() {
        val num = userGuess.toIntOrNull()
        if (num == null || num !in 1..100) {
            hintMessage = "⚠️ Por favor ingresa un número válido entre 1 y 100."
            return
        }
        attempts++
        userGuess = ""

        if (num == targetNumber) {
            gameWon = true
            gameOver = true
            hintMessage = "🎉 ¡BRUTAL! ¡Adivinaste el número mágico $targetNumber en $attempts intentos! 🏆✨"
        } else if (attempts >= maxAttempts) {
            gameOver = true
            hintMessage = "💥 ¡Te quedaste sin intentos! El número mágico era $targetNumber. 🔮"
        } else if (num < targetNumber) {
            hintMessage = "📈 ¡Más alto! El número es MAYOR que $num."
            guessHistory.add(0, num to "Mayor 📈")
        } else {
            hintMessage = "📉 ¡Más bajo! El número es MENOR que $num."
            guessHistory.add(0, num to "Menor 📉")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adivina el Número 🔮", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4C1D95),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2E1065))
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pet Crystal Ball Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4C1D95)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔮🐶", fontSize = 54.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        hintMessage,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Intentos: $attempts / $maxAttempts", color = Color(0xFFE9D5FF), fontWeight = FontWeight.Bold)
                        val remaining = maxAttempts - attempts
                        Text("Te quedan: $remaining 🎯", color = if (remaining <= 2) Color(0xFFF87171) else Color(0xFF34D399), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!gameOver) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = userGuess,
                        onValueChange = { if (it.length <= 3) userGuess = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Número (1-100)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFA855F7),
                            unfocusedBorderColor = Color(0xFF7E22CE),
                            focusedLabelColor = Color(0xFFD8B4FE),
                            unfocusedLabelColor = Color(0xFFC084FC)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { checkGuess() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("¡Adivinar! ✨", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Historial de Pistas:", color = Color(0xFFD8B4FE), fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    guessHistory.forEach { (num, hint) ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0764))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Dijiste: $num", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Pista: $hint", color = Color(0xFFF0ABFC), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (gameWon) Color(0xFF065F46) else Color(0xFF7F1D1D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (gameWon) "🏆 ¡VICTORIA MÁGICA!" else "💥 FIN DE JUEGO",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val rewardCoins = if (gameWon) (80 - attempts * 8).coerceAtLeast(30) else 15
                        val rewardXp = if (gameWon) (60 - attempts * 5).coerceAtLeast(20) else 10
                        Text("Recompensa: +$rewardCoins Monedas 🪙 | +$rewardXp XP ✨", color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { onGameFinished(rewardCoins, rewardXp) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reclamar Recompensa 🎁", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
