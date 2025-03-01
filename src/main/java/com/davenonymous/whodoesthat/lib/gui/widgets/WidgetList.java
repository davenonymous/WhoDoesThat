package com.davenonymous.whodoesthat.lib.gui.widgets;


import com.davenonymous.whodoesthat.lib.gui.GUI;
import com.davenonymous.whodoesthat.lib.gui.ISelectable;
import com.davenonymous.whodoesthat.lib.gui.event.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;


public class WidgetList extends WidgetPanel {
	public int padding = 2;
	public int scrollLines = 1;

	private int lineOffset = 0;
	private int lastVisibleLine = 0;
	private int visibleWidgets = 0;

	protected int selected = -1;

	boolean showSelection = true;
	boolean drawBackground = true;
	boolean autoSelectFirstEntry = false;

	public WidgetList() {
		super();

		this.addListener(
			MouseScrollEvent.class, (event, widget) -> {
				if(widget.isPosInside(event.mouseX, event.mouseY)) {
					if(event.up) {
						this.scrollUp();
					} else {
						this.scrollDown();
					}
				}

				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		this.addListener(
			MouseDraggedEvent.class, (event, widget) -> {
				boolean drawScrollbar = visibleWidgets < getTotalLines();
				if(!drawScrollbar) {
					return WidgetEventResult.CONTINUE_PROCESSING;
				}

				int scrollbarWidth = 8;
				int scrollBarX = this.getActualX() + this.width - scrollbarWidth + 1;
				if(event.mouseX() < scrollBarX || event.mouseX() > scrollBarX + scrollbarWidth) {
					return WidgetEventResult.CONTINUE_PROCESSING;
				}

				int scrollBarY = this.getActualY();
				int scrollBarHeight = this.height;
				float ratio = (float) (event.mouseY() - scrollBarY) / scrollBarHeight;

				int newOffset = (int) (ratio * getTotalLines());
				if(newOffset != lineOffset) {
					lineOffset = Math.min(newOffset, getTotalLines() - visibleWidgets);
					updateWidgets();
				}

				return WidgetEventResult.HANDLED;
			}
		);

		this.addListener(
			MouseClickEvent.class, (event, widget) -> {
				boolean drawScrollbar = visibleWidgets < getTotalLines();
				if(!drawScrollbar) {
					return WidgetEventResult.CONTINUE_PROCESSING;
				}

				int scrollbarWidth = 8;
				int scrollBarX = this.getActualX() + this.width - scrollbarWidth + 1;
				if(event.x < scrollBarX || event.x > scrollBarX + scrollbarWidth) {
					return WidgetEventResult.CONTINUE_PROCESSING;
				}

				int scrollBarY = this.getActualY();
				int scrollBarHeight = this.height;
				float ratio = (float) (event.y - scrollBarY) / scrollBarHeight;

				int newOffset = (int) (ratio * getTotalLines());
				if(newOffset != lineOffset) {
					lineOffset = Math.min(newOffset, getTotalLines() - visibleWidgets);
					updateWidgets();
				}

				return WidgetEventResult.HANDLED;
			}
		);
	}

	@Override
	public void clear() {
		super.clear();
		this.selected = -1;
		this.fireEvent(new ListSelectionEvent(this.selected));
	}

	public void scrollToTop() {
		this.lineOffset = 0;
	}

	public void scrollUp() {
		this.lineOffset = Math.max(0, this.lineOffset - this.scrollLines);
		this.updateWidgets();
	}

	public void scrollDown() {
		if(lastVisibleLine == getTotalLines() - 1) {
			return;
		}

		this.lineOffset += this.scrollLines;
		this.updateWidgets();
	}

	public ISelectable getSelectedWidget() {
		if(this.selected == -1 || this.children == null || this.selected >= this.children.size()) {
			return null;
		}
		return (ISelectable) this.children.get(this.selected);
	}

	public void deselect() {
		if(this.selected == -1) {
			return;
		}

		this.getSelectedWidget().setSelected(false);
		this.selected = -1;
		this.fireEvent(new ListSelectionEvent(this.selected));
	}

	public void select(int index) {
		if(index == -1) {
			this.deselect();
			return;
		}

		if(this.selected != -1) {
			this.getSelectedWidget().setSelected(false);
		}
		this.selected = index;
		if(this.getSelectedWidget() != null) {
			this.getSelectedWidget().setSelected(true);
		}
		this.fireEvent(new ListSelectionEvent(this.selected));
	}

	public int getTotalLines() {
		return this.children.size();
	}

	public int getLineHeight(int line) {
		if(line >= this.children.size()) {
			return 0;
		}

		return this.children.get(line).height;
	}

	public boolean shouldDrawBackground() {
		return drawBackground;
	}

	public WidgetList setDrawBackground(boolean drawBackground) {
		this.drawBackground = drawBackground;
		return this;
	}

	public boolean isShowSelection() {
		return showSelection;
	}

	public WidgetList setShowSelection(boolean showSelection) {
		this.showSelection = showSelection;
		return this;
	}

	@Override
	public void draw(GuiGraphics pGuiGraphics, Screen screen) {
		int backgroundColor = 0xFF333333;
		int borderColor = 0xFF000000;
		int selectedBackgroundColor = 0xFF225522;


		// Draw background
		boolean drawScrollbar = visibleWidgets < getTotalLines();
		int scrollbarWidth = drawScrollbar ? 8 : 0;

		int listWidth = width;
		if(this.shouldDrawBackground()) {
			pGuiGraphics.fill(0, 0, listWidth, height, borderColor);
			pGuiGraphics.fill(1, 1, listWidth - 1, height - 1, backgroundColor);
		}

		if(isShowSelection() && selected >= lineOffset && selected <= lastVisibleLine) {
			// We need to high-light a specific line
			int yOffset = padding;
			for(int line = lineOffset; line < selected; line++) {
				Widget widget = this.children.get(line);
				yOffset += widget.height;
			}

			Widget selectedWidget = this.children.get(selected);

			pGuiGraphics.fill(1, yOffset, listWidth - 2 - scrollbarWidth, yOffset + 1 + selectedWidget.height - 1, selectedBackgroundColor);
		}

		// Draw scrollbars
		if(drawScrollbar) {
			int scrollBarX = listWidth - scrollbarWidth - 1;

			int linesBefore = lineOffset;
			int linesAfter = getTotalLines() - lastVisibleLine - 1;

			int scrollColor = 0xFF666666;

			float ratioBefore = (float) linesBefore / getTotalLines();
			float ratioSize = (float) visibleWidgets / getTotalLines();

			int topOffset = (int) (height * ratioBefore);
			int paddleHeight = (int) (height * ratioSize);

			if(topOffset < 2) {
				topOffset = 2;
			}
			int maxPaddleHeight = topOffset + paddleHeight;
			if(maxPaddleHeight > height - 2) {
				maxPaddleHeight = height - 2;
			}
			pGuiGraphics.fill(scrollBarX + 1, topOffset, scrollBarX + scrollbarWidth - 1, maxPaddleHeight, scrollColor);
		}

		super.draw(pGuiGraphics, screen);
	}

	public <T extends Widget & ISelectable> void addListEntry(T widget) {
		if(widget.height <= 0) {
			GUI.LOGGER.warn("Height-less widget [{}] added to list. This will cause problems.", widget);
		}
		if(widget.height > this.height) {
			GUI.LOGGER.warn("List has an entry [{}] larger than the list itself. This will cause problems.", widget);
		}

		widget.addListener(
			MouseClickEvent.class, (event, clickedWidget) -> {
				if(this.selected == this.children.indexOf(widget)) {
					this.selected = -1;
					widget.setSelected(false);
				} else {
					if(this.selected != -1 && this.selected < this.children.size()) {
						Widget oldSelection = this.children.get(selected);
						if(oldSelection instanceof ISelectable) {
							((ISelectable) oldSelection).setSelected(false);
						}
					}

					this.selected = this.children.indexOf(widget);
					widget.setSelected(true);
				}

				this.fireEvent(new ListSelectionEvent(this.selected));

				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		super.add(widget);

		updateWidgets();
	}

	@Override
	public void remove(Widget widget) {
		super.remove(widget);
		this.updateWidgets();
	}

	public void updateWidgets() {
		int visibleHeight = padding;
		boolean exceededListHeight = false;
		visibleWidgets = 0;
		for(int line = 0; line < this.children.size(); line++) {
			Widget widget = this.children.get(line);

			if(line < lineOffset) {
				// Widget is scrolled past -> hide
				widget.setVisible(false);
				continue;
			}

			if(visibleHeight + widget.height > this.height - padding) {
				// Widget won't fit -> no more widgets from here on out
				exceededListHeight = true;
			}

			if(exceededListHeight) {
				widget.setVisible(false);
				continue;
			}

			if(line == this.selected && widget instanceof ISelectable) {
				((ISelectable) widget).setSelected(true);
			}

			widget.setVisible(true);
			widget.setY(visibleHeight);
			widget.setX(padding);
			visibleHeight += widget.height;
			lastVisibleLine = line;
			visibleWidgets++;
		}
	}
}
