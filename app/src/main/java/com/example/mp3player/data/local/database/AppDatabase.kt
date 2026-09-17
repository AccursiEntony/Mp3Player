package com.example.mp3player.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mp3player.data.local.database.dao.FavoriteDao
import com.example.mp3player.data.local.database.dao.QueueDao
import com.example.mp3player.data.local.database.dao.PlayCountDao
import com.example.mp3player.data.local.database.entity.FavoriteEntity
import com.example.mp3player.data.local.database.entity.QueueItemEntity
import com.example.mp3player.data.local.database.entity.PlayCountEntity

/**
 * Configurazione principale del database Room dell'applicazione.
 * Definisce le tabelle (entities), la versione e i DAO associati.
 */
@Database(
    entities = [
        FavoriteEntity::class,
        QueueItemEntity::class,
        PlayCountEntity::class   // ← nuovo: conteggio ascolti per le statistiche
    ],
    version = 3,           // ← incrementato da 2 a 3 (aggiunta tabella play_counts)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Esposizione dei DAO per l'accesso ai dati
    abstract fun favoriteDao(): FavoriteDao
    abstract fun queueDao(): QueueDao
    abstract fun playCountDao(): PlayCountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mp3player.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}