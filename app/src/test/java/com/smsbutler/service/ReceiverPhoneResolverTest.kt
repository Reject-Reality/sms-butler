package com.smsbutler.service

import org.junit.Assert.assertEquals
import org.junit.Test

class ReceiverPhoneResolverTest {
    @Test
    fun resolveBestEffort_prefersSystemNumber() {
        val receiver = ReceiverPhoneResolver.resolveBestEffort(
            systemNumber = "13900000000",
            simSlotIndex = 1,
            myPhoneNumbers = listOf("13800000000")
        )

        assertEquals("13900000000", receiver)
    }

    @Test
    fun resolveBestEffort_fallsBackToSingleConfiguredNumber() {
        val receiver = ReceiverPhoneResolver.resolveBestEffort(
            systemNumber = null,
            simSlotIndex = null,
            myPhoneNumbers = listOf("13800000000")
        )

        assertEquals("13800000000", receiver)
    }

    @Test
    fun resolveBestEffort_doesNotGuessWhenMultipleNumbersConfigured() {
        val receiver = ReceiverPhoneResolver.resolveBestEffort(
            systemNumber = null,
            simSlotIndex = null,
            myPhoneNumbers = listOf("13800000000", "13900000000")
        )

        assertEquals("", receiver)
    }

    @Test
    fun resolveBestEffort_usesSimSlotWhenNumberIsHidden() {
        val receiver = ReceiverPhoneResolver.resolveBestEffort(
            systemNumber = null,
            simSlotIndex = 1,
            myPhoneNumbers = listOf("13800000000", "13900000000")
        )

        assertEquals("13900000000", receiver)
    }
}
