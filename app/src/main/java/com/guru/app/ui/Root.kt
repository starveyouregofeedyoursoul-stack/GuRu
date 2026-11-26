package com.guru.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ---------- Cinematic "TV" palette ----------
object Guru {
    val Night = Color(0xFF07070F)
    val Deep = Color(0xFF10102A)
    val Glass = Color(0x1FFFFFFF)
    val GlassBorder = Color(0x33FFFFFF)
    val Aurora = Color(0xFF7C9CFF)
    val Cosmic = Color(0xFFB47CFF)
    val Ember = Color(0xFFFF8A5C)
    val Mint = Color(0xFF5CE1C6)
    val TextDim = Color(0xB3FFFFFF)
    val accent = Brush.linearGradient(listOf(Aurora, Cosmic))
    val background = Brush.verticalGradient(listOf(Deep, Night, Night))
}

private val TvScheme = darkColorScheme(
    primary = Guru.Aurora, onPrimary = Color.Black,
    secondary = Guru.Ember, tertiary = Guru.Mint,
    primaryContainer = Color(0xFF26265A), onPrimaryContainer = Color.White,
    secondaryContainer = Color(0xFF3A2430),
    background = Guru.Night, surface = Color(0xFF14142A), onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E3A), onSurfaceVariant = Guru.TextDim,
    surfaceContainerHigh = Color(0xFF1A1A36), outline = Color(0x66FFFFFF),
)

private val TvType = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun GuruTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = TvScheme, typography = TvType, shapes = Shapes(
        small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp),
    ), content = content)

enum class Tab(val label: String, val icon: ImageVector) {
    Plan("Plan", Icons.Default.CalendarMonth),
    Workouts("Train", Icons.Default.FitnessCenter),
    Meals("Eat", Icons.Default.Restaurant),
    Sleep("Sleep", Icons.Default.Bedtime),
    Alerts("Alerts", Icons.Default.NotificationsActive),
    Health("Health", Icons.Default.Watch),
}

@Composable
fun GuruRoot(vm: GuruViewModel = viewModel()) {
    var tab by rememberSaveable { mutableStateOf(Tab.Plan) }
    val snackbar = remember { SnackbarHostState() }
    val msg by vm.message.collectAsStateWithLifecycle()
    LaunchedEffect(msg) { msg?.let { snackbar.showSnackbar(it); vm.message.value = null } }

    Box(Modifier.fillMaxSize().background(Guru.background)) {
        StarField(Modifier.matchParentSize())
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                Row(Modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    GuruLogo(Modifier.size(44.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("GuRu", style = MaterialTheme.typography.headlineMedium)
                        AnimatedContent(tab, label = "subtitle",
                            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) }) {
                            Text(it.label, style = MaterialTheme.typography.labelLarge, color = Guru.TextDim)
                        }
                    }
                }
            },
            bottomBar = { GlassNavBar(tab) { tab = it } },
        ) { pad ->
            AnimatedContent(
                targetState = tab, label = "tabs", modifier = Modifier.padding(pad).fillMaxSize(),
                transitionSpec = {
                    val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it / 5 * dir } +
                        fadeIn(tween(250))) togetherWith
                        (slideOutHorizontally(tween(220)) { -it / 5 * dir } + fadeOut(tween(180)))
                },
            ) { t ->
                when (t) {
                    Tab.Plan -> CalendarScreen(vm)
                    Tab.Workouts -> WorkoutsScreen(vm)
                    Tab.Meals -> MealsScreen(vm)
                    Tab.Sleep -> SleepScreen(vm)
                    Tab.Alerts -> RemindersScreen(vm)
                    Tab.Health -> HealthScreen(vm)
                }
            }
        }
    }
}

