package com.davenonymous.whodoesthat.datagen;

import com.davenonymous.whodoesthat.WhoDoesThat;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = WhoDoesThat.MODID)
public class DGHandler {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();

		var defaultDescriptions = new DefaultDescriptions(output);
		generator.addProvider(event.includeServer(), defaultDescriptions);
	}
}
