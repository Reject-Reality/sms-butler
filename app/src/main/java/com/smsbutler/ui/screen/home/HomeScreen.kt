package com.smsbutler.ui.screen.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.permissions.SmsPermissions
import com.smsbutler.ui.components.ButlerBackground
import com.smsbutler.ui.components.EmptyState
import com.smsbutler.ui.components.ScreenHeader
import com.smsbutler.ui.components.SmsRecordCard
import com.smsbutler.ui.components.StatusBanner

@Composable
fun HomeScreen(
    onRecordClick: (SmsRecordEntity) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(hasSmsCorePermissions(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermission = hasSmsCorePermissions(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = hasSmsCorePermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        ButlerBackground {
            if (state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.records.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    ScreenHeader(
                        title = "短信助手",
                        subtitle = "自动归档真实收到的短信，帮你分清来源、号码和关键内容。",
                        icon = Icons.Outlined.Sms
                    )
                    if (!hasPermission) {
                        StatusBanner(
                            icon = Icons.Outlined.NotificationsOff,
                            title = "短信权限未完整开启",
                            message = "点此授权接收和读取短信，才能记录并补同步验证码。",
                            modifier = Modifier.padding(horizontal = 18.dp),
                            isError = true,
                            onClick = {
                                permissionLauncher.launch(SmsPermissions.requiredRuntimePermissions)
                            }
                        )
                    }
                    EmptyState(
                        icon = Icons.Outlined.Inbox,
                        title = "暂无短信记录",
                        message = "在设置中添加你的手机号，收到短信后会自动显示在这里。",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        ScreenHeader(
                            title = "短信助手",
                            subtitle = "共 ${state.records.size} 条记录，按最新接收时间排列。",
                            icon = Icons.Outlined.Sms,
                            trailing = {
                                FilledTonalButton(onClick = { viewModel.forceSyncRecentSms() }) {
                                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("同步")
                                }
                            }
                        )
                    }
                    if (!hasPermission) {
                        item {
                            StatusBanner(
                                icon = Icons.Outlined.NotificationsOff,
                                title = "短信权限未完整开启",
                                message = "点此授权接收和读取短信，才能记录并补同步验证码。",
                                modifier = Modifier.padding(horizontal = 18.dp),
                                isError = true,
                                onClick = {
                                    permissionLauncher.launch(SmsPermissions.requiredRuntimePermissions)
                                }
                            )
                        }
                    }
                    items(state.records, key = { it.id }) { record ->
                        SmsRecordCard(
                            record = record,
                            onClick = { onRecordClick(record) },
                            onStarClick = { viewModel.toggleStar(record.id, record.isStarred) },
                            modifier = Modifier.padding(horizontal = 18.dp),
                            myPhoneNumbers = state.myPhoneNumbers,
                            onAssignReceiver = { phone -> viewModel.assignReceiverPhone(record.id, phone) }
                        )
                    }
                }
            }
        }
    }
}

private fun hasSmsCorePermissions(context: android.content.Context): Boolean {
    return SmsPermissions.canReceiveSms(context) && SmsPermissions.canReadSms(context)
}
