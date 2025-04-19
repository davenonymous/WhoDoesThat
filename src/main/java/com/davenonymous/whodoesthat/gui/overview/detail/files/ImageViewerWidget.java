package com.davenonymous.whodoesthat.gui.overview.detail.files;

import com.davenonymous.whodoesthat.lib.gui.CircularPointedArrayList;
import com.davenonymous.whodoesthat.lib.gui.DynamicImageResources;
import com.davenonymous.whodoesthat.lib.gui.Icons;
import com.davenonymous.whodoesthat.lib.gui.event.MouseClickEvent;
import com.davenonymous.whodoesthat.lib.gui.event.MouseMoveEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetSizeChangeEvent;
import com.davenonymous.whodoesthat.lib.gui.tooltip.ColorDisplayTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetColorDisplay;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetImage;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetPanel;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTextBox;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Optional;

public class ImageViewerWidget extends WidgetPanel {
	private WidgetTextBox label;
	private WidgetTextBox zoomLabel;
	private WidgetImage image;
	private WidgetColorDisplay background;

	private CircularPointedArrayList<ChatFormatting> colors = new CircularPointedArrayList<>();
	private DynamicImageResources.ModLogo loadedImage;
	private float zoomLevel = 1;

	public ImageViewerWidget() {
		super();

		colors.addAll(List.of(
			ChatFormatting.LIGHT_PURPLE,
			ChatFormatting.DARK_BLUE,
			ChatFormatting.DARK_GREEN,
			ChatFormatting.DARK_AQUA,
			ChatFormatting.DARK_RED,
			ChatFormatting.DARK_PURPLE,
			ChatFormatting.GOLD,
			ChatFormatting.DARK_GRAY,
			ChatFormatting.BLUE,
			ChatFormatting.GREEN,
			ChatFormatting.AQUA,
			ChatFormatting.RED,
			ChatFormatting.YELLOW,
			ChatFormatting.WHITE,
			ChatFormatting.BLACK
		));

		colors.setPointerTo(ChatFormatting.LIGHT_PURPLE);
		background = new WidgetColorDisplay(colors.getPointedElement());
		this.add(background);

		this.addListener(
			MouseClickEvent.class, (event, widget) -> {
				colors.next();

				background.setColor(colors.getPointedElement());
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		image = new WidgetImage(Icons.paint);
		image.addListener(
			MouseMoveEvent.class, (event, widget) -> {
				if(loadedImage != null) {
					int x = (int) ((event.x - image.x - 4) / zoomLevel);
					int y = (int) ((event.y - image.y - 14) / zoomLevel);

					if(x >= 0 && x < image.textureWidth() && y >= 0 && y < image.textureHeight()) {

						int color = loadedImage.image().getPixelRGBA(x, y); // ABGR
						int r = color & 0xFF;
						int g = (color >> 8) & 0xFF;
						int b = (color >> 16) & 0xFF;
						int a = (color >> 24) & 0xFF;
						color = (r << 16) | (g << 8) | b;

						String hex = String.format("%06x", color).toUpperCase(); //"#" + Integer.toHexString(color).toUpperCase();
						String pos = "§6" + x + "§rx§6" + y + "§r has #§b" + hex + "§r";
						var textTooltip = WrappedStringTooltipComponent.gray(pos);
						this.setTooltipElements(
							textTooltip,
							new ColorDisplayTooltipComponent(color, textTooltip.getWidth(Minecraft.getInstance().font), 20),
							WrappedStringTooltipComponent.gray(String.format("R: §c%d§r", r)),
							WrappedStringTooltipComponent.gray(String.format("G: §2%d§r", g)),
							WrappedStringTooltipComponent.gray(String.format("B: §9%d§r", b)),
							WrappedStringTooltipComponent.gray(String.format("A: §b%.2f§r", a / 255.0f))
						);
					} else {
						this.setTooltipElements();
					}
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
		this.add(image);

		label = new WidgetTextBox(imageSizeString()).setStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));
		this.add(label);

		zoomLabel = new WidgetTextBox("100%").setStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));
		this.add(zoomLabel);

		this.addListener(
			WidgetSizeChangeEvent.class, (event, widget) -> {
				resetZoom();
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
	}

	private String imageSizeString() {
		return (int) image.textureWidth() + "x" + (int) image.textureHeight();
	}

	public ImageViewerWidget resetZoom() {
		if(this.height <= 0 || this.width <= 0) {
			return this;
		}

		zoomLevel = 1;
		int scaledWidth = (int) image.textureWidth();
		int scaledHeight = (int) image.textureHeight();
		while(scaledWidth > this.width || scaledHeight > this.height) {
			scaledWidth /= 2;
			scaledHeight /= 2;
			zoomLevel /= 2;
		}

		while(scaledWidth * 2 < this.width && scaledHeight * 2 < this.height) {
			scaledWidth *= 2;
			scaledHeight *= 2;
			zoomLevel *= 2;
		}

		int xOff = (this.width - scaledWidth) / 2;
		int yOff = (this.height - scaledHeight) / 2;
		background.setSize(scaledWidth + 4, scaledHeight + 4);
		background.setPosition(xOff - 2, yOff - 2);
		image.setSize(scaledWidth, scaledHeight);
		image.setPosition(xOff, yOff);
		label.setPosition(xOff - 2, yOff - 12);
		zoomLabel.setText((int) (zoomLevel * 100) + "%");
		zoomLabel.autoWidth();
		zoomLabel.setPosition(xOff + scaledWidth + 4 - zoomLabel.width, yOff - 12);
		return this;
	}

	public void setImage(String path, byte[] bytes) {
		Optional<DynamicImageResources.ModLogo> loadedImage = DynamicImageResources.getImage(path, bytes);
		if(loadedImage.isPresent()) {
			this.loadedImage = loadedImage.get();

			image.setImage(this.loadedImage.resource());
			image.setTextureSize(this.loadedImage.image().getWidth(), this.loadedImage.image().getHeight());
			label.setText(imageSizeString());

			resetZoom();
		}
	}

	public void unloadImage() {
		if(loadedImage != null) {
			image.setImage(Icons.paint);
			label.setText("");
			Minecraft.getInstance().getTextureManager().release(loadedImage.resource());
			loadedImage.image().close();
			loadedImage = null;
		}
	}
}
