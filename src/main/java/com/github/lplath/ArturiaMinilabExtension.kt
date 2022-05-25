package com.github.lplath

import com.bitwig.extension.callback.ShortMidiDataReceivedCallback
import com.bitwig.extension.callback.SysexMidiDataReceivedCallback
import com.bitwig.extension.controller.ControllerExtension
import com.bitwig.extension.controller.api.ControllerHost

class ArturiaMinilabExtension(private val definition: ArturiaMinilabExtensionDefinition, host: ControllerHost) :
	ControllerExtension(definition, host) {

	override fun init() {
		val mapping = Mapping(host)
		val hardware = Hardware(host, mapping)
        val track = TrackHandler(host, mapping)
		val pads = PadHandler(host, hardware, mapping)

        host.println("Minilab Initialized")
	}
	override fun exit() {
		host.println("Minilab Exited")
	}

	override fun flush() {}

}