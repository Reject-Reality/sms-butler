package com.smsbutler.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.data.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val phoneNumber: String = "",
    val records: List<SmsRecordEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SmsRepository
) : ViewModel() {

    private val phoneNumber: String = savedStateHandle["phoneNumber"] ?: ""

    private val _uiState = MutableStateFlow(DetailUiState(phoneNumber = phoneNumber))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRecordsByPhone(phoneNumber).collect { records ->
                _uiState.value = DetailUiState(
                    phoneNumber = phoneNumber,
                    records = records,
                    isLoading = false
                )
            }
        }
    }
}
