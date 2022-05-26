package com.github.lplath.handlers

import com.bitwig.extension.controller.api.*
import com.github.lplath.Hardware
import com.github.lplath.Mapping

class PadHandler(
    private val host: ControllerHost,
    private val hardware: Hardware
) {
    private val transport: Transport = host.createTransport()
    private val assignedMacros = MutableList(8) { "Unassigned" }
    private val macros = mapOf(
        "Unassigned" to {},
        "Continue Playback" to { transport.continuePlayback() },
        "Toggle Play" to { transport.togglePlay() },
        "Restart" to { transport.restart() },
        "Record" to { transport.record() },
        "Rewind" to { transport.rewind() },
        "Fast Forward" to { transport.fastForward() },
        "Tap Tempo" to { transport.tapTempo() },
        "Toggle Loop" to { transport.isArrangerLoopEnabled.toggle() },
        "Toggle Punch In" to { transport.isPunchInEnabled.toggle() },
        "Toggle Punch Out" to { transport.isPunchOutEnabled.toggle() },
        "Toggle Click" to { transport.isMetronomeEnabled.toggle() },
        "Toggle Metronome Ticks" to { transport.isMetronomeTickPlaybackEnabled.toggle() },
        "Toggle Metronome During Pre-Roll" to { transport.isMetronomeAudibleDuringPreRoll.toggle() },
        "Toggle Overdub" to { transport.isArrangerOverdubEnabled.toggle() },
        "Toggle Launcher Overdub" to { transport.isClipLauncherOverdubEnabled.toggle() },
        "Toggle Latch Automation Write-Mode" to { transport.toggleLatchAutomationWriteMode() },
        "Toggle Write Arranger Automation" to { transport.toggleWriteArrangerAutomation() },
        "Toggle Write Clip-Launcher Automation" to { transport.toggleWriteClipLauncherAutomation() },
        "Reset Automation Overrides" to { transport.resetAutomationOverrides() },
        "Return To Arrangement" to { transport.returnToArrangement() },
        "Launch From Play-Start Position" to { transport.launchFromPlayStartPosition() },
        "Jump To Play-Start Position" to { transport.jumpToPlayStartPosition() },
        "Jump To Previous Cue-Marker" to { transport.jumpToPreviousCueMarker() },
        "Jump To Next Cue-Marker" to { transport.jumpToNextCueMarker() }
    )

    init {
        val macroNames = macros.keys.toTypedArray()
        for (i in 0..7) {
            val macroSetting = host.preferences.getEnumSetting("Pad ${(i + 1)}", "Macros", macroNames, "Unassigned")
            macroSetting.markInterested()
            macroSetting.addValueObserver { macro ->
                assignedMacros[i] = macro
            }
        }

        val padRootSetting =
            host.preferences.getEnumSetting("Pads", "Root Note", arrayOf("C0", "C1", "C2", "C3", "C4"), "C1")
        padRootSetting.markInterested()
        padRootSetting.addValueObserver { note ->
            hardware.shiftPads(Integer.parseInt(note.substring(1, 2)))
        }

        val padVelocitySettings = host.preferences.getEnumSetting(
            "Pads",
            "Velocity Curve",
            arrayOf("Linear", "Logarithmic", "Exponential", "Full"),
            "Linear"
        )
        padVelocitySettings.markInterested()
        padVelocitySettings.addValueObserver { value ->
            when (value) {
                "Linear" -> hardware.setPadVelocityCurve(0)
                "Logarithmic" -> hardware.setPadVelocityCurve(1)
                "Exponential" -> hardware.setPadVelocityCurve(2)
                "Full" -> hardware.setPadVelocityCurve(3)
                else -> host.errorln("Setting 'Pads Velocity Curve' changed to invalid value '$value'")
            }
        }

        hardware.onNoteUp(Mapping.PADS_MACRO) { index ->
            // The macro should trigger with SHIFT + PAD. Unfortunately the device does not send midi-notes for the shift
            // button. Luckily, it does not send a down-note when SHIFT + PAD have been pressed. So, when an up-note was
            // triggered, but no pad has been pressed, we want to trigger the macro.
            if (!hardware.isPadPressed[index]) {
                macros[assignedMacros[index]]?.invoke()
            }
        }
    }
}