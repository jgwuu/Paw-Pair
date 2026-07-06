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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InventoryEntity
import com.example.data.PetEntity
import com.example.data.UserStatsEntity
import com.example.ui.components.WardrobeTesterDialog

@Composable
fun ShopScreen(
    inventory: List<InventoryEntity>,
    stats: UserStatsEntity?,
    activePet: PetEntity?,
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Ingredientes, 1: Alimentos & Pociones, 2: Accesorios, 3: Outfits
    var showWardrobeTester by remember { mutableStateOf(false) }
    var itemToConfirmBuy by remember { mutableStateOf<InventoryEntity?>(null) }

    if (itemToConfirmBuy != null) {
        val item = itemToConfirmBuy!!
        AlertDialog(
            onDismissRequest = { itemToConfirmBuy = null },
            title = { Text("Confirmar Compra 🛍️", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas comprar ${item.name} por ${item.cost} monedas?") },
            confirmButton = {
                Button(onClick = {
                    val targetId = item.itemId
                    itemToConfirmBuy = null
                    viewModel.buyShopItem(targetId)
                }) {
                    Text("Comprar")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToConfirmBuy = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showWardrobeTester) {
        WardrobeTesterDialog(
            initialPetType = activePet?.type ?: "SHIBA",
            initialHat = activePet?.equippedHat,
            initialAccessory = activePet?.equippedAccessory,
            onDismiss = { showWardrobeTester = false },
            onApplyToActivePet = { hat, cloth ->
                hat?.let { viewModel.equipActivePetAccessory("HAT_$it") }
                cloth?.let { viewModel.equipActivePetOutfit(cloth) }
            }
        )
    }

    val walletCoins = stats?.coins ?: 0

    val filteredItems = when (selectedTab) {
        0 -> inventory.filter { it.category == "INGREDIENT" || it.itemId.startsWith("ING_") }
        1 -> inventory.filter { (it.category == "FOOD" || it.category == "POTION") && !it.itemId.startsWith("ING_") }
        2 -> inventory.filter { it.category == "ACCESSORY" }
        else -> inventory.filter { it.category == "CLOTHING" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Sleek Supermarket Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🛍️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Boutique & Mercado PawPair",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Sleek Coins Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.MonetizationOn,
                        contentDescription = "Monedas",
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$walletCoins",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("shop_coins_display")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Supermarket Category Filter Row (Scrollable to prevent squishing)
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            val tabs = listOf(
                0 to ("🥑" to "Ingredientes"),
                1 to ("🍔" to "Alimentos"),
                2 to ("👒" to "Accesorios"),
                3 to ("👗" to "Atuendos")
            )
            tabs.forEach { (index, pair) ->
                val (emoji, label) = pair
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(emoji, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            onClick = { showWardrobeTester = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            modifier = Modifier.fillMaxWidth().testTag("open_wardrobe_tester_button")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✨", fontSize = 26.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pasarela",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Prueba cómo le queda la ropa a tu mascota",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    Text(
                        text = "Entrar 👗",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Shop items list
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay artículos disponibles en esta categoría.", fontSize = 14.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(filteredItems) { item ->
                    ShopGalleryCard(
                        item = item,
                        currentCoins = walletCoins,
                        activePet = activePet,
                        onBuy = { itemToConfirmBuy = item },
                        onEquip = {
                            if (item.category == "CLOTHING") {
                                val isCurrentlyEquipped = activePet?.equippedAccessory == item.itemId
                                if (isCurrentlyEquipped) {
                                    viewModel.equipActivePetOutfit(null)
                                } else {
                                    viewModel.equipActivePetOutfit(item.itemId)
                                }
                            } else {
                                val isCurrentlyEquipped = activePet?.equippedHat == item.itemId.replace("HAT_", "")
                                if (isCurrentlyEquipped) {
                                    viewModel.equipActivePetAccessory(null) // unequip
                                } else {
                                    viewModel.equipActivePetAccessory(item.itemId) // equip
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShopGalleryCard(
    item: InventoryEntity,
    currentCoins: Int,
    activePet: PetEntity?,
    onBuy: () -> Unit,
    onEquip: () -> Unit
) {
    val emoji = when (item.itemId) {
        "FOOD_APPLE" -> "🍎"
        "FOOD_SUSHI" -> "🍣"
        "FOOD_PIZZA" -> "🍕"
        "FOOD_CAKE" -> "🍰"
        "FOOD_COOKIE" -> "🍪"
        "FOOD_BURGER" -> "🍔"
        "FOOD_ICECREAM" -> "🍦"
        "FOOD_MILK" -> "🥛"
        "FOOD_RAMEN" -> "🍜"
        
        "ING_PLATANO" -> "🍌"
        "ING_SALCHICHA" -> "🌭"
        "ING_PAPA" -> "🥔"
        "ING_QUESO" -> "🧀"
        "ING_HUEVO" -> "🥚"
        "ING_HARINA" -> "🌾"
        "ING_LECHE" -> "🥛"
        "ING_AZUCAR" -> "🍬"
        "ING_COCOA" -> "🍫"
        "ING_PESCADO" -> "🐟"

        "POTION_ENERGY" -> "⚡"
        "POTION_LOVE" -> "💖"
        "CLEANER_SOAP" -> "🧼"
        "CLEANER_SHAMPOO" -> "🫧"
        
        "HAT_CROWN" -> "👑"
        "HAT_TOP_HAT" -> "🎩"
        "HAT_SUNGLASSES" -> "🕶️"
        "HAT_BOWTIE" -> "🎀"
        "HAT_HALO" -> "😇"
        "HAT_WITCH" -> "🧙"
        "HAT_REINDEER" -> "🦌"
        "HAT_STRAW_HAT" -> "👒"
        "HAT_DETECTIVE" -> "🕵️"
        "HAT_CAP" -> "🧢"
        "HAT_CAT_EARS" -> "🐱"
        "HAT_CHEF" -> "👨‍🍳"
        "HAT_HEADPHONES" -> "🎧"
        "HAT_FLOWER" -> "🌸"
        "HAT_SCARF" -> "🧣"
        "HAT_MONOCLE" -> "🧐"
        "HAT_MUSTACHE" -> "🥸"
        "HAT_PARTY" -> "🥳"
        "HAT_DIAMOND" -> "💎"
        "HAT_PIRATE" -> "🏴‍☠️"
        "HAT_NINJA" -> "🥷"
        "HAT_VIKING" -> "🛡️"
        "HAT_ANGEL_WINGS" -> "🪽"
        "HAT_DRAGON_WINGS" -> "🐉"
        "HAT_UNICORN" -> "🦄"
        "HAT_SUPERHERO" -> "🦸‍♂️"
        "HAT_TIARA" -> "👸"
        "HAT_ALIEN" -> "👽"
        "HAT_FLOWER_CROWN" -> "🌺"
        "HAT_GOGGLES" -> "🥽"
        "HAT_PARTY_GLASSES" -> "🕶️"
        "HAT_MAGICAL_AURA" -> "✨"
        "HAT_ROYAL_CAPE" -> "👑"
        "HAT_CYBER_VISOR" -> "🤖"
        "HAT_GOLDEN_BELL" -> "🔔"
        "HAT_FLOWER_GARLAND" -> "🌺"
        "HAT_STAR_PIN" -> "🌟"
        "HAT_DIAMOND_RING" -> "💍"
        "HAT_RAINBOW" -> "🌈"
        "HAT_FLAME" -> "🔥"
        "HAT_ICE_CROWN" -> "❄️"
        "HAT_PHARAOH" -> "🐍"
        "HAT_SAMURAI" -> "🏯"
        "HAT_GALAXY" -> "🌌"

        "CLOTH_HOODIE" -> "👘"
        "CLOTH_SUIT" -> "🕴️"
        "CLOTH_PAJAMAS" -> "🛌"
        "CLOTH_KIMONO" -> "👘"
        "CLOTH_ARMOR" -> "🛡️"
        "CLOTH_SPORTSWR" -> "🎽"
        "CLOTH_ASTRONAUT" -> "🧑‍🚀"
        "CLOTH_MAGICIAN" -> "🧙‍♂️"
        "CLOTH_DETECTIVE" -> "🧥"
        "CLOTH_RAINCOAT" -> "🥼"
        "CLOTH_ROYAL_ROBE" -> "🥻"
        "CLOTH_SUPERHERO" -> "🦸"
        "CLOTH_DINOSAUR" -> "🦖"
        "CLOTH_CHEF_APRON" -> "🍳"
        "CLOTH_DOCTOR" -> "🩺"
        "CLOTH_PIRATE_COAT" -> "🏴‍☠️"
        "CLOTH_NINJA_SUIT" -> "🥷"
        "CLOTH_FAIRY_DRESS" -> "🧚"
        "CLOTH_COWBOY_VEST" -> "🤠"
        "CLOTH_TUXEDO_GOLD" -> "🍸"
        "CLOTH_HAWAIIAN" -> "🏖️"
        "CLOTH_GOTHIC" -> "🧛"
        "CLOTH_SAMURAI_ARMOR" -> "🏯"
        "CLOTH_PHARAOH_ROBE" -> "🐍"
        "CLOTH_ROBOT_SHELL" -> "🤖"
        "CLOTH_CYBER_JACKET" -> "⚡"
        "CLOTH_KNIGHT_SILVER" -> "⚔️"
        "CLOTH_MERMAID" -> "🧜‍♀️"
        "CLOTH_VIKING_PELT" -> "🪓"
        "CLOTH_CHRISTMAS" -> "🎄"
        "CLOTH_HALLOWEEN" -> "🎃"
        "CLOTH_OVERALLS" -> "👖"
        "CLOTH_BALLET" -> "🩰"
        "CLOTH_ROCKSTAR" -> "🎸"
        "CLOTH_WIZARD_STAR" -> "🌌"
        "CLOTH_GOLDEN_KING" -> "👑"
        else -> "🎁"
    }

    val isWearable = item.category == "ACCESSORY" || item.category == "CLOTHING"
    
    // Check if equipped on the active pet
    val cleanItemCode = item.itemId.replace("HAT_", "")
    val isEquipped = if (item.category == "CLOTHING") {
        activePet != null && activePet.equippedAccessory == item.itemId
    } else {
        item.category == "ACCESSORY" && activePet != null && activePet.equippedHat == cleanItemCode
    }
    val isUnlocked = isWearable && item.isUnlocked

    val canAfford = currentCoins >= item.cost

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isEquipped) 2.dp else 1.dp,
            color = if (isEquipped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper details: emoji + shelf qty
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = emoji,
                    fontSize = 44.sp,
                    modifier = Modifier.align(Alignment.Center).padding(vertical = 4.dp)
                )

                // Owned quantity pill (non wearable)
                if (!isWearable && item.quantity > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Tengo: ${item.quantity}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                } else if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("Comprado", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Text info
            Text(
                text = item.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = if (isWearable) (if (item.category == "CLOTHING") "Atuendo" else "Accesorio") else "+${item.valueBoost.toInt()} beneficio",
                fontSize = 10.sp,
                color = if (isWearable) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // CTA Button (Target height 48dp+ for accessibility)
            if (isWearable && isUnlocked) {
                // Interactive Equip Action
                Button(
                    onClick = onEquip,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEquipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondary,
                        contentColor = if (isEquipped) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    val label = if (isEquipped) "Desequipar ✖" else "Equipar 👗"
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // Purchase Action
                Button(
                    onClick = onBuy,
                    enabled = canAfford,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.MonetizationOn, "Coins", tint = Color(0xFFFFA000), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("${item.cost}", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
