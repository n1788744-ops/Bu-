package com.example.core

import android.util.Log
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.example.data.local.ProxyConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executor

sealed interface ProxyApplyStatus {
    data object DirectKeepAlive : ProxyApplyStatus
    data class ActiveProxy(val label: String) : ProxyApplyStatus
    data class Unsupported(val reason: String) : ProxyApplyStatus
}

data class ProxyTestResult(
    val reachable: Boolean,
    val latencyMs: Int?,
    val message: String,
)

object TelegramProxyManager {
    private const val TAG = "TelegramProxyManager"
    private val directExecutor = Executor { runnable -> runnable.run() }

    private var lastAppliedSignature: String? = null

    fun applyForUrl(
        url: String,
        proxyEnabled: Boolean,
        activeConfig: ProxyConfigEntity?,
    ): ProxyApplyStatus {
        val isTelegram = UrlSafetyAnalyzer.isTelegramHost(url)
        val shouldUseProxy = proxyEnabled &&
            activeConfig != null &&
            activeConfig.protocol != "DIRECT" &&
            activeConfig.host.isNotBlank() &&
            activeConfig.port in 1..65535 &&
            (!activeConfig.telegramOnly || isTelegram)

        if (!shouldUseProxy || activeConfig == null) {
            clearProxyIfNeeded()
            return ProxyApplyStatus.DirectKeepAlive
        }

        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            return ProxyApplyStatus.Unsupported("Системный WebView не поддерживает Proxy Override")
        }

        val scheme = when (activeConfig.protocol.uppercase()) {
            "SOCKS5", "SOCKS" -> "socks5"
            "HTTPS" -> "https"
            else -> "http"
        }
        val proxyRule = "$scheme://${activeConfig.host.trim()}:${activeConfig.port}"
        val signature = "$proxyRule|${activeConfig.telegramOnly}"
        if (lastAppliedSignature == signature) {
            return ProxyApplyStatus.ActiveProxy("${activeConfig.protocol} ${activeConfig.host}:${activeConfig.port}")
        }

        return try {
            val builder = ProxyConfig.Builder()
                .addProxyRule(proxyRule)
                .addDirect() // Automatic fallback if custom proxy drops packets
            if (activeConfig.telegramOnly) {
                builder.addBypassRule("<-loopback>")
            }
            ProxyController.getInstance().setProxyOverride(
                builder.build(),
                directExecutor,
            ) {
                Log.d(TAG, "Proxy applied: $proxyRule")
            }
            lastAppliedSignature = signature
            ProxyApplyStatus.ActiveProxy("${activeConfig.protocol} ${activeConfig.host}:${activeConfig.port}")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to apply proxy", t)
            ProxyApplyStatus.Unsupported(t.localizedMessage ?: "Ошибка настройки прокси")
        }
    }

    fun clearProxyIfNeeded() {
        if (lastAppliedSignature == null) return
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            runCatching {
                ProxyController.getInstance().clearProxyOverride(directExecutor) {}
            }
        }
        lastAppliedSignature = null
    }

    suspend fun testProxyReachability(host: String, port: Int): ProxyTestResult =
        withContext(Dispatchers.IO) {
            val cleanHost = host.trim()
            if (cleanHost.isBlank() || port !in 1..65535) {
                return@withContext ProxyTestResult(
                    reachable = false,
                    latencyMs = null,
                    message = "Укажите корректный хост и порт (1–65535)",
                )
            }
            val start = System.currentTimeMillis()
            return@withContext try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(cleanHost, port), 3500)
                }
                val elapsed = (System.currentTimeMillis() - start).toInt().coerceAtLeast(1)
                ProxyTestResult(
                    reachable = true,
                    latencyMs = elapsed,
                    message = "Соединение стабильно • отклик $elapsed мс",
                )
            } catch (e: Exception) {
                ProxyTestResult(
                    reachable = false,
                    latencyMs = null,
                    message = "Не удалось подключиться: ${e.localizedMessage ?: "тайм-аут"}",
                )
            }
        }

    const val TELEGRAM_KEEP_ALIVE_JS = """
        (function() {
            if (window.__kBrowserTgPatched) return;
            window.__kBrowserTgPatched = true;
            try {
                Object.defineProperty(document, 'hidden', { get: function() { return false; } });
                Object.defineProperty(document, 'visibilityState', { get: function() { return 'visible'; } });
                setInterval(function() {
                    window.dispatchEvent(new Event('online'));
                }, 20000);
            } catch (e) {}
        })();
    """
}
