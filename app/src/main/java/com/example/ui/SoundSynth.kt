package com.example.ui

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

object SoundSynth {
    fun playMelody(frequencies: DoubleArray, durationsMs: IntArray) {
        Thread {
            try {
                val sampleRate = 8000
                var totalDurationSeconds = 0.0
                for (d in durationsMs) {
                    totalDurationSeconds += d / 1000.0
                }
                
                val totalSamples = (sampleRate * totalDurationSeconds).toInt()
                val buffer = ShortArray(totalSamples)
                
                var currentSample = 0
                for (i in frequencies.indices) {
                    val freq = frequencies[i]
                    val durationMs = durationsMs[i]
                    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                    
                    for (j in 0 until numSamples) {
                        if (currentSample >= totalSamples) break
                        
                        // Sine wave generation
                        val angle = 2.0 * Math.PI * freq * j / sampleRate
                        var sample = Math.sin(angle)
                        
                        // Fade in and out to prevent popping clicks
                        val fadeRange = (numSamples * 0.1).toInt().coerceAtMost(100)
                        if (j < fadeRange) {
                            sample *= (j.toDouble() / fadeRange)
                        } else if (j > numSamples - fadeRange) {
                            sample *= ((numSamples - j).toDouble() / fadeRange)
                        }
                        
                        buffer[currentSample] = (sample * 8000).toInt().toShort()
                        currentSample++
                    }
                }
                
                @Suppress("DEPRECATION")
                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    AudioTrack.MODE_STATIC
                )
                
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                
                // Wait for playback and release
                Thread.sleep((totalDurationSeconds * 1000).toLong() + 100)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun playSuccess() {
        playMelody(
            doubleArrayOf(880.0, 1109.73, 1318.51), // A5, C#6, E6 - Beautiful A Major chord arpeggio
            intArrayOf(100, 100, 180)
        )
    }

    fun playError() {
        playMelody(
            doubleArrayOf(220.0, 180.0), // Low buzz sliding down
            intArrayOf(150, 200)
        )
    }
}
