package com.github.lplath

import com.bitwig.extension.callback.ShortMidiDataReceivedCallback
import com.bitwig.extension.callback.SysexMidiDataReceivedCallback
import com.bitwig.extension.controller.api.*

class Hardware(private val host: ControllerHost) : SysexMidiDataReceivedCallback, ShortMidiDataReceivedCallback {
    companion object {
        private const val KNOB_INCREMENT = 0.1
    }

    private val portIn = host.getMidiInPort(0)
    private val portOut = host.getMidiOutPort(0)
    private val keyboard = portIn.createNoteInput(
        "MiniLab Keys",
        "80????",
        "90????",
        "B002??",
        "B007??",
        "B00B??",
        "B040??",
        "C0????",
        "D0????",
        "E0????"
    )
    private val pads = portIn.createNoteInput("MiniLab Pads", "?9????")

    private val noteUpCallbacks: HashMap<Int, () -> Unit> = HashMap()
    private val noteDownCallbacks: HashMap<Int, () -> Unit> = HashMap()
    private val valueChangedCallbacks: HashMap<Int, (Double) -> Unit> = HashMap()

    var isOctaveDownPressed = false
    var isOctaveUpPressed = false
    var isPadPressed = Array(8) { false }

    init {
        keyboard.setShouldConsumeEvents(true)
        pads.setShouldConsumeEvents(false)
        pads.assignPolyphonicAftertouchToExpression(0, NoteInput.NoteExpression.TIMBRE_UP, 2)

        portIn.setMidiCallback(this)
        portIn.setSysexCallback(this)

        Memory.writeReset(portOut)

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

        val pitchBendSettings = host.preferences.getEnumSetting("Pitch Bend", "Mode", arrayOf("Standard", "Hold"), "Standard")
        pitchBendSettings.markInterested()
        pitchBendSettings.addValueObserver { value ->
            when (value) {
                "Standard" -> setPitchBendMode(0)
                "Hold" -> setPitchBendMode(1)
            }
        }
    }

    fun onNoteUp(keys: List<Int>, callback: (Int) -> Unit) {
        for ((index, key) in keys.withIndex()) {
            noteUpCallbacks[key] = { callback(index) }
        }
    }

    fun onNoteUp(key: Int, callback: (Int) -> Unit) {
        noteUpCallbacks[key] = { callback(0) }
    }

    fun onNoteDown(keys: List<Int>, callback: (Int) -> Unit) {
        for ((index, key) in keys.withIndex()) {
            noteDownCallbacks[key] = { callback(index) }
        }
    }

    fun onNoteDown(key: Int, callback: (Int) -> Unit) {
        noteDownCallbacks[key] = { callback(0) }
    }

    fun onValueChanged(knobs: List<Int>, callback: (Int, Double) -> Unit) {
        for ((index, knob) in knobs.withIndex())
            valueChangedCallbacks[knob] = { inc -> callback(index, inc) }
    }

    fun onValueChanged(knob: Int, callback: (Int, Double) -> Unit) {
        valueChangedCallbacks[knob] = { inc -> callback(0, inc) }
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

    fun setPadVelocityCurve(value: Int) {
        portOut.sendSysex("F0 00 20 6B 7F 42 02 00 41 03 0$value F7")
    }

    fun setKeyboardVelocityCurve(value: Int) {
        portOut.sendSysex("F0 00 20 6B 7F 42 02 00 41 01 0$value F7")
    }

    fun setKnobAcceleration(value: Int) {
        //TODO: 'Slow' doesn't work
        portOut.sendSysex("F0 00 20 6B 7F 42 02 00 41 04 0$value F7")
    }

    fun setPitchBendMode(value: Int) {
        portOut.sendSysex("F0 00 20 6B 7F 42 02 00 06 41 0$value F7")
    }

    override fun sysexDataReceived(data: String?) {
        host.println("Sysex data received: $data")
    }

    override fun midiReceived(status: Int, data1: Int, data2: Int) {
        //host.println("status: $status, data1: $data1, data2: $data2")

        //TODO: Fix issue when UP + DOWN is pressed, and the second up is registered, so that the page advances
        if (Midi.isCC(status)) {
            // Octave buttons don't use Note on/off. Instead, they send velocity 0 = off, 127 = on
            if (data1 == Mapping.OCTAVE_UP) {
                if (data2 == 127) {
                    isOctaveUpPressed = true
                }
                if (data2 == 0) {
                    noteUpCallbacks[Mapping.OCTAVE_UP]?.invoke()
                    isOctaveUpPressed = false
                }
            }

            if (data1 == Mapping.OCTAVE_DOWN) {
                if (data2 == 127) {
                    isOctaveDownPressed = true
                }
                if (data2 == 0) {
                    noteUpCallbacks[Mapping.OCTAVE_DOWN]?.invoke()
                    isOctaveDownPressed = false
                }
            }

            valueChangedCallbacks[data1]?.invoke((data2 - 64) * KNOB_INCREMENT)

            // else is important, because CC messages are also note-down
        } else if (Midi.isNoteDown(status)) {
            if (data1 in Mapping.PADS_MACRO) {
                isPadPressed[Mapping.PADS_MACRO.indexOf(data1)] = true
            }

            noteDownCallbacks[data1]?.invoke()
        } else if (Midi.isNoteUp(status)) {
            noteUpCallbacks[data1]?.invoke()

            // Resetting the pad needs to be done last!
            if (data1 in Mapping.PADS_MACRO) {
                isPadPressed[Mapping.PADS_MACRO.indexOf(data1)] = false
            }
        }
    }

}