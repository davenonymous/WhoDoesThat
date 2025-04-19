package com.davenonymous.whodoesthat.gui.overview;

import com.davenonymous.whodoesthat.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.whodoesthatlib.api.descriptors.*;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import com.davenonymous.whodoesthatlib.api.result.IModdedJarInfo;
import net.minecraft.client.resources.language.I18n;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SummaryFilterEntryWidget extends FilterEntryWidget {
	private ISummaryDescription summary;

	public SummaryFilterEntryWidget(ISummaryDescription summary, Set<IJarInfo> iJarInfos) {
		super(summary.description() + " §7[" + iJarInfos.size() + "]§r");
		this.height = 13;
		this.summary = summary;

		String header = I18n.get("whodoesthat.gui.descriptors." + summary.configKey() + ".label");
		summaryWidget.addTooltipElement(WrappedStringTooltipComponent.white(header));

		String summaryTranslationKey = "whodoesthat.gui.descriptors." + summary.configKey() + ".tooltip";
		String translatedSummary = null;
		if(summary instanceof InheritanceDescription inheritance) {
			translatedSummary = I18n.get(summaryTranslationKey, inheritance.getParentClassName());
		} else if(summary instanceof MixinDescription mixin) {
			translatedSummary = I18n.get(summaryTranslationKey, mixin.getTargetClassName());
		} else if(summary instanceof FileGlobDescription glob) {
			translatedSummary = I18n.get(summaryTranslationKey, glob.getPathGlob());
		} else if(summary instanceof EventDescription event) {
			translatedSummary = I18n.get(summaryTranslationKey, event.getEventClassName().substring(event.getEventClassName().lastIndexOf('.') + 1));
		} else if(summary instanceof FieldTypeDescription fieldType) {
			translatedSummary = I18n.get(summaryTranslationKey, fieldType.getFieldClassName());
		} else if(summary instanceof AnnotationDescription annotation) {
			translatedSummary = I18n.get(summaryTranslationKey, annotation.getAnnotationClassName());
		}
		if(translatedSummary != null) {
			summaryWidget.addTooltipElement(WrappedStringTooltipComponent.gray(translatedSummary));
		}

		if(iJarInfos != null && !iJarInfos.isEmpty()) {
			Set<String> modNames = new HashSet<>();
			for(IJarInfo jarInfo : iJarInfos) {
				if(jarInfo instanceof IModdedJarInfo<?> neoJar) {
					if(neoJar.mods().isEmpty()) {
						continue;
					}

					for(var mod : neoJar.mods()) {
						modNames.add(mod.displayName());
					}
				}
			}
			summaryWidget.addTooltipElement(WrappedStringTooltipComponent.yellow(modNames.stream().sorted(Comparator.naturalOrder()).collect(Collectors.joining("\n"))));
		}
	}

	public boolean isChecked(ModOverviewScreen modOverviewScreen) {
		return modOverviewScreen.mustHave.contains(summary) || modOverviewScreen.mustNotHave.contains(summary);
	}
}
