package com.davenonymous.whodoesthat.config;

import com.davenonymous.whodoesthat.data.FullConfigBuilder;
import com.davenonymous.whodoesthat.data.StringyElementType;
import com.davenonymous.whodoesthat.data.getter.FullConfig;
import com.mojang.brigadier.Command;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.data.DataProvider;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkTicketLevelUpdatedEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.spongepowered.asm.mixin.injection.Inject;

public class DefaultDescriptions {
	public static final FullConfig CUSTOM_CONTENT;
	public static final FullConfig SYSTEMS;
	public static final FullConfig MODIFICATIONS;

	public static boolean WRITING_DEFAULTS = false;

	static {
		CUSTOM_CONTENT = new FullConfigBuilder()
			.event(AnvilUpdateEvent.class, "Custom Anvil Recipes")
			.event(DataPackRegistryEvent.NewRegistry.class, "Custom Registries")
			.event(EntityAttributeCreationEvent.class, "Custom Entity Attributes")
			.event(EntityRenderersEvent.RegisterRenderers.class, "Custom Entity Renderers")
			.event(RegisterBrewingRecipesEvent.class, "Custom Brewing Recipes")
			.event(RegisterClientTooltipComponentFactoriesEvent.class, "Custom Tooltip Components")
			.event(RegisterDataMapTypesEvent.class, "Custom Data Map Types")
			.event(RegisterKeyMappingsEvent.class, "Custom Key Mappings")
			.event(RegisterShadersEvent.class, "Custom Shaders")
			.inheritance(Animal.class, "Custom Animals")
			.inheritance(BakedModel.class, "Custom 3D Models")
			.inheritance(Block.class, "Custom Blocks")
			.inheritance(BlockEntityRenderer.class, "Custom Block Renderer")
			.inheritance(BlockEntityWithoutLevelRenderer.class, "Custom Item Renderer")
			.inheritance(Enchantment.class, "Custom Enchantments")
			.inheritance(Entity.class, "Custom Entities")
			.inheritance(IGeometryLoader.class, "Custom Model Loaders")
			.inheritance(Item.class, "Custom Items")
			.inheritance(Mob.class, "Custom Mobs")
			.inheritance(MobEffect.class, "Custom Potion Effects")
			.inheritance(Particle.class, "Custom Particles")
			.inheritance(Recipe.class, "Custom Recipe Types")
			.inheritance(Screen.class, "Custom GUI Screens")
			.hasFile("assets/*/blockstates/*.json", "Custom Blocks")
			.hasFile("assets/*/lang/*.json", "Localization")
			.hasFile("assets/*/models/block/*", "Custom Block Models")
			.hasFile("assets/*/models/item/*", "Custom Item Models")
			.hasFile("assets/*/textures/block/*.png", "Custom Block Textures")
			.hasFile("assets/*/textures/item/*.png", "Custom Item Textures")
			.hasFile("assets/*/textures/gui/*.png", "Custom GUI Textures")
			.hasFile("data/*/advancement/**/*.json", "Custom Advancements")
			.hasFile("data/*/tags/**/*.json", "Custom Tag Assignments")
			.hasFile("data/*/loot_modifiers/*.json", "Uses Loot Modifiers")
			.hasFile("data/*/loot_table/**/*.json", "Custom Loot Tables")
			.hasFile("data/*/recipe/**/*.json", "Custom Recipes")
			.hasFile("data/*/structure/**/*.nbt", "Custom Structures")
			.hasFile("data/*/worldgen/**/*.json", "Custom World Generation")
			.hasFile("data/*/dimension_type/*.json", "Custom Dimension Types")
			.hasFile("data/*/dimension/*.json", "Custom Dimensions")
			.hasFile("data/*/function/*.mcfunction", "Uses Minecraft Functions")
			.hasFile("assets/*/patchouli_books/**/*.json", "Patchouli Guide Book")

			.build();

		MODIFICATIONS = new FullConfigBuilder()
			.event(BlockEvent.BreakEvent.class, "Modifies Block Breaking")
			.event(BlockEvent.EntityPlaceEvent.class, "Modifies Block Placement")
			.event(BlockEvent.FarmlandTrampleEvent.class, "Modifies Farmland Trampling")
			.event(BlockEvent.FluidPlaceBlockEvent.class, "Modifies Fluid Block Placement")
			.event(ExplosionEvent.Start.class, "Modifies Explosions")
			.event(ItemAttributeModifierEvent.class, "Modifies Item Attributes")
			.event(LivingDamageEvent.class, "Modifies Entity Damage Received")
			.event(ModifyDefaultComponentsEvent.class, "Modifies Default Components")
			.event(PlayerEvent.BreakSpeed.class, "Modifies Block Breaking Speed")
			.event(RenderLevelStageEvent.class, "Modifies World Rendering")
			.event(RenderPlayerEvent.class, "Modifies Player Rendering")
			.event(RenderTooltipEvent.GatherComponents.class, "Modifies Tooltips")
			.event(ScreenEvent.Render.Post.class, "Modifies GUI Rendering")
			.event(ScreenEvent.Render.Pre.class, "Modifies GUI Rendering")
			.event(ScreenEvent.Render.class, "Modifies GUI Rendering")
			.event(VillagerTradesEvent.class, "Modifies Villager Trades")
			.event(WandererTradesEvent.class, "Modifies Wandering Trader Trades")

			.annotation(Inject.class, StringyElementType.METHOD, "Modifies Methods", "id", "at", "cancellable")
			.mixin(Screen.class, "Modifies GUI Screens (invasive")
			.mixin(Brain.class, "Modifies Entity AI (invasive)")
			.mixin(LevelRenderer.class, "Modifies World Rendering (invasive)")

			.build();

		SYSTEMS = new FullConfigBuilder()
			.inheritance(Command.class, "Has Commands")
			.inheritance(DataProvider.class, "Uses Data Generators")
			.inheritance(SavedData.class, "Stores Data in World Saves")
			.event(AddPackFindersEvent.class, "Adds Resource/Data Pack Finders")
			.event(AddReloadListenerEvent.class, "Adds Reload Listeners")
			.event(ArrowLooseEvent.class, "Listens to Bow Loose Events")
			.event(ArrowNockEvent.class, "Listens to Bow Nock Events")
			.event(BlockEvent.PortalSpawnEvent.class, "Listens to portal spawning")
			.event(ChunkEvent.Load.class, "Listens to Chunk Load Events")
			.event(ChunkEvent.Unload.class, "Listens to Chunk Unload Events")
			.event(ChunkTicketLevelUpdatedEvent.class, "Handles Chunk Loading")
			.event(ClientChatEvent.class, "Listens to Client Chat Messages")
			.event(ClientTickEvent.Post.class, "Runs Client Tick Logic")
			.event(ClientTickEvent.Pre.class, "Runs Client Tick Logic")
			.event(DataMapsUpdatedEvent.class, "Uses Data Maps")
			.event(EntityJoinLevelEvent.class, "Listens to Entity Join World Events")
			.event(EntityTickEvent.Post.class, "Runs Entity Tick Logic")
			.event(EntityTickEvent.Pre.class, "Runs Entity Tick Logic")
			.event(ExplosionEvent.Detonate.class, "Handles Explosion Detonation")
			.event(FinalizeSpawnEvent.class, "Modifies Entity Spawning")
			.event(InputEvent.class, "Handles Keyboard/Mouse Input")
			.event(InterModProcessEvent.class, "Listens to Inter-Mod Communication Events")
			.event(ItemEntityPickupEvent.Post.class, "Listens to Item Pickup Events")
			.event(ItemEntityPickupEvent.Pre.class, "Listens to Item Pickup Events")
			.event(ItemEntityPickupEvent.class, "Listens to Item Pickup Events")
			.event(LevelTickEvent.Post.class, "Runs World Tick Logic")
			.event(LevelTickEvent.Pre.class, "Runs World Tick Logic")
			.event(LivingBreatheEvent.class, "Listens to Entity Breathing Events")
			.event(LivingChangeTargetEvent.class, "Listens to Entity AI Target Change Events")
			.event(LivingConversionEvent.class, "Listens to Entity Conversion Events")
			.event(LivingDeathEvent.class, "Listens to Entity Death Events")
			.event(LivingDestroyBlockEvent.class, "Listens to Entity Block Destruction Events")
			.event(LivingDropsEvent.class, "Listens to Entity Drop Events")
			.event(LivingDrownEvent.class, "Listens to Entity Drowning Events")
			.event(LivingEntityUseItemEvent.class, "Listens to Entity Item Use Events")
			.event(LivingEquipmentChangeEvent.class, "Listens to Entity Equipment Change Events")
			.event(LivingEvent.LivingJumpEvent.class, "Listens to Entity Jump Events")
			.event(LivingFallEvent.class, "Listens to Entity Fall Events")
			.event(LivingHealEvent.class, "Listens to Entity Healing Events")
			.event(LivingIncomingDamageEvent.class, "Listens to Entity Damage Events")
			.event(LivingKnockBackEvent.class, "Listens to Entity Knockback Events")
			.event(LivingShieldBlockEvent.class, "Listens to Entity Shield Block Events")
			.event(MobEffectEvent.Added.class, "Listens to Potion Effect Added Events")
			.event(MobEffectEvent.Applicable.class, "Listens to Potion Effect Applicable Events")
			.event(MobEffectEvent.Expired.class, "Listens to Potion Effect Expired Events")
			.event(MobEffectEvent.Remove.class, "Listens to Potion Effect Removed Events")
			.event(MobEffectEvent.class, "Listens to Potion Effect Events")
			.event(ModConfigEvent.Loading.class, "Auto-reloads Config Files")
			.event(ModConfigEvent.Reloading.class, "Auto-reloads Config Files")
			.event(ModConfigEvent.Unloading.class, "Auto-reloads Config Files")
			.event(ModConfigEvent.class, "Auto-reloads Config Files")
			.event(OnDatapackSyncEvent.class, "Custom Data Pack Sync")
			.event(PlayerEvent.HarvestCheck.class, "Handles Block Harvesting")
			.event(PlayerEvent.ItemCraftedEvent.class, "Listens to Item Crafting Events")
			.event(PlayerEvent.ItemSmeltedEvent.class, "Listens to Item Smelting Events")
			.event(PlayerEvent.PlayerChangeGameModeEvent.class, "Listens to Game Mode Change Events")
			.event(PlayerEvent.PlayerChangedDimensionEvent.class, "Listens to Dimension Change Events")
			.event(PlayerEvent.PlayerLoggedInEvent.class, "Listens to Player Login Events")
			.event(PlayerEvent.PlayerLoggedOutEvent.class, "Listens to Player Logout Events")
			.event(PlayerInteractEvent.EntityInteract.class, "Listens to Entity Interaction Events")
			.event(PlayerInteractEvent.EntityInteractSpecific.class, "Listens to Entity Interaction Events with Click Positions")
			.event(PlayerInteractEvent.LeftClickBlock.class, "Listens to Left Click Block Events")
			.event(PlayerInteractEvent.LeftClickEmpty.class, "Handles Left Clicks with Empty Hands (Client-side)")
			.event(PlayerInteractEvent.RightClickBlock.class, "Listens to Right Click Block Events")
			.event(PlayerInteractEvent.RightClickEmpty.class, "Handles Right Clicks with Empty Hands (Client-side)")
			.event(PlayerInteractEvent.RightClickItem.class, "Handles Right Clicks with Items")
			.event(PlayerTickEvent.Post.class, "Runs Player Tick Logic")
			.event(PlayerTickEvent.Pre.class, "Runs Player Tick Logic")
			.event(PlayerTickEvent.class, "Runs Player Tick Logic")
			.event(RecipesUpdatedEvent.class, "Listens to Recipe Updates")
			.event(RegisterCapabilitiesEvent.class, "Has Capability Providers")
			.event(RegisterColorHandlersEvent.class, "Custom Block/Item Color Handlers")
			.event(RegisterPayloadHandlersEvent.class, "Custom Network Packets")
			.event(RegisterClientCommandsEvent.class, "Custom Client Commands")
			.event(RegisterCommandsEvent.class, "Custom Server Commands")
			.event(ScreenEvent.CharacterTyped.class, "Handles Typed Characters in GUIs")
			.event(ScreenEvent.Closing.class, "Listens to GUI Close Events")
			.event(ScreenEvent.KeyPressed.class, "Handles Key Presses in GUIs")
			.event(ScreenEvent.KeyReleased.class, "Handles Key Presses in GUIs")
			.event(ScreenEvent.MouseButtonPressed.class, "Handles Mouse Clicks in GUIs")
			.event(ScreenEvent.MouseButtonReleased.class, "Handles Mouse Clicks in GUIs")
			.event(ScreenEvent.MouseScrolled.class, "Handles Mouse Scrolling in GUIs")
			.event(ScreenEvent.Opening.class, "Listens to GUI Open Events")
			.event(ServerAboutToStartEvent.class, "Listens to Server About to Start Events")
			.event(ServerChatEvent.class, "Listens to Server Chat Messages")
			.event(ServerStartedEvent.class, "Listens to Server Started Events")
			.event(ServerStartingEvent.class, "Listens to Server Starting Events")
			.event(ServerStoppedEvent.class, "Listens to Server Stopped Events")
			.event(ServerStoppingEvent.class, "Listens to Server Stopping Events")
			.event(ServerTickEvent.Post.class, "Runs Server Tick Logic")
			.event(ServerTickEvent.Pre.class, "Runs Server Tick Logic")
			.event(TagsUpdatedEvent.class, "Listens to Tag Updates")


			.annotation(GameTest.class, StringyElementType.METHOD, "Automatic Testing", "template")

			.hasFile("META-INF/accesstransformer.cfg", "Uses Access Transformers")

			.build();
	}

	public static void writeDefaultConfigs() {
		var customContentPath = PathConfig.configPath.resolve("custom_content.yaml");
		var systemsPath = PathConfig.configPath.resolve("systems.yaml");
		var modificationsPath = PathConfig.configPath.resolve("modifications.yaml");

		WRITING_DEFAULTS = true;
		CUSTOM_CONTENT.writeYaml(customContentPath);
		SYSTEMS.writeYaml(systemsPath);
		MODIFICATIONS.writeYaml(modificationsPath);
		WRITING_DEFAULTS = false;
	}
}
