package com.davenonymous.whodoesthat.lib.gui;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public interface IClientTooltipProvider {
	List<TooltipComponent> getClientTooltip();
}
