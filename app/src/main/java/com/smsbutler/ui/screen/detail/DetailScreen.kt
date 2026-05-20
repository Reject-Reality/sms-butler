package com.smsbutler.ui.screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsbutler.ui.components.ButlerBackground
import com.smsbutler.ui.components.EmptyState
import com.smsbutler.ui.components.MetricCard
import com.smsbutler.ui.components.ScreenHeader
import com.smsbutler.ui.components.SmsRecordCard

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        ButlerBackground {
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.records.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        ScreenHeader(
                            title = state.phoneNumber,
                            subtitle = "该号码暂时没有短信记录。",
                            icon = Icons.Outlined.PhoneAndroid,
                            trailing = {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                                }
                            }
                        )
                        EmptyState(
                            icon = Icons.Outlined.Inbox,
                            title = "暂无详情",
                            message = "返回记录列表查看其他号码。",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    val senders = state.records.map { it.appLabel ?: it.sender }.distinct()

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(bottom = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            ScreenHeader(
                                title = state.phoneNumber,
                                subtitle = "来自 ${senders.size} 个发送方的短信明细。",
                                icon = Icons.Outlined.PhoneAndroid,
                                trailing = {
                                    IconButton(onClick = onBack) {
                                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                                    }
                                }
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MetricCard(
                                    label = "短信数量",
                                    value = state.records.size.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                MetricCard(
                                    label = "发送方",
                                    value = senders.size.toString(),
                                    modifier = Modifier.weight(1f),
                                    accent = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                        items(state.records, key = { it.id }) { record ->
                            SmsRecordCard(
                                record = record,
                                onClick = {},
                                onStarClick = null,
                                modifier = Modifier.padding(horizontal = 18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
