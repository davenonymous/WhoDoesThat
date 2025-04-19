package com.davenonymous.whodoesthat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashSet;
import java.util.List;

public class ReportsConfig {
	public static HashSet<String> modBlacklist;
	public static boolean includeFiles;
	public static boolean includeClasses;
	public static boolean includeMethods;
	public static boolean includeFields;
	public static boolean includeAnnotations;
	public static boolean includeAnalysis;
	public static boolean includeTags;
	public static boolean includeSummary;

	public final ModConfigSpec.ConfigValue<List<? extends String>> MOD_BLACKLIST;
	public final ModConfigSpec.BooleanValue INCLUDE_FILES;
	public final ModConfigSpec.BooleanValue INCLUDE_CLASSES;
	public final ModConfigSpec.BooleanValue INCLUDE_METHODS;
	public final ModConfigSpec.BooleanValue INCLUDE_FIELDS;
	public final ModConfigSpec.BooleanValue INCLUDE_ANNOTATIONS;
	public final ModConfigSpec.BooleanValue INCLUDE_ANALYSIS;
	public final ModConfigSpec.BooleanValue INCLUDE_TAGS;
	public final ModConfigSpec.BooleanValue INCLUDE_SUMMARY;


	public ReportsConfig(ModConfigSpec.Builder builder) {
		builder.push("report");

		MOD_BLACKLIST = builder
			.comment("Mods to exclude from analysis")
			.translation("whodoesthat.configuration.mod_blacklist")
			.<String>defineListAllowEmpty(
				"modBlacklist", List.of("minecraft", "neoforge"), String::new, p -> p instanceof String);

		// See IConfig.IncludedOutput
		INCLUDE_FILES = builder
			.comment("Include globbed files")
			.translation("whodoesthat.configuration.include_files")
			.define("includeFiles", false);

		INCLUDE_CLASSES = builder
			.comment("Include class information")
			.translation("whodoesthat.configuration.include_classes")
			.define("includeClasses", false);

		INCLUDE_METHODS = builder
			.comment("Include method information")
			.translation("whodoesthat.configuration.include_methods")
			.define("includeMethods", false);

		INCLUDE_FIELDS = builder
			.comment("Include field information")
			.translation("whodoesthat.configuration.include_fields")
			.define("includeFields", false);

		INCLUDE_ANNOTATIONS = builder
			.comment("Include annotation information")
			.translation("whodoesthat.configuration.include_annotations")
			.define("includeAnnotations", false);

		INCLUDE_ANALYSIS = builder
			.comment("Include analysis information")
			.translation("whodoesthat.configuration.include_analysis")
			.define("includeAnalysis", true);

		INCLUDE_TAGS = builder
			.comment("Include tag information")
			.translation("whodoesthat.configuration.include_tags")
			.define("includeTags", true);

		INCLUDE_SUMMARY = builder
			.comment("Include summary information")
			.translation("whodoesthat.configuration.include_summary")
			.define("includeSummary", true);

		builder.pop();
	}

	public void load() {
		modBlacklist = new HashSet<>(MOD_BLACKLIST.get());
		includeFiles = INCLUDE_FILES.get();
		includeClasses = INCLUDE_CLASSES.get();
		includeMethods = INCLUDE_METHODS.get();
		includeFields = INCLUDE_FIELDS.get();
		includeAnnotations = INCLUDE_ANNOTATIONS.get();
		includeAnalysis = INCLUDE_ANALYSIS.get();
		includeTags = INCLUDE_TAGS.get();
		includeSummary = INCLUDE_SUMMARY.get();
	}
}
