package com.davenonymous.whodoesthat.config;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.gui.overview.ModOverviewScreen;
import com.davenonymous.whodoesthat.setup.ScanHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

import java.util.Collection;
import java.util.List;

public class ConfigScreen {

	public static Screen createScreen(ModContainer modContainer, Screen screen) {
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
							ScanHelper.generateModInfoFilesAsync().thenAccept(iScanResult -> {
								final long endTime = System.nanoTime();
								final long duration = (endTime - startTime) / 1000000;

								if(iScanResult.isEmpty()) {
									button.setMessage(Component.translatable("whodoesthat.configuration.generate_now.error"));
									return;
								} else {
									button.setMessage(Component.translatable("whodoesthat.configuration.generate_now.success"));
									button.setTooltip(Tooltip.create(Component.translatable("whodoesthat.configuration.generate_now.success.tooltip", modCount, duration)));
									WhoDoesThat.LAST_ANALYSIS = iScanResult.get();
								}

							});
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
}
