package com.jgwuu.pawpair.data

import android.content.Context
import android.util.Log
import com.jgwuu.pawpair.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseManager {
    private var initialized = false
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        if (initialized) return
        val url = BuildConfig.FIREBASE_DATABASE_URL.trim()
        if (url.isBlank() || url.contains("tu-proyecto")) {
            return
        }

        try {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:123456789012:android:abcdef1234567890")
                .setApiKey("fake-api-key-for-rtdb-only")
                .setDatabaseUrl(url)
                .build()

            FirebaseApp.initializeApp(context, options, "co-mascota-app")
            initialized = true
            ensureAuth()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ID persistente al inicio: Al abrir la app, revisa FirebaseAuth.getInstance().currentUser.
     * Si es null, recién ahí llama signInAnonymously() y guarda el UID en SharedPreferences.
     * En cada inicio posterior, reutiliza ese mismo UID sin volver a autenticar.
     * Nunca reiniciar sesión: No se ejecuta signOut ni se limpian SharedPreferences al cambiar versión.
     */
    fun ensureAuth(onReady: (String) -> Unit = {}) {
        val prefs = appContext?.getSharedPreferences("co_mascota_auth", Context.MODE_PRIVATE)
        try {
            val auth = FirebaseAuth.getInstance()
            val current = auth.currentUser
            if (current != null) {
                prefs?.edit()?.putString("user_uid", current.uid)?.apply()
                onReady(current.uid)
            } else {
                // Revisa primero SharedPreferences en arranques posteriores
                val savedUid = prefs?.getString("user_uid", null)
                if (!savedUid.isNullOrBlank()) {
                    Log.d("FirebaseManager", "Reutilizando UID persistente desde SharedPreferences: $savedUid")
                    onReady(savedUid)
                    return
                }

                // Si es null en ambos (primer inicio), llama signInAnonymously y guarda el UID
                auth.signInAnonymously().addOnSuccessListener { res ->
                    val uid = res.user?.uid ?: "anon_default"
                    prefs?.edit()?.putString("user_uid", uid)?.apply()
                    Log.d("FirebaseManager", "Sesión creada y guardada: $uid")
                    onReady(uid)
                }.addOnFailureListener {
                    val fallback = "usr_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12)
                    prefs?.edit()?.putString("user_uid", fallback)?.apply()
                    onReady(fallback)
                }
            }
        } catch (e: Exception) {
            val savedUid = prefs?.getString("user_uid", null)
            if (!savedUid.isNullOrBlank()) {
                onReady(savedUid)
            } else {
                val fallback = "usr_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12)
                prefs?.edit()?.putString("user_uid", fallback)?.apply()
                onReady(fallback)
            }
        }
    }

    fun getCurrentUserId(): String? {
        return try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (!uid.isNullOrBlank()) return uid
            appContext?.getSharedPreferences("co_mascota_auth", Context.MODE_PRIVATE)?.getString("user_uid", null)
        } catch (e: Exception) {
            appContext?.getSharedPreferences("co_mascota_auth", Context.MODE_PRIVATE)?.getString("user_uid", null)
        }
    }

    fun getDatabase(): FirebaseDatabase? {
        if (!initialized) return null
        return try {
            val app = FirebaseApp.getInstance("co-mascota-app")
            FirebaseDatabase.getInstance(app)
        } catch (e: Exception) {
            null
        }
    }
}
