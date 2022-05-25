package com.github.lplath

import com.bitwig.extension.controller.api.ControllerHost



class TrackHandler(private val host: ControllerHost, mapping: Mapping) {

	private val cursor = host.createCursorTrack(4, 0)
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

		mapping.onValueChanged(Mapping.KNOBS_REMOTE) { index, increment ->
			remote.getParameter(index).inc(increment, 128)
		}

		mapping.onValueChanged(Mapping.KNOBS_SEND) { index, increment ->
			sends.getItemAt(index).value().inc(increment, 128)
		}

		mapping.onValueChanged(Mapping.KNOB_VOLUME) { _, increment ->
			cursor.volume().value().inc(increment, 128)
		}

		mapping.onValueChanged(Mapping.KNOB_PAN) { _, increment ->
			cursor.pan().value().inc(increment, 128)
		}
	}
}