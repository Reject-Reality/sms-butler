package com.smsbutler.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.util.Log
import com.smsbutler.data.local.PreferencesManager
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

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(context, SmsReceiverEntryPoint::class.java)
                val repository = entryPoint.repository()
                val preferences = entryPoint.preferences()
                val userPreferences = preferences.preferences.first()

                val subscriptionId = extractSubscriptionId(intent)
                val receiverPhone = ReceiverPhoneResolver(context).resolve(
                    subscriptionId = subscriptionId,
                    myPhoneNumbers = userPreferences.myPhoneNumbers
                )

                val senderPhone = messages.firstNotNullOfOrNull { it.originatingAddress?.takeIf(String::isNotBlank) }
                    ?: return@launch
                val messageBody = SmsRecordFactory.mergeMessageBodies(messages.map { it.messageBody })
                val receivedAt = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

                Log.d(
                    "SmsButler",
                    "SMS_RECEIVED: parts=${messages.size}, subscriptionId=$subscriptionId, sender=$senderPhone, receiver=$receiverPhone"
                )

                val record = SmsRecordFactory.buildRecord(
                    senderPhone = senderPhone,
                    messageBody = messageBody,
                    receiverPhone = receiverPhone,
                    recordContent = userPreferences.recordContent,
                    receivedAt = receivedAt
                )
                if (SmsFingerprint.hasDuplicate(senderPhone, messageBody, receivedAt, repository.getRecordsAround(receivedAt))) {
                    return@launch
                }
                repository.insertRecord(record)
            } catch (e: Exception) {
                Log.e("SmsButler", "Failed to record SMS: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun extractSubscriptionId(intent: Intent): Int {
        val keys = listOf(
            "subscription",
            "subscription_id",
            "android.telephony.extra.SUBSCRIPTION_INDEX",
            "android.telephony.extra.SUBSCRIPTION_ID",
            "slot",
            "simSlot",
            "sim_slot"
        )
        return keys.firstNotNullOfOrNull { key ->
            intent.getIntExtra(key, SubscriptionManager.INVALID_SUBSCRIPTION_ID)
                .takeIf { it != SubscriptionManager.INVALID_SUBSCRIPTION_ID }
        } ?: SubscriptionManager.INVALID_SUBSCRIPTION_ID
    }
}
