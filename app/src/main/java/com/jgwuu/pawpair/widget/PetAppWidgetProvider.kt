package com.jgwuu.pawpair.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.jgwuu.pawpair.R
import com.jgwuu.pawpair.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PetAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val pet = db.petDao().getLatestPet()
            
            withContext(Dispatchers.Main) {
                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.pet_app_widget)
                    
                    if (pet != null) {
                        val petEmoji = when (pet.type) {
                            "SHIBA" -> "🐶"
                            "SLIME" -> "💧"
                            "KITTY" -> "🐱"
                            "DRACO" -> "🐉"
                            "AXOLOTL" -> "🌸"
                            else -> "🐾"
                        }
                        views.setTextViewText(R.id.widget_pet_name, pet.name)
                        views.setTextViewText(R.id.widget_pet_avatar, petEmoji)
                        
                        val statusText = if (pet.isSleeping) "Durmiendo 💤" else "Feliz ✨"
                        views.setTextViewText(R.id.widget_pet_status, statusText)
                        
                        val dialogueText = when {
                            pet.isSleeping -> "💤 Zzz... ¡Duermo!"
                            pet.hunger < 40f -> "😋 ¡Tengo hambre! Dame Arepa..."
                            pet.happiness < 40f -> "🥺 ¡Hagamos sopa de letras!"
                            else -> {
                                val quotes = listOf(
                                    "💬 ¡Qué lindo día costeño!",
                                    "👀 ¿Cocinamos salchipapa?",
                                    "🌟 ¡Te quiero mucho!",
                                    "🎮 ¡Vamos a jugar!",
                                    "🌴 Qué brisa tan sabrosa..."
                                )
                                quotes[kotlin.math.abs((pet.name.hashCode() + (System.currentTimeMillis() / 60000).toInt()) % quotes.size)]
                            }
                        }
                        views.setTextViewText(R.id.widget_pet_bubble, dialogueText)
                        
                        views.setProgressBar(R.id.widget_hunger_progress, 100, pet.hunger.toInt(), false)
                        views.setProgressBar(R.id.widget_happiness_progress, 100, pet.happiness.toInt(), false)
                        views.setProgressBar(R.id.widget_energy_progress, 100, pet.energy.toInt(), false)
                        views.setProgressBar(R.id.widget_cleanliness_progress, 100, pet.cleanliness.toInt(), false)
                        
                        val userStats = db.petDao().getUserStats()
                        if (userStats != null) {
                            views.setTextViewText(R.id.widget_level, "Lvl ${userStats.level}")
                            views.setTextViewText(R.id.widget_coins, "🪙 ${userStats.coins}")
                        } else {
                            views.setTextViewText(R.id.widget_level, "Lvl 1")
                            views.setTextViewText(R.id.widget_coins, "🪙 0")
                        }
                    } else {
                        views.setTextViewText(R.id.widget_pet_name, "PawPair")
                        views.setTextViewText(R.id.widget_pet_avatar, "🐾")
                        views.setTextViewText(R.id.widget_pet_status, "Inactivo")
                        views.setTextViewText(R.id.widget_pet_bubble, "¡Hola! Crea tu mascota")
                        views.setProgressBar(R.id.widget_hunger_progress, 100, 0, false)
                        views.setProgressBar(R.id.widget_happiness_progress, 100, 0, false)
                        views.setProgressBar(R.id.widget_energy_progress, 100, 0, false)
                        views.setProgressBar(R.id.widget_cleanliness_progress, 100, 0, false)
                        views.setTextViewText(R.id.widget_level, "Lvl -")
                        views.setTextViewText(R.id.widget_coins, "🪙 0")
                    }
                    
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, PetAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = android.content.Intent(context, PetAppWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
