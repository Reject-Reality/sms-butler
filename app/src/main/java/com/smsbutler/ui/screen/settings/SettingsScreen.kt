package com.smsbutler.ui.screen.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.smsbutler.permissions.SmsPermissions
import com.smsbutler.ui.components.ButlerBackground
import com.smsbutler.ui.components.ScreenHeader
import com.smsbutler.ui.components.SectionTitle

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.refreshPermissionState()
        val deniedPermanently = activity != null && SmsPermissions.requiredRuntimePermissions.any { permission ->
            permissions[permission] == false &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
        if (deniedPermanently && !SmsPermissions.canReceiveSms(context)) {
            viewModel.openAppPermissionSettings()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        ButlerBackground {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ScreenHeader(
                        title = "设置",
                        subtitle = "管理短信接收权限、接收号码和隐私记录方式。",
                        icon = Icons.Outlined.Settings
                    )
                }

                item { SectionTitle(text = "权限") }
                item {
                    val permissionsReady = state.smsReceivePermission && state.smsReadPermission && state.phoneNumberPermission
                    SettingsCard(
                        icon = if (permissionsReady) Icons.Outlined.CheckCircle else Icons.Outlined.NotificationsOff,
                        title = if (state.smsReceivePermission) "短信接收权限已开启" else "短信接收权限未开启",
                        description = if (permissionsReady) {
                            "应用可以记录真实短信，漏收时会从收件箱补同步，并尽量识别接收手机号。"
                        } else if (state.smsReceivePermission && !state.smsReadPermission) {
                            "已能接收新短信；缺少短信读取权限时，部分漏收验证码无法从收件箱补同步。"
                        } else if (state.smsReceivePermission) {
                            "已能接收短信；缺少手机号读取权限时，多卡接收号可能无法自动识别。"
                        } else {
                            "开启后才能记录真实收到的短信。读取短信用于补漏，手机号权限用于多卡匹配。"
                        },
                        modifier = Modifier.padding(horizontal = 18.dp),
                        isError = !state.smsReceivePermission
                    ) {
                        if (!permissionsReady) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(SmsPermissions.requiredRuntimePermissions)
                                },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Outlined.Warning, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("开启短信权限")
                            }
                        }
                    }
                }

                item {
                    SettingsCard(
                        icon = if (state.notificationListenerPermission) Icons.Outlined.CheckCircle else Icons.Outlined.NotificationsOff,
                        title = if (state.notificationListenerPermission) "通知兜底已开启" else "通知兜底未开启",
                        description = if (state.notificationListenerPermission) {
                            "当系统短信广播拿不到验证码时，会从短信通知中严格过滤后补记录。"
                        } else {
                            "你的设备可能不开放标准短信入口。开启后可恢复验证码通知兜底，并会跳过正在运行类通知。"
                        },
                        modifier = Modifier.padding(horizontal = 18.dp),
                        isError = !state.notificationListenerPermission
                    ) {
                        if (!state.notificationListenerPermission) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.openNotificationListenerSettings() },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Outlined.Warning, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("开启通知兜底")
                            }
                        }
                    }
                }

                item { SectionTitle(text = "我的手机号") }
                item {
                    SettingsCard(
                        icon = Icons.Outlined.PhoneAndroid,
                        title = "接收号码",
                        description = "按 SIM1、SIM2 的顺序填写手机号。系统不暴露号码时，会用 SIM 槽位匹配这里的顺序。",
                        modifier = Modifier.padding(horizontal = 18.dp)
                    ) {
                        Spacer(modifier = Modifier.height(14.dp))
                        if (state.myPhoneNumbers.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "尚未添加任何手机号",
                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column {
                                state.myPhoneNumbers.forEachIndexed { index, number ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 56.dp)
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = number,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        IconButton(onClick = { viewModel.removePhoneNumber(number) }) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "删除",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    if (index != state.myPhoneNumbers.lastIndex) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = state.newPhoneNumber,
                            onValueChange = { viewModel.onNewPhoneNumberChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("输入手机号，如 138xxxx1234") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.addPhoneNumber() },
                            enabled = state.newPhoneNumber.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("添加号码")
                        }
                    }
                }

                item { SectionTitle(text = "隐私") }
                item {
                    SettingsCard(
                        icon = Icons.Outlined.Lock,
                        title = "记录短信正文",
                        description = "关闭时只保留号码、来源、分类和接收时间。",
                        modifier = Modifier.padding(horizontal = 18.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.recordContent) "当前会保存短信正文" else "当前不保存短信正文",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Switch(
                                checked = state.recordContent,
                                onCheckedChange = { viewModel.toggleRecordContent(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val iconContainer = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val iconColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconContainer,
                    contentColor = iconColor
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}
