package com.guru.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guru.app.GuruApp
import com.guru.app.data.*
import com.guru.app.health.HealthConnectManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
class GuruViewModel(app: Application) : AndroidViewModel(app) {
    private val guru = app as GuruApp
    private val repo = guru.repo
    private val dao = repo.dao
    val health = guru.health

    val month = MutableStateFlow(YearMonth.now())
    val selectedDay = MutableStateFlow(LocalDate.now())

    val templates = dao.templates().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val meals = dao.meals().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val reminders = dao.reminders().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Load a window around the visible month (covers grid overflow days)
    private val window = month.map { it.atDay(1).minusDays(7).toEpochDay() to it.atEndOfMonth().plusDays(14).toEpochDay() }
    val workouts = window.flatMapLatest { (a, b) -> dao.workoutsBetween(a, b) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val planned = window.flatMapLatest { (a, b) -> dao.plannedBetween(a, b) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Single-owner profile, stored only on this device
    private val profileStore = ProfileStore(app)
    val profile = MutableStateFlow(profileStore.load())
    val targets = profile.map { ProfileMath.targets(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, ProfileMath.targets(profile.value))
    fun saveProfile(p: Profile) { profileStore.save(p); profile.value = p; message.value = "Profile saved" }
    fun clearProfile() { profileStore.clear(); profile.value = Profile(); message.value = "Profile cleared" }
    fun burnFor(t: WorkoutTemplate) = ProfileMath.burn(t.met, t.durationMin, t.intensity, profile.value.weightLb)
    fun burnFor(w: ScheduledWorkout) = ProfileMath.burn(w.met, w.durationMin, null, profile.value.weightLb)

    // Sleep
    val sleep = dao.sleepSince(java.time.LocalDate.now().minusDays(60).toEpochDay()).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val sleepWake = MutableStateFlow(profileStore.sleepWakeMin)
    val sleepHours = MutableStateFlow(profileStore.sleepHours)
    fun setSleepGoal(wakeMin: Int, hours: Float) { profileStore.sleepWakeMin = wakeMin; profileStore.sleepHours = hours; sleepWake.value = wakeMin; sleepHours.value = hours; message.value = "Sleep goal updated" }
    fun bedtimeMin() = (sleepWake.value - (sleepHours.value * 60).toInt() - 15 + 1440) % 1440
    fun saveSleep(e: SleepEntry) = io { dao.upsertSleep(e); message.value = "Logged ${SleepCoach.hm(e.minutes.toDouble())}" }
    fun deleteSleep(e: SleepEntry) = io { dao.deleteSleep(e) }
    fun checklist(day: Long) = profileStore.checklist(day)
    fun setChecklist(day: Long, s: Set<Int>) = profileStore.setChecklist(day, s)
    fun addSleepAlerts() = io {
        val b = bedtimeMin()
        fun at(m: Int) = (m % 1440 + 1440) % 1440
        val existing = dao.allReminders().map { it.title }.toSet()
        if ("Start winding down" !in existing) repo.saveReminder(Reminder(title = "Start winding down", type = "Other", hour = at(b - 45) / 60, minute = at(b - 45) % 60, note = "Dim lights, phone away, wind-down stretch"))
        if ("Last call for caffeine" !in existing) repo.saveReminder(Reminder(title = "Last call for caffeine", type = "Other", hour = at(b - 480) / 60, minute = at(b - 480) % 60, note = "Switch to water or herbal tea"))
        message.value = "Sleep alerts added"
    }
    /** Pull last night's bed/wake times from Health Connect (watch, Samsung Health, etc.). */
    suspend fun sleepFromHealth(): Pair<java.time.LocalDateTime, java.time.LocalDateTime>? =
        try { if (health.hasAllPermissions()) health.lastSleep() else null } catch (e: Exception) { null }

    val healthSummary = MutableStateFlow<HealthConnectManager.Summary?>(null)
    val healthGranted = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)

    fun io(block: suspend () -> Unit) = viewModelScope.launch {
        try { block() } catch (e: Exception) { message.value = e.message ?: "Something went wrong" }
    }

    // Workouts
    fun saveTemplate(t: WorkoutTemplate) = io { dao.upsertTemplate(t) }
    fun deleteTemplate(t: WorkoutTemplate) = io { dao.deleteTemplate(t) }
    fun applyTemplate(t: WorkoutTemplate, start: LocalDate, weeks: Int, days: Set<DayOfWeek>, minute: Int) = io {
        repo.applyTemplate(t, start, weeks, days, minute)
        message.value = "Added ${t.name} to your calendar"
    }
    fun deleteWorkout(w: ScheduledWorkout) = io { dao.deleteWorkout(w) }
    fun toggleDone(w: ScheduledWorkout) = io {
        val done = !w.done
        var synced = w.syncedToHealth
        if (done && !synced && healthGranted.value) {
            health.writeWorkout(w); synced = true
            message.value = "Workout synced to Health Connect"
        }
        dao.upsertWorkout(w.copy(done = done, syncedToHealth = synced))
    }

    // Meals
    fun saveMeal(m: Meal) = io { dao.upsertMeal(m) }
    fun deleteMeal(m: Meal) = io { dao.deleteMeal(m) }
    fun planMeal(m: Meal, day: LocalDate) = io {
        dao.insertPlanned(PlannedMeal(mealId = m.id, epochDay = day.toEpochDay(), slot = m.kind))
        message.value = "${m.name} planned for ${day.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}"
    }
    fun unplan(p: PlannedMeal) = io { dao.deletePlanned(p) }
    fun autoPlan(maxPrep: Int, tag: String?, prepType: String?) = io {
        repo.autoPlanWeek(selectedDay.value, maxPrep, tag, prepType, targets.value)
        message.value = "Dinners planned for the next 7 days"
    }
    fun shoppingList(): List<String> {
        val from = selectedDay.value.toEpochDay(); val to = from + 6
        return repo.shoppingList(meals.value, planned.value.filter { it.epochDay in from..to })
    }

    // Reminders
    fun saveReminder(r: Reminder) = io { repo.saveReminder(r) }
    fun deleteReminder(r: Reminder) = io { repo.deleteReminder(r) }

    // Health
    fun refreshHealth() = io {
        healthGranted.value = health.hasAllPermissions()
        if (healthGranted.value) healthSummary.value = health.summary()
    }
    // Water: tracked on the phone; also written to Health Connect when connected
    private fun todayKey() = java.time.LocalDate.now().toEpochDay()
    val waterToday = MutableStateFlow(profileStore.water(todayKey()))
    val waterOz = MutableStateFlow(profileStore.waterOz)
    private var lastWater = 0
    fun waterGoalMl(): Int {
        val mins = workouts.value.filter { it.epochDay == todayKey() }.sumOf { it.durationMin }
        return ProfileMath.waterGoalMl(targets.value, profile.value.weightLb, mins)
    }
    fun addWater(ml: Int) = io {
        val before = profileStore.water(todayKey()); val now = before + ml
        profileStore.setWater(todayKey(), now); waterToday.value = now; lastWater = ml
        val goal = waterGoalMl()
        message.value = if (before < goal && now >= goal) "Bottle full! Daily water goal reached" else "+${fmtWater(ml)}"
        if (healthGranted.value) try { health.logWater(ml.toDouble()) } catch (_: Exception) {}
    }
    fun undoWater() { if (lastWater > 0) { val v = (profileStore.water(todayKey()) - lastWater).coerceAtLeast(0); profileStore.setWater(todayKey(), v); waterToday.value = v; lastWater = 0 } }
    fun toggleWaterUnit() { profileStore.waterOz = !waterOz.value; waterOz.value = !waterOz.value }
    fun fmtWater(ml: Int) = if (waterOz.value) "${(ml / 29.5735).roundToInt()} oz" else if (ml >= 1000) "%.2f L".format(ml / 1000.0) else "$ml ml"
}
