package com.davenonymous.whodoesthat.gui;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.data.result.ModAnalysisResult;
import com.davenonymous.whodoesthat.lib.gui.DynamicImageResources;
import com.davenonymous.whodoesthat.lib.gui.GUI;
import com.davenonymous.whodoesthat.lib.gui.GUIHelper;
import com.davenonymous.whodoesthat.lib.gui.ISelectable;
import com.davenonymous.whodoesthat.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.widgets.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.ArrayList;
import java.util.List;

public class LightModWidget extends WidgetSelectablePanelWithValue<ModAnalysisResult> implements ISelectable {
	public static final ResourceLocation BACKGROUND = WhoDoesThat.resource("textures/gui/window.png");
	public static final ResourceLocation BACKGROUND_SELECTED = WhoDoesThat.resource("textures/gui/window_pushed.png");

	public String modId;
	ModAnalysisResult modInfo;
	IModInfo modFile;
	WidgetTextBox modName;
	WidgetTextBox version;
	WidgetTextBox description;
	WidgetSprite updateIndicator;
	int fullWidth;

	public LightModWidget(String modId, ModAnalysisResult modInfo, int width) {
		this.modId = modId;
		this.modInfo = modInfo;
		this.height = 40;
		this.width = width;
		this.fullWidth = width;

		var analysis = modInfo.modInfoResult().entries();
		String containingJar = analysis.get("file");

		modName = new WidgetTextBox(analysis.get("displayName"));
		modName.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		modName.setPosition(6, 6);
		modName.setWordWrap(true);
		modName.setStyle(style -> style.withBold(true));
		modName.autoWidth();
		List<TooltipComponent> nameTooltip = new ArrayList<>();
		nameTooltip.add(StringTooltipComponent.white(analysis.get("displayName")));
		if(analysis.containsKey("authors")) {
			nameTooltip.add(StringTooltipComponent.gray("By: " + analysis.get("authors")));
		}
		nameTooltip.add(StringTooltipComponent.gray("ID: " + modId));
		nameTooltip.add(StringTooltipComponent.gray("File: " + analysis.get("file")));
		if(analysis.containsKey("jarInJar")) {
			nameTooltip.add(StringTooltipComponent.gray("Jar in Jar: " + analysis.get("jarInJar")));
		}
		modName.setTooltipElements(nameTooltip);
		this.add(modName);

		version = new WidgetTextBox(analysis.get("version"));
		version.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		version.autoWidth();
		version.setPosition(this.width - version.width - 4, 6);
		this.add(version);

		updateIndicator = new WidgetSprite(GUIHelper.tabIcons, 36, 84, 4, 11);
		updateIndicator.setSize(4, 11);
		updateIndicator.setPosition(this.width - version.width - 10, 5);
		updateIndicator.scale = 0.8f;
		if(analysis.containsKey("latest")) {
			updateIndicator.setTooltipLines(Component.literal("Update Available"), Component.literal(analysis.get("latest")));
			this.add(updateIndicator);
		}

		description = new WidgetTextBox(analysis.get("description"));
		description.setWordWrap(true);
		description.setStyle(style -> style.withItalic(true));
		description.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		description.setPosition(90, 17);
		description.setWidth(this.width - 96);
		description.setHeight(19);
		this.add(description);

		Widget imageWidget = null;
		if(ModList.get().getModContainerById(modId).isPresent()) {
			this.modFile = ModList.get().getModContainerById(modId).get().getModInfo();
			var loadedImage = DynamicImageResources.getModLogo(this.modFile);

			if(loadedImage.isPresent()) {
				var modImage = new WidgetImage(loadedImage.get());
				modImage.setPosition(6, 15);
				modImage.setSize(84, 21);
				this.add(modImage);
				imageWidget = modImage;
			}
		}
		if(imageWidget == null) {
			var missingSprite = new WidgetSprite(GUI.tabIcons, 0, 84, 14, 14);
			missingSprite.setSize(14, 14);
			missingSprite.setPosition(6 + 34, 18);
			this.add(missingSprite);
			imageWidget = missingSprite;
		}

		imageWidget.setTooltipLines(Component.literal(analysis.get("description")));

	}

	public LightModWidget setMinimalMode(boolean minimalMode) {
		description.setVisible(!minimalMode);
		version.setVisible(!minimalMode);
		updateIndicator.setVisible(!minimalMode);
		modName.autoWidth();
		if(isSelected()) {
			modName.setTextColor(ChatFormatting.WHITE.getColor());
		} else {
			modName.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		}
		if(minimalMode) {
			setWidth(100);
			if(modName.width > 100) {
				modName.setWidth(100);
			}
		} else {
			setWidth(fullWidth);
		}
		return this;
	}

	@Override
	public ModAnalysisResult getValue() {
		return modInfo;
	}

	@Override
	public void setValue(ModAnalysisResult value) {
		this.modInfo = value;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, Screen screen) {
		//GUIHelper.drawWindow(guiGraphics, this.width, this.height, false);
		if(isSelected()) {
			GUIHelper.drawEmbossedWindow(guiGraphics, BACKGROUND_SELECTED, this.width, this.height);
		} else {
			GUIHelper.drawEmbossedWindow(guiGraphics, BACKGROUND, this.width, this.height);
		}

		super.draw(guiGraphics, screen);
	}
}
