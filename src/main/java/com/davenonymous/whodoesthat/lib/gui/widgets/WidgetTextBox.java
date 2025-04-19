package com.davenonymous.whodoesthat.lib.gui.widgets;


import com.davenonymous.whodoesthat.lib.gui.GUIHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.function.Function;

public class WidgetTextBox extends Widget {
	private String text;
	private int textColor = 0xFFFFFF;
	private boolean dropShadow = false;
	private boolean wordWrap = false;
	protected Style style = Style.EMPTY;

	public WidgetTextBox(String text) {
		this.text = text;
		this.setWidth(100);
		this.setHeight(9);
	}

	public WidgetTextBox(String text, int textColor) {
		this.text = text;
		this.textColor = textColor;
		this.setWidth(100);
		this.setHeight(9);
	}

	public void autoWidth() {
		this.setWidth(Minecraft.getInstance().font.width(FormattedText.of(text, style)) + 2);
	}

	public void autoWidth(int maxWidth) {
		this.setWidth(Math.min(Minecraft.getInstance().font.width(FormattedText.of(text, style)) + 2, maxWidth));
	}

	public WidgetTextBox setStyle(Function<Style, Style> style) {
		this.style = style.apply(this.style);
		return this;
	}

	public boolean isWordWrap() {
		return wordWrap;
	}

	public WidgetTextBox setWordWrap(boolean wordWrap) {
		this.wordWrap = wordWrap;
		return this;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setDropShadow(boolean dropShadow) {
		this.dropShadow = dropShadow;
	}

	public boolean getDropShadow() {
		return dropShadow;
	}

	@Override
	public void draw(GuiGraphics pGuiGraphics, Screen screen) {
		if(text == null) {
			return;
		}

		pGuiGraphics.pose().pushPose();
		RenderSystem.enableBlend();

		int scale = computeGuiScale(screen.getMinecraft());
		int bottomOffset = (int) (((double) (screen.getMinecraft().getWindow().getHeight() / scale) - (getActualY() + height)) * scale);
		int heightTmp = (height * scale) - 1;
		if(heightTmp < 0) {
			heightTmp = 0;
		}

		RenderSystem.enableScissor(getActualX() * scale - 3, bottomOffset + 2, width * scale, heightTmp);
		if(wordWrap) {
			GUIHelper.drawWordWrap(pGuiGraphics, screen.getMinecraft().font, FormattedText.of(text, style), 0, 0, width, textColor);
		} else {
			GUIHelper.drawWordWrap(pGuiGraphics, screen.getMinecraft().font, FormattedText.of(text, style), 0, 0, Integer.MAX_VALUE, textColor);
		}
		RenderSystem.disableScissor();

		RenderSystem.disableBlend();
		pGuiGraphics.pose().popPose();
	}
}
