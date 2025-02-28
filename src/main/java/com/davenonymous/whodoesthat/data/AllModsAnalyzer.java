package com.davenonymous.whodoesthat.data;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.config.ActionConfig;
import com.davenonymous.whodoesthat.config.PathConfig;
import com.davenonymous.whodoesthat.data.result.FullAnalysisResult;
import com.davenonymous.whodoesthat.data.result.ModAnalysisResult;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class AllModsAnalyzer {
	public static Optional<String> generateModInfoFilesLogged() {
		var errors = generateModInfoFiles();
		if(errors.isEmpty()) {
			WhoDoesThat.LOGGER.info("Analysis written to: {}, {}", PathConfig.outputFileJson, PathConfig.outputFileCsv);
		} else {
			WhoDoesThat.LOGGER.error(errors.get());
		}
		return errors;
	}

	// TODO: Lock this method to prevent concurrent analysis
	public static CompletableFuture<Optional<String>> generateModInfoFilesAsync() {
		return CompletableFuture.supplyAsync(
			() -> {
				WhoDoesThat.LOGGER.info("Starting mod analysis in background thread...");
				return generateModInfoFilesLogged();
			}, Executors.newSingleThreadExecutor()
		);
	}

	private static Optional<String> generateModInfoFiles() {
		List<String> errors = new ArrayList<>();

		FullAnalysisResult fullAnalysisResult = analyzeAllMods();
		Optional<String> prettyJson = fullAnalysisResult.prettyJson();
		if(prettyJson.isEmpty()) {
			errors.add("Failed to encode json.");
		}

		Optional<String> csv = fullAnalysisResult.csvTable();
		if(csv.isEmpty()) {
			errors.add("Failed to encode csv.");
		}

		Optional<String> yaml = fullAnalysisResult.yaml();
		if(yaml.isEmpty()) {
			errors.add("Failed to encode yaml.");
		}

		Map<Path, String> files = new HashMap<>();
		prettyJson.ifPresent(s -> files.put(PathConfig.outputFileJson, s));
		csv.ifPresent(s -> files.put(PathConfig.outputFileCsv, s));
		yaml.ifPresent(s -> files.put(PathConfig.outputFileYaml, s));

		for(var entry : files.entrySet()) {
			Path path = entry.getKey();
			String content = entry.getValue();

			try {
				Files.writeString(path, content);
			} catch (java.io.IOException e) {
				errors.add("Failed to write to file: " + e.getMessage());
			}
		}

		return errors.isEmpty() ? Optional.empty() : Optional.of(String.join("\n", errors));
	}

	private static FullAnalysisResult analyzeAllMods() {

		Map<String, ModAnalysisResult> modAnalysisResults = new HashMap<>();
		final long startTime = System.nanoTime();
		int modCount = ModList.get().getMods().size();
		for(IModInfo modInfo : ModList.get().getMods()) {
			if(ActionConfig.modBlacklist.contains(modInfo.getModId())) {
				continue;
			}

			ModAnalyzer analyzer = new ModAnalyzer(modInfo);
			ModAnalysisResult result = analyzer.analyze();

			modAnalysisResults.put(modInfo.getModId(), result);
		}
		final long endTime = System.nanoTime();
		final long duration = (endTime - startTime) / 1000000;
		WhoDoesThat.LOGGER.info("Analysis of {} mods took {}ms", modCount, duration);

		WhoDoesThat.LAST_ANALYSIS = new FullAnalysisResult(modAnalysisResults);
		return WhoDoesThat.LAST_ANALYSIS;
	}
}
