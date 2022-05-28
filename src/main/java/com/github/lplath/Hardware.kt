package com.github.lplath

import com.bitwig.extension.callback.*
import com.bitwig.extension.controller.api.*

class Hardware(private val host: ControllerHost) : SysexMidiDataReceivedCallback, ShortMidiDataReceivedCallback {
	companion object {
		private const val KNOB_INCREMENT = 0.1
	}

	private val portIn = host.getMidiInPort(0)
	private val portOut = host.getMidiOutPort(0)
	private val keyboard = portIn.createNoteInput(
		"Keys", "80????", "90????", "B002??", "B007??", "B00B??", "B040??", "C0????", "D0????", "E0????"
	)
	private val pads = portIn.createNoteInput("Pads", "?9????")

	private val handlers: MutableList<MidiHandler> = mutableListOf()

	var isOctaveDownPressed = false
	var isOctaveUpPressed = false
	var isPadPressed = Array(8) { false }

	init {
		keyboard.setShouldConsumeEvents(true)
		pads.setShouldConsumeEvents(false)
		pads.assignPolyphonicAftertouchToExpression(0, NoteInput.NoteExpression.TIMBRE_UP, 2)
		portIn.setMidiCallback(this)
		portIn.setSysexCallback(this)

	}

	fun shiftPads(rootNote: Int) {
		val table = Array(128) { 0 }
		for (i in table.indices) {
			table[i] = ((rootNote - 1) * 12) + i
			if (table[i] < 0 || table[i] > 127) table[i] = -1
		}
		pads.setKeyTranslationTable(table)
	}

	fun addListener(handler: MidiHandler) {
		handlers.add(handler)
	}


	override fun midiReceived(status: Int, data1: Int, data2: Int) {
		if (Midi.isCC(status)) {
			// Octave buttons don't use Note on/off. Instead, they send CC with velocity 0 = off, 127 = on
			when (data1) {
				Mapping.OCTAVE_UP -> {
					when (data2) {
						0 -> {
							isOctaveUpPressed = false
							if (handlers.any { handler -> handler.onNoteUp(Mapping.OCTAVE_UP, 127) })
								return
						}
						127 -> {
							isOctaveUpPressed = true
							if (handlers.any { handler -> handler.onNoteDown(Mapping.OCTAVE_UP, 0) })
								return
						}
					}
				}
				Mapping.OCTAVE_DOWN -> {
					when (data2) {
						0 -> {
							isOctaveDownPressed = false
							if (handlers.any { handler -> handler.onNoteUp(Mapping.OCTAVE_DOWN, 127) })
								return
						}
						127 -> {
							isOctaveDownPressed = true
							if (handlers.any { handler -> handler.onNoteDown(Mapping.OCTAVE_DOWN, 0) })
								return
						}
					}
				}
			}

			// CC value changed
			if (handlers.any { handler -> handler.onValueChanged(data1, (data2 - 64) * KNOB_INCREMENT) })
				return

			// else is important, because CC messages are also note-down events
		} else if (Midi.isNoteDown(status)) {
			if (data1 in Mapping.PAD_MACRO_START..Mapping.PAD_MACRO_END)
				isPadPressed[data1 - Mapping.PAD_MACRO_START] = true

			if (handlers.any { handler -> handler.onNoteDown(data1, data2) })
				return

		} else if (Midi.isNoteUp(status)) {
			val handled = handlers.any { handler -> handler.onNoteUp(data1, data2) }

			if (data1 in Mapping.PAD_MACRO_START..Mapping.PAD_MACRO_END)
				isPadPressed[data1 - Mapping.PAD_MACRO_START] = false

			if (handled) return
		}

		//host.println("[INFO] Unhandled midi event: (status: $status, data1: $data1, data2: $data2)")
	}


	override fun sysexDataReceived(data: String?) {
		if (data != null)
			host.println("[INFO] Sysex data received: $data")
	}

	fun writeMemory() {
		host.println("Sending device memory..")
		portOut.sendSysex(Memory.DeviceMemory)
		host.println("Done")
	}

	fun setPadVelocityCurve(value: Int) = portOut.sendSysex("F000206B7F42020041030${value}F7")

	fun setKeyboardVelocityCurve(value: Int) = portOut.sendSysex("F000206B7F42020041010${value}F7")

	fun setKnobAcceleration(value: Int) = portOut.sendSysex("F000206B7F42020041040${value}F7")

	fun setPitchBendMode(value: Int) = portOut.sendSysex("F000206B7F42020006410${value}F7")

}
