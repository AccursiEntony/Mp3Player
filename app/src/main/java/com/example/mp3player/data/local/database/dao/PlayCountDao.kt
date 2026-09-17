package com.example.mp3player.data.local.database.dao

import androidx.room.*
import com.example.mp3player.data.local.database.entity.PlayCountEntity
import kotlinx.coroutines.flow.Flow

/** Gestione dei conteggi di ascolto dei brani nel database Room. (DAO = Data Access Object)*/
@Dao
interface PlayCountDao {

    // Osserva tutti i brani ascoltati almeno una volta, dal più ascoltato al meno ascoltato
    @Query("SELECT * FROM play_counts ORDER BY count DESC, lastPlayedAt DESC")
    fun observeAll(): Flow<List<PlayCountEntity>>

    // Incrementa il contatore di un brano già presente. Ritorna le righe modificate (0 se non esiste ancora)
    @Query("UPDATE play_counts SET count = count + 1, lastPlayedAt = :timestamp WHERE id = :id")
    suspend fun increment(id: Long, timestamp: Long): Int

    @Query("DELETE FROM play_counts")
    suspend fun clearAll()

    // Crea la riga la prima volta che un brano viene ascoltato
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entity: PlayCountEntity)

    // Registra un ascolto: incrementa se il brano è già presente, altrimenti lo crea con count = 1
    @Transaction
    suspend fun registerPlay(entity: PlayCountEntity, timestamp: Long) {
        val rowsUpdated = increment(entity.id, timestamp)
        if (rowsUpdated == 0) {
            insertIfAbsent(entity.copy(count = 1, lastPlayedAt = timestamp))
        }
    }
}