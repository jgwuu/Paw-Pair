package com.jgwuu.pawpair.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    // --- Pets ---
    @Query("SELECT * FROM pets ORDER BY id DESC")
    fun getAllPetsFlow(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getPetById(petId: Int): PetEntity?

    @Query("SELECT * FROM pets ORDER BY id DESC LIMIT 1")
    suspend fun getLatestPet(): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity): Long

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Delete
    suspend fun deletePet(pet: PetEntity)

    // --- User Stats ---
    @Query("SELECT * FROM user_stats WHERE userId = 'local_player' LIMIT 1")
    fun getUserStatsFlow(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE userId = 'local_player' LIMIT 1")
    suspend fun getUserStats(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStatsEntity)

    @Update
    suspend fun updateUserStats(stats: UserStatsEntity)

    // --- Inventory ---
    @Query("SELECT * FROM inventory")
    fun getInventoryFlow(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory WHERE itemId = :itemId LIMIT 1")
    suspend fun getInventoryItem(itemId: String): InventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInventory(items: List<InventoryEntity>)

    @Update
    suspend fun updateInventoryItem(item: InventoryEntity)

    // --- Activity Logs ---
    @Query("SELECT * FROM activity_logs WHERE petId = :petId ORDER BY timestamp DESC LIMIT 30")
    fun getActivityLogsFlow(petId: Int): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs WHERE petId = :petId")
    suspend fun clearLogsForPet(petId: Int)
}
