package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InventoryEntity
import com.example.data.PetEntity
import com.example.data.UserStatsEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenScreen(
    activePet: PetEntity?,
    inventory: List<InventoryEntity>,
    stats: UserStatsEntity?,
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    if (activePet == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Adopta una mascota para usar la cocina.")
        }
        return
    }

    val currentTheme = stats?.appTheme ?: "FOREST"
    val kitchenBrushes = when (currentTheme) {
        "SUNSET" -> Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFCCBC)))
        "OCEAN" -> Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2)))
        "CANDY" -> Brush.verticalGradient(listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0)))
        "CYBER" -> Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
        else -> Brush.verticalGradient(listOf(Color(0xFFFFFDF0), Color(0xFFFFF9C4))) // FOREST / WARM
    }
    val headerContainerColor = when (currentTheme) {
        "SUNSET" -> Color(0xFFFBE9E7)
        "OCEAN" -> Color(0xFFE0F2F1)
        "CANDY" -> Color(0xFFF8BBD0)
        "CYBER" -> Color(0xFF0F3460)
        else -> Color(0xFFFFF8E1)
    }
    val themeAccentColor = when (currentTheme) {
        "SUNSET" -> Color(0xFFD84315)
        "OCEAN" -> Color(0xFF00695C)
        "CANDY" -> Color(0xFFC2185B)
        "CYBER" -> Color(0xFF00E5FF)
        else -> Color(0xFFE65100)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(kitchenBrushes)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Sleek Kitchen Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = headerContainerColor),
            border = BorderStroke(1.5.dp, themeAccentColor.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeAccentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧑‍🍳", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cocina Gourmet & Recetas Mágicas ✨",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (currentTheme == "CYBER") Color.White else themeAccentColor
                    )
                    Text(
                        text = "¡Alquimia culinaria para consentir a ${activePet.name}!",
                        fontSize = 11.sp,
                        color = if (currentTheme == "CYBER") Color(0xFFCBD5E1) else Color.DarkGray
                    )
                }
            }
        }

        // Available ingredients indicators (Despensa Visual)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (currentTheme == "CYBER") Color(0xFF1E293B) else Color.White),
            border = BorderStroke(1.dp, if (currentTheme == "CYBER") Color(0xFF334155) else Color(0xFFE0E0E0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "Despensa Actual (Ingredientes en tu Inventario) 🛒🌾",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeAccentColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val ingredientItems = listOf(
                    Triple("ING_PLATANO", "🍌", "Plátano"),
                    Triple("ING_QUESO", "🧀", "Queso"),
                    Triple("ING_SALCHICHA", "🌭", "Salchicha"),
                    Triple("ING_PAPA", "🥔", "Papa"),
                    Triple("ING_HUEVO", "🥚", "Huevo"),
                    Triple("ING_HARINA", "🌾", "Harina"),
                    Triple("ING_LECHE", "🥛", "Leche"),
                    Triple("ING_AZUCAR", "🍬", "Azúcar"),
                    Triple("ING_COCOA", "🍫", "Cocoa"),
                    Triple("ING_PESCADO", "🐟", "Pescado")
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ingredientItems.take(5).forEach { (id, emoji, name) ->
                            val qty = inventory.find { it.itemId == id }?.quantity ?: 0
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (qty > 0) Color(0xFFE8F5E9) else Color(0xFFFAFAFA)
                                ),
                                border = BorderStroke(1.2.dp, if (qty > 0) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentTheme == "CYBER") Color.Black else Color(0xFF37474F),
                                        maxLines = 1
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (qty > 0) Color(0xFF2E7D32) else Color(0xFFD32F2F))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "x$qty",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ingredientItems.drop(5).forEach { (id, emoji, name) ->
                            val qty = inventory.find { it.itemId == id }?.quantity ?: 0
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (qty > 0) Color(0xFFE8F5E9) else Color(0xFFFAFAFA)
                                ),
                                border = BorderStroke(1.2.dp, if (qty > 0) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentTheme == "CYBER") Color.Black else Color(0xFF37474F),
                                        maxLines = 1
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (qty > 0) Color(0xFF2E7D32) else Color(0xFFD32F2F))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "x$qty",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recipes list (Visual Gourmet Recipes)
        val platanoQty = inventory.find { it.itemId == "ING_PLATANO" }?.quantity ?: 0
        val quesoQty = inventory.find { it.itemId == "ING_QUESO" }?.quantity ?: 0
        val salchichaQty = inventory.find { it.itemId == "ING_SALCHICHA" }?.quantity ?: 0
        val papaQty = inventory.find { it.itemId == "ING_PAPA" }?.quantity ?: 0
        val huevoQty = inventory.find { it.itemId == "ING_HUEVO" }?.quantity ?: 0
        val harinaQty = inventory.find { it.itemId == "ING_HARINA" }?.quantity ?: 0
        val lecheQty = inventory.find { it.itemId == "ING_LECHE" }?.quantity ?: 0
        val azucarQty = inventory.find { it.itemId == "ING_AZUCAR" }?.quantity ?: 0
        val cocoaQty = inventory.find { it.itemId == "ING_COCOA" }?.quantity ?: 0
        val pescadoQty = inventory.find { it.itemId == "ING_PESCADO" }?.quantity ?: 0

        val recipeList = listOf(
            Triple("Delicia Dorada Gourmet", listOf("ING_HARINA", "ING_HUEVO"), "Pastel horneado de harina fina y huevo. (+40 Hambre, +20 Felicidad)"),
            Triple("Patacón Supremo Real", listOf("ING_PLATANO", "ING_QUESO"), "Crujiente plátano imperial con queso fundido. (+35 Hambre, +25 Felicidad)"),
            Triple("Banquete Festivo", listOf("ING_SALCHICHA", "ING_PAPA", "ING_QUESO"), "Papa dorada con salchichas y queso fundido. (+45 Hambre, +30 Felicidad)"),
            Triple("Trufas Dulces Nevadas", listOf("ING_AZUCAR", "ING_LECHE", "ING_COCOA"), "Dulce artesanal de chocolate y crema nevada. (+25 Hambre, +40 Felicidad)"),
            Triple("Chocolate Alpino Mágico", listOf("ING_COCOA", "ING_LECHE", "ING_QUESO"), "Bebida caliente con queso suave fundido. (+30 Hambre, +35 Felicidad)"),
            Triple("Mojarra Mar y Tierra", listOf("ING_PESCADO", "ING_PLATANO"), "Pescado dorado acompañado de crujientes patacones. (+50 Hambre, +40 Felicidad)"),
            Triple("Sopa Real de Queso", listOf("ING_PAPA", "ING_QUESO", "ING_HARINA"), "Sopa cremosa con especias y queso artesanal. (+55 Hambre, +35 Felicidad)"),
            Triple("Arroz Imperial Dulce", listOf("ING_PLATANO", "ING_PESCADO", "ING_AZUCAR"), "Arroz caribeño cocinado lentamente en leche dulce. (+45 Hambre, +45 Felicidad)"),
            Triple("Empanada Crujiente de Oro", listOf("ING_PAPA", "ING_QUESO", "ING_SALCHICHA"), "Pastel relleno de salchicha y queso derretido. (+38 Hambre, +30 Felicidad)"),
            Triple("Cazuela Marinera Real", listOf("ING_PESCADO", "ING_LECHE", "ING_HUEVO"), "Sopa marinera cocida en leche y mariscos. (+60 Hambre, +45 Felicidad)"),
            Triple("Arroz Marinero Suprema", listOf("ING_PESCADO", "ING_HUEVO", "ING_HARINA"), "Arroz gourmet de mar con huevo cocido. (+45 Hambre, +35 Felicidad)")
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(recipeList) { (recipeName, ingredients, descriptionText) ->
                val groupedReqs = ingredients.groupingBy { it }.eachCount()
                val canCook = groupedReqs.all { (ingId, reqQty) ->
                    val item = inventory.find { it.itemId == ingId }
                    item != null && item.quantity >= reqQty
                }

                val boosts = when (recipeName) {
                    "Delicia Dorada Gourmet" -> Pair(40f, 20f)
                    "Patacón Supremo Real" -> Pair(35f, 25f)
                    "Banquete Festivo" -> Pair(45f, 30f)
                    "Trufas Dulces Nevadas" -> Pair(25f, 40f)
                    "Chocolate Alpino Mágico" -> Pair(30f, 35f)
                    "Mojarra Mar y Tierra" -> Pair(50f, 40f)
                    "Sopa Real de Queso" -> Pair(55f, 35f)
                    "Arroz Imperial Dulce" -> Pair(45f, 45f)
                    "Empanada Crujiente de Oro" -> Pair(38f, 30f)
                    "Cazuela Marinera Real" -> Pair(60f, 45f)
                    "Arroz Marinero Suprema" -> Pair(45f, 35f)
                    else -> Pair(30f, 20f)
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (currentTheme == "CYBER") Color(0xFF1E293B) else Color.White),
                    border = BorderStroke(1.5.dp, if (canCook) Color(0xFF4CAF50) else Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (canCook) 4.dp else 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("recipe_card_$recipeName")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (canCook) Color(0xFFE8F5E9) else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (canCook) "🥘" else "🍳", fontSize = 30.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = recipeName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (currentTheme == "CYBER") Color.White else themeAccentColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = descriptionText,
                                fontSize = 11.sp,
                                color = if (currentTheme == "CYBER") Color(0xFFCBD5E1) else Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Ingredients pill indicators
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                ingredients.forEach { ingId ->
                                    val emoji = when (ingId) {
                                        "ING_PLATANO" -> "🍌"
                                        "ING_QUESO" -> "🧀"
                                        "ING_SALCHICHA" -> "🌭"
                                        "ING_PAPA" -> "🥔"
                                        "ING_HUEVO" -> "🥚"
                                        "ING_HARINA" -> "🌾"
                                        "ING_LECHE" -> "🥛"
                                        "ING_AZUCAR" -> "🍬"
                                        "ING_COCOA" -> "🍫"
                                        "ING_PESCADO" -> "🐟"
                                        else -> "🥗"
                                    }
                                    val qty = when (ingId) {
                                        "ING_PLATANO" -> platanoQty
                                        "ING_QUESO" -> quesoQty
                                        "ING_SALCHICHA" -> salchichaQty
                                        "ING_PAPA" -> papaQty
                                        "ING_HUEVO" -> huevoQty
                                        "ING_HARINA" -> harinaQty
                                        "ING_LECHE" -> lecheQty
                                        "ING_AZUCAR" -> azucarQty
                                        "ING_COCOA" -> cocoaQty
                                        "ING_PESCADO" -> pescadoQty
                                        else -> 0
                                    }
                                    Card(
                                        shape = RoundedCornerShape(6.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (qty > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                        ),
                                        border = BorderStroke(1.dp, if (qty > 0) Color(0xFF81C784) else Color(0xFFE57373))
                                    ) {
                                        Text(
                                            text = "$emoji Tengo: $qty",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            color = if (qty > 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                }
                            }

                            // Cook Button (target height 44dp+ for accessibility)
                            Button(
                                onClick = {
                                    if (canCook) {
                                        viewModel.cookActivePetRecipe(recipeName, ingredients, boosts.first, boosts.second)
                                    }
                                },
                                enabled = canCook,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canCook) Color(0xFF2E7D32) else Color(0xFFB0BEC5),
                                    disabledContainerColor = Color(0xFFECEFF1),
                                    disabledContentColor = Color(0xFF90A4AE)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text(
                                    if (canCook) "¡Cocinar Delicia! 🧑‍🍳✨" else "Faltan Ingredientes 🛒",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
