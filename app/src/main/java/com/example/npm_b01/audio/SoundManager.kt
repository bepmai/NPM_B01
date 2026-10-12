package com.example.npm_b01.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundManager {

    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Sound 1: Pick Up Block (Chime pop)
     */
    fun playPickUpSound() {
        scope.launch {
            playToneSweep(startFreq = 650f, endFreq = 1000f, durationMs = 50)
        }
    }

    /**
     * Sound 2: Place Block (Solid thud)
     */
    fun playPlaceSound() {
        scope.launch {
            playToneSweep(startFreq = 320f, endFreq = 160f, durationMs = 70)
        }
    }

    /**
     * Sound 3: Clear Line (Triumphant Arpeggio)
     */
    fun playLineClearSound(combo: Int = 1) {
        scope.launch {
            val mult = 1f + (combo - 1) * 0.1f
            playToneSweep(523.25f * mult, 659.25f * mult, 60)
            playToneSweep(659.25f * mult, 783.99f * mult, 60)
            playToneSweep(783.99f * mult, 1046.50f * mult, 100)
        }
    }

    private fun playToneSweep(startFreq: Float, endFreq: Float, durationMs: Int) {
        val sampleRate = 22050
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)

        var currentPhase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = startFreq + (endFreq - startFreq) * progress
            val phaseIncrement = 2.0 * Math.PI * freq / sampleRate
            currentPhase += phaseIncrement

            // Envelope
            val envelope = when {
                i < numSamples * 0.1f -> i / (numSamples * 0.1f)
                i > numSamples * 0.7f -> (numSamples - i) / (numSamples * 0.3f)
                else -> 1f
            }

            val sample = (sin(currentPhase) * 32767 * 0.35f * envelope).toInt()
            buffer[i] = sample.coerceIn(-32768, 32767).toShort()
        }

        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 10)
            audioTrack.release()
        } catch (_: Exception) {}
    }
}
