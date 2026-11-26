package com.guru.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** One night, keyed by the morning you woke up. CPAP fields mirror what ResMed myAir shows. */
@Entity
data class SleepEntry(
    @PrimaryKey val epochDay: Long,
    val bedMin: Int,                 // minutes after midnight
    val wakeMin: Int,
    val quality: Int = 3,            // 1-5
    val wakeups: Int = 0,
    val factors: String = "",        // comma-separated keys from SleepCoach.factors
    val cpapHours: Double? = null,
    val ahi: Double? = null,         // events per hour
    val maskOnOff: Int? = null,
    val myAirScore: Int? = null,
    val seal: String = "",           // good / adjust
    val notes: String = "",
    val source: String = "manual",   // manual / health_connect
) {
    val minutes get() = ((wakeMin - bedMin) + 1440) % 1440
    fun has(f: String) = factors.split(",").contains(f)
}

object SleepCoach {
    val factors = listOf("caffeine" to "Caffeine after 2 PM", "alcohol" to "Alcohol", "lateMeal" to "Late or heavy meal",
        "screens" to "Screens in bed", "stress" to "Stressed", "nap" to "Napped", "pain" to "Pain or discomfort")
    val avoid = listOf("Caffeine within about 8 hours of bed: coffee, tea, energy drinks, pre-workout, dark chocolate",
        "Alcohol within 3 hours. It fragments sleep and can make snoring and sleep apnea worse",
        "Heavy, spicy or fatty meals within 2-3 hours (reflux and indigestion)",
        "Big drinks in the last 1-2 hours (fewer bathroom trips)", "Sugary desserts and snacks late at night")
    val better = listOf("If you're hungry: a small snack with protein and fiber, like Greek yogurt with oats or a banana with peanut butter",
        "Caffeine-free herbal tea such as chamomile", "Kiwi or tart cherries. Some small studies link them to better sleep",
        "Magnesium-rich foods at dinner: leafy greens, beans, nuts, seeds")
    val habits = listOf("Dim the lights an hour before bed", "Phone away 30-60 minutes before bed", "Bedroom cool (65-68°F), dark and quiet",
        "Do the wind-down stretch", "Write tomorrow's to-dos so your mind can let go", "CPAP mask clean and ready")
    val windDownMoves = listOf("Neck & Shoulder Rolls", "Cat-Cow", "Child's Pose", "Supine Spinal Twist", "Butterfly Stretch", "Legs Up the Wall", "4-7-8 Breathing")

    fun hm(m: Double) = if (m < 60) "${m.roundToInt()} min" else "${(m / 60).toInt()}h ${"%02d".format((m % 60).roundToInt())}m"

    /** Pattern-spotting over the last two weeks. Returns (isGood, message). */
    fun insights(nights: List<SleepEntry>, goalHours: Double, workedOutBefore: (Long) -> Boolean): List<Pair<Boolean, String>> {
        val n = nights.sortedBy { it.epochDay }.takeLast(14); if (n.size < 3) return emptyList()
        val out = mutableListOf<Pair<Boolean, String>>()
        val goal = goalHours * 60; val avg = n.map { it.minutes }.average()
        out += if (avg < goal - 30) false to "You're averaging ${hm(avg)} a night, about ${hm(goal - avg)} short of your ${goalHours.fmt()}-hour goal. Try moving bedtime 15 minutes earlier each week until you're there."
        else true to "You're averaging ${hm(avg)} a night, right around your ${goalHours.fmt()}-hour goal. Nice."
        val beds = n.map { if (it.bedMin < 720) it.bedMin + 1440 else it.bedMin }; val mb = beds.average()
        val sd = sqrt(beds.map { (it - mb) * (it - mb) }.average())
        if (sd > 50) out += false to "Your bedtime swings by about ${hm(sd)} from night to night. A steady bedtime, even on weekends, is one of the strongest habits for better sleep."
        fun q(l: List<SleepEntry>) = l.map { it.quality }.average()
        for ((k, label) in factors) {
            val yes = n.filter { it.has(k) }; val no = n.filterNot { it.has(k) }
            if (yes.size >= 2 && no.size >= 2 && q(no) - q(yes) >= .6)
                out += false to "On nights with \"${label.lowercase()}\", your sleep quality averages ${"%.1f".format(q(yes))} vs ${"%.1f".format(q(no))} without it."
        }
        val wo = n.filter { workedOutBefore(it.epochDay) }; val nwo = n.filterNot { workedOutBefore(it.epochDay) }
        if (wo.size >= 2 && nwo.size >= 2 && q(wo) - q(nwo) >= .5)
            out += true to "You sleep better after workout days (${"%.1f".format(q(wo))} vs ${"%.1f".format(q(nwo))}). Keep training, ideally finishing hard sessions 2+ hours before bed."
        val wk = n.map { it.wakeups }.average()
        if (wk >= 2) out += false to "You're waking about ${"%.1f".format(wk)} times a night. Cut fluids 1-2 hours before bed, keep the room cool and dark, and limit alcohol."
        val wd = n.filter { LocalDate.ofEpochDay(it.epochDay).dayOfWeek.value <= 5 }; val we = n - wd.toSet()
        if (wd.size >= 2 && we.isNotEmpty()) {
            val diff = we.map { it.wakeMin }.average() - wd.map { it.wakeMin }.average()
            if (abs(diff) > 90) out += false to "You wake about ${hm(abs(diff))} ${if (diff > 0) "later" else "earlier"} on weekends. That \"social jet lag\" can make Mondays rough. Try to keep it within an hour."
        }
        val cp = n.filter { it.cpapHours != null }
        if (cp.size >= 3) {
            val ok = cp.count { (it.cpapHours ?: 0.0) >= 4 }; val pct = ok * 100 / cp.size
            out += (pct >= 70) to "CPAP used 4+ hours on $ok of ${cp.size} nights ($pct%)." +
                if (pct < 70) " Wearing it all night gets you the full benefit. If it feels uncomfortable, ask your provider about mask fit or humidity." else ""
            val ahi = cp.mapNotNull { it.ahi }
            if (ahi.size >= 3 && ahi.average() > 5) out += false to "Your events per hour average ${"%.1f".format(ahi.average())}. Above 5 is worth mentioning to your sleep doctor; your settings or mask may need adjusting."
            if (cp.count { it.seal == "adjust" } >= 2) out += false to "Mask seal needed adjusting on several nights. Refit the mask while lying in your sleep position, and replace cushions on schedule."
        }
        if (n.size >= 6) { val h = n.size / 2; val a = q(n.take(h)); val b = q(n.drop(h))
            if (b - a >= .5) out += true to "Your sleep quality is trending up (${"%.1f".format(a)} → ${"%.1f".format(b)})." }
        return out
    }
    private fun Double.fmt() = if (this % 1.0 == 0.0) toInt().toString() else toString()
}
