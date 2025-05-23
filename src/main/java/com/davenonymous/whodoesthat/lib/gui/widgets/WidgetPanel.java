package com.davenonymous.whodoesthat.lib.gui.widgets;


import com.davenonymous.whodoesthat.lib.gui.event.*;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class WidgetPanel extends Widget {
	List<Widget> children;
	List<Widget> previouslyHovered;

	public WidgetPanel() {
		this.children = new LinkedList<>();
		this.previouslyHovered = new ArrayList<>();

		// Pass mouse move events along to the children, shift positions accordingly
		// Also notify widgets when the mouse entered or exited their area
		this.addListener(
			MouseMoveEvent.class, (event, widget) -> {

				//Logz.info("[%s] Moved mouse to x=%d, y=%d", widget.id, innerX, innerY);
				for(Widget child : children) {
					MouseMoveEvent shifted = new MouseMoveEvent(event.x, event.y);
					child.fireEvent(shifted);

					if(!child.isPosInside(event.x, event.y)) {
						if(previouslyHovered.contains(child)) {
							child.fireEvent(new MouseExitEvent());
							previouslyHovered.remove(child);
						}
						continue;
					}

					if(!previouslyHovered.contains(child)) {
						child.fireEvent(new MouseEnterEvent());
						previouslyHovered.add(child);
					}
				}

				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		this.addListener(
			VisibilityChangedEvent.class, (event, widget1) -> {
				if(Minecraft.getInstance().player == null) {
					return WidgetEventResult.CONTINUE_PROCESSING;
				}

				if(this.getGUI() == null) {
					return WidgetEventResult.CONTINUE_PROCESSING;
				}

				if(this.getGUI().getContainer() == null) {
					return WidgetEventResult.CONTINUE_PROCESSING;
				}

				// TODO: Reimplement sending of enabled slots
				// Networking.sendEnabledSlotsMessage(this.getGUI().getContainer().slots);
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		// Pass click events along to the children, shift click position accordingly
		this.addListener(
			MouseClickEvent.class, ((event, widget) -> {
				double innerX = event.x - widget.getActualX();
				double innerY = event.y - widget.getActualY();
				// GUI.LOGGER.info("{}: Click: screen@{},{}, panel@{},{}", this.toString(), event.x, event.y, innerX, innerY);

				for(Widget child : children) {
					if(!child.visible) {
						continue;
					}

					// GUI.LOGGER.info("Checking child: {} @ {},{}", child.toString(), innerX, innerY);
					if(!child.isPosInside(event.x, event.y)) {
						continue;
					}

					// GUI.LOGGER.info("Passing along to child={} with {},{}", child.toString(), innerX, innerY);
					if(child.fireEvent(new MouseClickEvent(event.x, event.y, event.button)) == WidgetEventResult.HANDLED) {
						return WidgetEventResult.HANDLED;
					}
				}

				return WidgetEventResult.CONTINUE_PROCESSING;
			})
		);

		this.addListener(
			MouseDraggedEvent.class, ((event, widget) -> {

				for(Widget child : children) {
					if(!child.visible) {
						continue;
					}

					if(!child.isPosInside(event.mouseX(), event.mouseY())) {
						continue;
					}

					// GUI.LOGGER.info("Passing along to child={} with {},{}", child.toString(), innerX, innerY);
					if(child.fireEvent(new MouseDraggedEvent(event.mouseX(), event.mouseY(), event.button(), event.dragX(), event.dragY())) == WidgetEventResult.HANDLED) {
						return WidgetEventResult.HANDLED;
					}
				}

				return WidgetEventResult.CONTINUE_PROCESSING;
			})
		);

		Set<Class<? extends IEvent>> eventsToIgnore = Sets.newHashSet(
			MouseClickEvent.class, MouseMoveEvent.class, MouseEnterEvent.class, MouseExitEvent.class, ValueChangedEvent.class, MouseDraggedEvent.class);

		// Forward all other events to all children directly
		this.addAnyListener(((event, widget) -> {
			if(eventsToIgnore.contains(event.getClass())) {
				return WidgetEventResult.CONTINUE_PROCESSING;
			}

			for(Widget child : children) {
				WidgetEventResult immediateResult = child.fireEvent(event);
				if(immediateResult == WidgetEventResult.HANDLED) {
					return WidgetEventResult.HANDLED;
				}
			}

			return WidgetEventResult.CONTINUE_PROCESSING;
		}));

	}

	public WidgetPanel forAllChildren(Consumer<Widget> consumer) {
		for(Widget child : children) {
			consumer.accept(child);
		}
		return this;
	}

	public List<Widget> children() {
		return children;
	}

	@Override
	public boolean focusable() {
		return false;
	}

	public void clear() {
		this.children.clear();
	}

	public void add(Widget widget) {
		children.add(widget);
		widget.setParent(this);
	}

	public void remove(Widget widget) {
		children.remove(widget);
	}

	public List<Widget> getHoveredWidgets() {
		List<Widget> result = new ArrayList<>();
		this.getHoveredWidgets(result);
		return result;
	}

	private void getHoveredWidgets(List<Widget> result) {
		for(Widget widget : previouslyHovered) {
			if(widget instanceof WidgetPanel) {
				((WidgetPanel) widget).getHoveredWidgets(result);
			} else {
				result.add(widget);
			}
		}
	}

	public Widget getHoveredWidget(int mouseX, int mouseY) {
		for(Widget child : children) {
			if(!child.visible) {
				continue;
			}

			if(child.isPosInside(mouseX, mouseY)) {
				Widget maybeResult = null;
				if(child instanceof WidgetPanel) {
					maybeResult = ((WidgetPanel) child).getHoveredWidget(mouseX, mouseY);
				}

				if(maybeResult != null && maybeResult.hasToolTip()) {
					return maybeResult;
				}

				return child;
			}
		}

		return null;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, Screen screen) {
		for(Widget child : children) {
			if(!child.visible) {
				continue;
			}

			child.shiftAndDraw(guiGraphics, screen);
		}
	}

	protected void findValueWidgets(WidgetPanel panel) {
		for(Widget child : panel.children) {
			if(child instanceof WidgetPanel && !(child instanceof IValueProvider)) {
				findValueWidgets((WidgetPanel) child);
			}

			if(child instanceof IValueProvider provider) {
				if(provider.getId() != null) {
					this.getGUI().registerValueWidget(provider.getId(), provider);
				}
			}
		}
	}

	public void adjustSizeToContent() {
		int smallestY = Integer.MAX_VALUE;
		int smallestX = Integer.MAX_VALUE;
		int largestY = Integer.MIN_VALUE;
		int largestX = Integer.MIN_VALUE;

		for(Widget child : this.children) {
			smallestX = Math.min(child.x, smallestX);
			smallestY = Math.min(child.y, smallestY);
			largestX = Math.max(child.x + child.width, largestX);
			largestY = Math.max(child.y + child.height, largestY);
		}

		largestX -= smallestX;
		largestY -= smallestY;

		for(Widget child : this.children) {
			child.setX(child.x - smallestX);    // Shift all children to 0
			child.setY(child.y - smallestY);    // Shift all children to 0
		}
		this.setSize(largestX, largestY);
	}
}
