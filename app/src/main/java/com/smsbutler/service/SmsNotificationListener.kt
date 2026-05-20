package com.smsbutler.service

import android.Manifest
import android.app.Notification
import android.app.Person
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.smsbutler.data.local.PreferencesManager
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.data.repository.SmsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsNotificationListener : NotificationListenerService() {
    @Inject lateinit var repository: SmsRepository
    @Inject lateinit var preferences: PreferencesManager

    private val scope = CoroutineScope(Dispatchers.IO)

    private val smsPackages = setOf(
        "com.android.mms",
        "com.google.android.apps.messaging",
        "com.android.messaging",
        "com.samsung.android.messaging",
        "com.oneplus.mms",
        "com.xiaomi.smsextra",
        "com.miui.smsextra",
        "com.huawei.message",
        "com.coloros.mms",
        "com.heytap.mms",
        "com.vivo.messaging",
        "com.transsion.messaging"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in smsPackages) return
        if (isServiceLikeNotification(sbn.notification)) return

        val parsed = parseSmsNotification(sbn) ?: return

        scope.launch {
            val prefs = preferences.preferences.first()
            val receivedAt = sbn.postTime.takeIf { it > 0L } ?: System.currentTimeMillis()
            delay(1800)
            val inboxMatch = findMatchingInboxSms(parsed.content, receivedAt)
            val recordPhone = inboxMatch?.senderPhone ?: parsed.senderName
            val recordAt = inboxMatch?.receivedAt ?: receivedAt

            val receiverPhone = if (inboxMatch != null) {
                ReceiverPhoneResolver(this@SmsNotificationListener).resolve(
                    subscriptionId = inboxMatch.subscriptionId,
                    myPhoneNumbers = prefs.myPhoneNumbers
                )
            } else {
                ""
            }

            val record = SmsRecordEntity(
                phoneNumber = recordPhone,
                sender = parsed.senderName,
                receiverPhoneNumber = receiverPhone,
                content = if (prefs.recordContent) parsed.content else "",
                recordedContent = prefs.recordContent,
                category = SmsRecordFactory.categorizeSms("${parsed.senderName} ${parsed.content}"),
                receivedAt = recordAt,
                appLabel = getAppLabel(sbn.packageName)
            )
            if (SmsFingerprint.hasDuplicate(recordPhone, parsed.content, recordAt, repository.getRecordsAround(recordAt))) {
                return@launch
            }
            repository.insertRecord(record)
            Log.d("SmsButler", "Notification fallback inserted SMS: sender=${parsed.senderName}, phone=$recordPhone, receiver=$receiverPhone")
        }
    }

    private fun isServiceLikeNotification(notification: Notification): Boolean {
        val flags = notification.flags
        if ((flags and Notification.FLAG_ONGOING_EVENT) != 0) return true
        if ((flags and Notification.FLAG_FOREGROUND_SERVICE) != 0) return true
        if (notification.category in setOf(Notification.CATEGORY_SERVICE, Notification.CATEGORY_STATUS, Notification.CATEGORY_PROGRESS)) {
            return true
        }
        return false
    }

    private fun parseSmsNotification(sbn: StatusBarNotification): ParsedSmsNotification? {
        val extras = sbn.notification.extras
        val title = extras.textExtra(Notification.EXTRA_TITLE)
        val text = extras.textExtra(Notification.EXTRA_TEXT)
        val bigText = extras.textExtra(Notification.EXTRA_BIG_TEXT)
        val subText = extras.textExtra(Notification.EXTRA_SUB_TEXT)
        val summary = extras.textExtra(Notification.EXTRA_SUMMARY_TEXT)
        val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.mapNotNull { it?.toString()?.takeIf(String::isNotBlank) }
            .orEmpty()

        val content = listOf(bigText, text)
            .plus(textLines)
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
        val allText = listOf(title, subText, summary, content).plus(textLines)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()

        if (allText.isBlank()) return null
        if (looksLikeRunningStatus(allText)) return null
        if (!looksLikeSmsMessage(allText)) return null

        val senderName = listOf(subText, title)
            .firstOrNull { it.isNotBlank() && !looksLikeRunningStatus(it) }
            ?: getAppLabel(sbn.packageName)
        val phoneNumber = extractPhoneFromPeople(extras)
            ?: extractPhoneNumber(senderName)
            ?: senderName

        return ParsedSmsNotification(
            phoneNumber = phoneNumber,
            senderName = senderName,
            content = content.ifBlank { allText }
        )
    }

    private fun looksLikeRunningStatus(text: String): Boolean {
        val normalized = text.trim()
        return normalized.contains("正在运行") ||
            normalized.contains("运行中") ||
            normalized.contains("is running", ignoreCase = true) ||
            normalized == "短信"
    }

    private fun looksLikeSmsMessage(text: String): Boolean {
        return extractPhoneNumber(text) != null ||
            text.contains("验证码") ||
            text.contains("校验码") ||
            text.contains("动态码") ||
            text.contains("驗證碼") ||
            text.contains("中国移动") ||
            text.contains("中国电信") ||
            text.contains("中国联通") ||
            text.contains("10086") ||
            text.contains("10000") ||
            text.contains("10010") ||
            text.length >= 8 && text.any { it.isDigit() }
    }

    @Suppress("DEPRECATION")
    private fun extractPhoneFromPeople(extras: Bundle): String? {
        val people = extras.get("android.people.list") as? ArrayList<*> ?: return null
        return people.firstNotNullOfOrNull { item ->
            if (item is Person) extractPhoneNumber(item.uri?.toString().orEmpty()) else null
        }
    }

    private fun extractPhoneNumber(text: String): String? {
        if (text.isBlank()) return null
        return Regex("(?:\\+?86[\\s-]*)?1[3-9]\\d{9}")
            .find(text)
            ?.value
            ?.replace(Regex("[\\s-]"), "")
            ?.removePrefix("+86")
            ?.removePrefix("86")
    }

    private fun findMatchingInboxSms(content: String, receivedAt: Long): InboxNotificationMatch? {
        if (content.isBlank()) return null
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val projectionWithSubId = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.SUBSCRIPTION_ID
        )
        val projectionWithoutSubId = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        return runCatching {
            queryMatchingInboxSms(projectionWithSubId, hasSubscriptionId = true, content = content, receivedAt = receivedAt)
        }.recoverCatching {
            queryMatchingInboxSms(projectionWithoutSubId, hasSubscriptionId = false, content = content, receivedAt = receivedAt)
        }.getOrNull()
    }

    private fun queryMatchingInboxSms(
        projection: Array<String>,
        hasSubscriptionId: Boolean,
        content: String,
        receivedAt: Long
    ): InboxNotificationMatch? {
        val startAt = receivedAt - NOTIFICATION_MATCH_WINDOW_MILLIS
        val endAt = System.currentTimeMillis() + NOTIFICATION_MATCH_FUTURE_WINDOW_MILLIS
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            "${Telephony.Sms.DATE} BETWEEN ? AND ? AND ${Telephony.Sms.TYPE} = ?",
            arrayOf(startAt.toString(), endAt.toString(), Telephony.Sms.MESSAGE_TYPE_INBOX.toString()),
            "${Telephony.Sms.DATE} DESC LIMIT 20"
        ) ?: return null

        cursor.use {
            while (it.moveToNext()) {
                val sender = it.getStringOrNull(Telephony.Sms.ADDRESS)?.takeIf(String::isNotBlank) ?: continue
                val body = it.getStringOrNull(Telephony.Sms.BODY).orEmpty()
                if (!body.matchesNotificationContent(content)) continue

                return InboxNotificationMatch(
                    senderPhone = sender,
                    body = body,
                    receivedAt = it.getLongOrNull(Telephony.Sms.DATE) ?: receivedAt,
                    subscriptionId = if (hasSubscriptionId) {
                        it.getIntOrNull(Telephony.Sms.SUBSCRIPTION_ID)
                            ?: SubscriptionManager.INVALID_SUBSCRIPTION_ID
                    } else {
                        SubscriptionManager.INVALID_SUBSCRIPTION_ID
                    }
                )
            }
        }

        return null
    }

    private fun String.matchesNotificationContent(notificationContent: String): Boolean {
        val body = compactForMatch()
        val notification = notificationContent.compactForMatch()
        if (body.isBlank() || notification.isBlank()) return false

        val bodySnippet = body.take(24)
        val notificationSnippet = notification.take(24)
        return body.contains(notificationSnippet) ||
            notification.contains(bodySnippet) ||
            SmsFingerprint.extractVerificationCode(body) == SmsFingerprint.extractVerificationCode(notification) &&
                SmsFingerprint.extractVerificationCode(body) != null ||
            body.commonPrefixWith(notification).length >= 12
    }

    private fun String.compactForMatch(): String {
        return SmsFingerprint.normalize(this)
    }

    private fun Cursor.getStringOrNull(column: String): String? {
        val index = getColumnIndex(column)
        return if (index >= 0) getString(index) else null
    }

    private fun Cursor.getLongOrNull(column: String): Long? {
        val index = getColumnIndex(column)
        return if (index >= 0) getLong(index) else null
    }

    private fun Cursor.getIntOrNull(column: String): Int? {
        val index = getColumnIndex(column)
        return if (index >= 0) getInt(index) else null
    }

    private fun getAppLabel(packageName: String): String {
        return runCatching {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault(packageName)
    }

    private fun Bundle.textExtra(key: String): String {
        return getCharSequence(key)?.toString()?.trim().orEmpty()
    }
}

private data class ParsedSmsNotification(
    val phoneNumber: String,
    val senderName: String,
    val content: String
)

private data class InboxNotificationMatch(
    val senderPhone: String,
    val body: String,
    val receivedAt: Long,
    val subscriptionId: Int
)

private const val NOTIFICATION_MATCH_WINDOW_MILLIS = 30 * 60 * 1000L
private const val NOTIFICATION_MATCH_FUTURE_WINDOW_MILLIS = 60 * 1000L
