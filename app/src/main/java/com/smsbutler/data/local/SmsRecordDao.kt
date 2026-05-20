package com.smsbutler.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class PhoneSummary(
    val phoneNumber: String,
    val receiverPhoneNumber: String,
    val count: Int
)

@Dao
interface SmsRecordDao {
    @Query("SELECT * FROM sms_records ORDER BY receivedAt DESC")
    fun getAllRecords(): Flow<List<SmsRecordEntity>>

    @Query("SELECT * FROM sms_records WHERE phoneNumber LIKE '%' || :query || '%' OR sender LIKE '%' || :query || '%' ORDER BY receivedAt DESC")
    fun searchRecords(query: String): Flow<List<SmsRecordEntity>>

    @Query("SELECT * FROM sms_records WHERE phoneNumber = :phone ORDER BY receivedAt DESC")
    fun getRecordsByPhone(phone: String): Flow<List<SmsRecordEntity>>

    @Query("SELECT phoneNumber, receiverPhoneNumber, COUNT(*) as count FROM sms_records GROUP BY phoneNumber, receiverPhoneNumber ORDER BY receiverPhoneNumber, count DESC")
    fun getPhoneNumberSummary(): Flow<List<PhoneSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SmsRecordEntity)

    @Query("SELECT * FROM sms_records WHERE receivedAt BETWEEN :startAt AND :endAt ORDER BY receivedAt DESC")
    suspend fun getRecordsAround(startAt: Long, endAt: Long): List<SmsRecordEntity>

    @Query("UPDATE sms_records SET isStarred = :starred WHERE id = :id")
    suspend fun toggleStar(id: Long, starred: Boolean)

    @Query("UPDATE sms_records SET receiverPhoneNumber = :receiverPhone WHERE id = :id")
    suspend fun updateReceiverPhone(id: Long, receiverPhone: String)

    @Query("DELETE FROM sms_records")
    suspend fun deleteAll()
}
