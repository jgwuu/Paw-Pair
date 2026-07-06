package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WordSearchPuzzleData(
    val grid: List<List<Char>>,
    val placedWords: List<String>,
    val wordCoords: Map<String, List<Pair<Int, Int>>>,
    val themeName: String
)

fun generateRandomWordSearchPuzzle(gridSize: Int = 8, wordCount: Int = 5): WordSearchPuzzleData {
    val themes = listOf(
        "Sabores Gourmet 🍽️" to listOf("AREPA", "PATACON", "SUERO", "QUESO", "YUCA", "COCO", "MOTE", "FRITO"),
        "Mascotas PawPair 🐾" to listOf("SHIBA", "SLIME", "KITTY", "DRACO", "AXOLOTL", "PASEO", "JUEGO", "CUIDADO", "AMOR"),
        "Paseos y Sol ☀️" to listOf("CAMPO", "PLAYA", "BRISA", "ARENA", "PARQUE", "MAR", "SOL", "OLA"),
        "Aventuras 🎮" to listOf("AMIGO", "FAMILIA", "NIVEL", "MONEDA", "PREMIO", "MAGIA", "PODER", "TESORO"),
        "Naturaleza 🌿" to listOf("PERRO", "GATO", "LORO", "JARDIN", "FLOR", "BOSQUE", "RIO", "MONO")
    )
    val chosenTheme = themes.random()
    val themeName = chosenTheme.first
    val wordBank = chosenTheme.second
    val shuffledBank = wordBank.filter { it.length <= gridSize }.shuffled()
    val selectedWords = mutableListOf<String>()
    val wordCoords = mutableMapOf<String, List<Pair<Int, Int>>>()
    val grid = Array(gridSize) { CharArray(gridSize) { ' ' } }

    val directions = listOf(
        0 to 1,   // horizontal right
        0 to -1,  // horizontal left
        1 to 0,   // vertical down
        -1 to 0,  // vertical up
        1 to 1,   // diagonal down-right
        -1 to -1, // diagonal up-left
        1 to -1,  // diagonal down-left
        -1 to 1   // diagonal up-right
    )

    for (word in shuffledBank) {
        if (selectedWords.size >= wordCount) break
        var placed = false
        val shuffledDirs = directions.shuffled()
        for (dir in shuffledDirs) {
            if (placed) break
            val (dr, dc) = dir
            val allStarts = (0 until gridSize).flatMap { r ->
                (0 until gridSize).map { c -> r to c }
            }.shuffled()

            for ((sr, sc) in allStarts) {
                val endR = sr + dr * (word.length - 1)
                val endC = sc + dc * (word.length - 1)
                if (endR !in 0 until gridSize || endC !in 0 until gridSize) continue

                var canPlace = true
                val coords = mutableListOf<Pair<Int, Int>>()
                for (i in word.indices) {
                    val r = sr + dr * i
                    val c = sc + dc * i
                    if (grid[r][c] != ' ' && grid[r][c] != word[i]) {
                        canPlace = false
                        break
                    }
                    coords.add(r to c)
                }

                if (canPlace) {
                    for (i in word.indices) {
                        val (r, c) = coords[i]
                        grid[r][c] = word[i]
                    }
                    selectedWords.add(word)
                    wordCoords[word] = coords
                    placed = true
                    break
                }
            }
        }
    }

    val alphabet = ('A'..'Z').toList()
    val finalGrid = List(gridSize) { r ->
        List(gridSize) { c ->
            if (grid[r][c] == ' ') alphabet.random() else grid[r][c]
        }
    }
    return WordSearchPuzzleData(finalGrid, selectedWords, wordCoords, themeName)
}

