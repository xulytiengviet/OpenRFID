/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.common.sound

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * High-performance audio beeper for tag scan audio feedback.
 * Uses system ToneGenerator for zero-latency acoustic feedback on handheld devices.
 */
object SoundBeeper {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (_: Exception) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Plays a short high-pitched RFID tag scan beep (approx 50ms).
     */
    fun playBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (_: Exception) {
        }
    }
}
