package com.davenonymous.whodoesthat.config;

import com.davenonymous.whodoesthat.WhoDoesThat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = WhoDoesThat.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModConfig {
	public static final ModConfigSpec CLIENT_SPEC;
	public static final PathConfig Paths;
	public static final ActionConfig Actions;

	static {
		var clientBuilder = new ModConfigSpec.Builder();

		Paths = new PathConfig(clientBuilder);
		Actions = new ActionConfig(clientBuilder);

		CLIENT_SPEC = clientBuilder.build();
	}

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		if(event.getConfig().getSpec() == CLIENT_SPEC) {
			Paths.load();
			Actions.load();
		}
	}
}
