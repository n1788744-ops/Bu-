package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "k_browser_prefs")

data class BrowserSettingsSnapshot(
    val isAuthenticated: Boolean = false,
    val consentAccepted: Boolean = false,
    val activeTabId: Long = 1L,
    val searchEngine: String = "Yandex",
    val safeBrowsingEnabled: Boolean = true,
    val themeMode: String = "system",
)

class BrowserPreferences(private val context: Context) {

    private object Keys {
        val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
        val CONSENT_ACCEPTED = booleanPreferencesKey("consent_accepted")
        val ACTIVE_TAB_ID = longPreferencesKey("active_tab_id")
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val SAFE_BROWSING_ENABLED = booleanPreferencesKey("safe_browsing_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val settingsFlow: Flow<BrowserSettingsSnapshot> = context.dataStore.data.map { prefs ->
        BrowserSettingsSnapshot(
            isAuthenticated = prefs[Keys.IS_AUTHENTICATED] ?: false,
            consentAccepted = prefs[Keys.CONSENT_ACCEPTED] ?: false,
            activeTabId = prefs[Keys.ACTIVE_TAB_ID] ?: 1L,
            searchEngine = prefs[Keys.SEARCH_ENGINE] ?: "Yandex",
            safeBrowsingEnabled = prefs[Keys.SAFE_BROWSING_ENABLED] ?: true,
            themeMode = prefs[Keys.THEME_MODE] ?: "system",
        )
    }

    suspend fun setAuthenticated(authenticated: Boolean, consentAccepted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_AUTHENTICATED] = authenticated
            prefs[Keys.CONSENT_ACCEPTED] = consentAccepted
        }
    }

    suspend fun setActiveTabId(tabId: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_TAB_ID] = tabId
        }
    }

    suspend fun setSearchEngine(engine: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SEARCH_ENGINE] = engine
        }
    }

    suspend fun setSafeBrowsingEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SAFE_BROWSING_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }
}
