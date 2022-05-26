package com.github.lplath

import com.bitwig.extension.controller.ControllerExtension
import com.bitwig.extension.controller.api.ControllerHost
import com.github.lplath.handlers.ClipHandler
import com.github.lplath.handlers.PadHandler
import com.github.lplath.handlers.TrackHandler

class ArturiaMinilabExtension(definition: ArturiaMinilabExtensionDefinition, host: ControllerHost) :
    ControllerExtension(definition, host) {

    override fun init() {
        val hardware = Hardware(host)
        TrackHandler(host, hardware)
        PadHandler(host, hardware)
        ClipHandler(host, hardware)

        host.println("Minilab Initialized")
    }
    override fun exit() {
        host.println("Minilab Exited")
    }

    override fun flush() {}

}