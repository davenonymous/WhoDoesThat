package com.davenonymous.whodoesthat.lib.gui.event;

public class KeyReleasedEvent implements IEvent {
	public int keyCode;
	public int scanCode;
	public int modifiers;

	public KeyReleasedEvent(int keyCode, int scanCode, int modifiers) {
		this.keyCode = keyCode;
		this.scanCode = scanCode;
		this.modifiers = modifiers;
	}
}
