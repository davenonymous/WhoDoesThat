package com.davenonymous.whodoesthat.gui.menu;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.config.GuiConfig;
import com.davenonymous.whodoesthat.gui.overview.ModOverviewScreen;
import com.davenonymous.whodoesthat.lib.gui.GUI;
import com.davenonymous.whodoesthat.lib.gui.Icons;
import com.davenonymous.whodoesthat.lib.gui.WidgetScreen;
import com.davenonymous.whodoesthat.lib.gui.event.MouseClickEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetImage;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetImageButton;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetProgressBar;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetWithLabel;
import com.davenonymous.whodoesthat.setup.ScanHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class TitleScreenAddon extends WidgetScreen {
	WidgetProgressBar progressWidget;

	WidgetWithLabel<?> jarsLabel;
	WidgetWithLabel<?> modsLabel;
	WidgetWithLabel<?> analyzedLabel;

	WidgetImageButton openModOverviewButton;

	boolean expanded = false;
	int padding;

	int openY;
	int closedY;
	int barY;
	int barY_expanded;
	int barHeight;
	int barHeight_expanded;

	public TitleScreenAddon(Minecraft mc) {
		super(Component.empty());
		this.width = 150;
		this.height = 48;
		this.minecraft = mc;
		this.padding = 5;
		this.barHeight = 4;
		this.barHeight_expanded = 16;

		this.barY = height - padding - barHeight;
		this.barY_expanded = padding;

		this.openY = -4;
		this.closedY = -38;
		if(GuiConfig.menuDockSide != null && GuiConfig.menuDockSide.isBottom) {
			var guiPos = GuiConfig.menuDockSide.getChildPosition(minecraft.getWindow(), this.width, this.height);
			this.barY = padding;
			this.openY = guiPos.getY() + 4;
			this.closedY = guiPos.getY() + 38;
		}
	}

	@Override
	protected GUI createGUI() {
		var guiPos = GuiConfig.menuDockSide.getChildPosition(minecraft.getWindow(), this.width, this.height);

		GUI gui = new NoBoundsGUI(guiPos.getX(), closedY, this.width, this.height);
		gui.zLevel = 20.0f;

		gui.addListener(
			MouseClickEvent.class, ((event, widget) -> {
				if(!expanded && gui.isPosInside(event.x, event.y)) {
					this.expanded = true;
					this.updateWidgets(gui);
					return WidgetEventResult.HANDLED;
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
			})
		);

		progressWidget = new WidgetProgressBar();
		progressWidget.setDisplayMode(WidgetProgressBar.EnumDisplayMode.NOTHING);
		progressWidget.setValue(WhoDoesThat.LAST_ANALYSIS == null ? 0.0d : 100.0d);
		progressWidget.setPosition(padding, barY);
		progressWidget.setSize(this.width - padding * 2, barHeight);
		progressWidget.addListener(
			MouseClickEvent.class, ((event, widget) -> {
				if(expanded) {
					this.expanded = false;
					this.updateWidgets(gui);
					return WidgetEventResult.HANDLED;
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
			})
		);
		gui.add(progressWidget);

		int yOffset = padding + barHeight_expanded + 6;
		int xOffset = padding;

		int badgeWidth = 35;
		var jarsIcon = new WidgetImage(Icons.folderZipperSmall);
		jarsIcon.setSize(13, 11);
		jarsIcon.setTextureSize(13, 11);

		jarsLabel = new WidgetWithLabel<>(jarsIcon, "0");
		jarsLabel.applyLabelStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));
		jarsLabel.setPosition(xOffset, yOffset);
		jarsLabel.setLabelOffset(0, 1);
		jarsLabel.setSize(badgeWidth, 16);
		gui.add(jarsLabel);
		xOffset += jarsLabel.width + 2;

		var analyzedIcon = new WidgetImage(Icons.javaCup);
		analyzedIcon.setSize(16, 16);
		analyzedIcon.setTextureSize(16, 16);

		analyzedLabel = new WidgetWithLabel<>(analyzedIcon, "0");
		analyzedLabel.applyLabelStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));
		analyzedLabel.setPosition(xOffset, yOffset - 2);
		analyzedLabel.setLabelOffset(-2, 1);
		analyzedLabel.setSize(badgeWidth, 16);
		gui.add(analyzedLabel);
		xOffset += analyzedLabel.width + 2;

		var modsIcon = new WidgetImage(Icons.javaForgeMod);
		modsIcon.setSize(16, 16);
		modsIcon.setTextureSize(16, 16);

		modsLabel = new WidgetWithLabel<>(modsIcon, "0");
		modsLabel.applyLabelStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));
		modsLabel.setPosition(xOffset, yOffset - 2);
		modsLabel.setLabelOffset(-2, 1);
		modsLabel.setSize(badgeWidth, 16);
		gui.add(modsLabel);
		xOffset += modsLabel.width + 2;


		var buttonIcon = new WidgetImage(Icons.javaCup);
		buttonIcon.setSize(16, 16);
		buttonIcon.setTextureSize(16, 16);

		openModOverviewButton = new WidgetImageButton(buttonIcon);
		openModOverviewButton.setPosition(this.width - padding - openModOverviewButton.width, yOffset - 4);
		openModOverviewButton.setVisible(false);
		openModOverviewButton.setEnabled(WhoDoesThat.LAST_ANALYSIS != null);
		openModOverviewButton.addListener(
			MouseClickEvent.class, ((event, widget) -> {
				if(WhoDoesThat.LAST_ANALYSIS != null) {
					Minecraft.getInstance().pushGuiLayer(new ModOverviewScreen());
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
			})
		);
		if(WhoDoesThat.LAST_ANALYSIS == null) {
			openModOverviewButton.setTooltipElements(WrappedStringTooltipComponent.white(I18n.get("whodoesthat.gui.modlist_inject.show_overview.not_ready.tooltip")));
		} else {
			openModOverviewButton.setTooltipElements(WrappedStringTooltipComponent.white(I18n.get("whodoesthat.gui.modlist_inject.show_overview.ready.tooltip")));
		}
		gui.add(openModOverviewButton);

		if(ScanHelper.scanProgress != null) {
			progressWidget.setValue(ScanHelper.scanProgress.getProgress() * 100.0d);
			jarsLabel.setText(ScanHelper.scanProgress.scannedJars() + "");
			modsLabel.setText(ScanHelper.scanProgress.foundMods() + "");
			analyzedLabel.setText(ScanHelper.scanProgress.analyzedJars() + "");

			jarsLabel.setTooltipElements(WrappedStringTooltipComponent.white(I18n.get("whodoesthat.gui.jars_scanned.tooltip", ScanHelper.scanProgress.scannedJars())));
			modsLabel.setTooltipElements(WrappedStringTooltipComponent.white(I18n.get("whodoesthat.gui.mods_found.tooltip", ScanHelper.scanProgress.foundMods())));
			analyzedLabel.setTooltipElements(WrappedStringTooltipComponent.white(I18n.get("whodoesthat.gui.jars_analyzed.tooltip", ScanHelper.scanProgress.analyzedJars())));
		}

		gui.addListener(
			ProgressEvent.class, (event, widget) -> {
				if(progressWidget == null) {
					return WidgetEventResult.CONTINUE_PROCESSING;
				}
				progressWidget.setValue(event.progress().getProgress() * 100.0d);
				jarsLabel.setText(event.progress().scannedJars() + "");
				modsLabel.setText(event.progress().foundMods() + "");
				analyzedLabel.setText(event.progress().analyzedJars() + "");
				openModOverviewButton.setEnabled(WhoDoesThat.LAST_ANALYSIS != null);
				jarsLabel.setTooltipElements(WrappedStringTooltipComponent.white(I18n.get("whodoesthat.gui.jars_scanned.tooltip", ScanHelper.scanProgress.scannedJars())));
				modsLabel.setTooltipElements(WrappedStringTooltipComponent.white(I18n.get("whodoesthat.gui.mods_found.tooltip", ScanHelper.scanProgress.foundMods())));
				analyzedLabel.setTooltipElements(WrappedStringTooltipComponent.white(I18n.get("whodoesthat.gui.jars_analyzed.tooltip", ScanHelper.scanProgress.analyzedJars())));
				if(WhoDoesThat.LAST_ANALYSIS == null) {
					openModOverviewButton.setTooltipElements(WrappedStringTooltipComponent.white(I18n.get("whodoesthat.gui.modlist_inject.show_overview.not_ready.tooltip")));
				} else {
					openModOverviewButton.setTooltipElements(WrappedStringTooltipComponent.white(I18n.get("whodoesthat.gui.modlist_inject.show_overview.ready.tooltip")));
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		updateWidgets(gui);
		return gui;
	}

	public void updateWidgets(GUI gui) {
		openModOverviewButton.setVisible(expanded);
		jarsLabel.setVisible(expanded);
		modsLabel.setVisible(expanded);
		analyzedLabel.setVisible(expanded);

		if(expanded) {
			progressWidget.setY(barY_expanded);
			progressWidget.setHeight(barHeight_expanded);
			progressWidget.setDisplayMode(WidgetProgressBar.EnumDisplayMode.PERCENTAGE);
			gui.setY(openY);
		} else {
			progressWidget.setY(barY);
			progressWidget.setHeight(barHeight);
			progressWidget.setDisplayMode(WidgetProgressBar.EnumDisplayMode.NOTHING);
			gui.setY(closedY);
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

	}

	private static class NoBoundsGUI extends GUI {

		public NoBoundsGUI(int x, int y, int width, int height) {
			super(x, y, width, height);
		}

		@Override
		public void drawGUI(GuiGraphics pGuiGraphics, Screen screen) {
			this.shiftAndDraw(pGuiGraphics, screen);
		}
	}
}
