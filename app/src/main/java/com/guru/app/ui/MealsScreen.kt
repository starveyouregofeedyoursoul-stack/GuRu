package com.guru.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guru.app.data.Categories
import com.guru.app.data.Meal
import com.guru.app.data.ProfileMath
import com.guru.app.data.Targets

private fun prepArt(prep: String): Pair<Brush, ImageVector> = when (prep) {
    "Sheet Pan" -> Brush.linearGradient(listOf(Color(0xFFFF9966), Color(0xFFFF5E62))) to Icons.Default.Kitchen
    "One Pot" -> Brush.linearGradient(listOf(Color(0xFFC94B4B), Color(0xFF4B134F))) to Icons.Default.SoupKitchen
    "Air Fryer" -> Brush.linearGradient(listOf(Color(0xFFF2994A), Color(0xFFF2C94C))) to Icons.Default.Air
    "Slow Cooker" -> Brush.linearGradient(listOf(Color(0xFF834D9B), Color(0xFFD04ED6))) to Icons.Default.Timer
    "No-Cook" -> Brush.linearGradient(listOf(Color(0xFF56AB2F), Color(0xFFA8E063))) to Icons.Default.Eco
    "Batch Prep" -> Brush.linearGradient(listOf(Color(0xFF2193B0), Color(0xFF6DD5ED))) to Icons.Default.Inventory2
    "Grill" -> Brush.linearGradient(listOf(Color(0xFFCB2D3E), Color(0xFFEF473A))) to Icons.Default.OutdoorGrill
    "Blender" -> Brush.linearGradient(listOf(Color(0xFFDA22FF), Color(0xFF9733EE))) to Icons.Default.LocalDrink
    else -> Guru.accent to Icons.Default.Restaurant
}

@Composable
fun MealsScreen(vm: GuruViewModel) {
    val meals by vm.meals.collectAsStateWithLifecycle()
    val day by vm.selectedDay.collectAsStateWithLifecycle()
    val targets by vm.targets.collectAsStateWithLifecycle()
    var fitOnly by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    var maxPrep by remember { mutableIntStateOf(60) }
    var tag by remember { mutableStateOf<String?>(null) }
    var prep by remember { mutableStateOf<String?>(null) }
    var open by remember { mutableStateOf<Meal?>(null) }
    var editing by remember { mutableStateOf<Meal?>(null) }
    var shopping by remember { mutableStateOf<List<String>?>(null) }

    val tags = remember(meals) { meals.flatMap { it.tags.split(",") }.map { it.trim() }.filter { it.isNotEmpty() }.distinct().sorted() }
    val filtered = meals.filter { it.prepMinutes <= maxPrep && (tag == null || it.tags.contains(tag!!)) && (prep == null || it.prepType == prep) &&
        (!fitOnly || ProfileMath.fits(it, targets)) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 96.dp)) {
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassCard(Modifier.fillMaxWidth(), highlight = true) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Plan my week", style = MaterialTheme.typography.titleLarge)
                            targets?.let { t -> Text("Your targets: ${t.kcal} kcal a day, ${t.protein}g protein. Dinner around ${t.dinner} kcal.", color = Guru.TextDim) }
                            Text("Fills 7 dinners starting ${day.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${day.dayOfMonth} using your filters below.",
                                color = Guru.TextDim, style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GradientButton("Auto-plan", Icons.Default.AutoAwesome) { vm.autoPlan(maxPrep, tag, prep.takeIf { it != "Blender" }) }
                                OutlinedButton({ shopping = vm.shoppingList() }) { Icon(Icons.Default.ShoppingCart, null); Spacer(Modifier.width(6.dp)); Text("Shopping list") }
                            }
                        }
                    }
                    if (targets != null) GlassChip("Only meals that fit my target", fitOnly) { fitOnly = !fitOnly }
                    Text("Max prep time", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 45, 60).forEach { m -> GlassChip(if (m == 60) "Any" else "≤$m min", maxPrep == m) { maxPrep = m } }
                    }
                    Text("Prep style", style = MaterialTheme.typography.labelLarge)
                    ChipRow(Categories.prepTypes, prep, { prep = it }, allowNone = true)
                    Text("Goal / diet", style = MaterialTheme.typography.labelLarge)
                    ChipRow(tags, tag, { tag = it }, allowNone = true)
                }
            }
            listOf("Dinner" to "🍲 Healthy dinners", "Smoothie" to "🥤 Smoothies & shakes").forEach { (kind, title) ->
                val list = filtered.filter { it.kind == kind }
                item(key = kind) {
                    Box(Modifier.padding(horizontal = 16.dp)) { SectionHeader(title, if (list.isEmpty()) "Nothing matches – loosen your filters" else null) }
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(list, key = { it.id }) { m -> MealPoster(m, ProfileMath.fits(m, targets)) { open = m } }
                    }
                }
            }
            item {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader("Find more easy meals", "Search the web using your current filters, then save favourites with +")
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton({
                        val q = listOfNotNull("easy healthy", prep?.lowercase(), tag, "dinner recipe",
                            if (maxPrep < 60) "under $maxPrep minutes" else null).joinToString(" ")
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(q))))
                    }) { Icon(Icons.Default.TravelExplore, null); Spacer(Modifier.width(6.dp)); Text("Search recipes") }
                }
            }
        }
        GradientButton("Add recipe", Icons.Default.Add, Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            editing = Meal(name = "", kind = "Dinner", prepType = "One Pot", prepMinutes = 30, tags = "", ingredients = "", steps = "")
        }
    }

    open?.let { m -> MealDetail(m, targets, onClose = { open = null }, onPlan = { vm.planMeal(m, day); open = null },
        onEdit = { editing = m; open = null }) }
    editing?.let { m -> MealEditor(m, { editing = null }, { vm.saveMeal(it); editing = null }, { vm.deleteMeal(m); editing = null }) }
    shopping?.let { list ->
        AlertDialog(onDismissRequest = { shopping = null },
            title = { Text("Shopping list · 7 days") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (list.isEmpty()) Text("Plan some meals first.")
                    list.forEach { var got by remember { mutableStateOf(false) }
                        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(got, { got = it }); Text(it) } }
                }
            },
            confirmButton = {
                TextButton({
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, "GuRu shopping list\n" + list.joinToString("\n") { "☐ $it" }), "Share list"))
                }) { Text("Share") }
            },
            dismissButton = { TextButton({ shopping = null }) { Text("Close") } })
    }
}

