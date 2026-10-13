package de.motivationcode.app.reminders

import de.motivationcode.app.data.settings.ReminderSettings
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderTimesTest {

    private fun t(h: Int, m: Int = 0) = LocalTime.of(h, m)
    private fun at(day: Int, h: Int, m: Int = 0) = LocalDateTime.of(2026, 10, day, h, m)

    @Test
    fun `three reminders between 9 and 21 are evenly spread`() {
        assertEquals(listOf(0, 360, 720), ReminderTimes.slotOffsets(3, t(9), t(21)))
    }

    @Test
    fun `one reminder is at the start time`() {
        assertEquals(listOf(0), ReminderTimes.slotOffsets(1, t(9), t(21)))
    }

    @Test
    fun `window across midnight works`() {
        assertEquals(listOf(0, 120, 240), ReminderTimes.slotOffsets(3, t(22), t(2)))
    }

    @Test
    fun `count is clamped to the allowed range`() {
        assertEquals(ReminderSettings.MAX_PER_DAY, ReminderTimes.slotOffsets(99, t(8), t(20)).size)
        assertEquals(1, ReminderTimes.slotOffsets(0, t(8), t(20)).size)
    }

    @Test
    fun `next reminder later today`() {
        val settings = ReminderSettings(enabled = true, perDay = 3, start = t(9), end = t(21))
        assertEquals(at(13, 15), ReminderTimes.nextReminder(at(13, 10), settings))
    }

    @Test
    fun `next reminder is strictly after now`() {
        val settings = ReminderSettings(enabled = true, perDay = 3, start = t(9), end = t(21))
        assertEquals(at(13, 21), ReminderTimes.nextReminder(at(13, 15), settings))
    }

    @Test
    fun `after the last reminder comes tomorrow morning`() {
        val settings = ReminderSettings(enabled = true, perDay = 3, start = t(9), end = t(21))
        assertEquals(at(14, 9), ReminderTimes.nextReminder(at(13, 21, 30), settings))
    }

    @Test
    fun `window across midnight continues after midnight`() {
        val settings = ReminderSettings(enabled = true, perDay = 3, start = t(22), end = t(2))
        assertEquals(at(14, 2), ReminderTimes.nextReminder(at(14, 0, 30), settings))
    }
}
