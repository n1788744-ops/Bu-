package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.core.ProxyApplyStatus
import com.example.core.ProxyTestResult
import com.example.data.local.ProxyConfigEntity
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramProxySheet(
    proxyEnabled: Boolean,
    telegramKeepAlive: Boolean,
    telegramWebVariant: String,
    proxyConfigs: List<ProxyConfigEntity>,
    activeProxy: ProxyConfigEntity?,
    proxyApplyStatus: ProxyApplyStatus,
    proxyTestResult: ProxyTestResult?,
    isTestingProxy: Boolean,
    onToggleProxyEnabled: (Boolean) -> Unit,
    onToggleKeepAlive: (Boolean) -> Unit,
    onChangeTelegramVariant: (String) -> Unit,
    onActivateConfig: (ProxyConfigEntity) -> Unit,
    onSaveCustomConfig: (
        name: String,
        protocol: String,
        host: String,
        port: Int,
        username: String,
        password: String,
        telegramOnly: Boolean,
    ) -> Unit,
    onDeleteConfig: (Long) -> Unit,
    onTestProxy: (host: String, port: Int, configId: Long?) -> Unit,
    onOpenTelegramWeb: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var customName by remember { mutableStateOf("") }
    var customProtocol by remember { mutableStateOf("SOCKS5") }
    var customHost by remember(activeProxy?.id) {
        mutableStateOf(
            if (activeProxy != null && activeProxy.protocol != "DIRECT") activeProxy.host else ""
        )
    }
    var customPort by remember(activeProxy?.id) {
        mutableStateOf(
            if (activeProxy != null && activeProxy.protocol != "DIRECT") activeProxy.port.toString() else "1080"
        )
    }
    var customUser by remember { mutableStateOf("") }
    var customPass by remember { mutableStateOf("") }
    var telegramOnly by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TelegramBlue),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Прокси для Telegram Web",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        val statusSubtitle = when (proxyApplyStatus) {
                            is ProxyApplyStatus.ActiveProxy -> "Активно: ${proxyApplyStatus.label}"
                            is ProxyApplyStatus.Unsupported -> proxyApplyStatus.reason
                            ProxyApplyStatus.DirectKeepAlive -> "Режим Keep-Alive (прямое соединение)"
                        }
                        Text(
                            text = statusSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть")
                }
            }

            // Master Switch & Stability Toggles
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Включить прокси-модуль",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Автоматическая маршрутизация для web.telegram.org",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = proxyEnabled,
                            onCheckedChange = onToggleProxyEnabled,
                            modifier = Modifier.testTag("proxy_master_switch"),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Стабильный WebSocket Keep-Alive",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Предотвращает обрыв соединения при переключении",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = telegramKeepAlive,
                            onCheckedChange = onToggleKeepAlive,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Версия Telegram Web",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = telegramWebVariant == "webk",
                            onClick = { onChangeTelegramVariant("webk") },
                            label = { Text("Web K (Рекомендуется)") },
                        )
                        FilterChip(
                            selected = telegramWebVariant == "weba",
                            onClick = { onChangeTelegramVariant("weba") },
                            label = { Text("Web A") },
                        )
                    }
                }
            }

            // Saved Proxy Profiles
            Text(
                text = "Профили подключения",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            proxyConfigs.forEach { cfg ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (cfg.isActive) TelegramBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (cfg.isActive) 1.5.dp else 1.dp,
                        color = if (cfg.isActive) TelegramBlue else MaterialTheme.colorScheme.outline,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onActivateConfig(cfg) },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (cfg.isActive) TelegramBlue else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cfg.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            val detail = buildString {
                                append("${cfg.protocol} • ${cfg.host}:${cfg.port}")
                                if (cfg.lastLatencyMs != null) {
                                    append(" • ${cfg.lastLatencyMs} мс")
                                }
                            }
                            Text(
                                text = detail,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (cfg.id > 3L) {
                            IconButton(onClick = { onDeleteConfig(cfg.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Удалить",
                                    tint = DangerCoral,
                                )
                            }
                        }
                    }
                }
            }

            // Custom Proxy Server Form
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Добавить / настроить свой прокси",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SOCKS5", "HTTPS", "HTTP").forEach { proto ->
                            FilterChip(
                                selected = customProtocol == proto,
                                onClick = { customProtocol = proto },
                                label = { Text(proto) },
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Название (необязательно)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = customHost,
                            onValueChange = { customHost = it },
                            label = { Text("Сервер (IP/домен)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(2f)
                                .testTag("proxy_host_input"),
                        )
                        OutlinedTextField(
                            value = customPort,
                            onValueChange = { customPort = it.filter { ch -> ch.isDigit() }.take(5) },
                            label = { Text("Порт") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("proxy_port_input"),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = customUser,
                            onValueChange = { customUser = it },
                            label = { Text("Логин (опц.)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = customPass,
                            onValueChange = { customPass = it },
                            label = { Text("Пароль (опц.)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Только для Telegram Web",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Switch(
                            checked = telegramOnly,
                            onCheckedChange = { telegramOnly = it },
                        )
                    }

                    if (proxyTestResult != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (proxyTestResult.reachable) {
                                SafeEmerald.copy(alpha = 0.14f)
                            } else {
                                DangerCoral.copy(alpha = 0.12f)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = proxyTestResult.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (proxyTestResult.reachable) SafeEmerald else DangerCoral,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val parsedPort = customPort.toIntOrNull() ?: 0
                        OutlinedButton(
                            onClick = {
                                val hostToTest = customHost.ifBlank { activeProxy?.host ?: "web.telegram.org" }
                                val portToTest = if (customHost.isBlank()) (activeProxy?.port ?: 443) else parsedPort
                                onTestProxy(hostToTest, portToTest, activeProxy?.id)
                            },
                            enabled = !isTestingProxy,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            if (isTestingProxy) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Speed,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Проверить")
                        }

                        Button(
                            onClick = {
                                if (customHost.isNotBlank() && parsedPort in 1..65535) {
                                    onSaveCustomConfig(
                                        customName,
                                        customProtocol,
                                        customHost,
                                        parsedPort,
                                        customUser,
                                        customPass,
                                        telegramOnly,
                                    )
                                }
                            },
                            enabled = customHost.isNotBlank() && (customPort.toIntOrNull() ?: 0) in 1..65535,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("proxy_save_button"),
                        ) {
                            Text("Сохранить")
                        }
                    }
                }
            }

            Button(
                onClick = onOpenTelegramWeb,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TelegramBlue,
                    contentColor = PureWhite,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("proxy_open_telegram_button"),
            ) {
                Text(
                    text = "Открыть Telegram Web",
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
