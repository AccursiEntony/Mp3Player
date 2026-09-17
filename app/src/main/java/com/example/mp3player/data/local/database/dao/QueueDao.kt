package com.example.mp3player.data.local.database.dao

import androidx.room.*
import com.example.mp3player.data.local.database.entity.QueueItemEntity
import kotlinx.coroutines.flow.Flow

/** Gestione della coda di riproduzione musicale nel database Room. (DAO = Data Access Object)*/
@Dao
interface QueueDao {

    // Osserva i brani in coda in tempo reale, ordinati per posizione
    @Query("SELECT * FROM queue ORDER BY position ASC")
    fun observeAll(): Flow<List<QueueItemEntity>>

    // Recupera la lista statica di tutti i brani in coda
    @Query("SELECT * FROM queue ORDER BY position ASC")
    suspend fun getAll(): List<QueueItemEntity>

    // Inserisce o aggiorna un intero elenco di brani nella coda
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<QueueItemEntity>)

    // Svuota completamente la coda di riproduzione
    @Query("DELETE FROM queue")
    suspend fun clearAll()
}
