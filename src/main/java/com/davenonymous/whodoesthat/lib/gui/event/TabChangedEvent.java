package com.davenonymous.whodoesthat.lib.gui.event;

import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetPanel;

public class TabChangedEvent extends ValueChangedEvent<WidgetPanel> {
	public TabChangedEvent(WidgetPanel oldValue, WidgetPanel newValue) {
		super(oldValue, newValue);
	}
}
