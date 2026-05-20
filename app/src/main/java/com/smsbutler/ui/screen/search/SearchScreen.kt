package com.smsbutler.ui.screen.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.ui.components.ButlerBackground
import com.smsbutler.ui.components.EmptyState
import com.smsbutler.ui.components.ScreenHeader
import com.smsbutler.ui.components.SmsRecordCard

@Composable
fun SearchScreen(
    onRecordClick: (SmsRecordEntity) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        ButlerBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ScreenHeader(
                    title = "搜索",
                    subtitle = "按手机号、发送方或应用名称快速定位短信。",
                    icon = Icons.AutoMirrored.Outlined.ManageSearch
                )
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { viewModel.onQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    placeholder = { Text("输入手机号或发送方") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "清除")
                            }
                        }
                    }
                )

                when {
                    state.query.isBlank() -> {
                        EmptyState(
                            icon = Icons.Outlined.Search,
                            title = "开始搜索",
                            message = "输入手机号、发送方或应用名称即可筛选记录。",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    state.results.isEmpty() -> {
                        EmptyState(
                            icon = Icons.AutoMirrored.Outlined.ManageSearch,
                            title = "没有匹配结果",
                            message = "换个关键词，或检查号码是否已经被记录。",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.results, key = { it.id }) { record ->
                                SmsRecordCard(
                                    record = record,
                                    onClick = { onRecordClick(record) },
                                    onStarClick = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
