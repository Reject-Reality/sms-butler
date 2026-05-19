package com.smsbutler.service

import android.annotation.SuppressLint
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.data.repository.SmsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("OverrideAbstract")
class SmsNotificationListener : NotificationListenerService() {

    @Inject lateinit var repository: SmsRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras

        val title = extras.getString(NotificationCompat.EXTRA_TITLE) ?: return
        val text = extras.getString(NotificationCompat.EXTRA_TEXT) ?: return

        val phoneNumber = extractPhoneNumber(title) ?: title
        val sender = packageName

        val record = SmsRecordEntity(
            phoneNumber = phoneNumber,
            sender = sender,
            content = text,
            category = categorizeSms(text),
            appLabel = getAppLabel(packageName)
        )

        scope.launch {
            repository.insertRecord(record)
        }
    }

    private fun extractPhoneNumber(text: String): String? {
        val regex = Regex("1[3-9]\\d{9}")
        return regex.find(text)?.value
    }

    private fun categorizeSms(text: String): String {
        return when {
            text.contains("验证码") || text.contains("校验码") || text.contains("驗證碼") -> "验证码"
            text.contains("广告") || text.contains("推广") || text.contains("优惠") -> "广告"
            else -> "通知"
        }
    }

    private fun getAppLabel(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
