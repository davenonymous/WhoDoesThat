package com.davenonymous.whodoesthat;

import com.davenonymous.whodoesthat.config.ConfigScreen;
import com.davenonymous.whodoesthat.config.ModConfig;
import com.davenonymous.whodoesthat.setup.ScanHelper;
import com.davenonymous.whodoesthatlib.api.result.IScanResult;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mod(value = WhoDoesThat.MODID, dist = Dist.CLIENT)
public class WhoDoesThat {
	public static final String MODID = "whodoesthat";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static ModContainer CONTAINER;
	public static IScanResult LAST_ANALYSIS;
	public static Map<String, Set<String>> reverseDependencies = new HashMap<>();

	public WhoDoesThat(IEventBus modEventBus, ModContainer modContainer) {
		CONTAINER = modContainer;
		modEventBus.register(this);

		modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, ModConfig.CLIENT_SPEC);
	}

	public static ResourceLocation resource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	@SubscribeEvent
	public void onClientSetup(FMLClientSetupEvent event) {
		CONTAINER.registerExtensionPoint(IConfigScreenFactory.class, ConfigScreen::createScreen);
	}

	@SubscribeEvent
	public void imcEnqueue(InterModEnqueueEvent event) {
		InterModComms.sendTo("darkmodeeverywhere", "dme-shaderblacklist", () -> "com.davenonymous.whodoesthat");
	}

	@SubscribeEvent
	public void loadingFinished(FMLLoadCompleteEvent event) {
		ScanHelper.generateModInfoFilesAsync();
	}
}
