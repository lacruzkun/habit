package com.focusflow.app.domain.time

import android.os.SystemClock
import java.time.Instant
import java.time.ZoneId

interface TimeSource {
    fun wallMillis(): Long
    fun elapsedRealtime(): Long
    fun zoneId(): ZoneId
}

object SystemTimeSource : TimeSource {
    override fun wallMillis(): Long = System.currentTimeMillis()
    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
    override fun zoneId(): ZoneId = ZoneId.systemDefault()
}

fun TimeSource.now(): Instant = Instant.ofEpochMilli(wallMillis())
