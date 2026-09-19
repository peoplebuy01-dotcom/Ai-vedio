package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.ProjectEntity
import com.example.pipeline.ActivePipelineStatus
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NexoraDarkBg
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun greeting_screenshot() {
        val sampleProjects = listOf(
            ProjectEntity(
                id = 1L,
                title = "The Forgotten Resonance",
                prompt = "A deep space discovery...",
                topic = "The Forgotten Resonance",
                durationMinutes = 10,
                language = "ENGLISH",
                voiceGender = "MALE",
                videoStyle = "CINEMATIC",
                aspectRatio = "LANDSCAPE_16_9",
                status = "COMPLETED",
                progress = 100,
                currentStageText = "Complete",
                totalScenes = 65,
                completedScenes = 65
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NexoraDarkBg
                ) {
                    DashboardScreen(
                        projects = sampleProjects,
                        pipelineStatus = com.example.pipeline.ActivePipelineStatus(),
                        onNavigateToCreate = {},
                        onOpenProject = {},
                        onDownloadProject = {}
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
