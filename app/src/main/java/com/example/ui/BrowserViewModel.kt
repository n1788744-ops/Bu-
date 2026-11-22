package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.LegalDocumentType
import com.example.core.SafetyVerdict
import com.example.core.UrlSafetyAnalyzer
import com.example.data.BrowserRepository
import com.example.data.local.BookmarkEntity
import com.example.data.local.BrowserTabEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.KBrowserDatabase
import com.example.data.local.UserProfileEntity
import com.example.data.preferences.BrowserPreferences
import com.example.data.preferences.BrowserSettingsSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AuthInputMode {
    PHONE,
    EMAIL,
}

enum class AuthStep {
    ENTER_IDENTIFIER,
    VERIFY_CODE,
}

enum class BrowserOverlay {
    NONE,
    OMNIBOX_EDIT,
    MENU_SHEET,
    TABS_SHEET,
    PROFILE_SCREEN,
    BOOKMARKS_SHEET,
    HISTORY_SHEET,
    SETTINGS_SHEET,
}

data class AuthUiState(
    val inputMode: AuthInputMode = AuthInputMode.PHONE,
    val step: AuthStep = AuthStep.ENTER_IDENTIFIER,
    val phoneDigits: String = "",
    val emailInput: String = "",
    val otpInput: String = "",
    val generatedOtp: String = "",
    val consentAccepted: Boolean = false,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
)

