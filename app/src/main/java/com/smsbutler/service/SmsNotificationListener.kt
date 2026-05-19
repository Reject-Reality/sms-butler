package com.smsbutler.service

import android.annotation.SuppressLint
import android.app.Person
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.smsbutler.data.local.PreferencesManager
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.data.repository.SmsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("OverrideAbstract")
class SmsNotificationListener : NotificationListenerService() {

    @Inject lateinit var repository: SmsRepository
    @Inject lateinit var preferences: PreferencesManager

    private val scope = CoroutineScope(Dispatchers.IO)

    // 常见的短信/消息类 App 的包名
    private val smsPackages = setOf(
        "com.android.mms",                                // AOSP 短信
        "com.google.android.apps.messaging",              // Google Messages
        "com.android.messaging",                          // Android Messages
        "com.samsung.android.messaging",                  // Samsung Messages
        "com.oneplus.mms",                                // OnePlus
        "com.xiaomi.smsextra",                            // 小米
        "com.miui.smsextra",
        "com.huawei.message",                             // 华为
        "com.tencent.mm",                                 // 微信
    )

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i("SmsButler", "=== 通知监听服务已连接 ===")
    }

    override fun onListenerDisconnected() {
        Log.w("SmsButler", "=== 通知监听服务已断开 ===")
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        // 打印所有通知的来源（调试用）
        Log.d("SmsButler", "收到通知: package=$packageName")

        // 只处理短信/消息类 App 的通知
        if (packageName !in smsPackages) return

        Log.i("SmsButler", "=== 匹配到短信通知: $packageName ===")
        Log.d("SmsButler", "  sbn.tag=${sbn.tag}, sbn.id=${sbn.id}, sbn.key=${sbn.key}")

        val extras = sbn.notification.extras

        // 打印所有 extras 用于调试
        val keys = extras.keySet()
        keys.forEach { key ->
            val value = extras.get(key)
            if (value != null) {
                Log.d("SmsButler", "  extra[$key] = $value (${value::class.simpleName})")
            }
        }

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

        Log.d("SmsButler", "  title=$title")
        Log.d("SmsButler", "  text=$text")
        Log.d("SmsButler", "  subText=$subText")
        Log.d("SmsButler", "  summaryText=$summaryText")

        // 把所有有效的文本字段拼起来用于提取手机号
        val allText = listOf(title, subText, summaryText, text)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        if (allText.isBlank()) {
            Log.w("SmsButler", "  所有文本字段为空，跳过")
            return
        }

        // 发送方显示名称
        val senderDisplayName = subText.takeIf { it.isNotBlank() }
            ?: title.takeIf { it.isNotBlank() }
            ?: packageName

        // 尝试从 Person 对象获取手机号（最常见的位置）
        val personPhone = extractPhoneFromPeople(extras)

        // 从所有文本中提取手机号
        val phoneNumber = extractPhoneNumber(allText)
            ?: extractPhoneNumber(title)
            ?: personPhone  // 从 Person URI 中提取
            ?: senderDisplayName

        // 短信内容
        val content = text.takeIf { it.isNotBlank() }

        // 分类
        val category = categorizeSms(allText)

        // App 标签
        val appLabel = getAppLabel(packageName)

        // 获取用户设置的本机号码
        val myNumbers = runCatching {
            kotlinx.coroutines.runBlocking { preferences.preferences.first().myPhoneNumbers }
        }.getOrDefault(emptyList())

        // 只有配置了唯一号码时才自动分配，多卡用户留空让用户手动指定
        val receiverPhone = if (myNumbers.size == 1) myNumbers.first() else ""

        val record = SmsRecordEntity(
            phoneNumber = phoneNumber,
            sender = senderDisplayName,
            receiverPhoneNumber = receiverPhone,
            content = content ?: "",
            category = category,
            appLabel = appLabel
        )

        Log.i("SmsButler", "插入记录: phone=$phoneNumber, sender=$senderDisplayName, receiver=$receiverPhone, app=$appLabel")

        scope.launch {
            repository.insertRecord(record)
        }
    }

    private fun extractPhoneFromPeople(extras: android.os.Bundle): String? {
        // android.people.list 里是 Person 对象，其 URI 往往包含 tel:138xxxx1234
        val people = extras.get("android.people.list") as? ArrayList<*>
        if (people != null) {
            for (item in people) {
                if (item is Person) {
                    val uri = item.uri
                    if (uri != null) {
                        val uriStr = uri.toString()
                        Log.d("SmsButler", "  Person URI: $uriStr")
                        val phone = extractPhoneNumber(uriStr)
                        if (phone != null) return phone
                    }
                    // 名字作为备选
                    val name = item.name?.toString()
                    if (!name.isNullOrBlank()) {
                        Log.d("SmsButler", "  Person name: $name")
                    }
                }
            }
        }
        return null
    }

    private fun extractPhoneNumber(text: String): String? {
        if (text.isBlank()) return null
        val regex = Regex("(?:\\+?86[\\s-]*)?1[3-9]\\d{9}")
        return regex.find(text)?.value?.replace(Regex("[\\s-]"), "")
    }

    private fun categorizeSms(text: String): String {
        return when {
            text.contains("验证码") || text.contains("校验码") || text.contains("驗證碼") -> "验证码"
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
