package com.github.lplath

object Mapping {
	const val OCTAVE_DOWN = 20
	const val OCTAVE_UP = 21
	const val PAD_MACRO_START = 36
	const val PAD_MACRO_END = 43
	const val KNOBS_REMOTE_UPPER_START = 102
	const val KNOBS_REMOTE_UPPER_END = 105
	const val KNOBS_REMOTE_LOWER_START = 110
	const val KNOBS_REMOTE_LOWER_END = 113
	const val KNOB_SELECT_TRACK = 106
	const val KNOB_SELECT_DEVICE = 114
	const val KNOBS_SEND_UPPER_START = 107
	const val KNOBS_SEND_UPPER_END = 108
	const val KNOBS_SEND_LOWER_START = 115
	const val KNOBS_SEND_LOWER_END = 116
	const val KNOB_PAN = 117
	const val KNOB_VOLUME = 109

	fun getKnobRemoteIndex(note: Int): Int = note - when(note) {
		in KNOBS_REMOTE_UPPER_START..KNOBS_REMOTE_UPPER_END -> KNOBS_REMOTE_UPPER_START
		in KNOBS_REMOTE_LOWER_START..KNOBS_REMOTE_LOWER_END -> KNOBS_REMOTE_LOWER_START - 4
		else -> throw IndexOutOfBoundsException("Cannot get index of $note. Index is not in range.")
	}

	fun getKnobSendIndex(note: Int): Int = note - when(note) {
		in KNOBS_SEND_UPPER_START..KNOBS_SEND_UPPER_END -> KNOBS_SEND_UPPER_START
		in KNOBS_SEND_LOWER_START..KNOBS_SEND_LOWER_END -> KNOBS_SEND_LOWER_START - 2
		else -> throw IndexOutOfBoundsException("Cannot get index of $note. Index is not in range.")
	}
}