data class BrowserUiState(
    val isInitialized: Boolean = false,
    val settings: BrowserSettingsSnapshot = BrowserSettingsSnapshot(),
    val profile: UserProfileEntity? = null,
    val tabs: List<BrowserTabEntity> = emptyList(),
    val activeTab: BrowserTabEntity? = null,
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val history: List<HistoryEntity> = emptyList(),
    val activeOverlay: BrowserOverlay = BrowserOverlay.NONE,
    val legalDialog: LegalDocumentType? = null,
    val pendingSafetyWarning: SafetyVerdict.Suspicious? = null,
    val webLoadingProgress: Int = 0,
    val isWebLoading: Boolean = false,
    val canWebGoBack: Boolean = false,
    val canWebGoForward: Boolean = false,
    val snackbarMessage: String? = null,
    val approvedLoadRequest: Pair<Long, String>? = null,
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BrowserRepository(
        db = KBrowserDatabase.getInstance(application),
        preferences = BrowserPreferences(application),
    )

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState

    private val _ephemeral = MutableStateFlow(EphemeralBrowserState())
    private val _initialized = MutableStateFlow(false)

    private data class EphemeralBrowserState(
        val activeOverlay: BrowserOverlay = BrowserOverlay.NONE,
        val legalDialog: LegalDocumentType? = null,
        val pendingSafetyWarning: SafetyVerdict.Suspicious? = null,
        val webLoadingProgress: Int = 0,
        val isWebLoading: Boolean = false,
        val canWebGoBack: Boolean = false,
        val canWebGoForward: Boolean = false,
        val snackbarMessage: String? = null,
        val approvedLoadRequest: Pair<Long, String>? = null,
    )

    val uiState: StateFlow<BrowserUiState> = combine(
        _initialized,
        repository.settings,
        repository.profile,
        repository.tabs,
        repository.bookmarks,
        repository.history,
        _ephemeral,
    ) { values ->
        val initialized = values[0] as Boolean
        val settings = values[1] as BrowserSettingsSnapshot
        val profile = values[2] as UserProfileEntity?
        @Suppress("UNCHECKED_CAST")
        val tabs = values[3] as List<BrowserTabEntity>
        @Suppress("UNCHECKED_CAST")
        val bookmarks = values[4] as List<BookmarkEntity>
        @Suppress("UNCHECKED_CAST")
        val history = values[5] as List<HistoryEntity>
        val eph = values[6] as EphemeralBrowserState

        val activeTab = tabs.firstOrNull { it.id == settings.activeTabId } ?: tabs.firstOrNull()

        BrowserUiState(
            isInitialized = initialized,
            settings = settings,
            profile = profile,
            tabs = tabs,
            activeTab = activeTab,
            bookmarks = bookmarks,
            history = history,
            activeOverlay = eph.activeOverlay,
            legalDialog = eph.legalDialog,
            pendingSafetyWarning = eph.pendingSafetyWarning,
            webLoadingProgress = eph.webLoadingProgress,
            isWebLoading = eph.isWebLoading,
            canWebGoBack = eph.canWebGoBack,
            canWebGoForward = eph.canWebGoForward,
            snackbarMessage = eph.snackbarMessage,
            approvedLoadRequest = eph.approvedLoadRequest,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BrowserUiState(),
    )

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
            _initialized.value = true
        }
    }

    // --- ID K Auth Actions ---

    fun onPhoneDigitsChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(10)
        _authState.update { it.copy(phoneDigits = digits, errorMessage = null) }
    }

    fun onEmailInputChange(value: String) {
        _authState.update { it.copy(emailInput = value, errorMessage = null) }
    }

    fun toggleAuthInputMode() {
        _authState.update { state ->
            val nextMode = if (state.inputMode == AuthInputMode.PHONE) {
                AuthInputMode.EMAIL
            } else {
                AuthInputMode.PHONE
            }
            state.copy(
                inputMode = nextMode,
                step = AuthStep.ENTER_IDENTIFIER,
                errorMessage = null,
            )
        }
    }

    fun onConsentCheckedChange(checked: Boolean) {
        _authState.update { it.copy(consentAccepted = checked, errorMessage = null) }
    }

    fun submitIdentifierStep() {
        val state = _authState.value
        if (!state.consentAccepted) {
            _authState.update {
                it.copy(errorMessage = "Подтвердите согласие на обработку персональных данных")
            }
            return
        }
        when (state.inputMode) {
            AuthInputMode.PHONE -> {
                if (state.phoneDigits.length < 10) {
                    _authState.update {
                        it.copy(errorMessage = "Введите 10 цифр номера телефона после +7")
                    }
                    return
                }
            }
            AuthInputMode.EMAIL -> {
                val trimmed = state.emailInput.trim()
                if (!trimmed.contains("@") || trimmed.length < 5) {
                    _authState.update {
                        it.copy(errorMessage = "Введите корректный e-mail или логин ID K (например, name@mail.ru)")
                    }
                    return
                }
            }
        }
        val otp = Random.nextInt(1000, 9999).toString()
        _authState.update {
            it.copy(
                step = AuthStep.VERIFY_CODE,
                generatedOtp = otp,
                otpInput = "",
                errorMessage = null,
            )
        }
    }

    fun onOtpInputChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(4)
        _authState.update { it.copy(otpInput = digits, errorMessage = null) }
    }

    fun fillDemoOtp() {
        _authState.update { it.copy(otpInput = it.generatedOtp, errorMessage = null) }
    }

    fun backToIdentifierStep() {
        _authState.update { it.copy(step = AuthStep.ENTER_IDENTIFIER, errorMessage = null) }
    }

    fun verifyOtpAndSignIn() {
        val state = _authState.value
        if (state.otpInput.length < 4) {
            _authState.update { it.copy(errorMessage = "Введите 4-значный код подтверждения") }
            return
        }
        if (state.otpInput != state.generatedOtp) {
            _authState.update { it.copy(errorMessage = "Неверный код. Используйте код ${state.generatedOtp}") }
            return
        }

        _authState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val formattedPhone = if (state.phoneDigits.length == 10) {
                formatRussianPhone(state.phoneDigits)
            } else {
                ""
            }
            val cleanEmail = state.emailInput.trim()
            repository.createOrRestoreIdKProfile(
                phone = formattedPhone,
                email = cleanEmail,
                consentAccepted = state.consentAccepted,
            )
            _authState.update { AuthUiState() }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _ephemeral.update { EphemeralBrowserState() }
            repository.signOut()
        }
    }

    // --- Profile Editing ---

    fun saveUserProfile(updated: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(updated)
            showSnackbar("Профиль ID K обновлён")
        }
    }

    // --- Navigation & Dangerous Resource Check ---

    fun navigateFromOmnibox(rawInput: String) {
        val resolved = UrlSafetyAnalyzer.resolveOmniboxInput(
            rawInput = rawInput,
            searchEngine = uiState.value.settings.searchEngine,
        )
        if (resolved.isBlank()) return
        setOverlay(BrowserOverlay.NONE)
        requestLoadUrl(resolved)
    }

    fun requestLoadUrl(url: String) {
        val safeEnabled = uiState.value.settings.safeBrowsingEnabled
        val verdict = UrlSafetyAnalyzer.analyzeUrl(url, safeBrowsingEnabled = safeEnabled)
        when (verdict) {
            is SafetyVerdict.Safe -> {
                proceedToUrl(url, wasSuspicious = false, wasBypassed = false)
            }
            is SafetyVerdict.Suspicious -> {
                _ephemeral.update { it.copy(pendingSafetyWarning = verdict) }
            }
        }
    }

    fun shouldInterceptWebViewNavigation(url: String): Boolean {
        val safeEnabled = uiState.value.settings.safeBrowsingEnabled
        val verdict = UrlSafetyAnalyzer.analyzeUrl(url, safeBrowsingEnabled = safeEnabled)
        return if (verdict is SafetyVerdict.Suspicious) {
            _ephemeral.update { it.copy(pendingSafetyWarning = verdict) }
            true
        } else {
            false
        }
    }

    fun dismissSafetyWarningBackToSafety() {
        _ephemeral.update { it.copy(pendingSafetyWarning = null) }
    }

    fun proceedAnywayOnSuspiciousUrl() {
        val warning = _ephemeral.value.pendingSafetyWarning ?: return
        UrlSafetyAnalyzer.allowForSession(warning.url)
        _ephemeral.update { it.copy(pendingSafetyWarning = null) }
        proceedToUrl(warning.url, wasSuspicious = true, wasBypassed = true)
    }

    private fun proceedToUrl(url: String, wasSuspicious: Boolean, wasBypassed: Boolean) {
        val tab = uiState.value.activeTab ?: return
        viewModelScope.launch {
            val hostTitle = UrlSafetyAnalyzer.extractHost(url) ?: url
            repository.updateTabNavigation(
                tabId = tab.id,
                title = hostTitle,
                url = url,
                isHome = false,
            )
            repository.recordHistory(
                title = hostTitle,
                url = url,
                wasSuspicious = wasSuspicious,
                wasBypassed = wasBypassed,
            )
            _ephemeral.update {
                it.copy(
                    approvedLoadRequest = System.nanoTime() to url,
                )
            }
        }
    }

    fun goHomeInActiveTab() {
        val tab = uiState.value.activeTab ?: return
        viewModelScope.launch {
            repository.updateTabNavigation(
                tabId = tab.id,
                title = "Главная",
                url = "",
                isHome = true,
            )
            _ephemeral.update {
                it.copy(
                    isWebLoading = false,
                    webLoadingProgress = 0,
                    canWebGoBack = false,
                    canWebGoForward = false,
                )
            }
        }
    }

    fun onWebPageUpdated(
        title: String?,
        url: String?,
        progress: Int,
        isLoading: Boolean,
        canGoBack: Boolean,
        canGoForward: Boolean,
    ) {
        _ephemeral.update {
            it.copy(
                webLoadingProgress = progress,
                isWebLoading = isLoading,
                canWebGoBack = canGoBack,
                canWebGoForward = canGoForward,
            )
        }
        val activeTab = uiState.value.activeTab ?: return
        if (!activeTab.isHome && !url.isNullOrBlank()) {
            val resolvedTitle = title?.takeIf { it.isNotBlank() } ?: (UrlSafetyAnalyzer.extractHost(url) ?: url)
            if (resolvedTitle != activeTab.title || url != activeTab.url) {
                viewModelScope.launch {
                    repository.updateTabNavigation(
                        tabId = activeTab.id,
                        title = resolvedTitle,
                        url = url,
                        isHome = false,
                    )
                }
            }
        }
    }

    // --- Tabs ---

    fun createNewTab() {
        viewModelScope.launch {
            repository.createNewTab()
            setOverlay(BrowserOverlay.NONE)
        }
    }

    fun selectTab(tabId: Long) {
        viewModelScope.launch {
            repository.selectTab(tabId)
            setOverlay(BrowserOverlay.NONE)
        }
    }

    fun closeTab(tabId: Long) {
        viewModelScope.launch {
            repository.closeTab(tabId)
        }
    }

    // --- Bookmarks & History ---

    fun toggleCurrentPageBookmark() {
        val tab = uiState.value.activeTab ?: return
        if (tab.isHome || tab.url.isBlank()) return
        val existing = uiState.value.bookmarks.any { it.url == tab.url }
        viewModelScope.launch {
            if (existing) {
                repository.removeBookmarkByUrl(tab.url)
                showSnackbar("Удалено из закладок")
            } else {
                repository.toggleBookmark(tab.title, tab.url)
                showSnackbar("Добавлено в закладки")
            }
        }
    }

    fun addCustomBookmark(title: String, url: String) {
        val normalized = UrlSafetyAnalyzer.resolveOmniboxInput(url, uiState.value.settings.searchEngine)
        if (normalized.isBlank()) return
        viewModelScope.launch {
            repository.toggleBookmark(title.ifBlank { normalized }, normalized)
            showSnackbar("Закладка сохранена")
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            repository.removeBookmarkById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showSnackbar("История посещений очищена")
        }
    }

    // --- Settings & Overlays ---

    fun setSearchEngine(engine: String) {
        viewModelScope.launch { repository.setSearchEngine(engine) }
    }

    fun setSafeBrowsingEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSafeBrowsingEnabled(enabled) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setOverlay(overlay: BrowserOverlay) {
        _ephemeral.update { it.copy(activeOverlay = overlay) }
    }

    fun openLegalDialog(type: LegalDocumentType?) {
        _ephemeral.update { it.copy(legalDialog = type) }
    }

    fun showSnackbar(message: String) {
        _ephemeral.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _ephemeral.update { it.copy(snackbarMessage = null) }
    }

    companion object {
        fun formatRussianPhone(tenDigits: String): String {
            val d = tenDigits.filter { it.isDigit() }.take(10)
            if (d.isEmpty()) return ""
            val p1 = d.take(3)
            val p2 = d.drop(3).take(3)
            val p3 = d.drop(6).take(2)
            val p4 = d.drop(8).take(2)
            return buildString {
                append("+7 ")
                if (p1.isNotEmpty()) append("($p1")
                if (p1.length == 3) append(") ")
                if (p2.isNotEmpty()) append(p2)
                if (p3.isNotEmpty()) append("-$p3")
                if (p4.isNotEmpty()) append("-$p4")
            }
        }
    }
}
