package com.smsbutler.data.repository

import com.smsbutler.data.local.PhoneSummary
import com.smsbutler.data.local.SmsRecordDao
import com.smsbutler.data.local.SmsRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRepository @Inject constructor(
    private val dao: SmsRecordDao
) {
    fun getAllRecords(): Flow<List<SmsRecordEntity>> = dao.getAllRecords()

    fun searchRecords(query: String): Flow<List<SmsRecordEntity>> = dao.searchRecords(query)

    fun getRecordsByPhone(phone: String): Flow<List<SmsRecordEntity>> = dao.getRecordsByPhone(phone)

    fun getPhoneNumberSummary(): Flow<List<PhoneSummary>> = dao.getPhoneNumberSummary()

    suspend fun insertRecord(record: SmsRecordEntity) = dao.insert(record)

    suspend fun toggleStar(id: Long, starred: Boolean) = dao.toggleStar(id, starred)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun updateReceiverPhone(recordId: Long, receiverPhone: String) {
        // Future: allow manual changing of receiver phone
    }
}
