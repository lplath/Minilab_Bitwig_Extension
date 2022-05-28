package com.github.lplath.handlers

import com.bitwig.extension.controller.api.ControllerHost
import com.github.lplath.Hardware
import com.github.lplath.Mapping
import com.github.lplath.MidiHandler

class TrackHandler(private val host: ControllerHost, private val hardware: Hardware) : MidiHandler() {
	private val cursor = host.createCursorTrack(6, 0)
	private val device = cursor.createCursorDevice()
	private val remote = device.createCursorRemoteControlsPage(8)
	private val sends = device.channel().sendBank()

	init {
		for (i in 0..7) {
			remote.getParameter(i).apply {
				setIndication(true)
				setLabel("P ${i + 1}")
			}
		}
	}

	override fun onNoteUp(note: Int, velocity: Int): Boolean {
		when (note) {
			Mapping.OCTAVE_DOWN ->
				if (hardware.isOctaveUpPressed) remote.selectedPageIndex().set(0)
				else remote.selectPreviousPage(false)
			Mapping.OCTAVE_UP ->
				if (hardware.isOctaveDownPressed) remote.selectedPageIndex().set(0)
				else remote.selectNextPage(false)
			else -> return false
		}
		return true
	}

	override fun onValueChanged(note: Int, increment: Double): Boolean {
		when (note) {
			in Mapping.KNOBS_REMOTE_UPPER_START..Mapping.KNOBS_REMOTE_UPPER_END, in Mapping.KNOBS_REMOTE_LOWER_START..Mapping.KNOBS_REMOTE_LOWER_END -> {
				remote.getParameter(Mapping.getKnobRemoteIndex(note)).inc(increment, 128)
			}
			in Mapping.KNOBS_SEND_UPPER_START..Mapping.KNOBS_SEND_UPPER_END, in Mapping.KNOBS_SEND_LOWER_START..Mapping.KNOBS_SEND_LOWER_END -> {
				sends.getItemAt(Mapping.getKnobSendIndex(note)).value().inc(increment, 128)
			}
			Mapping.KNOB_VOLUME -> cursor.volume().value().inc(increment, 128)
			Mapping.KNOB_PAN -> cursor.pan().value().inc(increment, 128)
			else -> return false
		}
		return true
	}
}