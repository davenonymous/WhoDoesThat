package com.davenonymous.whodoesthat.gui.menu;

import com.davenonymous.whodoesthat.lib.gui.event.IEvent;
import com.davenonymous.whodoesthatlib.api.IProgressTracker;

public record ProgressEvent(IProgressTracker progress, String event) implements IEvent {
}