/** Floating frosted pill nav bar with a glow under the active tab. */
@Composable
private fun GlassNavBar(current: Tab, onSelect: (Tab) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(
        Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth()
            .clip(RoundedCornerShape(32.dp)).background(Color(0xCC14142A))
            .border(1.dp, Guru.GlassBorder, RoundedCornerShape(32.dp)).padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Tab.entries.forEach { t ->
            val selected = t == current
            val scale by animateFloatAsState(if (selected) 1.12f else 1f, spring(0.5f, Spring.StiffnessMedium), label = "s")
            val bg by animateColorAsState(if (selected) Color(0x337C9CFF) else Color.Transparent, label = "bg")
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(bg)
                    .clickable { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onSelect(t) }
                    .padding(vertical = 8.dp).graphicsLayer { scaleX = scale; scaleY = scale },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(t.icon, t.label, tint = if (selected) Color.White else Guru.TextDim)
                Text(t.label, style = MaterialTheme.typography.labelSmall, color = if (selected) Color.White else Guru.TextDim)
            }
        }
    }
}

// ---------- shared bits ----------

/** Frosted glass card that sinks slightly when pressed and springs back on release. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    highlight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(0.6f, Spring.StiffnessMedium), label = "press")
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier.graphicsLayer { scaleX = scale; scaleY = scale }.clip(shape)
            .background(if (highlight) Brush.linearGradient(listOf(Color(0x557C9CFF), Color(0x40B47CFF)))
                else Brush.linearGradient(listOf(Color(0x24FFFFFF), Color(0x10FFFFFF))))
            .border(1.dp, Guru.GlassBorder, shape)
            .then(if (onClick != null) Modifier.clickable(src, indication = null, onClick = onClick) else Modifier),
        content = content,
    )
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(Modifier.padding(top = 12.dp, bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Guru.TextDim) }
    }
}

@Composable
fun GradientButton(text: String, icon: ImageVector? = null, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, spring(0.5f), label = "b")
    Row(
        modifier.graphicsLayer { scaleX = scale; scaleY = scale }.clip(CircleShape).background(Guru.accent)
            .clickable(src, null, onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
    ) {
        icon?.let { Icon(it, null, tint = Color.White); Spacer(Modifier.width(8.dp)) }
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ChipRow(options: List<String>, selected: String?, onSelect: (String?) -> Unit, allowNone: Boolean = false) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (allowNone) item { GlassChip("All", selected == null) { onSelect(null) } }
        items(options) { o -> GlassChip(o, selected == o) { onSelect(if (allowNone && selected == o) null else o) } }
    }
}

@Composable
fun GlassChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Guru.Aurora else Guru.Glass, label = "chip")
    Text(label, color = if (selected) Color.Black else Color.White, style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.clip(CircleShape).background(bg).border(1.dp, Guru.GlassBorder, CircleShape)
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp))
}

@Composable
fun NumberField(label: String, value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text, onValueChange = { text = it.filter(Char::isDigit).take(4); text.toIntOrNull()?.let(onChange) },
        label = { Text(label) }, singleLine = true, modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeButton(hour: Int, minute: Int, onPick: (Int, Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }) { Icon(Icons.Default.Schedule, null); Spacer(Modifier.width(6.dp)); Text(fmtTime(hour, minute)) }
    if (open) {
        val state = rememberTimePickerState(hour, minute, false)
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = { TextButton({ onPick(state.hour, state.minute); open = false }) { Text("OK") } },
            dismissButton = { TextButton({ open = false }) { Text("Cancel") } },
            text = { TimePicker(state) },
        )
    }
}

fun fmtTime(h: Int, m: Int): String {
    val hh = if (h % 12 == 0) 12 else h % 12
    return "%d:%02d %s".format(hh, m, if (h < 12) "AM" else "PM")
}

val weekdayLetters = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun DaysPicker(mask: Int, onChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        weekdayLetters.forEachIndexed { i, l ->
            val on = mask and (1 shl i) != 0
            val bg by animateColorAsState(if (on) Guru.Aurora else Guru.Glass, label = "d")
            Box(Modifier.size(34.dp).clip(CircleShape).background(bg).clickable { onChange(mask xor (1 shl i)) },
                contentAlignment = Alignment.Center) { Text(l, color = if (on) Color.Black else Color.White) }
        }
    }
}
