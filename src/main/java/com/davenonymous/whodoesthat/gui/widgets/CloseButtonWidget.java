package com.davenonymous.whodoesthat.gui.widgets;

import com.davenonymous.whodoesthat.lib.gui.event.*;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class CloseButtonWidget extends WidgetImage {
	private static final ResourceLocation IMAGE = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/cross_button.png");
	private static final ResourceLocation IMAGE_HIGHLIGHT = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/cross_button_highlighted.png");

	public CloseButtonWidget() {
		this((event, widget) -> {
			Minecraft.getInstance().popGuiLayer();
			return WidgetEventResult.HANDLED;
		});
	}

	public CloseButtonWidget(IWidgetListener<MouseClickEvent> listener) {
		super(IMAGE);
		this.setSize(10, 10);
		this.addListener(MouseClickEvent.class, listener);

		this.addListener(
			MouseEnterEvent.class, (event, widget) -> {
				this.setImage(IMAGE_HIGHLIGHT);
				return WidgetEventResult.HANDLED;
			}
		);

		this.addListener(
			MouseExitEvent.class, (event, widget) -> {
				this.setImage(IMAGE);
				return WidgetEventResult.HANDLED;
			}
		);
	}
}
