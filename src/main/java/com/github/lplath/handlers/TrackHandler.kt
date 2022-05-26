package com.github.lplath.handlers

import com.bitwig.extension.controller.api.ControllerHost
import com.github.lplath.Hardware
import com.github.lplath.Mapping

class TrackHandler(
    private val host: ControllerHost,
    private val hardware: Hardware
) {
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

        val knobAccelerationSettings = host.preferences.getEnumSetting(
            "Knobs",
            "Acceleration",
            arrayOf("Slow (Off)", "Medium", "Fast"),
            "Slow (Off)"
        )
        knobAccelerationSettings.markInterested()
        knobAccelerationSettings.addValueObserver { value ->
            when (value) {
                "Slow (Off)" -> hardware.setKnobAcceleration(0)
                "Medium" -> hardware.setKnobAcceleration(1)
                "Fast" -> hardware.setKnobAcceleration(2)
                else -> host.errorln("Setting 'Knobs Acceleration' changed to invalid value '$value'")
            }
        }

        hardware.onValueChanged(Mapping.KNOBS_REMOTE) { index, increment ->
            remote.getParameter(index).inc(increment, 128)
        }

        hardware.onValueChanged(Mapping.KNOBS_SEND) { index, increment ->
            sends.getItemAt(index).value().inc(increment, 128)
        }

        hardware.onValueChanged(Mapping.KNOB_VOLUME) { _, increment ->
            cursor.volume().value().inc(increment, 128)
        }

        hardware.onValueChanged(Mapping.KNOB_PAN) { _, increment ->
            cursor.pan().value().inc(increment, 128)
        }

        /*
         *  Up + Down   -> Reset page to 0,
         *  Down        -> Prev page
         */
        hardware.onNoteUp(Mapping.OCTAVE_DOWN) {
            if (hardware.isOctaveUpPressed) {
                remote.selectedPageIndex().set(0)
            } else {
                remote.selectPreviousPage(false)
            }
        }

        /*
         *  Up + Down   -> Reset page to 0,
         *  Up          -> Next page
         */
        hardware.onNoteUp(Mapping.OCTAVE_UP) {
            if (hardware.isOctaveDownPressed) {
                remote.selectedPageIndex().set(0)
            } else {
                remote.selectNextPage(false)
            }
        }
    }
}