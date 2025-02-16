package com.davenonymous.whodoesthat.setup;


import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.command.WhoDoesThatCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = WhoDoesThat.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModCommands {

	@SubscribeEvent
	public static void onRegisterCommands(RegisterClientCommandsEvent event) {
		var dispatcher = event.getDispatcher();
		dispatcher.register(WhoDoesThatCommand.register(dispatcher));
	}

}
