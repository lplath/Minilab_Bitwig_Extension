package com.github.lplath

import com.bitwig.extension.callback.ShortMidiDataReceivedCallback
import com.bitwig.extension.controller.api.ControllerHost

const val KNOB_INCREMENT = 0.1

class Mapping(private val host: ControllerHost) : ShortMidiDataReceivedCallback {

	companion object {
		val OCTAVE_DOWN = listOf(20)
		val OCTAVE_UP = listOf(21)
		val PADS_1 = listOf(36, 37, 38, 39, 40, 41, 42, 43)
		val PADS_2 = listOf(44, 45, 46, 47, 48, 49, 50, 51)
		val KNOBS_SEND = listOf(106, 107, 108, 109)
		val KNOBS_REMOTE = listOf(102, 103, 104, 105, 110, 111, 112, 113)
		val KNOB_PAN = listOf(116)
		val KNOB_VOLUME = listOf(117)
	}

	private val noteUpCallbacks: HashMap<Int, () -> Unit> = HashMap()
	private val noteDownCallbacks: HashMap<Int, () -> Unit> = HashMap()
	private val valueChangedCallbacks: HashMap<Int, (Double) -> Unit> = HashMap()

	fun onUp(keys: List<Int>, callback: (Int) -> Unit) {
		for ((index, key) in keys.withIndex()) {
			noteUpCallbacks[key] = { callback(index) }
		}
	}

	fun onDown(keys: List<Int>, callback: (Int) -> Unit) {
		for ((index, key) in keys.withIndex()) {
			noteDownCallbacks[key] = { callback(index) }
		}
	}

	fun onValueChanged(knobs: List<Int>, callback: (Int, Double) -> Unit) {
		for ((index, knob) in knobs.withIndex())
			valueChangedCallbacks[knob] = { inc -> callback(index, inc) }
	}

	override fun midiReceived(status: Int, data1: Int, data2: Int) {
		if (Midi.isCC(status)) {
			valueChangedCallbacks[data1]?.invoke((data2 - 64) * KNOB_INCREMENT)
		} else if (Midi.isNoteDown(status)) {
			noteDownCallbacks[data1]?.invoke()

		} else if (Midi.isNoteUp(status)) {
			noteUpCallbacks[data1]?.invoke()
		}
	}
}