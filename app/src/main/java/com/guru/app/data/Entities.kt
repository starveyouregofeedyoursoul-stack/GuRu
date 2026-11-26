package com.guru.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A reusable workout "recipe" – presets ship with the app, users can add/edit their own. */
@Entity
data class WorkoutTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,          // Strength, HIIT, Cardio, Mobility, Trend...
    val durationMin: Int,
    val intensity: String,         // Easy / Moderate / Hard
    val exercises: String,         // one exercise per line
    val isPreset: Boolean = false,
    val met: Double = 5.0,         // metabolic equivalent, for calorie-burn estimates
)

/** A workout placed on a specific calendar day. */
@Entity
data class ScheduledWorkout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long?,
    val title: String,
    val category: String,
    val epochDay: Long,
    val startMinuteOfDay: Int,     // e.g. 7*60+30 = 7:30am
    val durationMin: Int,
    val exercises: String,
    val done: Boolean = false,
    val syncedToHealth: Boolean = false,
    val met: Double = 5.0,
)

/** A dinner or smoothie in the recipe library. */
@Entity
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: String,              // Dinner / Smoothie
    val prepType: String,          // Sheet Pan, One Pot, Air Fryer, Slow Cooker, No-Cook, Batch Prep, Blender
    val prepMinutes: Int,
    val tags: String,              // comma separated: high-protein, vegetarian, low-carb...
    val ingredients: String,       // one per line
    val steps: String,             // one per line
    val isPreset: Boolean = false,
    // per-serving nutrition estimates
    val servings: Int = 1,
    val kcal: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
)

/** A meal placed on a calendar day. */
@Entity
data class PlannedMeal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val epochDay: Long,
    val slot: String,              // Dinner / Smoothie
)

/** A repeating alert: meds, workout, smoothie, water, anything. */
@Entity
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String,              // Meds, Workout, Smoothie, Meal, Water, Other
    val hour: Int,
    val minute: Int,
    val daysMask: Int = 0b1111111, // bit0 = Monday ... bit6 = Sunday
    val note: String = "",
    val enabled: Boolean = true,
)

object Categories {
    val workout = listOf("Strength", "HIIT", "Cardio", "Mobility", "Yoga/Pilates", "Sport", "Trend", "Custom")
    val prepTypes = listOf("Sheet Pan", "One Pot", "Air Fryer", "Slow Cooker", "No-Cook", "Batch Prep", "Grill", "Blender")
    val reminderTypes = listOf("Meds", "Workout", "Smoothie", "Meal", "Water", "Supplements", "Other")
    val intensity = listOf("Easy", "Moderate", "Hard")
}
