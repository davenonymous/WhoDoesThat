package com.davenonymous.whodoesthat.lib.gui;


import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.lib.gui.widgets.IValueProvider;
import com.davenonymous.whodoesthat.lib.gui.widgets.Widget;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetPanel;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GUI extends WidgetPanel {
	public static ResourceLocation tabIcons = WhoDoesThat.resource("textures/gui/tabicons.png");
	public static ResourceLocation windowBackground = WhoDoesThat.resource("textures/gui/window.png");
	public static ResourceLocation defaultButtonTexture = WhoDoesThat.resource("textures/gui/button_background.png");
	public static final Logger LOGGER = LogUtils.getLogger();

	public boolean hasTabs = false;
	private final Map<ResourceLocation, IValueProvider> valueMap = new HashMap<>();
	private WidgetContainer container;

	public GUI(int x, int y, int width, int height) {
		this.setX(x);
		this.setY(y);
		this.setWidth(width);
		this.setHeight(height);
	}

	public void findValueWidgets() {
		this.findValueWidgets(this);
	}

	public void registerValueWidget(ResourceLocation id, IValueProvider widget) {
		this.valueMap.put(id, widget);
	}

	public Object getValue(ResourceLocation id) {
		if(id == null || !valueMap.containsKey(id)) {
			return null;
		}

		return valueMap.get(id).getValue();
	}

	public void drawGUI(GuiGraphics pGuiGraphics, Screen screen) {
		this.setX((screen.width - this.width) / 2);
		this.setY((screen.height - this.height) / 2);

		this.shiftAndDraw(pGuiGraphics, screen);
	}

	@Override
	public void drawBeforeShift(GuiGraphics pGuiGraphics, Screen screen) {
		//screen.drawDefaultBackground();

		super.drawBeforeShift(pGuiGraphics, screen);
	}

	@Override
	public void draw(GuiGraphics pGuiGraphics, Screen screen) {
		GUIHelper.drawWindow(pGuiGraphics, this.width, this.height, this.hasTabs);
		super.draw(pGuiGraphics, screen);
	}

	public void drawTooltips(GuiGraphics pGuiGraphics, Screen screen, int mouseX, int mouseY) {
		Widget hoveredWidget = getHoveredWidget(mouseX, mouseY);
		if(hoveredWidget == null || hoveredWidget.getTooltip() == null) {
			return;
		}
		if(hoveredWidget.getTooltip().isEmpty()) {
			return;
		}

		Font font = screen.getMinecraft().font;
		pGuiGraphics.renderComponentTooltipFromElements(font, hoveredWidget.getTooltipFormatted(), mouseX, mouseY, ItemStack.EMPTY);
	}

	public void drawSlot(GuiGraphics pGuiGraphics, Screen screen, Slot slot, int guiLeft, int guiTop) {
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

		if(slot instanceof WidgetSlot) {
			if(!slot.allowModification(screen.getMinecraft().player)) {
				RenderSystem.setShaderColor(1.0f, 0.3f, 0.3f, 1.0f);
			}
		}

		float offsetX = guiLeft - 1;
		float offsetY = guiTop - 1;

		pGuiGraphics.pose().pushPose();
		pGuiGraphics.pose().translate(offsetX, offsetY, 0.0f);
		int texOffsetY = 84;
		int texOffsetX = 84;

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUIHelper.tabIcons);
		pGuiGraphics.blit(GUIHelper.tabIcons, slot.x, slot.y, texOffsetX, texOffsetY, 18, 18);

		//RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		pGuiGraphics.pose().popPose();
	}

	public void setContainer(WidgetContainer container) {
		this.container = container;
	}

	public WidgetContainer getContainer() {
		return container;
	}
}