@Composable
private fun MealPoster(m: Meal, fits: Boolean, onClick: () -> Unit) {
    val (bg, icon) = prepArt(m.prepType)
    GlassCard(Modifier.width(180.dp), onClick = onClick) {
        Box(Modifier.fillMaxWidth().height(100.dp).background(bg).padding(10.dp)) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(36.dp).align(Alignment.Center))
            Text("${m.prepMinutes} min", color = Color.White, style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.BottomEnd))
        }
        Column(Modifier.padding(10.dp)) {
            Text(m.name, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Text(if (m.kcal > 0) "${m.kcal} kcal · ${m.protein}g protein" else m.prepType,
                style = MaterialTheme.typography.bodySmall, color = Guru.TextDim)
            if (fits) Text("Fits your target", style = MaterialTheme.typography.labelSmall, color = Guru.Mint)
        }
    }
}

@Composable
private fun MealDetail(m: Meal, targets: Targets?, onClose: () -> Unit, onPlan: () -> Unit, onEdit: () -> Unit) {
    val (bg, icon) = prepArt(m.prepType)
    Dialog(onClose, DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Guru.background)) {
            LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                item {
                    Box(Modifier.fillMaxWidth().height(220.dp).background(bg)) {
                        Icon(icon, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(180.dp).align(Alignment.CenterEnd))
                        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Guru.Night))))
                        IconButton(onClose, Modifier.statusBarsPadding().padding(8.dp)) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                            Text("${m.prepType.uppercase()} · ${m.prepMinutes} MIN", style = MaterialTheme.typography.labelMedium, color = Guru.TextDim)
                            Text(m.name, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                    Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GradientButton("Plan for selected day", Icons.Default.EventAvailable, onClick = onPlan)
                        FilledTonalIconButton(onEdit) { Icon(Icons.Default.Edit, "Edit") }
                    }
                    if (m.tags.isNotBlank()) Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        m.tags.split(",").forEach { GlassChip(it.trim(), false) {} }
                    }
                }
                item {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MacroCard(m)
                        if (targets != null && m.kcal > 0) Text(
                            "One serving is ${m.kcal * 100 / targets.kcal}% of your ${targets.kcal} kcal day and gives " +
                                "${m.protein * 100 / targets.protein.coerceAtLeast(1)}% of your protein target.", color = Guru.TextDim)
                        GlassCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                                m.ingredients.lines().filter { it.isNotBlank() }.forEach { Text("•  $it", Modifier.padding(top = 4.dp)) }
                            }
                        }
                        GlassCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("How to prep", style = MaterialTheme.typography.titleMedium)
                                m.steps.lines().filter { it.isNotBlank() }.forEachIndexed { i, s ->
                                    Row(Modifier.padding(top = 8.dp)) {
                                        Text("${i + 1}", color = Guru.Aurora, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.width(28.dp))
                                        Text(s)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Per-serving calories and macros with a split bar showing where the calories come from. */
@Composable
fun MacroCard(m: Meal) {
    if (m.kcal == 0) { Text("No macros set. Edit the recipe to add them.", color = Guru.TextDim); return }
    val pc = m.protein * 4f; val cc = m.carbs * 4f; val fc = m.fat * 9f; val tot = (pc + cc + fc).coerceAtLeast(1f)
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row { Text("Per serving", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); Text("Serves ${m.servings} · estimates", color = Guru.TextDim) }
            Row {
                listOf(Triple("${m.kcal}", "kcal", Color.White), Triple("${m.protein}g", "protein", Guru.Aurora),
                    Triple("${m.carbs}g", "carbs", Color(0xFFFFC876)), Triple("${m.fat}g", "fat", Guru.Ember)).forEach { (v, l, c) ->
                    Column(Modifier.weight(1f)) { Text(v, color = c, fontWeight = FontWeight.Bold, fontSize = 22.sp); Text(l, color = Guru.TextDim, fontSize = 13.sp) }
                }
            }
            Row(Modifier.fillMaxWidth().height(8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (pc > 0) Box(Modifier.weight(pc / tot).fillMaxHeight().background(Guru.Aurora))
                if (cc > 0) Box(Modifier.weight(cc / tot).fillMaxHeight().background(Color(0xFFFFC876)))
                if (fc > 0) Box(Modifier.weight(fc / tot).fillMaxHeight().background(Guru.Ember))
            }
            Text("${(pc / tot * 100).toInt()}% protein · ${(cc / tot * 100).toInt()}% carbs · ${(fc / tot * 100).toInt()}% fat",
                color = Guru.TextDim, fontSize = 13.sp)
        }
    }
}

@Composable
private fun MealEditor(m: Meal, onDismiss: () -> Unit, onSave: (Meal) -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(m.name) }
    var kind by remember { mutableStateOf(m.kind) }
    var prep by remember { mutableStateOf(m.prepType) }
    var mins by remember { mutableIntStateOf(m.prepMinutes) }
    var tags by remember { mutableStateOf(m.tags) }
    var ing by remember { mutableStateOf(m.ingredients) }
    var steps by remember { mutableStateOf(m.steps) }
    var serv by remember { mutableIntStateOf(m.servings) }
    var kcal by remember { mutableIntStateOf(m.kcal) }
    var pro by remember { mutableIntStateOf(m.protein) }
    var carb by remember { mutableIntStateOf(m.carbs) }
    var fat by remember { mutableIntStateOf(m.fat) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (m.id == 0L) "New recipe" else "Edit recipe") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                ChipRow(listOf("Dinner", "Smoothie"), kind, { kind = it ?: kind })
                ChipRow(Categories.prepTypes, prep, { prep = it ?: prep })
                NumberField("Prep minutes", mins, { mins = it })
                Text("Macros per serving", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumberField("Serves", serv, { serv = it }, Modifier.weight(1f)); NumberField("kcal", kcal, { kcal = it }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumberField("Protein g", pro, { pro = it }, Modifier.weight(1f)); NumberField("Carbs g", carb, { carb = it }, Modifier.weight(1f))
                    NumberField("Fat g", fat, { fat = it }, Modifier.weight(1f))
                }
                OutlinedTextField(tags, { tags = it }, label = { Text("Tags (comma separated)") }, singleLine = true)
                OutlinedTextField(ing, { ing = it }, label = { Text("Ingredients (one per line)") }, minLines = 3)
                OutlinedTextField(steps, { steps = it }, label = { Text("Steps (one per line)") }, minLines = 3)
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = {
                onSave(m.copy(name = name.trim(), kind = kind, prepType = prep, prepMinutes = mins, tags = tags.trim(),
                    ingredients = ing.trim(), steps = steps.trim(),
                    servings = serv.coerceAtLeast(1), kcal = kcal, protein = pro, carbs = carb, fat = fat))
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (m.id != 0L) TextButton(onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                TextButton(onDismiss) { Text("Cancel") }
            }
        },
    )
}
