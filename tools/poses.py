# Keyframe poses for GuRu's in-house exercise animations.
# Angles in degrees. Limbs: 0 = pointing down, 90 = forward (facing right), 180 = up, -90 = back.
# Torso (hip -> shoulder): 0 = upright, 90 = leaning forward flat, -90 = lying back (head left).
# Fields: t torso, hd head tilt, lt/ls far thigh/shin, rt/rs near thigh/shin, lu/lf far upper/forearm, ru/rf near upper/forearm, up lift.
import json
FIELDS = ['t', 'hd', 'lt', 'ls', 'rt', 'rs', 'lu', 'lf', 'ru', 'rf', 'up']
def P(legs=None, arms=None, **kw):
    d = dict.fromkeys(FIELDS, 0)
    if legs: d['lt'], d['ls'], d['rt'], d['rs'] = legs[0], legs[1], legs[0], legs[1]
    if arms: d['lu'], d['lf'], d['ru'], d['rf'] = arms[0], arms[1], arms[0], arms[1]
    d.update(kw); return [d[f] for f in FIELDS]
def pat(frames, ms=900, prop=None, float_=False): return dict(frames=frames, ms=ms, prop=prop, float=float_)

STAND = P()
PU_TOP = P(t=64, legs=(-64, -64))
PU_BOT = P(t=80, legs=(-80, -80), arms=(-60, 70))
SQ_BOT = dict(t=35, legs=(80, -30))
DDOG = P(t=140, legs=(-32, -32), arms=(40, 40))
RUN_A = P(t=10, lt=-25, ls=-60, rt=70, rs=-10, lu=40, lf=110, ru=-45, rf=45)
RUN_B = P(t=10, rt=-25, rs=-60, lt=70, ls=-10, ru=40, rf=110, lu=-45, lf=45)
WALK_A = P(t=3, lt=-20, ls=-30, rt=25, rs=5, lu=20, lf=35, ru=-20, rf=-5)
WALK_B = P(t=3, rt=-20, rs=-30, lt=25, ls=5, ru=20, rf=35, lu=-20, lf=-5)
LYING = dict(t=-90)

