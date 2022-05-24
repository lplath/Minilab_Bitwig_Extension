package com.github.lplath

import com.bitwig.extension.callback.ShortMidiDataReceivedCallback
import com.bitwig.extension.callback.SysexMidiDataReceivedCallback
import com.bitwig.extension.controller.ControllerExtension
import com.bitwig.extension.controller.api.ControllerHost

class ArturiaMinilabExtension(private val definition: ArturiaMinilabExtensionDefinition, host: ControllerHost) : ControllerExtension(definition, host),
    ShortMidiDataReceivedCallback, SysexMidiDataReceivedCallback {

    lateinit var hardware: Hardware
    lateinit var track: TrackHandler
    lateinit var pads: PadHandler

    override fun init() {
        hardware = Hardware(host, this, this)
        track = TrackHandler()
        pads = PadHandler(host, hardware)
        host.println("Minilab Initialized")
    }

    override fun exit() {
        host.println("Minilab Exited")
    }

    override fun flush() {}

    override fun midiReceived(status: Int, data1: Int, data2: Int) {
        host.println("[MIDI] status: $status, data1: $data1, data2: $data2")

        hardware.midiReceived(status, data1, data2)
        track.midiReceived(status, data1, data2)
        pads.midiReceived(status, data1, data2)
    }

    override fun sysexDataReceived(data: String?) {
        host.println("Sysex data: $data")
    }
}