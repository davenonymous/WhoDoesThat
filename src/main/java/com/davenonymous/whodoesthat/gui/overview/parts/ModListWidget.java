package com.davenonymous.whodoesthat.gui.overview.parts;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.gui.overview.ModOverviewScreen;
import com.davenonymous.whodoesthat.gui.overview.ModWidget;
import com.davenonymous.whodoesthat.lib.gui.event.ListSelectionEvent;
import com.davenonymous.whodoesthat.lib.gui.event.MouseClickEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetSizeChangeEvent;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetList;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetNativeWidget;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetPanel;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTextBox;
import com.davenonymous.whodoesthatlib.api.descriptors.ISummaryDescription;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import com.davenonymous.whodoesthatlib.api.result.IModInfo;
import com.davenonymous.whodoesthatlib.api.result.IModdedJarInfo;
import com.davenonymous.whodoesthatlib.api.result.asm.IClassInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.*;

public class ModListWidget extends WidgetPanel {
	private WidgetList modList;
	private WidgetTextBox modsLabel;
	private WidgetNativeWidget<EditBox> searchField;
	private ModOverviewScreen modOverviewScreen;
	private int regularWidth = 118;
	private String searchString = "";
	private Set<ModListSearchables> includeInSearch = new HashSet<>(List.of(
		ModListSearchables.ID,
		ModListSearchables.NAME,
		ModListSearchables.AUTHORS,
		ModListSearchables.DESCRIPTION
	));
	private boolean searchCompareCase = false;
	private boolean searchWithRegex = false;
	private Map<IModdedJarInfo<?>, String> searchTextCache = new HashMap<>();
	private WidgetBadge caseSensitiveBadge;
	private WidgetBadge searchRegexBadge;
	private WidgetBadge searchLocalizationsBadge;
	private WidgetBadge searchFilesBadge;
	private WidgetBadge searchClassesBadge;

