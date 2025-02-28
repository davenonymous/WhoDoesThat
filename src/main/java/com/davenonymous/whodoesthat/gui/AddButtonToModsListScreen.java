package com.davenonymous.whodoesthat.gui;

import com.davenonymous.whodoesthat.WhoDoesThat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ModListScreen;

import java.util.Optional;

@EventBusSubscriber(modid = WhoDoesThat.MODID, bus = EventBusSubscriber.Bus.GAME)
public class AddButtonToModsListScreen {
	@SubscribeEvent
	public static void onScreenInit(ScreenEvent.Init.Post event) {
		if(event.getScreen() instanceof ModListScreen screen) {
			Optional<Renderable> doneRenderable = screen.renderables.stream()
				.filter(r -> r instanceof Button b && b.getMessage().getContents() instanceof TranslatableContents t && t.getKey().equals("gui.done")).findAny();

			if(doneRenderable.isPresent()) {
				Button doneButton = (Button) doneRenderable.get();
				var openModOverviewButton = new ImageButton(
					doneButton.getX() + doneButton.getWidth() + 5, doneButton.getY(),
					20, 20,
					RecipeBookComponent.RECIPE_BUTTON_SPRITES,
					button -> Minecraft.getInstance().pushGuiLayer(new ModOverviewScreen())
				);
				event.addListener(openModOverviewButton);
			}
		}
	}
}
