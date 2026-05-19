package com.smsbutler.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.util.Log
import com.smsbutler.data.local.PreferencesManager
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.data.repository.SmsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SmsReceiverEntryPoint {
        fun repository(): SmsRepository
        fun preferences(): PreferencesManager
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val entryPoint = EntryPointAccessors.fromApplication(context, SmsReceiverEntryPoint::class.java)
        val repository = entryPoint.repository()
        val preferences = entryPoint.preferences()

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val subId = intent.getIntExtra("subscription", SubscriptionManager.INVALID_SUBSCRIPTION_ID)
        Log.d("SmsButler", "SMS_RECEIVED: messages=${messages.size}, subscriptionId=$subId")

        val myNumbers = runCatching {
            kotlinx.coroutines.runBlocking { preferences.preferences.first().myPhoneNumbers }
        }.getOrDefault(emptyList())

        val receiverPhone = if (myNumbers.size == 1) {
            myNumbers.first()
        } else if (subId in 0 until myNumbers.size) {
            myNumbers[subId]  // subscription = SIM 卡槽序号
        } else {
            ""  // 多卡但无法匹配时留空
        }

        for (message in messages) {
            val senderPhone = message.originatingAddress ?: continue
            val msgBody = message.messageBody ?: ""

            Log.d("SmsButler", "  from=$senderPhone, body=${msgBody.take(50)}, receiver=$receiverPhone, subId=$subId")

            val record = SmsRecordEntity(
                phoneNumber = senderPhone,
                sender = senderPhone,
                receiverPhoneNumber = receiverPhone,
                content = msgBody,
                category = categorizeSms(msgBody),
                appLabel = ""
            )

            scope.launch {
                repository.insertRecord(record)
            }
        }
    }

    private fun categorizeSms(text: String): String {
        return when {
            text.contains("验证码") || text.contains("校验码") || text.contains("驗證碼") -> "验证码"
            text.contains("广告") || text.contains("推广") || text.contains("优惠") -> "广告"
            text.contains("流量") || text.contains("余额") || text.contains("账单") -> "运营商"
            text.contains("快递") || text.contains("物流") || text.contains("包裹") -> "快递"
            else -> "通知"
        }
    }
}
