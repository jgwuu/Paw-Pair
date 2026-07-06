package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.InventoryEntity
import com.example.data.PetEntity
import com.example.data.UserStatsEntity
import com.example.ui.components.PetRenderer
import com.example.ui.components.WardrobeTesterDialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    rawPet: PetEntity,
    stats: UserStatsEntity?,
    inventory: List<InventoryEntity>,
    viewModel: PetViewModel,
    onAdoptNew: () -> Unit,
    onOpenGames: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pet = rawPet
    val interactionTrigger by viewModel.interactionTrigger.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val linkCode = stats?.linkCode
    DisposableEffect(linkCode) {
        if (!linkCode.isNullOrBlank() && linkCode.length == 6 && linkCode != "XXXX-XXXX") {
            com.example.data.RealtimeSyncManager.startListening(linkCode) { _ -> }
        }
        onDispose {
            com.example.data.RealtimeSyncManager.stopListening()
        }
    }
    var showShareCodeDialog by remember { mutableStateOf(false) }
    var permanentCodeToShare by remember { mutableStateOf("") }
    var showFoodPanel by remember { mutableStateOf(false) }
    var showWardrobeTester by remember { mutableStateOf(false) }
    var currentRoom by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("LIVING_ROOM") }

    if (showWardrobeTester) {
        WardrobeTesterDialog(
            initialPetType = pet.type,
            initialHat = pet.equippedHat,
            initialAccessory = pet.equippedAccessory,
            onDismiss = { showWardrobeTester = false },
            onApplyToActivePet = { hat, cloth ->
                hat?.let { viewModel.equipActivePetAccessory("HAT_$it") }
                cloth?.let { viewModel.equipActivePetOutfit(cloth) }
            }
        )
    }

    // Custom variety states
    var activeGroomingAction by remember { mutableStateOf("NONE") } // "NONE", "SOAP", "SHOWER", "BRUSH"
    var showGroomingSelector by remember { mutableStateOf(false) }
    
    // Walk simulation states (Garden)
    var isWalking by remember { mutableStateOf(false) }
    var walkProgress by remember { mutableStateOf(0f) }
    var walkEventText by remember { mutableStateOf("") }

    // Widgets hub simulation toggle
    var showWidgetsHub by remember { mutableStateOf(false) }

    // Handle automated clearing of grooming animations
    LaunchedEffect(activeGroomingAction) {
        if (activeGroomingAction != "NONE") {
            kotlinx.coroutines.delay(2800)
            activeGroomingAction = "NONE"
        }
    }

    // Run Walk simulation
    LaunchedEffect(isWalking) {
        if (isWalking) {
            walkProgress = 0f
            walkEventText = "🦮 Iniciando paseo costeño con ${pet.name} en el parque..."
            kotlinx.coroutines.delay(1300)
            walkProgress = 0.33f
            walkEventText = "🌴 ¡Qué divertido! Correteó unos cangrejos en la arena y saltó alegre."
            kotlinx.coroutines.delay(1300)
            walkProgress = 0.66f
            val finds = listOf("unas monedas antiguas 🪙", "un mango biche con sal 🥭", "una caracola marina gigante 🐚", "un sombrero vueltiao extraviado 👒")
            walkEventText = "🌟 ¡Increíble! ${pet.name} excavó en el sendero y encontró ${finds.random()}"
            kotlinx.coroutines.delay(1400)
            walkProgress = 1.0f
            walkEventText = "🏠 Regresando a casa cansado pero muy feliz."
            kotlinx.coroutines.delay(1000)
            viewModel.walkActivePet()
            isWalking = false
        }
    }

    // Rutina calculation based on real system hour (Colombia Local Time)
    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val routineInfo = when {
        currentHour in 6..11 -> Pair("Desayuno 🍳", "Es momento de desayunar.")
        currentHour in 11..17 -> Pair("Almuerzo 🍽️", "Turno de almorzar.")
        currentHour in 17..23 -> Pair("Cena 🥣", "Hora de cenar.")
        else -> Pair("Dormir 💤", "Hora para dormir.")
    }

    val petColorModel = when (pet.type) {
        "SHIBA" -> Color(0xFFE5A65D)
        "SLIME" -> Color(0xFF4AC4F3)
        "KITTY" -> Color(0xFFF1B7C6)
        "DRACO" -> Color(0xFF4CB050)
        "AXOLOTL" -> Color(0xFFEC407A)
        else -> MaterialTheme.colorScheme.primary
    }

    val foodItems = inventory.filter { it.category == "FOOD" || it.itemId == "POTION_ENERGY" || it.itemId == "POTION_LOVE" }
    val soapItem = inventory.find { it.itemId == "CLEANER_SOAP" }
    val shampooItem = inventory.find { it.itemId == "CLEANER_SHAMPOO" }
    val totalCleaners = (soapItem?.quantity ?: 0) + (shampooItem?.quantity ?: 0)

    val currentTheme = stats?.appTheme ?: "FOREST"
    val backgroundBrush = if (pet.isSleeping) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A), // Sleep dark mode stays Cozy
                Color(0xFF1E293B)
            )
        )
    } else {
        when (currentTheme) {
            "SUNSET" -> Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFCCBC)))
            "OCEAN" -> Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2)))
            "CANDY" -> Brush.verticalGradient(listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0)))
            "CYBER" -> Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
            else -> Brush.verticalGradient(listOf(Color(0xFFF5EFEB), Color(0xFFE8F5E9)))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Star Particle Background for Sleeping Pet
        if (pet.isSleeping) {
            Box(modifier = Modifier.fillMaxSize()) {
                repeat(8) { i ->
                    Box(
                        modifier = Modifier
                            .offset(x = (40 + i * 50).dp, y = (100 + (i * i) % 300).dp)
                            .size(if (i % 2 == 0) 4.dp else 6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.4f))
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scrollable central area (totally avoids overlays!)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Firebase Config Error Badge on Dashboard
                val configError = remember { com.example.data.CoMascotaSyncService.firebaseConfigError }
                if (configError != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp)
                            .clickable {
                                viewModel.triggerToast("⚠️ Ve a la pestaña Co-Care para ver los detalles del error de Firebase y cómo solucionarlo.")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Error de Firebase",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Configuración de Firebase incorrecta. Toca para ver cómo solucionarlo.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (stats?.isLinked == true) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (pet.isSleeping) Color(0x33FFFFFF) else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                        ),
                        border = if (!pet.isSleeping) BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)) else null,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = stats.partnerAvatar ?: "👤", fontSize = 18.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Compañero Vinculado 🤝",
                                    fontSize = 10.sp,
                                    color = if (pet.isSleeping) Color.White.copy(0.8f) else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stats.partnerName ?: "Compañero",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "En vivo 🟢",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Upper Header (Pet selection header + level display)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level Indicator with Botanical Style
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (pet.isSleeping) Color(0xFF334155) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    border = if (!pet.isSleeping) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) else null,
                    modifier = Modifier.weight(1.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.MilitaryTech,
                            "Level",
                            tint = if (pet.isSleeping) Color(0xFF94A3B8) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        val currentLvl = stats?.level ?: 1
                        val stageBadge = when {
                            currentLvl >= 200 -> "🌌 Definitivo"
                            currentLvl >= 100 -> "👑 Legendario"
                            currentLvl >= 50 -> "⭐ Adulto"
                            else -> "🌱 Bebé"
                        }
                        Text(
                            text = "Nvl $currentLvl ($stageBadge)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Switch Pet or Adopt Button Mode
                Button(
                    onClick = onAdoptNew,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pet.isSleeping) Color(0x33FFFFFF) else MaterialTheme.colorScheme.secondary,
                        contentColor = if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.onSecondary
                    ),
                    border = if (!pet.isSleeping) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null,
                    modifier = Modifier.height(36.dp).weight(1.1f),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    Icon(Icons.Filled.Group, "Familias", modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Familias", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Compartir Pet Button
                Button(
                    onClick = {
                        viewModel.ensurePermanentShareCode { code ->
                            permanentCodeToShare = code
                            showShareCodeDialog = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pet.isSleeping) Color(0x33FFFFFF) else MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    border = if (!pet.isSleeping) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null,
                    modifier = Modifier.height(36.dp).weight(1.1f),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    Icon(Icons.Filled.Share, "Compartir", modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Compartir", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Widgets Toggle Button
                Button(
                    onClick = { showWidgetsHub = !showWidgetsHub },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showWidgetsHub) MaterialTheme.colorScheme.primary else if (pet.isSleeping) Color(0x33FFFFFF) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (showWidgetsHub) Color.White else if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = if (!pet.isSleeping) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null,
                    modifier = Modifier.height(36.dp).weight(1.1f),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    Icon(Icons.Filled.Widgets, "Widgets Switch", modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(if (showWidgetsHub) "Hub 🟢" else "Widgets", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Visual Level Progress Indicator (Cuanto falta para subir de nivel)
            val currentLvl = stats?.level ?: 1
            val currentXp = stats?.xp ?: 0
            val requiredXp = com.example.data.GameRules.getRequiredXp(currentLvl)
            val xpMissing = (requiredXp - currentXp).coerceAtLeast(0)
            val xpProgress = (currentXp.toFloat() / requiredXp.toFloat()).coerceIn(0f, 1f)

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pet.isSleeping) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                border = BorderStroke(1.dp, if (pet.isSleeping) Color(0xFF334155) else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ Progreso hacia Nivel ${currentLvl + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Faltan $xpMissing XP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pet.isSleeping) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (pet.isSleeping) Color(0xFF334155) else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(xpProgress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            // Modern Aesthetic Routine & Schedule Hub Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pet.isSleeping) Color(0x19FFFFFF) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🕒", fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Rutina & Horario Biológico",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = routineInfo.second,
                                    fontSize = 10.sp,
                                    color = if (pet.isSleeping) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = routineInfo.first,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Visual Stepper Timeline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val phases = listOf(
                            Triple(6..10, "🍳", "Desayuno"),
                            Triple(11..16, "🍽️", "Almuerzo"),
                            Triple(17..21, "🥣", "Cena"),
                            Triple(22..24, "💤", "Sueño")
                        )
                        val activeIdx = when {
                            currentHour in 6..10 -> 0
                            currentHour in 11..16 -> 1
                            currentHour in 17..21 -> 2
                            else -> 3
                        }
                        phases.forEachIndexed { idx, (_, emoji, name) ->
                            val isActive = idx == activeIdx
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isActive) {
                                    if (pet.isSleeping) Color(0x44FFFFFF) else MaterialTheme.colorScheme.primary
                                } else {
                                    if (pet.isSleeping) Color(0x11FFFFFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                                ) {
                                    Text(emoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = name,
                                        fontSize = 9.sp,
                                        fontWeight = if (isActive) FontWeight.Black else FontWeight.Medium,
                                        color = if (isActive) Color.White else if (pet.isSleeping) Color.White.copy(0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Urgent Alerts / Avisos de Atención Urgente
            val urgentAlerts = remember(pet.hunger, pet.energy, pet.happiness, pet.cleanliness) {
                mutableListOf<Pair<String, String>>().apply {
                    if (pet.hunger < 35f) {
                        add("¡Tengo mucha hambre! 🍗" to "Alimenta a ${pet.name} en la cocina costeña pronto.")
                    }
                    if (pet.energy < 30f) {
                        add("¡Estoy muy cansado! 😴" to "Lleva a ${pet.name} a tomar una siesta en el dormitorio.")
                    }
                    if (pet.happiness < 40f) {
                        add("¡Me siento triste! 🥺" to "Juega algún minijuego con ${pet.name} para animarlo.")
                    }
                    if (pet.cleanliness < 35f) {
                        add("¡Estoy muy sucio! 🧼" to "Dale un baño refrescante a ${pet.name} para mantenerlo aseado.")
                    }
                }
            }

            if (urgentAlerts.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Alerta Urgente",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Avisos de Atención Urgente ⚠️",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        urgentAlerts.forEach { (title, desc) ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(end = 6.dp))
                                Column {
                                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                    Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }

            // ANDROID HOME SCREEN WIDGET SIMULATOR PANEL
            AnimatedVisibility(
                visible = showWidgetsHub,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F5)),
                    border = BorderStroke(1.5.dp, Color(0xFF90A4AE).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Live Companion Widget 📱",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF37474F)
                                )
                            }
                            Text(
                                "Escritorio 4x1",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, Color(0xFFCFD8DC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (pet.type) {
                                                "SHIBA" -> Color(0xFFFFF3E0)
                                                "SLIME" -> Color(0xFFA1E3FF)
                                                "KITTY" -> Color(0xFFFFF1F3)
                                                "DRACO" -> Color(0xFFC7F7C4)
                                                "AXOLOTL" -> Color(0xFFFCE4EC)
                                                else -> Color.White
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val petEmoji = when (pet.type) {
                                        "SHIBA" -> "🐶"
                                        "SLIME" -> "💧"
                                        "KITTY" -> "🐱"
                                        "DRACO" -> "🐉"
                                        "AXOLOTL" -> "🌸"
                                        else -> "🐾"
                                    }
                                    Text(petEmoji, fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = pet.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF1E293B)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (pet.isSleeping) Color(0xFF475569) else Color(0xFFE2F0D9)
                                                )
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (pet.isSleeping) "Zzz..." else "Jugando",
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (pet.isSleeping) Color.White else Color(0xFF388E3C)
                                            )
                                        }
                                    }
                                    
                                    val activeLiveActivity = when (currentRoom) {
                                        "LIVING_ROOM" -> if (pet.isSleeping) "Durmiendo plácidamente en el sofá 🛋️" else "Explorando la sala y buscando de comer 🧸"
                                        "GARDEN" -> if (isWalking) "Paseando alegremente por el parque costeño 🌳" else "Buscando grillos y oliendo flores 🌸"
                                        "KITCHEN" -> "Esperando que cocines deliciosa comida costeña 🧑‍🍳"
                                        "BEDROOM" -> "Metido debajo de las sábanas soñando despierto ☁️"
                                        else -> "Jugando feliz"
                                    }

                                    Text(
                                        text = activeLiveActivity,
                                        fontSize = 10.sp,
                                        color = Color(0xFF546E7A),
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Compact Room Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val rooms = listOf(
                    Triple("LIVING_ROOM", "🛋️", "Sala"),
                    Triple("GARDEN", "🌸", "Jardín"),
                    Triple("BEDROOM", "🛏️", "Dormitorio")
                )
                rooms.forEach { (roomId, emoji, label) ->
                    val isSelected = currentRoom == roomId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            if (pet.isSleeping) Color(0x33FFFFFF) else MaterialTheme.colorScheme.primary
                        } else {
                            if (pet.isSleeping) Color(0x19FFFFFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clickable { currentRoom = roomId }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = emoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) Color.White else if (pet.isSleeping) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Central Interactive Pet Area (Artistic Presentation Style matching initial screen)
            val petHighlightColor = when (pet.type) {
                "SHIBA" -> Color(0xFFFF9800)
                "SLIME" -> Color(0xFF29B6F6)
                "KITTY" -> Color(0xFFF06292)
                "DRACO" -> Color(0xFF66BB6A)
                "AXOLOTL" -> Color(0xFFEC407A)
                else -> MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(275.dp)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(26.dp))
                        .background(if (pet.isSleeping) Color(0xFF1E293B) else petHighlightColor.copy(alpha = 0.14f))
                        .border(1.5.dp, if (pet.isSleeping) Color.Transparent else petHighlightColor.copy(alpha = 0.35f), RoundedCornerShape(26.dp))
                        .clickable { viewModel.interactWithActivePet() },
                    contentAlignment = Alignment.Center
                ) {
                    // Background artistic aura circles (just like initial presentation!)
                    if (!pet.isSleeping) {
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .background(petHighlightColor.copy(alpha = 0.16f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(135.dp)
                                .background(petHighlightColor.copy(alpha = 0.22f), CircleShape)
                        )
                    }

                    // Room decor badges & emojis
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentRoom) {
                            "LIVING_ROOM" -> {
                                Text("🛋️", fontSize = 38.sp, modifier = Modifier.align(Alignment.BottomStart).offset(16.dp, (-12).dp))
                                Text("📺", fontSize = 30.sp, modifier = Modifier.align(Alignment.BottomEnd).offset((-16).dp, (-12).dp))
                                Text("🌴", fontSize = 32.sp, modifier = Modifier.align(Alignment.TopStart).offset(16.dp, 16.dp))
                            }
                            "GARDEN" -> {
                                Text("⛲", fontSize = 36.sp, modifier = Modifier.align(Alignment.BottomStart).offset(16.dp, (-12).dp))
                                Text("🌻", fontSize = 28.sp, modifier = Modifier.align(Alignment.BottomEnd).offset((-24).dp, (-12).dp))
                                Text("🦜", fontSize = 26.sp, modifier = Modifier.align(Alignment.TopStart).offset(20.dp, 16.dp))
                            }
                            "BEDROOM" -> {
                                Text("🧸", fontSize = 30.sp, modifier = Modifier.align(Alignment.BottomStart).offset(20.dp, (-12).dp))
                                Text("🛏️", fontSize = 36.sp, modifier = Modifier.align(Alignment.BottomEnd).offset((-16).dp, (-12).dp))
                                Text("🌙", fontSize = 26.sp, modifier = Modifier.align(Alignment.TopStart).offset(20.dp, 16.dp))
                            }
                        }
                    }

                    PetRenderer(
                        type = pet.type,
                        isSleeping = pet.isSleeping,
                        hunger = pet.hunger,
                        happiness = pet.happiness,
                        equippedHat = pet.equippedHat,
                        equippedAccessory = pet.equippedAccessory,
                        groomingAction = activeGroomingAction,
                        level = stats?.level ?: 1,
                        interactionTrigger = interactionTrigger,
                        modifier = Modifier
                            .size(215.dp)
                            .testTag("pet_viewport")
                    )
                }

                // Interactive badge instruction at top center
                if (!pet.isSleeping) {
                    Surface(
                        color = petHighlightColor.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 10.dp)
                            .clickable { viewModel.interactWithActivePet() }
                    ) {
                        Text(
                            text = "✨ ¡Tócame para Mimos & Animación de Etapa!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (pet.isSleeping) {
                    Text(
                        text = "Zzz...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFA5F3FC),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-24).dp, y = (16).dp)
                    )
                } else if (pet.hunger < 35f) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.TopCenter).offset(y = 8.dp)
                    ) {
                        Text(
                            text = "¡Tengo Hambre! 🍗",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Botón flotante para la Pasarela
                Surface(
                    onClick = { showWardrobeTester = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pasarela", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }

            // Spacious 2x2 Pet Status Grid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pet.isSleeping) Color(0x22FFFFFF) else MaterialTheme.colorScheme.surface
                ),
                border = if (!pet.isSleeping) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (pet.isSleeping) Color(0x19FFFFFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.padding(10.dp)) {
                                StatMeter(
                                    label = "Hambre",
                                    value = pet.hunger,
                                    icon = Icons.Filled.Restaurant,
                                    tint = SoftOliveAccent,
                                    isSleeping = pet.isSleeping
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (pet.isSleeping) Color(0x19FFFFFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.padding(10.dp)) {
                                StatMeter(
                                    label = "Energía",
                                    value = pet.energy,
                                    icon = Icons.Filled.ElectricBolt,
                                    tint = Color(0xFFD19B00),
                                    isSleeping = pet.isSleeping
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (pet.isSleeping) Color(0x19FFFFFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.padding(10.dp)) {
                                StatMeter(
                                    label = "Ánimo",
                                    value = pet.happiness,
                                    icon = Icons.Filled.Mood,
                                    tint = SoftOliveAccent,
                                    isSleeping = pet.isSleeping
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (pet.isSleeping) Color(0x19FFFFFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.padding(10.dp)) {
                                StatMeter(
                                    label = "Aseo",
                                    value = pet.cleanliness,
                                    icon = Icons.Filled.Soap,
                                    tint = SoftOliveAccent,
                                    isSleeping = pet.isSleeping
                                )
                            }
                        }
                    }
                }
            }

            // ------------------ HABITACIÓN ESPECÍFICOS PANELS ------------------

            // 2. GARDEN ROOM: Paseos costeños
            if (currentRoom == "GARDEN" && !pet.isSleeping) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = BorderStroke(1.5.dp, Color(0xFF4CAF50).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌴", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Parque de Aventuras & Paseos 🦮",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                        
                        if (isWalking) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = walkEventText,
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            LinearProgressIndicator(
                                progress = { walkProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF4CAF50),
                                trackColor = Color(0xFFC8E6C9)
                            )
                        } else {
                            Text(
                                "Llévalo a pasear para que explore. ¡Gastará un poco de energía pero recolectará monedas!",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    if (pet.energy >= 20f) {
                                        isWalking = true
                                    } else {
                                        viewModel.walkActivePet()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Icon(Icons.Filled.DirectionsWalk, "Walk Icon", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("¡Llevar a Pasear! 🌳🚶", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Floating Accessories indicator
            if (pet.equippedHat != null) {
                Text(
                    text = "Llevando: ${pet.equippedHat}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (pet.isSleeping) Color.White.copy(0.4f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            } // close scrollable central body column

            Spacer(modifier = Modifier.height(4.dp))

            // Grooming options choices bar
            AnimatedVisibility(
                visible = showGroomingSelector && !pet.isSleeping,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "Opciones de Cuidado & Baño ✨",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                modifier = Modifier.weight(1f).clickable {
                                    activeGroomingAction = "SOAP"
                                    viewModel.soapActivePet(useShampoo = false)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
                                border = BorderStroke(1.dp, Color(0xFF00ACC1))
                            ) {
                                Text(
                                    text = "🧼 Jabón",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF006064),
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                    style = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                )
                            }

                            Card(
                                modifier = Modifier.weight(1.1f).clickable {
                                    activeGroomingAction = "SHOWER"
                                    viewModel.rinseActivePet()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                                border = BorderStroke(1.dp, Color(0xFF03A9F4))
                            ) {
                                Text(
                                    text = "🚿 Enjuagar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF01579B),
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                    style = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                )
                            }

                            Card(
                                modifier = Modifier.weight(1f).clickable {
                                    activeGroomingAction = "BRUSH"
                                    viewModel.brushActivePetTeeth()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                border = BorderStroke(1.dp, Color(0xFF4CAF50))
                            ) {
                                Text(
                                    text = "🪥 Dientes",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20),
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                    style = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                )
                            }
                        }
                    }
                }
            }

            // Primary Interactive Action Dock (custom-themed buttons aligned to color rules)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pet.isSleeping) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                ),
                border = if (!pet.isSleeping) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Food Button (Active highlighting in light moss container)
                    ActionButton(
                        onClick = { showFoodPanel = !showFoodPanel },
                        label = "Alimentar",
                        icon = Icons.Filled.RestaurantMenu,
                        containerColor = if (pet.isSleeping) Color(0xFF334155) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        testTag = "feed_action_button"
                    )

                    // 2. Clean Button (Sage secondary tones, toggles beautiful selectors)
                    ActionButton(
                        onClick = { showGroomingSelector = !showGroomingSelector },
                        label = "Cuidado",
                        icon = Icons.Filled.AutoAwesome,
                        containerColor = if (showGroomingSelector) MaterialTheme.colorScheme.primaryContainer else if (pet.isSleeping) Color(0xFF334155) else MaterialTheme.colorScheme.secondary,
                        contentColor = if (showGroomingSelector) MaterialTheme.colorScheme.onPrimaryContainer else if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.onSecondary,
                        testTag = "bathe_action_button"
                    )

                    // 3. Sleep Button (Sage secondary tones)
                    ActionButton(
                        onClick = { viewModel.toggleSleepActivePet() },
                        label = if (pet.isSleeping) "Despertar" else "Dormir",
                        icon = if (pet.isSleeping) Icons.Filled.WbSunny else Icons.Filled.NightsStay,
                        containerColor = if (pet.isSleeping) Color(0xFFFFFDE7) else MaterialTheme.colorScheme.secondary,
                        contentColor = if (pet.isSleeping) Color(0xFFF57F17) else MaterialTheme.colorScheme.onSecondary,
                        testTag = "sleep_action_button"
                    )

                    // 4. Minigames Launcher Button (Sage secondary tones)
                    ActionButton(
                        onClick = onOpenGames,
                        label = "Juegos",
                        icon = Icons.Filled.SportsEsports,
                        containerColor = if (pet.isSleeping) Color(0xFF334155) else MaterialTheme.colorScheme.secondary,
                        contentColor = if (pet.isSleeping) Color.White else MaterialTheme.colorScheme.onSecondary,
                        testTag = "games_action_button"
                    )
                }
            }
        }

        // Floating Food / Potion Panel Shelf
        AnimatedVisibility(
            visible = showFoodPanel,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Despensa de ${pet.name} 🧁",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { showFoodPanel = false }) {
                            Icon(Icons.Filled.Close, "Dismiss Panel")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val favText = when (pet.type) {
                        "SHIBA" -> "🍔 Hamburguesa, 🍕 Pizza, 🍎 Manzana"
                        "SLIME" -> "🍜 Ramen, 🍦 Helado, 🍎 Manzana"
                        "KITTY" -> "🍣 Sushi, 🥛 Leche, 🐟 Pescado"
                        "DRACO" -> "🍕 Pizza, 🍔 Hamburguesa, 🌭 Salchipapas"
                        "AXOLOTL" -> "🍣 Sushi, 🍰 Pastel, 🍪 Galletas"
                        else -> "🍎 Manzana Dulce"
                    }
                    val repText = when (pet.type) {
                        "SHIBA" -> "🍣 Sushi, 🍜 Ramen"
                        "SLIME" -> "🍕 Pizza, 🍪 Galletas"
                        "KITTY" -> "🍔 Hamburguesa, 🍎 Manzana"
                        "DRACO" -> "🍦 Helado, 🥛 Leche"
                        "AXOLOTL" -> "🍔 Hamburguesa, 🍕 Pizza"
                        else -> "Ninguna"
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("😍 Favoritas:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(favText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🤢 Repulsión:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(repText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }

                    if (foodItems.isEmpty() || foodItems.all { it.quantity == 0 }) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "No posees alimentos ni pociones de recarga.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "¡Ve a conseguir monedas en los minijuegos o visita la Tienda!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(foodItems) { item ->
                                val isFav = when (pet.type) {
                                    "SHIBA" -> item.itemId in listOf("FOOD_BURGER", "FOOD_PIZZA", "FOOD_APPLE")
                                    "SLIME" -> item.itemId in listOf("FOOD_RAMEN", "FOOD_ICECREAM", "FOOD_APPLE")
                                    "KITTY" -> item.itemId in listOf("FOOD_SUSHI", "FOOD_MILK", "ING_PESCADO")
                                    "DRACO" -> item.itemId in listOf("FOOD_PIZZA", "FOOD_BURGER", "RECIPE_SALCHICHA_PAPAS")
                                    "AXOLOTL" -> item.itemId in listOf("FOOD_SUSHI", "FOOD_CAKE", "FOOD_COOKIE")
                                    else -> false
                                }
                                val isRep = when (pet.type) {
                                    "SHIBA" -> item.itemId in listOf("FOOD_SUSHI", "FOOD_RAMEN")
                                    "SLIME" -> item.itemId in listOf("FOOD_PIZZA", "FOOD_COOKIE")
                                    "KITTY" -> item.itemId in listOf("FOOD_BURGER", "FOOD_APPLE")
                                    "DRACO" -> item.itemId in listOf("FOOD_ICECREAM", "FOOD_MILK")
                                    "AXOLOTL" -> item.itemId in listOf("FOOD_BURGER", "FOOD_PIZZA")
                                    else -> false
                                }
                                FoodSackItem(
                                    item = item,
                                    isFavorite = isFav,
                                    isDisliked = isRep,
                                    onClick = { viewModel.feedActivePet(item.itemId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShareCodeDialog) {
        AlertDialog(
            onDismissRequest = { showShareCodeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Código de Vinculación Permanente")
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Comparte este código con otro usuario para vincular sus mascotas. El usuario vinculado tendrá permisos completos de lectura/escritura sobre tu mascota.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = permanentCodeToShare,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🔒 Código permanente (No expira)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (permanentCodeToShare.isNotBlank()) {
                            clipboardManager.setText(AnnotatedString(permanentCodeToShare))
                            viewModel.triggerToast("📋 ¡Código copiado al portapapeles!")
                        }
                        showShareCodeDialog = false
                    }
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar Código")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareCodeDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun StatMeter(
    label: String,
    value: Float,
    icon: ImageVector,
    tint: Color,
    isSleeping: Boolean
) {
    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = value / 100f,
        animationSpec = androidx.compose.animation.core.tween(1000)
    )
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isSleeping) Color.White.copy(0.4f) else tint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSleeping) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${value.toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = if (value < 30f) Color(0xFFC62828) else if (isSleeping) Color.White else tint
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = if (value < 30f) Color(0xFFEF5350) else tint,
            trackColor = if (isSleeping) Color(0x22FFFFFF) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ActionButton(
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(containerColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FoodSackItem(
    item: InventoryEntity,
    isFavorite: Boolean = false,
    isDisliked: Boolean = false,
    onClick: () -> Unit
) {
    val visualEmoji = when {
        item.itemId.startsWith("FOOD_") -> {
            when (item.itemId) {
                "FOOD_APPLE" -> "🍎"
                "FOOD_SUSHI" -> "🍣"
                "FOOD_PIZZA" -> "🍕"
                "FOOD_CAKE" -> "🍰"
                "FOOD_COOKIE" -> "🍪"
                "FOOD_BURGER" -> "🍔"
                "FOOD_ICECREAM" -> "🍦"
                "FOOD_MILK" -> "🥛"
                "FOOD_RAMEN" -> "🍜"
                else -> "🍩"
            }
        }
        item.itemId.startsWith("RECIPE_") -> {
            when (item.itemId) {
                "RECIPE_AREPA_HUEVO" -> "🌾"
                "RECIPE_PATACONES_QUESO" -> "🍌"
                "RECIPE_SALCHICHA_PAPAS" -> "🌭"
                "RECIPE_COCADAS" -> "🍬"
                "RECIPE_CHOCO_QUESO" -> "🍫"
                "RECIPE_PESCADO_PATACON" -> "🐟"
                "RECIPE_MOTE_QUESO" -> "🍲"
                "RECIPE_ARROZ_COCO" -> "🍚"
                "RECIPE_CARIMANOLA" -> "🥟"
                "RECIPE_CAZUELA" -> "🍲"
                "RECIPE_ARROZ_LISA" -> "🍚"
                else -> "🍲"
            }
        }
        item.itemId == "POTION_ENERGY" -> "⚡"
        item.itemId == "POTION_LOVE" -> "💖"
        else -> "📦"
    }

    val cardBorder = when {
        isFavorite -> BorderStroke(2.dp, Color(0xFF4CAF50))
        isDisliked -> BorderStroke(2.dp, Color(0xFFE53935))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = cardBorder,
        modifier = Modifier
            .width(115.dp)
            .height(120.dp)
            .clickable(enabled = item.quantity > 0, onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = visualEmoji,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (item.itemId.startsWith("RECIPE_")) {
                        item.name.replace(Regex("[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]"), "").trim().split(" ").take(2).joinToString(" ")
                    } else {
                        item.name.substringBefore(" ")
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "+${item.valueBoost.toInt()}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (isFavorite) {
                Surface(
                    color = Color(0xFF2E7D32),
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text("😍 Fav", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            } else if (isDisliked) {
                Surface(
                    color = Color(0xFFC62828),
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text("🤢 Rep", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }

            // Quantity Tag Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (item.quantity > 0) MaterialTheme.colorScheme.primary else Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.quantity}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

