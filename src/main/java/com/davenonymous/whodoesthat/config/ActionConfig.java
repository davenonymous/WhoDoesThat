package com.davenonymous.whodoesthat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashSet;
import java.util.List;

public class ActionConfig {
	public static boolean generateAsynchronously;
	public static boolean generateOnStartup;
	public static boolean forceGenerateOnStartup;
	public static HashSet<String> modBlacklist;
	public static String csvDelimiter;
	public static boolean csvAlwaysQuote;
	public static boolean includeFilesInFullReport;

	public final ModConfigSpec.BooleanValue GENERATE_ASYNCHRONOUSLY;
	public final ModConfigSpec.BooleanValue GENERATE_ON_STARTUP;
	public final ModConfigSpec.BooleanValue FORCE_GENERATE_ON_STARTUP;
	public final ModConfigSpec.ConfigValue<List<? extends String>> MOD_BLACKLIST;

	public final ModConfigSpec.BooleanValue INCLUDE_FILES_IN_FULL_REPORT;

	public final ModConfigSpec.ConfigValue<String> CSV_DELIMITER;
	public final ModConfigSpec.BooleanValue CSV_ALWAYS_QUOTE;

	public ActionConfig(ModConfigSpec.Builder builder) {
		builder.push("actions");

		GENERATE_ASYNCHRONOUSLY = builder
			.comment("Generate mod reports asynchronously")
			.translation("whodoesthat.configuration.generate_asynchronously")
			.define("generateAsynchronously", true);

		GENERATE_ON_STARTUP = builder
			.comment("Generate mod analysis when the game starts")
			.translation("whodoesthat.configuration.generate_on_startup")
			.define("generateOnStartup", true);

		FORCE_GENERATE_ON_STARTUP = builder
			.comment("Generate mod analysis when the game starts")
			.translation("whodoesthat.configuration.force_generate_on_startup")
			.define("forceGenerationOnStartup", false);

		MOD_BLACKLIST = builder
			.comment("Mods to exclude from analysis")
			.translation("whodoesthat.configuration.mod_blacklist")
			.<String>defineListAllowEmpty(
				"modBlacklist", List.of("minecraft", "neoforge"), String::new, p -> p instanceof String);

		CSV_DELIMITER = builder
			.comment("Delimiter to use in CSV output")
			.translation("whodoesthat.configuration.csv_delimiter")
			.define("csvDelimiter", ",");

		CSV_ALWAYS_QUOTE = builder
			.comment("Always quote CSV fields")
			.translation("whodoesthat.configuration.csv_always_quote")
			.define("csvAlwaysQuote", false);

		INCLUDE_FILES_IN_FULL_REPORT = builder
			.comment("Include globbed files in the full report")
			.translation("whodoesthat.configuration.include_files_in_full_report")
			.define("includeFilesInFullReport", false);

		builder.pop();
	}

	public void load() {
		generateAsynchronously = GENERATE_ASYNCHRONOUSLY.get();
		generateOnStartup = GENERATE_ON_STARTUP.get();
		forceGenerateOnStartup = FORCE_GENERATE_ON_STARTUP.get();
		modBlacklist = new HashSet<>(MOD_BLACKLIST.get());
		csvDelimiter = CSV_DELIMITER.get();
		csvAlwaysQuote = CSV_ALWAYS_QUOTE.get();
		includeFilesInFullReport = INCLUDE_FILES_IN_FULL_REPORT.get();
	}
}
