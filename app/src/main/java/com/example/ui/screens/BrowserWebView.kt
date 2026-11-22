package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Stable
class BrowserWebViewController {
    internal var webView: WebView? = null

    fun loadUrl(url: String) {
        if (url.isNotBlank()) {
            webView?.loadUrl(url)
        }
    }

    fun reload() {
        webView?.reload()
    }

    fun stopLoading() {
        webView?.stopLoading()
    }

    fun goBack(): Boolean {
        val wv = webView ?: return false
        return if (wv.canGoBack()) {
            wv.goBack()
            true
        } else {
            false
        }
    }

    fun goForward(): Boolean {
        val wv = webView ?: return false
        return if (wv.canGoForward()) {
            wv.goForward()
            true
        } else {
            false
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tabId: Long,
    targetUrl: String,
    approvedLoadRequest: Pair<Long, String>?,
    controller: BrowserWebViewController,
    onInterceptNavigation: (String) -> Boolean,
    onPageUpdated: (
        title: String?,
        url: String?,
        progress: Int,
        isLoading: Boolean,
        canGoBack: Boolean,
        canGoForward: Boolean,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestIntercept by rememberUpdatedState(onInterceptNavigation)
    val latestPageUpdated by rememberUpdatedState(onPageUpdated)

    var lastHandledNonce by remember(tabId) { mutableStateOf(-1L) }

    LaunchedEffect(approvedLoadRequest) {
        val req = approvedLoadRequest ?: return@LaunchedEffect
        if (req.first != lastHandledNonce) {
            lastHandledNonce = req.first
            controller.loadUrl(req.second)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    allowFileAccess = false
                    allowContentAccess = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    javaScriptCanOpenWindowsAutomatically = false
                    mediaPlaybackRequiresUserGesture = false
                    userAgentString = userAgentString.replace("; wv)", ")")
                }

                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        latestPageUpdated(
                            view?.title,
                            view?.url,
                            newProgress,
                            newProgress < 100,
                            view?.canGoBack() ?: false,
                            view?.canGoForward() ?: false,
                        )
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        latestPageUpdated(
                            title,
                            view?.url,
                            view?.progress ?: 100,
                            (view?.progress ?: 100) < 100,
                            view?.canGoBack() ?: false,
                            view?.canGoForward() ?: false,
                        )
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        val url = request?.url?.toString() ?: return false
                        return latestIntercept(url)
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        latestPageUpdated(
                            view?.title,
                            url,
                            12,
                            true,
                            view?.canGoBack() ?: false,
                            view?.canGoForward() ?: false,
                        )
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        latestPageUpdated(
                            view?.title,
                            url,
                            100,
                            false,
                            view?.canGoBack() ?: false,
                            view?.canGoForward() ?: false,
                        )
                    }
                }

                controller.webView = this
                if (targetUrl.isNotBlank()) {
                    loadUrl(targetUrl)
                }
            }
        },
        update = { webView ->
            controller.webView = webView
            val currentLoaded = webView.url
            if (targetUrl.isNotBlank() && currentLoaded.isNullOrBlank()) {
                webView.loadUrl(targetUrl)
            }
        },
    )

    DisposableEffect(Unit) {
        onDispose {
            controller.webView?.stopLoading()
            controller.webView?.destroy()
            controller.webView = null
        }
    }
}
