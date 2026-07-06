package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Refresh
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
import kotlinx.coroutines.launch

@Composable
fun TicTacToeGameView(
    petType: String,
    petName: String,
    onGameFinished: (coins: Int, score: Int) -> Unit,
    onClose: () -> Unit
) {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var gameActive by remember { mutableStateOf(true) }
    var gameResultStatus by remember { mutableStateOf("¡Tu turno! Haz tap en una casilla vacía.") }
    var petReactionEmoji by remember { mutableStateOf("🧠") }
    var movesCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Determine state of game
    fun checkWin(b: List<String>): String? {
        val ways = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Cols
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diags
        )
        for (w in ways) {
            if (b[w[0]].isNotEmpty() && b[w[0]] == b[w[1]] && b[w[1]] == b[w[2]]) {
                return b[w[0]]
            }
        }
        if (b.none { it.isEmpty() }) return "TIE"
        return null
    }

    fun makePetMove() {
        if (!gameActive) return
        coroutineScope.launch {
            gameResultStatus = "¡$petName está pensando su jugada...!"
            petReactionEmoji = "🤔"
            delay(1000) // Feel natural

            // Look for winning move for Pet ("O")
            var chosenIndex = -1
            for (i in 0..8) {
                if (board[i].isEmpty()) {
                    val testBoard = board.toMutableList()
                    testBoard[i] = "O"
                    if (checkWin(testBoard) == "O") { chosenIndex = i; break }
                }
            }

            // Look to block Player winning move ("X")
            if (chosenIndex == -1) {
                for (i in 0..8) {
                    if (board[i].isEmpty()) {
                        val testBoard = board.toMutableList()
                        testBoard[i] = "X"
                        if (checkWin(testBoard) == "X") { chosenIndex = i; break }
                    }
                }
            }

            // Fallback to center, corners or random empty
            if (chosenIndex == -1) {
                val availableCoords = board.indices.filter { board[it].isEmpty() }
                if (availableCoords.isNotEmpty()) {
                    chosenIndex = when {
                        4 in availableCoords -> 4
                        else -> availableCoords.random()
                    }
                }
            }

            if (chosenIndex != -1) {
                val newBoard = board.toMutableList()
                newBoard[chosenIndex] = "O"
                board = newBoard
                movesCount++

                val winner = checkWin(board)
                if (winner != null) {
                    gameActive = false
                    if (winner == "O") {
                        gameResultStatus = "¡$petName ganó esta ronda! 🏆 (+10 monedas)"
                        petReactionEmoji = "😆🎉"
                    } else {
                        gameResultStatus = "¡Empate técnico! 🤝 (+35 monedas)"
                        petReactionEmoji = "🙂"
                    }
                } else {
                    isPlayerTurn = true
                    gameResultStatus = "¡Tu turno! Supera la estrategia de $petName."
                    petReactionEmoji = "👀"
                }
            }
        }
    }

    fun makePlayerMove(index: Int) {
        if (!gameActive || !isPlayerTurn || board[index].isNotEmpty()) return
        val newBoard = board.toMutableList()
        newBoard[index] = "X"
        board = newBoard
        movesCount++

        val winner = checkWin(board)
        if (winner != null) {
            gameActive = false
            if (winner == "X") {
                gameResultStatus = "¡Ganaste la partida! 🎉 (+80 monedas)"
                petReactionEmoji = "😢"
            } else {
                gameResultStatus = "¡Empate técnico! 🤝 (+35 monedas)"
                petReactionEmoji = "🙂"
            }
        } else {
            isPlayerTurn = false
            makePetMove()
        }
    }

    fun resetGame() {
        board = List(9) { "" }
        isPlayerTurn = true
        gameActive = true
        movesCount = 0
        gameResultStatus = "¡Tu turno! Haz tap en una casilla vacía."
        petReactionEmoji = "🧠"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBFA))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                "Tres en Raya PawPair 🎮",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { resetGame() }, enabled = gameActive) {
                Icon(Icons.Filled.Refresh, "Reiniciar", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Live Pet reactions section
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    PetRenderer(
                        type = petType,
                        isSleeping = false,
                        hunger = 95f,
                        happiness = if (gameResultStatus.contains("Ganaste")) 30f else 95f,
                        equippedHat = null,
                        modifier = Modifier.size(54.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$petName dice:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Pensando... $petReactionEmoji Jugadas: $movesCount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Main Board grid (3x3)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0..2) {
                            val index = row * 3 + col
                            val mark = board[index]

                            // Dynamic cell style
                            val cellColor = when (mark) {
                                "X" -> Color(0xFFFCE2E3) // Pastel Soft Red
                                "O" -> Color(0xFFD5ECD0) // Pastel Soft Mint
                                else -> Color(0xFFF9F7FA) // Standard soft pastel grey
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cellColor)
                                    .clickable(enabled = gameActive && board[index].isEmpty()) {
                                        makePlayerMove(index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mark,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when (mark) {
                                        "X" -> Color(0xFFD32F2F)
                                        "O" -> Color(0xFF388E3C)
                                        else -> Color.Transparent
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Match outcome status bar
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    gameResultStatus.contains("Ganaste") -> Color(0xFFE8F5E9)
                    gameResultStatus.contains("ganó") -> Color(0xFFFFEBEE)
                    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = gameResultStatus,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                color = when {
                    gameResultStatus.contains("Ganaste") -> Color(0xFF2E7D32)
                    gameResultStatus.contains("ganó") -> Color(0xFFC62828)
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
        }

        // Action controls (Finish / Recoger Monedas or Reset check)
        Button(
            onClick = {
                val winner = checkWin(board)
                val coinReward = when (winner) {
                    "X" -> 80
                    "O" -> 10
                    "TIE" -> 35
                    else -> 0
                }
                onGameFinished(coinReward, if (winner == "X") 200 else if (winner == "TIE") 80 else 30)
            },
            enabled = !gameActive,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                if (gameActive) "Termina la Partida Primero" else "¡Reclamar Recompensa y Salir! 💰",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
