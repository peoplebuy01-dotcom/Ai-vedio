package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AspectRatio
import com.example.data.model.Language
import com.example.data.model.VideoStyle
import com.example.pipeline.CharacterBibleEngine
import com.example.pipeline.ScriptWriterEngine
import com.example.pipeline.StoryAnalysisEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NEXORA Video AI", appName)
    }

    @Test
    fun `story analysis calculates 10-minute scenes`() {
        val scenes10m = StoryAnalysisEngine.calculateScenesCount(10)
        assertEquals(65, scenes10m)

        val analysis = StoryAnalysisEngine.analyzeStory(
            prompt = "In the year 2184, orbital researchers discover a hidden signal.",
            topic = "The Signal",
            durationMinutes = 10,
            style = VideoStyle.CINEMATIC,
            language = Language.ENGLISH
        )
        assertEquals(65, analysis.targetScenesCount)
        assertTrue(analysis.characterPrototypes.isNotEmpty())
    }

    @Test
    fun `character bible generates consistent profiles`() {
        val analysis = StoryAnalysisEngine.analyzeStory(
            prompt = "A mysterious ancient temple awakening",
            topic = "Ancient Temple",
            durationMinutes = 10,
            style = VideoStyle.CINEMATIC,
            language = Language.HINDI
        )
        val characters = CharacterBibleEngine.generateCharacterBible(
            projectId = 42L,
            prototypes = analysis.characterPrototypes,
            videoStyleKeyword = "Cinematic"
        )
        assertTrue(characters.size >= 2)
        assertEquals("High definition cinematic facial structure, defined cheekbones, intense expressive eyes reflecting environment lighting, natural skin micro-creases.", characters[0].face)
    }

    @Test
    fun `script writer generates full timeline scenes`() {
        val analysis = StoryAnalysisEngine.analyzeStory(
            prompt = "A voyage through quantum space",
            topic = "Voyage",
            durationMinutes = 1,
            style = VideoStyle.CINEMATIC,
            language = Language.ENGLISH
        )
        val characters = CharacterBibleEngine.generateCharacterBible(
            projectId = 99L,
            prototypes = analysis.characterPrototypes,
            videoStyleKeyword = "Cinematic"
        )
        val scenes = ScriptWriterEngine.generateScenesScript(
            projectId = 99L,
            analysis = analysis,
            characters = characters,
            style = VideoStyle.CINEMATIC,
            language = Language.ENGLISH,
            aspectRatio = AspectRatio.LANDSCAPE_16_9
        )
        assertEquals(analysis.targetScenesCount, scenes.size)
        assertTrue(scenes.all { it.durationSeconds > 0 })
    }
}
