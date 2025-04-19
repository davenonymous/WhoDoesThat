package com.davenonymous.whodoesthat.gui.overview.detail.simple;

import com.davenonymous.whodoesthat.gui.overview.detail.AbstractResultList;
import com.davenonymous.whodoesthat.gui.widgets.SelectableTextWidget;
import com.davenonymous.whodoesthat.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import com.davenonymous.whodoesthatlib.api.result.IScanResult;
import com.davenonymous.whodoesthatlib.api.result.asm.IClassInfo;
import com.davenonymous.whodoesthatlib.api.result.mod.InheritanceResult;
import net.minecraft.client.resources.language.I18n;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InheritanceList extends AbstractResultList<InheritanceResult> {
	private final IScanResult scanResult;

	public InheritanceList(IJarInfo jarInfo, IScanResult scanResult) {
		super(jarInfo, InheritanceResult.class);
		this.scanResult = scanResult;
	}

	@Override
	public void createEntries() {
		Map<Type, List<IClassInfo>> inheritors = result.inheritance();
		Map<String, List<IClassInfo>> shortInheritors = inheritors.entrySet().stream()
			.collect(Collectors.toMap(e -> e.getKey().getClassName().substring(e.getKey().getClassName().lastIndexOf('.') + 1), Map.Entry::getValue));

		List<String> superNames = new ArrayList<>(shortInheritors.keySet());
		superNames.sort(Comparator.naturalOrder());
		for(String key : superNames) {
			var entryWidget = new SelectableTextWidget(key);
			entryWidget.setStyle(style -> style.withBold(true));
			entryWidget.autoWidth();
			if(entryWidget.width > this.width - 15) {
				entryWidget.setWidth(this.width - 15);
			}
			this.addListEntry(entryWidget);

			List<IClassInfo> values = shortInheritors.get(key);
			values.sort(Comparator.comparing(IClassInfo::getSimpleName));
			for(IClassInfo value : values) {
				var inheritedByWidget = new SelectableTextWidget(" - " + value.getSimpleName());
				inheritedByWidget.autoWidth();
				if(inheritedByWidget.width > this.width - 15) {
					inheritedByWidget.setWidth(this.width - 15);
				}

				var supers = scanResult.supersOf(value.getClassName()).stream().filter(s -> !s.equals("java.lang.Object")).sorted(Comparator.naturalOrder()).toList();
				if(supers.size() > 0) {
					var inheritanceLabelWidget = WrappedStringTooltipComponent.white(I18n.get("whodoesthat.gui.inheritance.tooltip", value.getClassName()));
					inheritedByWidget.addTooltipElement(inheritanceLabelWidget);

					for(String superClass : supers) {
						inheritedByWidget.addTooltipElement(WrappedStringTooltipComponent.yellow("- " + superClass));
					}
				}
				this.addListEntry(inheritedByWidget);
			}
			this.addListEntry(new SelectableTextWidget(""));
		}
	}
}
