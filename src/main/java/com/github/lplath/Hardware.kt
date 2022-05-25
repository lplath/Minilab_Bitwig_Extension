package com.github.lplath

import com.bitwig.extension.callback.SysexMidiDataReceivedCallback
import com.bitwig.extension.controller.api.*

/**
 * @see https://www.midi.org/specifications-old/item/table-2-expanded-messages-list-status-bytes
 */
object Midi {
	fun isCC(status: Int): Boolean = status and 0xB0 == 0xB0
	fun isNoteUp(status: Int): Boolean = status and 0x80 == 0x80
	fun isNoteDown(status: Int): Boolean = status and 0x90 == 0x90
}

class Hardware(
	private val host: ControllerHost,
	mapping: Mapping,
) : SysexMidiDataReceivedCallback {

	private val portIn = host.getMidiInPort(0)
	private val portOut = host.getMidiOutPort(0)
	private val keyboard = portIn.createNoteInput(
		"MiniLab Keys",
		"80????",   // Ch. 1 Note off
		"90????",   // Ch. 1 Note on
		"B001??",   // CC 1 (Mod Wheel)
		"B002??",   // CC 1 (Breath Controller)
		"B007??",   // CC 1 (Channel Volume)
		"B00B??",   // CC 1 (Expression Controller)
		"B040??",   // CC 1 (Sustain Pedal)
		"C0????",   // Ch. 1 Prog. Change
		"D0????",   // Ch. 1 Aftertouch
		"E0????"
	)
	private val pads = portIn.createNoteInput("MiniLab Pads", "?9????")

	var isOctaveDownPressed = false
	var isOctaveUpPressed = false

	init {
		keyboard.setShouldConsumeEvents(true)
		pads.setShouldConsumeEvents(false)
		pads.assignPolyphonicAftertouchToExpression(0, NoteInput.NoteExpression.TIMBRE_UP, 2)

		// Arturia ID: 00 20 6B
		//host.defineSysexIdentityReply("F0 7E ?? 06 02 00 20 6B 02 00 04 0? ?? ?? ?? ?? F7");


		portIn.setMidiCallback(mapping)
		portIn.setSysexCallback(this)

		portOut.sendSysex(Memory.DeviceMemory)

		val padVelocitySettings = host.preferences.getEnumSetting(
			"Pads",
			"Velocity Curve",
			arrayOf("Linear", "Logarithmic", "Exponential", "Full"),
			"Linear"
		)
		padVelocitySettings.markInterested()
		padVelocitySettings.addValueObserver { value ->
			when (value) {
				"Linear" -> setPadVelocityCurve(0)
				"Logarithmic" -> setPadVelocityCurve(1)
				"Exponential" -> setPadVelocityCurve(2)
				"Full" -> setPadVelocityCurve(3)
				else -> host.errorln("Setting 'Pads Velocity Curve' changed to invalid value '$value'")
			}
		}

		val keyVelocitySettings = host.preferences.getEnumSetting(
			"Keyboard",
			"Velocity Curve",
			arrayOf("Linear", "Logarithmic", "Exponential", "Full"),
			"Linear"
		)
		keyVelocitySettings.markInterested()
		keyVelocitySettings.addValueObserver { value ->
			when (value) {
				"Linear" -> setKeyboardVelocityCurve(0)
				"Logarithmic" -> setKeyboardVelocityCurve(1)
				"Exponential" -> setKeyboardVelocityCurve(2)
				"Full" -> setKeyboardVelocityCurve(3)
				else -> host.errorln("Setting 'Keyboard Velocity Curve' changed to invalid value '$value'")
			}
		}

		val knobAccelerationSettings = host.preferences.getEnumSetting(
			"Knobs",
			"Acceleration",
			arrayOf("Slow (Off)", "Medium", "Fast"),
			"Slow (Off)"
		)
		knobAccelerationSettings.markInterested()
		knobAccelerationSettings.addValueObserver { value ->
			when (value) {
				"Slow (Off)" -> setKnobAcceleration(0)
				"Medium" -> setKnobAcceleration(1)
				"Fast" -> setKnobAcceleration(2)
				else -> host.errorln("Setting 'Knobs Acceleration' changed to invalid value '$value'")
			}
		}

		mapping.onDown(Mapping.OCTAVE_UP) {
			isOctaveUpPressed = true
		}

		mapping.onUp(Mapping.OCTAVE_UP) {
			isOctaveUpPressed = false
		}

		mapping.onDown(Mapping.OCTAVE_DOWN) {
			isOctaveDownPressed = true
		}

		mapping.onUp(Mapping.OCTAVE_DOWN) {
			isOctaveDownPressed = false
		}
	}

	fun shiftPads(rootNote: Int) {
		val table = Array(128) { 0 }
		for (i in table.indices) {
			table[i] = ((rootNote - 1) * 12) + i
			if (table[i] < 0 || table[i] > 127)
				table[i] = -1
		}
		pads.setKeyTranslationTable(table)
	}

	private fun setPadVelocityCurve(value: Int) {
		portOut.sendSysex("F0 00 20 6B 7F 42 02 00 41 03 0$value F7")
	}

	private fun setKeyboardVelocityCurve(value: Int) {
		portOut.sendSysex("F0 00 20 6B 7F 42 02 00 41 01 0$value F7")
	}

	private fun setKnobAcceleration(value: Int) {
		portOut.sendSysex("F0 00 20 6B 7F 42 02 00 41 04 0$value F7")
	}

	override fun sysexDataReceived(data: String?) {
		host.println("Sysex data received: $data")
	}
}