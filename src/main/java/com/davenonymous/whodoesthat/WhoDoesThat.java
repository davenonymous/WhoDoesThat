package com.davenonymous.whodoesthat;

import com.davenonymous.whodoesthat.config.*;
import com.davenonymous.whodoesthat.data.ModAnalyzer;
import com.davenonymous.whodoesthat.data.result.FullAnalysisResult;
import com.davenonymous.whodoesthat.gui.ModOverviewScreen;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mod(value = WhoDoesThat.MODID, dist = Dist.CLIENT)
public class WhoDoesThat {
	public static final String MODID = "whodoesthat";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static ModContainer CONTAINER;
	public static FullAnalysisResult LAST_ANALYSIS;

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
		DescriptionConfig instance = DescriptionConfig.INSTANCE;
		var fileWatcher = FileWatcher.defaultInstance();
		try(var files = Files.walk(PathConfig.configPath)) {
			files.filter(Files::isRegularFile).forEach(path -> {
				fileWatcher.addWatch(
					path, () -> {
						instance.load();
						if(DefaultDescriptions.WRITING_DEFAULTS) {
							return;
						}
						if(ActionConfig.generateOnConfigChange) {
							ModAnalyzer.generateModInfoFilesLogged();
						}
					}
				);
			});
		} catch (IOException ignored) {
		}

		CONTAINER.registerExtensionPoint(
			IConfigScreenFactory.class, (modContainer, screen) -> {
				return new ConfigurationScreen(
					modContainer, screen, (configurationScreen, type, modConfig, component) -> {
					return new ConfigurationScreen.ConfigurationSectionScreen(configurationScreen, type, modConfig, component) {
						@Override
						protected Collection<? extends Element> createSyntheticValues() {
							Button generate = new Button.Builder(
								Component.translatable("whodoesthat.configuration.generate_now.button"),
								button -> {
									int modCount = ModList.get().getMods().size();
									final long startTime = System.nanoTime();
									Optional<String> error = ModAnalyzer.generateModInfoFilesLogged();
									final long endTime = System.nanoTime();
									final long duration = (endTime - startTime) / 1000000;

									if(error.isPresent()) {
										button.setMessage(Component.translatable("whodoesthat.configuration.generate_now.error"));
										button.setTooltip(Tooltip.create(Component.literal(error.get())));
									} else {
										button.setMessage(Component.translatable("whodoesthat.configuration.generate_now.success"));
										button.setTooltip(Tooltip.create(Component.translatable("whodoesthat.configuration.generate_now.success.tooltip", modCount, duration)));
									}
								}
							).build();

							Button show = new Button.Builder(
								Component.translatable("whodoesthat.configuration.show.button"),
								button -> {
									Minecraft.getInstance().pushGuiLayer(new ModOverviewScreen());
								}
							).build();

							return List.of(
								new Element(
									Component.translatable("whodoesthat.configuration.generate_now"),
									Component.translatable("whodoesthat.configuration.generate_now.tooltip"),
									generate
								),
								new Element(
									Component.translatable("whodoesthat.configuration.show"),
									Component.translatable("whodoesthat.configuration.show.tooltip"),
									show
								)
							);
						}
					};
				}
				);
			}
		);
	}

	@SubscribeEvent
	public void loadingFinished(FMLLoadCompleteEvent event) {
		if(!ActionConfig.generateOnStartup) {
			return;
		}

		boolean shouldGenerate = true;
		if(Files.exists(PathConfig.outputFileJson)) {
			shouldGenerate = false;
			try {
				FileTime modified = Files.getLastModifiedTime(PathConfig.outputFileJson);
				for(var modInfo : ModList.get().getModFiles()) {
					if(Files.getLastModifiedTime(modInfo.getFile().getFilePath()).compareTo(modified) > 0) {
						shouldGenerate = true;
						break;
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if(shouldGenerate) {
			ModAnalyzer.generateModInfoFilesLogged();
		}
	}
}
