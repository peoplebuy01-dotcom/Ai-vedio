package com.example.pipeline

import com.example.data.model.*
import kotlin.random.Random

object ScriptWriterEngine {

    fun generateScenesScript(
        projectId: Long,
        analysis: StoryAnalysisResult,
        characters: List<CharacterEntity>,
        style: VideoStyle,
        language: Language,
        aspectRatio: AspectRatio
    ): List<SceneEntity> {
        val totalScenes = analysis.targetScenesCount
        val scenes = mutableListOf<SceneEntity>()
        val rnd = Random(projectId + 42)

        val cameraMoves = CameraMovement.values()
        val transitions = TransitionType.values()

        val mainChar = characters.firstOrNull()?.name ?: "The Wanderer"
        val companionChar = characters.getOrNull(1)?.name ?: "The Observer"

        val sfxPool = listOf(
            "Heavy automated vault door hiss, pneumatic pressure release",
            "Deliberate echoing footsteps on wet obsidian stone",
            "Howling atmospheric winds whistling through canyon spires",
            "Distant low-frequency thunder, sudden raindrops striking glass",
            "Muffled rhythmic heartbeat increasing in tension",
            "High-tech propulsion drone humming, magnetic levitation hum",
            "Murmuring crowd whispers falling silent, subtle rustling leaves",
            "Sudden electrical spark crackle, fluctuating neon transformer hum",
            "Deep sub-bass impact drop, shattering crystalline resonance",
            "Gentle natural forest ambience, distant nocturnal bird call"
        )

        val musicMoods = listOf(
            "Ethereal mystery with low cello swell and celestial synth pads",
            "Mounting psychological tension with sharp violin ostinato",
            "Grand cinematic brass crescendo, thunderous percussion pulse",
            "Quiet emotional acoustic resonance with solitary piano melody",
            "Driving cyberpunk pulse with distorted analog bass arpeggios",
            "Dark atmospheric silence punctuated by eerie chime echoes"
        )

        val colorPalettes = listOf(
            Pair(0xFF0F172A, 0xFF0284C7), // Deep midnight & cyan
            Pair(0xFF18181B, 0xFF7C3AED), // Charcoal & violet
            Pair(0xFF0A0A0A, 0xFFDC2626), // Obsidian & crimson
            Pair(0xFF052E16, 0xFF10B981), // Emerald forest & jade
            Pair(0xFF1E1B4B, 0xFFF59E0B), // Twilight indigo & amber
            Pair(0xFF172554, 0xFF38BDF8)  // Deep ocean & sky blue
        )

        for (i in 1..totalScenes) {
            val progressRatio = i.toFloat() / totalScenes.toFloat()
            val act = when {
                progressRatio < 0.33f -> 1
                progressRatio < 0.75f -> 2
                else -> 3
            }

            val cam = cameraMoves[(i + rnd.nextInt(2)) % cameraMoves.size]
            val trans = if (i == totalScenes) TransitionType.DIP_TO_BLACK else transitions[i % transitions.size]
            val sfx = sfxPool[(i + rnd.nextInt(3)) % sfxPool.size]
            val music = musicMoods[(i + (act * 2)) % musicMoods.size]
            val palette = colorPalettes[(act + i) % colorPalettes.size]

            val (visualDesc, actions, env) = buildSceneVisuals(
                act = act,
                sceneIndex = i,
                totalScenes = totalScenes,
                mainChar = mainChar,
                companionChar = companionChar,
                style = style,
                analysis = analysis
            )

            val (dialogueSpeaker, dialogueText) = if (i % 3 == 0) {
                val speaker = if (i % 6 == 0) companionChar else mainChar
                Pair(speaker, buildDialogue(speaker, act, language, i))
            } else {
                Pair(null, null)
            }

            val narration = buildNarration(
                act = act,
                sceneIndex = i,
                totalScenes = totalScenes,
                mainChar = mainChar,
                language = language,
                analysis = analysis
            )

            val visualSeed = projectId * 10000L + i * 137L + rnd.nextLong(1, 999)

            scenes.add(
                SceneEntity(
                    projectId = projectId,
                    sceneIndex = i,
                    durationSeconds = analysis.calculatedSceneDuration,
                    visualDescription = visualDesc,
                    characterActions = actions,
                    cameraMovement = cam.name,
                    environment = env,
                    dialogueSpeaker = dialogueSpeaker,
                    dialogueText = dialogueText,
                    narrationText = narration,
                    soundEffect = sfx,
                    backgroundMusicMood = music,
                    transition = trans.name,
                    visualSeed = visualSeed,
                    colorTonePrimary = palette.first,
                    colorToneSecondary = palette.second,
                    status = SceneStatus.PENDING.name,
                    isCached = false
                )
            )
        }

        return scenes
    }

