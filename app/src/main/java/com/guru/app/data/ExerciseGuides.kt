package com.guru.app.data

import android.content.Intent
import android.net.Uri

/** How-to coaching for each move: what it works, step-by-step form, a key cue and the mistake to avoid. */
data class ExerciseGuide(
    val name: String,
    val muscles: String,
    val steps: List<String>,
    val cue: String,
    val avoid: String,
    val keywords: List<String>,
)

object ExerciseGuides {

    val all = listOf(
        g("Goblet Squat", "Quads, glutes, core", listOf(
            "Hold a dumbbell or kettlebell vertically at your chest, elbows tucked.",
            "Feet shoulder-width, toes turned out slightly.",
            "Sit your hips down and back between your heels, chest tall.",
            "Go as deep as you can with a flat back, then drive up through your whole foot."),
            "Push your knees out in line with your toes.", "Heels lifting or knees caving inward.", "goblet"),
        g("Squat", "Quads, glutes, hamstrings, core", listOf(
            "Stand with feet shoulder-width, toes slightly out.",
            "Brace your core like you're about to be poked in the stomach.",
            "Bend hips and knees together, sitting down between your heels.",
            "Reach at least parallel, then stand up by pushing the floor away."),
            "Keep your chest proud and weight mid-foot.", "Rounding your lower back at the bottom.", "squat", "air squat"),
        g("Jump Squat", "Quads, glutes, calves (power)", listOf(
            "Drop into a quarter-to-half squat, arms back.",
            "Explode up, swinging arms forward, and leave the floor.",
            "Land softly on the balls of your feet, then heels.",
            "Sink straight into the next rep."),
            "Land quiet – like a cat.", "Stiff-legged landings that jar the knees.", "jump squat"),
        g("Deep Squat Hold", "Hips, ankles, lower back (mobility)", listOf(
            "Feet a little wider than shoulders, toes out.",
            "Sink as low as you can, elbows pressing knees outward.",
            "Keep heels down – hold onto a post if you need balance.",
            "Breathe slowly and relax into the position."),
            "Long exhales let the hips open further.", "Forcing depth with heels lifting.", "squat hold"),
        g("Wall Sit", "Quads, glutes", listOf(
            "Back flat against a wall, slide down until thighs are parallel to the floor.",
            "Knees stacked over ankles, feet hip-width.",
            "Arms crossed or by your sides – don't push on your legs.",
            "Hold, breathing steadily."),
            "Press your low back into the wall.", "Knees drifting past toes.", "wall sit"),
        g("Romanian Deadlift", "Hamstrings, glutes, lower back", listOf(
            "Stand tall holding weights in front of your thighs, soft knees.",
            "Push your hips back like closing a car door with your butt.",
            "Slide the weights down your legs until you feel a strong hamstring stretch.",
            "Squeeze glutes to drive hips forward and stand tall."),
            "Weights stay close – shave your legs with them.", "Rounding your back or turning it into a squat.", "romanian", "rdl", "deadlift"),
        g("Hip Thrust", "Glutes, hamstrings", listOf(
            "Sit with upper back against a bench, weight across your hips.",
            "Feet flat, about hip-width, knees bent ~90° at the top.",
            "Tuck chin, drive through heels and lift hips until body is a straight line.",
            "Squeeze glutes hard for one second, lower with control."),
            "Ribs down, slight posterior pelvic tilt at the top.", "Arching the lower back instead of using glutes.", "hip thrust"),
        g("Glute Bridge", "Glutes, hamstrings, core", listOf(
            "Lie on your back, knees bent, feet flat (or on a wall for wall bridges).",
            "Press your low back gently into the floor.",
            "Drive through heels and lift hips until knees, hips and shoulders line up.",
            "Pause and squeeze, then lower one vertebra at a time."),
            "Think 'squeeze a coin between your glutes'.", "Pushing up through your toes.", "bridge"),
        g("Walking Lunge", "Quads, glutes, balance", listOf(
            "Stand tall and take a long step forward.",
            "Lower until both knees are about 90°, back knee just above the floor.",
            "Push through the front heel and bring the back foot forward into the next step.",
            "Alternate legs, keeping your torso upright."),
            "Front knee tracks over the middle toes.", "Short steps that overload the front knee.", "lunge"),
        g("Calf Raise", "Calves", listOf(
            "Stand on the edge of a step, heels hanging off.",
            "Rise as high as possible onto the balls of your feet.",
            "Pause at the top, then lower slowly below step level.",
            "Hold a wall for balance if needed."),
            "Slow 3-second lowering builds the most strength.", "Bouncing through reps.", "calf"),
        g("Bench Press", "Chest, shoulders, triceps", listOf(
            "Lie on the bench, eyes under the bar, feet planted.",
            "Squeeze shoulder blades together and down.",
            "Lower the bar to mid-chest with elbows ~45° from your body.",
            "Press up and slightly back over your shoulders."),
            "Keep your shoulder blades pinned the whole set.", "Flaring elbows to 90° or bouncing off the chest.", "bench"),
        g("Push-Up", "Chest, shoulders, triceps, core", listOf(
            "Hands slightly wider than shoulders, body in a straight plank.",
            "Lower your chest toward the floor, elbows ~45° from your sides.",
            "Touch chest just above the floor.",
            "Push the floor away to return. Drop to knees or use a wall to scale."),
            "Squeeze glutes so your hips don't sag.", "Hips sagging or head poking forward.", "push-up", "push up", "pushup"),
        g("Overhead Press", "Shoulders, triceps, upper back", listOf(
            "Stand with weights at shoulder height, palms forward.",
            "Brace core and squeeze glutes.",
            "Press straight up, moving your head back slightly to clear the path.",
            "Finish with biceps by your ears, then lower with control."),
            "Ribs down – don't lean back.", "Arching the lower back to finish the rep.", "overhead press", "shoulder press"),
        g("Row", "Upper back, lats, biceps", listOf(
            "Hinge forward with a flat back (or brace one hand on a bench for dumbbell rows).",
            "Let the weight hang straight down.",
            "Pull the elbow back toward your hip, squeezing your shoulder blade.",
            "Lower slowly to a full stretch."),
            "Lead with the elbow, not the hand.", "Jerking the weight with your torso.", "row"),
        g("Band Pull-Apart", "Rear delts, upper back (posture)", listOf(
            "Hold a band at shoulder height, arms straight, hands shoulder-width.",
            "Pull the band apart until it touches your chest.",
            "Squeeze shoulder blades together for a beat.",
            "Return slowly."),
            "Keep shoulders down away from ears.", "Shrugging up.", "pull-apart", "pull apart"),
        g("Bicep Curl", "Biceps, forearms", listOf(
            "Stand tall, weights at your sides, palms forward.",
            "Pin elbows to your ribs.",
            "Curl weights up toward your shoulders.",
            "Lower slowly over 2-3 seconds."),
            "Only your forearms should move.", "Swinging your body for momentum.", "curl"),
        g("Tricep Dip", "Triceps, chest, shoulders", listOf(
            "Hands on a bench or chair edge behind you, fingers forward.",
            "Legs bent (easier) or straight (harder).",
            "Bend elbows straight back to ~90°.",
            "Press back up to straight arms."),
            "Keep your back close to the bench.", "Dropping too deep and straining the shoulders.", "dip"),
        g("Plank", "Core, shoulders, glutes", listOf(
            "Forearms on the floor, elbows under shoulders.",
            "Step feet back so your body forms a straight line.",
            "Squeeze glutes and pull belly button toward spine.",
            "Breathe steadily and hold."),
            "Imagine pulling your elbows toward your toes.", "Hips too high or sagging.", "plank"),
        g("Side Plank", "Obliques, hips, shoulders", listOf(
            "Lie on your side, elbow under shoulder.",
            "Stack feet (or stagger for balance).",
            "Lift hips so your body is a straight line from head to feet.",
            "Hold, then switch sides."),
            "Push the floor away with your forearm.", "Letting the hips drop.", "side plank"),
        g("Dead Bug", "Deep core", listOf(
            "Lie on your back, arms up, knees bent 90° over hips.",
            "Press your low back flat into the floor.",
            "Slowly extend opposite arm and leg toward the floor.",
            "Return and switch sides."),
            "Exhale fully as you extend.", "Low back arching off the floor.", "dead bug"),
        g("Bicycle Crunch", "Abs, obliques", listOf(
            "Lie on your back, hands lightly behind head, legs lifted.",
            "Bring one knee in while rotating the opposite elbow toward it.",
            "Extend the other leg long.",
            "Alternate in a slow, controlled pedaling motion."),
            "Rotate from your ribs, not your neck.", "Yanking on your head.", "bicycle"),
        g("Russian Twist", "Obliques, core", listOf(
            "Sit with knees bent, lean back ~45°, chest tall.",
            "Hold a weight or clasp hands.",
            "Rotate your torso to tap beside one hip, then the other.",
            "Lift feet for more challenge."),
            "Move your shoulders, not just your arms.", "Rounding the back.", "russian twist"),
        g("Leg Raise", "Lower abs, hip flexors", listOf(
            "Lie flat, hands under your hips for support.",
            "Keep legs straight and together.",
            "Raise legs to vertical.",
            "Lower slowly, stopping before your back arches."),
            "Low back stays glued to the floor.", "Dropping legs fast.", "leg raise"),
        g("Burpee", "Full body, conditioning", listOf(
            "From standing, squat and place hands on the floor.",
            "Jump or step feet back into a plank.",
            "Chest to floor (optional), then jump feet back to hands.",
            "Explode up with a small jump and clap overhead."),
            "Find a rhythm you can keep – smooth beats fast.", "Sagging hips in the plank.", "burpee"),
        g("Mountain Climber", "Core, shoulders, cardio", listOf(
            "Start in a high plank, hands under shoulders.",
            "Drive one knee toward your chest.",
            "Quickly switch legs like running in place.",
            "Keep hips level with shoulders."),
            "Shoulders stay over wrists.", "Bouncing hips up high.", "mountain climber"),
        g("High Knees", "Cardio, hip flexors, calves", listOf(
            "Stand tall, run in place.",
            "Drive knees up to hip height.",
            "Pump arms in rhythm.",
            "Stay light on the balls of your feet."),
            "Quick feet, tall posture.", "Leaning back.", "high knees"),
        g("Kettlebell Swing", "Glutes, hamstrings, core, cardio", listOf(
            "Feet wider than hips, kettlebell a foot in front of you.",
            "Hinge and hike the bell back between your legs.",
            "Snap your hips forward to float the bell to chest height.",
            "Let it fall back, hinge again and repeat."),
            "It's a hip snap, not an arm lift.", "Squatting the swing or lifting with your arms.", "kettlebell swing", "swing"),
        g("Wall Ball", "Legs, shoulders, conditioning", listOf(
            "Hold a medicine ball at chest, stand an arm's length from a wall.",
            "Squat to parallel.",
            "Drive up and throw the ball to a target ~10 ft high.",
            "Catch it and flow into the next squat."),
            "Use your legs to throw, arms just guide.", "Catching it standing tall and then squatting.", "wall ball"),
        g("Farmer Carry", "Grip, traps, core", listOf(
            "Pick up heavy weights in each hand with a flat back.",
            "Stand tall, shoulders back and down.",
            "Walk with short, controlled steps.",
            "Set the weights down with a hip hinge."),
            "Walk like you're balancing a book on your head.", "Leaning to one side.", "farmer"),
        g("Sled Push", "Legs, conditioning", listOf(
            "Grip the sled handles with arms extended or bent.",
            "Lean in at ~45°.",
            "Drive with short powerful steps, pushing through the balls of your feet.",
            "No sled? Push a loaded plate on turf or do a heavy plank-walk."),
            "Keep hips low and back flat.", "Standing too upright.", "sled"),
        g("Burpee Broad Jump", "Full body power", listOf(
            "Do a burpee: chest to floor.",
            "Jump feet in.",
            "Swing arms and jump forward as far as you can.",
            "Land softly and repeat."),
            "Land in a quarter squat to absorb force.", "Locking knees on landing.", "broad jump"),
        g("Cat-Cow", "Spine mobility", listOf(
            "Start on hands and knees.",
            "Inhale: drop belly, lift chest and tailbone (cow).",
            "Exhale: round your back, tuck chin and tailbone (cat).",
            "Move slowly with your breath."),
            "Move one vertebra at a time.", "Rushing through it.", "cat-cow", "cat cow"),
        g("World's Greatest Stretch", "Hips, hamstrings, T-spine", listOf(
            "Step into a deep lunge, both hands inside the front foot.",
            "Drop the inside elbow toward the ankle.",
            "Rotate and reach that arm to the ceiling.",
            "Return, straighten front leg for a hamstring stretch, switch sides."),
            "Follow your top hand with your eyes.", "Letting the back knee collapse.", "greatest stretch"),
        g("90/90 Hip Switch", "Hip rotation", listOf(
            "Sit with both knees bent 90°, one in front, one to the side.",
            "Sit tall, hands behind you if needed.",
            "Rotate both knees to the other side without using your hands.",
            "Keep alternating slowly."),
            "Lead with the knees, keep the chest tall.", "Collapsing the spine.", "90/90"),
        g("Thoracic Rotation", "Upper back mobility", listOf(
            "Lie on your side, knees bent 90° and stacked, arms straight in front.",
            "Open the top arm across your body like a book.",
            "Follow your hand with your eyes until it rests on the other side.",
            "Return slowly."),
            "Keep knees together and down.", "Rotating from the lower back.", "thoracic"),
        g("Leg Circles", "Hips, core control", listOf(
            "Lie on your back, one leg extended to the ceiling.",
            "Draw small controlled circles with the lifted leg.",
            "Keep the pelvis still.",
            "Reverse direction, then switch legs."),
            "Small and controlled beats big and wobbly.", "Hips rocking side to side.", "leg circle"),
        g("Incline Treadmill Walk", "Glutes, calves, cardio", listOf(
            "Set incline (e.g. 12%) and speed (e.g. 3.0 mph).",
            "Walk tall without holding the rails.",
            "Short, quick steps; push through your whole foot.",
            "Lower the incline if you need the handrails."),
            "Hands-free makes it work.", "Hanging on the rails.", "treadmill", "incline"),
        g("Easy Run / Jog", "Heart, legs", listOf(
            "Start with a 5-minute brisk walk.",
            "Jog at a pace where you can still talk in short sentences.",
            "Land under your hips with quick light steps.",
            "Relax shoulders and hands."),
            "Slower than you think – conversational pace.", "Overstriding and heel-slamming.", "jog", "run", "s jog", "min jog", "jog /"),
        g("Zone 2 Cardio", "Heart health, endurance", listOf(
            "Choose a bike, rower, incline walk or easy jog.",
            "Keep heart rate around 60-70% of max (roughly 180 minus your age).",
            "You should be able to talk but not sing.",
            "Hold the effort steady for the full session."),
            "Use your watch's heart-rate readout to stay in zone.", "Drifting into hard effort.", "zone 2", "bike", "walk"),
        g("Stair Climber", "Glutes, quads, calves, cardio", listOf(
            "Step on, set a steady level.",
            "Stand tall, light fingertips on rails only for balance.",
            "Push through your whole foot on each step.",
            "Raise the speed for fast intervals, then ease back."),
            "Drive through the heel to work your glutes.", "Leaning on the rails with straight arms.", "stair"),
        g("Interval Walking", "Heart, legs", listOf(
            "Walk fast enough that talking is hard (about 70% effort) for 3 minutes.",
            "Slow to an easy stroll for 3 minutes.",
            "Swing your arms and keep your posture tall.",
            "Repeat 5 rounds."),
            "Fast walks should feel like a 'somewhat hard' effort.", "Making every interval the same easy pace.", "fast walk", "slow walk"),
        g("Shadowboxing", "Shoulders, core, cardio", listOf(
            "Stand in a boxing stance: feet staggered, hands guarding your chin.",
            "Jab: snap the lead hand straight out and back.",
            "Cross: rotate your back hip and throw the rear hand.",
            "Hook: bend the elbow 90° and swing from your hips."),
            "Exhale sharply on every punch.", "Locking your elbows at full extension.", "jab", "hook", "shadowbox", "punch"),
        g("Rowing Machine", "Legs, back, arms, cardio", listOf(
            "Strap in, grab the handle, shins vertical.",
            "Drive with your legs first, then lean back slightly.",
            "Finish by pulling the handle to your lower ribs.",
            "Return: arms, then body, then bend the knees."),
            "Legs, body, arms. Then arms, body, legs.", "Pulling with your arms before your legs push.", "rowing machine"),
        g("Indoor Cycling", "Quads, glutes, cardio", listOf(
            "Set the seat at hip height so your knee is slightly bent at the bottom.",
            "Warm up spinning at light resistance.",
            "Climbs: add resistance and slow your cadence.",
            "Sprints: lighter resistance, fast legs, stay seated."),
            "Keep your upper body relaxed and still.", "Bouncing in the saddle at high speed.", "spin bike", "seated climb", "sprint"),
        g("Freestyle Swim", "Full body, cardio", listOf(
            "Push off on your front, body long and flat.",
            "Alternate arms: reach, pull under your body, push past the hip.",
            "Turn your head to the side to breathe every 2-3 strokes.",
            "Keep a steady flutter kick from the hips."),
            "Keep one eye in the water when you breathe.", "Lifting your whole head forward to breathe.", "swim", "freestyle", "kickboard"),
        g("Sun Salutation A", "Full body flow", listOf(
            "Inhale, reach your arms overhead.",
            "Exhale, fold forward. Inhale, half lift.",
            "Step back to plank, lower down, then upward dog.",
            "Exhale to downward dog for 5 breaths, step forward, rise up."),
            "One breath per movement.", "Rushing and holding your breath.", "sun salutation"),
        g("Downward Dog", "Hamstrings, calves, shoulders", listOf(
            "From hands and knees, tuck toes and lift hips up and back.",
            "Hands shoulder-width, fingers spread.",
            "Press the floor away and lengthen your spine.",
            "Bend the knees as much as you need."),
            "A long back matters more than straight legs.", "Rounding the back to force the heels down.", "downward dog"),
        g("Warrior II", "Legs, hips, shoulders", listOf(
            "Step feet wide, front toes forward, back foot turned in.",
            "Bend the front knee over the ankle.",
            "Reach arms out at shoulder height.",
            "Look over your front hand."),
            "Sink the hips while lifting through the chest.", "Front knee collapsing inward.", "warrior"),
        g("Triangle Pose", "Hamstrings, side body", listOf(
            "Feet wide, front toes forward, both legs straight.",
            "Reach forward over the front leg, then hinge sideways.",
            "Rest your hand on your shin or a block.",
            "Reach the top arm to the sky."),
            "Imagine your body between two panes of glass.", "Collapsing the chest toward the floor.", "triangle"),
        g("Child's Pose", "Back, hips (rest)", listOf(
            "Kneel, big toes together, knees apart.",
            "Sit hips back to your heels.",
            "Walk hands forward and rest your forehead down.",
            "Breathe slowly into your back."),
            "Let each exhale soften you further.", "Forcing hips down if knees complain. Add a pillow.", "child"),
        g("Pilates Hundred", "Deep core", listOf(
            "Lie down, legs in tabletop or extended at 45°.",
            "Curl head and shoulders up, arms long by your sides.",
            "Pump arms up and down in small pulses.",
            "Inhale for 5 pulses, exhale for 5. Ten rounds."),
            "Keep the low back gently pressed down.", "Straining your neck. Rest your head if needed.", "hundred"),
        g("Roll-Up", "Abs, spine mobility", listOf(
            "Lie flat, arms overhead, legs long.",
            "Raise arms, curl chin to chest and peel up one vertebra at a time.",
            "Reach for your toes with a rounded spine.",
            "Roll back down slowly."),
            "Move slowly. Control beats momentum.", "Jerking up with the arms.", "roll-up"),
        g("Single-Leg Stretch", "Abs, hip flexors", listOf(
            "Curl head and shoulders up, hug one knee in.",
            "Extend the other leg long at 45°.",
            "Switch legs, swapping hands to the bent knee.",
            "Keep your torso still."),
            "Exhale each time you switch.", "Rocking the torso side to side.", "single-leg stretch"),
        g("Pilates Swimming", "Back, glutes, shoulders", listOf(
            "Lie face down, arms and legs long.",
            "Lift chest, arms and legs slightly off the mat.",
            "Flutter opposite arm and leg in small quick beats.",
            "Keep your neck long, eyes down."),
            "Lengthen out before you lift up.", "Cranking your neck up to look forward.", "swimming (pilates)"),
        g("Neck & Shoulder Rolls", "Neck, upper traps", listOf(
            "Sit or stand tall.",
            "Drop your chin and slowly roll the head ear to ear.",
            "Roll shoulders up, back and down 10 times.",
            "Reverse direction."),
            "Slow and easy, never forced.", "Rolling the head far backward.", "neck roll", "shoulder roll"),
        g("Hip Flexor Stretch", "Hip flexors, quads", listOf(
            "Kneel on one knee, other foot forward.",
            "Tuck your pelvis under and squeeze the back glute.",
            "Shift forward gently until you feel the front of the hip stretch.",
            "Reach the same-side arm overhead for more."),
            "The tuck matters more than how far you lean.", "Arching the lower back.", "hip flexor"),
        g("Lat Pulldown / Pull-Up", "Lats, biceps, upper back", listOf(
            "Grip the bar slightly wider than shoulders.",
            "Pull shoulder blades down first.",
            "Pull elbows toward your ribs until the chin passes the bar (or bar reaches your chest).",
            "Lower with control to full stretch."),
            "Think 'elbows to back pockets'.", "Swinging or using momentum.", "pulldown", "pull-up"),
        g("Face Pull", "Rear delts, rotator cuff", listOf(
            "Set a rope at face height on a cable or band.",
            "Pull the rope toward your eyes, elbows high.",
            "Pull the ends apart beside your ears.",
            "Return slowly."),
            "Finish in a 'double biceps' pose.", "Letting elbows drop below shoulders.", "face pull"),
        g("Lateral Raise", "Side delts", listOf(
            "Stand tall, light dumbbells at your sides.",
            "Raise arms out to the sides with soft elbows.",
            "Stop at shoulder height.",
            "Lower over 2-3 seconds."),
            "Lead with the elbows, pinkies slightly up.", "Shrugging or swinging heavy weights.", "lateral raise"),
        g("Incline Dumbbell Press", "Upper chest, shoulders, triceps", listOf(
            "Set bench to 30°, dumbbells at shoulder level.",
            "Shoulder blades back and down.",
            "Press up and slightly in.",
            "Lower to a stretch at chest level."),
            "Wrists stacked over elbows.", "Too steep an incline turning it into a shoulder press.", "incline dumbbell", "dumbbell press"),
        g("Legs Up the Wall", "Calms the nervous system, eases tired legs", listOf(
            "Sit sideways next to a wall.",
            "Swing your legs up the wall as you lie back.",
            "Rest your arms out to the sides, palms up.",
            "Breathe slowly for 3-5 minutes."),
            "Scoot your hips as close to the wall as comfortable.", "Forcing straight legs if hamstrings are tight. Bend the knees.", "legs up the wall", "legs-up"),
        g("Supine Spinal Twist", "Lower back, glutes", listOf(
            "Lie on your back and hug one knee in.",
            "Guide that knee across your body toward the floor.",
            "Stretch the opposite arm out and look that way.",
            "Hold 60 seconds, then switch."),
            "Let gravity do the work. Relax with every exhale.", "Pushing the knee down hard.", "supine twist", "spinal twist"),
        g("Seated Forward Fold", "Hamstrings, back (calming)", listOf(
            "Sit with legs straight in front of you.",
            "Inhale tall, then exhale and fold forward from the hips.",
            "Rest your hands wherever they reach.",
            "Breathe slowly and soften."),
            "Bend your knees so your back can relax.", "Yanking yourself deeper.", "forward fold"),
        g("Butterfly Stretch", "Inner thighs, hips", listOf(
            "Sit and bring the soles of your feet together.",
            "Hold your feet and sit tall.",
            "Let your knees fall open.",
            "Lean forward slightly for more stretch."),
            "Gently flutter the knees to release tension.", "Pressing your knees down with your hands.", "butterfly"),
        g("4-7-8 Breathing", "Relaxation, sleep onset", listOf(
            "Sit or lie comfortably and exhale fully.",
            "Breathe in quietly through your nose for 4 counts.",
            "Hold for 7 counts.",
            "Exhale through your mouth for 8 counts. Repeat 4 times."),
            "Slow and gentle. The long exhale is what calms you.", "Straining. Shorten the counts if you feel light-headed.", "4-7-8", "breathing"),
    )

    private fun g(name: String, muscles: String, steps: List<String>, cue: String, avoid: String, vararg keys: String) =
        ExerciseGuide(name, muscles, steps, cue, avoid, keys.toList())

    /** Best guide for a free-text line like "Goblet squat 3x12". Longer keyword matches win. */
    fun find(line: String): ExerciseGuide? {
        val l = line.lowercase()
        return all.flatMap { g -> g.keywords.filter { l.contains(it) }.map { it.length to g } }
            .maxByOrNull { it.first }?.second
    }

    /** Pull a seconds value out of a line like "Plank 3x45s" or "Deep squat hold 60s". */
    fun secondsIn(line: String): Int? =
        Regex("""(\d+)\s*(s|sec|seconds)\b""", RegexOption.IGNORE_CASE).find(line)?.groupValues?.get(1)?.toIntOrNull()
            ?: Regex("""(\d+)\s*min""", RegexOption.IGNORE_CASE).find(line)?.groupValues?.get(1)?.toIntOrNull()?.times(60)

    /** Opens YouTube (or the browser) with a form-demo search. */
    fun demoIntent(name: String): Intent = Intent(Intent.ACTION_VIEW,
        Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode("how to $name proper form")))
}
