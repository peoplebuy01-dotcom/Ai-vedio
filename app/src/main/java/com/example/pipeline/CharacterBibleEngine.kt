package com.example.pipeline

import com.example.data.model.CharacterEntity
import kotlin.random.Random

object CharacterBibleEngine {

    fun generateCharacterBible(
        projectId: Long,
        prototypes: List<CharacterPrototype>,
        videoStyleKeyword: String
    ): List<CharacterEntity> {
        val list = mutableListOf<CharacterEntity>()
        val rnd = Random(projectId)

        val hairStyles = listOf(
            "Side-swept jet-black hair with precision taper",
            "Textured silver-white braids tied back with metallic ringlets",
            "Tousled dark chestnut waves with windblown realism",
            "Close-cropped fade with subtle shaved cyber-patterns",
            "Auburn shoulder-length bob framed with sharp bangs"
        )

        val skinTones = listOf(
            "Warm golden bronze with subtle luminous subsurface scattering",
            "Deep rich espresso with velvet undertone",
            "Pale alabaster with fine porcelain skin texture",
            "Sun-kissed olive with subtle freckles across nasal bridge",
            "Tan weathered complexion with subtle character laughter lines"
        )

        val bodyTypes = listOf(
            "Athletic, poised, agile silhouette",
            "Broad-shouldered, commanding physical posture",
            "Slender, precise, fluid deliberate movement",
            "Stout, grounded, resilient stance"
        )

        val accessoriesList = listOf(
            "Cerulean ocular retinal glass lens, miniature carbon fiber comm-stud",
            "Brass engraved chronometer pendant, weathered leather wrist cuff",
            "Titanium magnetic collar clasp, polarized anti-glare specs",
            "Fiber-optic woven scarf, bio-metric feedback wristlet"
        )

        val personalities = listOf(
            "Analytical, relentlessly inquisitive, stoic under pressure, protective of comrades",
            "Intuitive, visionary, prone to bold leaps of faith, deeply empathetic",
            "Pragmatic, cynical yet fiercely loyal, master of tactical survival",
            "Mysterious, measured, contemplative with moments of sudden profound courage"
        )

        for ((index, proto) in prototypes.withIndex()) {
            val seed = projectId * 1000L + index * 37L + rnd.nextLong(100, 999)
            list.add(
                CharacterEntity(
                    projectId = projectId,
                    name = proto.name,
                    role = proto.role,
                    face = "High definition cinematic facial structure, defined cheekbones, intense expressive eyes reflecting environment lighting, natural skin micro-creases.",
                    hair = hairStyles[index % hairStyles.size],
                    clothes = "${proto.visualKeyword}, tailored for $videoStyleKeyword aesthetic with realistic fabric drape, crease physics, and ambient occlusion.",
                    age = proto.age,
                    bodyType = bodyTypes[index % bodyTypes.size],
                    skinTone = skinTones[index % skinTones.size],
                    accessories = accessoriesList[index % accessoriesList.size],
                    personality = personalities[index % personalities.size],
                    voiceCharacteristics = proto.voiceType,
                    avatarSeed = seed
                )
            )
        }

        return list
    }
}
