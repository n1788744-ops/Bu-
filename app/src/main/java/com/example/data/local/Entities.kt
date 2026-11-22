package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val idKCode: String,
    val phone: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val displayName: String,
    val city: String,
    val birthDate: String,
    val bio: String,
    val avatarColorHex: Long,
    val consentAcceptedAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "browser_tabs")
data class BrowserTabEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val isHome: Boolean,
    val lastVisitedAt: Long,
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val shortBadge: String,
    val accentColorHex: Long,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "history_items")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis(),
    val wasSuspicious: Boolean = false,
    val wasBypassed: Boolean = false,
)
