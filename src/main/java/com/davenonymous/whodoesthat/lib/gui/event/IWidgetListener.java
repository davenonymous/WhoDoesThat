package com.davenonymous.whodoesthat.lib.gui.event;


import com.davenonymous.whodoesthat.lib.gui.widgets.Widget;

public interface IWidgetListener<T extends IEvent> {
	WidgetEventResult call(T event, Widget widget);
}
