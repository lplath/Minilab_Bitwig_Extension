package com.github.lplath

import com.bitwig.extension.api.PlatformType
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList
import com.bitwig.extension.controller.ControllerExtensionDefinition
import com.bitwig.extension.controller.api.ControllerHost
import java.util.*

class ArturiaMinilabExtensionDefinition : ControllerExtensionDefinition() {
	override fun getName() = "Minilab"
	override fun getAuthor() = "lplath"
	override fun getVersion() = "1.0"
	override fun getId(): UUID = UUID.fromString("ccac789e-bcef-4f62-b541-89b20c0f0c62")
	override fun getHardwareVendor() = "Arturia"
	override fun getHardwareModel() = "Minilab"
	override fun getRequiredAPIVersion() = 10
	override fun getNumMidiInPorts() = 1
	override fun getNumMidiOutPorts() = 1
	override fun listAutoDetectionMidiPortNames(list: AutoDetectionMidiPortNamesList, platformType: PlatformType) {}
	override fun createInstance(host: ControllerHost): ArturiaMinilabExtension = ArturiaMinilabExtension(this, host)
}