	public ModListWidget(ModOverviewScreen modOverviewScreen) {
		super();

		this.modOverviewScreen = modOverviewScreen;

		modsLabel = new WidgetTextBox(I18n.get("whodoesthat.gui.mods"));
		modsLabel.setPosition(0, 0);
		modsLabel.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		modsLabel.autoWidth();
		this.add(modsLabel);

		var editBox = new EditBox(Minecraft.getInstance().font, 0, 0, Component.translatable("fml.menu.mods.search"));
		editBox.setPosition(2, 2);
		editBox.setValue("");
		editBox.setEditable(true);
		editBox.active = true;
		editBox.setBordered(false);
		editBox.setTextColor(0x5c5c5c);
		editBox.setTextShadow(false);
		editBox.setHint(Component.translatable("fml.menu.mods.search"));
		editBox.setResponder(queryString -> {
			if(queryString.isBlank() || !editBox.isFocused()) {
				editBox.setTextColor(0x5c5c5c);
			} else {
				editBox.setTextColor(ChatFormatting.WHITE.getColor());
			}
			this.searchString = queryString;
			this.fillList();
		});

		searchField = new WidgetNativeWidget<>(editBox);
		searchField.setDrawBackground(true);
		searchField.setPosition(modsLabel.width + 2, -2);
		searchField.setHeight(12);
		searchField.addListener(
			MouseClickEvent.class, (event, widget) -> {
				if(!event.isLeftClick()) {
					editBox.setValue("");
				}


				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
		this.add(searchField);

		caseSensitiveBadge = new WidgetBadge("aA", 0xFF173f5f);
		caseSensitiveBadge.addListener(
			MouseClickEvent.class, (event, widget) -> {
				searchCompareCase = !searchCompareCase;
				caseSensitiveBadge.active = searchCompareCase;
				this.fillList();
				return WidgetEventResult.HANDLED;
			}
		);
		caseSensitiveBadge.active = searchCompareCase;
		caseSensitiveBadge.first = true;
		caseSensitiveBadge.setVisible(false);
		caseSensitiveBadge.setTooltipLines(Component.translatable("whodoesthat.gui.search.case_sensitive"));
		this.add(caseSensitiveBadge);

		searchRegexBadge = new WidgetBadge(".*", 0xFFF6D55C);
		searchRegexBadge.addListener(
			MouseClickEvent.class, (event, widget) -> {
				searchWithRegex = !searchWithRegex;
				searchRegexBadge.active = searchWithRegex;
				this.fillList();
				return WidgetEventResult.HANDLED;
			}
		);
		searchRegexBadge.active = searchWithRegex;
		searchRegexBadge.setVisible(false);
		searchRegexBadge.setTooltipLines(Component.translatable("whodoesthat.gui.search.regex"));
		this.add(searchRegexBadge);

		searchLocalizationsBadge = new WidgetBadge("i18n", 0xFF20639b);
		searchLocalizationsBadge.addListener(
			MouseClickEvent.class, (event, widget) -> {
				if(includeInSearch.contains(ModListSearchables.TRANSLATIONS)) {
					includeInSearch.remove(ModListSearchables.TRANSLATIONS);
					searchLocalizationsBadge.active = false;
				} else {
					includeInSearch.add(ModListSearchables.TRANSLATIONS);
					searchLocalizationsBadge.active = true;
				}
				searchTextCache.clear();
				this.fillList();
				return WidgetEventResult.HANDLED;
			}
		);
		searchLocalizationsBadge.active = includeInSearch.contains(ModListSearchables.TRANSLATIONS);
		searchLocalizationsBadge.setVisible(false);
		searchLocalizationsBadge.setTooltipLines(Component.translatable("whodoesthat.gui.search.localizations"));
		this.add(searchLocalizationsBadge);

		searchFilesBadge = new WidgetBadge("C:\\", 0xFFED553B);
		searchFilesBadge.addListener(
			MouseClickEvent.class, (event, widget) -> {
				if(includeInSearch.contains(ModListSearchables.FILE_NAMES)) {
					includeInSearch.remove(ModListSearchables.FILE_NAMES);
					searchFilesBadge.active = false;
				} else {
					includeInSearch.add(ModListSearchables.FILE_NAMES);
					searchFilesBadge.active = true;
				}
				searchTextCache.clear();
				this.fillList();
				return WidgetEventResult.HANDLED;
			}
		);
		searchFilesBadge.active = includeInSearch.contains(ModListSearchables.FILE_NAMES);
		searchFilesBadge.setVisible(false);
		searchFilesBadge.setTooltipLines(Component.translatable("whodoesthat.gui.search.files"));
		this.add(searchFilesBadge);

		searchClassesBadge = new WidgetBadge("Java", 0xFF3CAEA3);
		searchClassesBadge.addListener(
			MouseClickEvent.class, (event, widget) -> {
				if(includeInSearch.contains(ModListSearchables.CLASS_NAMES)) {
					includeInSearch.remove(ModListSearchables.CLASS_NAMES);
					searchClassesBadge.active = false;
				} else {
					includeInSearch.add(ModListSearchables.CLASS_NAMES);
					searchClassesBadge.active = true;
				}
				searchTextCache.clear();
				this.fillList();
				return WidgetEventResult.HANDLED;
			}
		);
		searchClassesBadge.active = includeInSearch.contains(ModListSearchables.CLASS_NAMES);
		searchClassesBadge.setVisible(false);
		searchClassesBadge.last = true;
		searchClassesBadge.setTooltipLines(Component.translatable("whodoesthat.gui.search.classes"));
		this.add(searchClassesBadge);

		modList = new WidgetList();
		modList.setPosition(0, modsLabel.height + 2);
		modList.padding = 4;
		this.add(modList);

		this.addListener(
			WidgetSizeChangeEvent.class, (event, widget) -> {
				updateWidgetSizes();

				//modList.setDimensions(0, modsLabel.height, event.newWidth(), event.newHeight());
				//regularWidth = event.newWidth() != 118 ? event.newWidth() : regularWidth;
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		var self = this;
		modList.addListener(
			ListSelectionEvent.class, (event, widget) -> {
				boolean hasSelection = modList.getSelectedWidget() != null;

				modList.forAllChildren(child -> {
					if(child instanceof ModWidget) {
						((ModWidget) child).setMinimalMode(hasSelection);
					}
				});

				if(hasSelection) {
					modList.setWidth(118);
					searchField.setVisible(false);
					caseSensitiveBadge.setVisible(false);
					searchRegexBadge.setVisible(false);
					searchLocalizationsBadge.setVisible(false);
					searchClassesBadge.setVisible(false);
					searchFilesBadge.setVisible(false);
					ModWidget selected = (ModWidget) modList.getSelectedWidget();

					modOverviewScreen.detailWidget.setMod(selected.modInfo, selected.jarInfo);
				} else {
					int listWidth = modOverviewScreen.width * 2 / 3;
					modList.setWidth(listWidth);

					modOverviewScreen.detailWidget.setVisible(false);
					searchField.setVisible(true);
					caseSensitiveBadge.setVisible(true);
					searchRegexBadge.setVisible(true);
					searchLocalizationsBadge.setVisible(true);
					searchClassesBadge.setVisible(true);
					searchFilesBadge.setVisible(true);
				}


				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		updateWidgetSizes();
	}

	private void updateWidgetSizes() {
		int listWidth = modOverviewScreen.width * 2 / 3;
		var detailWidget = modOverviewScreen.detailWidget;
		if(detailWidget.isVisible()) {
			modList.setSize(118, this.height);
			searchField.setVisible(false);
			caseSensitiveBadge.setVisible(false);
			searchRegexBadge.setVisible(false);
			searchLocalizationsBadge.setVisible(false);
			searchClassesBadge.setVisible(false);
			searchFilesBadge.setVisible(false);
		} else {
			modList.setSize(listWidth, this.height);
			int searchFieldWidth = listWidth - modsLabel.width - 2 - 16;
			searchFieldWidth -= caseSensitiveBadge.width + searchRegexBadge.width + searchLocalizationsBadge.width;
			searchFieldWidth -= searchFilesBadge.width + searchClassesBadge.width;
			searchField.setWidth(searchFieldWidth);
			searchField.setVisible(true);
			caseSensitiveBadge.setPosition(searchField.x + searchField.width, searchField.y);
			caseSensitiveBadge.setVisible(true);
			searchRegexBadge.setPosition(caseSensitiveBadge.x + caseSensitiveBadge.width, searchField.y);
			searchRegexBadge.setVisible(true);
			searchLocalizationsBadge.setPosition(searchRegexBadge.x + searchRegexBadge.width, searchField.y);
			searchLocalizationsBadge.setVisible(true);
			searchFilesBadge.setPosition(searchLocalizationsBadge.x + searchLocalizationsBadge.width, searchField.y);
			searchFilesBadge.setVisible(true);
			searchClassesBadge.setPosition(searchFilesBadge.x + searchFilesBadge.width, searchField.y);
			searchClassesBadge.setVisible(true);
		}
	}

	public ModListWidget fillList() {
		modList.clear();

		Set<String> modIds = new HashSet<>();

		List<ModWidget> sortedModWidgets = new ArrayList<>();
		for(IJarInfo jarInfo : WhoDoesThat.LAST_ANALYSIS.jars()) {
			Set<ISummaryDescription> jarHasProperties = jarInfo.getSummaries().keySet();
			boolean mustHaveAllTags = jarInfo.getTags().containsAll(modOverviewScreen.mustHaveTags);
			boolean mustNotHaveAnyTags = modOverviewScreen.mustNotHaveTags.stream().noneMatch(jarInfo.getTags()::contains);
			boolean mustHaveAll = jarHasProperties.containsAll(modOverviewScreen.mustHave);
			boolean mustNotHaveAny = modOverviewScreen.mustNotHave.stream().noneMatch(jarHasProperties::contains);
			if(!mustHaveAll || !mustNotHaveAny || !mustHaveAllTags || !mustNotHaveAnyTags) {
				continue;
			}

			if(jarInfo instanceof IModdedJarInfo<?> moddedJarInfo) {
				for(IModInfo modInfo : moddedJarInfo.mods()) {
					if(modIds.contains(modInfo.modId())) {
						continue;
					}
					if(!searchTextCache.containsKey(moddedJarInfo)) {
						StringBuilder searchStringBuilder = new StringBuilder();
						if(includeInSearch.contains(ModListSearchables.ID)) {
							searchStringBuilder.append(modInfo.modId());
						}
						if(includeInSearch.contains(ModListSearchables.NAME)) {
							searchStringBuilder.append(modInfo.displayName());
						}
						if(includeInSearch.contains(ModListSearchables.DESCRIPTION)) {
							searchStringBuilder.append(modInfo.description());
						}
						if(includeInSearch.contains(ModListSearchables.AUTHORS)) {
							modInfo.authors().ifPresent(searchStringBuilder::append);
						}
						if(includeInSearch.contains(ModListSearchables.TRANSLATIONS)) {
							for(String localization : jarInfo.getLocalizations()) {
								searchStringBuilder.append(localization);
							}
						}
						if(includeInSearch.contains(ModListSearchables.JAR_NAME)) {
							searchStringBuilder.append(jarInfo.jar().getFileName());
						}
						if(includeInSearch.contains(ModListSearchables.FILE_NAMES)) {
							for(Path file : jarInfo.files()) {
								searchStringBuilder.append(file.getFileName().toString());
							}
						}
						if(includeInSearch.contains(ModListSearchables.CLASS_NAMES)) {
							searchStringBuilder.append(jarInfo.getShortestCommonPackage());
							for(IClassInfo classInfo : jarInfo.getClasses()) {
								searchStringBuilder.append(classInfo.getSimpleName());
							}
						}

						String searchText = searchCompareCase ? searchStringBuilder.toString() : searchStringBuilder.toString().toLowerCase(Locale.ROOT);
						searchTextCache.put(moddedJarInfo, searchText);
					}

					String searchText = searchTextCache.get(moddedJarInfo);
					if(!searchString.isBlank()) {
						String searchQuery = searchCompareCase ? searchString : searchString.toLowerCase(Locale.ROOT);

						boolean found = false;
						if(searchWithRegex) {
							try {
								found = searchText.matches(searchQuery);
							} catch (Exception ignore) {
							}
						} else {
							found = searchText.contains(searchQuery);
						}

						if(!found) {
							continue;
						}
					}
					modIds.add(modInfo.modId());

					var modWidget = new ModWidget(moddedJarInfo, modInfo, this.modList.width - 15);
					sortedModWidgets.add(modWidget);
				}
			}
		}

		int yOffset = 0;
		sortedModWidgets.sort(Comparator.comparing(modWidget -> modWidget.modInfo().displayName().toLowerCase(Locale.ROOT), Comparator.naturalOrder()));
		for(ModWidget modWidget : sortedModWidgets) {
			modWidget.setPosition(3, yOffset);
			modList.addListEntry(modWidget);
			yOffset += modWidget.height;
		}
		modList.scrollToTop();

		return this;
	}

	public ModListWidget deselect() {
		modList.deselect();
		return this;
	}

	public ModListWidget unloadAll() {
		modList.children().forEach(w -> w.setVisible(false));
		return this;
	}
}
