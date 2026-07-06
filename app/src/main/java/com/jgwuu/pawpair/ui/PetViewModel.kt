package com.jgwuu.pawpair.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jgwuu.pawpair.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class PetViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PetRepository(db.petDao(), application)

    // UI exposed states
    val allPets: StateFlow<List<PetEntity>> = repository.allPets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStatsEntity?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val inventory: StateFlow<List<InventoryEntity>> = repository.inventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activePetId = MutableStateFlow<Int?>(null)
    val activePetId: StateFlow<Int?> = _activePetId.asStateFlow()

    // Active pet observed from DB reactive flow
    val activePet: StateFlow<PetEntity?> = combine(allPets, _activePetId) { list, id ->
        if (id != null) {
            list.find { it.id == id }
        } else {
            list.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Activity logs for active pet
    val activityLogs: StateFlow<List<ActivityLogEntity>> = activePet
        .flatMapLatest { pet ->
            if (pet != null) {
                repository.getActivityLogs(pet.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _serverTestResult = MutableStateFlow<String?>(null)
    val serverTestResult: StateFlow<String?> = _serverTestResult.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _interactionTrigger = MutableStateFlow(0L)
    val interactionTrigger: StateFlow<Long> = _interactionTrigger.asStateFlow()

    // AI Chat States
    private val _chatMessages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val chatMessages: StateFlow<List<Pair<String, String>>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun initializeChatIfEmpty(petName: String) {
        if (_chatMessages.value.isEmpty()) {
            _chatMessages.value = listOf("Mascota" to "¡Hola! Soy $petName. ¿De qué quieres que hablemos hoy? ✨🐾")
        }
    }

    fun sendChatMessage(message: String, pet: PetEntity) {
        if (message.isBlank()) return
        
        val currentList = _chatMessages.value.toMutableList()
        currentList.add("User" to message)
        _chatMessages.value = currentList
        _isChatLoading.value = true

        val currentLevel = userStats.value?.level ?: 1

        viewModelScope.launch {
            CoMascotaSyncService.chatWithMascota(message, pet, currentLevel) { reply ->
                viewModelScope.launch {
                    _isChatLoading.value = false
                    val updatedList = _chatMessages.value.toMutableList()
                    updatedList.add("Mascota" to reply)
                    _chatMessages.value = updatedList
                }
            }
        }
    }

    fun clearChat(petName: String) {
        _chatMessages.value = listOf("Mascota" to "¡Hola! Soy $petName. ¿De qué quieres que hablemos hoy? ✨🐾")
    }

    init {
        com.jgwuu.pawpair.data.FirebaseManager.init(application)
        viewModelScope.launch {
            repository.checkAndInitializeData()
            // Set latest pet as active pet
            val latest = repository.getLatestPet()
            if (latest != null) {
                _activePetId.value = latest.id
            }
            _isLoading.value = false

            // Start passive decay loop
            launchTimeDecayLoop()
            
            // Listen to remote changes
            launch {
                com.jgwuu.pawpair.data.RealtimeSyncManager.remoteState.collect { remote ->
                    if (remote != null) {
                        repository.applyDownloadedState(remote)
                    }
                }
            }
            

            var lastNotifiedTimestamp = 0L
            launch {
                com.jgwuu.pawpair.data.RealtimeSyncManager.latestAction.collect { action ->
                    if (action != null && action.timestamp > lastNotifiedTimestamp) {
                        lastNotifiedTimestamp = action.timestamp
                        val myUid = com.jgwuu.pawpair.data.CoMascotaSyncService.getFirebaseUid(getApplication<Application>().applicationContext)
                        if (action.uid != myUid && action.uid.isNotEmpty()) {
                            val actorName = userStats.value?.partnerName ?: "Compañero"
                            val actionDesc = when (action.tipo) {
                                "FEED" -> "alimentó a la mascota 🍗"
                                "PLAY" -> "jugó con la mascota 🎾"
                                "BATH" -> "bañó a la mascota 🧼"
                                "SLEEP" -> "acostó a dormir a la mascota 💤"
                                else -> "realizó una acción (${action.tipo})"
                            }
                            viewModelScope.launch {
                                _toastMessage.emit("🔔 ¡$actorName $actionDesc!")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchTimeDecayLoop() {
        viewModelScope.launch {
            while (true) {
                delay(15000) // update decay every 15s for visual responsiveness
                repository.applyTimeDecayForAllPets()
            }
        }
    }

    fun pullSharedStateFromServer(onComplete: ((Boolean) -> Unit)? = null) {
        val stats = userStats.value ?: return
        val code = stats.linkCode ?: return
        if (!stats.isLinked) {
            onComplete?.invoke(false)
            return
        }

        com.jgwuu.pawpair.data.RealtimeSyncManager.fetchSingleState(code) { fetched ->
            if (fetched != null) {
                // To avoid overwrite loops: only update if remote timestamp is newer!
                if (fetched.syncTimestamp > stats.syncTimestamp) {
                    viewModelScope.launch {
                        repository.applyDownloadedState(fetched)
                        _toastMessage.emit("🔄 ¡Mascota compartida sincronizada de la nube!")
                        triggerWidgetUpdate()
                        onComplete?.invoke(true)
                    }
                } else {
                    onComplete?.invoke(false)
                }
            } else {
                onComplete?.invoke(false)
            }
        }
    }

    fun selectPet(petId: Int) {
        _activePetId.value = petId
    }

    fun adoptNewPet(name: String, type: String) {
        viewModelScope.launch {
            repository.checkAndInitializeData()
            val id = repository.createPet(name, type)
            _activePetId.value = id
            _toastMessage.emit("🎉 ¡Adoptaste a $name con éxito! Registrando en la nube...")
            
            // Request a cloud token asynchronously to make this pet real-person shareable instantly!
            CoMascotaSyncService.createNewLinkKey { token ->
                viewModelScope.launch {
                    if (token != null) {
                        repository.checkAndInitializeData() // ensure it's not null
                        val stats = db.petDao().getUserStats()
                        if (stats != null) {
                            val updatedStats = stats.copy(linkCode = token)
                            db.petDao().updateUserStats(updatedStats)
                            
                            // Push initial state to this web token
                            val pet = repository.getLatestPet()
                            if (pet != null) {
                                CoMascotaSyncService.uploadState(
                                    token = token,
                                    pet = pet,
                                    stats = updatedStats,
                                    logs = emptyList(), // we just created it
                                    inventory = db.petDao().getInventoryFlow().firstOrNull() ?: emptyList(),
                                    context = getApplication<Application>().applicationContext
                                )
                            }
                        }
                    }
                }
            }
            triggerWidgetUpdate()
        }
    }

    fun interactWithActivePet() {
        val pet = activePet.value ?: return
        if (pet.isSleeping) {
            viewModelScope.launch { _toastMessage.emit("💤 Shhh... ${pet.name} está durmiendo profundamente.") }
            return
        }
        viewModelScope.launch {
            _interactionTrigger.value = System.currentTimeMillis()
            repository.interactPet(pet.id)
            val lvl = userStats.value?.level ?: 1
            val stageName = when {
                lvl >= 200 -> "Legendario Supremo 👑"
                lvl >= 100 -> "Místico Evolucionado 🔮"
                lvl >= 50 -> "Joven Evolución 🌟"
                else -> "Cachorro Bebé 🍼"
            }
            _toastMessage.emit("💖 ¡Mimos para ${pet.name}! Animación especial de etapa ($stageName) ✨")
            triggerWidgetUpdate()
        }
    }

    fun feedActivePet(itemId: String) {
        val pet = activePet.value ?: return
        viewModelScope.launch {
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
            val success = repository.feedPet(pet.id, itemId)
            if (success) {
                _interactionTrigger.value = System.currentTimeMillis()
                if (isFav) {
                    _toastMessage.emit("😍 ¡LE ENCANTÓ! ¡Es la COMIDA FAVORITA de ${pet.name}! (+Ánimo & Energía extra)")
                } else if (isRep) {
                    _toastMessage.emit("🤢 ¡A ${pet.name} LE DIO REPULSIÓN! Hizo una mueca de disgusto (-12 Ánimo)")
                } else {
                    _toastMessage.emit("🍲 ¡${pet.name} comió delicioso!")
                }
                triggerWidgetUpdate()
            } else {
                val item = inventory.value.find { it.itemId == itemId }
                if (item != null && item.quantity <= 0) {
                    _toastMessage.emit("❌ No te queda ${item.name}. ¡Compra en la tienda!")
                } else if (pet.isSleeping) {
                    _toastMessage.emit("💤 No puedes alimentar a ${pet.name} mientras duerme.")
                }
            }
        }
    }

    fun batheActivePet() {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val success = repository.bathePet(pet.id)
            if (success) {
                _toastMessage.emit("🧽 ¡${pet.name} quedó reluciente y feliz!")
            } else {
                _toastMessage.emit("🧼 ¡No te queda Jabón! Compra en la tienda por 8 monedas.")
            }
        }
    }

    fun soapActivePet(useShampoo: Boolean) {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val success = repository.soapPet(pet.id, useShampoo)
            if (success) {
                _toastMessage.emit("🧼 ¡Enjabonaste a ${pet.name} con mucha espuma!")
                triggerWidgetUpdate()
            } else {
                val itemName = if (useShampoo) "Champú" else "Jabón"
                _toastMessage.emit("❌ No tienes $itemName. Compra en el Supermercado.")
            }
        }
    }

    fun rinseActivePet() {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val success = repository.rinsePet(pet.id)
            if (success) {
                _toastMessage.emit("🚿 ¡Enjuagaste la espuma de ${pet.name}! Quedó impecable.")
                triggerWidgetUpdate()
            } else {
                _toastMessage.emit("❌ Error tratando de enjuagar a la mascota.")
            }
        }
    }

    fun brushActivePetTeeth() {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val success = repository.brushPetTeeth(pet.id)
            if (success) {
                _toastMessage.emit("🪥 ¡Dientecitos limpios! ${pet.name} brilla de felicidad ✨")
                triggerWidgetUpdate()
            } else {
                _toastMessage.emit("❌ Error tratando de cepillar los dientes.")
            }
        }
    }

    fun walkActivePet() {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val coins = repository.walkPet(pet.id)
            if (coins > 0) {
                _toastMessage.emit("🌳 ¡Volviendo del paseo! ${pet.name} encontró +$coins monedas 🪙 y se divirtió mucho.")
                triggerWidgetUpdate()
            } else if (coins == -1) {
                _toastMessage.emit("🥱 ${pet.name} está muy cansado. ¡Pónlo a dormir primero para recuperar energía!")
            } else {
                _toastMessage.emit("❌ No puedes pasear a ${pet.name} ahora.")
            }
        }
    }

    fun cookActivePetRecipe(recipeName: String, requiredIngredients: List<String>, hungerBoost: Float, happinessBoost: Float) {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val success = repository.cookAndFeedRecipe(pet.id, recipeName, requiredIngredients, hungerBoost, happinessBoost)
            if (success) {
                _toastMessage.emit("🍳 ¡Cocinaste un platazo de $recipeName! Se guardó en la Despensa, listo para alimentar a ${pet.name}. ✨")
                triggerWidgetUpdate()
            } else {
                _toastMessage.emit("❌ Ingredientes insuficientes para preparar $recipeName. ¡Ve al Supermercado!")
            }
        }
    }

    fun toggleSleepActivePet() {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            repository.toggleSleep(pet.id)
            if (!pet.isSleeping) {
                _toastMessage.emit("😴 Shhh... ${pet.name} se ha dormido.")
            } else {
                _toastMessage.emit("☀️ ¡Buenos días! ${pet.name} se ha despertado.")
            }
            triggerWidgetUpdate()
        }
    }

    fun buyShopItem(itemId: String) {
        viewModelScope.launch {
            val success = repository.buyItem(itemId)
            if (success) {
                val item = inventory.value.find { it.itemId == itemId }
                _toastMessage.emit("🛒 Compraste ${item?.name ?: "objeto"} con éxito.")
            } else {
                _toastMessage.emit("❌ Monedas insuficientes o item no disponible.")
            }
        }
    }

    fun equipActivePetAccessory(itemId: String?) {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val success = repository.equipAccessory(pet.id, itemId)
            if (success) {
                if (itemId == null) {
                    _toastMessage.emit("👒 Retiraste accesorio de ${pet.name}.")
                } else {
                    val item = inventory.value.find { it.itemId == itemId }
                    _toastMessage.emit("🎀 Le equipaste ${item?.name} a ${pet.name}.")
                }
            } else {
                _toastMessage.emit("❌ Primero debes comprar este accesorio en la tienda.")
            }
        }
    }

    fun equipActivePetOutfit(itemId: String?) {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val success = repository.equipOutfit(pet.id, itemId)
            if (success) {
                if (itemId == null) {
                    _toastMessage.emit("🦺 Retiraste el atuendo de ${pet.name}.")
                } else {
                    val item = inventory.value.find { it.itemId == itemId }
                    _toastMessage.emit("👗 Le equipaste ${item?.name} a ${pet.name}.")
                }
            } else {
                _toastMessage.emit("❌ Primero debes comprar este atuendo en la tienda.")
            }
        }
    }

    fun earnCoinsAndStatsFromMinigame(gameName: String, coinsEarned: Int, score: Int) {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            repository.saveMinigameResult(pet.id, gameName, coinsEarned, score)
            _toastMessage.emit("🎮 ¡Juego Terminado! Recompensa: +$coinsEarned Monedas y +20 Felicidad")
            triggerWidgetUpdate()
        }
    }

    fun updateProfile(name: String, avatar: String) {
        viewModelScope.launch {
            repository.checkAndInitializeData()
            val stats = db.petDao().getUserStats()
            if (stats != null) {
                val updatedStats = stats.copy(myName = name, myAvatar = avatar)
                db.petDao().updateUserStats(updatedStats)
                
                val context = getApplication<Application>().applicationContext

                val pet = repository.getLatestPet()
                val currentCode = updatedStats.linkCode
                if (pet != null && currentCode != null && currentCode.length == 6 && currentCode != "XXXX-XXXX") {
                    com.jgwuu.pawpair.data.CoMascotaSyncService.uploadState(
                        token = currentCode,
                        pet = pet,
                        stats = updatedStats,
                        logs = activityLogs.value,
                        inventory = inventory.value,
                        context = context
                    )
                }
                _toastMessage.emit("¡Perfil guardado con éxito!")
            }
        }
    }

    fun updateAppTheme(themeName: String) {
        viewModelScope.launch {
            val stats = db.petDao().getUserStats()
            if (stats != null) {
                db.petDao().updateUserStats(stats.copy(appTheme = themeName))
                _toastMessage.emit("🎨 Tema actualizado a: $themeName")
            }
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val stats = db.petDao().getUserStats()
            if (stats != null) {
                db.petDao().updateUserStats(stats.copy(notificationsEnabled = enabled))
                _toastMessage.emit(if (enabled) "🔔 Notificaciones activadas" else "🔕 Notificaciones desactivadas")
            }
        }
    }

    fun updateRoutineHour(hour: Int) {
        viewModelScope.launch {
            val stats = db.petDao().getUserStats()
            if (stats != null) {
                db.petDao().updateUserStats(stats.copy(routineHour = hour))
                _toastMessage.emit("⏰ Rutina configurada a las ${hour}:00 hrs")
            }
        }
    }

    fun generateLinkCode() {
        viewModelScope.launch {
            _toastMessage.emit("Generando código permanente de vinculación...")
            val token = CoMascotaSyncService.generate6CharCode()
            repository.setPermanentShareCode(token)
            val stats = db.petDao().getUserStats()
            val pet = repository.getLatestPet()
            if (stats != null && pet != null) {
                CoMascotaSyncService.uploadState(
                    token = token,
                    pet = pet,
                    stats = stats,
                    logs = activityLogs.value,
                    inventory = db.petDao().getInventoryFlow().firstOrNull() ?: emptyList(),
                    context = getApplication<Application>().applicationContext
                )
            }
            _toastMessage.emit("✅ Código permanente generado: $token")
        }
    }

    fun ensurePermanentShareCode(onReady: (String) -> Unit = {}) {
        viewModelScope.launch {
            val stats = userStats.value
            val pet = activePet.value ?: repository.getLatestPet()
            val currentCode = stats?.linkCode
            if (!currentCode.isNullOrBlank() && currentCode.length == 6 && currentCode != "XXXX-XXXX") {
                if (pet != null && stats != null) {
                    CoMascotaSyncService.uploadState(
                        token = currentCode,
                        pet = pet,
                        stats = stats,
                        logs = activityLogs.value,
                        inventory = inventory.value,
                        context = getApplication<Application>().applicationContext
                    )
                }
                onReady(currentCode)
            } else {
                val newCode = CoMascotaSyncService.generate6CharCode()
                repository.setPermanentShareCode(newCode)
                val updatedStats = db.petDao().getUserStats()
                if (pet != null && updatedStats != null) {
                    CoMascotaSyncService.uploadState(
                        token = newCode,
                        pet = pet,
                        stats = updatedStats,
                        logs = activityLogs.value,
                        inventory = inventory.value,
                        context = getApplication<Application>().applicationContext
                    )
                }
                onReady(newCode)
            }
        }
    }

    fun runServerTest() {
        viewModelScope.launch {
            _serverTestResult.value = "Probando conexión con el servidor..."
            CoMascotaSyncService.testServerConnection { result ->
                _serverTestResult.value = result
            }
        }
    }
    
    fun clearServerTest() {
        _serverTestResult.value = null
    }

    fun linkWithPartnerCode(code: String, onFinished: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val cleanCode = code.trim().uppercase()
            if (cleanCode.isBlank()) {
                _toastMessage.emit("❌ Código requerido.")
                onFinished?.invoke(false)
                return@launch
            }

            _toastMessage.emit("Vinculando mascota con código $cleanCode...")
            val myUid = CoMascotaSyncService.getFirebaseUid(getApplication<Application>().applicationContext)
            com.jgwuu.pawpair.data.RealtimeSyncManager.joinPetAndFetch(cleanCode, myUid) { success, fetched ->
                viewModelScope.launch {
                    if (success && fetched != null) {
                        repository.linkPartner(cleanCode)
                        repository.applyDownloadedState(fetched)
                        val latest = repository.getLatestPet()
                        if (latest != null) {
                            _activePetId.value = latest.id
                        }
                        _toastMessage.emit("🔗 ¡Mascota vinculada y sincronizada con éxito!")
                        triggerWidgetUpdate()
                        onFinished?.invoke(true)
                    } else {
                        _toastMessage.emit("❌ La mascota con código $cleanCode no existe o no se pudo acceder.")
                        onFinished?.invoke(false)
                    }
                }
            }
        }
    }

    fun triggerSimulatedPartnerInteraction() {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val detail = repository.simulatePartnerAction(pet.id)
            if (detail != null) {
                // If linked, also upload latest state to cloud so it propagates to actual partner device
                val stats = userStats.value
                val code = stats?.linkCode
                if (stats?.isLinked == true && code != null) {
                    CoMascotaSyncService.uploadState(
                        token = code,
                        pet = pet,
                        stats = stats,
                        logs = activityLogs.value,
                        inventory = inventory.value,
                        context = getApplication<Application>().applicationContext
                    )
                }
                _toastMessage.emit("🎯 Acción de Socio: $detail")
            } else {
                _toastMessage.emit("❌ Vincula tu mascota primero para jugar con un amigo.")
            }
        }
    }

    fun unlinkCurrentPartner() {
        viewModelScope.launch {
            repository.unlinkPartner()
            _toastMessage.emit("💔 Se ha eliminado el vínculo co-care.")
        }
    }

    fun triggerWidgetUpdate() {
        try {
            val context = getApplication<Application>().applicationContext
            val intent = android.content.Intent(context, com.jgwuu.pawpair.widget.PetAppWidgetProvider::class.java).apply {
                action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = android.appwidget.AppWidgetManager.getInstance(context).getAppWidgetIds(
                    android.content.ComponentName(context, com.jgwuu.pawpair.widget.PetAppWidgetProvider::class.java)
                )
                putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            android.util.Log.e("PetViewModel", "Widget broadcast update fail", e)
        }

        // Asynchronous cloud push of live state to ensure actual partner sees updates
        val stats = userStats.value
        val pet = activePet.value
        val code = stats?.linkCode
        if (stats != null && stats.isLinked && code != null && pet != null) {
            viewModelScope.launch {
                CoMascotaSyncService.uploadState(
                    token = code,
                    pet = pet,
                    stats = stats,
                    logs = activityLogs.value,
                    inventory = inventory.value,
                    context = getApplication<Application>().applicationContext
                )
            }
        }
    }

    fun triggerToast(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    fun releaseOrDeletePet(isOwner: Boolean, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val pet = activePet.value
            val stats = userStats.value
            if (pet == null) {
                onResult(false, "No tienes mascota activa.")
                return@launch
            }
            val db = com.jgwuu.pawpair.data.FirebaseManager.getDatabase()
            val uid = com.jgwuu.pawpair.data.FirebaseManager.getCurrentUserId() ?: "anon"
            val code = stats?.linkCode?.trim()?.uppercase()
            
            if (db != null && !code.isNullOrBlank()) {
                if (isOwner) {
                    db.getReference("pets/$code").removeValue()
                    db.getReference("codigos/$code").removeValue()
                    db.getReference("users/$uid/linkedPets/$code").removeValue()
                } else {
                    db.getReference("pets/$code/colaboradorUid").removeValue()
                    db.getReference("users/$uid/linkedPets/$code").removeValue()
                }
            }
            com.jgwuu.pawpair.data.RealtimeSyncManager.stopListening()
            repository.deletePet(pet)
            onResult(true, if (isOwner) "Mascota eliminada por completo." else "Has abandonado el cuidado compartido.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        com.jgwuu.pawpair.data.RealtimeSyncManager.stopListening()
    }
}
