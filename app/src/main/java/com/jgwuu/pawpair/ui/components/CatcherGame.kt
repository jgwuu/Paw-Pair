package com.jgwuu.pawpair.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

data class FallingEntity(
    val id: Int,
    var xPositionPercent: Float, // 0.0f to 1.0f
    var yPositionDp: Float,
    val type: String, // "COIN", "FRUIT", "BOMB"
    val speed: Float
)

@Composable
fun CatcherGameView(
    petType: String,
    petName: String,
    onGameFinished: (coinsEarned: Int, score: Int) -> Unit,
    onClose: () -> Unit
) {
    var gameStarted by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var coinsEarned by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    
    // Player position percentage: 0.0 (Left) to 1.0 (Right). Center is 0.5.
    var playerXPercent by remember { mutableFloatStateOf(0.5f) }
    val entityList = remember { mutableStateListOf<FallingEntity>() }
    var entityCounter by remember { mutableIntStateOf(0) }

    // Constants
    val playerWidthPercent = 0.18f

    // Main Timer Loop
    LaunchedEffect(gameStarted, gameOver) {
        if (gameStarted && !gameOver) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            gameOver = true
        }
    }

    // Physics Engine / Game Loop
    LaunchedEffect(gameStarted, gameOver) {
        if (gameStarted && !gameOver) {
            while (!gameOver) {
                delay(30) // ~33 FPS

                // Spawn items occasionally
                if (Random.nextFloat() < 0.07f && entityList.size < 6) {
                    val types = listOf("COIN", "COIN", "COIN", "FRUIT", "BOMB")
                    entityList.add(
                        FallingEntity(
                            id = entityCounter++,
                            xPositionPercent = Random.nextFloat().coerceIn(0.05f, 0.95f),
                            yPositionDp = -20f,
                            type = types.random(),
                            speed = Random.nextFloat() * 4f + 3f + (30 - timeLeft) * 0.1f // gets faster
                        )
                    )
                }

                // Move items and check collisions
                val iterator = entityList.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    item.yPositionDp += item.speed

                    // Check bounds (assuming container is 320dp deep)
                    if (item.yPositionDp > 310f) {
                        // Check collision with the pet catcher basket
                        val deltaX = kotlin.math.abs(item.xPositionPercent - playerXPercent)
                        if (deltaX <= (playerWidthPercent + 0.04f)) {
                            // Collision! Award points
                            if (item.type == "COIN") {
                                score += 15
                                coinsEarned += 3
                            } else if (item.type == "FRUIT") {
                                score += 10
                                coinsEarned += 1
                            } else if (item.type == "BOMB") {
                                score = (score - 20).coerceAtLeast(0)
                                coinsEarned = (coinsEarned - 5).coerceAtLeast(0)
                            }
                            
                            iterator.remove() // hit pet, dissolve
                        } else if (item.yPositionDp > 340f) {
                            iterator.remove() // missed, dissolved on grass
                        }
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    text = "Slime Catcher 🍓",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Game")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (!gameStarted) {
                // Intro Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.SportsEsports,
                            contentDescription = "Game Icon",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "¡Atrapa Dulces, Evita Bombas!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mueve a $petName a la izquierda y derecha para recolectar frutas y monedas de oro. ¡Ojo con los explosivos!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { gameStarted = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("¡EMPEZAR A JUGAR! (30s)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (gameOver) {
                // Game Over Score Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¡TIEMPO TERMINADO!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD32F2F)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Estadísticas de la Partida", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Puntos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    Text("$score pts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Monedas Ganadas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.MonetizationOn, "Coins", tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+$coinsEarned", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            onGameFinished(coinsEarned, score)
                            onClose()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("RECLAMAR RECOMPENSA 🎉", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                // Live gameplay area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Score",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Score: $score",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.MonetizationOn,
                            contentDescription = "Coins",
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$coinsEarned",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (timeLeft < 8) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Tiempo: ${timeLeft}s",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeft < 8) Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // The Canvas Game Board
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFE1F5FE), Color(0xFFC8E6C9)) // Sky to Lawn Gradient
                            )
                        )
                ) {
                    val boardWidth = constraints.maxWidth
                    val boardHeight = constraints.maxHeight

                    // Render Falling Items
                    entityList.forEach { entity ->
                        val leftPositionOffset = (entity.xPositionPercent * boardWidth.toFloat()) - with(LocalDensity.current) { 16.dp.toPx() }
                        Box(
                            modifier = Modifier
                                .absoluteOffset(
                                    x = with(LocalDensity.current) { leftPositionOffset.toDp() },
                                    y = entity.yPositionDp.dp
                                )
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when (entity.type) {
                                        "COIN" -> Color(0xFFFFC107)
                                        "FRUIT" -> Color(0xFFE91E63)
                                        else -> Color(0xFF212121)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (entity.type) {
                                    "COIN" -> Icons.Filled.MonetizationOn
                                    "FRUIT" -> Icons.Filled.Spa
                                    else -> Icons.Filled.Warning
                                },
                                contentDescription = entity.type,
                                modifier = Modifier.size(20.dp),
                                tint = if (entity.type == "BOMB") Color(0xFFFF5252) else Color.White
                            )
                        }
                    }

                    // Render Pet Player at the bottom
                    val playerXOffset = (playerXPercent * boardWidth.toFloat()) - with(LocalDensity.current) { 32.dp.toPx() }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .absoluteOffset(
                                x = with(LocalDensity.current) { playerXOffset.toDp() },
                                y = (-10).dp
                            )
                            .size(70.dp, 64.dp)
                    ) {
                        // We recycle PetRenderer or draw a super simplified version of current pet!
                        PetRenderer(
                            type = petType,
                            isSleeping = false,
                            hunger = 80f,
                            happiness = 100f,
                            equippedHat = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Left & Right Control Buttons at target size (48dp+)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = {
                            playerXPercent = (playerXPercent - 0.09f).coerceAtLeast(0.05f)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Mover Izquierda")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("← Izq", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            playerXPercent = (playerXPercent + 0.09f).coerceAtMost(0.95f)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text("Der →", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Mover Derecha")
                    }
                }
            }
        }
    }
}
