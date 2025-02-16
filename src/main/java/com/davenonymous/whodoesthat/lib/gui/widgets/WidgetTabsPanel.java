package com.davenonymous.whodoesthat.lib.gui.widgets;

import com.davenonymous.whodoesthat.lib.gui.GUIHelper;
import com.davenonymous.whodoesthat.lib.gui.event.TabChangedEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WidgetTabsPanel extends WidgetPanel {
	protected final List<WidgetPanel> pages = new ArrayList<>();
	protected final Map<WidgetPanel, Widget> pageButtonWidget = new HashMap<>();
	protected final Map<WidgetPanel, List<Component>> pageTooltips = new HashMap<>();
	protected TabDockEdge edge = TabDockEdge.WEST;

	protected WidgetPanel activePanel = null;
	protected WidgetPanel buttonsPanel = null;

	public WidgetTabsPanel() {
		super();
		this.buttonsPanel = new WidgetPanel();
		this.buttonsPanel.setVisible(false);
		this.add(buttonsPanel);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		this.buttonsPanel.setDimensions(0, 0, this.width, 32);
	}

	public WidgetTabsPanel setEdge(TabDockEdge edge) {
		this.edge = edge;
		return this;
	}

	@Override
	public void clear() {
		super.clear();
		this.pages.clear();
		this.pageButtonWidget.clear();
		this.pageTooltips.clear();
		this.children.clear();
	}

	public void addPage(WidgetPanel panel, Widget buttonStack) {
		this.addPage(panel, buttonStack, null);
	}

	public void addPage(WidgetPanel panel, Widget buttonStack, List<Component> tooltip) {
		panel.setWidth(this.width);
		panel.setHeight(this.height - 32);
		panel.setY(32);
		panel.parent = this;

		pages.add(panel);
		pageButtonWidget.put(panel, buttonStack);

		if(activePanel == null) {
			activePanel = panel;
			activePanel.setVisible(true);
		} else {
			panel.setVisible(false);
		}

		if(tooltip != null) {
			pageTooltips.put(panel, tooltip);
		}

		this.add(panel);

		updateButtonsPanel();
	}

	public void setActivePage(int page) {
		if(page < 0 || page >= pages.size()) {
			return;
		}

		activePanel.setVisible(false);
		pages.get(page).setVisible(true);

		WidgetPanel tmpOld = activePanel;
		activePanel = pages.get(page);

		this.fireEvent(new TabChangedEvent(tmpOld, pages.get(page)));
	}

	public void updateButtonsPanel() {
		this.buttonsPanel.clear();
		this.buttonsPanel.setVisible(false);
		if(pages.size() > 0) {
			this.buttonsPanel.setVisible(true);
		}

		int y = 0;
		int x = edge == TabDockEdge.NORTH ? 4 : 0;
		for(WidgetPanel page : pages) {
			WidgetTabsButton button = new WidgetTabsButton(this, page, pageButtonWidget.get(page), edge);
			button.setPosition(x, y);
			switch(edge) {
				default:
				case WEST:
					button.setSize(32, 28);
					y += 28;
					break;
				case NORTH:
					button.setSize(31, 32);
					x += 31;
					break;
			}
			buttonsPanel.add(button);

			if(pageTooltips.containsKey(page)) {
				button.addTooltipLine(pageTooltips.get(page));
			}
		}
	}

	@Override
	public void draw(GuiGraphics guiGraphics, Screen screen) {
		GUIHelper.drawWindow(guiGraphics, this.width, this.height - 30, false, this.x - 9, this.y - 10 + 32);
		super.draw(guiGraphics, screen);
	}

	public enum TabDockEdge {
		WEST, NORTH
	}

}
