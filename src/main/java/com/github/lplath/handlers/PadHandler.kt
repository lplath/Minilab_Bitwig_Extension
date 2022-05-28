package com.github.lplath.handlers

import com.bitwig.extension.controller.api.*
import com.github.lplath.Hardware
import com.github.lplath.Mapping
import com.github.lplath.MidiHandler

class PadHandler(private val host: ControllerHost, private val hardware: Hardware): MidiHandler() {
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
	}

	override fun onNoteUp(note: Int, velocity: Int): Boolean {
		if (note in Mapping.PAD_MACRO_START..Mapping.PAD_MACRO_END) {
			val index = note - Mapping.PAD_MACRO_START
			if (!hardware.isPadPressed[index]) {
				macros[assignedMacros[index]]?.invoke()
				return true
			}
		}
		return false
	}
}