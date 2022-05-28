package com.github.lplath

import com.bitwig.extension.controller.api.ControllerHost

class Settings(host: ControllerHost, hardware: Hardware) {
	init {
		// Keyboard responsiveness
		with(
			host.preferences.getEnumSetting(
				"Keyboard",
				"Velocity Curve",
				arrayOf("Linear", "Logarithmic", "Exponential", "Full"),
				"Linear"
			)
		) {
			markInterested()
			addValueObserver { value ->
				when (value) {
					"Linear" -> hardware.setKeyboardVelocityCurve(0)
					"Logarithmic" -> hardware.setKeyboardVelocityCurve(1)
					"Exponential" -> hardware.setKeyboardVelocityCurve(2)
					"Full" -> hardware.setKeyboardVelocityCurve(3)
					else -> host.errorln("Setting 'Keyboard Velocity Curve' changed to invalid value '$value'")
				}
			}
		}

		// Pad responsiveness
		with(
			host.preferences.getEnumSetting(
				"Pads",
				"Velocity Curve",
				arrayOf("Linear", "Logarithmic", "Exponential", "Full"),
				"Linear"
			)
		) {
			markInterested()
			addValueObserver { value ->
				when (value) {
					"Linear" -> hardware.setPadVelocityCurve(0)
					"Logarithmic" -> hardware.setPadVelocityCurve(1)
					"Exponential" -> hardware.setPadVelocityCurve(2)
					"Full" -> hardware.setPadVelocityCurve(3)
					else -> host.errorln("Setting 'Pads Velocity Curve' changed to invalid value '$value'")
				}
			}
		}

		// Knob acceleration
		with(
			host.preferences.getEnumSetting(
				"Knobs",
				"Acceleration",
				arrayOf("Slow (Off)", "Medium", "Fast"),
				"Slow (Off)"
			)
		) {
			markInterested()
			addValueObserver { value ->
				when (value) {
					"Slow (Off)" -> hardware.setKnobAcceleration(0)
					"Medium" -> hardware.setKnobAcceleration(1)
					"Fast" -> hardware.setKnobAcceleration(2)
					else -> host.errorln("Setting 'Knobs Acceleration' changed to invalid value '$value'")
				}
			}
		}

		// Pad root note
		with(host.preferences.getEnumSetting("Pads", "Root Note", arrayOf("C0", "C1", "C2", "C3", "C4"), "C1")) {
			markInterested()
			addValueObserver { note ->
				hardware.shiftPads(Integer.parseInt(note.substring(1, 2)))
			}
		}

		// Pitch Bend mode
		with(host.preferences.getEnumSetting("Pitch Bend", "Mode", arrayOf("Standard", "Hold"), "Standard")) {
			markInterested()
			addValueObserver { value ->
				when (value) {
					"Standard" -> hardware.setPitchBendMode(0)
					"Hold" -> hardware.setPitchBendMode(1)
				}
			}

		}

		// Reset device memory
		with(host.preferences.getSignalSetting("Memory", "Device", "Reset")) {
			addSignalObserver() {
				hardware.writeMemory()
			}
		}
	}
}