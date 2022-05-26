package com.github.lplath.handlers

import com.bitwig.extension.controller.api.ControllerHost
import com.github.lplath.Hardware
import com.github.lplath.Mapping

class ClipHandler(private val host: ControllerHost, hardware: Hardware) {

	private val trackBank = host.createTrackBank(6, 0, 0)

	init {
		hardware.onNoteUp(Mapping.PADS_CLIPS) { index ->
			if (!hardware.isPadPressed[index]) {
				host.println("Clip $index launched")
				selectTrack(index)
			}
		}

		hardware.onNoteUp(Mapping.PAD_CLIP_UP) {
			trackBank.scrollPageBackwards()
			selectTrack(0)
		}

		hardware.onNoteUp(Mapping.PAD_CLIP_DOWN) {
			trackBank.scrollPageForwards()
			selectTrack(0)
		}
	}

	private fun selectTrack(index: Int) {
		val track = trackBank.getItemAt(index)
		track.selectInMixer()
		track.selectInEditor()
		track.makeVisibleInArranger()
		track.makeVisibleInMixer()
	}
}