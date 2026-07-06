package com.example.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class ActionPayload(val uid: String, val tipo: String, val timestamp: Long)

object RealtimeSyncManager {
    private var activePetCode: String? = null
    private var petListener: ValueEventListener? = null
    
    private var lastProcessedActionTimestamp: Long = 0L

    private val _remoteState = MutableStateFlow<CoMascotaSyncService.FetchedState?>(null)
    val remoteState = _remoteState.asStateFlow()

    private val _latestAction = MutableStateFlow<ActionPayload?>(null)
    val latestAction = _latestAction.asStateFlow()

    /**
     * Listener idéntico para ambos roles (Dueño y Compañero).
     * Escucha exclusivamente sobre /pets/{code} con addValueEventListener + keepSynced(true).
     */
    fun startListening(code: String, onUpdate: (CoMascotaSyncService.FetchedState) -> Unit = {}) {
        val db = FirebaseManager.getDatabase() ?: return
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) return
        
        if (activePetCode == cleanCode && petListener != null) return
        stopListening()
        
        activePetCode = cleanCode
        val petRef = db.getReference("pets/$cleanCode")

        try {
            petRef.keepSynced(true)
        } catch (e: Exception) {
            Log.e("RealtimeSyncManager", "keepSynced error", e)
        }
        
        var isFirstSnapshot = true
        petListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!snapshot.exists() || snapshot.value == null) return
                    val rawJson = snapshot.value
                    if (rawJson is Map<*, *>) {
                        val json = JSONObject(rawJson)
                        val state = CoMascotaSyncService.parsePayloadToFetchedState(json)
                        if (state != null) {
                            _remoteState.value = state
                            onUpdate(state)
                        }
                        
                        val actionNode = rawJson["ultimaAccion"] as? Map<*, *>
                        if (actionNode != null) {
                            val uid = actionNode["uid"] as? String ?: ""
                            val tipo = actionNode["tipo"] as? String ?: ""
                            val timestamp = (actionNode["timestamp"] as? Number)?.toLong() ?: 0L
                            if (uid.isNotEmpty() && tipo.isNotEmpty() && timestamp > 0L) {
                                if (isFirstSnapshot) {
                                    // Ignorar el primer onDataChange para evitar acciones fantasma al conectar
                                    lastProcessedActionTimestamp = Math.max(lastProcessedActionTimestamp, timestamp)
                                } else if (timestamp > lastProcessedActionTimestamp) {
                                    lastProcessedActionTimestamp = timestamp
                                    _latestAction.value = ActionPayload(uid, tipo, timestamp)
                                }
                            }
                        }
                    }
                    if (isFirstSnapshot) {
                        isFirstSnapshot = false
                    }
                } catch (e: Exception) {
                    Log.e("RealtimeSyncManager", "Error parsing real-time pet data", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RealtimeSyncManager", "Real-time sync cancelled: ${error.message}")
            }
        }

        petRef.addValueEventListener(petListener!!)
    }
    
    /**
     * Remueve el listener al salir de la pantalla para evitar duplicados o acciones perdidas.
     */
    fun stopListening() {
        if (activePetCode != null) {
            val db = FirebaseManager.getDatabase()
            if (db != null && petListener != null) {
                db.getReference("pets/$activePetCode").removeEventListener(petListener!!)
            }
            activePetCode = null
            petListener = null
        }
    }

    fun fetchSingleState(code: String, onResult: (CoMascotaSyncService.FetchedState?) -> Unit) {
        val db = FirebaseManager.getDatabase()
        val cleanCode = code.trim().uppercase()
        if (db == null || cleanCode.isBlank()) {
            onResult(null)
            return
        }
        val petRef = db.getReference("pets/$cleanCode")
        petRef.get().addOnSuccessListener { snapshot ->
            try {
                if (!snapshot.exists() || snapshot.value == null) {
                    onResult(null)
                    return@addOnSuccessListener
                }
                val rawJson = snapshot.value
                if (rawJson is Map<*, *>) {
                    val json = JSONObject(rawJson)
                    val state = CoMascotaSyncService.parsePayloadToFetchedState(json)
                    onResult(state)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                Log.e("RealtimeSyncManager", "fetchSingleState error", e)
                onResult(null)
            }
        }.addOnFailureListener {
            onResult(null)
        }
    }

    /**
     * Unirse con código respetando reglas de seguridad de Firebase RTDB y el orden de vinculación:
     * 1. Consulta pública de validación en /codigos/{codigo} si existe.
     * 2. PRIMERO agrega el UID a colaboradorUid.
     * 3. SOLO DESPUÉS de que colaboradorUid se registra, lee el resto de los datos (/pets/{id}).
     */
    fun joinPetAndFetch(code: String, myUid: String, onFinished: (Boolean, CoMascotaSyncService.FetchedState?) -> Unit) {
        val db = FirebaseManager.getDatabase()
        if (db == null) {
            onFinished(false, null)
            return
        }
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            onFinished(false, null)
            return
        }

        // 1. Validamos código en índice público de solo lectura /codigos/{codigo}
        val codigosRef = db.getReference("codigos/$cleanCode")
        codigosRef.get().addOnCompleteListener { codigosTask ->
            var targetPetId = cleanCode
            if (codigosTask.isSuccessful && codigosTask.result != null && codigosTask.result.exists()) {
                val valObj = codigosTask.result.value
                if (valObj is Map<*, *>) {
                    val idObj = valObj["idMascota"] as? String
                    if (!idObj.isNullOrBlank()) targetPetId = idObj
                } else if (valObj is String && valObj.isNotBlank()) {
                    targetPetId = valObj
                }
            }

            // 2. ORDEN CORRECTO AL VINCULAR: Primero agregamos el UID a colaboradorUid
            val petRef = db.getReference("pets/$targetPetId")
            petRef.child("colaboradorUid").setValue(myUid).addOnCompleteListener { setTask ->
                if (!setTask.isSuccessful) {
                    onFinished(false, null)
                    return@addOnCompleteListener
                }

                // 3. Solo después de registrar colaboradorUid, cargamos el resto de los datos de la mascota
                petRef.get().addOnCompleteListener { getTask ->
                    if (getTask.isSuccessful && getTask.result != null && getTask.result.exists()) {
                        val rawJson = getTask.result.value
                        if (rawJson is Map<*, *>) {
                            try {
                                val json = JSONObject(rawJson)
                                val state = CoMascotaSyncService.parsePayloadToFetchedState(json)
                                if (state != null) {
                                    _remoteState.value = state
                                    onFinished(true, state)
                                    return@addOnCompleteListener
                                }
                            } catch (e: Exception) {
                                Log.e("RealtimeSyncManager", "Error parsing pet state after link", e)
                            }
                        }
                    }
                    // Si no existía o falló la lectura de datos, limpiamos la vinculación fallida
                    petRef.child("colaboradorUid").removeValue()
                    onFinished(false, null)
                }
            }
        }
    }
    
    fun pushUpdate(code: String, payload: JSONObject, context: android.content.Context? = null) {
        val db = FirebaseManager.getDatabase() ?: return
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) return
        val ref = db.getReference("pets/$cleanCode")
        try {
            val updatesMap = flattenMapFromJsonObject(payload)
            ref.updateChildren(updatesMap).addOnFailureListener { error ->
                Log.e("RealtimeSyncManager", "Error updating pet data: ${error.message}")
            }

            // Confirmamos nodo en /codigos/{codigo} de solo lectura pública para validación del código
            val codigosRef = db.getReference("codigos/$cleanCode")
            codigosRef.updateChildren(mapOf("idMascota" to cleanCode, "codigoVinculacion" to cleanCode))
        } catch (e: Exception) {
            Log.e("RealtimeSyncManager", "Error pushing update", e)
        }
    }

    private fun jsonArrayToList(array: org.json.JSONArray): List<Any> {
        val list = mutableListOf<Any>()
        for (i in 0 until array.length()) {
            val value = array.get(i)
            if (value is org.json.JSONArray) {
                list.add(jsonArrayToList(value))
            } else if (value is JSONObject) {
                list.add(flattenMapFromJsonObject(value))
            } else {
                list.add(value)
            }
        }
        return list
    }

    private fun flattenMapFromJsonObject(json: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            if (value is org.json.JSONArray) {
                map[key] = jsonArrayToList(value)
            } else if (value is JSONObject) {
                map[key] = flattenMapFromJsonObject(value)
            } else {
                map[key] = value
            }
        }
        return map
    }
}
