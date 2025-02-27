package com.davenonymous.whodoesthat.lib.gui.event;

public class KeyPressedEvent implements IEvent {
	public int keyCode;
	public int scanCode;
	public int modifiers;

	public KeyPressedEvent(int keyCode, int scanCode, int modifiers) {
		this.keyCode = keyCode;
		this.scanCode = scanCode;
		this.modifiers = modifiers;
	}
}
