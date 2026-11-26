package com.guru.app

import android.app.Application
import com.guru.app.data.Repository
import com.guru.app.health.HealthConnectManager
import com.guru.app.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GuruApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var repo: Repository
    lateinit var health: HealthConnectManager

    override fun onCreate() {
        super.onCreate()
        repo = Repository(this)
        health = HealthConnectManager(this)
        ReminderScheduler.createChannel(this)
        appScope.launch { repo.seedIfEmpty() }
    }
}
