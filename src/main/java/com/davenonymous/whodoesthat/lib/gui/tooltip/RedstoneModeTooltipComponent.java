package com.davenonymous.whodoesthat.lib.gui.tooltip;

import com.davenonymous.whodoesthat.lib.gui.GUIHelper;
import com.davenonymous.whodoesthat.lib.gui.RedstoneMode;

public class RedstoneModeTooltipComponent extends HBoxTooltipComponent {
	public RedstoneModeTooltipComponent(RedstoneMode mode) {
		super();

		this.setAlignment(BoxAlignment.CENTER);

		switch(mode) {
			case IGNORE_POWER -> {
				add(new SpriteTooltipComponent(GUIHelper.tabIcons, 16, 16, 26, 84, 10, 10));
			}
			case REQUIRE_POWER -> {
				add(new SpriteTooltipComponent(GUIHelper.tabIcons, 16, 16, 36, 84, 4, 11));
			}
			case STOP_ON_POWER -> {
				add(new SpriteTooltipComponent(GUIHelper.tabIcons, 16, 16, 40, 84, 2, 11));
			}
		}

		add(StringTooltipComponent.gray(mode.getDescription().getString()));
	}
}
