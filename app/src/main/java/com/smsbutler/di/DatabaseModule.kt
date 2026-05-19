package com.smsbutler.di

import android.content.Context
import androidx.room.Room
import com.smsbutler.data.local.SmsDatabase
import com.smsbutler.data.local.SmsRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmsDatabase {
        return Room.databaseBuilder(
            context,
            SmsDatabase::class.java,
            "sms_butler.db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideSmsRecordDao(database: SmsDatabase): SmsRecordDao {
        return database.smsRecordDao()
    }
}
