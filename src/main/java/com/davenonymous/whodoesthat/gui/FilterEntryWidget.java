package com.davenonymous.whodoesthat.gui;

import com.davenonymous.whodoesthat.lib.gui.GUI;
import com.davenonymous.whodoesthat.lib.gui.IClientTooltipProvider;
import com.davenonymous.whodoesthat.lib.gui.event.IWidgetListener;
import com.davenonymous.whodoesthat.lib.gui.event.ValueChangedEvent;
import com.davenonymous.whodoesthat.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetSelectablePanelWithValue;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetSpriteSelect;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTextBox;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public class FilterEntryWidget extends WidgetSelectablePanelWithValue<FilterEntryWidget.FilterEntryState> {
	public enum FilterEntryState implements IClientTooltipProvider {
		MUST,
		NOT,
		WHATEVER;

		@Override
		public List<TooltipComponent> getClientTooltip() {
			ChatFormatting color = switch(this) {
				case MUST -> ChatFormatting.GREEN;
				case NOT -> ChatFormatting.RED;
				default -> ChatFormatting.GRAY;
			};
			TooltipComponent tooltip = switch(this) {
				case MUST -> new StringTooltipComponent(I18n.get("whodoesthat.gui.filter_entry.must.tooltip"), color.getColor());
				case NOT -> new StringTooltipComponent(I18n.get("whodoesthat.gui.filter_entry.not.tooltip"), color.getColor());
				default -> new StringTooltipComponent(I18n.get("whodoesthat.gui.filter_entry.whatever.tooltip"), color.getColor());
			};
			return List.of(tooltip);
		}
	}

	private FilterEntryState checked = FilterEntryState.WHATEVER;
	private String summary;
	private WidgetTextBox summaryWidget;
	private WidgetSpriteSelect<FilterEntryState> checkbox;

	public FilterEntryWidget(String summary) {
		this.summary = summary;
		this.height = 13;

		checkbox = new WidgetSpriteSelect<>();
		checkbox.addChoiceWithSprite(
			FilterEntryState.WHATEVER,
			new WidgetSpriteSelect.SpriteData(GUI.tabIcons, 104, 0, 14, 11)
		);
		checkbox.addChoiceWithSprite(
			FilterEntryState.MUST,
			new WidgetSpriteSelect.SpriteData(GUI.tabIcons, 76, 0, 14, 11)
		);
		checkbox.addChoiceWithSprite(
			FilterEntryState.NOT,
			new WidgetSpriteSelect.SpriteData(GUI.tabIcons, 118, 0, 14, 11)
		);

		checkbox.setValue(FilterEntryState.WHATEVER);
		checkbox.setPosition(0, 0);
		checkbox.setSize(14, 11);
		this.add(checkbox);

		summaryWidget = new WidgetTextBox(summary);
		summaryWidget.setPosition(18, 2);
		this.add(summaryWidget);
	}

	public FilterEntryWidget setState(FilterEntryState state) {
		checked = state;
		checkbox.setValue(state);
		return this;
	}

	public void addCheckboxListener(IWidgetListener<ValueChangedEvent<FilterEntryState>> listener) {
		checkbox.addListener(
			ValueChangedEvent.class, (event, widget) -> {
				listener.call(event, this);
				return null;
			}
		);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		summaryWidget.setWidth(width - 18);
	}

	@Override
	public FilterEntryState getValue() {
		return checked;
	}

	@Override
	public void setValue(FilterEntryState value) {
		checked = value;
	}
}
