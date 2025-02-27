package com.davenonymous.whodoesthat.lib.gui.widgets;

import com.davenonymous.whodoesthat.lib.gui.GUIHelper;
import com.davenonymous.whodoesthat.lib.gui.RedstoneMode;
import com.davenonymous.whodoesthat.lib.gui.event.ValueChangedEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;


public class WidgetRedstoneMode extends WidgetSpriteSelect<RedstoneMode> {

	public WidgetRedstoneMode() {
		this(RedstoneMode.IGNORE_POWER);
	}

	public WidgetRedstoneMode(RedstoneMode initial) {
		this.addChoiceWithSprite(RedstoneMode.IGNORE_POWER, new SpriteData(GUIHelper.tabIcons, 26, 84, 10, 10));
		this.addChoiceWithSprite(RedstoneMode.REQUIRE_POWER, new SpriteData(GUIHelper.tabIcons, 36, 84, 4, 11));
		this.addChoiceWithSprite(RedstoneMode.STOP_ON_POWER, new SpriteData(GUIHelper.tabIcons, 40, 84, 2, 11));
		this.setValue(initial);
		updateToolTips();

		this.addListener(
			ValueChangedEvent.class, (event, widget) -> {
				updateToolTips();
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
	}

	public void updateToolTips() {
		this.setTooltipLines(this.getValue().getDescription());
	}
}
