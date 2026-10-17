package com.standbypro.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.standbypro.domain.FaceContext
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.ui.faces.DigitalSpokenFace
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/** Smoke tests that render real faces on a device. */
class MainScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sevenOClock = FaceContext(time = LocalDateTime.of(2026, 10, 17, 7, 0), isPreview = true)

    @Test
    fun spokenFace_showsTimeInWords() {
        composeTestRule.setContent { DigitalSpokenFace(sevenOClock, Modifier.size(600.dp, 300.dp)) }
        composeTestRule.onNodeWithText("o'clock").assertExists()
        composeTestRule.onNodeWithText("seven").assertExists()
    }

    @Test
    fun everyFace_rendersWithoutCrashing() {
        val ids = WatchFaceRegistry.getAll().map { it.id }
        var index by mutableIntStateOf(0)
        composeTestRule.setContent {
            WatchFaceRegistry.Render(ids[index], sevenOClock, Modifier.fillMaxSize())
        }
        ids.indices.forEach { i ->
            composeTestRule.runOnIdle { index = i }
            composeTestRule.waitForIdle()
        }
    }
}
