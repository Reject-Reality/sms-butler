package com.smsbutler.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object SmsPermissions {
    val requiredRuntimePermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.READ_PHONE_STATE
    )

    fun canReceiveSms(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.RECEIVE_SMS)
    }

    fun canReadPhoneNumbers(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.READ_PHONE_NUMBERS) ||
            hasPermission(context, Manifest.permission.READ_PHONE_STATE)
    }

    fun canReadSms(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.READ_SMS)
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
