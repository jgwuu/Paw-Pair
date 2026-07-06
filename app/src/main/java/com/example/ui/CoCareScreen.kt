package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.CoMascotaSyncService
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityLogEntity
import com.example.data.PetEntity
import com.example.data.UserStatsEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CoCareScreen(
    stats: UserStatsEntity?,
    activePet: PetEntity?,
    logs: List<ActivityLogEntity>,
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    var partnerNameInput by remember { mutableStateOf("") }
    var partnerCodeInput by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val isLinked = stats?.isLinked ?: false
    val myLinkCode = stats?.linkCode ?: "XXXX-XXXX"
    val partnerName = stats?.partnerName ?: "Tu Compañero"

    DisposableEffect(myLinkCode) {
        if (!myLinkCode.isBlank() && myLinkCode.length == 6 && myLinkCode != "XXXX-XXXX") {
            com.example.data.RealtimeSyncManager.startListening(myLinkCode) { _ -> }
        }
        onDispose {
            com.example.data.RealtimeSyncManager.stopListening()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("co_care_tab_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Title
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "Vínculo Co-Care 🤝",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Cria y alimenta tu mascota virtual junto a otra persona",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                )
            }
        }

        // Firebase Config Error Warning Banner (Always visible if incorrect)
        val configError = CoMascotaSyncService.firebaseConfigError
        if (configError != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Error de Configuración",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Error de Configuración de Firebase ⚠️",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = configError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Por favor, ve a la Consola de Firebase -> Realtime Database y copia la URL correcta (empieza con https:// y termina con .firebaseio.com o .firebasedatabase.app), luego actualízala en tu configuración (.env) con el nombre FIREBASE_DATABASE_URL.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (!isLinked) {
            // UNLINKED STATE VIEWS
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.GroupAdd, "Link instructions", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Instrucciones de Vínculo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "¡Cuidar es más divertido de a dos! Comparte tu código de cuidador único de abajo con un amigo, o escribe su código para conectarse instantáneamente.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Display My Key Code Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tu Código de Cuidador Único 🔑",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Selectable Code display box
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = myLinkCode,
                                fontSize = 24.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp).testTag("my_share_code")
                            )
                        }

                        if (myLinkCode == "XXXX-XXXX" || myLinkCode.isBlank() || myLinkCode.length != 6) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.generateLinkCode() },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Generar Código Permanente (6 Caracteres) 🔑", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(myLinkCode))
                                        viewModel.triggerToast("¡Código copiado!")
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, "Copy Code", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copiar Código", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Linking Input Fields Form
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Enlazar con un Compañero",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = partnerCodeInput,
                            onValueChange = {
                                partnerCodeInput = it
                                if (it.isNotBlank()) formError = null
                            },
                            label = { Text("Código de Cuidador del Compañero") },
                            placeholder = { Text("Pega el código o el enlace aquí") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("partner_code_field")
                        )

                        if (formError != null) {
                            Text(
                                text = formError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Connect Button (Target size 48dp+)
                        Button(
                            onClick = {
                                if (partnerCodeInput.trim().isBlank()) {
                                    formError = "¡Código de Cuidador no válido!"
                                } else {
                                    val finalCode = partnerCodeInput.trim()
                                    isLoading = true
                                    viewModel.linkWithPartnerCode(finalCode) {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("apply_link_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Vinculando...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            } else {
                                Icon(Icons.Filled.Link, "Link code")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vincular Mascota Compartida 🔗", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // LINKED STATE VIEWS
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pulsing connection badge
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                "Cuidado Cooperativo Activo 🤝",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Estás cuidando de la mascota virtual de forma conjunta en la nube local. Los alimentos, baños, cariños y monedas de minijuegos se sincronizan entre ambos.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Partner Status Board + Simulation playground
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Compañero Vinculado: $partnerName",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            "¿Quieres probar la sincronización de cuidado cooperativo? Presiona el botón de abajo para simular que tu compañero realiza una acción (alimentar, jugar, bañar o descansar) y comprueba cómo se actualizan las estadísticas y el diario en tiempo real.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f)
                        )

                        var isForceSyncing by remember { mutableStateOf(false) }

                        Button(
                            onClick = {
                                isForceSyncing = true
                                viewModel.pullSharedStateFromServer {
                                    isForceSyncing = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("force_pull_sync_btn"),
                            enabled = !isForceSyncing
                        ) {
                            if (isForceSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Sync, "Force cloud pull")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sincronizar con Compañero Real 🔄", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Simulate action button (target height 48dp+)
                            Button(
                                onClick = { viewModel.triggerSimulatedPartnerInteraction() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("simulate_partner_btn")
                            ) {
                                Icon(Icons.Filled.Bolt, "Simulate")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simular Acción", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Unlink partner button
                            Button(
                                onClick = { viewModel.unlinkCurrentPartner() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.5f)
                                    .height(48.dp)
                                    .testTag("unlink_partner_btn")
                            ) {
                                Text("Unlink 💔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Shared Achievements Board
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Logros Compartidos 🏆",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Jueguen minijuegos en equipo para desbloquear logros cooperativos.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f)
                        )

                        val achievements = listOf(
                            Pair("Recogedor Novato", (stats?.catcherPlayed ?: 0) >= 5),
                            Pair("Mente Brillante", (stats?.memoryPlayed ?: 0) >= 5),
                            Pair("Tres en Raya", (stats?.tictactoePlayed ?: 0) >= 5),
                            Pair("Sopa de Letras", (stats?.wordsearchPlayed ?: 0) >= 5),
                            Pair("Amigos de Verdad", (stats?.catcherPlayed ?: 0) + (stats?.memoryPlayed ?: 0) + (stats?.tictactoePlayed ?: 0) + (stats?.wordsearchPlayed ?: 0) >= 20)
                        )

                        achievements.forEach { (name, unlocked) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (unlocked) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Star",
                                        tint = if (unlocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        name,
                                        fontSize = 13.sp,
                                        fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Normal,
                                        color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                }
                                if (unlocked) {
                                    Text("¡Completado!", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                                } else {
                                    Text("Bloqueado", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }

            // Real Cohort Feed (Activity journal)
            item {
                Text(
                    text = "Diario de Actividades de ${activePet?.name ?: "Mascota"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (logs.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            "Sin actividades registradas por el momento.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            } else {
                items(logs) { log ->
                    ActivityLogFeedCard(log = log, partnerName = partnerName)
                }
            }
        }
    }
}

@Composable
fun ActivityLogFeedCard(
    log: ActivityLogEntity,
    partnerName: String
) {
    val isPartner = log.actorName == partnerName
    val borderCol = if (isPartner) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val bgCol = borderCol.copy(alpha = 0.05f)

    val actionIcon = when (log.actionType) {
        "FEED" -> Icons.Filled.Restaurant
        "PLAY" -> Icons.Filled.SportsEsports
        "BATH" -> Icons.Filled.Spa
        "SLEEP" -> Icons.Filled.NightsStay
        "BUY" -> Icons.Filled.ShoppingCart
        "LINK" -> Icons.Filled.Link
        else -> Icons.Filled.Info
    }

    val timeText = remember(log.timestamp) {
        val diff = System.currentTimeMillis() - log.timestamp
        val minutes = diff / (1000 * 60)
        when {
            minutes < 1 -> "hace un momento"
            minutes < 60 -> "hace $minutes min"
            minutes < 24 * 60 -> "hace ${minutes / 60} h"
            else -> "hace ${minutes / (24 * 60)} d"
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgCol),
        border = BorderStroke(1.dp, borderCol.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("log_card_${log.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(borderCol.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    actionIcon,
                    contentDescription = log.actionType,
                    tint = borderCol,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val actionName = when (log.actionType) {
                        "FEED" -> "Alimentado por"
                        "PLAY" -> "Jugó con"
                        "BATH" -> "Bañado por"
                        "SLEEP" -> "Acostado por"
                        "BUY" -> "Comprado por"
                        "LINK" -> "Vinculado por"
                        else -> "Acción de"
                    }
                    val actorText = if (log.actorName == "Tú" || log.actorName == "local_player") "Ti" else log.actorName
                    
                    Text(
                        text = "$actionName $actorText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = borderCol
                    )
                    Text(
                        text = timeText,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.actionDetails,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
