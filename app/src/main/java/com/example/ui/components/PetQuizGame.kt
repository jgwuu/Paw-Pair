package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class QuizQuestion(val question: String, val options: List<String>, val correctIndex: Int)

@Composable
fun PetQuizGame(
    onGameFinished: (Int, Int) -> Unit,
    onClose: () -> Unit
) {
    val questions = remember {
        listOf(
            QuizQuestion("¿Qué alimento NUNCA debes darle a un perro?", listOf("Manzana", "Chocolate", "Zanahoria", "Pollo"), 1),
            QuizQuestion("¿Cuántas horas duerme un gatito en promedio al día?", listOf("6 a 8 horas", "12 a 16 horas", "4 a 5 horas", "20 a 22 horas"), 1),
            QuizQuestion("¿Cuál es la raza de perro conocida por su sonrisa en Japón?", listOf("Shiba Inu / Akita", "Chihuahua", "Bulldog", "Poodle"), 0),
            QuizQuestion("¿Por qué ronronean los gatos principalmente?", listOf("Sólo por hambre", "Para expresar comodidad o calma", "Porque están enojados", "Para espantar insectos"), 1),
            QuizQuestion("¿Qué nutriente es esencial para que tu mascota tenga mucha energía?", listOf("Azúcar pura", "Proteínas y agua fresca", "Sal marina", "Café"), 1)
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }

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
                Text("💡 Trivia Mascotas PawPair", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Text("¡Demuestra cuánto sabes sobre el cuidado animal!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            if (!gameOver) {
                val q = questions[currentIndex]
                Text("Pregunta ${currentIndex + 1} de ${questions.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(q.question, fontSize = 16.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))

                q.options.forEachIndexed { index, opt ->
                    val color = when {
                        selectedOption == null -> MaterialTheme.colorScheme.surfaceVariant
                        index == q.correctIndex -> Color(0xFFC8E6C9)
                        index == selectedOption -> Color(0xFFFFCDD2)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = color,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable(enabled = selectedOption == null) {
                                selectedOption = index
                                if (index == q.correctIndex) score += 20
                            }
                    ) {
                        Text(opt, modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                if (selectedOption != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            selectedOption = null
                            if (currentIndex + 1 < questions.size) {
                                currentIndex++
                            } else {
                                gameOver = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (currentIndex + 1 < questions.size) "Siguiente Pregunta ➡️" else "Ver Resultados 🎉")
                    }
                }
            } else {
                Text("🎊 ¡Trivia Completada!", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Puntuación final: $score / 100", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        onGameFinished(score, score / 2)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎁 Reclamar $score Monedas", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
