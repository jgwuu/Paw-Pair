package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class OutfitOption(
    val id: String,
    val name: String,
    val emoji: String,
    val isCloth: Boolean = false
)

@Composable
fun WardrobeTesterDialog(
    initialPetType: String = "SHIBA",
    initialHat: String? = null,
    initialAccessory: String? = null,
    onDismiss: () -> Unit,
    onApplyToActivePet: (hat: String?, accessory: String?) -> Unit
) {
    var selectedPetType by remember { mutableStateOf(initialPetType) }
    var selectedHat by remember { mutableStateOf(initialHat) }
    var selectedAccessory by remember { mutableStateOf(initialAccessory) }
    var selectedCategory by remember { mutableStateOf("HATS") } // "HATS" or "CLOTHES"

    val petTypes = listOf(
        Triple("SHIBA", "Shiba Inu", "🐶"),
        Triple("SLIME", "Astro Slime", "💧"),
        Triple("KITTY", "Neko Kitty", "🐱"),
        Triple("DRACO", "Mini Draco", "🐲"),
        Triple("AXOLOTL", "Axolotl", "🌸")
    )

    val allHats = remember {
        listOf(
            OutfitOption("CROWN", "Corona de Oro", "👑"),
            OutfitOption("ICE_CROWN", "Corona de Hielo", "❄️"),
            OutfitOption("FLAME", "Corona de Fuego", "🔥"),
            OutfitOption("SAMURAI", "Casco Samurái", "🏯"),
            OutfitOption("PHARAOH", "Tocado de Faraón", "🐍"),
            OutfitOption("TOP_HAT", "Sombrero de Copa", "🎩"),
            OutfitOption("SUNGLASSES", "Gafas de Sol", "🕶️"),
            OutfitOption("BOWTIE", "Pajarita Roja", "🎀"),
            OutfitOption("HALO", "Halo Celestial", "😇"),
            OutfitOption("WITCH", "Sombrero de Bruja", "🧙"),
            OutfitOption("REINDEER", "Astas de Reno", "🦌"),
            OutfitOption("STRAW_HAT", "Sombrero de Paja", "👒"),
            OutfitOption("DETECTIVE", "Gorra Detective", "🕵️"),
            OutfitOption("CAP", "Gorra Deportiva", "🧢"),
            OutfitOption("CAT_EARS", "Orejas de Gato", "🐱"),
            OutfitOption("CHEF", "Sombrero de Chef", "👨‍🍳"),
            OutfitOption("HEADPHONES", "Audífonos Gamer", "🎧"),
            OutfitOption("FLOWER", "Flor Primavera", "🌸"),
            OutfitOption("SCARF", "Bufanda Suave", "🧣"),
            OutfitOption("MONOCLE", "Monóculo de Oro", "🧐"),
            OutfitOption("MUSTACHE", "Bigote Elegante", "🥸"),
            OutfitOption("PARTY", "Gorrito Fiesta", "🥳"),
            OutfitOption("DIAMOND", "Collar Diamantes", "💎"),
            OutfitOption("DIAMOND_RING", "Anillo Diamante", "💍"),
            OutfitOption("PIRATE", "Sombrero Pirata", "🏴‍☠️"),
            OutfitOption("NINJA", "Bandana Ninja", "🥷"),
            OutfitOption("VIKING", "Casco Vikingo", "🛡️"),
            OutfitOption("ANGEL_WINGS", "Alas de Ángel", "🪽"),
            OutfitOption("DRAGON_WINGS", "Alas de Dragón", "🐉"),
            OutfitOption("UNICORN", "Cuerno Unicornio", "🦄"),
            OutfitOption("SUPERHERO", "Capa Superhéroe", "🦸‍♂️"),
            OutfitOption("TIARA", "Tiara Real", "👸"),
            OutfitOption("ALIEN", "Antenas Alien", "👽"),
            OutfitOption("FLOWER_CROWN", "Corona de Flores", "🌺"),
            OutfitOption("GOGGLES", "Gafas de Aviador", "🥽"),
            OutfitOption("PARTY_GLASSES", "Gafas de Fiesta", "🕶️"),
            OutfitOption("MAGICAL_AURA", "Aura Mágica", "✨"),
            OutfitOption("ROYAL_CAPE", "Capa Real", "👑"),
            OutfitOption("CYBER_VISOR", "Visor Cyberpunk", "🤖"),
            OutfitOption("GOLDEN_BELL", "Cascabel de Oro", "🔔"),
            OutfitOption("FLOWER_GARLAND", "Guirnalda Tropical", "🌺"),
            OutfitOption("STAR_PIN", "Pin de Estrella", "🌟"),
            OutfitOption("RAINBOW", "Arcoíris Brillante", "🌈"),
            OutfitOption("GALAXY", "Aura Galáctica", "🌌")
        )
    }

    val allClothes = remember {
        listOf(
            OutfitOption("CLOTH_SPORTSWR", "Uniforme Deportivo", "🎽", true),
            OutfitOption("CLOTH_PAJAMAS", "Pijama Estelar", "🛌", true),
            OutfitOption("CLOTH_RAINCOAT", "Impermeable Amarillo", "🥼", true),
            OutfitOption("CLOTH_HOODIE", "Sudadera Kawaii", "👘", true),
            OutfitOption("CLOTH_DETECTIVE", "Gabardina Detective", "🧥", true),
            OutfitOption("CLOTH_SUIT", "Traje Elegante", "🕴️", true),
            OutfitOption("CLOTH_KIMONO", "Kimono Tradicional", "👘", true),
            OutfitOption("CLOTH_MAGICIAN", "Túnica Mágica", "🧙‍♂️", true),
            OutfitOption("CLOTH_SUPERHERO", "Traje de Héroe", "🦸", true),
            OutfitOption("CLOTH_ROYAL_ROBE", "Manto Real", "🥻", true),
            OutfitOption("CLOTH_ASTRONAUT", "Traje Espacial", "🧑‍🚀", true),
            OutfitOption("CLOTH_ARMOR", "Armadura Dorada", "🛡️", true),
            OutfitOption("CLOTH_DINOSAUR", "Pijama Dinosaurio", "🦖", true),
            OutfitOption("CLOTH_CHEF_APRON", "Delantal Chef", "🍳", true),
            OutfitOption("CLOTH_DOCTOR", "Bata de Médico", "🩺", true),
            OutfitOption("CLOTH_PIRATE_COAT", "Chaqueta Pirata", "🏴‍☠️", true),
            OutfitOption("CLOTH_NINJA_SUIT", "Traje Ninja", "🥷", true),
            OutfitOption("CLOTH_FAIRY_DRESS", "Vestido de Hada", "🧚", true),
            OutfitOption("CLOTH_COWBOY_VEST", "Chaleco Vaquero", "🤠", true),
            OutfitOption("CLOTH_TUXEDO_GOLD", "Esmoquin Gala", "🍸", true),
            OutfitOption("CLOTH_HAWAIIAN", "Camisa Hawaiana", "🏖️", true),
            OutfitOption("CLOTH_GOTHIC", "Capa Gótica", "🧛", true),
            OutfitOption("CLOTH_SAMURAI_ARMOR", "Armadura Samurái", "🏯", true),
            OutfitOption("CLOTH_PHARAOH_ROBE", "Túnica Faraón", "🐍", true),
            OutfitOption("CLOTH_ROBOT_SHELL", "Chasis Robótico", "🤖", true),
            OutfitOption("CLOTH_CYBER_JACKET", "Chaqueta Cyber", "⚡", true),
            OutfitOption("CLOTH_KNIGHT_SILVER", "Armadura Plata", "⚔️", true),
            OutfitOption("CLOTH_MERMAID", "Escamas Sirena", "🧜‍♀️", true),
            OutfitOption("CLOTH_VIKING_PELT", "Manto Vikingo", "🪓", true),
            OutfitOption("CLOTH_CHRISTMAS", "Suéter Navideño", "🎄", true),
            OutfitOption("CLOTH_HALLOWEEN", "Disfraz Calabaza", "🎃", true),
            OutfitOption("CLOTH_OVERALLS", "Overol Mezclilla", "👖", true),
            OutfitOption("CLOTH_BALLET", "Tutú de Ballet", "🩰", true),
            OutfitOption("CLOTH_ROCKSTAR", "Chaqueta Rockera", "🎸", true),
            OutfitOption("CLOTH_WIZARD_STAR", "Túnica Estelar", "🌌", true),
            OutfitOption("CLOTH_GOLDEN_KING", "Manto Imperial", "👑", true)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Pasarela",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Prueba atuendos y accesorios en cada mascota",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 1. Selector de Mascota (Pet Selector)
                Text("1. Selecciona el Muñeco / Mascota:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(petTypes) { (type, name, emoji) ->
                        val isSelected = selectedPetType == type
                        Surface(
                            onClick = { selectedPetType = type },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Pasarela 3D Render Viewport
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PetRenderer(
                            type = selectedPetType,
                            isSleeping = false,
                            hunger = 95f,
                            happiness = 95f,
                            equippedHat = selectedHat,
                            equippedAccessory = selectedAccessory,
                            level = 10,
                            modifier = Modifier.size(190.dp)
                        )

                        // Currently equipped tags floating
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (selectedHat != null) {
                                val hatName = allHats.find { it.id == selectedHat }?.name ?: selectedHat
                                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.7f)) {
                                    Text("👒 $hatName", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                            if (selectedAccessory != null) {
                                val clothName = allClothes.find { it.id == selectedAccessory }?.name ?: selectedAccessory
                                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.7f)) {
                                    Text("👕 $clothName", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                        }

                        // Clear Button
                        if (selectedHat != null || selectedAccessory != null) {
                            TextButton(
                                onClick = {
                                    selectedHat = null
                                    selectedAccessory = null
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                Text("🧹 Quitar todo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Category selector (Hats vs Clothes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedCategory = "HATS" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCategory == "HATS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedCategory == "HATS") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text("👑 Sombreros & Accesorios (${allHats.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { selectedCategory = "CLOTHES" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCategory == "CLOTHES") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedCategory == "CLOTHES") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text("👕 Atuendos & Ropa (${allClothes.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grid of items
                val currentList = if (selectedCategory == "HATS") allHats else allClothes
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(currentList) { option ->
                        val isSelected = if (option.isCloth) selectedAccessory == option.id else selectedHat == option.id
                        Surface(
                            onClick = {
                                if (option.isCloth) {
                                    selectedAccessory = if (selectedAccessory == option.id) null else option.id
                                } else {
                                    selectedHat = if (selectedHat == option.id) null else option.id
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(option.emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    option.name,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("Cerrar", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onApplyToActivePet(selectedHat, selectedAccessory)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.3f).height(46.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Equipar", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aplicar en Mascota", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
