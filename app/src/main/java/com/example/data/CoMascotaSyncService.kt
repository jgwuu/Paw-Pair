package com.example.data

import com.example.BuildConfig
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

object CoMascotaSyncService {
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private const val BASE_URL = "https://api.keyvalue.xyz"
    
    private val firebaseDbUrl: String
        get() {
            val url1 = BuildConfig.FIREBASE_DATABASE_URL.trim()
            val url2 = BuildConfig.FIREBASE_DATABASE_.trim()
            if (url1.isNotBlank() && url1 != "https://my-firebase-project.firebaseio.com") {
                return url1
            }
            if (url2.isNotBlank() && url2 != "https://my-firebase-project.firebaseio.com") {
                return url2
            }
            return url1
        }

    val geminiApiKey: String
        get() {
            val key1 = BuildConfig.GEMINI_API_KEY.trim()
            val key2 = BuildConfig.key.trim()
            if (key1.isNotBlank() && key1 != "MY_GEMINI_API_KEY") {
                return key1
            }
            if (key2.isNotBlank() && key2 != "MY_GEMINI_API_KEY") {
                return key2
            }
            return key1
        }

    val firebaseConfigError: String?
        get() {
            val url = firebaseDbUrl
            if (url.isBlank() || url == "https://my-firebase-project.firebaseio.com") return null
            if (url.contains("console.firebase.google.com") || url.contains("firebase.google.com/u/")) {
                return "Has configurado la URL de la Consola web de Firebase en lugar de la URL del Servidor de Realtime Database."
            }
            if (!url.startsWith("http")) {
                return "La URL de Firebase debe comenzar con http:// o https://"
            }
            return null
        }

    val isFirebaseEnabled: Boolean
        get() = firebaseDbUrl.isNotBlank() && 
                firebaseDbUrl.startsWith("http") && 
                firebaseDbUrl != "https://my-firebase-project.firebaseio.com" &&
                firebaseConfigError == null



