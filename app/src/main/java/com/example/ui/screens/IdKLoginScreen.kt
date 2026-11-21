package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LegalContent
import com.example.core.LegalDocumentType
import com.example.ui.AuthInputMode
import com.example.ui.AuthStep
import com.example.ui.AuthUiState
import com.example.ui.BrowserViewModel
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DangerCoralSoft
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.Obsidian
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SafeEmerald

@Composable
fun IdKLoginScreen(
    authState: AuthUiState,
    onPhoneDigitsChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onToggleInputMode: () -> Unit,
    onConsentChange: (Boolean) -> Unit,
    onSubmitIdentifier: () -> Unit,
    onOtpChange: (String) -> Unit,
    onFillDemoOtp: () -> Unit,
    onBackToIdentifier: () -> Unit,
    onVerifyOtp: () -> Unit,
    onOpenLegal: (LegalDocumentType) -> Unit,
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Atmospheric blurred nature-inspired backdrop similar to Yandex ID screenshot
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4F6D7A),
                        Color(0xFF3E5C69),
                        Color(0xFF2D4654),
                        Color(0xFF1F323E),
                    )
                )
            )
            // Soft misty sun & foliage glows
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66D4A373), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.16f),
                    radius = size.width * 0.7f,
                ),
                center = Offset(size.width * 0.85f, size.height * 0.16f),
                radius = size.width * 0.7f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x5573A580), Color.Transparent),
                    center = Offset(size.width * 0.12f, size.height * 0.78f),
                    radius = size.width * 0.65f,
                ),
                center = Offset(size.width * 0.12f, size.height * 0.78f),
                radius = size.width * 0.65f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x55C98E4B), Color.Transparent),
                    center = Offset(size.width * 0.75f, size.height * 0.88f),
                    radius = size.width * 0.5f,
                ),
                center = Offset(size.width * 0.75f, size.height * 0.88f),
                radius = size.width * 0.5f,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                shape = RoundedCornerShape(28.dp),
                color = PureWhite,
                shadowElevation = 18.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Brand Logo matching "Яндекс [ID]" -> "ID [K]" / "Браузер K [ID]"
                    IdKBrandHeader()

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (authState.step == AuthStep.ENTER_IDENTIFIER) {
                            "Войдите с ID K"
                        } else {
                            "Подтверждение входа"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = Obsidian,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedContent(
                        targetState = authState.step,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "auth_step_transition",
                    ) { step ->
                        if (step == AuthStep.ENTER_IDENTIFIER) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                if (authState.inputMode == AuthInputMode.PHONE) {
                                    RussianPhoneField(
                                        digits = authState.phoneDigits,
                                        onDigitsChange = onPhoneDigitsChange,
                                        onDone = onSubmitIdentifier,
                                    )
                                } else {
                                    EmailIdentifierField(
                                        email = authState.emailInput,
                                        onEmailChange = onEmailChange,
                                        onDone = onSubmitIdentifier,
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                ConsentCheckboxRow(
                                    checked = authState.consentAccepted,
                                    onCheckedChange = onConsentChange,
                                    onOpenLegal = onOpenLegal,
                                )

                                AnimatedVisibility(visible = authState.errorMessage != null) {
                                    ErrorNoticePill(message = authState.errorMessage.orEmpty())
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = onSubmitIdentifier,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("idk_next_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Obsidian,
                                        contentColor = PureWhite,
                                    ),
                                ) {
                                    Text(
                                        text = "Далее",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = onToggleInputMode,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("idk_alt_method_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE0E4EC)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = PureWhite,
                                        contentColor = Obsidian,
                                    ),
                                ) {
                                    Text(
                                        text = if (authState.inputMode == AuthInputMode.PHONE) {
                                            "Другой способ входа"
                                        } else {
                                            "Войти по номеру телефона"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        } else {
                            // Step 2: 4-digit OTP code verification
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                val targetLabel = if (authState.inputMode == AuthInputMode.PHONE) {
                                    BrowserViewModel.formatRussianPhone(authState.phoneDigits)
                                } else {
                                    authState.emailInput.trim()
                                }

                                Text(
                                    text = "Мы отправили код подтверждения на\n$targetLabel",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF5D6678),
                                    textAlign = TextAlign.Center,
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Instant test-friendly notification card with 1-tap autofill
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFEFF8FF),
                                    border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Уведомление ID K",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color(0xFF0369A1),
                                            )
                                            Text(
                                                text = "Код входа: ${authState.generatedOtp}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = JetBrainsMono,
                                                ),
                                                color = Obsidian,
                                            )
                                        }
                                        TextButton(
                                            onClick = onFillDemoOtp,
                                            modifier = Modifier.testTag("idk_fill_otp_button"),
                                        ) {
                                            Text("Вставить", color = Color(0xFF0284C7))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OtpInputField(
                                    otp = authState.otpInput,
                                    onOtpChange = onOtpChange,
                                    onDone = onVerifyOtp,
                                )

                                AnimatedVisibility(visible = authState.errorMessage != null) {
                                    ErrorNoticePill(message = authState.errorMessage.orEmpty())
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = onVerifyOtp,
                                    enabled = !authState.isSubmitting,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("idk_verify_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Obsidian,
                                        contentColor = PureWhite,
                                    ),
                                ) {
                                    Text(
                                        text = "Войти и создать профиль",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(
                                    onClick = onBackToIdentifier,
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF5D6678),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Изменить данные входа",
                                        color = Color(0xFF5D6678),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer matching screenshot ("ID K — ключ для всех сервисов" / "Узнать больше")
            Text(
                text = "ID K — ключ для всех сервисов",
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = { onOpenLegal(LegalDocumentType.ABOUT_ID_K) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.testTag("idk_learn_more_button"),
            ) {
                Text(
                    text = "Узнать больше",
                    style = MaterialTheme.typography.titleMedium,
                    color = PureWhite.copy(alpha = 0.92f),
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${LegalContent.SUPPORT_EMAIL}")
                            putExtra(Intent.EXTRA_SUBJECT, "Поддержка ID K / Браузер K")
                        }
                        runCatching { context.startActivity(intent) }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.SupportAgent,
                    contentDescription = "Техподдержка",
                    tint = PureWhite.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Техподдержка: ${LegalContent.SUPPORT_EMAIL}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PureWhite.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun IdKBrandHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "ID",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Obsidian,
            letterSpacing = (-0.5).sp,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Obsidian)
                .padding(horizontal = 10.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "K",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = PureWhite,
            )
        }
    }
}

@Composable
private fun RussianPhoneField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    val formattedPreview = buildPhoneVisual(digits)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, Obsidian, RoundedCornerShape(18.dp))
            .background(PureWhite)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Circular Russian Flag matching screenshot
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F5F8)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            ) {
                val h = size.height / 3f
                drawRect(Color.White, Offset(0f, 0f), androidx.compose.ui.geometry.Size(size.width, h))
                drawRect(Color(0xFF0039A6), Offset(0f, h), androidx.compose.ui.geometry.Size(size.width, h))
                drawRect(Color(0xFFD52B1E), Offset(0f, h * 2f), androidx.compose.ui.geometry.Size(size.width, h))
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "+7",
            style = MaterialTheme.typography.titleMedium,
            color = Obsidian,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (digits.isEmpty()) {
                Text(
                    text = "(000) 000-00-00",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF9AA4B8),
                )
            }
            BasicTextField(
                value = TextFieldValue(
                    text = digits,
                    selection = TextRange(digits.length),
                ),
                onValueChange = { onDigitsChange(it.text) },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (digits.isEmpty()) Color.Transparent else Obsidian,
                ),
                cursorBrush = SolidColor(Obsidian),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                visualTransformation = PhoneMaskTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("idk_phone_input"),
            )
            // Keep unused helper reference suppressed cleanly
            if (false) Text(formattedPreview)
        }

        if (digits.isNotEmpty()) {
            IconButton(
                onClick = { onDigitsChange("") },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("idk_phone_clear"),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Очистить номер",
                    tint = Obsidian,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private class PhoneMaskTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.take(10)
        val out = buildPhoneVisual(raw)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, raw.length)
                return when {
                    o == 0 -> 0
                    o <= 3 -> o + 1
                    o <= 6 -> o + 3
                    o <= 8 -> o + 4
                    else -> o + 5
                }.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return raw.length
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

private fun buildPhoneVisual(digits: String): String {
    if (digits.isEmpty()) return ""
    val sb = StringBuilder()
    sb.append("(")
    for (i in digits.indices) {
        if (i == 3) sb.append(") ")
        if (i == 6) sb.append("-")
        if (i == 8) sb.append("-")
        sb.append(digits[i])
    }
    return sb.toString()
}

@Composable
private fun EmailIdentifierField(
    email: String,
    onEmailChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, Obsidian, RoundedCornerShape(18.dp))
            .background(PureWhite)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = null,
            tint = Color(0xFF5D6678),
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (email.isEmpty()) {
                Text(
                    text = "Логин или e-mail ID K",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF9AA4B8),
                )
            }
            BasicTextField(
                value = email,
                onValueChange = onEmailChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Obsidian,
                ),
                cursorBrush = SolidColor(Obsidian),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("idk_email_input"),
            )
        }
        if (email.isNotEmpty()) {
            IconButton(
                onClick = { onEmailChange("") },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Очистить",
                    tint = Obsidian,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun OtpInputField(
    otp: String,
    onOtpChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, Obsidian, RoundedCornerShape(18.dp))
            .background(PureWhite)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = SafeEmerald,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (otp.isEmpty()) {
                Text(
                    text = "4-значный код",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF9AA4B8),
                )
            }
            BasicTextField(
                value = otp,
                onValueChange = onOtpChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = JetBrainsMono,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    color = Obsidian,
                ),
                cursorBrush = SolidColor(Obsidian),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("idk_otp_input"),
            )
        }
    }
}

