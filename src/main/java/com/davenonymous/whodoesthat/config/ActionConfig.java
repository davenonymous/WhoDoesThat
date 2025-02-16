package com.davenonymous.whodoesthat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashSet;
import java.util.List;

public class ActionConfig {
	public static boolean generateOnConfigChange;
	public static boolean generateOnStartup;
	public static HashSet<String> modBlacklist;

	public final ModConfigSpec.BooleanValue GENERATE_ON_CONFIG_CHANGE;
	public final ModConfigSpec.BooleanValue GENERATE_ON_STARTUP;
	public final ModConfigSpec.ConfigValue<List<? extends String>> MOD_BLACKLIST;


	public ActionConfig(ModConfigSpec.Builder builder) {
		builder.push("actions");

		GENERATE_ON_CONFIG_CHANGE = builder
			.comment("Generate mod analysis when the config changes")
			.translation("whodoesthat.configuration.generate_on_config_change")
			.define("generateOnConfigChange", true);

		GENERATE_ON_STARTUP = builder
			.comment("Generate mod analysis when the game starts")
			.translation("whodoesthat.configuration.generate_on_startup")
			.define("generateOnStartup", true);

		MOD_BLACKLIST = builder
			.comment("Mods to exclude from analysis")
			.translation("whodoesthat.configuration.mod_blacklist")
			.<String>defineListAllowEmpty(
				"modBlacklist", List.of("minecraft", "neoforge"), String::new, p -> p instanceof String);

		builder.pop();
	}

	public void load() {
		generateOnConfigChange = GENERATE_ON_CONFIG_CHANGE.get();
		generateOnStartup = GENERATE_ON_STARTUP.get();
		modBlacklist = new HashSet<>(MOD_BLACKLIST.get());
	}
}
