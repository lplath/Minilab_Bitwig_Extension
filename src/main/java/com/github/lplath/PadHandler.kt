package com.github.lplath

import com.bitwig.extension.callback.ShortMidiDataReceivedCallback
import com.bitwig.extension.controller.api.ControllerHost
import com.bitwig.extension.controller.api.Transport

const val MIDI_START = 36
const val MIDI_END = 43

class PadHandler(private val host: ControllerHost, private val hardware: Hardware) : ShortMidiDataReceivedCallback {

	private val transport: Transport = host.createTransport()
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

	private val assignedMacros = MutableList(8) { "Unassigned" }
	private val isPressed = MutableList(8) { false }

	init {
		val macroNames = macros.keys.toTypedArray()
		for (i in 0..7) {
			val macroSetting = host.preferences.getEnumSetting("Pad ${(i + 1)}", "Macros", macroNames , "Unassigned")
			macroSetting.markInterested()
			macroSetting.addValueObserver { macro ->
				assignedMacros[i] = macro
			}
		}

		val padRootSetting = host.preferences.getEnumSetting("Pads", "Root Note", arrayOf("C0", "C1", "C2", "C3", "C4"), "C1")
		padRootSetting.markInterested()
		padRootSetting.addValueObserver { note ->
			hardware.shiftPads(Integer.parseInt(note.substring(1,2)))
		}
	}

	override fun midiReceived(status: Int, data1: Int, data2: Int) {
		if (data1 in MIDI_START..MIDI_END) {
			val index = data1 - MIDI_START

			if (status == 153) {
				isPressed[index] = true
			}
			if (status == 137) {
				macros[assignedMacros[index]]?.let { it() }
				isPressed[index] = false
			}
		}
	}
}