@Composable
private fun ConsentCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onOpenLegal: (LegalDocumentType) -> Unit,
) {
    val legalText = buildAnnotatedString {
        append("Согласен на ")
        withLink(
            LinkAnnotation.Clickable("pd") {
                onOpenLegal(LegalDocumentType.PERSONAL_DATA)
            }
        ) {
            withStyle(
                SpanStyle(
                    color = Color(0xFF0284C7),
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Medium,
                )
            ) {
                append("обработку персональных данных")
            }
        }
        append(", принимаю ")
        withLink(
            LinkAnnotation.Clickable("privacy") {
                onOpenLegal(LegalDocumentType.PRIVACY_POLICY)
            }
        ) {
            withStyle(
                SpanStyle(
                    color = Color(0xFF0284C7),
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Medium,
                )
            ) {
                append("Политику конфиденциальности")
            }
        }
        append(" и ")
        withLink(
            LinkAnnotation.Clickable("license") {
                onOpenLegal(LegalDocumentType.LICENSE_AGREEMENT)
            }
        ) {
            withStyle(
                SpanStyle(
                    color = Color(0xFF0284C7),
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Medium,
                )
            ) {
                append("Лицензионное соглашение")
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Obsidian,
                checkmarkColor = PureWhite,
                uncheckedColor = Color(0xFF7B8499),
            ),
            modifier = Modifier
                .padding(top = 0.dp)
                .testTag("idk_consent_checkbox"),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = legalText,
            style = MaterialTheme.typography.bodySmall.copy(
                lineHeight = 17.sp,
                color = Color(0xFF475569),
            ),
            modifier = Modifier
                .weight(1f)
                .padding(top = 10.dp),
        )
    }
}

@Composable
private fun ErrorNoticePill(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        shape = RoundedCornerShape(12.dp),
        color = DangerCoralSoft,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF991B1B),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}
