package com.example.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserStatsEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    stats: UserStatsEntity?,
    onSave: (String, String) -> Unit,
    onGoToCoCare: () -> Unit = {},
    onThemeChanged: (String) -> Unit = {},
    onNotificationsChanged: (Boolean) -> Unit = {},
    onRoutineHourChanged: (Int) -> Unit = {},
    onReleaseOrDeletePet: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE) }

    val isAlreadyCreated = remember {
        prefs.getBoolean("profile_created", false) || (stats?.myName != null && stats.myName != "Cuidador")
    }

    var isEditing by remember { mutableStateOf(!isAlreadyCreated) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showBlindajeDialog by remember { mutableStateOf(false) }
    val myPersistentUid = remember { com.example.data.CoMascotaSyncService.getFirebaseUid(context) }

    var name by remember { mutableStateOf(stats?.myName ?: prefs.getString("saved_name", "Cuidador") ?: "Cuidador") }
    var selectedAvatar by remember { mutableStateOf(stats?.myAvatar ?: prefs.getString("saved_avatar", "👤") ?: "👤") }
    var selectedRole by remember { mutableStateOf(prefs.getString("saved_role", "Mami Perruna") ?: "Mami Perruna") }
    var motto by remember { mutableStateOf(prefs.getString("saved_motto", "¡Adoro consentir y alimentar a mis mascotas! 🐾") ?: "¡Adoro consentir y alimentar a mis mascotas! 🐾") }

    val avatars = listOf("👤", "👩", "👨", "👦", "👧", "🧑", "👵", "🧓", "🤖", "👽", "👾", "🦸")
    val roles = listOf("Mami Perruna 🐶", "Papá Consentidor 🧢", "Mejor Amigo 🌟", "Veterinario Estrella 🩺", "Cuidador Oficial 🏆", "Hermano Mayor 🧑‍🤝‍🧑")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isEditing) {
            // VIEW PROFILE MODE
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedAvatar, fontSize = 48.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = name,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = selectedRole,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "\"$motto\"",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NIVEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${stats?.level ?: 1}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MONEDAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${stats?.coins ?: 0} 🪙", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFA000))
                        }
                    }

                    if (stats?.isLinked == true) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = stats.partnerAvatar ?: "👤", fontSize = 22.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("COMPAÑERO VINCULADO 🤝", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
                                    Text(stats.partnerName ?: "Compañero", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                ) {
                                    Text("En vivo 🟢", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onGoToCoCare,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Icon(Icons.Filled.Group, contentDescription = "Co-Care")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gestionar Co-Care", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar Perfil", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val isOwner = stats?.linkRole == "dueño" || stats?.linkRole == null
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isOwner) "Liberar / Eliminar Mascota" else "Abandonar Cuidado Compartido", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BLINDAJE DE CUENTA & ID PERSISTENTE
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, Color(0xFF1B5E20).copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = "Blindaje", tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🛡️ Blindaje de Cuenta & ID", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Tu ID Persistente (UID):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = myPersistentUid,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "✅ Sesión 100% protegida ante actualizaciones de la app. Nunca se ejecuta signOut ni se borran tus SharedPreferences.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showBlindajeDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Filled.VpnKey, contentDescription = "Vincular", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Más sobre Blindaje y Vincular Cuenta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CONFIGURACIÓN DE APP & TEMAS
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "⚙️ Configuración de PawPair",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("🎨 Cambiar Tema Visual", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val themes = listOf(
                        "FOREST" to "🌿 Bosque",
                        "SUNSET" to "🌅 Atardecer",
                        "OCEAN" to "🌊 Océano",
                        "CANDY" to "🍬 Dulce",
                        "CYBER" to "👾 Neón"
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(96.dp)
                    ) {
                        items(themes) { (code, label) ->
                            val isSelected = (stats?.appTheme ?: "FOREST") == code
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .clickable { onThemeChanged(code) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal)
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Notificaciones push
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🔔 Notificaciones Push", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Avisos automáticos según rutinas y necesidades", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = stats?.notificationsEnabled ?: true,
                            onCheckedChange = { onNotificationsChanged(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hora de rutina diaria
                    Text("⏰ Hora de Rutina Diaria", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Recibe un recordatorio para revisar y cuidar a tu mascota", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    val routineOptions = listOf(12 to "12:00 PM", 17 to "5:00 PM", 20 to "8:00 PM", 22 to "10:00 PM")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        routineOptions.forEach { (hour, label) ->
                            val selected = (stats?.routineHour ?: 20) == hour
                            OutlinedButton(
                                onClick = { onRoutineHourChanged(hour) },
                                shape = RoundedCornerShape(10.dp),
                                colors = if (selected) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors(),
                                border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else ButtonDefaults.outlinedButtonBorder(),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        } else {
            // EDIT / CREATE PROFILE MODE
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAlreadyCreated) "Editar Perfil" else "Crear tu Perfil",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tu Nombre o Apodo") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Título / Rol en la Familia", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(110.dp)
                    ) {
                        items(roles) { role ->
                            val isSel = role == selectedRole
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSel) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.clickable { selectedRole = role }
                            ) {
                                Text(
                                    text = role,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = motto,
                        onValueChange = { motto = it },
                        label = { Text("Tu Lema o Frase Favorita") },
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Elige tu Avatar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(108.dp)
                    ) {
                        items(avatars) { avatar ->
                            val isSelected = avatar == selectedAvatar
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedAvatar = avatar }
                                    .padding(4.dp)
                            ) {
                                Text(text = avatar, fontSize = 22.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            prefs.edit()
                                .putBoolean("profile_created", true)
                                .putString("saved_name", name)
                                .putString("saved_avatar", selectedAvatar)
                                .putString("saved_role", selectedRole)
                                .putString("saved_motto", motto)
                                .apply()

                            onSave(name, selectedAvatar)
                            isEditing = false
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "Guardar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar y Ver Perfil", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        val isOwner = stats?.linkRole == "dueño" || stats?.linkRole == null
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(if (isOwner) "⚠️ ¿Eliminar Mascota?" else "⚠️ ¿Abandonar Cuidado?", fontWeight = FontWeight.Bold) },
            text = {
                Text(if (isOwner) "Esta acción borrará a tu mascota y su progreso de forma permanente para todos los cuidadores. ¿Estás seguro?" else "Dejarás de compartir el cuidado de esta mascota. El dueño seguirá conservándola.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onReleaseOrDeletePet(isOwner)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showBlindajeDialog) {
        AlertDialog(
            onDismissRequest = { showBlindajeDialog = false },
            icon = { Icon(Icons.Filled.Security, contentDescription = "Security", tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp)) },
            title = { Text("🛡️ Blindaje Total de Cuenta", fontWeight = FontWeight.Black) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Tu cuenta tiene 3 niveles de protección:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1️⃣ ID Persistente en SharedPreferences\nAl abrir la app, verificamos primero tu sesión activa. Si es null, recuperamos tu UID guardado en SharedPreferences para que jamás pierdas a tu mascota al reiniciar.", fontSize = 12.sp, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("2️⃣ Sin Reinicio de Sesión\nEliminamos por completo cualquier llamada a signOut() o limpieza de caché/SharedPreferences al actualizar de versión.", fontSize = 12.sp, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("3️⃣ Firma Consistente (Keystore)\nMantén siempre el mismo archivo keystore (.jks) al compilar tu APK de actualización para que Android conserve intactos todos los datos locales.", fontSize = 12.sp, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("💡 Blindaje Extra Recomendado:\nEn un entorno de producción, puedes vincular tu usuario anónimo de Firebase con Google Sign-In mediante linkWithCredential(googleCred). Así, incluso si cambias de teléfono o desinstalas la app, recuperarás tu mascota instantáneamente al iniciar sesión con tu cuenta de Google.", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20), lineHeight = 16.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBlindajeDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("¡Entendido y Blindado! 🛡️")
                }
            }
        )
    }
}
