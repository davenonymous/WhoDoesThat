package com.davenonymous.whodoesthat.lib.gui.event;

public record MouseDraggedEvent(double mouseX, double mouseY, int button, double dragX, double dragY) implements IEvent {
}
