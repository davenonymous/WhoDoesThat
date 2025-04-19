package com.davenonymous.whodoesthat.gui.overview.detail.files;

import com.davenonymous.whodoesthat.lib.gui.event.IEvent;
import com.davenonymous.whodoesthat.lib.gui.widgets.Widget;

public record FileSelectedEvent(int selectedEntry, Widget selectedWidget) implements IEvent {
}
