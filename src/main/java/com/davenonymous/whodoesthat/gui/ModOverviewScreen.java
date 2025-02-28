package com.davenonymous.whodoesthat.gui;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.data.AllModsAnalyzer;
import com.davenonymous.whodoesthat.data.result.ModAnalysisResult;
import com.davenonymous.whodoesthat.lib.gui.GUI;
import com.davenonymous.whodoesthat.lib.gui.WidgetScreen;
import com.davenonymous.whodoesthat.lib.gui.event.ListSelectionEvent;
import com.davenonymous.whodoesthat.lib.gui.event.UpdateScreenEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetList;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTextBox;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModOverviewScreen extends WidgetScreen {
	Window window;
	int lastWindowWidth;
	int lastWindowHeight;
	Set<String> mustHave = new HashSet<>();
	Set<String> mustNotHave = new HashSet<>();
	DetailWidget detailWidget;
	WidgetList modList;

	public ModOverviewScreen() {
		super(Component.translatable("whodoesthat.gui.overview.title"));
		this.window = Minecraft.getInstance().getWindow();

		this.width = window.getGuiScaledWidth();
		this.height = window.getGuiScaledHeight();
		this.lastWindowWidth = this.width;
		this.lastWindowHeight = this.height;
		this.detailWidget = new DetailWidget();
	}

	private void buildModList(GUI gui) {
		gui.remove(this.modList);
		gui.remove(this.detailWidget);
		int listWidth = this.width * 2 / 3;

		detailWidget.setDimensions(this.width - listWidth - 5 + 121, 10, listWidth - 125, this.height - 27);
		detailWidget.setVisible(false);
		gui.add(detailWidget);

		modList = new WidgetList();
		modList.padding = 4;
		modList.setDimensions(
			this.width - listWidth - 5, 15,
			listWidth, this.height - 27
		);

		modList.addListener(
			ListSelectionEvent.class, (event, widget) -> {
				boolean hasSelection = modList.getSelectedWidget() != null;

				modList.forAllChildren(child -> {
					if(child instanceof LightModWidget) {
						((LightModWidget) child).setMinimalMode(hasSelection);
					}
				});

				if(hasSelection) {
					modList.setWidth(118);
					LightModWidget selected = (LightModWidget) modList.getSelectedWidget();
					String selectedModId = selected.modId;

					detailWidget.setMod(selectedModId, selected.modInfo);

				} else {
					modList.setWidth(listWidth);
					detailWidget.setVisible(false);
				}

				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
		gui.add(modList);

		var modsLabel = new WidgetTextBox(I18n.get("whodoesthat.gui.mods"));
		modsLabel.setPosition(this.width - listWidth - 3, 6);
		modsLabel.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		modsLabel.autoWidth();
		gui.add(modsLabel);

		int yOffset = 0;
		List<String> modIds = new ArrayList<>(WhoDoesThat.LAST_ANALYSIS.modAnalysisResults().keySet());
		modIds.sort(String::compareTo);

		for(var modId : modIds) {
			ModAnalysisResult results = WhoDoesThat.LAST_ANALYSIS.modAnalysisResults().get(modId);

			boolean mustHaveAll = mustHave.stream().allMatch(results.summarySet()::contains);
			boolean mustNotHaveAny = mustNotHave.stream().noneMatch(results.summarySet()::contains);
			if(!mustHaveAll || !mustNotHaveAny) {
				continue;
			}

			var lightModWidget = new LightModWidget(modId, results, listWidth - 15);
			lightModWidget.setPosition(3, yOffset);
			modList.addListEntry(lightModWidget);

			yOffset += lightModWidget.height;
		}
	}

	private void buildGuiContent(GUI gui) {
		gui.clear();

		int listWidth = this.width * 2 / 3;
		int filterWidth = this.width - listWidth - 10;

		var filterList = new WidgetList();
		filterList.padding = 4;
		filterList.setDimensions(
			4, 15,
			filterWidth, this.height - 27
		);
		gui.add(filterList);

		var filterLabel = new WidgetTextBox(I18n.get("whodoesthat.gui.filter"));
		filterLabel.setPosition(6, 6);
		filterLabel.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		filterLabel.autoWidth();
		gui.add(filterLabel);

		var closeButton = new CloseButtonWidget();
		closeButton.setPosition(this.width - 14, 4);
		gui.add(closeButton);

		for(String description : WhoDoesThat.LAST_ANALYSIS.getSummaryColumns()) {
			var filterEntryWidget = new FilterEntryWidget(description);
			if(mustHave.contains(description)) {
				filterEntryWidget.setState(FilterEntryWidget.FilterEntryState.MUST);
			} else if(mustNotHave.contains(description)) {
				filterEntryWidget.setState(FilterEntryWidget.FilterEntryState.NOT);
			}
			filterEntryWidget.addCheckboxListener(
				(event, widget) -> {
					FilterEntryWidget.FilterEntryState state = event.newValue;
					mustHave.remove(description);
					mustNotHave.remove(description);
					if(state == FilterEntryWidget.FilterEntryState.MUST) {
						mustHave.add(description);
					} else if(state == FilterEntryWidget.FilterEntryState.NOT) {
						mustNotHave.add(description);
					}

					buildModList(gui);

					return WidgetEventResult.HANDLED;
				}
			);
			filterEntryWidget.setWidth(filterWidth - 15);
			filterList.addListEntry(filterEntryWidget);
		}

		buildModList(gui);
	}

	@Override
	protected GUI createGUI() {
		if(WhoDoesThat.LAST_ANALYSIS == null) {
			AllModsAnalyzer.generateModInfoFilesLogged();
		}

		GUI gui = new GUI(0, 0, this.width, this.height);
		gui.addListener(
			UpdateScreenEvent.class, (event, widget) -> {
				if(this.lastWindowWidth != this.window.getGuiScaledWidth() || this.lastWindowHeight != this.window.getGuiScaledHeight()) {
					this.lastWindowWidth = this.window.getGuiScaledWidth();
					this.lastWindowHeight = this.window.getGuiScaledHeight();
					this.width = this.lastWindowWidth;
					this.height = this.lastWindowHeight;
					gui.setWidth(this.width);
					gui.setHeight(this.height);
					buildGuiContent(gui);
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
		buildGuiContent(gui);

		return gui;
	}
}
