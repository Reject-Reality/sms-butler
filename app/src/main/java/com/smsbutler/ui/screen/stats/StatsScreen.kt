package com.smsbutler.ui.screen.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsbutler.data.local.PhoneSummary
import com.smsbutler.ui.components.ButlerBackground
import com.smsbutler.ui.components.EmptyState
import com.smsbutler.ui.components.MetaChip
import com.smsbutler.ui.components.MetricCard
import com.smsbutler.ui.components.ScreenHeader
import com.smsbutler.ui.components.SectionTitle

@Composable
fun StatsScreen(
    onPhoneClick: (String) -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
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
                state.summaries.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        ScreenHeader(
                            title = "统计",
                            subtitle = "按发送号码汇总短信数量和接收号码。",
                            icon = Icons.Outlined.Analytics
                        )
                        EmptyState(
                            icon = Icons.Outlined.Insights,
                            title = "暂无统计数据",
                            message = "有短信记录后，这里会显示号码热度和归属。",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    val total = state.summaries.sumOf { it.count }
                    val receivers = state.summaries.map { it.receiverPhoneNumber }.filter { it.isNotBlank() }.distinct().size

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(bottom = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            ScreenHeader(
                                title = "统计",
                                subtitle = "按发送号码汇总短信数量和接收号码。",
                                icon = Icons.Outlined.Analytics
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MetricCard(
                                    label = "记录总数",
                                    value = total.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                MetricCard(
                                    label = "接收号码",
                                    value = receivers.toString(),
                                    modifier = Modifier.weight(1f),
                                    accent = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                        item { SectionTitle(text = "号码排行") }
                        items(state.summaries, key = { "${it.receiverPhoneNumber}_${it.phoneNumber}" }) { summary ->
                            PhoneSummaryCard(
                                summary = summary,
                                onClick = { onPhoneClick(summary.phoneNumber) },
                                modifier = Modifier.padding(horizontal = 18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneSummaryCard(
    summary: PhoneSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 84.dp)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Outlined.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.phoneNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (summary.receiverPhoneNumber.isNotBlank()) {
                    MetaChip(
                        text = "接收 ${summary.receiverPhoneNumber}",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = summary.count.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "条",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