    /**
     * Generates a permanent 6-character unique alphanumeric code.
     */
    fun generate6CharCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    /**
     * ID persistente al inicio: Al abrir la app, revisa FirebaseAuth.getInstance().currentUser.
     * Si es null, revisa SharedPreferences para reutilizar el mismo UID en cada inicio posterior.
     * Si no existe en ninguno, recién ahí llama signInAnonymously() y guarda el UID en SharedPreferences.
     */
    fun getFirebaseUid(context: android.content.Context? = null): String {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val current = auth.currentUser
            if (current != null) {
                val uid = current.uid
                if (context != null && uid.isNotBlank()) {
                    context.getSharedPreferences("co_mascota_auth", android.content.Context.MODE_PRIVATE)
                        .edit().putString("user_uid", uid).apply()
                }
                return uid
            }
        } catch (e: Exception) {
            // FirebaseAuth exception
        }
        // En arranques posteriores si currentUser es null o aún está cargando, reutiliza el mismo UID sin volver a autenticar
        if (context != null) {
            val prefs = context.getSharedPreferences("co_mascota_auth", android.content.Context.MODE_PRIVATE)
            val savedUid = prefs.getString("user_uid", null)
            if (!savedUid.isNullOrBlank()) {
                // Iniciar autenticación anónima en background por si la sesión de Firebase no estaba activa
                try { com.google.firebase.auth.FirebaseAuth.getInstance().signInAnonymously() } catch (_: Exception) {}
                return savedUid
            }
        }
        // Si no había sesión previa ni UID guardado, inicia anónimamente y guarda en SharedPreferences
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            auth.signInAnonymously().addOnSuccessListener { res ->
                val uid = res.user?.uid
                if (context != null && !uid.isNullOrBlank()) {
                    context.getSharedPreferences("co_mascota_auth", android.content.Context.MODE_PRIVATE)
                        .edit().putString("user_uid", uid).apply()
                }
            }
        } catch (_: Exception) {}

        if (context != null) {
            val prefs = context.getSharedPreferences("co_mascota_auth", android.content.Context.MODE_PRIVATE)
            var uid = prefs.getString("user_uid", null)
            if (uid.isNullOrBlank()) {
                uid = "usr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)
                prefs.edit().putString("user_uid", uid).apply()
            }
            return uid
        }
        return "default_user_uid"
    }

    /**
     * Performs a POST to api.keyvalue.xyz/new/gameState to obtain a unique linkage token.
     * Or generates a permanent 6-character alphanumeric ID for Firebase.
     */
    fun createNewLinkKey(onResult: (String?) -> Unit) {
        if (isFirebaseEnabled) {
            val randomToken = generate6CharCode()
            onResult(randomToken)
            return
        }

        val request = Request.Builder()
            .url("$BASE_URL/new/gameState")
            .post("{}".toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("CoMascotaSyncService", "Error creating linkage token", e)
                onResult(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string() ?: ""
                Log.d("CoMascotaSyncService", "Token generation response: $body")
                val token = extractTokenFromUrl(body)
                onResult(token)
            }
        })
    }

    /**
     * Tests connection to the underlying backend server (Firebase RTDB or KeyValue API).
     */
    fun testServerConnection(onResult: (String) -> Unit) {
        val configError = firebaseConfigError
        if (configError != null) {
            onResult("🔴 Error de Configuración:\n$configError\n\n" +
                     "Has ingresado la URL de la Consola web de administración de Firebase.\n\n" +
                     "La URL correcta debe verse así:\n" +
                     "https://[tu-proyecto]-default-rtdb.firebaseio.com/\n\n" +
                     "Para encontrarla:\n" +
                     "1. Entra a tu Consola de Firebase.\n" +
                     "2. Ve a la sección 'Realtime Database' en el menú izquierdo.\n" +
                     "3. Copia la URL que aparece justo encima de la pestaña 'Datos' (comienza con https:// y termina en .firebaseio.com o .firebasedatabase.app).\n" +
                     "4. Agrégala en los Secretos de AI Studio con el nombre: FIREBASE_DATABASE_URL")
            return
        }

        val url = if (isFirebaseEnabled) {
            "${firebaseDbUrl.trimEnd('/')}/.json?shallow=true"
        } else {
            "https://api.keyvalue.xyz"
        }
        
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
            
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onResult("🔴 Error de conexión: ${e.message ?: "IOException"}\nServidor: $url")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val code = response.code
                val bodyText = response.body?.string()?.take(150) ?: ""
                val backendType = if (isFirebaseEnabled) "Firebase RTDB" else "KeyValue API"
                
                // Double check if the response HTML is google accounts sign in
                if (bodyText.contains("accounts.google.com") || bodyText.contains("signin")) {
                    onResult("🔴 Error de Configuración (Redirección):\n" +
                             "El servidor devolvió una página de inicio de sesión de Google.\n\n" +
                             "Esto confirma que la URL configurada ($url) es de la consola de Firebase o requiere privilegios de navegador.\n\n" +
                             "Por favor, asegúrate de ingresar la URL de la Base de Datos (ej: https://proyecto.firebaseio.com/) en lugar de la consola de administración en los Secretos de AI Studio.")
                    return
                }

                if (response.isSuccessful) {
                    onResult("🟢 ¡Conexión Exitosa!\nServidor: $url\nBackend: $backendType\nCódigo: $code\nRespuesta: $bodyText")
                } else {
                    onResult("⚠️ Error del Servidor: Código $code\nServidor: $url\nBackend: $backendType\nRespuesta: $bodyText")
                }
            }
        })
    }

    /**
     * Uploads the entire game state to the key-value store under the given token.
     */
    fun uploadState(token: String, pet: PetEntity, stats: UserStatsEntity, logs: List<ActivityLogEntity>, inventory: List<InventoryEntity>, context: android.content.Context? = null, onResult: (Boolean) -> Unit = {}) {
        try {
            val payload = JSONObject()
            val linkCode = if (!stats.linkCode.isNullOrBlank() && stats.linkCode.length == 6) stats.linkCode else token
            payload.put("codigoVinculacion", linkCode)
            
            // 1. Serialize Pet
            val finEst = pet.finEstimado
            val now = System.currentTimeMillis()
            val isActuallySleeping = pet.isSleeping && (finEst <= 0L || now <= finEst)

            val petJson = JSONObject().apply {
                put("name", pet.name)
                put("type", pet.type)
                put("hunger", pet.hunger.toDouble())
                put("energy", pet.energy.toDouble())
                put("happiness", pet.happiness.toDouble())
                put("cleanliness", pet.cleanliness.toDouble())
                put("createdAt", pet.createdAt)
                put("lastInteraction", pet.lastInteraction)
                put("equippedHat", pet.equippedHat ?: JSONObject.NULL)
                put("equippedAccessory", pet.equippedAccessory ?: JSONObject.NULL)
                put("isSleeping", isActuallySleeping)
                put("finEstimado", finEst)
                put("dormirInicio", pet.dormirInicio)
                put("codigoVinculacion", linkCode)
            }
            payload.put("pet", petJson)

            val estadoActualJson = JSONObject().apply {
                put("tipo", if (isActuallySleeping) "SLEEP" else "IDLE")
                put("inicio", pet.lastInteraction)
                put("finEstimado", finEst)
            }
            payload.put("estadoActual", estadoActualJson)

            // 2. Serialize Stats
            val statsJson = JSONObject().apply {
                put("coins", stats.coins)
                put("level", stats.level)
                put("xp", stats.xp)
                put("syncTimestamp", System.currentTimeMillis())
                put("catcherPlayed", stats.catcherPlayed)
                put("memoryPlayed", stats.memoryPlayed)
                put("tictactoePlayed", stats.tictactoePlayed)
                put("wordsearchPlayed", stats.wordsearchPlayed)
                put("achievementsClaimed", stats.achievementsClaimed)
                put("linkCode", linkCode)
                put("myName", stats.myName)
                put("myAvatar", stats.myAvatar)
                put("isLinked", stats.isLinked)
                put("partnerName", stats.partnerName ?: "Compañero")
                put("partnerAvatar", stats.partnerAvatar ?: "👤")
            }
            payload.put("stats", statsJson)

            // 3. Serialize Logs
            val logsArray = JSONArray()
            logs.take(15).forEach { log ->
                val logJson = JSONObject().apply {
                    val finalName = if (log.actorName == "Tú" || log.actorName == "local_player") stats.myName else log.actorName
                    put("actorName", finalName)
                    put("actionType", log.actionType)
                    put("actionDetails", log.actionDetails)
                    put("timestamp", log.timestamp)
                }
                logsArray.put(logJson)
            }
            payload.put("logs", logsArray)

            // 4. Serialize Inventory
            val invArray = JSONArray()
            inventory.forEach { inv ->
                if (inv.quantity > 0 || inv.itemId.startsWith("HAT_")) {
                    val invJson = JSONObject().apply {
                        put("itemId", inv.itemId)
                        put("category", inv.category)
                        put("name", inv.name)
                        put("cost", inv.cost)
                        put("quantity", inv.quantity)
                        put("isUnlocked", inv.isUnlocked)
                        put("valueBoost", inv.valueBoost.toDouble())
                    }
                    invArray.put(invJson)
                }
            }
            payload.put("inventory", invArray)

            if (context != null) {
                val uid = getFirebaseUid(context)
                payload.put("dueñoUid", uid)
                logs.firstOrNull()?.let { latestLog ->
                    val actionJson = JSONObject().apply {
                        put("uid", uid)
                        put("tipo", latestLog.actionType)
                        put("timestamp", latestLog.timestamp)
                    }
                    payload.put("ultimaAccion", actionJson)
                }
                RealtimeSyncManager.pushUpdate(linkCode, payload, context)
                if (linkCode != token) {
                    RealtimeSyncManager.pushUpdate(token, payload, context)
                }
            }

            if (isFirebaseEnabled && context != null) {
                // Con Firebase RTDB activo, el push por WebSocket es instantáneo y definitivo.
                // Evitamos peticiones HTTP REST lentas que compiten con el socket en tiempo real.
                onResult(true)
                return
            }

            val url = if (isFirebaseEnabled) {
                "${firebaseDbUrl.trimEnd('/')}/pets/$token.json"
            } else {
                "$BASE_URL/$token/gameState"
            }

            val request = Request.Builder()
                .url(url)
                .put(payload.toString().toRequestBody(JSON))
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("CoMascotaSyncService", "Failed to upload co-care sync", e)
                    onResult(false)
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (!response.isSuccessful) {
                        Log.e("CoMascotaSyncService", "Upload failed: ${response.code} - ${response.body?.string()}")
                    } else {
                        Log.d("CoMascotaSyncService", "Upload successful: ${response.code}")
                    }
                    onResult(response.isSuccessful)
                }
            })
        } catch (e: Exception) {
            Log.e("CoMascotaSyncService", "Sychronization error", e)
            onResult(false)
        }
    }

    /**
     * Fetches the entire game state from the key-value store.
     */
    fun fetchState(token: String, onResult: (FetchedState?) -> Unit) {
        try {
            // If the user pastes a full link instead of a code, extract the token
            val actualToken = (extractTokenFromUrl(token) ?: token).trim()

            val url = if (isFirebaseEnabled) {
                "${firebaseDbUrl.trimEnd('/')}/pets/$actualToken.json"
            } else {
                "$BASE_URL/$actualToken/gameState"
            }

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("CoMascotaSyncService", "Failed to fetch state for token $actualToken", e)
                    onResult(generateMockState(actualToken))
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (!response.isSuccessful) {
                        onResult(generateMockState(actualToken))
                        return
                    }
                    val rawBody = response.body?.string() ?: ""
                    try {
                        val payload = JSONObject(rawBody)
                        val parsed = parsePayloadToFetchedState(payload)
                        if (parsed != null) {
                            onResult(parsed)
                        } else {
                            onResult(generateMockState(actualToken))
                        }
                    } catch (e: Exception) {
                        Log.e("CoMascotaSyncService", "Error parsing game state", e)
                        onResult(null)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("CoMascotaSyncService", "Error creating fetch request", e)
            onResult(null)
        }
    }

    fun parsePayloadToFetchedState(payload: JSONObject): FetchedState? {
        return try {
            if (!payload.has("pet") || !payload.has("stats")) return null
            val petJson = payload.getJSONObject("pet")
            val estadoActualObj = payload.optJSONObject("estadoActual")
            val remoteTipo = estadoActualObj?.optString("tipo") ?: if (petJson.optBoolean("isSleeping", false)) "SLEEP" else "IDLE"
            val remoteFinEst = estadoActualObj?.optLong("finEstimado", 0L) ?: petJson.optLong("finEstimado", 0L)
            val now = System.currentTimeMillis()
            val isSleepingRemote = (remoteTipo == "SLEEP") && (remoteFinEst <= 0L || now <= remoteFinEst)
            val remoteDormirInicio = petJson.optLong("dormirInicio", if (isSleepingRemote) petJson.optLong("lastInteraction", now) else 0L)

            val pet = PetEntity(
                name = petJson.getString("name"),
                type = petJson.getString("type"),
                hunger = petJson.getDouble("hunger").toFloat(),
                energy = petJson.getDouble("energy").toFloat(),
                happiness = petJson.getDouble("happiness").toFloat(),
                cleanliness = petJson.getDouble("cleanliness").toFloat(),
                createdAt = petJson.optLong("createdAt", System.currentTimeMillis()),
                lastInteraction = petJson.optLong("lastInteraction", System.currentTimeMillis()),
                equippedHat = if (petJson.isNull("equippedHat")) null else petJson.getString("equippedHat"),
                equippedAccessory = if (petJson.isNull("equippedAccessory")) null else petJson.getString("equippedAccessory"),
                isSleeping = isSleepingRemote,
                finEstimado = remoteFinEst,
                dormirInicio = remoteDormirInicio
            )

            val statsJson = payload.getJSONObject("stats")
            val coins = statsJson.getInt("coins")
            val level = statsJson.getInt("level")
            val xp = statsJson.getInt("xp")
            val syncTimestamp = statsJson.optLong("syncTimestamp", System.currentTimeMillis())
            val catcherPlayed = statsJson.optInt("catcherPlayed", 0)
            val memoryPlayed = statsJson.optInt("memoryPlayed", 0)
            val tictactoePlayed = statsJson.optInt("tictactoePlayed", 0)
            val wordsearchPlayed = statsJson.optInt("wordsearchPlayed", 0)
            val achievementsClaimed = statsJson.optString("achievementsClaimed", "")
            val partnerName = statsJson.optString("partnerName", statsJson.optString("myName", "Compañero"))
            val partnerAvatar = statsJson.optString("partnerAvatar", statsJson.optString("myAvatar", "👤"))
            val colUid = payload.optString("colaboradorUid", "")
            val isLinkedRemote = statsJson.optBoolean("isLinked", false) || colUid.isNotBlank()

            val logsArray = payload.optJSONArray("logs")
            val logs = mutableListOf<ActivityLogEntity>()
            if (logsArray != null) {
                for (i in 0 until logsArray.length()) {
                    val logJson = logsArray.getJSONObject(i)
                    logs.add(
                        ActivityLogEntity(
                            petId = 1,
                            actorName = logJson.getString("actorName"),
                            actionType = logJson.getString("actionType"),
                            actionDetails = logJson.getString("actionDetails"),
                            timestamp = logJson.getLong("timestamp")
                        )
                    )
                }
            }

            val invArray = payload.optJSONArray("inventory")
            val inventory = mutableListOf<InventoryEntity>()
            if (invArray != null) {
                for (i in 0 until invArray.length()) {
                    val invJson = invArray.getJSONObject(i)
                    inventory.add(
                        InventoryEntity(
                            itemId = invJson.getString("itemId"),
                            category = invJson.getString("category"),
                            name = invJson.getString("name"),
                            cost = invJson.getInt("cost"),
                            quantity = invJson.getInt("quantity"),
                            isUnlocked = invJson.optBoolean("isUnlocked", false),
                            valueBoost = invJson.optDouble("valueBoost", 20.0).toFloat()
                        )
                    )
                }
            }

            FetchedState(
                pet = pet,
                coins = coins,
                level = level,
                xp = xp,
                syncTimestamp = syncTimestamp,
                catcherPlayed = catcherPlayed,
                memoryPlayed = memoryPlayed,
                tictactoePlayed = tictactoePlayed,
                wordsearchPlayed = wordsearchPlayed,
                achievementsClaimed = achievementsClaimed,
                logs = logs,
                inventory = inventory,
                partnerName = partnerName,
                partnerAvatar = partnerAvatar,
                isLinked = isLinkedRemote
            )
        } catch (e: Exception) {
            Log.e("CoMascotaSyncService", "Error parsing game state JSON", e)
            null
        }
    }

    private fun extractTokenFromUrl(url: String): String? {
        // Also handle firebase urls if pasted
        if (url.contains("firebaseio.com")) {
            val fbRegex = Regex("pets/([a-zA-Z0-9_-]+)\\.json")
            val fbMatch = fbRegex.find(url)
            if (fbMatch != null) return fbMatch.groupValues[1]
        }
        val parts = url.split("/")
        if (parts.size >= 5) {
            val token = parts[parts.size - 2]
            if (token.isNotBlank() && token != "api.keyvalue.xyz" && token != "new") {
                return token
            }
        }
        val regex = Regex("keyvalue\\.xyz/([a-zA-Z0-9_-]+)/")
        val match = regex.find(url)
        return match?.groupValues?.get(1)
    }

    private fun generateMockState(token: String): FetchedState {
        // Generates a mock pet so the user can test the app without a real server or code.
        return FetchedState(
            pet = PetEntity(
                name = "Mascota de $token",
                type = "SHIBA",
                hunger = 80f,
                energy = 80f,
                happiness = 80f,
                cleanliness = 80f
            ),
            coins = 150,
            level = 2,
            xp = 120,
            syncTimestamp = System.currentTimeMillis(),
            catcherPlayed = 2,
            memoryPlayed = 1,
            tictactoePlayed = 0,
            wordsearchPlayed = 0,
            achievementsClaimed = "",
            logs = listOf(
                ActivityLogEntity(
                    petId = 1,
                    actorName = "Compañero",
                    actionType = "FEED",
                    actionDetails = "¡Le dio de comer un pastel!",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2 // 2 hours ago
                )
            ),
            inventory = emptyList(),
            partnerName = "Compañero",
            partnerAvatar = "👤",
            isLinked = true
        )
    }

    data class FetchedState(
        val pet: PetEntity,
        val coins: Int,
        val level: Int,
        val xp: Int,
        val syncTimestamp: Long,
        val catcherPlayed: Int,
        val memoryPlayed: Int,
        val tictactoePlayed: Int,
        val wordsearchPlayed: Int,
        val achievementsClaimed: String,
        val logs: List<ActivityLogEntity>,
        val inventory: List<InventoryEntity>,
        val partnerName: String?,
        val partnerAvatar: String?,
        val isLinked: Boolean = false
    )

    /**
     * Sends a chat prompt to Gemini API to talk with your pet virtual mascot.
     */
    fun chatWithMascota(
        prompt: String,
        pet: PetEntity,
        level: Int,
        onResult: (String) -> Unit
    ) {
        val apiKey = geminiApiKey
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            onResult("⚠️ Configuración pendiente:\nPara que tu mascota te hable con Inteligencia Artificial, agrega tu API Key en los Secretos de AI Studio con el nombre 'GEMINI_API_KEY' o 'key'.")
            return
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        
        val petTypeDescriptor = when (pet.type) {
            "SHIBA" -> "un adorable perrito Shiba Inu"
            "SLIME" -> "un juguetón Slime mágico y gelatinoso"
            "KITTY" -> "un tierno gatito juguetón"
            "DRACO" -> "un pequeño y travieso dragón de fuego"
            "AXOLOTL" -> "un adorable axolotl cósmico de las estrellas"
            else -> "una linda mascota virtual"
        }

        val sleepStatus = if (pet.isSleeping) "Estás durmiendo plácidamente." else "Estás despierto y con ganas de interactuar."

        val systemContext = "Eres ${pet.name}, $petTypeDescriptor. " +
                "Tus estadísticas actuales son: Nivel $level, Hambre ${pet.hunger.toInt()}/100, " +
                "Energía ${pet.energy.toInt()}/100, Felicidad ${pet.happiness.toInt()}/100, " +
                "Limpieza ${pet.cleanliness.toInt()}/100. $sleepStatus " +
                "Responde al jugador de manera extremadamente cariñosa, corta (máximo 3 frases), juguetona y tierna en español. Usa emojis tiernos. " +
                "Por favor, actúa exactamente como tu tipo de mascota (por ejemplo, si eres perrito di '¡Guau!', si eres gatito di '¡Miau!', si eres dragón haz ruidos de fueguito '¡Fshhh!', si eres axolotl di '¡Bloop, glu!')."

        val fullPrompt = "$systemContext\n\nEl jugador te dice: \"$prompt\"\nTu respuesta:"

        try {
            val requestBodyJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", fullPrompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(JSON))
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("CoMascotaSyncService", "Gemini API call failed", e)
                    onResult("❤️ *${pet.name} bosteza y te mira con cariño, parece que la red de telepatía está durmiendo.* (Error de conexión: ${e.message})")
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val bodyText = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        Log.e("CoMascotaSyncService", "Gemini API response error: ${response.code} - $bodyText")
                        if (response.code == 400 || response.code == 403) {
                            onResult("⚠️ Error de API Key:\nLa API Key configurada para Gemini es inválida o no tiene permisos. Verifica que sea correcta en los Secretos de AI Studio con el nombre 'GEMINI_API_KEY' o 'key'.")
                        } else {
                            onResult("❤️ *${pet.name} inclina la cabeza confundido y da una vueltita.* (Error de API: código ${response.code})")
                        }
                        return
                    }

                    try {
                        val responseJson = JSONObject(bodyText)
                        val candidates = responseJson.optJSONArray("candidates")
                        if (candidates == null || candidates.length() == 0) {
                            val promptFeedback = responseJson.optJSONObject("promptFeedback")
                            if (promptFeedback != null) {
                                val blockReason = promptFeedback.optString("blockReason", "")
                                if (blockReason.isNotBlank()) {
                                    onResult("⚠️ La respuesta fue bloqueada por políticas de seguridad de Gemini (Motivo: $blockReason).")
                                    return
                                }
                            }
                            val snippet = if (bodyText.length > 100) bodyText.substring(0, 100) + "..." else bodyText
                            onResult("❤️ *${pet.name} bosteza.* No se recibió respuesta. (JSON: $snippet)")
                            return
                        }
                        
                        val firstCandidate = candidates.getJSONObject(0)
                        val finishReason = firstCandidate.optString("finishReason", "")
                        if (finishReason == "SAFETY") {
                            onResult("⚠️ La respuesta de ${pet.name} fue bloqueada por filtros de seguridad (SAFETY).")
                            return
                        }
                        
                        val content = firstCandidate.optJSONObject("content")
                        if (content == null) {
                            onResult("❤️ *${pet.name} hace un gesto de confusión.* No se generó contenido (Motivo: $finishReason).")
                            return
                        }
                        
                        val parts = content.optJSONArray("parts")
                        if (parts == null || parts.length() == 0) {
                            onResult("❤️ *${pet.name} te mira en silencio.* (Respuesta vacía)")
                            return
                        }
                        
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isBlank()) {
                            onResult("❤️ *${pet.name} parpadea tiernamente.* (Respuesta vacía)")
                        } else {
                            onResult(text.trim())
                        }
                    } catch (e: Exception) {
                        Log.e("CoMascotaSyncService", "Failed to parse Gemini response JSON", e)
                        val snippet = if (bodyText.length > 150) bodyText.substring(0, 150) + "..." else bodyText
                        onResult("❤️ *${pet.name} hace un sonidito alegre pero no logras entenderlo.* (Error de procesamiento: ${e.message}. Body: $snippet)")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("CoMascotaSyncService", "Error constructing Gemini request", e)
            onResult("❤️ *${pet.name} te sonríe con ternura.*")
        }
    }
}
