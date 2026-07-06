package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.PetRenderer

data class PetOption(
    val type: String,
    val title: String,
    val icon: String,
    val description: String,
    val lore: String,
    val highlightColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(
    viewModel: PetViewModel,
    onBack: (() -> Unit)? = null
) {
    var petName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("SHIBA") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewLevel by remember { mutableStateOf(1) }

    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    val unlockedLevel = userStats?.level ?: 1
    val isMysterious = previewLevel > unlockedLevel

    val petOptions = remember {
        listOf(
            PetOption(
                type = "SHIBA",
                title = "Shiba Inu 🐶",
                icon = "🐶",
                description = "Un perrito alegre, enérgico y fiel.",
                lore = "¡Le encanta jugar, comer carne frita y dar paseos divertidos por el parque virtual!",
                highlightColor = Color(0xFFE5A65D)
            ),
            PetOption(
                type = "SLIME",
                title = "Slime Burbuja 💧",
                icon = "💧",
                description = "Una criatura gelatinosa, saltarina y adorable.",
                lore = "Procedente del Valle Mágico. Duerme mucho y le encantan las pociones energéticas de estrellas.",
                highlightColor = Color(0xFF29B6F6)
            ),
            PetOption(
                type = "KITTY",
                title = "Gatito Kawaii 🐱",
                icon = "🐱",
                description = "Sometido a su realeza. Peludo y cariñoso.",
                lore = "Un felino juguetón que ama los bocadillos de sushi tiernos y las siestas calientes bajo el sol.",
                highlightColor = Color(0xFFF06292)
            ),
            PetOption(
                type = "DRACO",
                title = "Mini Dragón 🔥",
                icon = "🔥",
                description = "Una mística lagartija voladora que ama el picante.",
                lore = "¡Escupe pequeñas chispas juguetonas cuando está superfeliz! Su comida favorita son los tacos de cristal.",
                highlightColor = Color(0xFF66BB6A)
            ),
            PetOption(
                type = "AXOLOTL",
                title = "Axol Mágico 🌸",
                icon = "🌸",
                description = "Un anfibio estelar de la eterna juventud y asombro.",
                lore = "Regenera chispas de felicidad cósmica. Ama nadar en nubes de caramelo y comer estrellas cristalinas.",
                highlightColor = Color(0xFFEC407A)
            )
        )
    }

    val selectedOption = petOptions.find { it.type == selectedType } ?: petOptions.first()

    fun getConnectedSagaLore(petType: String, level: Int): String {
        return when (petType) {
            "SHIBA" -> when (level) {
                200 -> "📖 Cap. IV (Nvl 200 - Trascendencia Solar): Al abrir su Tercer Ojo Cósmico y esferas de fuego sagrado Magatama, se erige como el Dios Supremo de la Lealtad Eterna."
                100 -> "📖 Cap. III (Nvl 100 - Okami Celestial): Despierta las marcas rojas ancestrales y el Disco Solar de Amaterasu, iluminando el horizonte con poder divino."
                50 -> "📖 Cap. II (Nvl 50 - Caballero Áureo): Viste su capa real samurái y brazaletes de oro; jura lealtad inquebrantable protegiendo tu paz y energía cotidiana."
                else -> "📖 Cap. I (Nvl 1 - Destello Solar): Nace como un cachorrito juguetón en los Prados del Amanecer, siguiendo tu voz para iniciar una gran saga de fidelidad."
            }
            "SLIME" -> when (level) {
                200 -> "📖 Cap. IV (Nvl 200 - Núcleo Cuántico): Su ser alberga un microuniverso en expansión perpetua; armoniza la gravedad cuántica y otorga serenidad absoluta."
                100 -> "📖 Cap. III (Nvl 100 - Orbe Planetario): Manifiesta anillos planetarios y amatistas orbitales, sincronizando sus latidos de gel con el pulso sagrado del cosmos."
                50 -> "📖 Cap. II (Nvl 50 - Corona Alquímica): Sintetiza gemas interiores y una corona flotante de oro, dominando las corrientes secretas de agua y cristal místico."
                else -> "📖 Cap. I (Nvl 1 - Gota de Estrellas): Una pequeña gota gelatinosa caída de la Nebulosa Cristalina, absorbiendo tu afecto para estabilizar su tierna forma."
            }
            "KITTY" -> when (level) {
                200 -> "📖 Cap. IV (Nvl 200 - Emperatriz Astral): Flotando en un trono de nebulosa con collar de polvo de estrellas, entrelaza el destino de ambas almas en luz perpetua."
                100 -> "📖 Cap. III (Nvl 100 - Trono Estelar): Invoca una tiara de diamantes y un halo áureo, vigilando el santuario nocturno contra cualquier sombra o pesadilla."
                50 -> "📖 Cap. II (Nvl 50 - Manto Real): Coronada por la Luna Creciente en su testuz y vestida en seda real noble, se convierte en la guardiana de tus sueños."
                else -> "📖 Cap. I (Nvl 1 - Ronroneo Lunar): Un gatito de terciopelo que llegó en un rayo de luna plateada, ronroneando melodías suaves que curan el cansancio."
            }
            "DRACO" -> when (level) {
                200 -> "📖 Cap. IV (Nvl 200 - Supernova Primordial): El Corazón de Dragón Rubí resplandece en su pecho en un campo de plasma solar impenetrable y glorioso."
                100 -> "📖 Cap. III (Nvl 100 - Plasma Solar): Sus alas elementales de fuego cósmico y orbe solar flotante desintegran cualquier tristeza o energía negativa al instante."
                50 -> "📖 Cap. II (Nvl 50 - Escudo de Oro): Desarrolla armadura dorada en su testuz y alas robustas, jurando ser el escudo valeroso de todas tus metas y proyectos."
                else -> "📖 Cap. I (Nvl 1 - Chispa del Volcán): Recién nacido del Huevo de Obsidiana, lanza chispitas tiernas mientras aprende a volar a tu lado con gran curiosidad."
            }
            else -> when (level) {
                200 -> "📖 Cap. IV (Nvl 200 - Gran Axolotl Cósmico): Irradia una aurora boreal perpetua de dimensiones infinitas, conectando todos los reinos en una eterna juventud."
                100 -> "📖 Cap. III (Nvl 100 - Oráculo del Océano): Rodeado por lotos de luz acuática y esferas estelares, recuerda y magnifica cada instante feliz compartido."
                50 -> "📖 Cap. II (Nvl 50 - Tejedora de Corrientes): Sus branquias florecen con perlas de luz y aletas de seda marina, tejiendo lazos empáticos inquebrantables."
                else -> "📖 Cap. I (Nvl 1 - Sonrisa de Loto): Pequeño anfibio rosa con branquias de pluma que danza en aguas cristalinas, trayendo magia y frescura a tu vida."
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Adopta un Compañero",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.Pets, "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Elige tu nueva mascota virtual!",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stage Selector Pills (Nivel 1, Nivel 50, Nivel 100)
            Text(
                text = "✨ Previsualiza sus etapas de crecimiento:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val stages = listOf(
                    Triple(1, "🌱 Bebé", "Nvl 1"),
                    Triple(50, "⭐ Adulto", "Nvl 50"),
                    Triple(100, "👑 Legend.", "Nvl 100"),
                    Triple(200, "🌌 Definitivo", "Nvl 200")
                )
                stages.forEach { (lvl, title, sub) ->
                    val isSel = previewLevel == lvl
                    val isStageLocked = lvl > unlockedLevel
                    val displayTitle = if (isStageLocked) "❓ Misterio" else title
                    val displaySub = if (isStageLocked) "🔒 $sub" else sub

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) selectedOption.highlightColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (isSel) selectedOption.highlightColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { previewLevel = lvl }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = displayTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = displaySub,
                                fontSize = 9.sp,
                                color = if (isSel) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large Animated Mascot Preview Room
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isMysterious) Color(0xFF0F172A) else selectedOption.highlightColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                // Background aura elements
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            if (isMysterious) Color(0xFF6B21A8).copy(alpha = 0.25f)
                            else selectedOption.highlightColor.copy(alpha = 0.15f),
                            CircleShape
                        )
                )

                PetRenderer(
                    type = selectedType,
                    isSleeping = false,
                    hunger = 90f,
                    happiness = 90f,
                    equippedHat = null,
                    level = previewLevel,
                    modifier = Modifier
                        .size(170.dp)
                        .padding(16.dp),
                    isSilhouette = isMysterious
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isMysterious) Color(0xFF1E293B).copy(alpha = 0.8f) else selectedOption.highlightColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, if (isMysterious) Color(0xFFAB47BC).copy(alpha = 0.5f) else selectedOption.highlightColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(
                    text = if (isMysterious) {
                        "🔒 FORMA MISTERIOSA BLOQUEADA\nAlcanza el Nivel $previewLevel para disipar la niebla mística, revelar la verdadera apariencia de esta evolución y desbloquear todo su poder ancestral ✨"
                    } else {
                        getConnectedSagaLore(selectedType, previewLevel)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isMysterious) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Carousel/Grid Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                petOptions.forEach { opt ->
                    val isSelected = opt.type == selectedType
                    OutlinedCard(
                        onClick = { selectedType = opt.type },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) opt.highlightColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) opt.highlightColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .aspectRatio(0.92f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = opt.icon,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = opt.type.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                color = if (isSelected) opt.highlightColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description info card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = selectedOption.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = selectedOption.highlightColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedOption.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedOption.lore,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Name and validation
            Text(
                text = "Dale un nombre especial:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = petName,
                onValueChange = {
                    petName = it
                    if (it.isNotBlank()) errorMessage = null
                },
                placeholder = { Text("Ej: Pompón, Kiko, Toby...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pet_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedOption.highlightColor,
                    focusedLabelColor = selectedOption.highlightColor
                ),
                isError = errorMessage != null
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 4.dp, start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Adapt button (target height 48dp+)
            Button(
                onClick = {
                    if (petName.trim().isEmpty()) {
                        errorMessage = "¡Por favor escribe un nombre!"
                    } else {
                        viewModel.adoptNewPet(petName.trim(), selectedType)
                        onBack?.invoke()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("adopt_pet_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedOption.highlightColor
                )
            ) {
                Icon(Icons.Filled.Star, contentDescription = "Adopt", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "¡ADOPTAR AHORA! 🎉",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
