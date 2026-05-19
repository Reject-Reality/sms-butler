package com.smsbutler.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // ===== 权限状态 =====
            Text("权限状态", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.notificationAccess)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!state.notificationAccess) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            if (state.notificationAccess) "✅ 通知监听权限已开启" else "⚠️ 通知监听权限未开启",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if (!state.notificationAccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.openNotificationSettings() }) {
                            Text("前往开启权限")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 我的手机号 =====
            Text("我的手机号", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "用于区分多卡收到的短信，设置后在记录中会标注接收号码",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 已添加的号码列表
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (state.myPhoneNumbers.isEmpty()) {
                        Text(
                            "尚未添加任何手机号",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        state.myPhoneNumbers.forEach { number ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(number, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                IconButton(onClick = { viewModel.removePhoneNumber(number) }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            // 添加新号码
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.newPhoneNumber,
                    onValueChange = { viewModel.onNewPhoneNumberChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入手机号，如 138xxxx1234") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.addPhoneNumber() },
                    enabled = state.newPhoneNumber.isNotBlank()
                ) {
                    Text("添加")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 隐私 =====
            Text("隐私设置", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("记录短信内容", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "开启后将同时记录短信正文",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.recordContent,
                        onCheckedChange = { viewModel.toggleRecordContent(it) }
                    )
                }
            }
        }
    }
}
