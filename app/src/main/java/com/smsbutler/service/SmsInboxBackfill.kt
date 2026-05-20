package com.smsbutler.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.smsbutler.data.local.PreferencesManager
import com.smsbutler.data.repository.SmsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsInboxBackfill @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SmsRepository,
    private val preferences: PreferencesManager
) {
    suspend fun syncRecentInbox(limit: Int = 100) {
        if (!canReadSms()) return

        val prefs = preferences.preferences.first()
        val currentMaxId = queryMaxSmsId()
        if (currentMaxId <= 0L) return

        if (prefs.lastInboxSmsId <= 0L) {
            preferences.setInboxSyncWatermark(System.currentTimeMillis(), currentMaxId)
            Log.d("SmsButler", "Initialized inbox sync id watermark at $currentMaxId")
            return
        }

        val rows = queryNewInboxRows(afterId = prefs.lastInboxSmsId, limit = limit)
        val resolver = ReceiverPhoneResolver(context)
        var newestSeenId = prefs.lastInboxSmsId
        var newestSeenAt = prefs.lastInboxSyncAt

        rows.forEach { row ->
            if (row.id > newestSeenId) newestSeenId = row.id
            if (row.receivedAt > newestSeenAt) newestSeenAt = row.receivedAt

            val existingRecords = repository.getRecordsAround(row.receivedAt)

            if (SmsFingerprint.hasDuplicate(row.senderPhone, row.body, row.receivedAt, existingRecords)) {
                // 已有记录但 receiver 为空 → 补填 SIM 信息
                val receiverPhone = resolver.resolve(
                    subscriptionId = row.subscriptionId,
                    myPhoneNumbers = prefs.myPhoneNumbers
                )
                if (receiverPhone.isNotBlank()) {
                    patchEmptyReceivers(existingRecords, row, receiverPhone)
                }
                return@forEach
            }

            val receiverPhone = resolver.resolve(
                subscriptionId = row.subscriptionId,
                myPhoneNumbers = prefs.myPhoneNumbers
            )
            val record = SmsRecordFactory.buildRecord(
                senderPhone = row.senderPhone,
                messageBody = row.body,
                receiverPhone = receiverPhone,
                recordContent = prefs.recordContent,
                receivedAt = row.receivedAt
            )
            repository.insertRecord(record)
            Log.d("SmsButler", "Backfilled SMS: id=${row.id}, sender=${row.senderPhone}, receiver=$receiverPhone")
        }

        if (newestSeenId > prefs.lastInboxSmsId) {
            preferences.setInboxSyncWatermark(
                timestamp = maxOf(newestSeenAt, System.currentTimeMillis()),
                smsId = newestSeenId
            )
        }
    }

    suspend fun forceSyncRecentInbox(limit: Int = 200) {
        if (!canReadSms()) return

        val maxId = queryMaxSmsId()
        if (maxId <= 0L) return

        val startId = (maxId - limit).coerceAtLeast(0L)
        val rows = queryNewInboxRows(afterId = startId, limit = limit)
        val prefs = preferences.preferences.first()
        val resolver = ReceiverPhoneResolver(context)
        var newestSeenId = prefs.lastInboxSmsId
        var newestSeenAt = prefs.lastInboxSyncAt

        rows.forEach { row ->
            if (row.id > newestSeenId) newestSeenId = row.id
            if (row.receivedAt > newestSeenAt) newestSeenAt = row.receivedAt
            val existingRecords = repository.getRecordsAround(row.receivedAt)

            if (SmsFingerprint.hasDuplicate(row.senderPhone, row.body, row.receivedAt, existingRecords)) {
                val receiverPhone = resolver.resolve(
                    subscriptionId = row.subscriptionId,
                    myPhoneNumbers = prefs.myPhoneNumbers
                )
                if (receiverPhone.isNotBlank()) {
                    patchEmptyReceivers(existingRecords, row, receiverPhone)
                }
                return@forEach
            }

            val receiverPhone = resolver.resolve(
                subscriptionId = row.subscriptionId,
                myPhoneNumbers = prefs.myPhoneNumbers
            )
            repository.insertRecord(
                SmsRecordFactory.buildRecord(
                    senderPhone = row.senderPhone,
                    messageBody = row.body,
                    receiverPhone = receiverPhone,
                    recordContent = prefs.recordContent,
                    receivedAt = row.receivedAt
                )
            )
            Log.d("SmsButler", "Force synced SMS: id=${row.id}, sender=${row.senderPhone}")
        }

        preferences.setInboxSyncWatermark(
            timestamp = maxOf(newestSeenAt, System.currentTimeMillis()),
            smsId = maxOf(newestSeenId, maxId)
        )
    }

    private fun canReadSms(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun queryMaxSmsId(): Long {
        return queryMaxSmsId(Telephony.Sms.Inbox.CONTENT_URI).takeIf { it > 0L }
            ?: queryMaxSmsId(Telephony.Sms.CONTENT_URI)
    }

    private fun queryMaxSmsId(uri: Uri): Long {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(Telephony.Sms._ID),
            null,
            null,
            "${Telephony.Sms._ID} DESC LIMIT 1"
        ) ?: return 0L

        cursor.use {
            return if (it.moveToFirst()) it.getLongOrNull(Telephony.Sms._ID) ?: 0L else 0L
        }
    }

    private fun queryNewInboxRows(afterId: Long, limit: Int): List<InboxSms> {
        val rows = queryNewInboxRows(
            uri = Telephony.Sms.Inbox.CONTENT_URI,
            afterId = afterId,
            limit = limit,
            requireInboxType = false
        )
        if (rows.isNotEmpty()) return rows

        return queryNewInboxRows(
            uri = Telephony.Sms.CONTENT_URI,
            afterId = afterId,
            limit = limit,
            requireInboxType = true
        )
    }

    private fun queryNewInboxRows(
        uri: Uri,
        afterId: Long,
        limit: Int,
        requireInboxType: Boolean
    ): List<InboxSms> {
        val projectionWithSubId = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.SUBSCRIPTION_ID
        )
        val projectionWithoutSubId = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        return runCatching {
            queryRows(
                uri = uri,
                projection = projectionWithSubId,
                hasSubscriptionId = true,
                afterId = afterId,
                limit = limit,
                requireInboxType = requireInboxType
            )
        }.recoverCatching {
            queryRows(
                uri = uri,
                projection = projectionWithoutSubId,
                hasSubscriptionId = false,
                afterId = afterId,
                limit = limit,
                requireInboxType = requireInboxType
            )
        }.getOrElse { error ->
            Log.w("SmsButler", "Inbox sync query failed: ${error.message}")
            emptyList()
        }
    }

    private fun queryRows(
        uri: Uri,
        projection: Array<String>,
        hasSubscriptionId: Boolean,
        afterId: Long,
        limit: Int,
        requireInboxType: Boolean
    ): List<InboxSms> {
        val selection = if (requireInboxType) {
            "${Telephony.Sms._ID} > ? AND ${Telephony.Sms.TYPE} = ?"
        } else {
            "${Telephony.Sms._ID} > ?"
        }
        val selectionArgs = if (requireInboxType) {
            arrayOf(afterId.toString(), Telephony.Sms.MESSAGE_TYPE_INBOX.toString())
        } else {
            arrayOf(afterId.toString())
        }

        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms._ID} ASC LIMIT $limit"
        ) ?: return emptyList()

        cursor.use {
            return buildList {
                while (it.moveToNext()) {
                    val id = it.getLongOrNull(Telephony.Sms._ID) ?: continue
                    val type = it.getIntOrNull(Telephony.Sms.TYPE)
                    if (requireInboxType && type != Telephony.Sms.MESSAGE_TYPE_INBOX) continue

                    val sender = it.getStringOrNull(Telephony.Sms.ADDRESS)?.takeIf(String::isNotBlank)
                        ?: continue
                    val body = it.getStringOrNull(Telephony.Sms.BODY).orEmpty()
                    val date = it.getLongOrNull(Telephony.Sms.DATE) ?: continue
                    val subId = if (hasSubscriptionId) {
                        it.getIntOrNull(Telephony.Sms.SUBSCRIPTION_ID)
                            ?: SubscriptionManager.INVALID_SUBSCRIPTION_ID
                    } else {
                        SubscriptionManager.INVALID_SUBSCRIPTION_ID
                    }

                    add(
                        InboxSms(
                            id = id,
                            senderPhone = sender,
                            body = body,
                            receivedAt = date,
                            subscriptionId = subId
                        )
                    )
                }
            }
        }
    }

    private fun Cursor.getStringOrNull(column: String): String? {
        val index = getColumnIndex(column)
        return if (index >= 0) getString(index) else null
    }

    private fun Cursor.getLongOrNull(column: String): Long? {
        val index = getColumnIndex(column)
        return if (index >= 0) getLong(index) else null
    }

    private fun Cursor.getIntOrNull(column: String): Int? {
        val index = getColumnIndex(column)
        return if (index >= 0) getInt(index) else null
    }

    private suspend fun patchEmptyReceivers(
        existing: List<SmsRecordEntity>,
        row: InboxSms,
        receiverPhone: String
    ) {
        existing.forEach { record ->
            if (record.receiverPhoneNumber.isBlank() &&
                SmsFingerprint.isDuplicate(
                    senderA = row.senderPhone,
                    bodyA = row.body,
                    atA = row.receivedAt,
                    senderB = record.phoneNumber,
                    bodyB = record.content,
                    atB = record.receivedAt
                )
            ) {
                repository.updateReceiverPhone(record.id, receiverPhone)
                Log.d("SmsButler", "Patched receiver for record #${record.id}: $receiverPhone")
            }
        }
    }
}

private data class InboxSms(
    val id: Long,
    val senderPhone: String,
    val body: String,
    val receivedAt: Long,
    val subscriptionId: Int
)
