package com.chommiehub.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chommiehub.app.data.local.dao.ShortlistDao
import com.chommiehub.app.data.local.entity.CachedAccommodationEntity
import com.chommiehub.app.data.local.entity.ShortlistEntryEntity

/**
 * The on-phone database used for offline residences and shortlists.
 */
@Database(
    entities = [
        CachedAccommodationEntity::class,
        ShortlistEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shortlistDao(): ShortlistDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chommiehub.db"
                ).build().also { instance = it }
            }
        }

        /** Used by classes that have no Context. Needs getInstance() to have run first. */
        fun get(): AppDatabase {
            return instance ?: error(
                "AppDatabase is not ready. It is created in ChommieHubApplication."
            )
        }
    }
}