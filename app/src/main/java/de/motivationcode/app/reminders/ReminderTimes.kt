package de.motivationcode.app.reminders

import de.motivationcode.app.data.settings.ReminderSettings
import java.time.LocalDateTime
import java.time.LocalTime

/** Reine Zeitberechnung für die Erinnerungen, ohne Android-Abhängigkeiten (gut testbar). */
object ReminderTimes {
    private const val MINUTES_PER_DAY = 24 * 60

    /**
     * Minuten ab Startzeit, gleichmäßig zwischen Start und Ende verteilt (beide inklusive).
     * Liegt das Ende vor dem Start, geht das Zeitfenster über Mitternacht.
     */
    fun slotOffsets(perDay: Int, start: LocalTime, end: LocalTime): List<Int> {
        val count = perDay.coerceIn(ReminderSettings.MIN_PER_DAY, ReminderSettings.MAX_PER_DAY)
        val startMinute = start.hour * 60 + start.minute
        val endMinute = end.hour * 60 + end.minute
        val window = (endMinute - startMinute + MINUTES_PER_DAY) % MINUTES_PER_DAY
        if (count == 1 || window == 0) return listOf(0)
        return (0 until count).map { i -> window * i / (count - 1) }.distinct()
    }

    /** Nächster Erinnerungszeitpunkt strikt nach [now]. */
    fun nextReminder(now: LocalDateTime, settings: ReminderSettings): LocalDateTime {
        val offsets = slotOffsets(settings.perDay, settings.start, settings.end)
        // Gestern mitnehmen, falls das Fenster von gestern über Mitternacht noch läuft.
        return (-1L..1L).asSequence()
            .map { dayShift -> now.toLocalDate().plusDays(dayShift).atTime(settings.start) }
            .flatMap { windowStart -> offsets.asSequence().map { windowStart.plusMinutes(it.toLong()) } }
            .filter { it.isAfter(now) }
            .min()
    }
}
