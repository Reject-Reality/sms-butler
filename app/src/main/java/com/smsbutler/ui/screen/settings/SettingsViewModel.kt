package com.smsbutler.ui.screen.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsbutler.data.local.PreferencesManager
import com.smsbutler.service.SmsNotificationListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val recordContent: Boolean = false,
    val notificationAccess: Boolean = false,
    val myPhoneNumbers: List<String> = emptyList(),
    val newPhoneNumber: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.preferences.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    recordContent = prefs.recordContent,
                    notificationAccess = isNotificationListenerEnabled(),
                    myPhoneNumbers = prefs.myPhoneNumbers
                )
            }
        }
    }

    fun toggleRecordContent(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setRecordContent(enabled) }
    }

    fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun onNewPhoneNumberChanged(value: String) {
        _uiState.value = _uiState.value.copy(newPhoneNumber = value)
    }

    fun addPhoneNumber() {
        val number = _uiState.value.newPhoneNumber.trim()
        if (number.isBlank()) return
        val current = _uiState.value.myPhoneNumbers.toMutableList()
        if (number !in current) {
            current.add(number)
            viewModelScope.launch {
                preferencesManager.setMyPhoneNumbers(current)
            }
        }
        _uiState.value = _uiState.value.copy(newPhoneNumber = "")
    }

    fun removePhoneNumber(number: String) {
        val current = _uiState.value.myPhoneNumbers.toMutableList()
        current.remove(number)
        viewModelScope.launch {
            preferencesManager.setMyPhoneNumbers(current)
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        val myComponent = ComponentName(context, SmsNotificationListener::class.java)
        return enabledListeners.any { it == myComponent.flattenToString() }
    }
}
