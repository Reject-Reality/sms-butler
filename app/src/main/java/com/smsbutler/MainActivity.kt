package com.smsbutler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.smsbutler.service.SmsInboxBackfill
import com.smsbutler.ui.navigation.SmsButlerNavGraph
import com.smsbutler.ui.theme.SmsButlerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var smsInboxBackfill: SmsInboxBackfill
    private var smsSyncJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmsButlerTheme {
                SmsButlerNavGraph()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        smsSyncJob?.cancel()
        smsSyncJob = lifecycleScope.launch {
            while (isActive) {
                smsInboxBackfill.syncRecentInbox()
                delay(15_000)
            }
        }
    }

    override fun onPause() {
        smsSyncJob?.cancel()
        smsSyncJob = null
        super.onPause()
    }
}