PATTERNS = {
 'stand': pat([P(), P(hd=4)], 1600),
 'squat': pat([P(arms=(90, 90)), P(**SQ_BOT, arms=(90, 90))], 1000),
 'goblet': pat([P(arms=(15, 165)), P(**SQ_BOT, arms=(25, 160))], 1000, 'kb'),
 'jumpsquat': pat([P(t=30, legs=(80, -30), arms=(-30, -20)), P(legs=(0, 0), arms=(170, 170), up=0.6), P(t=30, legs=(80, -30), arms=(-30, -20))], 450),
 'squathold': pat([P(t=25, legs=(100, -40), arms=(60, 110)), P(t=22, legs=(100, -40), arms=(60, 110), hd=6)], 1800),
 'wallsit': pat([P(legs=(90, 0), arms=(0, 0)), P(legs=(90, 0), arms=(5, 5), hd=4)], 1600, 'wall'),
 'rdl': pat([P(), P(t=80, legs=(8, 0))], 1100, 'db'),
 'kbswing': pat([P(t=65, legs=(15, -10), arms=(-15, -25)), P(arms=(90, 90))], 650, 'kb'),
 'bridge': pat([P(t=-92, legs=(145, 10), arms=(-95, -90)), P(t=-125, legs=(100, -10), arms=(-95, -90))], 1000),
 'lunge': pat([P(), P(rt=85, rs=-5, lt=-15, ls=-95)], 1000),
 'calf': pat([P(), P(up=0.13)], 700),
 'bench': pat([P(**LYING, legs=(90, 0), arms=(180, 180)), P(**LYING, legs=(90, 0), arms=(10, 175))], 1000, 'bench'),
 'pushup': pat([PU_TOP, PU_BOT], 900),
 'plank': pat([P(t=74, legs=(-74, -74), arms=(0, 90)), P(t=74, legs=(-74, -74), arms=(0, 90), hd=5)], 1600),
 'sideplank': pat([P(t=74, legs=(-74, -74), lu=0, lf=90, ru=180, rf=180), P(t=72, legs=(-72, -72), lu=0, lf=90, ru=170, rf=170)], 1400),
 'ohp': pat([P(arms=(25, 170)), P(arms=(178, 178))], 900, 'db'),
 'row': pat([P(t=75, legs=(10, 5)), P(t=75, legs=(10, 5), arms=(-75, 0))], 800, 'db'),
 'pullapart': pat([P(arms=(90, 90)), P(arms=(-30, -30))], 900),
 'curl': pat([P(), P(arms=(0, 155))], 800, 'db'),
 'dip': pat([P(t=-5, legs=(80, 10), arms=(-15, -15)), P(t=-5, legs=(60, -20), arms=(-95, 10))], 900, 'bench2'),
 'deadbug': pat([P(**LYING, legs=(180, 90), arms=(180, 180)), P(**LYING, lt=180, ls=90, rt=100, rs=100, lu=-100, lf=-100, ru=180, rf=180),
                 P(**LYING, legs=(180, 90), arms=(180, 180)), P(**LYING, rt=180, rs=90, lt=100, ls=100, ru=-100, rf=-100, lu=180, lf=180)], 900),
 'bicycle': pat([P(t=-65, rt=150, rs=80, lt=110, ls=110, arms=(-150, 60)), P(t=-65, lt=150, ls=80, rt=110, rs=110, arms=(-150, 60))], 600),
 'twist': pat([P(t=-35, legs=(125, 60), arms=(120, 100)), P(t=-35, legs=(125, 60), arms=(60, 80))], 600),
 'legraise': pat([P(**LYING, legs=(90, 90), arms=(90, 90)), P(**LYING, legs=(175, 175), arms=(90, 90))], 1100),
 'burpee': pat([P(), P(t=60, legs=(100, -40)), PU_TOP, P(t=60, legs=(100, -40)), P(arms=(175, 175), up=0.5)], 500),
 'climber': pat([P(t=64, lt=-64, ls=-64, rt=20, rs=-70), P(t=64, rt=-64, rs=-64, lt=20, ls=-70)], 380),
 'run': pat([RUN_A, RUN_B], 380),
 'jog': pat([RUN_A, RUN_B], 480),
 'walk': pat([WALK_A, WALK_B], 560),
 'fastwalk': pat([WALK_A, WALK_B], 420),
 'highknees': pat([P(t=0, rt=95, rs=0, lu=40, lf=120, ru=-30, rf=60, up=0.08), P(t=0, lt=95, ls=0, ru=40, rf=120, lu=-30, lf=60, up=0.08)], 300),
 'carry': pat([P(t=0, lt=-20, ls=-30, rt=25, rs=5), P(t=0, rt=-20, rs=-30, lt=25, ls=5)], 560, 'db'),
 'sled': pat([P(t=55, lt=-40, ls=-45, rt=60, rs=-20, arms=(75, 80)), P(t=55, rt=-40, rs=-45, lt=60, ls=-20, arms=(75, 80))], 520, 'sled'),
 'wallball': pat([P(t=30, legs=(80, -30), arms=(20, 160)), P(arms=(170, 170))], 800, 'ball'),
 'catcow': pat([P(t=66, legs=(0, -90), arms=(0, 0), hd=70), P(t=74, legs=(0, -90), arms=(0, 0), hd=-30)], 1500),
 'child': pat([P(t=105, legs=(60, -90), arms=(100, 95)), P(t=108, legs=(60, -90), arms=(100, 95))], 2000),
 'downdog': pat([DDOG, P(t=138, legs=(-30, -30), arms=(42, 42))], 1800),
 'warrior': pat([P(rt=80, rs=0, lt=-55, ls=-55, ru=90, rf=90, lu=-90, lf=-90), P(t=2, rt=82, rs=0, lt=-55, ls=-55, ru=90, rf=90, lu=-90, lf=-90)], 1800),
 'triangle': pat([P(t=70, rt=35, rs=35, lt=-35, ls=-35, ru=15, rf=15, lu=180, lf=180), P(t=72, rt=35, rs=35, lt=-35, ls=-35, ru=15, rf=15, lu=180, lf=180)], 1800),
 'sunsal': pat([P(arms=(180, 180)), P(t=170, legs=(0, 0), arms=(0, 0)), PU_TOP, P(t=60, legs=(-92, -92), arms=(0, 0)), DDOG, P()], 1100),
 'swim': pat([P(t=90, lt=-85, ls=-85, rt=-95, rs=-95, ru=180, rf=180, lu=0, lf=0), P(t=90, lt=-95, ls=-95, rt=-85, rs=-85, ru=90, rf=90, lu=-90, lf=-90),
              P(t=90, lt=-85, ls=-85, rt=-95, rs=-95, ru=0, rf=0, lu=180, lf=180), P(t=90, lt=-95, ls=-95, rt=-85, rs=-85, ru=-90, rf=-90, lu=90, lf=90)], 450, 'water', True),
 'rower': pat([P(t=25, legs=(150, 30), arms=(90, 90)), P(t=-25, legs=(92, 90), arms=(-40, 70))], 900, 'rail'),
 'cycle': pat([P(t=45, rt=60, rs=-15, lt=95, ls=35, arms=(70, 60)), P(t=45, rt=90, rs=10, lt=70, ls=25, arms=(70, 60)),
               P(t=45, rt=95, rs=35, lt=60, ls=-15, arms=(70, 60)), P(t=45, rt=70, rs=25, lt=90, ls=10, arms=(70, 60))], 260, 'bike', True),
 'box': pat([P(t=8, rt=15, rs=0, lt=-15, ls=0, ru=30, rf=165, lu=20, lf=160), P(t=10, rt=15, rs=0, lt=-15, ls=0, ru=88, rf=88, lu=20, lf=160),
             P(t=8, rt=15, rs=0, lt=-15, ls=0, ru=30, rf=165, lu=20, lf=160), P(t=12, rt=15, rs=0, lt=-15, ls=0, ru=30, rf=165, lu=88, lf=88)], 330),
 'hundred': pat([P(t=-65, legs=(135, 135), arms=(75, 75)), P(t=-65, legs=(135, 135), arms=(62, 62))], 260),
 'rollup': pat([P(**LYING, legs=(90, 90), arms=(-180, -180)), P(t=75, legs=(90, 90), arms=(90, 90))], 1500),
 'slstretch': pat([P(t=-65, rt=160, rs=60, lt=105, ls=105, arms=(110, 60)), P(t=-65, lt=160, ls=60, rt=105, rs=105, arms=(110, 60))], 700),
 'pswim': pat([P(t=84, lt=-95, ls=-95, rt=-85, rs=-85, lu=95, lf=95, ru=85, rf=85), P(t=84, rt=-95, rs=-95, lt=-85, ls=-85, ru=95, rf=95, lu=85, lf=85)], 330),
 'neck': pat([P(hd=-25), P(hd=25)], 1400),
 'hipflexor': pat([P(rt=85, rs=0, lt=-10, ls=-90), P(t=-8, rt=88, rs=0, lt=-10, ls=-90, ru=178, rf=178)], 1600),
 'pulldown': pat([P(legs=(90, 0), arms=(180, 180)), P(legs=(90, 0), arms=(10, 175))], 1000, 'pbar'),
 'facepull': pat([P(arms=(90, 90)), P(arms=(-80, 150))], 900),
 'latraise': pat([P(), P(arms=(80, 80))], 900, 'db'),
 'incline': pat([P(t=-35, legs=(90, 0), arms=(-10, 170)), P(t=-35, legs=(90, 0), arms=(155, 155))], 1000, 'db'),
 'stair': pat([P(t=8, rt=70, rs=0, lt=-5, ls=-5, lu=20, lf=40, ru=-20, rf=10), P(t=8, lt=70, ls=0, rt=-5, rs=-5, ru=20, rf=40, lu=-20, lf=10)], 520, 'steps'),
 'wgs': pat([P(t=60, rt=95, rs=-15, lt=-70, ls=-75, ru=0, rf=0, lu=0, lf=0), P(t=60, rt=95, rs=-15, lt=-70, ls=-75, ru=0, rf=0, lu=180, lf=180)], 1400),
 'ninety': pat([P(rt=100, rs=20, lt=140, ls=40), P(lt=100, ls=20, rt=140, rs=40)], 1200),
 'tspine': pat([P(**LYING, legs=(90, 90), arms=(90, 90)), P(**LYING, legs=(90, 90), lu=90, lf=90, ru=180, rf=180),
                P(**LYING, legs=(90, 90), lu=90, lf=90, ru=-90, rf=-90), P(**LYING, legs=(90, 90), lu=90, lf=90, ru=180, rf=180)], 1100),
 'legcircle': pat([P(**LYING, lt=90, ls=90, rt=150, rs=150, arms=(90, 90)), P(**LYING, lt=90, ls=90, rt=175, rs=175, arms=(90, 90)),
                   P(**LYING, lt=90, ls=90, rt=200, rs=200, arms=(90, 90)), P(**LYING, lt=90, ls=90, rt=175, rs=175, arms=(90, 90))], 500),
 'treadmill': pat([WALK_A, WALK_B], 560, 'tread'),
 # bedtime / sleep stretches
 'legsupwall': pat([P(**LYING, legs=(180, 180), arms=(-120, -120)), P(**LYING, legs=(178, 178), arms=(-120, -120), hd=3)], 2400, 'wallr'),
 'supinetwist': pat([P(**LYING, rt=150, rs=60, lt=90, ls=90, ru=-90, rf=-90, lu=90, lf=90), P(**LYING, rt=120, rs=40, lt=90, ls=90, ru=-90, rf=-90, lu=90, lf=90)], 2200),
 'fold': pat([P(t=75, legs=(90, 90), arms=(90, 90)), P(t=85, legs=(90, 90), arms=(95, 95))], 2200),
 'butterfly': pat([P(t=5, rt=125, rs=40, lt=125, ls=40, arms=(30, 60)), P(t=15, rt=110, rs=35, lt=110, ls=35, arms=(30, 60))], 1600),
 'breathe': pat([P(t=0, legs=(88, -92), arms=(20, 80)), P(t=-5, legs=(88, -92), arms=(20, 80), hd=-8)], 2600),
}

