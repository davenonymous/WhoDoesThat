package com.davenonymous.whodoesthat.setup;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.config.GuiConfig;
import com.davenonymous.whodoesthat.gui.menu.TitleScreenAddon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = WhoDoesThat.MODID, bus = EventBusSubscriber.Bus.GAME)
public class AddButtonToModsListScreen {


	@SubscribeEvent
	public static void onScreenInit(ScreenEvent.Init.Post event) {
		if(event.getScreen().getClass().getName().matches(GuiConfig.menuScreenClass)) {
			TitleScreenAddon addon = new TitleScreenAddon(event.getScreen().getMinecraft());
			event.addListener(addon);
		}
	}
}
