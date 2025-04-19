package com.davenonymous.whodoesthat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

public class PathConfig {


	public final ModConfigSpec.ConfigValue<String> CONFIG_PATH;
	public final ModConfigSpec.ConfigValue<String> OUTPUT_FILE_JSON;
	public final ModConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_PATHS;

	public static Path configPath;
	public static Path outputFileJson;
	public static HashSet<String> additionalPaths;

	public PathConfig(ModConfigSpec.Builder builder) {
		builder.push("paths");

		CONFIG_PATH = builder
			.comment("Which folder to load descriptor configs from")
			.translation("whodoesthat.configuration.config_folder")
			.define("configFolder", "config/whodoesthat.d");

		OUTPUT_FILE_JSON = builder
			.comment("Where to write the JSON formatted mod analysis to")
			.translation("whodoesthat.configuration.json_target")
			.define("jsonDestination", "modinfo.json");

		ADDITIONAL_PATHS = builder
			.comment("Additional paths to scan for jars.")
			.translation("whodoesthat.configuration.additional_paths")
			.<String>defineListAllowEmpty(
				"additionalPaths", List.of(), String::new, p -> p instanceof String);

		builder.pop();
	}

	public void load() {
		configPath = Path.of(CONFIG_PATH.get());
		outputFileJson = Path.of(OUTPUT_FILE_JSON.get());
		additionalPaths = new HashSet<>(ADDITIONAL_PATHS.get());
	}
}