# guide name -> pattern
MAP = {
 'Goblet Squat': 'goblet', 'Squat': 'squat', 'Jump Squat': 'jumpsquat', 'Deep Squat Hold': 'squathold', 'Wall Sit': 'wallsit',
 'Romanian Deadlift': 'rdl', 'Hip Thrust': 'bridge', 'Glute Bridge': 'bridge', 'Walking Lunge': 'lunge', 'Calf Raise': 'calf',
 'Bench Press': 'bench', 'Push-Up': 'pushup', 'Overhead Press': 'ohp', 'Row': 'row', 'Band Pull-Apart': 'pullapart',
 'Bicep Curl': 'curl', 'Tricep Dip': 'dip', 'Plank': 'plank', 'Side Plank': 'sideplank', 'Dead Bug': 'deadbug',
 'Bicycle Crunch': 'bicycle', 'Russian Twist': 'twist', 'Leg Raise': 'legraise', 'Burpee': 'burpee', 'Mountain Climber': 'climber',
 'High Knees': 'highknees', 'Kettlebell Swing': 'kbswing', 'Wall Ball': 'wallball', 'Farmer Carry': 'carry', 'Sled Push': 'sled',
 'Burpee Broad Jump': 'burpee', 'Cat-Cow': 'catcow', "World's Greatest Stretch": 'wgs', '90/90 Hip Switch': 'ninety',
 'Thoracic Rotation': 'tspine', 'Leg Circles': 'legcircle', 'Incline Treadmill Walk': 'treadmill', 'Easy Run / Jog': 'jog',
 'Zone 2 Cardio': 'walk', 'Stair Climber': 'stair', 'Interval Walking': 'fastwalk', 'Shadowboxing': 'box', 'Rowing Machine': 'rower',
 'Indoor Cycling': 'cycle', 'Freestyle Swim': 'swim', 'Sun Salutation A': 'sunsal', 'Downward Dog': 'downdog', 'Warrior II': 'warrior',
 'Triangle Pose': 'triangle', "Child's Pose": 'child', 'Pilates Hundred': 'hundred', 'Roll-Up': 'rollup', 'Single-Leg Stretch': 'slstretch',
 'Pilates Swimming': 'pswim', 'Neck & Shoulder Rolls': 'neck', 'Hip Flexor Stretch': 'hipflexor', 'Lat Pulldown / Pull-Up': 'pulldown',
 'Face Pull': 'facepull', 'Lateral Raise': 'latraise', 'Incline Dumbbell Press': 'incline',
 'Legs Up the Wall': 'legsupwall', 'Supine Spinal Twist': 'supinetwist', 'Seated Forward Fold': 'fold', 'Butterfly Stretch': 'butterfly',
 '4-7-8 Breathing': 'breathe',
}

