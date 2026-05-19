package com.smsbutler.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_records",
    indices = [
        Index(value = ["phoneNumber"]),
        Index(value = ["sender"])
    ]
)
data class SmsRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val sender: String,
    val content: String? = null,
    val recordedContent: Boolean = false,
    val category: String? = null,
    val receivedAt: Long = System.currentTimeMillis(),
    val isStarred: Boolean = false,
    val appLabel: String? = null
)
