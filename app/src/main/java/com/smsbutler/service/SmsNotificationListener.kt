package com.smsbutler.service

import android.annotation.SuppressLint
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
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

        // 获取通知中的文本信息
        val title = extras.getCharSequence("android.title")?.toString()
            ?: extras.getString("android.title")
            ?: ""
        val text = extras.getCharSequence("android.text")?.toString()
            ?: extras.getString("android.text")
            ?: ""
        val subText = extras.getCharSequence("android.subText")?.toString()
            ?: extras.getString("android.subText")
            ?: ""
        val summaryText = extras.getCharSequence("android.summaryText")?.toString()
            ?: extras.getString("android.summaryText")
            ?: ""

        // 把所有有效的文本字段拼起来用于提取手机号
        val allText = listOf(title, subText, summaryText, text)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        if (allText.isBlank()) return

        // 从所有文本中提取手机号
        val phoneNumber = extractPhoneNumber(allText)
            ?: extractPhoneNumber(title)
            ?: title.takeIf { it.isNotBlank() }  // 兜底：用标题本身

        // 发送方显示名称：优先用 subText（有些app放这里），其次 title
        val senderDisplayName = subText.takeIf { it.isNotBlank() }
            ?: title.takeIf { it.isNotBlank() }
            ?: packageName

        // 短信内容
        val content = text.takeIf { it.isNotBlank() }

        // 分类
        val category = categorizeSms(allText)

        // App 标签
        val appLabel = getAppLabel(packageName)

        val record = SmsRecordEntity(
            phoneNumber = phoneNumber,
            sender = senderDisplayName,
            content = content,
            category = category,
            appLabel = appLabel
        )

        Log.d("SmsButler", "SMS: phone=$phoneNumber, sender=$senderDisplayName, content=$content, app=$appLabel")

        scope.launch {
            repository.insertRecord(record)
        }
    }

    private fun extractPhoneNumber(text: String): String? {
        if (text.isBlank()) return null
        // 匹配中国手机号：1[3-9]xxxxxxxxx
        val regex = Regex("(?:\\+?86[\\s-]*)?1[3-9]\\d{9}")
        return regex.find(text)?.value?.replace(Regex("[\\s-]"), "")
    }

    private fun categorizeSms(text: String): String {
        return when {
            text.contains("验证码") || text.contains("校验码") || text.contains("驗證碼")
                || text.contains("验证") || text.contains("校验") -> "验证码"
            text.contains("广告") || text.contains("推广") || text.contains("优惠") -> "广告"
            text.contains("流量") || text.contains("余额") || text.contains("账单")
                || text.contains("套餐") || text.contains("缴费") -> "运营商"
            text.contains("快递") || text.contains("物流") || text.contains("包裹")
                || text.contains("取件") || text.contains("签收") -> "快递"
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