@Composable
fun WordSearchGameView(
    petType: String,
    petName: String,
    onGameFinished: (coins: Int, score: Int) -> Unit,
    onClose: () -> Unit
) {
    var puzzleSeed by remember { mutableIntStateOf(0) }
    val puzzle = remember(puzzleSeed) { generateRandomWordSearchPuzzle(8, 5) }
    val targetWords = puzzle.placedWords
    val grid = puzzle.grid
    
    // Found words state
    val foundWords = remember(puzzleSeed) { mutableStateListOf<String>() }
    
    // Highlighted coordinates (row, col) representing current selection path
    val currentSelection = remember(puzzleSeed) { mutableStateListOf<Pair<Int, Int>>() }

    // Permanently solved coordinates
    val solvedCoordinates = remember(puzzleSeed) { mutableStateListOf<Pair<Int, Int>>() }

    var score by remember(puzzleSeed) { mutableIntStateOf(0) }
    var coinsEarned by remember(puzzleSeed) { mutableIntStateOf(0) }
    var gameOver by remember(puzzleSeed) { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var boardWidth by remember { mutableStateOf(0f) }
    var boardHeight by remember { mutableStateOf(0f) }
    val startDragCell = remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Constructed current selected word
    val constructedWord = currentSelection.map { (r, c) -> grid[r][c] }.joinToString("")

    fun verifySelection(clearOnFail: Boolean = true) {
        val currentWord = currentSelection.map { (r, c) -> grid[r][c] }.joinToString("")
        val wordUpper = currentWord.uppercase()
        val wordReversed = wordUpper.reversed()
        val foundWord = when {
            targetWords.contains(wordUpper) && !foundWords.contains(wordUpper) -> wordUpper
            targetWords.contains(wordReversed) && !foundWords.contains(wordReversed) -> wordReversed
            else -> null
        }

        if (foundWord != null) {
            // Found a new word!
            foundWords.add(foundWord)
            solvedCoordinates.addAll(currentSelection)
            currentSelection.clear()
            score += 150
            coinsEarned += 25

            // check if overall game finished
            if (foundWords.size == targetWords.size) {
                score += 300 // completion bonus
                coinsEarned += 50
                gameOver = true
            }
        } else if (clearOnFail) {
            // invalid word - clear selection
            currentSelection.clear()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9)) // Tropical garden background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color(0xFF1B5E20))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Sopa de Letras ✍️🔎",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    "Temática: ${puzzle.themeName}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
            Row {
                IconButton(onClick = { puzzleSeed++ }) {
                    Icon(Icons.Filled.Refresh, "Nueva Sopa", tint = Color(0xFF1B5E20))
                }
                IconButton(onClick = { showHelpDialog = true }) {
                    Icon(Icons.Filled.Info, "Rulebook", tint = Color(0xFF1B5E20))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rewards dashboard
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, Color(0xFF81C784)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PUNTOS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$score", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF1B5E20))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ENCONTRADAS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${foundWords.size} de ${targetWords.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GANANCIA 🪙", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MonetizationOn, "coins", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+$coinsEarned", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFB300))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Word Checklist
        Text(
            "Palabras por encontrar en la Sopa:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            targetWords.forEach { word ->
                val isFound = foundWords.contains(word)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFound) Color(0xFFC8E6C9) else Color.White)
                        .border(1.dp, if (isFound) Color(0xFF4CAF50) else Color(0xFFCFD8DC), RoundedCornerShape(8.dp))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = word,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFound) Color(0xFF2E7D32) else Color.Gray,
                        style = androidx.compose.ui.text.TextStyle(
                            textDecoration = if (isFound) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game Grid Board Container with responsive swipe/drag gestures
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFF2E7D32)),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .onSizeChanged { size ->
                    boardWidth = size.width.toFloat()
                    boardHeight = size.height.toFloat()
                }
                .pointerInput(boardWidth, boardHeight) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (boardWidth > 0 && boardHeight > 0) {
                                val cellW = boardWidth / 8
                                val cellH = boardHeight / 8
                                val col = (offset.x / cellW).toInt().coerceIn(0, 7)
                                val row = (offset.y / cellH).toInt().coerceIn(0, 7)
                                
                                val cell = row to col
                                if (!solvedCoordinates.contains(cell)) {
                                    currentSelection.clear()
                                    currentSelection.add(cell)
                                    startDragCell.value = cell
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val pos = change.position
                            if (boardWidth > 0 && boardHeight > 0) {
                                val cellW = boardWidth / 8
                                val cellH = boardHeight / 8
                                val col = (pos.x / cellW).toInt().coerceIn(0, 7)
                                val row = (pos.y / cellH).toInt().coerceIn(0, 7)
                                val currentCell = row to col
                                val start = startDragCell.value
                                if (start != null && !solvedCoordinates.contains(currentCell)) {
                                    val (sr, sc) = start
                                    val dr = row - sr
                                    val dc = col - sc
                                    if (dr == 0 || dc == 0 || kotlin.math.abs(dr) == kotlin.math.abs(dc)) {
                                        val steps = kotlin.math.max(kotlin.math.abs(dr), kotlin.math.abs(dc))
                                        val stepR = if (steps == 0) 0 else dr / steps
                                        val stepC = if (steps == 0) 0 else dc / steps
                                        val line = (0..steps).map { k -> (sr + k * stepR) to (sc + k * stepC) }
                                        if (line != currentSelection && line.all { !solvedCoordinates.contains(it) }) {
                                            currentSelection.clear()
                                            currentSelection.addAll(line)
                                        }
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            startDragCell.value = null
                            verifySelection()
                        },
                        onDragCancel = {
                            startDragCell.value = null
                            currentSelection.clear()
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (rowIdx in 0..7) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (colIdx in 0..7) {
                            val char = grid[rowIdx][colIdx]
                            val isSelected = currentSelection.contains(rowIdx to colIdx)
                            val isSolved = solvedCoordinates.contains(rowIdx to colIdx)

                            val cellBg = when {
                                isSelected -> Color(0xFFFFCC80) // highlighted amber
                                isSolved -> Color(0xFFA5D6A7)   // solved pale green
                                else -> Color(0xFFF1F8E9)        // default off-white/lime
                            }

                            val cellBorder = when {
                                isSelected -> BorderStroke(1.5.dp, Color(0xFFE65100))
                                isSolved -> BorderStroke(1.5.dp, Color(0xFF2E7D32))
                                else -> BorderStroke(0.5.dp, Color(0xFFCFD8DC))
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellBg)
                                    .border(cellBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val cell = rowIdx to colIdx
                                        if (currentSelection.contains(cell)) {
                                            if (currentSelection.lastOrNull() == cell) {
                                                currentSelection.remove(cell)
                                            }
                                        } else {
                                            val lastCell = currentSelection.lastOrNull()
                                            if (lastCell == null || (kotlin.math.abs(lastCell.first - rowIdx) <= 1 && kotlin.math.abs(lastCell.second - colIdx) <= 1)) {
                                                currentSelection.add(cell)
                                                verifySelection(clearOnFail = false)
                                            } else {
                                                currentSelection.clear()
                                                currentSelection.add(cell)
                                            }
                                        }
                                    }
                                    .testTag("ws_cell_${rowIdx}_${colIdx}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        isSelected -> Color(0xFFE65100)
                                        isSolved -> Color(0xFF1B5E20)
                                        else -> Color(0xFF37474F)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic helper displaying crafted letters
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = BorderStroke(1.dp, Color(0xFFFFB74D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Letras seleccionadas:",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (constructedWord.isEmpty()) "(Toca letras en la sopa para armar la palabra)" else currentSelection.map { (r, c) -> grid[r][c] }.joinToString(" - "),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = if (constructedWord.isEmpty()) Color.LightGray else Color(0xFFE65100),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selection Action Controls (Verify, Reset)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { currentSelection.clear(); startDragCell.value = null },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCFD8DC), contentColor = Color.DarkGray),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Icon(Icons.Filled.Refresh, "Limpiar")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Limpiar 🔄", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { verifySelection() },
                enabled = constructedWord.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    disabledContainerColor = Color(0xFFB0BEC5)
                ),
                modifier = Modifier
                    .weight(1.3f)
                    .height(46.dp)
                    .testTag("ws_verify_button")
            ) {
                Icon(Icons.Filled.CheckCircle, "Verificar")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Comprobar 🔍", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Win popup sheet dialog
        if (gameOver) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    Button(
                        onClick = { onGameFinished(coinsEarned, score) },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Reclamar Monedas 🎉", fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        "¡Felicidades Chef Sopa! 🥳",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("🏆", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "¡Resolviste la Sopa de Letras gourmet de ${petName}!",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, "Score", tint = Color(0xFFFFA000))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Puntuación: $score", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.MonetizationOn, "Coins", tint = Color(0xFFFFB300))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Recompensa: +$coinsEarned Monedas 🪙", fontWeight = FontWeight.Black, color = Color(0xFFFFB300))
                        }
                    }
                }
            )
        }

        // Help Instructions Dialog
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text("¡Entendido! 👍")
                    }
                },
                title = { Text("¿Cómo Jugar? ❓", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• Toca las letras de la sopa para seleccionarlas en orden y deletrear las palabras requeridas.", fontSize = 12.sp)
                        Text("• Si te equivocas, puedes pulsar el botón Limpiar 🔄 para vaciar la selección actual.", fontSize = 12.sp)
                        Text("• Pulsa Comprobar 🔍 para verificar si deletreaste una de las cinco palabras típicas.", fontSize = 12.sp)
                        Text("• Las palabras correctas quedarán resaltadas de verde en el tablero y tachadas en tu lista.", fontSize = 12.sp)
                        Text("• ¡Encuentra las cinco palabras típicas costeñas y colecta monedas para tu Mascota!", fontSize = 12.sp)
                    }
                }
            )
        }
    }
}
