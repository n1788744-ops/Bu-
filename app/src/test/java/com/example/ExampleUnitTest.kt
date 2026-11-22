package com.example

import com.example.core.SafetyVerdict
import com.example.core.UrlSafetyAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun safe_urls_and_sitefortress_pass_without_warning() {
        assertTrue(UrlSafetyAnalyzer.analyzeUrl("http://sitefortress.ru/") is SafetyVerdict.Safe)
        assertTrue(UrlSafetyAnalyzer.analyzeUrl("https://ya.ru/") is SafetyVerdict.Safe)
        assertEquals("http://sitefortress.ru", UrlSafetyAnalyzer.resolveOmniboxInput("sitefortress.ru", "Yandex"))
    }

    @Test
    fun suspicious_urls_trigger_warning_and_can_be_bypassed_for_session() {
        val httpVerdict = UrlSafetyAnalyzer.analyzeUrl("http://suspicious-login.example")
        assertTrue(httpVerdict is SafetyVerdict.Suspicious)

        val phishingVerdict = UrlSafetyAnalyzer.analyzeUrl("https://yandx-prize.xyz/claim")
        assertTrue(phishingVerdict is SafetyVerdict.Suspicious)

        UrlSafetyAnalyzer.allowForSession("http://suspicious-login.example")
        assertTrue(UrlSafetyAnalyzer.analyzeUrl("http://suspicious-login.example") is SafetyVerdict.Safe)
    }
}
