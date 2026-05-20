package com.smsbutler.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsRecordFactoryTest {
    @Test
    fun buildRecord_omitsContentWhenDisabled() {
        val record = SmsRecordFactory.buildRecord(
            senderPhone = "10690000",
            messageBody = "验证码 123456",
            receiverPhone = "13800000000",
            recordContent = false,
            receivedAt = 100L
        )

        assertEquals("", record.content)
        assertFalse(record.recordedContent)
        assertEquals("验证码", record.category)
    }

    @Test
    fun buildRecord_keepsContentWhenEnabled() {
        val record = SmsRecordFactory.buildRecord(
            senderPhone = "10690000",
            messageBody = "验证码 123456",
            receiverPhone = "13800000000",
            recordContent = true,
            receivedAt = 100L
        )

        assertEquals("验证码 123456", record.content)
        assertTrue(record.recordedContent)
    }

    @Test
    fun mergeMessageBodies_combinesMultipartSms() {
        val merged = SmsRecordFactory.mergeMessageBodies(
            listOf("您的验证码", "是 123456", null)
        )

        assertEquals("您的验证码是 123456", merged)
    }

    @Test
    fun fingerprint_matchesSameVerificationCodeInWindow() {
        assertTrue(
            SmsFingerprint.isDuplicate(
                senderA = "中国移动",
                bodyA = "您的验证码是 123456，请勿泄露",
                atA = 10_000L,
                senderB = "中国移动",
                bodyB = "验证码123456",
                atB = 11_000L
            )
        )
    }

    @Test
    fun fingerprint_keepsDifferentVerificationCodesInWindow() {
        assertFalse(
            SmsFingerprint.isDuplicate(
                senderA = "中国移动",
                bodyA = "您的验证码是 123456，请勿泄露",
                atA = 10_000L,
                senderB = "中国移动",
                bodyB = "您的验证码是 654321，请勿泄露",
                atB = 11_000L
            )
        )
    }
}
