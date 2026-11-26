package com.guru.app.data

import android.content.Context
import com.guru.app.reminders.ReminderScheduler
import java.time.DayOfWeek
import java.time.LocalDate

class Repository(private val context: Context) {
    val dao = GuruDatabase.get(context).dao()

    suspend fun seedIfEmpty() {
        if (dao.templateCount() == 0) Presets.workouts.forEach { dao.upsertTemplate(it) }
        if (dao.mealCount() == 0) Presets.meals.forEach { dao.upsertMeal(it) }
        if (dao.allReminders().isEmpty()) Presets.reminders.forEach { saveReminder(it.copy(enabled = false)) }
    }

    /**
     * Turns a template into a repeating program on the calendar.
     * e.g. "Full Body 5x5" on Mon/Wed/Fri at 6:30am for 6 weeks.
     */
    suspend fun applyTemplate(
        t: WorkoutTemplate, start: LocalDate, weeks: Int, days: Set<DayOfWeek>, minuteOfDay: Int,
    ) {
        val items = (0 until weeks * 7).map { start.plusDays(it.toLong()) }
            .filter { it.dayOfWeek in days }
            .map {
                ScheduledWorkout(
                    templateId = t.id, title = t.name, category = t.category, epochDay = it.toEpochDay(),
                    startMinuteOfDay = minuteOfDay, durationMin = t.durationMin, exercises = t.exercises, met = t.met,
                )
            }
        dao.insertWorkouts(items)
    }

    /** Fills empty dinner slots for the next 7 days using the user's filters. */
    suspend fun autoPlanWeek(start: LocalDate, maxPrep: Int, tag: String?, prepType: String?, targets: Targets? = null) {
        val all = dao.allMeals().filter { m ->
            m.kind == "Dinner" && m.prepMinutes <= maxPrep &&
                (tag.isNullOrBlank() || m.tags.contains(tag, ignoreCase = true)) &&
                (prepType.isNullOrBlank() || m.prepType == prepType)
        }.shuffled()
        // prefer dinners close to the owner's per-dinner calorie target
        val pool = all.filter { ProfileMath.fits(it, targets) }.takeIf { it.size >= 3 } ?: all
        if (pool.isEmpty()) return
        for (i in 0 until 7) {
            val day = start.plusDays(i.toLong()).toEpochDay()
            dao.clearSlot(day, "Dinner")
            dao.insertPlanned(PlannedMeal(mealId = pool[i % pool.size].id, epochDay = day, slot = "Dinner"))
        }
    }

    /** Aggregated shopping list for planned meals in a date range. */
    fun shoppingList(meals: List<Meal>, planned: List<PlannedMeal>): List<String> {
        val byId = meals.associateBy { it.id }
        return planned.mapNotNull { byId[it.mealId] }
            .flatMap { it.ingredients.lines() }
            .map { it.trim() }.filter { it.isNotEmpty() }
            .groupingBy { it.lowercase() }.eachCount()
            .map { (item, n) -> if (n > 1) "$item  ×$n" else item }
            .sorted()
    }

    suspend fun saveReminder(r: Reminder) {
        val id = dao.upsertReminder(r).let { if (it == -1L) r.id else it }
        val saved = r.copy(id = id)
        if (saved.enabled) ReminderScheduler.schedule(context, saved) else ReminderScheduler.cancel(context, saved.id)
    }

    suspend fun deleteReminder(r: Reminder) {
        ReminderScheduler.cancel(context, r.id)
        dao.deleteReminder(r)
    }
}
