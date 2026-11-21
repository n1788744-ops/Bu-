package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LegalDocumentType
import com.example.core.ProxyApplyStatus
import com.example.core.UrlSafetyAnalyzer
import com.example.data.local.UserProfileEntity
import com.example.ui.BrowserOverlay
import com.example.ui.BrowserUiState
import com.example.ui.BrowserViewModel
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.WarningAmber

@Composable
fun BrowserMainScreen(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel,
) {
    val activeTab = uiState.activeTab
    val webViewController = remember(activeTab?.id) { BrowserWebViewController() }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    // Handle back navigation gracefully: overlay -> WebView back -> Home page
    val shouldInterceptBack = uiState.activeOverlay != BrowserOverlay.NONE ||
        uiState.pendingSafetyWarning != null ||
        (activeTab != null && !activeTab.isHome)

    BackHandler(enabled = shouldInterceptBack) {
        when {
            uiState.pendingSafetyWarning != null -> viewModel.dismissSafetyWarningBackToSafety()
            uiState.activeOverlay != BrowserOverlay.NONE -> viewModel.setOverlay(BrowserOverlay.NONE)
            webViewController.goBack() -> Unit
            activeTab != null && !activeTab.isHome -> viewModel.goHomeInActiveTab()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding(),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .then(
                            if (activeTab != null && !activeTab.isHome) {
                                Modifier.statusBarsPadding()
                            } else {
                                Modifier
                            }
                        ),
                ) {
                    if (activeTab == null || activeTab.isHome) {
                        StartPageContent(
                            profile = uiState.profile,
                            bookmarks = uiState.bookmarks,
                            history = uiState.history,
                            activeProxy = uiState.activeProxy,
                            proxyEnabled = uiState.settings.proxyEnabled,
                            safeBrowsingEnabled = uiState.settings.safeBrowsingEnabled,
                            searchEngine = uiState.settings.searchEngine,
                            onOpenOmnibox = { viewModel.setOverlay(BrowserOverlay.OMNIBOX_EDIT) },
                            onOpenUrl = { viewModel.requestLoadUrl(it) },
                            onOpenTelegramWeb = { viewModel.openTelegramWeb() },
                            onOpenProxySheet = { viewModel.setOverlay(BrowserOverlay.PROXY_SHEET) },
                            onOpenProfile = { viewModel.setOverlay(BrowserOverlay.PROFILE_SCREEN) },
                            onOpenBookmarks = { viewModel.setOverlay(BrowserOverlay.BOOKMARKS_SHEET) },
                            onOpenHistory = { viewModel.setOverlay(BrowserOverlay.HISTORY_SHEET) },
                        )
                    } else {
                        key(activeTab.id) {
                            BrowserWebView(
                                tabId = activeTab.id,
                                targetUrl = activeTab.url,
                                approvedLoadRequest = uiState.approvedLoadRequest,
                                activeProxy = uiState.activeProxy,
                                telegramKeepAliveEnabled = uiState.settings.telegramKeepAliveEnabled,
                                controller = webViewController,
                                onInterceptNavigation = { url ->
                                    viewModel.shouldInterceptWebViewNavigation(url)
                                },
                                onPageUpdated = { title, url, progress, isLoading, canBack, canForward ->
                                    viewModel.onWebPageUpdated(
                                        title = title,
                                        url = url,
                                        progress = progress,
                                        isLoading = isLoading,
                                        canGoBack = canBack,
                                        canGoForward = canForward,
                                    )
                                },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        // Compact Telegram Proxy pill floating at top when browsing Telegram Web
                        if (UrlSafetyAnalyzer.isTelegramHost(activeTab.url)) {
                            Surface(
                                onClick = { viewModel.setOverlay(BrowserOverlay.PROXY_SHEET) },
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                border = BorderStroke(1.dp, TelegramBlue.copy(alpha = 0.45f)),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (uiState.settings.proxyEnabled) SafeEmerald else WarningAmber),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val proxyBadge = when (val st = uiState.proxyApplyStatus) {
                                        is ProxyApplyStatus.ActiveProxy -> st.label
                                        is ProxyApplyStatus.Unsupported -> "Keep-Alive"
                                        ProxyApplyStatus.DirectKeepAlive -> "TG Стабильный канал"
                                    }
                                    Text(
                                        text = proxyBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Omnibox matching the screenshot's signature floating address bar
                BottomOmniboxBar(
                    tabCount = uiState.tabs.size.coerceAtLeast(1),
                    currentUrl = activeTab?.url.orEmpty(),
                    isHome = activeTab?.isHome ?: true,
                    isLoading = uiState.isWebLoading,
                    progress = uiState.webLoadingProgress,
                    onTabsClick = { viewModel.setOverlay(BrowserOverlay.TABS_SHEET) },
                    onAddressClick = { viewModel.setOverlay(BrowserOverlay.OMNIBOX_EDIT) },
                    onRefreshOrStop = {
                        if (activeTab == null || activeTab.isHome) {
                            viewModel.setOverlay(BrowserOverlay.OMNIBOX_EDIT)
                        } else if (uiState.isWebLoading) {
                            webViewController.stopLoading()
                        } else {
                            webViewController.reload()
                        }
                    },
                    onMenuClick = { viewModel.setOverlay(BrowserOverlay.MENU_SHEET) },
                )
            }

            // Fullscreen Overlays
            AnimatedVisibility(visible = uiState.activeOverlay == BrowserOverlay.OMNIBOX_EDIT) {
                OmniboxSearchOverlay(
                    initialQuery = if (activeTab?.isHome == true) "" else activeTab?.url.orEmpty(),
                    searchEngine = uiState.settings.searchEngine,
                    bookmarks = uiState.bookmarks,
                    history = uiState.history,
                    onSubmit = { viewModel.navigateFromOmnibox(it) },
                    onDismiss = { viewModel.setOverlay(BrowserOverlay.NONE) },
                )
            }

            AnimatedVisibility(visible = uiState.activeOverlay == BrowserOverlay.PROFILE_SCREEN) {
                val profile = uiState.profile
                if (profile != null) {
                    ProfileScreen(
                        profile = profile,
                        onSave = { updated: UserProfileEntity -> viewModel.saveUserProfile(updated) },
                        onBack = { viewModel.setOverlay(BrowserOverlay.NONE) },
                        onOpenLegal = { doc: LegalDocumentType -> viewModel.openLegalDialog(doc) },
                        onSignOut = { viewModel.signOut() },
                        onShowMessage = { msg -> viewModel.showSnackbar(msg) },
                    )
                }
            }
        }
    }

    // Modal Bottom Sheets
    when (uiState.activeOverlay) {
        BrowserOverlay.MENU_SHEET -> {
            val isBookmarked = activeTab != null && !activeTab.isHome &&
                uiState.bookmarks.any { it.url == activeTab.url }
            BrowserMenuSheet(
                profile = uiState.profile,
                tabCount = uiState.tabs.size,
                canGoBack = uiState.canWebGoBack,
                canGoForward = uiState.canWebGoForward,
                isHome = activeTab?.isHome ?: true,
                isBookmarked = isBookmarked,
                proxyEnabled = uiState.settings.proxyEnabled,
                onGoBack = { webViewController.goBack() },
                onGoForward = { webViewController.goForward() },
                onGoHome = { viewModel.goHomeInActiveTab() },
                onReload = { webViewController.reload() },
                onToggleBookmark = { viewModel.toggleCurrentPageBookmark() },
                onOpenProfile = { viewModel.setOverlay(BrowserOverlay.PROFILE_SCREEN) },
                onOpenProxy = { viewModel.setOverlay(BrowserOverlay.PROXY_SHEET) },
                onOpenTabs = { viewModel.setOverlay(BrowserOverlay.TABS_SHEET) },
                onOpenBookmarks = { viewModel.setOverlay(BrowserOverlay.BOOKMARKS_SHEET) },
                onOpenHistory = { viewModel.setOverlay(BrowserOverlay.HISTORY_SHEET) },
                onOpenSettings = { viewModel.setOverlay(BrowserOverlay.SETTINGS_SHEET) },
                onDismiss = { viewModel.setOverlay(BrowserOverlay.NONE) },
            )
        }

        BrowserOverlay.TABS_SHEET -> {
            TabSwitcherSheet(
                tabs = uiState.tabs,
                activeTabId = activeTab?.id ?: -1L,
                onSelectTab = { viewModel.selectTab(it) },
                onCloseTab = { viewModel.closeTab(it) },
                onNewTab = { viewModel.createNewTab() },
                onDismiss = { viewModel.setOverlay(BrowserOverlay.NONE) },
            )
        }

        BrowserOverlay.PROXY_SHEET -> {
            TelegramProxySheet(
                proxyEnabled = uiState.settings.proxyEnabled,
                telegramKeepAlive = uiState.settings.telegramKeepAliveEnabled,
                telegramWebVariant = uiState.settings.telegramWebVariant,
                proxyConfigs = uiState.proxyConfigs,
                activeProxy = uiState.activeProxy,
                proxyApplyStatus = uiState.proxyApplyStatus,
                proxyTestResult = uiState.proxyTestResult,
                isTestingProxy = uiState.isTestingProxy,
                onToggleProxyEnabled = { viewModel.setProxyEnabled(it) },
                onToggleKeepAlive = { viewModel.setTelegramKeepAlive(it) },
                onChangeTelegramVariant = { viewModel.setTelegramWebVariant(it) },
                onActivateConfig = { viewModel.activateProxyConfig(it) },
                onSaveCustomConfig = { name, proto, host, port, user, pass, tgOnly ->
                    viewModel.saveCustomProxyConfig(name, proto, host, port, user, pass, tgOnly)
                },
                onDeleteConfig = { viewModel.deleteProxyConfig(it) },
                onTestProxy = { host, port, id -> viewModel.testProxyConnection(host, port, id) },
                onOpenTelegramWeb = { viewModel.openTelegramWeb() },
                onDismiss = { viewModel.setOverlay(BrowserOverlay.NONE) },
            )
        }

        BrowserOverlay.BOOKMARKS_SHEET -> {
            BookmarksSheet(
                bookmarks = uiState.bookmarks,
                onOpenUrl = { viewModel.requestLoadUrl(it) },
                onAddBookmark = { title, url -> viewModel.addCustomBookmark(title, url) },
                onDeleteBookmark = { viewModel.deleteBookmark(it) },
                onDismiss = { viewModel.setOverlay(BrowserOverlay.NONE) },
            )
        }

        BrowserOverlay.HISTORY_SHEET -> {
            HistorySheet(
                history = uiState.history,
                onOpenUrl = { viewModel.requestLoadUrl(it) },
                onClearHistory = { viewModel.clearHistory() },
                onDismiss = { viewModel.setOverlay(BrowserOverlay.NONE) },
            )
        }

        BrowserOverlay.SETTINGS_SHEET -> {
            SettingsSheet(
                settings = uiState.settings,
                onChangeSearchEngine = { viewModel.setSearchEngine(it) },
                onToggleSafeBrowsing = { viewModel.setSafeBrowsingEnabled(it) },
                onChangeThemeMode = { viewModel.setThemeMode(it) },
                onOpenLegal = { viewModel.openLegalDialog(it) },
                onDismiss = { viewModel.setOverlay(BrowserOverlay.NONE) },
            )
        }

        else -> Unit
    }

    // Dangerous Resource Interstitial Warning
    val pendingWarning = uiState.pendingSafetyWarning
    if (pendingWarning != null) {
        DangerousResourceModal(
            warning = pendingWarning,
            onBackToSafety = { viewModel.dismissSafetyWarningBackToSafety() },
            onProceedAnyway = { viewModel.proceedAnywayOnSuspiciousUrl() },
        )
    }

    // Legal Document Dialog
    val legalDoc = uiState.legalDialog
    if (legalDoc != null) {
        LegalDocumentDialog(
            type = legalDoc,
            onDismiss = { viewModel.openLegalDialog(null) },
        )
    }
}

@Composable
private fun BottomOmniboxBar(
    tabCount: Int,
    currentUrl: String,
    isHome: Boolean,
    isLoading: Boolean,
    progress: Int,
    onTabsClick: () -> Unit,
    onAddressClick: () -> Unit,
    onRefreshOrStop: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val hostLabel = remember(currentUrl, isHome) {
        if (isHome || currentUrl.isBlank()) {
            "Главная • Браузер K"
        } else {
            UrlSafetyAnalyzer.extractHost(currentUrl) ?: currentUrl
        }
    }
    val isHttpInsecure = !isHome && currentUrl.startsWith("http://", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isLoading && !isHome) {
            LinearProgressIndicator(
                progress = { (progress.coerceIn(5, 100)) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .height(2.5.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(50)),
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Pill Omnibox mirroring screenshot bottom bar: [N] | lock + domain | refresh | menu
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .height(54.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Tab Counter Badge [N]
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onTabsClick)
                        .testTag("bottom_tabs_button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(
                                width = 1.8.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(7.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tabCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                // Center Address Pill
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onAddressClick)
                        .padding(horizontal = 8.dp)
                        .testTag("bottom_address_bar"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = when {
                            isHome -> Icons.Default.Search
                            isHttpInsecure -> Icons.Default.Warning
                            else -> Icons.Default.Lock
                        },
                        contentDescription = null,
                        tint = when {
                            isHome -> MaterialTheme.colorScheme.onSurfaceVariant
                            isHttpInsecure -> WarningAmber
                            else -> SafeEmerald
                        },
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = hostLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                IconButton(
                    onClick = onRefreshOrStop,
                    modifier = Modifier.testTag("bottom_refresh_button"),
                ) {
                    Icon(
                        imageVector = if (isLoading && !isHome) Icons.Default.Close else Icons.Default.Refresh,
                        contentDescription = if (isLoading && !isHome) "Остановить" else "Обновить",
                        modifier = Modifier.size(21.dp),
                    )
                }

                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.testTag("bottom_menu_button"),
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Меню",
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
        }
    }
}
