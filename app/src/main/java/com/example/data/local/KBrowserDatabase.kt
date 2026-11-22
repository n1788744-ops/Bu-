package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        BrowserTabEntity::class,
        BookmarkEntity::class,
        HistoryEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class KBrowserDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun browserTabDao(): BrowserTabDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var instance: KBrowserDatabase? = null

        fun getInstance(context: Context): KBrowserDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KBrowserDatabase::class.java,
                    "k_browser_db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
