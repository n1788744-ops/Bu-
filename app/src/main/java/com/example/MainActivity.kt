package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.LegalDocumentType
import com.example.ui.BrowserViewModel
import com.example.ui.screens.BrowserMainScreen
import com.example.ui.screens.IdKLoginScreen
import com.example.ui.screens.LegalDocumentDialog
import com.example.ui.theme.KBrowserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BrowserViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val authState by viewModel.authState.collectAsStateWithLifecycle()

            val useDarkTheme = when (uiState.settings.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            KBrowserTheme(darkTheme = useDarkTheme) {
                if (!uiState.isInitialized) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Crossfade(
                        targetState = uiState.settings.isAuthenticated,
                        label = "auth_to_browser_crossfade",
                    ) { isAuthenticated ->
                        if (!isAuthenticated) {
                            IdKLoginScreen(
                                authState = authState,
                                onPhoneDigitsChange = viewModel::onPhoneDigitsChange,
                                onEmailChange = viewModel::onEmailInputChange,
                                onToggleInputMode = viewModel::toggleAuthInputMode,
                                onConsentChange = viewModel::onConsentCheckedChange,
                                onSubmitIdentifier = viewModel::submitIdentifierStep,
                                onOtpChange = viewModel::onOtpInputChange,
                                onFillDemoOtp = viewModel::fillDemoOtp,
                                onBackToIdentifier = viewModel::backToIdentifierStep,
                                onVerifyOtp = viewModel::verifyOtpAndSignIn,
                                onOpenLegal = { doc: LegalDocumentType -> viewModel.openLegalDialog(doc) },
                            )

                            val legalDoc = uiState.legalDialog
                            if (legalDoc != null) {
                                LegalDocumentDialog(
                                    type = legalDoc,
                                    onDismiss = { viewModel.openLegalDialog(null) },
                                )
                            }
                        } else {
                            BrowserMainScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                            )
                        }
                    }
                }
            }
        }
    }
}
