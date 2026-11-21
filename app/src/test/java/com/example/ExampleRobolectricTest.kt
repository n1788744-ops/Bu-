package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.SafetyVerdict
import com.example.core.UrlSafetyAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name and support email from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Браузер K", context.getString(R.string.app_name))
        assertEquals("support@900.360org.ru", context.getString(R.string.support_email))
    }

    @Test
    fun `safe urls pass without warning`() {
        assertTrue(UrlSafetyAnalyzer.analyzeUrl("https://web.telegram.org/k/") is SafetyVerdict.Safe)
        assertTrue(UrlSafetyAnalyzer.analyzeUrl("https://ya.ru/") is SafetyVerdict.Safe)
    }

    @Test
    fun `suspicious urls trigger warning and can be bypassed for session`() {
        val httpVerdict = UrlSafetyAnalyzer.analyzeUrl("http://suspicious-login.example")
        assertTrue(httpVerdict is SafetyVerdict.Suspicious)

        val phishingVerdict = UrlSafetyAnalyzer.analyzeUrl("https://te1egram-gift.xyz/claim")
        assertTrue(phishingVerdict is SafetyVerdict.Suspicious)

        UrlSafetyAnalyzer.allowForSession("http://suspicious-login.example")
        assertTrue(UrlSafetyAnalyzer.analyzeUrl("http://suspicious-login.example") is SafetyVerdict.Safe)
    }
}