if __name__ == '__main__':
    d = json.load(open('/home/claude/tools/presets.json'))
    def G_(name, mus, steps, cue, avoid, *keys):
        if not any(g['name'] == name for g in d['G']):
            d['G'].append(dict(name=name, mus=mus, steps=steps, cue=cue, avoid=avoid, keys=list(keys)))
    G_('Legs Up the Wall', 'Calms the nervous system, eases tired legs', ['Sit sideways next to a wall.', 'Swing your legs up the wall as you lie back.', 'Rest your arms out to the sides, palms up.', 'Breathe slowly for 3-5 minutes.'], 'Scoot your hips as close to the wall as comfortable.', 'Forcing straight legs if hamstrings are tight. Bend the knees.', 'legs up the wall', 'legs-up')
    G_('Supine Spinal Twist', 'Lower back, glutes', ['Lie on your back and hug one knee in.', 'Guide that knee across your body toward the floor.', 'Stretch the opposite arm out and look that way.', 'Hold 60 seconds, then switch.'], 'Let gravity do the work. Relax with every exhale.', 'Pushing the knee down hard.', 'supine twist', 'spinal twist')
    G_('Seated Forward Fold', 'Hamstrings, back (calming)', ['Sit with legs straight in front of you.', 'Inhale tall, then exhale and fold forward from the hips.', 'Rest your hands wherever they reach.', 'Breathe slowly and soften.'], 'Bend your knees so your back can relax.', 'Yanking yourself deeper.', 'forward fold')
    G_('Butterfly Stretch', 'Inner thighs, hips', ['Sit and bring the soles of your feet together.', 'Hold your feet and sit tall.', 'Let your knees fall open.', 'Lean forward slightly for more stretch.'], 'Gently flutter the knees to release tension.', 'Pressing your knees down with your hands.', 'butterfly')
    G_('4-7-8 Breathing', 'Relaxation, sleep onset', ['Sit or lie comfortably and exhale fully.', 'Breathe in quietly through your nose for 4 counts.', 'Hold for 7 counts.', 'Exhale through your mouth for 8 counts. Repeat 4 times.'], 'Slow and gentle. The long exhale is what calms you.', 'Straining. Shorten the counts if you feel light-headed.', '4-7-8', 'breathing')
    if not any(w['name'] == 'Bedtime Wind-Down Stretch' for w in d['W']):
        d['W'].append(dict(name='Bedtime Wind-Down Stretch', cat='Mobility', min=12, lvl='Easy', met=2.0,
            ex="Neck & shoulder rolls 60s\nCat-cow 60s\nChild's pose 90s\nSupine twist 60s/side\nButterfly stretch 60s\nLegs up the wall 3 min\n4-7-8 breathing x4"))
    for g in d['G']:
        g['demo'] = MAP.get(g['name'], 'stand')
        if g['name'] not in MAP: print('unmapped', g['name'])
    json.dump(d, open('/home/claude/tools/presets.json', 'w'), indent=1)
    json.dump(PATTERNS, open('/home/claude/tools/poses.json', 'w'))
    print(len(PATTERNS), 'patterns', len(d['G']), 'guides', len(d['W']), 'workouts')
