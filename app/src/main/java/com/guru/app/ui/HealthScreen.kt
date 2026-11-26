package com.guru.app.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.guru.app.data.Profile
import com.guru.app.data.ProfileMath

@Composable
fun HealthScreen(vm: GuruViewModel) {
    val ctx = LocalContext.current
    val granted by vm.healthGranted.collectAsStateWithLifecycle()
    val summary by vm.healthSummary.collectAsStateWithLifecycle()
    val status = remember { vm.health.status() }
    val launcher = rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()) { vm.refreshHealth() }
    LaunchedEffect(Unit) { vm.refreshHealth() }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)) {
        item { ProfileSection(vm) }
        item { SectionHeader("Water today", "Also saved to Health Connect when connected"); WaterCard(vm) }
        item {
            GlassCard(Modifier.fillMaxWidth(), highlight = true) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Health Connect", style = MaterialTheme.typography.titleLarge)
                    Text("GuRu syncs through Android's Health Connect, so it works with Samsung Health, Galaxy Watch, " +
                        "Pixel Watch / Fitbit, Google Fit, Garmin, Oura, Withings, MyFitnessPal and any other connected app.",
                        color = Guru.TextDim, style = MaterialTheme.typography.bodySmall)
                    when {
                        status == HealthConnectClient.SDK_UNAVAILABLE ->
                            Text("Health Connect isn't supported on this device.", color = Guru.Ember)
                        status == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                            GradientButton("Install / update Health Connect", Icons.Default.Download) { ctx.startActivity(vm.health.installIntent()) }
                        !granted -> GradientButton("Connect my health apps & watch", Icons.Default.Link) { launcher.launch(vm.health.permissions) }
                        else -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Guru.Mint); Spacer(Modifier.width(6.dp))
                            Text("Connected – completed workouts sync automatically", color = Guru.Mint)
                        }
                    }
                }
            }
        }
        if (granted) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("Steps", summary?.stepsToday?.toString() ?: "–", Icons.Default.DirectionsWalk, Guru.Aurora, Modifier.weight(1f))
                    StatTile("Active kcal", summary?.activeKcalToday?.toInt()?.toString() ?: "–", Icons.Default.LocalFireDepartment, Guru.Ember, Modifier.weight(1f))
                    StatTile("Avg BPM", summary?.avgHeartRateToday?.toString() ?: "–", Icons.Default.Favorite, Color(0xFFFF6B8B), Modifier.weight(1f))
                }
            }
            item { SectionHeader("Recent workouts from all apps & watches", "Last 7 days") }
            val sessions = summary?.recentSessions.orEmpty()
            if (sessions.isEmpty()) item { Text("No sessions yet.", color = Guru.TextDim) }
            items(sessions) { s ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Watch, null, tint = Guru.Aurora); Spacer(Modifier.width(10.dp)); Text(s)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ vm.refreshHealth() }) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Refresh") }
                    OutlinedButton({ ctx.startActivity(Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)) }) { Text("Manage access") }
                }
            }
        }
        item {
            SectionHeader("Other ways GuRu connects")
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("⌚  Alerts appear on Wear OS & Galaxy watches with Done / Snooze buttons")
                    Text("📅  Any scheduled workout can be sent to Google or Samsung Calendar")
                    Text("🛒  Shopping lists share to Keep, Notes, WhatsApp, etc.")
                    Text("▶️  Every move links to a video demo")
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, icon: ImageVector, tint: Color, modifier: Modifier) {
    GlassCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, null, tint = tint)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Guru.TextDim)
        }
    }
}

