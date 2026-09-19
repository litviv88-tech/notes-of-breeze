package com.breez.notes.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.breez.notes.di.ReminderEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val entry = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    ReminderEntryPoint::class.java
                )
                entry.noteRepository().getAll().forEach { note ->
                    entry.reminderCoordinator().schedule(note)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
