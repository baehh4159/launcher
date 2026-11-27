package com.desklauncher.feature.alarm.domain

import com.desklauncher.feature.alarm.domain.model.Alarm
import com.desklauncher.feature.alarm.ui.FakeAlarmRepository
import com.desklauncher.feature.alarm.ui.FakeAlarmScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmSyncTest {

    private val zone = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(LocalDateTime.of(2026, 11, 26, 7, 0, 5).atZone(zone).toInstant(), zone)
    private val repository = FakeAlarmRepository()
    private val scheduler = FakeAlarmScheduler()
    private val sync = AlarmSync(repository, scheduler, clock)

    // 한 번짜리는 울린 뒤 꺼진다
    @Test
    fun onFired_disablesOneShot() = runTest {
        sync.save(Alarm("a", LocalTime.of(7, 0), createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH))
        sync.onFired("a")
        val alarm = checkNotNull(repository.get("a"))
        assertFalse(alarm.enabled)
        assertNull(alarm.nextTriggerAt)
    }

    // 반복은 다음 회차를 예약한다 (목 7시에 울림 → 금 7시)
    @Test
    fun onFired_schedulesNextRepeat() = runTest {
        sync.save(Alarm("a", LocalTime.of(7, 0), setOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY), createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH))
        sync.onFired("a")
        assertEquals(LocalDateTime.of(2026, 11, 27, 7, 0).atZone(zone).toInstant(), scheduler.scheduled["a"])
    }

    // 재예약은 켜진 알람만
    @Test
    fun rescheduleAll_onlyEnabled() = runTest {
        repository.save(Alarm("on", LocalTime.of(8, 0), createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH))
        repository.save(Alarm("off", LocalTime.of(8, 0), enabled = false, createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH))
        sync.rescheduleAll()
        assertEquals(setOf("on"), scheduler.scheduled.keys)
    }
}
