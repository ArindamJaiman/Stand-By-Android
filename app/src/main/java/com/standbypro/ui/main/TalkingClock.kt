package com.standbypro.ui.main

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.standbypro.ui.faces.FaceMath
import java.util.Locale

/** Speaks the time aloud ("It's quarter past seven") with the system text-to-speech engine. */
class TalkingClock(context: Context) : TextToSpeech.OnInitListener {

    @Volatile
    private var ready = false
    private val tts = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        // Phrases are English, so prefer an English voice when the device locale isn't.
        if (!Locale.getDefault().language.equals("en", ignoreCase = true)) {
            runCatching { tts.language = Locale.ENGLISH }
        }
        ready = true
    }

    fun speak(hour24: Int, minute: Int) {
        if (!ready) return
        tts.speak("It's ${FaceMath.spokenTime(hour24, minute)}", TextToSpeech.QUEUE_FLUSH, null, "standby_time")
    }

    fun shutdown() = tts.shutdown()
}

@Composable
fun rememberTalkingClock(): TalkingClock {
    val context = LocalContext.current
    val clock = remember { TalkingClock(context) }
    DisposableEffect(clock) { onDispose { clock.shutdown() } }
    return clock
}
