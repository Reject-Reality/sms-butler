package com.smsbutler.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_records",
    indices = [
        Index(value = ["phoneNumber"]),
        Index(value = ["sender"]),
        Index(value = ["receiverPhoneNumber"])
    ]
)
data class SmsRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val sender: String,
    val receiverPhoneNumber: String = "",    // 接收短信的本机号码（多卡用户）
    val content: String? = null,
    val recordedContent: Boolean = false,
    val category: String? = null,
    val receivedAt: Long = System.currentTimeMillis(),
    val isStarred: Boolean = false,
    val appLabel: String? = null
)
