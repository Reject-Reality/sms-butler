package com.smsbutler.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserPreferences(
    val recordContent: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val themeMode: String = "system",
    val myPhoneNumbers: List<String> = emptyList(),
    val lastInboxSyncAt: Long = 0L,
    val lastInboxSmsId: Long = 0L
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val RECORD_CONTENT = booleanPreferencesKey("record_content")
        val NOTIFICATION_PERMISSION = booleanPreferencesKey("notification_permission")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MY_PHONE_NUMBERS = stringPreferencesKey("my_phone_numbers")  // 逗号分隔
        val LAST_INBOX_SYNC_AT = longPreferencesKey("last_inbox_sync_at")
        val LAST_INBOX_SMS_ID = longPreferencesKey("last_inbox_sms_id")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.MY_PHONE_NUMBERS] ?: ""
        val numbers = if (raw.isBlank()) emptyList() else raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
        UserPreferences(
            recordContent = prefs[Keys.RECORD_CONTENT] ?: false,
            notificationPermissionGranted = prefs[Keys.NOTIFICATION_PERMISSION] ?: false,
            themeMode = prefs[Keys.THEME_MODE] ?: "system",
            myPhoneNumbers = numbers,
            lastInboxSyncAt = prefs[Keys.LAST_INBOX_SYNC_AT] ?: 0L,
            lastInboxSmsId = prefs[Keys.LAST_INBOX_SMS_ID] ?: 0L
        )
    }

    suspend fun setRecordContent(enabled: Boolean) {
        context.dataStore.edit { it[Keys.RECORD_CONTENT] = enabled }
    }

    suspend fun setNotificationPermission(granted: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_PERMISSION] = granted }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setMyPhoneNumbers(numbers: List<String>) {
        context.dataStore.edit { it[Keys.MY_PHONE_NUMBERS] = numbers.joinToString(",") }
    }

    suspend fun setLastInboxSyncAt(timestamp: Long) {
        context.dataStore.edit { it[Keys.LAST_INBOX_SYNC_AT] = timestamp }
    }

    suspend fun setInboxSyncWatermark(timestamp: Long, smsId: Long) {
        context.dataStore.edit {
            it[Keys.LAST_INBOX_SYNC_AT] = timestamp
            it[Keys.LAST_INBOX_SMS_ID] = smsId
        }
    }
}
