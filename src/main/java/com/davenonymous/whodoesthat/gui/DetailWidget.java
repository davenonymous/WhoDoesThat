package com.davenonymous.whodoesthat.gui;

import com.davenonymous.whodoesthat.data.result.ModAnalysisResult;
import com.davenonymous.whodoesthat.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.widgets.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DetailWidget extends WidgetPanel {
	public String modId = "";
	ModAnalysisResult modInfo = null;
	IModInfo modFile;
	public WidgetTabsPanel tabs;

	public DetailWidget() {
		super();
		this.setVisible(false);
	}

	public void setMod(String modId, ModAnalysisResult modInfo) {
		this.modId = modId;
		this.modInfo = modInfo;
		this.modFile = ModList.get().getModContainerById(modId).get().getModInfo();
		var analysis = modInfo.modInfoResult().entries();

		this.clear();

		var basePackage = analysis.getOrDefault("package", "");
		var modName = new WidgetTextBox(analysis.get("displayName"));
		modName.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		modName.setPosition(6, -37);

		this.add(modName);

		WidgetList summaryList = new WidgetList();
		summaryList.padding = 4;
		summaryList.setDrawBackground(false);
		summaryList.setShowSelection(false);

		WidgetList eventList = new WidgetList();
		eventList.padding = 4;
		eventList.setDrawBackground(false);
		eventList.setShowSelection(false);

		WidgetList mixinList = new WidgetList();
		mixinList.padding = 4;
		mixinList.setDrawBackground(false);
		mixinList.setShowSelection(false);

		WidgetList dependenciesList = new WidgetList();
		dependenciesList.padding = 4;
		dependenciesList.setDrawBackground(false);
		dependenciesList.setShowSelection(false);

		WidgetList inheritanceList = new WidgetList();
		inheritanceList.padding = 4;
		inheritanceList.setDrawBackground(false);
		inheritanceList.setShowSelection(false);

		tabs = new WidgetTabsPanel();
		tabs.setEdge(WidgetTabsPanel.TabDockEdge.NORTH);
		tabs.setDimensions(5, 5, this.width, this.height);
		this.add(tabs);

		List<String> summaries = new ArrayList<>(modInfo.summarySet());
		if(!summaries.isEmpty()) {
			ResourceLocation infoLogo = ResourceLocation.withDefaultNamespace("textures/gui/sprites/icon/info.png");
			WidgetImage infoLogoWidget = new WidgetImage(infoLogo);
			infoLogoWidget.setSize(16, 16);
			infoLogoWidget.setTextureSize(20, 20);
			tabs.addPage(summaryList, infoLogoWidget, List.of(Component.translatable("whodoesthat.gui.summary")));
			summaryList.setWidth(this.width - 8);
			summaryList.setHeight(this.height - 40);

			summaries.sort(String::compareTo);
			for(String summary : summaries) {
				var entryWidget = new SelectableTextWidget(summary);
				entryWidget.autoWidth();

				if(entryWidget.width >= summaryList.width - 15) {
					entryWidget.setWidth(summaryList.width - 15);
				}
				summaryList.addListEntry(entryWidget);
			}

		}

		if(modInfo.events().isPresent()) {
			ResourceLocation infoLogo = ResourceLocation.withDefaultNamespace("textures/gui/sprites/notification/more.png");
			WidgetImage infoLogoWidget = new WidgetImage(infoLogo);
			infoLogoWidget.setSize(16, 16);
			infoLogoWidget.setTextureSize(8, 8);
			infoLogoWidget.setScale(0.75f);
			infoLogoWidget.setOffset(3, 3);
			tabs.addPage(eventList, infoLogoWidget, List.of(Component.translatable("whodoesthat.gui.events")));
			Map<String, List<String>> events = modInfo.events().get();
			if(!events.isEmpty()) {
				Map<String, List<String>> shortenedEvents = events.entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey().substring(e.getKey().lastIndexOf('.') + 1), Map.Entry::getValue));
				List<String> eventNames = new ArrayList<>(shortenedEvents.keySet());
				eventNames.sort(String::compareTo);
				for(String eventName : eventNames) {
					List<String> listeningClasses = new ArrayList<>(shortenedEvents.get(eventName).stream().map(s -> s.substring(s.lastIndexOf('.') + 1)).toList());
					String shortEventName = eventName.substring(eventName.lastIndexOf('.') + 1);

					var entryWidget = new SelectableTextWidget(shortEventName);
					entryWidget.autoWidth();
					if(entryWidget.width > summaryList.width - 15) {
						entryWidget.setWidth(summaryList.width - 15);
					}
					List<StringTooltipComponent> foo = listeningClasses.stream().map(StringTooltipComponent::gray).toList();
					entryWidget.setTooltipElements(foo);
					eventList.addListEntry(entryWidget);
				}
			}
			eventList.setWidth(this.width - 8);
			eventList.setHeight(this.height - 40);
		}

		if(modInfo.modifiedClasses().isPresent() && !modInfo.modifiedClasses().get().isEmpty()) {
			var swordImage = new WidgetImage(ResourceLocation.withDefaultNamespace("textures/item/iron_sword.png"));
			swordImage.setSize(16, 16);

			tabs.addPage(mixinList, swordImage, List.of(Component.translatable("whodoesthat.gui.mixins")));
			mixinList.setWidth(this.width - 8);
			mixinList.setHeight(this.height - 40);

			List<String> mixins = modInfo.modifiedClasses().get();
			mixins.sort(String::compareTo);
			for(String mixin : mixins) {
				var entryWidget = new SelectableTextWidget(mixin);
				entryWidget.autoWidth();
				if(entryWidget.width > mixinList.width - 15) {
					entryWidget.setWidth(mixinList.width - 15);
				}
				mixinList.addListEntry(entryWidget);
			}


		}

		var depsWithoutDisabled = modInfo.dependenciesWithoutDisabledMods();
		if(!depsWithoutDisabled.isEmpty()) {
			var bookImage = new WidgetImage(ResourceLocation.withDefaultNamespace("textures/item/book.png"));
			bookImage.setSize(16, 16);

			tabs.addPage(dependenciesList, bookImage, List.of(Component.translatable("whodoesthat.gui.dependencies")));
			dependenciesList.setWidth(this.width - 8);
			dependenciesList.setHeight(this.height - 40);

			List<String> dependencies = new ArrayList<>(depsWithoutDisabled.keySet());
			dependencies.sort(String::compareTo);
			for(String dependency : dependencies) {
				String version = depsWithoutDisabled.get(dependency);
				var entryWidget = new SelectableTextWidget(dependency + " " + version);
				entryWidget.autoWidth();
				if(entryWidget.width > summaryList.width - 15) {
					entryWidget.setWidth(summaryList.width - 15);
				}
				dependenciesList.addListEntry(entryWidget);
			}

		}

		if(!modInfo.inheritance().isEmpty()) {
			var eggImage = new WidgetImage(ResourceLocation.withDefaultNamespace("textures/item/egg.png"));
			eggImage.setSize(16, 16);

			tabs.addPage(inheritanceList, eggImage, List.of(Component.translatable("whodoesthat.gui.inheritance")));
			inheritanceList.setWidth(this.width - 8);
			inheritanceList.setHeight(this.height - 40);

			Map<String, List<String>> entries = modInfo.inheritance().get();
			List<String> keys = new ArrayList<>(entries.keySet());
			keys.sort(String::compareTo);
			for(String key : keys) {
				var entryWidget = new SelectableTextWidget(key);
				entryWidget.setTextColor(ChatFormatting.DARK_BLUE.getColor());
				entryWidget.autoWidth();
				if(entryWidget.width > inheritanceList.width - 15) {
					entryWidget.setWidth(inheritanceList.width - 15);
				}
				inheritanceList.addListEntry(entryWidget);

				List<String> values = entries.get(key);
				values.sort(String::compareTo);
				for(String value : values) {
					String shortenedClassName = value.replaceFirst(basePackage + "\\.", "");
					var inheritedByWidget = new SelectableTextWidget(" - " + shortenedClassName);
					inheritedByWidget.autoWidth();
					if(inheritedByWidget.width > inheritanceList.width - 15) {
						inheritedByWidget.setWidth(inheritanceList.width - 15);
					}
					inheritanceList.addListEntry(inheritedByWidget);
				}
				inheritanceList.addListEntry(new SelectableTextWidget(""));
			}


		}

		tabs.setActivePage(0);
		this.setVisible(true);
	}

}
