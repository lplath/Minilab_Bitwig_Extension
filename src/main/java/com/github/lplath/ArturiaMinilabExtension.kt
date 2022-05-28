package com.github.lplath

import com.bitwig.extension.controller.ControllerExtension
import com.bitwig.extension.controller.api.ControllerHost
import com.github.lplath.handlers.*

class ArturiaMinilabExtension(definition: ArturiaMinilabExtensionDefinition, host: ControllerHost) :
	ControllerExtension(definition, host) {

	override fun init() {
		val hardware = Hardware(host)
		hardware.addListener(TrackHandler(host, hardware))
		hardware.addListener(PadHandler(host, hardware))

		Settings(host, hardware)

		host.println("Minilab Initialized")
	}

	override fun exit() {
		host.println("Minilab Exited")
	}

	override fun flush() {}

}