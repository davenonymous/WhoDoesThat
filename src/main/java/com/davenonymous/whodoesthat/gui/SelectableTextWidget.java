package com.davenonymous.whodoesthat.gui;

import com.davenonymous.whodoesthat.lib.gui.ISelectable;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTextBox;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

import java.util.function.Function;

public class SelectableTextWidget extends WidgetTextBox implements ISelectable {

	private String summary;

	public SelectableTextWidget(String text) {
		super(text);
		this.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		this.setHeight(12);
	}

	public SelectableTextWidget setStyle(Function<Style, Style> style) {
		this.style = style.apply(this.style);
		return this;
	}

	@Override
	public boolean isSelected() {
		return false;
	}

	@Override
	public void setSelected(boolean state) {

	}
}
