package com.davenonymous.whodoesthat.gui.overview.parts;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.gui.overview.FilterEntryWidget;
import com.davenonymous.whodoesthat.gui.overview.ModOverviewScreen;
import com.davenonymous.whodoesthat.gui.overview.SummaryFilterEntryWidget;
import com.davenonymous.whodoesthat.gui.widgets.SelectableTextWidget;
import com.davenonymous.whodoesthat.lib.gui.event.MouseClickEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetSizeChangeEvent;
import com.davenonymous.whodoesthat.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.widgets.*;
import com.davenonymous.whodoesthatlib.api.descriptors.ISummaryDescription;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class FilterListWidget extends WidgetPanel {
	private WidgetTree filterList;
	private WidgetTextBox filterLabel;
	private WidgetNativeWidget<EditBox> searchField;

	private ModOverviewScreen modOverviewScreen;
	private String searchString = "";

	public FilterListWidget(ModOverviewScreen modOverviewScreen) {
		super();

		this.modOverviewScreen = modOverviewScreen;

		filterLabel = new WidgetTextBox(I18n.get("whodoesthat.gui.filter"));
		filterLabel.setPosition(0, 0);
		filterLabel.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		filterLabel.autoWidth();
		this.add(filterLabel);

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
			this.filterList.setFilter(node -> {
				Widget widgetToCheck = node.widget();
				if(widgetToCheck instanceof SummaryFilterEntryWidget filterEntryWidget) {
					widgetToCheck = filterEntryWidget.summaryWidget();
					if(filterEntryWidget.isChecked(modOverviewScreen)) {
						return true;
					}
				}

				if(widgetToCheck instanceof WidgetTextBox filterEntryWidget) {
					String text = filterEntryWidget.getText();
					if(text == null) {
						return false;
					}
					if(text.isBlank()) {
						return true;
					}
					return text.toLowerCase().contains(queryString.toLowerCase());
				}

				return false;
			});
			this.filterList.buildListFromRootNodes();
			this.filterList.scrollToTop();
			this.filterList.scrollUp();
		});

		searchField = new WidgetNativeWidget<>(editBox);
		searchField.setDrawBackground(true);
		searchField.setPosition(filterLabel.width + 2, -2);
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

		filterList = new WidgetTree();
		filterList.setPosition(0, filterLabel.height + 2);
		filterList.padding = 4;
		filterList.setIndentPixels(0);

		this.addListener(
			WidgetSizeChangeEvent.class, (event, widget) -> {
				filterList.setSize(event.newWidth(), event.newHeight());
				searchField.setWidth(event.newWidth() - filterLabel.width - 18);
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		this.addListener(
			MouseClickEvent.class, (event, widget) -> {
				filterList.fireEvent(event);
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		this.add(filterList);
	}

	public FilterListWidget fillList() {
		Map<ISummaryDescription, Set<IJarInfo>> summariesInJars = WhoDoesThat.LAST_ANALYSIS.getUsedDescriptors();

		Map<String, List<ISummaryDescription>> summariesByCategory = WhoDoesThat.LAST_ANALYSIS.getDescriptors().values().stream()
			.flatMap(List::stream)
			.filter(summariesInJars::containsKey)
			.collect(
				Collectors.groupingBy(
					description -> {
						return I18n.get("whodoesthat.gui.categories." + description.resultCategory() + ".label");
					},
					Collectors.toList()
				)
			);


		List<String> categories = summariesByCategory.keySet().stream().sorted(Comparator.naturalOrder()).toList();
		for(String category : categories) {
			var categoryLabelWidget = new SelectableTextWidget(category);
			categoryLabelWidget.setWordWrap(true);
			categoryLabelWidget.setStyle(style -> style.withBold(true).withColor(ChatFormatting.WHITE));
			categoryLabelWidget.autoWidth();
			categoryLabelWidget.setHeight(14);

			if(categoryLabelWidget.width >= this.width - 15) {
				categoryLabelWidget.setWidth(this.width - 15);
			}

			WidgetTreeNode categoryNode = filterList.addRootNode(categoryLabelWidget);
			categoryNode.setExtensionButtonProvider(() -> {
				WidgetTextBox textButton = new WidgetTextBox(categoryNode.isExpanded() ? "-" : "+");
				textButton.setStyle(style -> style.withColor(ChatFormatting.WHITE).withBold(true));
				textButton.setSize(10, 9);
				return textButton;
			});


			List<ISummaryDescription> summaries = summariesByCategory.get(category);
			summaries.sort(Comparator.comparing(description -> description.description().toLowerCase(Locale.ROOT), Comparator.naturalOrder()));
			for(ISummaryDescription description : summaries) {
				String descriptionName = description.description();
				if(descriptionName == null) {
					continue;
				}

				FilterEntryWidget filterEntryWidget = getFilterEntryWidget(description, summariesInJars.get(description));
				categoryNode.addChild(filterEntryWidget);
			}

			var entryWidget = new SelectableTextWidget(" ");
			entryWidget.autoWidth();
			filterList.addRootNode(entryWidget);
		}


		List<String> foundTags = WhoDoesThat.LAST_ANALYSIS.jars().stream()
			.map(IJarInfo::getTags)
			.flatMap(List::stream)
			.collect(Collectors.toSet()).stream()
			.sorted(Comparator.naturalOrder())
			.toList();

		var tagsLabelWidget = new SelectableTextWidget(I18n.get("whodoesthat.gui.categories.tags.label"));
		tagsLabelWidget.setWordWrap(true);
		tagsLabelWidget.setStyle(style -> style.withBold(true).withColor(ChatFormatting.WHITE));
		tagsLabelWidget.autoWidth();
		tagsLabelWidget.setHeight(14);
		if(tagsLabelWidget.width >= this.width - 15) {
			tagsLabelWidget.setWidth(this.width - 15);
		}
		var tagCategoryNode = filterList.addRootNode(tagsLabelWidget);
		tagCategoryNode.setExtensionButtonProvider(() -> {
			WidgetTextBox textButton = new WidgetTextBox(tagCategoryNode.isExpanded() ? "-" : "+");
			textButton.setStyle(style -> style.withColor(ChatFormatting.WHITE).withBold(true));
			textButton.setSize(10, 9);
			textButton.setY(tagsLabelWidget.height > 12 ? tagsLabelWidget.height - 10 : 4);
			return textButton;
		});

		for(String tag : foundTags) {
			FilterEntryWidget filterEntryWidget = getFilterEntryWidget(tag);
			tagCategoryNode.addChild(filterEntryWidget);

			var descriptions = WhoDoesThat.LAST_ANALYSIS.getSummaryDescriptionsByTag(tag);
			if(descriptions.isEmpty()) {
				continue;
			}

			filterEntryWidget.summaryWidget().addTooltipElement(WrappedStringTooltipComponent.yellow(I18n.get("whodoesthat.gui.tag.tooltip")));
			for(var description : descriptions) {
				filterEntryWidget.summaryWidget().addTooltipElement(
					WrappedStringTooltipComponent.gray(description.description())
				);
			}
		}

		this.filterList.buildListFromRootNodes();

		return this;
	}

	private @NotNull FilterEntryWidget getFilterEntryWidget(String tag) {
		FilterEntryWidget filterEntryWidget = new FilterEntryWidget(tag);
		filterEntryWidget.addCheckboxListener(
			(event, widget) -> {
				FilterEntryWidget.FilterEntryState state = event.newValue;
				modOverviewScreen.mustHaveTags.remove(tag);
				modOverviewScreen.mustNotHaveTags.remove(tag);
				if(state == FilterEntryWidget.FilterEntryState.MUST) {
					modOverviewScreen.mustHaveTags.add(tag);
				} else if(state == FilterEntryWidget.FilterEntryState.NOT) {
					modOverviewScreen.mustNotHaveTags.add(tag);
				}

				// TODO: Update mod list
				modOverviewScreen.updateModList();

				return WidgetEventResult.HANDLED;
			}
		);
		return filterEntryWidget;
	}

	private @NotNull FilterEntryWidget getFilterEntryWidget(ISummaryDescription description, Set<IJarInfo> iJarInfos) {
		FilterEntryWidget filterEntryWidget = new SummaryFilterEntryWidget(description, iJarInfos);
		filterEntryWidget.addCheckboxListener(
			(event, widget) -> {
				FilterEntryWidget.FilterEntryState state = event.newValue;
				modOverviewScreen.mustHave.remove(description);
				modOverviewScreen.mustNotHave.remove(description);
				if(state == FilterEntryWidget.FilterEntryState.MUST) {
					modOverviewScreen.mustHave.add(description);
				} else if(state == FilterEntryWidget.FilterEntryState.NOT) {
					modOverviewScreen.mustNotHave.add(description);
				}

				// TODO: Update mod list
				modOverviewScreen.updateModList();

				return WidgetEventResult.HANDLED;
			}
		);
		return filterEntryWidget;
	}
}
