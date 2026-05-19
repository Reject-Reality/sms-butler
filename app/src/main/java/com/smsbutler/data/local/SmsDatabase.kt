package com.smsbutler.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SmsRecordEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SmsDatabase : RoomDatabase() {
    abstract fun smsRecordDao(): SmsRecordDao
}