    private fun buildSceneVisuals(
        act: Int,
        sceneIndex: Int,
        totalScenes: Int,
        mainChar: String,
        companionChar: String,
        style: VideoStyle,
        analysis: StoryAnalysisResult
    ): Triple<String, String, String> {
        val styleTag = style.promptModifier

        val env = when (act) {
            1 -> "${analysis.primaryLocation} — Exterior gate at dusk. Volumetric fog rolling over reflective wet ground. Ambient street lanterns casting geometric shadows."
            2 -> "Interior subterranean chamber beneath the observatory. Ancient towering pillars engraved with glowing circuitry. Shimmering dust motes floating in shaft of cobalt light."
            else -> "The Pinnacle Spire overlooking boundless horizon. Aurora borealis illuminating storm clouds above. Shattered monolith levitating in mid-air."
        }

        val actions = when (act) {
            1 -> "$mainChar walks forward with deliberate vigilance, gloves adjusting high-collar coat, pausing to survey the distant horizon with sharp narrowed gaze."
            2 -> "$mainChar reaches forward to touch the humming core relic. Energy ripples illuminate $mainChar's face as $companionChar urgently signals from behind."
            else -> "$mainChar stands defiant amidst the swirling temporal vortex, holding the illuminated key aloft as reality stabilizes in a blinding flash of golden luminescence."
        }

        val visualDesc = "Cinematic ${if (sceneIndex % 2 == 0) "wide shot" else "medium close-up"} at $env. $actions. Visual style: $styleTag. Volumetric atmospheric lighting, hyper-detailed texture, depth of field 1.8f."

        return Triple(visualDesc, actions, env)
    }

    private fun buildDialogue(
        speaker: String,
        act: Int,
        language: Language,
        sceneIndex: Int
    ): String {
        return when (language) {
            Language.HINDI -> when (act) {
                1 -> "हमे आगे बढ़ना होगा... समय बहुत कम बचा है।"
                2 -> "देखो यह क्या है! यह संकेत हमारे पुराने नक्शे से पूरी तरह मेल खाता है।"
                else -> "हम आ चुके हैं। अब सब कुछ इस एक फैसले पर निर्भर करता है।"
            }
            Language.GUJARATI -> when (act) {
                1 -> "આપણે આગળ વધવું જ પડશે... સમય ખૂબ ઓછો છે."
                2 -> "જુઓ આ તરફ! આ સંકેત આપણા રહસ્યમય નકશા સાથે મેળ ખાય છે."
                else -> "આપણે પહોંચી ગયા છીએ. હવે બધું આ એક નિર્ણય પર નિર્ભર છે."
            }
            Language.SPANISH -> when (act) {
                1 -> "Debemos continuar... el tiempo se nos está acabando."
                2 -> "¡Mira esto! Las coordenadas coinciden perfectamente con el artefacto."
                else -> "Hemos llegado al umbral final. Todo depende de este momento."
            }
            else -> when (act) {
                1 -> "There is no turning back now. The signals have crossed the threshold."
                2 -> "Look closely at the resonance pattern—it's responding to the neural key!"
                else -> "We stand at the apex of the cycle. Stand firm, no matter what opens."
            }
        }
    }

    private fun buildNarration(
        act: Int,
        sceneIndex: Int,
        totalScenes: Int,
        mainChar: String,
        language: Language,
        analysis: StoryAnalysisResult
    ): String {
        return when (language) {
            Language.HINDI -> when (act) {
                1 -> "दृश्य $sceneIndex: इस अंधेरी रात के सन्नाटे में, $mainChar ने कदम बढ़ाए। हर साए में एक अनकहा रहस्य छिपा हुआ था।"
                2 -> "दृश्य $sceneIndex: जैसे-जैसे वो गहराई में उतरे, प्राचीन शक्तियों की गूंज और तेज होती गई। हर संकेत एक नई पहेली बन रहा था।"
                else -> "दृश्य $sceneIndex: यह अंतिम निर्णायक मोड़ था। प्रकाश और अंधकार के बीच का वो क्षण, जो इतिहास को हमेशा के लिए बदलने वाला था।"
            }
            Language.GUJARATI -> when (act) {
                1 -> "દ્રશ્ય $sceneIndex: શાંત રાત્રિના અંધકારમાં, $mainChar એ આગળ ડગ ભર્યા. દરેક પડછાયા પાછળ એક અજાણ્યું રહસ્ય છુપાયેલું હતું."
                2 -> "દ્રશ્ય $sceneIndex: જેમ જેમ આગળ વધતા ગયા, તેમ તેમ પ્રાચીન ઊર્જાનો ગૂંજતો અવાજ વધુ તીવ્ર બનતો ગયો."
                else -> "દ્રશ્ય $sceneIndex: આ આખરી અને નિર્ણાયક ક્ષણ હતી. અહીંથી ભવિષ્યનો નવો અધ્યાય શરૂ થવાનો હતો."
            }
            Language.SPANISH -> when (act) {
                1 -> "Escena $sceneIndex: En el silencio de la noche, $mainChar avanzó con determinación. Cada sombra ocultaba un secreto ancestral."
                2 -> "Escena $sceneIndex: A medida que descendían a las profundidades, las resonancias del pasado despertaban con una fuerza implacable."
                else -> "Escena $sceneIndex: Este era el momento culminante. Donde el destino de ambos mundos se decidiría para siempre."
            }
            else -> when (act) {
                1 -> "Scene $sceneIndex: In the stillness before dawn, $mainChar ventured past the perimeter. Every silhouette whispered secrets of what was lost."
                2 -> "Scene $sceneIndex: Descending into the heart of the anomaly, the electromagnetic pulse intensified, bending light and gravity alike."
                else -> "Scene $sceneIndex: The climactic nexus opened before them—a singular convergence that would forever redefine their destiny."
            }
        }
    }
}
