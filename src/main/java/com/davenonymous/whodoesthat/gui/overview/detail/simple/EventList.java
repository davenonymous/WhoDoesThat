package com.davenonymous.whodoesthat.gui.overview.detail.simple;

import com.davenonymous.whodoesthat.gui.overview.detail.AbstractResultList;
import com.davenonymous.whodoesthat.gui.widgets.SelectableTextWidget;
import com.davenonymous.whodoesthat.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import com.davenonymous.whodoesthatlib.api.result.asm.IMethodInfo;
import com.davenonymous.whodoesthatlib.api.result.mod.EventResult;
import net.minecraft.client.resources.language.I18n;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventList extends AbstractResultList<EventResult> {
	public EventList(IJarInfo jarInfo) {
		super(jarInfo, EventResult.class);
	}

	@Override
	public void createEntries() {
		Map<Type, List<IMethodInfo>> eventListeners = result.listeners();
		if(!eventListeners.isEmpty()) {
			Map<String, List<IMethodInfo>> shortenedEvents = eventListeners.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().getClassName().substring(e.getKey().getClassName().lastIndexOf('.') + 1), Map.Entry::getValue));

			Map<String, String> eventDocs = eventListeners.entrySet().stream()
				.collect(Collectors.toMap(
					e -> e.getKey().getClassName().substring(e.getKey().getClassName().lastIndexOf('.') + 1), typeListEntry ->
						"whodoesthat.events." + typeListEntry.getKey().getClassName()
				));

			List<String> eventNames = new ArrayList<>(shortenedEvents.keySet());
			eventNames.sort(Comparator.naturalOrder());
			for(String eventName : eventNames) {
				List<IMethodInfo> listeningMethods = shortenedEvents.get(eventName);

				var entryWidget = new SelectableTextWidget(eventName);
				entryWidget.autoWidth();
				if(entryWidget.width > this.width - 15) {
					entryWidget.setWidth(this.width - 15);
				}

				String tooltipLanguageKeyBase = eventDocs.get(eventName);
				entryWidget.addTooltipElement(WrappedStringTooltipComponent.white(eventName));
				String translation = I18n.get(tooltipLanguageKeyBase);
				if(!translation.equals(tooltipLanguageKeyBase)) {
					entryWidget.addTooltipElement(WrappedStringTooltipComponent.gray(translation));
				}

				for(var methodInfo : listeningMethods) {
					entryWidget.addTooltipElement(WrappedStringTooltipComponent.yellow(methodInfo.locatableName()));
				}

				this.addListEntry(entryWidget);
			}
		}
	}
}
