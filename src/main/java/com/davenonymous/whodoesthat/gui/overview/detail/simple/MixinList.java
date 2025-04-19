package com.davenonymous.whodoesthat.gui.overview.detail.simple;

import com.davenonymous.whodoesthat.gui.overview.detail.AbstractResultList;
import com.davenonymous.whodoesthat.gui.widgets.SelectableTextWidget;
import com.davenonymous.whodoesthat.lib.gui.tooltip.HBoxTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import com.davenonymous.whodoesthatlib.api.result.asm.IAnnotationInfo;
import com.davenonymous.whodoesthatlib.api.result.asm.IClassInfo;
import com.davenonymous.whodoesthatlib.api.result.asm.IMethodInfo;
import com.davenonymous.whodoesthatlib.api.result.mod.MixinResult;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MixinList extends AbstractResultList<MixinResult> {

	public MixinList(IJarInfo jarInfo) {
		super(jarInfo, MixinResult.class);
	}

	@Override
	public void createEntries() {
		List<Type> mixinNames = result.mixins().keySet().stream().sorted(Comparator.comparing(tu -> tu.getClassName())).toList();
		for(Type mixinType : mixinNames) {
			var entryWidget = new SelectableTextWidget(mixinType.getClassName());
			entryWidget.autoWidth();
			if(entryWidget.width > this.width - 15) {
				entryWidget.setWidth(this.width - 15);
			}

			List<TooltipComponent> mixinTooltip = new ArrayList<>();
			List<IClassInfo> mixinClasses = result.mixins().get(mixinType);
			mixinClasses.sort(Comparator.comparing(a -> a.type().getClassName()));
			for(IClassInfo mixinClass : mixinClasses) {
				String className = mixinClass.type().getClassName();
				String shortClassName = className.substring(jarInfo.getShortestCommonPackage().length());
				String label = I18n.get("whodoesthat.gui.descriptors.mixins.tooltip_detailed", shortClassName, mixinType.getClassName());
				mixinTooltip.add(WrappedStringTooltipComponent.white(label));

				for(IMethodInfo methodInfo : mixinClass.methods().stream().sorted(Comparator.comparing(IMethodInfo::name)).toList()) {
					if(methodInfo.name().equals("<init>")) {
						continue;
					}
					HBoxTooltipComponent methodTooltip = new HBoxTooltipComponent();
					if(!methodInfo.annotations().isEmpty()) {
						var spongeAnnotations = methodInfo.annotations().stream()
							.filter(iAnnotationInfo -> iAnnotationInfo.getClassName().contains("spongepowered.asm"))
							.map(IAnnotationInfo::getSimpleName)
							.sorted(Comparator.naturalOrder())
							.toList();

						for(String annotation : spongeAnnotations) {
							methodTooltip.add(WrappedStringTooltipComponent.yellow(annotation));
						}
					}
					methodTooltip.add(StringTooltipComponent.gray(methodInfo.name()));
					mixinTooltip.add(methodTooltip);
				}
			}


			entryWidget.setTooltipElements(mixinTooltip);
			this.addListEntry(entryWidget);
		}
	}
}
