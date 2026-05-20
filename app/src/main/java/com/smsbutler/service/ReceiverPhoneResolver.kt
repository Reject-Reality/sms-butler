package com.smsbutler.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

class ReceiverPhoneResolver(
    private val context: Context
) {
    fun resolve(subscriptionId: Int, myPhoneNumbers: List<String>): String {
        if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            return if (myPhoneNumbers.size == 1) myPhoneNumbers.first() else ""
        }
        val systemNumber = readSystemPhoneNumber(subscriptionId)
        val simSlotIndex = readSimSlotIndex(subscriptionId)
        return resolveBestEffort(systemNumber, simSlotIndex, subscriptionId, myPhoneNumbers)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun readSystemPhoneNumber(subscriptionId: Int): String? {
        if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) return null
        if (!hasPhoneNumberPermission()) return null

        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java) ?: return null
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                normalizePhoneNumber(subscriptionManager.getPhoneNumber(subscriptionId))
            } else {
                normalizePhoneNumber(subscriptionManager.getActiveSubscriptionInfo(subscriptionId)?.number)
            }
        }.getOrNull()
    }

    @SuppressLint("MissingPermission")
    private fun readSimSlotIndex(subscriptionId: Int): Int? {
        if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) return null
        if (!hasPhoneNumberPermission()) return null

        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java) ?: return null
        return runCatching {
            subscriptionManager.getActiveSubscriptionInfo(subscriptionId)?.simSlotIndex
        }.getOrNull()?.takeIf { it >= 0 }
    }

    private fun hasPhoneNumberPermission(): Boolean {
        val readPhoneNumbers = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_NUMBERS
        ) == PackageManager.PERMISSION_GRANTED
        val readPhoneState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        return readPhoneNumbers || readPhoneState
    }

    private fun normalizePhoneNumber(raw: String?): String? {
        val cleaned = raw
            ?.trim()
            ?.replace(Regex("[\\s-]"), "")
            ?.takeIf { it.isNotBlank() }
            ?: return null

        return when {
            cleaned.startsWith("+86") && cleaned.length > 3 -> cleaned.removePrefix("+86")
            cleaned.startsWith("86") && cleaned.length == 13 -> cleaned.removePrefix("86")
            else -> cleaned
        }
    }

    companion object {
        private const val SIM_LABEL_PREFIX = "sim:"

        internal fun resolveBestEffort(
            systemNumber: String?,
            simSlotIndex: Int?,
            subscriptionId: Int,
            myPhoneNumbers: List<String>
        ): String {
            if (!systemNumber.isNullOrBlank()) return systemNumber
            if (simSlotIndex != null && simSlotIndex in myPhoneNumbers.indices) {
                return myPhoneNumbers[simSlotIndex]
            }
            if (myPhoneNumbers.size == 1) return myPhoneNumbers.first()

            // 有 subscriptionId → 生成 SIM 标签（不需要 READ_PHONE_STATE 权限）
            if (subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID && subscriptionId > 0) {
                return "${SIM_LABEL_PREFIX}${subscriptionId}"
            }

            return ""
        }

        fun isSimLabel(value: String): Boolean = value.startsWith(SIM_LABEL_PREFIX)

        fun simLabelToDisplay(value: String): String {
            val subId = value.removePrefix(SIM_LABEL_PREFIX).toIntOrNull() ?: return value
            return "SIM $subId"
        }
    }
}
