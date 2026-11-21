package com.example.core

import android.net.Uri
import java.net.URLEncoder
import java.util.Locale

enum class RiskLevel {
    MEDIUM,
    HIGH,
}

sealed interface SafetyVerdict {
    data object Safe : SafetyVerdict
    data class Suspicious(
        val url: String,
        val host: String,
        val riskLevel: RiskLevel,
        val reasons: List<String>,
    ) : SafetyVerdict
}

object UrlSafetyAnalyzer {
    private val sessionAllowedUrls = mutableSetOf<String>()
    private val sessionAllowedHosts = mutableSetOf<String>()

    private val ipv4Regex = Regex("""^\d{1,3}(\.\d{1,3}){3}$""")
    private val domainLikeRegex = Regex("""^[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(/.*)?$""")

    private val highRiskTlds = setOf(
        "tk", "ml", "ga", "cf", "gq", "zip", "mov", "click", "loan", "buzz", "work", "xyz"
    )

    private val typosquattingTokens = listOf(
        "te1egram", "teleram", "telegarm", "telegrarn", "telegram-gift", "telegram-free",
        "yandx", "yandekc", "yandex-prize", "g00gle", "go0gle", "paypa1", "sberbanck",
        "sber-bonus", "tink0ff", "gosus1ugi", "vk-login", "vk-bonus", "steam-gift",
    )

    private val suspiciousKeywords = listOf(
        "airdrop", "free-ton", "claim-bonus", "wallet-connect", "seed-phrase",
        "verify-account", "urgent-login", "password-confirm", "phish", "malware",
        "scam", "virus", "hack-tool", ".apk",
    )

    fun allowForSession(url: String) {
        sessionAllowedUrls.add(url)
        extractHost(url)?.let { sessionAllowedHosts.add(it) }
    }

    fun isUrlAllowedForSession(url: String): Boolean {
        if (sessionAllowedUrls.contains(url)) return true
        val host = extractHost(url)
        return host != null && sessionAllowedHosts.contains(host)
    }

    fun resolveOmniboxInput(rawInput: String, searchEngine: String): String {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return ""
        val lower = trimmed.lowercase(Locale.ROOT)
        if (lower.startsWith("http://") ||
            lower.startsWith("https://") ||
            lower.startsWith("javascript:") ||
            lower.startsWith("data:")
        ) {
            return trimmed
        }
        val firstSegment = trimmed.substringBefore("/")
        val looksLikeIp = ipv4Regex.matches(firstSegment.substringBefore(":"))
        val looksLikeDomain = !trimmed.contains(" ") &&
            (domainLikeRegex.matches(trimmed) || looksLikeIp || firstSegment.startsWith("localhost"))

        if (looksLikeDomain) {
            return "https://$trimmed"
        }

        val encoded = URLEncoder.encode(trimmed, "UTF-8")
        return when (searchEngine) {
            "Google" -> "https://www.google.com/search?q=$encoded"
            "DuckDuckGo" -> "https://duckduckgo.com/?q=$encoded"
            else -> "https://ya.ru/search/?text=$encoded"
        }
    }

    fun analyzeUrl(rawUrl: String, safeBrowsingEnabled: Boolean = true): SafetyVerdict {
        if (!safeBrowsingEnabled) return SafetyVerdict.Safe
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return SafetyVerdict.Safe
        if (isUrlAllowedForSession(trimmed)) return SafetyVerdict.Safe

        val lower = trimmed.lowercase(Locale.ROOT)
        val reasons = mutableListOf<String>()
        var isHighRisk = false

        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("file:")) {
            reasons.add("Потенциально опасная схема выполнения кода или локального файла.")
            isHighRisk = true
        }

        val uri = runCatching { Uri.parse(trimmed) }.getOrNull()
        val scheme = uri?.scheme?.lowercase(Locale.ROOT) ?: ""
        val host = uri?.host?.lowercase(Locale.ROOT)?.removePrefix("www.") ?: ""

        if (scheme == "http" && host != "localhost" && host != "127.0.0.1") {
            reasons.add("Незащищённое соединение HTTP без шифрования SSL/TLS.")
        }

        if (host.isNotBlank()) {
            if (ipv4Regex.matches(host) || host.contains(":")) {
                reasons.add("Прямой переход по IP-адресу вместо доверенного доменного имени.")
                isHighRisk = true
            }
            if (host.contains("xn--")) {
                reasons.add("Обнаружен Punycode-домен (xn--), возможна подмена похожих символов.")
                isHighRisk = true
            }
            val tld = host.substringAfterLast('.', "")
            if (tld in highRiskTlds) {
                reasons.add("Доменная зона .$tld часто используется для подозрительных ресурсов.")
            }
            if (typosquattingTokens.any { host.contains(it) }) {
                reasons.add("Адрес напоминает подделку известного сервиса (подозрение на фишинг).")
                isHighRisk = true
            }
        }

        if (suspiciousKeywords.any { lower.contains(it) }) {
            reasons.add("В ссылке обнаружены признаки фишинга или опасной загрузки (.apk / кража данных).")
            isHighRisk = true
        }

        if (reasons.isEmpty()) {
            return SafetyVerdict.Safe
        }

        return SafetyVerdict.Suspicious(
            url = trimmed,
            host = host.ifBlank { trimmed.take(40) },
            riskLevel = if (isHighRisk) RiskLevel.HIGH else RiskLevel.MEDIUM,
            reasons = reasons.distinct(),
        )
    }

    fun extractHost(url: String): String? {
        return runCatching {
            Uri.parse(url).host?.lowercase(Locale.ROOT)?.removePrefix("www.")
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    fun isTelegramHost(url: String): Boolean {
        val host = extractHost(url) ?: return false
        return host == "web.telegram.org" ||
            host.endsWith(".telegram.org") ||
            host == "telegram.org" ||
            host == "t.me" ||
            host == "telegra.ph"
    }
}
