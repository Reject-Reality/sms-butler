package com.smsbutler.service

import com.smsbutler.data.local.SmsRecordEntity

object SmsRecordFactory {
    fun mergeMessageBodies(parts: List<String?>): String {
        return parts.joinToString(separator = "") { it.orEmpty() }
    }

    fun buildRecord(
        senderPhone: String,
        messageBody: String,
        receiverPhone: String,
        recordContent: Boolean,
        receivedAt: Long = System.currentTimeMillis()
    ): SmsRecordEntity {
        return SmsRecordEntity(
            phoneNumber = senderPhone,
            sender = senderPhone,
            receiverPhoneNumber = receiverPhone,
            content = if (recordContent) messageBody else "",
            recordedContent = recordContent,
            category = categorizeSms(messageBody),
            receivedAt = receivedAt,
            appLabel = ""
        )
    }

    fun categorizeSms(text: String): String {
        return when {
            text.contains("验证码") || text.contains("校验码") || text.contains("驗證碼") -> "验证码"
            text.contains("广告") || text.contains("推广") || text.contains("优惠") -> "广告"
            text.contains("流量") || text.contains("余额") || text.contains("账单") -> "运营商"
            text.contains("快递") || text.contains("物流") || text.contains("包裹") -> "快递"
            else -> "通知"
        }
    }

}
