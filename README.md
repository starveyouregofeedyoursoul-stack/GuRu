# GuRu – Workouts · Meals · Reminders · Health

Android app (Kotlin + Jetpack Compose). Dark, cinematic "TV-style" interface with glass cards,
snap carousels, spring animations and haptics. Logo: a middle-aged man meditating in front of a turning Milky Way.

## Open & run
1. Install **Android Studio** (Ladybug 2024.2 or newer).
2. File → Open → select this `GuRu` folder. Let Gradle sync (it downloads dependencies).
3. Plug in an Android phone (Android 9+) with USB debugging on, or start an emulator → press ▶ Run.
4. To get an installable APK: Build → Build App Bundle(s)/APK(s) → Build APK(s).

If Studio offers to upgrade the Android Gradle Plugin or library versions, accept – it's safe.

## Tabs
| Tab | What it does |
|---|---|
| **Plan** | Month calendar. Dots = workouts (blue) and dinners (orange). Tap a day for its workouts, meals and alerts. ▶ starts a guided session; 📅 sends it to Google/Samsung Calendar. |
| **Train** | Featured carousel + shelves by type (Strength, HIIT, Cardio, Mobility, Pilates, Trend: 12-3-30, Hyrox, Zone 2, rucking…). Open a workout to see **how to do every move** (steps, cue, mistake to avoid, video demo). **Start guided** runs a work/rest timer move by move. Schedule as a repeating program (days × weeks × time, with optional 15-min heads-up alert). Create/edit your own workouts; quick-add moves from the 39-exercise how-to library. |
| **Eat** | Dinners & smoothies by prep style (sheet pan, one pot, air fryer, slow cooker, no-cook, batch prep, grill, blender). Filter by time and goal tag. **Auto-plan** fills 7 dinners. **Shopping list** merges ingredients and shares to any app. **Search recipes** opens a web search using your filters. Add your own recipes. |
| **Alerts** | Repeating alerts for meds, supplements, workouts, smoothies, meals, water. Pick days of the week. Done / Snooze 10 min from the notification or your watch. Survives reboots. |
| **Health** | Connects to **Health Connect** – the hub used by Samsung Health, Galaxy Watch, Pixel Watch/Fitbit, Google Fit, Garmin, Oura, Withings, MyFitnessPal and more. Shows today's steps, active calories, avg heart rate, and the last 7 days of workouts from any app/watch. Completed GuRu workouts are written back automatically. Log water. |

## What's new
- **Your profile (Health tab):** weight, height, age, sex, activity and goal, with optional BMI. It's for one person and stays on the phone, with no account or sign-in. It sets daily calorie and protein targets (Mifflin-St Jeor formula), flags meals that fit, steers Auto-plan toward them, and adds a "Recommended for you" workout row.
- **Macros:** every recipe shows calories, protein, carbs and fat per serving (estimates). You can edit them for your own recipes.
- **Calorie burn:** every workout shows an estimate based on its MET value and your weight.
- **More presets:** 28 workouts, 20 dinners, 10 smoothies and 60 how-to guides. `tools/presets.json` is the source for both this app and the web version.
- The database schema changed to v2. Existing test installs re-seed their presets automatically.

## Exercise demos and Sleep
- **Built-in exercise animations:** every one of the 65 how-to guides shows a looping animated figure, drawn by the app from keyframe poses in `data/Poses.kt` (generated from `tools/poses.py`). No video, no network. They appear in the guide cards, the workout detail and the guided player.
- **Sleep tab:** log bedtime, wake time, how rested you feel, wake-ups and possible factors (caffeine, alcohol, late meal, screens, stress, naps, pain), plus optional CPAP/myAir numbers (usage hours, events per hour, mask on/off, myAir score, mask seal). It can fill bed and wake times from Health Connect (`READ_SLEEP`). It spots patterns over the last 14 nights, plans tonight (lights-out, wind-down and caffeine cutoff from your wake time and sleep goal), offers a 12-minute guided wind-down stretch, a before-bed checklist and evening eating guidance, and can add wind-down and caffeine alerts.
- **myAir note:** ResMed myAir has no public API and doesn't share CPAP therapy data with Health Connect, so those numbers are entered by hand. (The web version in Claude can also read them from a myAir screenshot.)
- The database schema is now v3 (adds `SleepEntry`).

## Water bottle
The Health tab shows a bottle that fills toward a personal daily water goal: about 1 ml per calorie you burn (from your profile's height, weight, age, sex and activity), or about 0.5 oz per lb without a profile, plus about 12 ml per workout minute scheduled today (clamped to 1.5-5 L). Quick adds are 8, 16.9 and 24 oz (or ml), with a custom amount, undo and an oz/ml toggle. Water is stored on the phone and also written to Health Connect when connected.

## Project map
```
app/src/main/java/com/guru/app/
  data/        Room database, entities, preset workouts/meals, exercise how-to guides
  reminders/   AlarmManager scheduling, notification + Done/Snooze, reboot restore
  health/      Health Connect read/write, calendar export
  ui/          Theme, logo (animated), screens, guided workout player
app/src/main/res/drawable/   launcher icon (vector) + monochrome notification icon
```

## Customize
- Add preset workouts/meals: `data/Presets.kt` (seeded on first launch; users can also add in-app).
- Add how-to guides: `data/ExerciseGuides.kt` – keywords map free-text lines like "Goblet squat 3x12" to a guide.
- Colors & feel: `Guru` object and `GlassCard` in `ui/Root.kt`.
