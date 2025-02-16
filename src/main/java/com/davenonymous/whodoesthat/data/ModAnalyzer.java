package com.davenonymous.whodoesthat.data;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.config.ActionConfig;
import com.davenonymous.whodoesthat.config.DescriptionConfig;
import com.davenonymous.whodoesthat.config.PathConfig;
import com.davenonymous.whodoesthat.data.getter.AnnotationDescription;
import com.davenonymous.whodoesthat.data.getter.GlobDescription;
import com.davenonymous.whodoesthat.data.getter.InheritanceDescription;
import com.davenonymous.whodoesthat.data.result.*;
import com.davenonymous.whodoesthat.util.OptionalHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.neoforgespi.locating.IModFile;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ModAnalyzer {
	private final IModInfo mod;
	private final IModFileInfo info;
	private final IModFile modFile;
	private final ModFileScanData scanData;
	private final List<Path> extraFiles;

	public ModAnalyzer(IModInfo mod) {
		this.mod = mod;
		this.info = mod.getOwningFile();
		this.modFile = info.getFile();
		this.scanData = modFile.getScanResult();

		var rootPath = modFile.getSecureJar().getRootPath();
		List<Path> foundFiles = Collections.emptyList();
		try(Stream<Path> files = Files.find(rootPath, Integer.MAX_VALUE, (p, a) -> p.getNameCount() > 0 && a.isRegularFile() && !p.toString().endsWith(".class"))) {
			foundFiles = files.toList();
		} catch (IOException ignore) {
		}

		extraFiles = foundFiles;
	}

	public static Optional<String> generateModInfoFilesLogged() {
		var errors = ModAnalyzer.generateModInfoFiles();
		if(errors.isEmpty()) {
			WhoDoesThat.LOGGER.info("Analysis written to: {}, {}", PathConfig.outputFileJson, PathConfig.outputFileCsv);
		} else {
			WhoDoesThat.LOGGER.error(errors.get());
		}
		return errors;
	}

	private static Optional<String> generateModInfoFiles() {
		List<String> errors = new ArrayList<>();

		FullAnalysisResult fullAnalysisResult = ModAnalyzer.analyzeAllMods();
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

	public static FullAnalysisResult analyzeAllMods() {

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

	public ModAnalysisResult analyze() {
		Map<String, List<String>> events = new HashMap<>();
		Map<String, List<String>> inheritance = new HashMap<>();
		Map<String, List<AnnotationResultEntry>> annotations = new HashMap<>();
		Set<String> summary = new HashSet<>();
		Set<String> modifiedClasses = new HashSet<>();

		// TOOD: Attach more data to summary entries, e.g. their source (java, datapack) and a category

		String shortestBasePackage = "";
		for(var classData : scanData.getClasses()) {
			String inClass = classData.clazz().getClassName();
			int lastDot = inClass.lastIndexOf('.');
			if(lastDot != -1) {
				String packageName = inClass.substring(0, inClass.lastIndexOf('.'));
				if(shortestBasePackage.isEmpty() || packageName.length() < shortestBasePackage.length()) {
					shortestBasePackage = packageName;
				}
			}
		}

		ScanDataCache.getAnnotatedBy(scanData, "net.neoforged.bus.api.SubscribeEvent", ElementType.METHOD).forEach(annotationData -> {
			String inClass = annotationData.clazz().getClassName();
			String methodName = annotationData.memberName();
			MethodDataResult methodData = MethodDataResult.fromRaw(methodName);
			if(methodData.argumentTypes().isEmpty() || methodData.argumentTypes().get().isEmpty()) {
				return;
			}
			String event = methodData.argumentTypes().get().getFirst();
			events.computeIfAbsent(event, k -> new ArrayList<>()).add(inClass + "#" + methodData.method());

			DescriptionConfig.get().getEvent(event).ifPresent(eventDescription -> summary.add(eventDescription.description()));
		});

		ScanDataCache.getAnnotatedBy(scanData, "org.spongepowered.asm.mixin.Mixin", ElementType.TYPE).forEach(annotationData -> {
			//noinspection unchecked
			List<Type> values = (List<Type>) annotationData.annotationData().get("value");
			//noinspection unchecked
			List<String> targets = (List<String>) annotationData.annotationData().get("targets");
			if(values != null) {
				for(Type value : values) {
					modifiedClasses.add(value.getClassName());
				}
			}
			if(targets != null) {
				for(String target : targets) {
					modifiedClasses.add(target.replaceAll("/", "."));
				}
			}
		});

		for(GlobDescription getter : DescriptionConfig.get().globs()) {
			Predicate<String> matcher = DescriptionConfig.get().getCompiledGlob(getter).asMatchPredicate();
			boolean matchingFiles = this.extraFiles.stream().anyMatch(path -> matcher.test(path.toString()));
			if(!matchingFiles) {
				continue;
			}

			summary.add(getter.description());
		}

		for(AnnotationDescription getter : DescriptionConfig.get().annotations()) {
			String key = getter.getKey();
			AnnotationResult result = getter.getData(scanData);
			if(result.isEmpty()) {
				continue;
			}
			annotations.put(key, result.annotations());
			summary.add(getter.description());
		}

		for(InheritanceDescription getter : DescriptionConfig.get().inheritance()) {
			String key = getter.getKey();
			InheritanceResult result = getter.getData(scanData);
			if(result.isEmpty()) {
				continue;
			}
			inheritance.put(key, result.classes());
			summary.add(getter.description());
		}

		List<String> sortedSummary = new ArrayList<>(summary);
		Collections.sort(sortedSummary);

		List<String> sortedModifiedClasses = new ArrayList<>(modifiedClasses);
		Collections.sort(sortedModifiedClasses);

		var modInfo = StringMapResult.modInfo(mod);
		if(shortestBasePackage != null && !shortestBasePackage.isEmpty()) {
			modInfo.entries().put("package", shortestBasePackage);
		}

		return new ModAnalysisResult(
			OptionalHelper.optionalOfMap(events),
			OptionalHelper.optionalOfMap(inheritance),
			OptionalHelper.optionalOfMap(annotations),
			OptionalHelper.optionalOfCollection(sortedSummary),
			OptionalHelper.optionalOfCollection(sortedModifiedClasses),
			modInfo,
			StringMapResult.dependencies(mod)
		);
	}

	public String getModId() {
		return mod.getModId();
	}

	public int getClassesCount() {
		return scanData.getClasses().size();
	}

}
