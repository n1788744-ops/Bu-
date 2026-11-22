package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LegalDocumentType
import com.example.core.UrlSafetyAnalyzer
import com.example.data.local.UserProfileEntity
import com.example.ui.BrowserOverlay
import com.example.ui.BrowserUiState
import com.example.ui.BrowserViewModel
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.Obsidian
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SafeEmerald
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
            val isHome = activeTab == null || activeTab.isHome

            if (isHome) {
                // Full-bleed start screen with bottom dock floating over meadow matching screenshot
                StartPageContent(
                    profile = uiState.profile,
                    bookmarks = uiState.bookmarks,
                    safeBrowsingEnabled = uiState.settings.safeBrowsingEnabled,
                    onOpenUrl = { viewModel.requestLoadUrl(it) },
                    onOpenProfile = { viewModel.setOverlay(BrowserOverlay.PROFILE_SCREEN) },
                    modifier = Modifier.fillMaxSize(),
                )

                BottomBrowserDock(
                    tabCount = uiState.tabs.size.coerceAtLeast(1),
                    currentUrl = "",
                    isHome = true,
                    isLoading = false,
                    progress = 0,
                    canGoBack = false,
                    onBackClick = { viewModel.goHomeInActiveTab() },
                    onNewTabClick = { viewModel.createNewTab() },
                    onCenterOrbClick = { viewModel.setOverlay(BrowserOverlay.PROFILE_SCREEN) },
                    onTabsClick = { viewModel.setOverlay(BrowserOverlay.TABS_SHEET) },
                    onAddressClick = { viewModel.setOverlay(BrowserOverlay.OMNIBOX_EDIT) },
                    onRefreshOrStop = { viewModel.setOverlay(BrowserOverlay.OMNIBOX_EDIT) },
                    onMenuClick = { viewModel.setOverlay(BrowserOverlay.MENU_SHEET) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .statusBarsPadding(),
                    ) {
                        key(activeTab.id) {
                            BrowserWebView(
                                tabId = activeTab.id,
                                targetUrl = activeTab.url,
                                approvedLoadRequest = uiState.approvedLoadRequest,
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
                    }

                    BottomBrowserDock(
                        tabCount = uiState.tabs.size.coerceAtLeast(1),
                        currentUrl = activeTab.url,
                        isHome = false,
                        isLoading = uiState.isWebLoading,
                        progress = uiState.webLoadingProgress,
                        canGoBack = true,
                        onBackClick = {
                            if (!webViewController.goBack()) {
                                viewModel.goHomeInActiveTab()
                            }
                        },
                        onNewTabClick = { viewModel.createNewTab() },
                        onCenterOrbClick = { viewModel.setOverlay(BrowserOverlay.PROFILE_SCREEN) },
                        onTabsClick = { viewModel.setOverlay(BrowserOverlay.TABS_SHEET) },
                        onAddressClick = { viewModel.setOverlay(BrowserOverlay.OMNIBOX_EDIT) },
                        onRefreshOrStop = {
                            if (uiState.isWebLoading) {
                                webViewController.stopLoading()
                            } else {
                                webViewController.reload()
                            }
                        },
                        onMenuClick = { viewModel.setOverlay(BrowserOverlay.MENU_SHEET) },
                    )
                }
            }

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
                onGoBack = { webViewController.goBack() },
                onGoForward = { webViewController.goForward() },
                onGoHome = { viewModel.goHomeInActiveTab() },
                onReload = { webViewController.reload() },
                onToggleBookmark = { viewModel.toggleCurrentPageBookmark() },
                onOpenProfile = { viewModel.setOverlay(BrowserOverlay.PROFILE_SCREEN) },
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

    val pendingWarning = uiState.pendingSafetyWarning
    if (pendingWarning != null) {
        DangerousResourceModal(
            warning = pendingWarning,
            onBackToSafety = { viewModel.dismissSafetyWarningBackToSafety() },
            onProceedAnyway = { viewModel.proceedAnywayOnSuspiciousUrl() },
        )
    }

    val legalDoc = uiState.legalDialog
    if (legalDoc != null) {
        LegalDocumentDialog(
            type = legalDoc,
            onDismiss = { viewModel.openLegalDialog(null) },
        )
    }
}

@Composable
private fun BottomBrowserDock(
    tabCount: Int,
    currentUrl: String,
    isHome: Boolean,
    isLoading: Boolean,
    progress: Int,
    canGoBack: Boolean,
    onBackClick: () -> Unit,
    onNewTabClick: () -> Unit,
    onCenterOrbClick: () -> Unit,
    onTabsClick: () -> Unit,
    onAddressClick: () -> Unit,
    onRefreshOrStop: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hostLabel = remember(currentUrl, isHome) {
        if (isHome || currentUrl.isBlank()) {
            "поиск или сайт"
        } else {
            UrlSafetyAnalyzer.extractHost(currentUrl) ?: currentUrl
        }
    }
    val isHttpInsecure = !isHome &&
        currentUrl.startsWith("http://", ignoreCase = true) &&
        !UrlSafetyAnalyzer.isUrlAllowedForSession(currentUrl)

    val dockContainerColor = if (isHome) {
        Color(0xFFDCEBC6).copy(alpha = 0.94f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val dockIconTint = if (isHome) Obsidian else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(dockContainerColor)
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 6.dp, start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isLoading && !isHome) {
            LinearProgressIndicator(
                progress = { (progress.coerceIn(5, 100)) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .height(2.5.dp)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(50)),
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // White rounded search / address pill matching right phone in screenshot
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .height(48.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onAddressClick)
                    .padding(horizontal = 14.dp)
                    .testTag("bottom_address_bar"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = when {
                        isHome -> Icons.Default.Search
                        isHttpInsecure -> Icons.Default.Warning
                        else -> Icons.Default.Lock
                    },
                    contentDescription = null,
                    tint = when {
                        isHome -> Color(0xFF334155)
                        isHttpInsecure -> WarningAmber
                        else -> SafeEmerald
                    },
                    modifier = Modifier.size(19.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = hostLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isHome) Color(0xFF64748B) else Obsidian,
                    fontWeight = if (isHome) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                if (!isHome) {
                    IconButton(
                        onClick = onRefreshOrStop,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("bottom_refresh_button"),
                    ) {
                        Icon(
                            imageVector = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                            contentDescription = if (isLoading) "Остановить" else "Обновить",
                            tint = Obsidian,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 5-icon bottom bar matching right phone in screenshot: < | + | (Orb) | [N] | ≡
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            IconButton(
                onClick = onBackClick,
                enabled = canGoBack,
                modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Назад",
                    tint = if (canGoBack) dockIconTint else dockIconTint.copy(alpha = 0.35f),
                    modifier = Modifier.size(26.dp),
                )
            }

            IconButton(
                onClick = onNewTabClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("bottom_new_tab_button"),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Новая вкладка",
                    tint = dockIconTint,
                    modifier = Modifier.size(24.dp),
                )
            }

            // Center glowing orb matching the screenshot (opens Profile ID K)
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(onClick = onCenterOrbClick)
                    .testTag("bottom_idk_orb"),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                            )
                        )
                        .border(2.5.dp, PureWhite.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(PureWhite.copy(alpha = 0.9f)),
                    )
                }
            }

            // Tab counter square [N]
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onTabsClick)
                    .testTag("bottom_tabs_button"),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(
                            width = 1.8.dp,
                            color = dockIconTint,
                            shape = RoundedCornerShape(6.dp),
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
                        color = dockIconTint,
                    )
                }
            }

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("bottom_menu_button"),
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Меню",
                    tint = dockIconTint,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}
