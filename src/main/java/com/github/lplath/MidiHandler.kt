package com.github.lplath

abstract class MidiHandler {
	open fun onNoteUp(note: Int, velocity: Int): Boolean = false
	open fun onNoteDown(note: Int, velocity: Int): Boolean = false
	open fun onValueChanged(note: Int, increment: Double): Boolean = false
}