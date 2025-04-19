package com.davenonymous.whodoesthat.gui.overview.detail.simple;

import com.davenonymous.whodoesthat.gui.widgets.SelectableTextWidget;
import com.davenonymous.whodoesthat.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.widgets.Widget;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetList;
import com.davenonymous.whodoesthatlib.api.descriptors.*;
import com.davenonymous.whodoesthatlib.api.result.IDependencyInfo;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import com.davenonymous.whodoesthatlib.api.result.asm.IClassInfo;
import com.davenonymous.whodoesthatlib.api.result.asm.IFieldInfo;
import com.davenonymous.whodoesthatlib.api.result.asm.IMethodInfo;
import net.minecraft.client.resources.language.I18n;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SummaryList extends WidgetList {
	IJarInfo jarInfo;

	public SummaryList(IJarInfo jarInfo) {
		this.jarInfo = jarInfo;
		this.padding = 4;
		this.setDrawBackground(false);
		this.setShowSelection(false);
	}

	public void createEntries(Set<IDependencyInfo> dependencies, Set<String> reverseDependencies) {
		List<IDependencyInfo> requiredDependencies = dependencies.stream()
			.filter(IDependencyInfo::mandatory)
			.sorted(Comparator.comparing(IDependencyInfo::modId))
			.toList();
		List<IDependencyInfo> optionalDependencies = dependencies.stream()
			.filter(dependency -> !dependency.mandatory())
			.sorted(Comparator.comparing(IDependencyInfo::modId))
			.toList();

		if(!requiredDependencies.isEmpty()) {
			var categoryLabelWidget = new SelectableTextWidget(I18n.get("whodoesthat.gui.dependencies.label"));
			categoryLabelWidget.setStyle(style -> style.withBold(true));
			categoryLabelWidget.autoWidth();
			this.addListEntry(categoryLabelWidget);

			for(IDependencyInfo dependency : requiredDependencies) {
				String version = dependency.versionRange().orElse("*");
				var entryWidget = new SelectableTextWidget("- " + dependency.modId() + " " + version);
				entryWidget.autoWidth();
				if(entryWidget.width > this.width - 15) {
					entryWidget.setWidth(this.width - 15);
				}
				this.addListEntry(entryWidget);
			}
		}

		if(!optionalDependencies.isEmpty()) {
			if(!requiredDependencies.isEmpty()) {
				this.addListEntry(new SelectableTextWidget(""));
			}

			var categoryLabelWidget = new SelectableTextWidget(I18n.get("whodoesthat.gui.optional_dependencies.label"));
			categoryLabelWidget.setStyle(style -> style.withBold(true));
			categoryLabelWidget.autoWidth();
			this.addListEntry(categoryLabelWidget);

			for(IDependencyInfo dependency : optionalDependencies) {
				String version = dependency.versionRange().orElse("*");
				var entryWidget = new SelectableTextWidget("- " + dependency.modId() + " " + version);
				entryWidget.autoWidth();
				if(entryWidget.width > this.width - 15) {
					entryWidget.setWidth(this.width - 15);
				}
				this.addListEntry(entryWidget);
			}
		}

		if(!reverseDependencies.isEmpty()) {
			if(!requiredDependencies.isEmpty() || !optionalDependencies.isEmpty()) {
				this.addListEntry(new SelectableTextWidget(""));
			}

			var categoryLabelWidget = new SelectableTextWidget(I18n.get("whodoesthat.gui.reverse_dependencies.label"));
			categoryLabelWidget.setStyle(style -> style.withBold(true));
			categoryLabelWidget.autoWidth();
			this.addListEntry(categoryLabelWidget);

			for(String modId : reverseDependencies) {
				var entryWidget = new SelectableTextWidget("- " + modId);
				entryWidget.autoWidth();
				if(entryWidget.width > this.width - 15) {
					entryWidget.setWidth(this.width - 15);
				}
				this.addListEntry(entryWidget);
			}
		}

		if(!dependencies.isEmpty() || !reverseDependencies.isEmpty()) {
			this.addListEntry(new SelectableTextWidget(""));
		}
	}

	public void createEntries(List<ISummaryDescription> sortedDescriptions) {
		Map<ISummaryDescription, List<Object>> rawSummaries = jarInfo.getSummaries();
		Map<String, List<ISummaryDescription>> summariesByCategory = sortedDescriptions.stream()
			.collect(Collectors.groupingBy(desc -> {
				if(desc.resultCategory() == null) {
					return "<empty>";
				}

				String categoryTranslationKey = "whodoesthat.gui.categories." + desc.resultCategory() + ".label";
				if(!I18n.exists(categoryTranslationKey)) {
					return desc.resultCategory();
				}

				return I18n.get(categoryTranslationKey);
			}));

		List<String> sortedCategoryTranslationKeys = new ArrayList<>(summariesByCategory.keySet());
		sortedCategoryTranslationKeys.sort(Comparator.naturalOrder());

		for(String category : sortedCategoryTranslationKeys) {
			var categoryLabelWidget = new SelectableTextWidget(category);
			categoryLabelWidget.setStyle(style -> style.withBold(true));
			categoryLabelWidget.autoWidth();

			if(categoryLabelWidget.width >= this.width - 15) {
				categoryLabelWidget.setWidth(this.width - 15);
			}
			this.addListEntry(categoryLabelWidget);

			List<ISummaryDescription> categorySummaries = summariesByCategory.get(category);
			categorySummaries.sort(Comparator.comparing(d -> d.description().toLowerCase(Locale.ROOT), Comparator.naturalOrder()));
			for(ISummaryDescription description : categorySummaries) {
				List<Object> extraData = rawSummaries.get(description);

				var entryWidget = new SelectableTextWidget("- " + description.description());
				entryWidget.autoWidth();
				if(entryWidget.width > this.width - 15) {
					entryWidget.setWidth(this.width - 15);
				}

				String tooltipLanguageKeyBase = "whodoesthat.gui.descriptors." + description.configKey();
				String label = tooltipLanguageKeyBase + ".label";
				String tooltip = tooltipLanguageKeyBase + ".tooltip";
				Consumer<String> addTooltip = i18nArg -> {
					entryWidget.addTooltipElement(WrappedStringTooltipComponent.white(I18n.get(label)));
					entryWidget.addTooltipElement(WrappedStringTooltipComponent.gray(I18n.get(tooltip, i18nArg)));
				};

				if(description instanceof InheritanceDescription inheritance) {
					addTooltip.accept(inheritance.getParentClassName());

					attachDescriptionTooltip(extraData, entryWidget, o -> ((IClassInfo) o).type().getClassName().substring(jarInfo.getShortestCommonPackage().length()));
				} else if(description instanceof MixinDescription mixin) {
					addTooltip.accept(mixin.getTargetClassName());

					attachDescriptionTooltip(extraData, entryWidget, o -> ((IClassInfo) o).type().getClassName().substring(jarInfo.getShortestCommonPackage().length()));
				} else if(description instanceof FileGlobDescription glob) {
					addTooltip.accept(glob.getPathGlob());

					attachDescriptionTooltip(extraData, entryWidget, o -> ((Path) o).toString());
				} else if(description instanceof UsedTypeDescription usedTypeDescription) {
					addTooltip.accept(usedTypeDescription.getTypeClassName());

					attachDescriptionTooltip(extraData, entryWidget, o -> ((IClassInfo) o).type().getClassName().substring(jarInfo.getShortestCommonPackage().length()));
				} else if(description instanceof CalledMethodDescription call) {
					addTooltip.accept(call.getMethodQuery());

					attachDescriptionTooltip(extraData, entryWidget, o -> ((IClassInfo) o).type().getClassName().substring(jarInfo.getShortestCommonPackage().length()));
				} else if(description instanceof EventDescription event) {
					addTooltip.accept(event.getEventClassName().substring(event.getEventClassName().lastIndexOf('.') + 1));

					attachDescriptionTooltip(extraData, entryWidget, o -> ((IMethodInfo) o).locatableName().substring(jarInfo.getShortestCommonPackage().length()));
				} else if(description instanceof FieldTypeDescription fieldType) {
					addTooltip.accept(fieldType.getFieldClassName());

					attachDescriptionTooltip(extraData, entryWidget, o -> ((IFieldInfo) o).locatableName().substring(jarInfo.getShortestCommonPackage().length()));
				} else if(description instanceof AnnotationDescription annotation) {
					addTooltip.accept(annotation.getAnnotationClassName());

					attachDescriptionTooltip(
						extraData, entryWidget, o -> {
							if(o instanceof IFieldInfo fieldInfo) {
								return fieldInfo.locatableName().substring(jarInfo.getShortestCommonPackage().length());
							} else if(o instanceof IMethodInfo methodInfo) {
								return methodInfo.locatableName().substring(jarInfo.getShortestCommonPackage().length());
							} else if(o instanceof IClassInfo classInfo) {
								return classInfo.type().getClassName().substring(jarInfo.getShortestCommonPackage().length());
							}

							return null;
						}
					);
				}
				this.addListEntry(entryWidget);
			}

			var entryWidget = new SelectableTextWidget(" ");
			entryWidget.autoWidth();
			this.addListEntry(entryWidget);
		}
	}

	private void attachDescriptionTooltip(List<Object> extraData, Widget entryWidget, Function<Object, String> tooltipFunction) {
		int limit = 8;
		for(var extra : extraData) {
			String supplied = tooltipFunction.apply(extra);
			if(supplied != null) {
				entryWidget.addTooltipElement(WrappedStringTooltipComponent.yellow(supplied));
				limit--;
				if(limit <= 0) {
					// TODO: Add "+ n more" tooltip line
					break;
				}
			}
		}
	}
}
