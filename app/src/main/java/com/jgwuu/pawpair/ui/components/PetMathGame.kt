package com.jgwuu.pawpair.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import kotlin.math.roundToInt

enum class MathDifficulty(val label: String, val desc: String, val rewardMultiplier: Int) {
    EASY("Fácil", "Sumas y restas básicas", 1),
    MEDIUM("Medio", "Multiplicaciones y divisiones", 2),
    HARD("Difícil", "Ecuaciones y álgebra", 4),
    IMPOSSIBLE("Imposible", "Cálculo multivariado: Derivadas parciales, gradientes e integrales dobles", 8)
}

data class MathProblem(val question: String, val answer: String, val options: List<String>)

@Composable
fun PetMathGame(
    onGameFinished: (Int, Int) -> Unit,
    onClose: () -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf<MathDifficulty?>(null) }

    fun generateProblem(diff: MathDifficulty): MathProblem {
        return when (diff) {
            MathDifficulty.EASY -> {
                val n1 = (2..25).random()
                val n2 = (2..25).random()
                val op = listOf("+", "-").random()
                val ans = if (op == "+") n1 + n2 else n1 - n2
                val opts = mutableSetOf(ans.toString())
                while (opts.size < 4) { opts.add((ans + (-6..6).random()).toString()) }
                MathProblem("$n1 $op $n2 = ?", ans.toString(), opts.shuffled())
            }
            MathDifficulty.MEDIUM -> {
                val op = listOf("*", "/").random()
                if (op == "*") {
                    val n1 = (3..15).random()
                    val n2 = (3..15).random()
                    val ans = n1 * n2
                    val opts = mutableSetOf(ans.toString())
                    while (opts.size < 4) { opts.add((ans + (-10..10).random()).toString()) }
                    MathProblem("$n1 × $n2 = ?", ans.toString(), opts.shuffled())
                } else {
                    val n2 = (2..12).random()
                    val ans = (2..12).random()
                    val n1 = n2 * ans
                    val opts = mutableSetOf(ans.toString())
                    while (opts.size < 4) { opts.add((ans + (-5..5).random()).coerceAtLeast(0).toString()) }
                    MathProblem("$n1 ÷ $n2 = ?", ans.toString(), opts.shuffled())
                }
            }
            MathDifficulty.HARD -> {
                val x = (1..10).random()
                val a = (2..6).random()
                val b = (1..15).random()
                val c = a * x + b
                val ans = x.toString()
                val opts = mutableSetOf(ans)
                while (opts.size < 4) { opts.add((x + (-4..4).random()).coerceAtLeast(0).toString()) }
                MathProblem("Resolver x:\n${a}x + $b = $c", ans, opts.shuffled())
            }
            MathDifficulty.IMPOSSIBLE -> {
                val type = (1..4).random()
                when (type) {
                    1 -> {
                        // Derivada parcial
                        val a = (2..5).random()
                        val b = (2..5).random()
                        val q = "∂/∂x [ ${a}x²y + ${b}xy³ ]"
                        val ans = "${2 * a}xy + ${b}y³"
                        val fake1 = "${2 * a}x²y + ${3 * b}xy²"
                        val fake2 = "${a}xy + ${b}y³"
                        val fake3 = "${2 * a}y + ${3 * b}xy²"
                        MathProblem(q, ans, listOf(ans, fake1, fake2, fake3).shuffled())
                    }
                    2 -> {
                        // Gradiente en punto
                        val a = (1..4).random()
                        val b = (1..4).random()
                        val x0 = (1..2).random()
                        val y0 = (1..2).random()
                        val q = "Gradiente ∇f en ($x0,$y0)\nf(x,y) = ${a}x² + ${b}y²"
                        val dx = 2 * a * x0
                        val dy = 2 * b * y0
                        val ans = "($dx, $dy)"
                        val fake1 = "($dy, $dx)"
                        val fake2 = "(${dx + 2}, $dy)"
                        val fake3 = "($dx, ${dy + 2})"
                        MathProblem(q, ans, listOf(ans, fake1, fake2, fake3).shuffled())
                    }
                    3 -> {
                        // Integral doble sencilla
                        val c = (2..6).random()
                        val q = "∫₀¹ ∫₀¹ ($c·xy) dx dy"
                        val ansVal = if (c % 4 == 0) (c / 4).toString() else if (c % 2 == 0) "${c/2}/2" else "$c/4"
                        // For simplicity keep neat integer choices
                        val cInt = c * 4
                        val qInt = "∫₀¹ ∫₀¹ ($cInt·xy) dx dy"
                        val ans = c.toString()
                        val opts = mutableSetOf(ans)
                        while (opts.size < 4) { opts.add((c + (-3..3).random()).coerceAtLeast(1).toString()) }
                        MathProblem(qInt, ans, opts.shuffled())
                    }
                    else -> {
                        // Divergencia de campo vectorial
                        val a = (2..6).random()
                        val b = (2..6).random()
                        val q = "Divergencia ∇·F de\nF(x,y) = (${a}x²y, ${b}xy²)"
                        val ans = "${2 * a}xy + ${2 * b}xy"
                        val simplifiedAns = if (2 * a + 2 * b > 0) "${2 * a + 2 * b}xy" else "${2 * a}xy"
                        val fake1 = "${2 * a}x + ${2 * b}y"
                        val fake2 = "${a + b}xy"
                        val fake3 = "${2 * a}x² + ${2 * b}y²"
                        MathProblem(q, simplifiedAns, listOf(simplifiedAns, fake1, fake2, fake3).shuffled())
                    }
                }
            }
        }
    }

    var currentProblem by remember { mutableStateOf<MathProblem?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var questionsSolved by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

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
                Text("🔢 Matemáticas PawPair", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            if (selectedDifficulty == null) {
                Text("Selecciona una dificultad:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Entre mayor dificultad, mayores recompensas y monedas ganadas.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(14.dp))

                MathDifficulty.values().forEach { diff ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                selectedDifficulty = diff
                                currentProblem = generateProblem(diff)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(diff.label, fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Text(diff.desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text("x${diff.rewardMultiplier} Monedas", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            } else if (!gameOver && currentProblem != null) {
                val diff = selectedDifficulty!!
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dificultad: ${diff.label}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Text("$questionsSolved / 5", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        currentProblem!!.question,
                        fontSize = if (diff == MathDifficulty.IMPOSSIBLE) 20.sp else 30.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (diff == MathDifficulty.IMPOSSIBLE) 1 else 2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(180.dp)
                ) {
                    items(currentProblem!!.options) { opt ->
                        Button(
                            onClick = {
                                if (opt == currentProblem!!.answer) {
                                    score += 20 * diff.rewardMultiplier
                                }
                                questionsSolved++
                                if (questionsSolved >= 5) {
                                    gameOver = true
                                } else {
                                    currentProblem = generateProblem(diff)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(opt, fontSize = if (diff == MathDifficulty.IMPOSSIBLE) 14.sp else 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                val diff = selectedDifficulty ?: MathDifficulty.EASY
                val earnedCoins = score
                Text("🏆 ¡Cálculo Finalizado!", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nivel: ${diff.label}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Monedas Ganadas: $earnedCoins 🪙", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        onGameFinished(earnedCoins, score / diff.rewardMultiplier)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎁 Reclamar $earnedCoins Monedas", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
