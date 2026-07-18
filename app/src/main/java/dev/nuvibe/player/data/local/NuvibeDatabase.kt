package dev.nuvibe.player.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PlaylistEntity::class, PlaylistTrackEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class NuvibeDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile private var instance: NuvibeDatabase? = null

        fun get(context: Context): NuvibeDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NuvibeDatabase::class.java,
                    "nuvibe.db",
                ).build().also { instance = it }
            }
    }
}
