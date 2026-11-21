package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles")
    suspend fun clearProfile()
}

@Dao
interface BrowserTabDao {
    @Query("SELECT * FROM browser_tabs ORDER BY id ASC")
    fun observeTabs(): Flow<List<BrowserTabEntity>>

    @Query("SELECT * FROM browser_tabs ORDER BY id ASC")
    suspend fun getTabsOnce(): List<BrowserTabEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: BrowserTabEntity): Long

    @Update
    suspend fun updateTab(tab: BrowserTabEntity)

    @Query("UPDATE browser_tabs SET title = :title, url = :url, isHome = :isHome, lastVisitedAt = :timestamp WHERE id = :tabId")
    suspend fun updateTabNavigation(tabId: Long, title: String, url: String, isHome: Boolean, timestamp: Long)

    @Query("DELETE FROM browser_tabs WHERE id = :tabId")
    suspend fun deleteTab(tabId: Long)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY isTelegram DESC, id ASC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT COUNT(*) FROM bookmarks")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookmarks: List<BookmarkEntity>)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_items ORDER BY visitedAt DESC LIMIT 200")
    fun observeHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity)

    @Query("DELETE FROM history_items")
    suspend fun clearAll()
}

@Dao
interface ProxyConfigDao {
    @Query("SELECT * FROM proxy_configs ORDER BY id ASC")
    fun observeConfigs(): Flow<List<ProxyConfigEntity>>

    @Query("SELECT COUNT(*) FROM proxy_configs")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ProxyConfigEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<ProxyConfigEntity>)

    @Query("UPDATE proxy_configs SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE proxy_configs SET isActive = 1 WHERE id = :id")
    suspend fun activateById(id: Long)

    @Query("UPDATE proxy_configs SET lastLatencyMs = :latency WHERE id = :id")
    suspend fun updateLatency(id: Long, latency: Int?)

    @Query("DELETE FROM proxy_configs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
