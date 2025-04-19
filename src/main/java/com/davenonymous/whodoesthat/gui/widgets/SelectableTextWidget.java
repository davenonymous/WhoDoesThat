package com.davenonymous.whodoesthat.gui.widgets;

import com.davenonymous.whodoesthat.lib.gui.ISelectable;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTextBox;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;

import java.util.function.Function;

public class SelectableTextWidget extends WidgetTextBox implements ISelectable {
	private boolean isSelected = false;
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
		return isSelected;
	}

	@Override
	public void setSelected(boolean state) {
		isSelected = state;
	}

	@Override
	public void draw(GuiGraphics pGuiGraphics, Screen screen) {
		pGuiGraphics.pose().pushPose();
		pGuiGraphics.pose().translate(0, 2, 0);
		super.draw(pGuiGraphics, screen);
		pGuiGraphics.pose().popPose();
	}
}
