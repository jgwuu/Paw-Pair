package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.pow

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "SHIBA", "SLIME", "KITTY", "DRACO", "AXOLOTL"
    val hunger: Float = 80f, // 0 to 100
    val energy: Float = 80f, // 0 to 100
    val happiness: Float = 80f, // 0 to 100
    val cleanliness: Float = 80f, // 0 to 100
    val createdAt: Long = System.currentTimeMillis(),
    val lastInteraction: Long = System.currentTimeMillis(),
    val equippedHat: String? = null, // "CROWN", "TOP_HAT", "SUNGLASSES", "BOWTIE", "NONE"
    val equippedAccessory: String? = null,
    val isSleeping: Boolean = false,
    val finEstimado: Long = 0L,
    val dormirInicio: Long = 0L
) {
    fun calculateRealtimeEnergy(now: Long = System.currentTimeMillis()): Float {
        if (!isSleeping || dormirInicio <= 0L) return energy
        val elapsedMinutes = ((now - dormirInicio) / 60000f).coerceAtLeast(0f)
        return (energy + elapsedMinutes * 2.0f).coerceIn(0f, 100f)
    }
}

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val userId: String = "local_player",
    val coins: Int = 500,
    val level: Int = 1,
    val xp: Int = 0,
    val myName: String = "Cuidador",
    val myAvatar: String = "👤",
    // Co-care sharing properties
    val isLinked: Boolean = false,
    val partnerName: String? = null,
    val partnerAvatar: String? = null,
    val linkCode: String? = null, // e.g. "CO-PET-9812"
    val linkRole: String? = null, // "dueño" or "compañero"
    val syncTimestamp: Long = System.currentTimeMillis(),
    
    // Shared achievements counters
    val catcherPlayed: Int = 0,
    val memoryPlayed: Int = 0,
    val tictactoePlayed: Int = 0,
    val wordsearchPlayed: Int = 0,
    val achievementsClaimed: String = "", // comma separated claimed achievement IDs
    
    // Theme & Notifications settings
    val appTheme: String = "FOREST", // "FOREST", "SUNSET", "OCEAN", "CANDY", "CYBER"
    val notificationsEnabled: Boolean = true,
    val routineHour: Int = 20, // 8 PM default routine notification
    
    // New 5 minigames counters
    val simonPlayed: Int = 0,
    val flappyPlayed: Int = 0,
    val quizPlayed: Int = 0,
    val bubblePlayed: Int = 0,
    val mathPlayed: Int = 0
)

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey val itemId: String, // "FOOD_APPLE", "FOOD_SUSHI", "FOOD_PIZZA", "POTION_HEALTH", "HAT_CROWN", etc.
    val category: String, // "FOOD", "POTION", "ACCESSORY"
    val name: String,
    val cost: Int,
    val quantity: Int = 0,
    val isUnlocked: Boolean = false,
    val valueBoost: Float = 20f // how much stat it restores
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val petId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val actorName: String, // "Tú" or Partner Name
    val actionType: String, // "FEED", "PLAY", "BATH", "SLEEP", "BUY", "LINK"
    val actionDetails: String
)

object GameRules {
    fun getRequiredXp(level: Int): Int = (100 * level.toDouble().pow(1.3)).toInt()
}
