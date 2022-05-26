package com.github.lplath

/**
 * https://www.midi.org/specifications-old/item/table-2-expanded-messages-list-status-bytes
 */
object Midi {
	fun isCC(status: Int): Boolean = status and 0xB0 == 0xB0
	fun isNoteUp(status: Int): Boolean = status and 0x80 == 0x80
	fun isNoteDown(status: Int): Boolean = status and 0x90 == 0x90
}