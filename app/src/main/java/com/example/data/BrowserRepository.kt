package com.example.data

import com.example.data.local.BookmarkEntity
import com.example.data.local.BrowserTabEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.KBrowserDatabase
import com.example.data.local.ProxyConfigEntity
import com.example.data.local.UserProfileEntity
import com.example.data.preferences.BrowserPreferences
import com.example.data.preferences.BrowserSettingsSnapshot
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class BrowserRepository(
    private val db: KBrowserDatabase,
    private val preferences: BrowserPreferences,
) {
    val profile: Flow<UserProfileEntity?> = db.userProfileDao().observeProfile()
    val tabs: Flow<List<BrowserTabEntity>> = db.browserTabDao().observeTabs()
    val bookmarks: Flow<List<BookmarkEntity>> = db.bookmarkDao().observeBookmarks()
    val history: Flow<List<HistoryEntity>> = db.historyDao().observeHistory()
    val proxyConfigs: Flow<List<ProxyConfigEntity>> = db.proxyConfigDao().observeConfigs()
    val settings: Flow<BrowserSettingsSnapshot> = preferences.settingsFlow

    suspend fun ensureSeedData() {
        if (db.bookmarkDao().count() == 0) {
            db.bookmarkDao().insertAll(
                listOf(
                    BookmarkEntity(
                        title = "Telegram Web",
                        url = "https://web.telegram.org/k/",
                        shortBadge = "TG",
                        accentColorHex = 0xFF229ED9,
                        isTelegram = true,
                    ),
                    BookmarkEntity(
                        title = "Яндекс",
                        url = "https://ya.ru/",
                        shortBadge = "Я",
                        accentColorHex = 0xFFFC3F1D,
                    ),
                    BookmarkEntity(
                        title = "Google",
                        url = "https://www.google.com/",
                        shortBadge = "G",
                        accentColorHex = 0xFF4285F4,
                    ),
                    BookmarkEntity(
                        title = "Википедия",
                        url = "https://ru.wikipedia.org/",
                        shortBadge = "W",
                        accentColorHex = 0xFF334155,
                    ),
                    BookmarkEntity(
                        title = "GitHub",
                        url = "https://github.com/",
                        shortBadge = "GH",
                        accentColorHex = 0xFF181717,
                    ),
                    BookmarkEntity(
                        title = "Хабр",
                        url = "https://habr.com/ru/",
                        shortBadge = "H",
                        accentColorHex = 0xFF65A3BE,
                    ),
                )
            )
        }

        if (db.browserTabDao().getTabsOnce().isEmpty()) {
            val firstId = db.browserTabDao().insertTab(
                BrowserTabEntity(
                    title = "Главная",
                    url = "",
                    isHome = true,
                    lastVisitedAt = System.currentTimeMillis(),
                )
            )
            preferences.setActiveTabId(firstId)
        }

        if (db.proxyConfigDao().count() == 0) {
            db.proxyConfigDao().insertAll(
                listOf(
                    ProxyConfigEntity(
                        name = "Стабильный канал (Прямой + Keep-Alive)",
                        protocol = "DIRECT",
                        host = "web.telegram.org",
                        port = 443,
                        telegramOnly = true,
                        isActive = true,
                    ),
                    ProxyConfigEntity(
                        name = "Пользовательский HTTP/HTTPS прокси",
                        protocol = "HTTPS",
                        host = "127.0.0.1",
                        port = 8080,
                        telegramOnly = true,
                        isActive = false,
                    ),
                    ProxyConfigEntity(
                        name = "Пользовательский SOCKS5 прокси",
                        protocol = "SOCKS5",
                        host = "127.0.0.1",
                        port = 1080,
                        telegramOnly = true,
                        isActive = false,
                    ),
                )
            )
        }
    }

    suspend fun createOrRestoreIdKProfile(
        phone: String,
        email: String,
        consentAccepted: Boolean,
    ): UserProfileEntity {
        val existing = db.userProfileDao().getProfileOnce()
        val now = System.currentTimeMillis()
        val derivedName = when {
            existing != null && existing.displayName.isNotBlank() -> existing.displayName
            email.isNotBlank() -> email.substringBefore("@").replaceFirstChar { it.uppercase() }
            else -> "Пользователь K"
        }
        val profile = existing?.copy(
            phone = phone.ifBlank { existing.phone },
            email = email.ifBlank { existing.email },
            consentAcceptedAt = if (consentAccepted) now else existing.consentAcceptedAt,
            updatedAt = now,
        ) ?: UserProfileEntity(
            id = 1,
            idKCode = "IDK-${Random.nextInt(1000, 9999)}-${Random.nextInt(10, 99)}",
            phone = phone,
            email = email,
            firstName = "",
            lastName = "",
            displayName = derivedName,
            city = "",
            birthDate = "",
            bio = "",
            avatarColorHex = 0xFF0EA5E9,
            consentAcceptedAt = now,
            updatedAt = now,
        )
        db.userProfileDao().upsertProfile(profile)
        preferences.setAuthenticated(authenticated = true, consentAccepted = consentAccepted)
        return profile
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        db.userProfileDao().upsertProfile(profile.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun signOut() {
        preferences.setAuthenticated(authenticated = false, consentAccepted = false)
        db.userProfileDao().clearProfile()
    }

    suspend fun createNewTab(url: String = "", title: String = "Главная", isHome: Boolean = true): Long {
        val tabId = db.browserTabDao().insertTab(
            BrowserTabEntity(
                title = title,
                url = url,
                isHome = isHome,
                lastVisitedAt = System.currentTimeMillis(),
            )
        )
        preferences.setActiveTabId(tabId)
        return tabId
    }

    suspend fun selectTab(tabId: Long) {
        preferences.setActiveTabId(tabId)
    }

    suspend fun updateTabNavigation(tabId: Long, title: String, url: String, isHome: Boolean) {
        db.browserTabDao().updateTabNavigation(
            tabId = tabId,
            title = title,
            url = url,
            isHome = isHome,
            timestamp = System.currentTimeMillis(),
        )
    }

    suspend fun closeTab(tabId: Long) {
        val currentTabs = db.browserTabDao().getTabsOnce()
        db.browserTabDao().deleteTab(tabId)
        val remaining = currentTabs.filter { it.id != tabId }
        if (remaining.isEmpty()) {
            createNewTab()
        } else {
            preferences.setActiveTabId(remaining.last().id)
        }
    }

    suspend fun toggleBookmark(title: String, url: String) {
        if (url.isBlank()) return
        val cleanHost = url.removePrefix("https://").removePrefix("http://").removePrefix("www.")
        val badge = cleanHost.take(2).uppercase().ifBlank { "K" }
        val isTg = url.contains("telegram.org", ignoreCase = true)
        db.bookmarkDao().insertBookmark(
            BookmarkEntity(
                title = title.ifBlank { cleanHost.substringBefore("/") },
                url = url,
                shortBadge = badge,
                accentColorHex = if (isTg) 0xFF229ED9 else 0xFF334155,
                isTelegram = isTg,
            )
        )
    }

    suspend fun removeBookmarkByUrl(url: String) = db.bookmarkDao().deleteByUrl(url)
    suspend fun removeBookmarkById(id: Long) = db.bookmarkDao().deleteById(id)

    suspend fun recordHistory(title: String, url: String, wasSuspicious: Boolean, wasBypassed: Boolean) {
        if (url.isBlank()) return
        db.historyDao().insertHistory(
            HistoryEntity(
                title = title.ifBlank { url },
                url = url,
                visitedAt = System.currentTimeMillis(),
                wasSuspicious = wasSuspicious,
                wasBypassed = wasBypassed,
            )
        )
    }

    suspend fun clearHistory() = db.historyDao().clearAll()

    suspend fun saveAndActivateProxy(config: ProxyConfigEntity): Long {
        db.proxyConfigDao().deactivateAll()
        val id = db.proxyConfigDao().insertConfig(config.copy(isActive = true))
        preferences.setProxyEnabled(true)
        return id
    }

    suspend fun activateProxy(id: Long) {
        db.proxyConfigDao().deactivateAll()
        db.proxyConfigDao().activateById(id)
        preferences.setProxyEnabled(true)
    }

    suspend fun updateProxyLatency(id: Long, latencyMs: Int?) {
        db.proxyConfigDao().updateLatency(id, latencyMs)
    }

    suspend fun deleteProxyConfig(id: Long) = db.proxyConfigDao().deleteById(id)

    suspend fun setProxyEnabled(enabled: Boolean) = preferences.setProxyEnabled(enabled)
    suspend fun setTelegramKeepAlive(enabled: Boolean) = preferences.setTelegramKeepAlive(enabled)
    suspend fun setTelegramWebVariant(variant: String) = preferences.setTelegramWebVariant(variant)
    suspend fun setSearchEngine(engine: String) = preferences.setSearchEngine(engine)
    suspend fun setSafeBrowsingEnabled(enabled: Boolean) = preferences.setSafeBrowsingEnabled(enabled)
    suspend fun setThemeMode(mode: String) = preferences.setThemeMode(mode)
}
