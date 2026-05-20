package com.smsbutler.service

import com.smsbutler.data.local.SmsRecordEntity

object SmsFingerprint {
    fun isDuplicate(
        senderA: String,
        bodyA: String,
        atA: Long,
        senderB: String,
        bodyB: String?,
        atB: Long,
        windowMillis: Long = 120_000
    ): Boolean {
        if (kotlin.math.abs(atA - atB) > windowMillis) return false

        val normalizedA = normalize(bodyA)
        val normalizedB = normalize(bodyB.orEmpty())
        val codeA = extractVerificationCode(normalizedA)
        val codeB = extractVerificationCode(normalizedB)
        val senderRelated = senderA.isBlank() ||
            senderB.isBlank() ||
            normalize(senderA) == normalize(senderB)

        if (codeA != null && codeA == codeB) return senderRelated || normalizedA.commonPrefixWith(normalizedB).length >= 6
        if (normalizedA.isBlank() || normalizedB.isBlank()) return senderRelated

        return normalizedA.contains(normalizedB.take(24)) ||
            normalizedB.contains(normalizedA.take(24)) ||
            normalizedA.commonPrefixWith(normalizedB).length >= 12
    }

    fun hasDuplicate(candidate: SmsRecordEntity, existing: List<SmsRecordEntity>): Boolean {
        return existing.any { record ->
            isDuplicate(
                senderA = candidate.phoneNumber,
                bodyA = candidate.content.orEmpty(),
                atA = candidate.receivedAt,
                senderB = record.phoneNumber,
                bodyB = record.content,
                atB = record.receivedAt
            )
        }
    }

    fun hasDuplicate(
        sender: String,
        body: String,
        receivedAt: Long,
        existing: List<SmsRecordEntity>
    ): Boolean {
        return existing.any { record ->
            isDuplicate(
                senderA = sender,
                bodyA = body,
                atA = receivedAt,
                senderB = record.phoneNumber,
                bodyB = record.content,
                atB = record.receivedAt
            )
        }
    }

    fun normalize(text: String): String {
        return text
            .replace(Regex("\\s+"), "")
            .replace("【", "")
            .replace("】", "")
            .trim()
    }

    fun extractVerificationCode(text: String): String? {
        return Regex("(?<!\\d)\\d{4,8}(?!\\d)").find(text)?.value
    }
}
