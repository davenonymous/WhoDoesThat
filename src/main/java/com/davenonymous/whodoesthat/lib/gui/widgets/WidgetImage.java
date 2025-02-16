package com.davenonymous.whodoesthat.lib.gui.widgets;


import com.davenonymous.whodoesthat.lib.gui.DynamicImageResources;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Size2i;

import java.awt.*;

public class WidgetImage extends Widget {
	ResourceLocation image;
	float textureWidth = 16.0f;
	float textureHeight = 16.0f;
	Color color;
	float alpha = 1.0f;
	float scale = 1.0f;
	Size2i offset = new Size2i(0, 0);

	public WidgetImage(ResourceLocation image) {
		this.image = image;
	}

	public WidgetImage(DynamicImageResources.ModLogo logo) {
		this.image = logo.resource();
		this.textureWidth = logo.size().width;
		this.textureHeight = logo.size().height;
	}

	public WidgetImage setTextureSize(float width, float height) {
		this.textureWidth = width;
		this.textureHeight = height;
		return this;
	}

	public WidgetImage setAlpha(float alpha) {
		this.alpha = alpha;
		return this;
	}

	public WidgetImage setScale(float scale) {
		this.scale = scale;
		return this;
	}

	public WidgetImage setOffset(Size2i offset) {
		this.offset = offset;
		return this;
	}

	public WidgetImage setOffset(int x, int y) {
		return setOffset(new Size2i(x, y));
	}

	public WidgetImage setColor(Color color) {
		this.alpha = color.getAlpha() / 255;
		this.color = color;
		return this;
	}

	public WidgetImage resetColor() {
		this.alpha = 1.0f;
		this.color = null;
		return this;
	}

	public WidgetImage setImage(ResourceLocation image) {
		this.image = image;
		return this;
	}

	@Override
	public void draw(GuiGraphics pGuiGraphics, Screen screen) {
		if(visible && areAllParentsVisible()) {
			RenderSystem.enableBlend();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			pGuiGraphics.pose().pushPose();
			pGuiGraphics.pose().scale(scale, scale, 1);
			pGuiGraphics.blitInscribed(this.image, offset.width, offset.height, this.width, this.height, (int) this.textureWidth, (int) this.textureHeight, true, true);
			pGuiGraphics.pose().popPose();
		}
	}
}
