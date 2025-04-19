package com.davenonymous.whodoesthat.lib.gui.event;

import com.davenonymous.whodoesthat.lib.gui.widgets.Widget;

public record WidgetSizeChangeEvent(int oldWidth, int oldHeight, int newWidth, int newHeight, Widget changedWidget) implements IEvent {
	public boolean xChanged() {
		return oldWidth != newWidth;
	}

	public boolean yChanged() {
		return oldHeight != newHeight;
	}
}
