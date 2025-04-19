package com.davenonymous.whodoesthat.config;

import com.davenonymous.whodoesthat.gui.menu.ContentAlignment;
import net.neoforged.neoforge.common.ModConfigSpec;

public class GuiConfig {
	public static boolean showClassesForAllRightsReservedMods;
	public static ContentAlignment menuDockSide;
	public static String menuScreenClass;

	public final ModConfigSpec.BooleanValue SHOW_CLASSES_FOR_ALL_RIGHTS_RESERVED_MODS;
	public final ModConfigSpec.EnumValue<ContentAlignment> MENU_DOCK_SIDE;
	public final ModConfigSpec.ConfigValue<String> MENU_SCREEN_CLASS;

	public GuiConfig(ModConfigSpec.Builder builder) {
		builder.push("gui");

		SHOW_CLASSES_FOR_ALL_RIGHTS_RESERVED_MODS = builder
			.comment("Show class tree for mods that are \"All rights reserved\".")
			.translation("whodoesthat.configuration.show_classes_for_all_rights_reserved_mods")
			.define("showClassesForAllRightsReservedMods", false);

		MENU_DOCK_SIDE = builder
			.comment("Docking side of the menu")
			.translation("whodoesthat.configuration.menu_dock_side")
			.defineEnum("menuDockSide", ContentAlignment.TOP_RIGHT);

		MENU_SCREEN_CLASS = builder
			.comment("Class name of the screen to attach the WhoDoesThat menu to.")
			.translation("whodoesthat.configuration.menu_screen_class")
			.define("menuScreenClass", "net.neoforged.neoforge.client.gui.ModListScreen");

		builder.pop();
	}

	public void load() {
		showClassesForAllRightsReservedMods = SHOW_CLASSES_FOR_ALL_RIGHTS_RESERVED_MODS.get();
		menuDockSide = MENU_DOCK_SIDE.get();
		menuScreenClass = MENU_SCREEN_CLASS.get();
	}
}
