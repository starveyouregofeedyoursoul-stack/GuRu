package com.guru.app.data

import android.content.Context
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * The single owner of this device. Stored locally in SharedPreferences – no account, sign-in or cloud.
 */
data class Profile(
    val name: String = "",
    val weightLb: Double = 170.0,
    val heightIn: Int = 0,
    val age: Int = 0,
    val sex: String = "na",          // male / female / na
    val activity: Double = 1.375,
    val goal: String = "maintain",   // lose / lose-slow / maintain / gain
    val showBmi: Boolean = false,
) {
    val isComplete get() = heightIn > 0 && age > 0
}

data class Targets(
    val bmr: Int, val tdee: Int, val kcal: Int, val protein: Int, val carbs: Int, val fat: Int,
    val bmi: Double, val dinner: Int, val shake: Int, val raisedToMinimum: Boolean,
)

object ProfileMath {
    val activities = listOf(1.2 to "Mostly sitting", 1.375 to "Light: exercise 1-3 days/week",
        1.55 to "Moderate: 3-5 days/week", 1.725 to "Very active: 6-7 days/week", 1.9 to "Athlete / physical job")
    val goals = listOf(Triple("lose", "Lose weight (~1 lb/week)", -500), Triple("lose-slow", "Lose slowly (~0.5 lb/week)", -250),
        Triple("maintain", "Maintain", 0), Triple("gain", "Build muscle / gain", 300))
    private val metDefault = mapOf("Easy" to 3.5, "Moderate" to 5.0, "Hard" to 7.0)

    /** Mifflin-St Jeor BMR × activity, adjusted for goal, with a safe floor. */
    fun targets(p: Profile): Targets? {
        if (!p.isComplete) return null
        val kg = p.weightLb * 0.4536; val cm = p.heightIn * 2.54
        val bmr = 10 * kg + 6.25 * cm - 5 * p.age + when (p.sex) { "male" -> 5.0; "female" -> -161.0; else -> -78.0 }
        val tdee = bmr * p.activity
        val delta = goals.firstOrNull { it.first == p.goal }?.third ?: 0
        val floor = if (p.sex == "male") 1500 else 1200
        val kcal = (max(floor.toDouble(), tdee + delta) / 10).roundToInt() * 10
        val protein = (kg * if (p.goal == "maintain") 1.4 else 1.8).roundToInt()
        val fat = (kcal * 0.28 / 9).roundToInt()
        val carbs = max(0, ((kcal - protein * 4 - fat * 9) / 4.0).roundToInt())
        return Targets(bmr.roundToInt(), tdee.roundToInt(), kcal, protein, carbs, fat, kg / ((cm / 100) * (cm / 100)),
            (kcal * 0.35).roundToInt(), (kcal * 0.15).roundToInt(), tdee + delta < floor)
    }

    fun bmiCategory(b: Double) = when { b < 18.5 -> "Underweight range"; b < 25 -> "Healthy weight range"; b < 30 -> "Overweight range"; else -> "Obesity range" }

    /** ACSM estimate: kcal/min = MET × 3.5 × kg / 200. */
    fun burn(met: Double?, minutes: Int, intensity: String?, weightLb: Double): Int =
        ((met ?: metDefault[intensity] ?: 5.0) * 3.5 * weightLb * 0.4536 / 200 * minutes).roundToInt()

    /**
     * Daily drinking goal in ml: about 1 ml per kcal burned (TDEE uses height, weight, age, sex, activity),
     * or ~35 ml/kg body weight without a full profile, plus ~12 ml per workout minute. Clamped to 1.5-5 L.
     */
    fun waterGoalMl(t: Targets?, weightLb: Double, workoutMin: Int): Int {
        val base = t?.tdee?.toDouble() ?: (weightLb * 0.4536 * 35)
        return ((base + workoutMin * 12).coerceIn(1500.0, 5000.0) / 50).roundToInt() * 50
    }

    fun fits(m: Meal, t: Targets?): Boolean {
        if (t == null || m.kcal == 0) return false
        val goal = if (m.kind == "Smoothie") t.shake else t.dinner
        return abs(m.kcal - goal) <= goal * 0.25
    }

    fun recommend(p: Profile, t: Targets?, all: List<WorkoutTemplate>): List<WorkoutTemplate> {
        if (!p.isComplete) return emptyList()
        val gentle = (t?.bmi ?: 0.0) >= 30 || p.age >= 55
        return all.sortedByDescending { w ->
            var sc = when {
                p.goal == "gain" -> if (w.category == "Strength") 3.0 else if (w.category == "Mobility") 1.0 else 0.0
                p.goal.startsWith("lose") -> if (w.category in listOf("Cardio", "HIIT", "Trend")) 2.0 else if (w.category == "Strength") 1.5 else 0.0
                else -> if (w.category == "Strength" || w.category == "Cardio") 1.5 else 1.0
            }
            if (gentle) sc += when (w.intensity) { "Hard" -> -3.0; "Easy" -> 1.5; else -> 0.5 }
            if (p.activity <= 1.375 && w.intensity == "Hard") sc -= 1.5
            sc
        }.take(6)
    }
}

class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("guru_profile", Context.MODE_PRIVATE)
    fun load() = Profile(
        name = prefs.getString("name", "") ?: "",
        weightLb = prefs.getFloat("weightLb", 170f).toDouble(),
        heightIn = prefs.getInt("heightIn", 0), age = prefs.getInt("age", 0),
        sex = prefs.getString("sex", "na") ?: "na", activity = prefs.getFloat("activity", 1.375f).toDouble(),
        goal = prefs.getString("goal", "maintain") ?: "maintain", showBmi = prefs.getBoolean("showBmi", false),
    )
    fun save(p: Profile) = prefs.edit()
        .putString("name", p.name).putFloat("weightLb", p.weightLb.toFloat()).putInt("heightIn", p.heightIn)
        .putInt("age", p.age).putString("sex", p.sex).putFloat("activity", p.activity.toFloat())
        .putString("goal", p.goal).putBoolean("showBmi", p.showBmi).apply()
    fun clear() = prefs.edit().clear().apply()

    fun water(day: Long) = prefs.getInt("water$day", 0)
    fun setWater(day: Long, ml: Int) = prefs.edit().putInt("water$day", ml).apply()
    var waterOz: Boolean
        get() = prefs.getBoolean("waterOz", true)
        set(v) = prefs.edit().putBoolean("waterOz", v).apply()

    // sleep goal: wake time (minutes after midnight) and hours
    var sleepWakeMin: Int
        get() = prefs.getInt("sleepWake", 6 * 60 + 30)
        set(v) = prefs.edit().putInt("sleepWake", v).apply()
    var sleepHours: Float
        get() = prefs.getFloat("sleepHours", 8f)
        set(v) = prefs.edit().putFloat("sleepHours", v).apply()
    fun checklist(day: Long): Set<Int> = prefs.getStringSet("chk$day", emptySet())!!.map { it.toInt() }.toSet()
    fun setChecklist(day: Long, items: Set<Int>) = prefs.edit().putStringSet("chk$day", items.map { it.toString() }.toSet()).apply()
}