/** One-person profile: lives only on this phone, no account or sign-in. Drives targets, meal fit and workout picks. */
@Composable
private fun ProfileSection(vm: GuruViewModel) {
    val p by vm.profile.collectAsStateWithLifecycle()
    val t by vm.targets.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(!p.isComplete) }

    GlassCard(Modifier.fillMaxWidth(), highlight = true) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (p.name.isNotBlank()) "Hi ${p.name}" else "Your profile", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (!editing) TextButton({ editing = true }) { Text("Edit") }
            }
            val tg = t
            if (!editing && tg != null) {
                Row {
                    listOf("${tg.kcal}" to "kcal / day", "${tg.protein}g" to "protein", "${tg.carbs}g" to "carbs", "${tg.fat}g" to "fat").forEach { (v, l) ->
                        Column(Modifier.weight(1f)) { Text(v, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(l, style = MaterialTheme.typography.labelSmall, color = Guru.TextDim) }
                    }
                }
                Text("Burn at rest about ${tg.bmr} kcal; with your activity about ${tg.tdee} kcal." +
                    (if (p.showBmi) " BMI %.1f (%s). BMI doesn't account for muscle, so treat it as a rough guide.".format(tg.bmi, ProfileMath.bmiCategory(tg.bmi)) else "") +
                    (if (tg.raisedToMinimum) " Your target was raised to a safe minimum." else ""),
                    style = MaterialTheme.typography.bodySmall, color = Guru.TextDim)
            } else {
                ProfileForm(p, onSave = { vm.saveProfile(it); editing = false }, onClear = { vm.clearProfile() })
            }
        }
    }
}

@Composable
private fun ProfileForm(p: Profile, onSave: (Profile) -> Unit, onClear: () -> Unit) {
    var name by remember { mutableStateOf(p.name) }
    var weight by remember { mutableStateOf(p.weightLb.toInt().toString()) }
    var ft by remember { mutableStateOf(if (p.heightIn > 0) (p.heightIn / 12).toString() else "") }
    var inch by remember { mutableStateOf(if (p.heightIn > 0) (p.heightIn % 12).toString() else "") }
    var age by remember { mutableStateOf(if (p.age > 0) p.age.toString() else "") }
    var sex by remember { mutableStateOf(p.sex) }
    var act by remember { mutableStateOf(p.activity) }
    var goal by remember { mutableStateOf(p.goal) }
    var bmi by remember { mutableStateOf(p.showBmi) }
    val num = KeyboardOptions(keyboardType = KeyboardType.Number)
    Text("Just for you. Saved only on this phone. No account or sign-in.", style = MaterialTheme.typography.bodySmall, color = Guru.TextDim)
    OutlinedTextField(name, { name = it }, label = { Text("Name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(weight, { weight = it.filter(Char::isDigit).take(3) }, label = { Text("Weight lb") }, singleLine = true, keyboardOptions = num, modifier = Modifier.weight(1.2f))
        OutlinedTextField(ft, { ft = it.filter(Char::isDigit).take(1) }, label = { Text("ft") }, singleLine = true, keyboardOptions = num, modifier = Modifier.weight(0.8f))
        OutlinedTextField(inch, { inch = it.filter(Char::isDigit).take(2) }, label = { Text("in") }, singleLine = true, keyboardOptions = num, modifier = Modifier.weight(0.8f))
        OutlinedTextField(age, { age = it.filter(Char::isDigit).take(3) }, label = { Text("Age") }, singleLine = true, keyboardOptions = num, modifier = Modifier.weight(1f))
    }
    Text("Sex (for the calorie formula)", style = MaterialTheme.typography.labelMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("female" to "Female", "male" to "Male", "na" to "Prefer not to say").forEach { (v, l) -> GlassChip(l, sex == v) { sex = v } }
    }
    Text("Goal", style = MaterialTheme.typography.labelMedium)
    ChipRow(ProfileMath.goals.map { it.second }, ProfileMath.goals.first { it.first == goal }.second,
        { sel -> ProfileMath.goals.firstOrNull { it.second == sel }?.let { goal = it.first } })
    Text("Activity level", style = MaterialTheme.typography.labelMedium)
    ChipRow(ProfileMath.activities.map { it.second }, ProfileMath.activities.firstOrNull { it.first == act }?.second,
        { sel -> ProfileMath.activities.firstOrNull { it.second == sel }?.let { act = it.first } })
    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(bmi, { bmi = it }); Text("Show my BMI") }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        GradientButton("Save profile", Icons.Default.Check) {
            onSave(Profile(name.trim(), (weight.toDoubleOrNull() ?: 170.0).coerceIn(60.0, 700.0),
                (ft.toIntOrNull() ?: 0) * 12 + (inch.toIntOrNull() ?: 0), (age.toIntOrNull() ?: 0).coerceIn(0, 110), sex, act, goal, bmi))
        }
        if (p.isComplete) TextButton(onClear) { Text("Clear") }
    }
    Text("Targets use the Mifflin-St Jeor formula and are estimates, not medical advice. Check with your doctor before big diet changes.",
        style = MaterialTheme.typography.bodySmall, color = Guru.TextDim)
}
