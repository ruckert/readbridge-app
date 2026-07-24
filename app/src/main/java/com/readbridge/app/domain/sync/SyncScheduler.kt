package com.readbridge.app.domain.sync

/** Schedules background sync. Implemented with WorkManager in the data layer. */
interface SyncScheduler {
    /** Kick off a one-off sync now (drains the outbox, then pulls updates), network permitting. */
    fun requestSync()

    /** Ensure the recurring background sync is scheduled (idempotent). */
    fun ensurePeriodicSync()
}
