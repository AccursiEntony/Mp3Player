package com.example.mp3player.data.local.database.dao

import androidx.room.*
import com.example.mp3player.data.local.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

/** Gestione dei brani preferiti nel database Room. (DAO = Data Access Object)*/
@Dao
interface FavoriteDao {

    // Osserva la lista dei preferiti in tempo reale, ordinata per titolo
    @Query("SELECT * FROM favorites ORDER BY title ASC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    // Recupera la lista statica di tutti i preferiti (una sola volta)
    @Query("SELECT * FROM favorites")
    suspend fun getAll(): List<FavoriteEntity>

    // Inserisce o aggiorna un brano nei preferiti
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    // Rimuove un brano dai preferiti tramite il suo ID unico
    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteById(id: Long)
}
