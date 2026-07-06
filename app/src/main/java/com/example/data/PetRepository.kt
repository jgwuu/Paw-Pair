package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class PetRepository(private val petDao: PetDao, private val context: Context? = null) {

    val allPets: Flow<List<PetEntity>> = petDao.getAllPetsFlow()
    val userStats: Flow<UserStatsEntity?> = petDao.getUserStatsFlow()
    val inventory: Flow<List<InventoryEntity>> = petDao.getInventoryFlow()

    fun getActivityLogs(petId: Int): Flow<List<ActivityLogEntity>> = petDao.getActivityLogsFlow(petId)

    suspend fun getLatestPet(): PetEntity? = petDao.getLatestPet()

    suspend fun createPet(name: String, type: String): Int {
        val pet = PetEntity(
            name = name,
            type = type,
            hunger = 80f,
            energy = 80f,
            happiness = 80f,
            cleanliness = 80f
        )
        val id = petDao.insertPet(pet).toInt()
        
        // Log creation
        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = id,
                actorName = "Tú",
                actionType = "LINK",
                actionDetails = "¡Adoptaste a $name el ${getPetTypeLabel(type)}!"
            )
        )
        return id
    }

    suspend fun checkAndInitializeData() {
        // Core initialization of stats
        val currentStats = petDao.getUserStats()
        if (currentStats == null) {
            val validCode = CoMascotaSyncService.generate6CharCode()
            petDao.insertUserStats(
                UserStatsEntity(
                    userId = "local_player",
                    coins = 500,
                    level = 1,
                    xp = 0,
                    isLinked = false,
                    partnerName = null,
                    linkCode = validCode
                )
            )
        } else if (currentStats.linkCode.isNullOrBlank() || currentStats.linkCode == "XXXX-XXXX" || currentStats.linkCode!!.length != 6) {
            val validCode = CoMascotaSyncService.generate6CharCode()
            petDao.updateUserStats(currentStats.copy(linkCode = validCode))
        }

        // Prepopulate standard items in Shop
        val defaultItems = listOf(
            // Foods
            InventoryEntity("FOOD_APPLE", "FOOD", "Manzana Dulce 🍎", 35, quantity = 3, valueBoost = 15f),
            InventoryEntity("FOOD_SUSHI", "FOOD", "Sushi Fresco 🍣", 100, quantity = 0, valueBoost = 40f),
            InventoryEntity("FOOD_PIZZA", "FOOD", "Pizza Crujiente 🍕", 85, quantity = 1, valueBoost = 30f),
            InventoryEntity("FOOD_CAKE", "FOOD", "Pastel de Bodas 🍰", 150, quantity = 0, valueBoost = 50f),
            InventoryEntity("FOOD_COOKIE", "FOOD", "Galleta con Chispas 🍪", 40, quantity = 3, valueBoost = 18f),
            InventoryEntity("FOOD_BURGER", "FOOD", "Hamburguesa Deluxe 🍔", 120, quantity = 0, valueBoost = 45f),
            InventoryEntity("FOOD_ICECREAM", "FOOD", "Helado Suave de Fresa 🍦", 60, quantity = 2, valueBoost = 22f),
            InventoryEntity("FOOD_MILK", "FOOD", "Cartón de Leche Fresca 🥛", 50, quantity = 1, valueBoost = 20f),
            InventoryEntity("FOOD_RAMEN", "FOOD", "Ramen Calientito 🍜", 110, quantity = 0, valueBoost = 42f),
            
            // Ingredients for Coastal Foods (Colombian Coast Supermarket)
            InventoryEntity("ING_PLATANO", "INGREDIENT", "Plátano Verde 🍌", 40, quantity = 3, valueBoost = 5f),
            InventoryEntity("ING_SALCHICHA", "INGREDIENT", "Salchicha Ranchera 🌭", 45, quantity = 3, valueBoost = 6f),
            InventoryEntity("ING_PAPA", "INGREDIENT", "Papa Sabanera 🥔", 30, quantity = 3, valueBoost = 4f),
            InventoryEntity("ING_QUESO", "INGREDIENT", "Queso Costeño 🧀", 50, quantity = 3, valueBoost = 8f),
            InventoryEntity("ING_HUEVO", "INGREDIENT", "Huevo Sancochado 🥚", 25, quantity = 4, valueBoost = 5f),
            InventoryEntity("ING_HARINA", "INGREDIENT", "Harina de Maíz 🌾", 35, quantity = 4, valueBoost = 5f),
            InventoryEntity("ING_LECHE", "INGREDIENT", "Leche Parmalat 🥛", 35, quantity = 3, valueBoost = 6f),
            InventoryEntity("ING_AZUCAR", "INGREDIENT", "Azúcar Orgánico 🍬", 20, quantity = 5, valueBoost = 3f),
            InventoryEntity("ING_COCOA", "INGREDIENT", "Cocoa en Polvo 🍫", 40, quantity = 2, valueBoost = 6f),
            InventoryEntity("ING_PESCADO", "INGREDIENT", "Pescado de Mar 🐟", 65, quantity = 2, valueBoost = 10f),

            // Potions / Cleaners
            InventoryEntity("POTION_ENERGY", "POTION", "Bebida Energética ⚡", 1000, quantity = 1, valueBoost = 60f),
            InventoryEntity("POTION_LOVE", "POTION", "Poción de Amor 💖", 450, quantity = 0, valueBoost = 35f),
            InventoryEntity("CLEANER_SOAP", "POTION", "Jabón Cremoso 🧼", 80, quantity = 2, valueBoost = 35f),
            InventoryEntity("CLEANER_SHAMPOO", "POTION", "Champú de Burbujas 🫧", 120, quantity = 1, valueBoost = 48f),
            
            // Accessories (Precios de Lujo / Endgame)
            InventoryEntity("HAT_CAP", "ACCESSORY", "Gorra Deportiva 🧢", 5000, quantity = 0),
            InventoryEntity("HAT_BOWTIE", "ACCESSORY", "Pajarita Roja 🎀", 6500, quantity = 0),
            InventoryEntity("HAT_PARTY", "ACCESSORY", "Gorrito de Fiesta 🥳", 7500, quantity = 0),
            InventoryEntity("HAT_FLOWER", "ACCESSORY", "Flor de Primavera 🌸", 8500, quantity = 0),
            InventoryEntity("HAT_STAR_PIN", "ACCESSORY", "Pin de Estrella 🌟", 9000, quantity = 0),
            InventoryEntity("HAT_STRAW_HAT", "ACCESSORY", "Sombrero de Paja 👒", 10000, quantity = 0),
            InventoryEntity("HAT_SUNGLASSES", "ACCESSORY", "Gafas de Sol 🕶️", 11500, quantity = 0),
            InventoryEntity("HAT_MUSTACHE", "ACCESSORY", "Bigote Elegante 🥸", 12000, quantity = 0),
            InventoryEntity("HAT_FLOWER_GARLAND", "ACCESSORY", "Guirnalda Tropical 🌺", 13000, quantity = 0),
            InventoryEntity("HAT_SCARF", "ACCESSORY", "Bufanda Suave 🧣", 14000, quantity = 0),
            InventoryEntity("HAT_CHEF", "ACCESSORY", "Sombrero de Chef 👨‍🍳", 15000, quantity = 0),
            InventoryEntity("HAT_GOLDEN_BELL", "ACCESSORY", "Cascabel de Oro 🔔", 16000, quantity = 0),
            InventoryEntity("HAT_GOGGLES", "ACCESSORY", "Gafas de Aviador 🥽", 17500, quantity = 0),
            InventoryEntity("HAT_PARTY_GLASSES", "ACCESSORY", "Gafas de Fiesta 🕶️", 18000, quantity = 0),
            InventoryEntity("HAT_CAT_EARS", "ACCESSORY", "Orejas de Gato 🐱", 20000, quantity = 0),
            InventoryEntity("HAT_REINDEER", "ACCESSORY", "Astas de Reno 🦌", 22000, quantity = 0),
            InventoryEntity("HAT_DETECTIVE", "ACCESSORY", "Gorra de Detective 🕵️", 24000, quantity = 0),
            InventoryEntity("HAT_FLOWER_CROWN", "ACCESSORY", "Corona de Flores 🌺", 26000, quantity = 0),
            InventoryEntity("HAT_TOP_HAT", "ACCESSORY", "Sombrero de Copa 🎩", 28000, quantity = 0),
            InventoryEntity("HAT_MONOCLE", "ACCESSORY", "Monóculo de Oro 🧐", 30000, quantity = 0),
            InventoryEntity("HAT_WITCH", "ACCESSORY", "Sombrero de Bruja 🧙", 32000, quantity = 0),
            InventoryEntity("HAT_ALIEN", "ACCESSORY", "Antenas Alienígenas 👽", 35000, quantity = 0),
            InventoryEntity("HAT_HEADPHONES", "ACCESSORY", "Audífonos Gamer 🎧", 38000, quantity = 0),
            InventoryEntity("HAT_PIRATE", "ACCESSORY", "Sombrero Pirata 🏴‍☠️", 42000, quantity = 0),
            InventoryEntity("HAT_NINJA", "ACCESSORY", "Bandana Ninja 🥷", 45000, quantity = 0),
            InventoryEntity("HAT_CYBER_VISOR", "ACCESSORY", "Visor Cyberpunk 🤖", 50000, quantity = 0),
            InventoryEntity("HAT_VIKING", "ACCESSORY", "Casco Vikingo 🛡️", 55000, quantity = 0),
            InventoryEntity("HAT_RAINBOW", "ACCESSORY", "Arcoíris Brillante 🌈", 60000, quantity = 0),
            InventoryEntity("HAT_HALO", "ACCESSORY", "Halo Celestial 😇", 65000, quantity = 0),
            InventoryEntity("HAT_SUPERHERO", "ACCESSORY", "Capa de Superhéroe 🦸‍♂️", 70000, quantity = 0),
            InventoryEntity("HAT_TIARA", "ACCESSORY", "Tiara Real 👸", 75000, quantity = 0),
            InventoryEntity("HAT_UNICORN", "ACCESSORY", "Cuerno de Unicornio 🦄", 80000, quantity = 0),
            InventoryEntity("HAT_MAGICAL_AURA", "ACCESSORY", "Aura Mágica ✨", 90000, quantity = 0),
            InventoryEntity("HAT_ROYAL_CAPE", "ACCESSORY", "Capa Real 👑", 100000, quantity = 0),
            InventoryEntity("HAT_CROWN", "ACCESSORY", "Corona de Oro 👑", 120000, quantity = 0),
            InventoryEntity("HAT_ICE_CROWN", "ACCESSORY", "Corona de Hielo ❄️", 135000, quantity = 0),
            InventoryEntity("HAT_FLAME", "ACCESSORY", "Corona de Fuego 🔥", 150000, quantity = 0),
            InventoryEntity("HAT_SAMURAI", "ACCESSORY", "Casco Samurái 🏯", 170000, quantity = 0),
            InventoryEntity("HAT_PHARAOH", "ACCESSORY", "Tocado de Faraón 🐍", 200000, quantity = 0),
            InventoryEntity("HAT_DIAMOND", "ACCESSORY", "Collar de Diamantes 💎", 250000, quantity = 0),
            InventoryEntity("HAT_DIAMOND_RING", "ACCESSORY", "Anillo Diamante 💍", 300000, quantity = 0),
            InventoryEntity("HAT_ANGEL_WINGS", "ACCESSORY", "Alas de Ángel 🪽", 400000, quantity = 0),
            InventoryEntity("HAT_DRAGON_WINGS", "ACCESSORY", "Alas de Dragón 🐉", 500000, quantity = 0),
            InventoryEntity("HAT_GALAXY", "ACCESSORY", "Aura Galáctica 🌌", 750000, quantity = 0),

            // Outfits / Clothing (Precios de Alta Costura / Alta Rareza)
            InventoryEntity("CLOTH_SPORTSWR", "CLOTHING", "Uniforme Deportivo 🎽", 15000, quantity = 0),
            InventoryEntity("CLOTH_PAJAMAS", "CLOTHING", "Pijama Estelar 🛌", 18000, quantity = 0),
            InventoryEntity("CLOTH_RAINCOAT", "CLOTHING", "Impermeable Amarillo 🥼", 22000, quantity = 0),
            InventoryEntity("CLOTH_HOODIE", "CLOTHING", "Sudadera Kawaii 👘", 28000, quantity = 0),
            InventoryEntity("CLOTH_DETECTIVE", "CLOTHING", "Gabardina Detective 🧥", 35000, quantity = 0),
            InventoryEntity("CLOTH_SUIT", "CLOTHING", "Traje Elegante 🕴️", 45000, quantity = 0),
            InventoryEntity("CLOTH_KIMONO", "CLOTHING", "Kimono Tradicional 👘", 60000, quantity = 0),
            InventoryEntity("CLOTH_MAGICIAN", "CLOTHING", "Túnica Mágica 🧙‍♂️", 80000, quantity = 0),
            InventoryEntity("CLOTH_SUPERHERO", "CLOTHING", "Traje de Héroe 🦸", 100000, quantity = 0),
            InventoryEntity("CLOTH_ROYAL_ROBE", "CLOTHING", "Manto Real 🥻", 150000, quantity = 0),
            InventoryEntity("CLOTH_ASTRONAUT", "CLOTHING", "Traje Espacial 🧑‍🚀", 250000, quantity = 0),
            InventoryEntity("CLOTH_ARMOR", "CLOTHING", "Armadura Dorada 🛡️", 400000, quantity = 0),
            InventoryEntity("CLOTH_DINOSAUR", "CLOTHING", "Pijama de Dinosaurio 🦖", 16000, quantity = 0),
            InventoryEntity("CLOTH_CHEF_APRON", "CLOTHING", "Delantal de Chef 🍳", 19000, quantity = 0),
            InventoryEntity("CLOTH_DOCTOR", "CLOTHING", "Bata de Médico 🩺", 24000, quantity = 0),
            InventoryEntity("CLOTH_PIRATE_COAT", "CLOTHING", "Chaqueta Pirata 🏴‍☠️", 30000, quantity = 0),
            InventoryEntity("CLOTH_NINJA_SUIT", "CLOTHING", "Traje Ninja 🥷", 38000, quantity = 0),
            InventoryEntity("CLOTH_FAIRY_DRESS", "CLOTHING", "Vestido de Hada 🧚", 48000, quantity = 0),
            InventoryEntity("CLOTH_COWBOY_VEST", "CLOTHING", "Chaleco Vaquero 🤠", 52000, quantity = 0),
            InventoryEntity("CLOTH_TUXEDO_GOLD", "CLOTHING", "Esmoquin de Gala 🍸", 65000, quantity = 0),
            InventoryEntity("CLOTH_HAWAIIAN", "CLOTHING", "Camisa Hawaiana 🏖️", 20000, quantity = 0),
            InventoryEntity("CLOTH_GOTHIC", "CLOTHING", "Capa Gótica 🧛", 75000, quantity = 0),
            InventoryEntity("CLOTH_SAMURAI_ARMOR", "CLOTHING", "Armadura Samurái 🏯", 90000, quantity = 0),
            InventoryEntity("CLOTH_PHARAOH_ROBE", "CLOTHING", "Túnica de Faraón 🐍", 110000, quantity = 0),
            InventoryEntity("CLOTH_ROBOT_SHELL", "CLOTHING", "Chasis Robótico 🤖", 130000, quantity = 0),
            InventoryEntity("CLOTH_CYBER_JACKET", "CLOTHING", "Chaqueta Cyberpunk ⚡", 160000, quantity = 0),
            InventoryEntity("CLOTH_KNIGHT_SILVER", "CLOTHING", "Armadura de Plata ⚔️", 180000, quantity = 0),
            InventoryEntity("CLOTH_MERMAID", "CLOTHING", "Escamas de Sirena 🧜‍♀️", 200000, quantity = 0),
            InventoryEntity("CLOTH_VIKING_PELT", "CLOTHING", "Manto Vikingo 🪓", 220000, quantity = 0),
            InventoryEntity("CLOTH_CHRISTMAS", "CLOTHING", "Suéter Navideño 🎄", 35000, quantity = 0),
            InventoryEntity("CLOTH_HALLOWEEN", "CLOTHING", "Disfraz de Calabaza 🎃", 40000, quantity = 0),
            InventoryEntity("CLOTH_OVERALLS", "CLOTHING", "Overol de Mezclilla 👖", 25000, quantity = 0),
            InventoryEntity("CLOTH_BALLET", "CLOTHING", "Tutú de Ballet 🩰", 50000, quantity = 0),
            InventoryEntity("CLOTH_ROCKSTAR", "CLOTHING", "Chaqueta Rockera 🎸", 85000, quantity = 0),
            InventoryEntity("CLOTH_WIZARD_STAR", "CLOTHING", "Túnica Estelar 🌌", 300000, quantity = 0),
            InventoryEntity("CLOTH_GOLDEN_KING", "CLOTHING", "Manto Imperial de Oro 👑", 500000, quantity = 0)
        )

        // Make sure all individual default items exist in database and costs/categories are synced
        for (item in defaultItems) {
            val existing = petDao.getInventoryItem(item.itemId)
            if (existing == null) {
                petDao.insertInventoryItem(item)
            } else if (existing.cost != item.cost || existing.category != item.category || existing.name != item.name) {
                petDao.updateInventoryItem(existing.copy(cost = item.cost, category = item.category, name = item.name))
            }
        }
    }

    suspend fun feedPet(petId: Int, itemId: String): Boolean {
        val pet = petDao.getPetById(petId) ?: return false
        val item = petDao.getInventoryItem(itemId) ?: return false

        if (item.quantity <= 0) return false

        // Disallow actions if sleeping
        if (pet.isSleeping && item.itemId != "POTION_ENERGY") return false

        val isFav = when (pet.type) {
            "SHIBA" -> itemId in listOf("FOOD_BURGER", "FOOD_PIZZA", "FOOD_APPLE")
            "SLIME" -> itemId in listOf("FOOD_RAMEN", "FOOD_ICECREAM", "FOOD_APPLE")
            "KITTY" -> itemId in listOf("FOOD_SUSHI", "FOOD_MILK", "ING_PESCADO")
            "DRACO" -> itemId in listOf("FOOD_PIZZA", "FOOD_BURGER", "RECIPE_SALCHICHA_PAPAS")
            "AXOLOTL" -> itemId in listOf("FOOD_SUSHI", "FOOD_CAKE", "FOOD_COOKIE")
            else -> false
        }
        val isRep = when (pet.type) {
            "SHIBA" -> itemId in listOf("FOOD_SUSHI", "FOOD_RAMEN")
            "SLIME" -> itemId in listOf("FOOD_PIZZA", "FOOD_COOKIE")
            "KITTY" -> itemId in listOf("FOOD_BURGER", "FOOD_APPLE")
            "DRACO" -> itemId in listOf("FOOD_ICECREAM", "FOOD_MILK")
            "AXOLOTL" -> itemId in listOf("FOOD_BURGER", "FOOD_PIZZA")
            else -> false
        }

        val hungerGain = if (isRep) item.valueBoost * 0.35f else if (isFav) item.valueBoost * 1.3f else item.valueBoost
        val currentHunger = (pet.hunger + hungerGain).coerceAtMost(100f)
        var currentHappiness = pet.happiness
        var currentEnergy = pet.energy

        if (isFav) {
            currentHappiness = (currentHappiness + 25f).coerceAtMost(100f)
            currentEnergy = (currentEnergy + 2f).coerceAtMost(100f)
        } else if (isRep) {
            currentHappiness = (currentHappiness - 12f).coerceAtLeast(0f)
        }

        if (itemId == "FOOD_CAKE") {
            currentHappiness = (currentHappiness + 15f).coerceAtMost(100f)
        }
        if (itemId.startsWith("RECIPE_")) {
            currentHappiness = (currentHappiness + 30f).coerceAtMost(100f)
            currentEnergy = (currentEnergy + 3f).coerceAtMost(100f)
        }
        if (itemId == "POTION_ENERGY") {
            currentEnergy = (currentEnergy + item.valueBoost).coerceAtMost(100f)
        }

        val updatedPet = pet.copy(
            hunger = currentHunger,
            happiness = currentHappiness,
            energy = currentEnergy,
            lastInteraction = System.currentTimeMillis()
        )
        petDao.updatePet(updatedPet)

        // Decrement inventory
        petDao.updateInventoryItem(item.copy(quantity = item.quantity - 1))

        // Give XP and log
        awardXp(15)
        val logDetail = if (isFav) {
            "Alimentaste a ${pet.name} con su ¡FAVORITO! ${item.name} (+Ánimo/Energía)"
        } else if (isRep) {
            "Alimentaste a ${pet.name} con ${item.name} (¡Le causó repulsión!)"
        } else {
            "Alimentaste a ${pet.name} con ${item.name} (+${item.valueBoost.toInt()} Hambre)"
        }
        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = "Tú",
                actionType = "FEED",
                actionDetails = logDetail
            )
        )
        return true
    }

    suspend fun interactPet(petId: Int) {
        val pet = petDao.getPetById(petId) ?: return
        if (pet.isSleeping) return
        val newHappiness = (pet.happiness + 8f).coerceAtMost(100f)
        petDao.updatePet(pet.copy(happiness = newHappiness, lastInteraction = System.currentTimeMillis()))
        awardXp(8)
    }

    suspend fun bathePet(petId: Int): Boolean {
        val pet = petDao.getPetById(petId) ?: return false
        if (pet.isSleeping) return false

        // Try Shampoo first, then Soap
        val shampoo = petDao.getInventoryItem("CLEANER_SHAMPOO")
        val soap = petDao.getInventoryItem("CLEANER_SOAP")

        val chosenItem: InventoryEntity
        if (shampoo != null && shampoo.quantity > 0) {
            chosenItem = shampoo
        } else if (soap != null && soap.quantity > 0) {
            chosenItem = soap
        } else {
            return false
        }

        val newCleanliness = (pet.cleanliness + chosenItem.valueBoost).coerceAtMost(100f)
        val updatedPet = pet.copy(
            cleanliness = newCleanliness,
            lastInteraction = System.currentTimeMillis()
        )
        petDao.updatePet(updatedPet)

        // Decrement item
        petDao.updateInventoryItem(chosenItem.copy(quantity = chosenItem.quantity - 1))

        awardXp(12)
        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = "Tú",
                actionType = "BATH",
                actionDetails = "Bañaste a ${pet.name} con ${chosenItem.name} (+${chosenItem.valueBoost.toInt()} Limpieza)"
            )
        )
        return true
    }

    suspend fun walkPet(petId: Int): Int {
        val pet = petDao.getPetById(petId) ?: return 0
        if (pet.isSleeping) return 0
        if (pet.energy < 20f) return -1 // Not enough energy

        val gainedCoins = Random.nextInt(25, 60)
        val stats = petDao.getUserStats()
        if (stats != null) {
            petDao.updateUserStats(stats.copy(coins = stats.coins + gainedCoins))
        }

        val finalEnergy = (pet.energy - 15f).coerceAtLeast(0f)
        val finalHappiness = (pet.happiness + 25f).coerceAtMost(100f)
        val finalCleanliness = (pet.cleanliness - 15f).coerceAtLeast(0f)

        petDao.updatePet(pet.copy(
            energy = finalEnergy,
            happiness = finalHappiness,
            cleanliness = finalCleanliness,
            lastInteraction = System.currentTimeMillis()
        ))

        awardXp(20)

        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = "Tú",
                actionType = "PLAY",
                actionDetails = "Llevaste a pasear a ${pet.name}. ¡Encontró +$gainedCoins monedas en el parque!"
            )
        )
        return gainedCoins
    }

    suspend fun cookAndFeedRecipe(petId: Int, recipeName: String, requiredIngredients: List<String>, hungerBoost: Float, happinessBoost: Float): Boolean {
        val pet = petDao.getPetById(petId) ?: return false
        if (pet.isSleeping) return false

        val groupedReqs = requiredIngredients.groupingBy { it }.eachCount()
        // Check if user has all ingredients
        for ((ingId, reqQty) in groupedReqs) {
            val ing = petDao.getInventoryItem(ingId) ?: return false
            if (ing.quantity < reqQty) return false
        }

        // Deduct ingredients
        for ((ingId, reqQty) in groupedReqs) {
            val ing = petDao.getInventoryItem(ingId)!!
            petDao.updateInventoryItem(ing.copy(quantity = ing.quantity - reqQty))
        }

        // Generate matching recipe itemId
        val recipeId = when (recipeName) {
            "Arepa" -> "RECIPE_AREPA_HUEVO"
            "Patacones" -> "RECIPE_PATACONES_QUESO"
            "Salchipapa" -> "RECIPE_SALCHICHA_PAPAS"
            "Cocadas" -> "RECIPE_COCADAS"
            "Chocolate Caliente" -> "RECIPE_CHOCO_QUESO"
            "Pescado Frito" -> "RECIPE_PESCADO_PATACON"
            "Sopa de Queso" -> "RECIPE_MOTE_QUESO"
            "Arroz Dulce" -> "RECIPE_ARROZ_COCO"
            "Carimañola" -> "RECIPE_CARIMANOLA"
            "Cazuela" -> "RECIPE_CAZUELA"
            "Arroz de Pescado" -> "RECIPE_ARROZ_LISA"
            else -> "RECIPE_GENERIC"
        }

        // Add cooked dish directly to the inventory/despensa!
        val existingItem = petDao.getInventoryItem(recipeId)
        if (existingItem != null) {
            petDao.updateInventoryItem(existingItem.copy(quantity = existingItem.quantity + 1))
        } else {
            petDao.insertInventoryItem(
                InventoryEntity(
                    itemId = recipeId,
                    category = "FOOD",
                    name = recipeName,
                    cost = 0,
                    quantity = 1,
                    isUnlocked = true,
                    valueBoost = hungerBoost
                )
            )
        }

        awardXp(30)

        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = "Tú",
                actionType = "FEED",
                actionDetails = "Cocinaste receta costeña: ${recipeName} 🧑‍🍳 (Plato guardado en Despensa!)"
            )
        )
        return true
    }

    suspend fun soapPet(petId: Int, useShampoo: Boolean): Boolean {
        val pet = petDao.getPetById(petId) ?: return false
        if (pet.isSleeping) return false

        val itemId = if (useShampoo) "CLEANER_SHAMPOO" else "CLEANER_SOAP"
        val cleaner = petDao.getInventoryItem(itemId) ?: return false
        if (cleaner.quantity <= 0) return false

        val newCleanliness = (pet.cleanliness + 15f).coerceAtMost(100f)
        val newHappiness = (pet.happiness + 8f).coerceAtMost(100f)
        petDao.updatePet(pet.copy(
            cleanliness = newCleanliness,
            happiness = newHappiness,
            lastInteraction = System.currentTimeMillis()
        ))

        petDao.updateInventoryItem(cleaner.copy(quantity = cleaner.quantity - 1))
        awardXp(10)

        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = "Tú",
                actionType = "BATH",
                actionDetails = "Enjabonaste a ${pet.name} con ${cleaner.name} 🧼"
            )
        )
        return true
    }

    suspend fun rinsePet(petId: Int): Boolean {
        val pet = petDao.getPetById(petId) ?: return false
        if (pet.isSleeping) return false

        val newCleanliness = (pet.cleanliness + 25f).coerceAtMost(100f)
        petDao.updatePet(pet.copy(
            cleanliness = newCleanliness,
            lastInteraction = System.currentTimeMillis()
        ))

        awardXp(5)
        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = "Tú",
                actionType = "BATH",
                actionDetails = "Enjuagaste la espuma de ${pet.name} con agua fresca 🚿"
            )
        )
        return true
    }

    suspend fun brushPetTeeth(petId: Int): Boolean {
        val pet = petDao.getPetById(petId) ?: return false
        if (pet.isSleeping) return false

        val newCleanliness = (pet.cleanliness + 15f).coerceAtMost(100f)
        val newHappiness = (pet.happiness + 15f).coerceAtMost(100f)
        petDao.updatePet(pet.copy(
            cleanliness = newCleanliness,
            happiness = newHappiness,
            lastInteraction = System.currentTimeMillis()
        ))

        awardXp(12)
        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = "Tú",
                actionType = "BATH",
                actionDetails = "¡Le cepillaste los dientes a ${pet.name}! Sus dientecitos relucen 🪥✨"
            )
        )
        return true
    }

    suspend fun toggleSleep(petId: Int): Boolean {
        val pet = petDao.getPetById(petId) ?: return false
        val isSleeping = !pet.isSleeping
        val now = System.currentTimeMillis()
        
        val updatedPet = pet.copy(
            isSleeping = isSleeping,
            finEstimado = 0L,
            lastInteraction = now
        )
        petDao.updatePet(updatedPet)

        val detail = if (isSleeping) "¡Pusiste a dormir a ${pet.name}!" else "¡Despertaste a ${pet.name}!"
        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = "Tú",
                actionType = "SLEEP",
                actionDetails = detail
            )
        )
        return true
    }

    suspend fun buyItem(itemId: String): Boolean {
        val item = petDao.getInventoryItem(itemId) ?: return false
        val stats = petDao.getUserStats() ?: return false

        if (stats.coins < item.cost) return false

        // Spend coins
        val newCoins = stats.coins - item.cost
        petDao.updateUserStats(stats.copy(coins = newCoins))

        // Increment item quantity or unlock accessory/clothing
        val isWearable = item.category == "ACCESSORY" || item.category == "CLOTHING"
        val newQty = if (isWearable) 1 else item.quantity + 1
        val updatedItem = item.copy(
            quantity = newQty,
            isUnlocked = if (isWearable) true else item.isUnlocked
        )
        petDao.updateInventoryItem(updatedItem)

        // Log buying action
        val activePet = petDao.getLatestPet()
        if (activePet != null) {
            petDao.insertActivityLog(
                ActivityLogEntity(
                    petId = activePet.id,
                    actorName = "Tú",
                    actionType = "BUY",
                    actionDetails = "Compraste ${item.name} por ${item.cost} monedas"
                )
            )
        }
        return true
    }

    suspend fun equipAccessory(petId: Int, itemId: String?): Boolean {
        val pet = petDao.getPetById(petId) ?: return false
        
        // If equipping accessories, check if it's unlocked
        if (itemId != null) {
            val item = petDao.getInventoryItem(itemId) ?: return false
            if (!item.isUnlocked && item.quantity <= 0) return false
        }

        // Accessorise
        // Item ID will be like "HAT_CROWN", strip "HAT_" to save format "CROWN"
        val hatCode = itemId?.replace("HAT_", "")
        
        val updatedPet = pet.copy(equippedHat = hatCode)
        petDao.updatePet(updatedPet)

        val logText = if (hatCode == null) "Te quitaste el accesorio de cabeza" else "Le equipaste el accesorio ${hatCode.replace("_", " ")}"
        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = "Tú",
                actionType = "PLAY",
                actionDetails = logText
            )
        )
        return true
    }

    suspend fun equipOutfit(petId: Int, itemId: String?): Boolean {
        val pet = petDao.getPetById(petId) ?: return false
        if (itemId != null) {
            val item = petDao.getInventoryItem(itemId) ?: return false
            if (!item.isUnlocked && item.quantity <= 0) return false
        }
        val updatedPet = pet.copy(equippedAccessory = itemId)
        petDao.updatePet(updatedPet)

        val logText = if (itemId == null) "Te quitaste el atuendo" else "Le equipaste el atuendo ${itemId.replace("CLOTH_", "").replace("_", " ")}"
        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = "Tú",
                actionType = "PLAY",
                actionDetails = logText
            )
        )
        return true
    }

    suspend fun saveMinigameResult(petId: Int, gameName: String, coinsEarned: Int, points: Int): Boolean {
        val pet = petDao.getPetById(petId) ?: return false
        val stats = petDao.getUserStats() ?: return false

        // Increment happiness
        val newHappiness = (pet.happiness + 20f).coerceAtMost(100f)
        val newEnergy = (pet.energy - 10f).coerceAtLeast(10f)

        petDao.updatePet(pet.copy(
            happiness = newHappiness,
            energy = newEnergy,
            lastInteraction = System.currentTimeMillis()
        ))

        // Add coins and increment played counts
        val updatedStats = stats.copy(
            coins = stats.coins + coinsEarned,
            catcherPlayed = if (gameName == "Catcher") stats.catcherPlayed + 1 else stats.catcherPlayed,
            memoryPlayed = if (gameName == "Memory") stats.memoryPlayed + 1 else stats.memoryPlayed,
            tictactoePlayed = if (gameName == "TicTacToe") stats.tictactoePlayed + 1 else stats.tictactoePlayed,
            wordsearchPlayed = if (gameName == "WordSearch") stats.wordsearchPlayed + 1 else stats.wordsearchPlayed,
            simonPlayed = if (gameName == "Simon") stats.simonPlayed + 1 else stats.simonPlayed,
            flappyPlayed = if (gameName == "Flappy") stats.flappyPlayed + 1 else stats.flappyPlayed,
            quizPlayed = if (gameName == "Quiz") stats.quizPlayed + 1 else stats.quizPlayed,
            bubblePlayed = if (gameName == "Bubble") stats.bubblePlayed + 1 else stats.bubblePlayed,
            mathPlayed = if (gameName == "Math") stats.mathPlayed + 1 else stats.mathPlayed
        )
        petDao.updateUserStats(updatedStats)

        awardXp(15 + coinsEarned / 2)

        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = "Tú",
                actionType = "PLAY",
                actionDetails = "Jugaste un minijuego: ¡Obtuviste $points pts y ganaste +$coinsEarned Monedas!"
            )
        )
        return true
    }

    suspend fun setPermanentShareCode(code: String): Boolean {
        checkAndInitializeData()
        val stats = petDao.getUserStats() ?: return false
        val updatedStats = stats.copy(
            linkCode = code,
            syncTimestamp = System.currentTimeMillis()
        )
        petDao.updateUserStats(updatedStats)
        return true
    }

    suspend fun linkPartner(partnerCode: String): Boolean {
        checkAndInitializeData()
        val stats = petDao.getUserStats() ?: return false
        
        val partnerName = "Compañero"
        val updatedStats = stats.copy(
            isLinked = true,
            partnerName = partnerName,
            partnerAvatar = "👤",
            linkCode = partnerCode,
            syncTimestamp = System.currentTimeMillis()
        )
        petDao.updateUserStats(updatedStats)

        val activePet = petDao.getLatestPet()
        if (activePet != null) {
            petDao.insertActivityLog(
                ActivityLogEntity(
                    petId = activePet.id,
                    actorName = "Sistema",
                    actionType = "LINK",
                    actionDetails = "¡Mascota vinculada exitosamente! Ahora compartes el cuidado con $partnerName (Código: $partnerCode)."
                )
            )
        }
        return true
    }

    suspend fun unlinkPartner(): Boolean {
        val stats = petDao.getUserStats() ?: return false
        
        val updatedStats = stats.copy(
            isLinked = false,
            partnerName = null,
            partnerAvatar = null,
            syncTimestamp = System.currentTimeMillis()
        )
        petDao.updateUserStats(updatedStats)

        val activePet = petDao.getLatestPet()
        if (activePet != null) {
            petDao.insertActivityLog(
                ActivityLogEntity(
                    petId = activePet.id,
                    actorName = "Sistema",
                    actionType = "LINK",
                    actionDetails = "Desvinculaste la mascota. Ahora juegas de forma individual."
                )
            )
        }
        return true
    }

    suspend fun applyDownloadedState(fetched: CoMascotaSyncService.FetchedState) {
        val currentPet = petDao.getLatestPet()
        if (currentPet != null) {
            val updatedPet = currentPet.copy(
                name = fetched.pet.name,
                type = fetched.pet.type,
                hunger = fetched.pet.hunger,
                energy = fetched.pet.energy,
                happiness = fetched.pet.happiness,
                cleanliness = fetched.pet.cleanliness,
                lastInteraction = fetched.pet.lastInteraction,
                equippedHat = fetched.pet.equippedHat,
                equippedAccessory = fetched.pet.equippedAccessory,
                isSleeping = fetched.pet.isSleeping
            )
            petDao.updatePet(updatedPet)
        } else {
            petDao.insertPet(fetched.pet)
        }

        checkAndInitializeData()
        val stats = petDao.getUserStats()
        if (stats != null) {
            val updatedStats = stats.copy(
                coins = fetched.coins,
                level = fetched.level,
                xp = fetched.xp,
                syncTimestamp = fetched.syncTimestamp,
                catcherPlayed = Math.max(stats.catcherPlayed, fetched.catcherPlayed),
                memoryPlayed = Math.max(stats.memoryPlayed, fetched.memoryPlayed),
                tictactoePlayed = Math.max(stats.tictactoePlayed, fetched.tictactoePlayed),
                wordsearchPlayed = Math.max(stats.wordsearchPlayed, fetched.wordsearchPlayed),
                achievementsClaimed = if (fetched.achievementsClaimed.length > stats.achievementsClaimed.length) fetched.achievementsClaimed else stats.achievementsClaimed,
                partnerName = fetched.partnerName ?: stats.partnerName,
                partnerAvatar = fetched.partnerAvatar ?: stats.partnerAvatar,
                isLinked = stats.isLinked || fetched.isLinked
            )
            petDao.updateUserStats(updatedStats)
        }

        // Overwrite standard shop quantities
        fetched.inventory.forEach { inv ->
            val localInv = petDao.getInventoryItem(inv.itemId)
            if (localInv != null) {
                petDao.updateInventoryItem(localInv.copy(
                    quantity = inv.quantity,
                    isUnlocked = inv.isUnlocked
                ))
            } else {
                petDao.insertInventoryItem(inv)
            }
        }

        val statsBefore = petDao.getUserStats()
        val prevTimestamp = statsBefore?.syncTimestamp ?: 0L
        val myName = statsBefore?.myName ?: "Yo"

        val activePet = petDao.getLatestPet()
        if (activePet != null) {
            petDao.clearLogsForPet(activePet.id)
            fetched.logs.forEach { log ->
                petDao.insertActivityLog(log.copy(id = 0, petId = activePet.id))
            }
            if (prevTimestamp > 0 && fetched.syncTimestamp > prevTimestamp && context != null) {
                val newLog = fetched.logs.firstOrNull { log ->
                    log.timestamp > prevTimestamp && log.actorName != myName &&
                            (log.actionType == "FEED" || log.actionType == "SLEEP")
                }
                if (newLog != null) {
                    com.example.util.NotificationHelper.showCoCareNotification(
                        context,
                        "💖 Co-Mascota: ${newLog.actorName}",
                        newLog.actionDetails
                    )
                }
            }
        }
    }

    suspend fun simulatePartnerAction(petId: Int): String? {
        val pet = petDao.getPetById(petId) ?: return null
        val stats = petDao.getUserStats() ?: return null

        if (!stats.isLinked) return null

        val partner = stats.partnerName ?: "Tu Compañero"
        val actions = listOf("FEED", "PLAY", "BATH", "SLEEP")
        val chosenAction = actions.random()
        
        var details = ""
        var updatedPet = pet

        when (chosenAction) {
            "FEED" -> {
                val foodList = listOf("Manzana Dulce", "Sushi Fresco", "Pizza Crujiente", "Pastel de Bodas")
                val food = foodList.random()
                val boost = Random.nextInt(15, 35)
                updatedPet = pet.copy(
                    hunger = (pet.hunger + boost).coerceAtMost(100f),
                    happiness = (pet.happiness + 5).coerceAtMost(100f),
                    lastInteraction = System.currentTimeMillis()
                )
                details = "$partner alimentó a ${pet.name} con un(a) $food (+$boost Hambre)"
            }
            "PLAY" -> {
                val games = listOf("Slime Catcher", "Memory Fun", "Toma de Siesta", "Parque de Mascotas")
                val game = games.random()
                val joy = Random.nextInt(20, 30)
                updatedPet = pet.copy(
                    happiness = (pet.happiness + joy).coerceAtMost(100f),
                    energy = (pet.energy - 10f).coerceAtLeast(5f),
                    lastInteraction = System.currentTimeMillis()
                )
                details = "$partner jugó a $game con ${pet.name} (+$joy Felicidad)"
            }
            "BATH" -> {
                val cleanGains = Random.nextInt(25, 45)
                updatedPet = pet.copy(
                    cleanliness = (pet.cleanliness + cleanGains).coerceAtMost(100f),
                    lastInteraction = System.currentTimeMillis()
                )
                details = "$partner bañó a ${pet.name} (+$cleanGains Limpieza)"
            }
            "SLEEP" -> {
                val toggle = !pet.isSleeping
                updatedPet = pet.copy(
                    isSleeping = toggle,
                    lastInteraction = System.currentTimeMillis()
                )
                details = if (toggle) {
                    "$partner puso a dormir a ${pet.name} para recargar energías"
                } else {
                    "$partner despertó a ${pet.name} para jugar"
                }
            }
        }

        petDao.updatePet(updatedPet)
        petDao.insertActivityLog(
            ActivityLogEntity(
                petId = petId,
                actorName = partner,
                actionType = chosenAction,
                actionDetails = details
            )
        )
        if (context != null && (chosenAction == "FEED" || chosenAction == "SLEEP")) {
            com.example.util.NotificationHelper.showCoCareNotification(
                context,
                "💖 Co-Mascota: $partner",
                details
            )
        }
        return details
    }

    suspend fun applyTimeDecayForAllPets() {
        val pets = petDao.getAllPetsFlow().firstOrNull() ?: return
        val now = System.currentTimeMillis()
        for (pet in pets) {
            val elapsedSeconds = (now - pet.lastInteraction) / 1000f
            if (elapsedSeconds >= 12f) {
                val elapsedMinutes = (elapsedSeconds / 60f).coerceAtMost(120f)
                
                val hungerDecay = if (pet.isSleeping) elapsedMinutes * 0.1f else elapsedMinutes * 0.4f
                val cleanlinessDecay = if (pet.isSleeping) elapsedMinutes * 0.05f else elapsedMinutes * 0.3f
                val happinessDecay = if (pet.isSleeping) 0f else elapsedMinutes * 0.35f
                val energyGainOrLoss = if (pet.isSleeping) elapsedMinutes * 2.0f else -elapsedMinutes * 0.3f

                val hunger = (pet.hunger - hungerDecay).coerceIn(0f, 100f)
                val cleanliness = (pet.cleanliness - cleanlinessDecay).coerceIn(0f, 100f)
                val happiness = (pet.happiness - happinessDecay).coerceIn(0f, 100f)
                val energy = (pet.energy + energyGainOrLoss).coerceIn(0f, 100f)

                petDao.updatePet(
                    pet.copy(
                        hunger = hunger,
                        cleanliness = cleanliness,
                        happiness = happiness,
                        energy = energy,
                        isSleeping = pet.isSleeping,
                        finEstimado = pet.finEstimado,
                        lastInteraction = now
                    )
                )

                // Push notifications based on needs and routines
                val stats = petDao.getUserStats()
                if (stats != null && stats.notificationsEnabled && context != null) {
                    val cal = java.util.Calendar.getInstance()
                    val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)

                    if (hunger < 25f && pet.hunger >= 25f) {
                        com.example.util.NotificationHelper.showPushNotification(
                            context,
                            "🍗 ¡${pet.name} tiene mucha hambre!",
                            "Su nivel de hambre bajó al ${hunger.toInt()}%. ¡Entra a PawPair para darle un bocadillo!"
                        )
                    }
                    if (energy < 20f && pet.energy >= 20f && !pet.isSleeping) {
                        com.example.util.NotificationHelper.showPushNotification(
                            context,
                            "😴 ¡${pet.name} está agotado!",
                            "Su energía está muy baja (${energy.toInt()}%). ¡Ponlo a dormir para que recargue fuerzas!"
                        )
                    }
                    if (happiness < 25f && pet.happiness >= 25f) {
                        com.example.util.NotificationHelper.showPushNotification(
                            context,
                            "🧸 ¡${pet.name} se siente aburrido!",
                            "¡Juega un minijuego o dale mimos para subir su ánimo!"
                        )
                    }
                    if (currentHour == stats.routineHour) {
                        // Avoid spamming routine every second during the hour using SharedPreferences
                        val prefs = context.getSharedPreferences("pawpair_routine", android.content.Context.MODE_PRIVATE)
                        val lastRoutineDay = prefs.getInt("last_routine_day", -1)
                        val currentDay = cal.get(java.util.Calendar.DAY_OF_YEAR)
                        if (lastRoutineDay != currentDay) {
                            prefs.edit().putInt("last_routine_day", currentDay).apply()
                            com.example.util.NotificationHelper.showPushNotification(
                                context,
                                "⏰ Rutina diaria de PawPair",
                                "¡Es hora de revisar a ${pet.name}! Aliméntalo, límpialo y juega un rato antes de terminar el día."
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun awardXp(gainedXp: Int) {
        val stats = petDao.getUserStats() ?: return
        var currentXp = stats.xp + gainedXp
        var currentLevel = stats.level
        var coinsGained = 0
        var leveledUp = false

        while (currentXp >= GameRules.getRequiredXp(currentLevel)) {
            currentXp -= GameRules.getRequiredXp(currentLevel)
            currentLevel++
            coinsGained += 100
            leveledUp = true
        }

        if (leveledUp) {
            val activePet = petDao.getLatestPet()
            if (activePet != null) {
                petDao.insertActivityLog(
                    ActivityLogEntity(
                        petId = activePet.id,
                        actorName = "Sistema",
                        actionType = "LINK",
                        actionDetails = "¡Subiste de nivel! Ahora eres Nivel $currentLevel 🎉 (+$coinsGained Monedas)"
                    )
                )
            }
            petDao.updateUserStats(stats.copy(coins = stats.coins + coinsGained, level = currentLevel, xp = currentXp))
        } else {
            petDao.updateUserStats(stats.copy(xp = currentXp))
        }
    }

    private fun getPetTypeLabel(type: String): String {
        return when (type) {
            "SHIBA" -> "Shiba Inu 🐶"
            "SLIME" -> "Slime Saltarín 💧"
            "KITTY" -> "Gatito Kawaii 🐱"
            "DRACO" -> "Mini Dragón 🔥"
            "AXOLOTL" -> "Axol Mágico 🌸"
            else -> "Mascota 🐾"
        }
    }

    suspend fun deletePet(pet: PetEntity) {
        petDao.deletePet(pet)
    }
}
