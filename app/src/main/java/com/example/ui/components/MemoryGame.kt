package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MemoryCard(
    val id: Int,
    val iconName: String,
    val color: Color,
    val unicodeChar: String, // visual representation
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

@Composable
fun MemoryMatchGameView(
    onGameFinished: (coinsEarned: Int, score: Int) -> Unit,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isInitialized by remember { mutableStateOf(false) }
    val cards = remember { mutableStateListOf<MemoryCard>() }
    
    var firstSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var secondSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var matchesFound by remember { mutableIntStateOf(0) }
    var totalMoves by remember { mutableIntStateOf(0) }
    var isProcessingSelection by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    // Constants
    val rawElements = listOf(
        Pair("SHIBA", Pair(Color(0xFFE5A65D), "🐶")),
        Pair("SLIME", Pair(Color(0xFF29B6F6), "💧")),
        Pair("KITTY", Pair(Color(0xFFF06292), "🐱")),
        Pair("DRACO", Pair(Color(0xFF66BB6A), "🔥")),
        Pair("AXOLOTL", Pair(Color(0xFFEC407A), "🌸")),
        Pair("POTION", Pair(Color(0xFFAB47BC), "🧪")),
        Pair("HEART", Pair(Color(0xFFEF5350), "💖")),
        Pair("STAR", Pair(Color(0xFFFFD54F), "⭐"))
    )

    // Initialize cards
    LaunchedEffect(isInitialized) {
        if (!isInitialized) {
            val doubled = (rawElements + rawElements).shuffled()
            cards.clear()
            doubled.forEachIndexed { idx, pair ->
                cards.add(
                    MemoryCard(
                        id = idx,
                        iconName = pair.first,
                        color = pair.second.first,
                        unicodeChar = pair.second.second
                    )
                )
            }
            isInitialized = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Buscando Parejas 🧠",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Game")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isGameOver) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(Color(0xFFFFD54F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CardGiftcard,
                            contentDescription = "Success",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "¡Excelente Memoria! 🏆",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Encontraste todas las parejas en solo $totalMoves movimientos.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.MonetizationOn, "Coins", tint = Color(0xFFFFC107), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recompensa: +100 Monedas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            onGameFinished(100, 100 - totalMoves)
                            onClose()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Text("RECLAMAR PREMIO 🥳", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Info bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Parejas: $matchesFound / 8",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Movimientos: $totalMoves",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Card Grid (4 rows x 4 columns)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cards.size) { idx ->
                        val card = cards[idx]
                        
                        // Animated rotation
                        val rotationAnim by animateFloatAsState(
                            targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
                            animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
                            label = "CardFlip"
                        )

                        Box(
                            modifier = Modifier
                                .aspectRatio(0.85f)
                                .graphicsLayer {
                                    rotationY = rotationAnim
                                    cameraDistance = 12f * density
                                }
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (rotationAnim > 90f) {
                                        card.color.copy(alpha = 0.2f)
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                    }
                                )
                                .clickable {
                                    if (isProcessingSelection || card.isFlipped || card.isMatched) return@clickable

                                    coroutineScope.launch {
                                        // Flip card
                                        cards[idx] = card.copy(isFlipped = true)

                                        if (firstSelectedIndex == null) {
                                            firstSelectedIndex = idx
                                        } else {
                                            secondSelectedIndex = idx
                                            isProcessingSelection = true
                                            totalMoves++

                                            // delay slightly to show second card flip
                                            delay(700)

                                            val firstIdx = firstSelectedIndex!!
                                            val secondIdx = secondSelectedIndex!!

                                            if (cards[firstIdx].iconName == cards[secondIdx].iconName) {
                                                // MATCH!
                                                cards[firstIdx] = cards[firstIdx].copy(isMatched = true)
                                                cards[secondIdx] = cards[secondIdx].copy(isMatched = true)
                                                matchesFound++

                                                if (matchesFound == 8) {
                                                    isGameOver = true
                                                }
                                            } else {
                                                // MISMATCH, flip back
                                                cards[firstIdx] = cards[firstIdx].copy(isFlipped = false)
                                                cards[secondIdx] = cards[secondIdx].copy(isFlipped = false)
                                            }

                                            // Reset pointers
                                            firstSelectedIndex = null
                                            secondSelectedIndex = null
                                            isProcessingSelection = false
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (rotationAnim > 90f) {
                                // Revealed content
                                Text(
                                    text = card.unicodeChar,
                                    fontSize = 32.sp,
                                    // Mirror text so it looks upright after 180 degree rotation
                                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                                )
                            } else {
                                // Card back cover
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Psychology,
                                        contentDescription = "Card Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "?",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Encuentra dos tarjetas idénticas para conseguir el bono diario de cuidado.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
