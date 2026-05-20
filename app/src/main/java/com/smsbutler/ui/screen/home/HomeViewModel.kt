package com.smsbutler.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsbutler.data.local.PreferencesManager
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.data.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val records: List<SmsRecordEntity> = emptyList(),
    val isLoading: Boolean = true,
    val myPhoneNumbers: List<String> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SmsRepository,
    private val preferences: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                repository.getAllRecords().collect { records ->
                    _uiState.value = _uiState.value.copy(records = records, isLoading = false)
                }
            }
            launch {
                preferences.preferences.collect { prefs ->
                    _uiState.value = _uiState.value.copy(myPhoneNumbers = prefs.myPhoneNumbers)
                }
            }
        }
    }

    fun toggleStar(id: Long, currentStarred: Boolean) {
        viewModelScope.launch {
            repository.toggleStar(id, !currentStarred)
        }
    }

    fun assignReceiverPhone(recordId: Long, phone: String) {
        viewModelScope.launch {
            repository.updateReceiverPhone(recordId, phone)
        }
    }
}
