package com.davenonymous.whodoesthat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Path;

public class PathConfig {


	public final ModConfigSpec.ConfigValue<String> CONFIG_PATH;
	public final ModConfigSpec.ConfigValue<String> OUTPUT_FILE_JSON;
	public final ModConfigSpec.ConfigValue<String> OUTPUT_FILE_YAML;
	public final ModConfigSpec.ConfigValue<String> OUTPUT_FILE_CSV;

	public static Path configPath;
	public static Path outputFileJson;
	public static Path outputFileYaml;
	public static Path outputFileCsv;

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

		OUTPUT_FILE_YAML = builder
			.comment("Where to write the minimal YAML formatted mod analysis to")
			.translation("whodoesthat.configuration.yaml_target")
			.define("yamlDestination", "modinfo.yaml");

		OUTPUT_FILE_CSV = builder
			.comment("Where to write the CSV summary table to")
			.translation("whodoesthat.configuration.csv_target")
			.define("csvDestination", "modinfo-summary.csv");

		builder.pop();
	}

	public void load() {
		configPath = Path.of(CONFIG_PATH.get());
		outputFileJson = Path.of(OUTPUT_FILE_JSON.get());
		outputFileYaml = Path.of(OUTPUT_FILE_YAML.get());
		outputFileCsv = Path.of(OUTPUT_FILE_CSV.get());
	}
}
