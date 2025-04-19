package com.davenonymous.whodoesthat.setup;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.config.GuiConfig;
import com.davenonymous.whodoesthat.config.PathConfig;
import com.davenonymous.whodoesthat.config.ReportsConfig;
import com.davenonymous.whodoesthat.gui.menu.ProgressEvent;
import com.davenonymous.whodoesthat.gui.menu.TitleScreenAddon;
import com.davenonymous.whodoesthat.util.JarHelper;
import com.davenonymous.whodoesthatlib.api.GsonHelper;
import com.davenonymous.whodoesthatlib.api.IConfig;
import com.davenonymous.whodoesthatlib.api.IJarScanner;
import com.davenonymous.whodoesthatlib.api.IProgressTracker;
import com.davenonymous.whodoesthatlib.api.result.IDependencyInfo;
import com.davenonymous.whodoesthatlib.api.result.IModInfo;
import com.davenonymous.whodoesthatlib.api.result.IModdedJarInfo;
import com.davenonymous.whodoesthatlib.api.result.IScanResult;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class ScanHelper {
	public static IProgressTracker scanProgress = null;

	private static Optional<TitleScreenAddon> findScreenAddon() {
		var screen = Minecraft.getInstance().screen;
		if(!screen.getClass().getName().matches(GuiConfig.menuScreenClass)) {
			return Optional.empty();
		}
		var optionalRenderable = screen.renderables.stream()
			.filter(renderable -> renderable instanceof TitleScreenAddon)
			.findFirst();
		if(optionalRenderable.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of((TitleScreenAddon) optionalRenderable.get());
	}

	private static Optional<IScanResult> runScan() {
		IJarScanner scanner = IJarScanner.get();

		if(ReportsConfig.includeAnalysis) {
			scanner.config().addOutput(IConfig.IncludedOutput.analysis);
		}
		if(ReportsConfig.includeFiles) {
			scanner.config().addOutput(IConfig.IncludedOutput.files);
		}
		if(ReportsConfig.includeClasses) {
			scanner.config().addOutput(IConfig.IncludedOutput.classes);
		}
		if(ReportsConfig.includeMethods) {
			scanner.config().addOutput(IConfig.IncludedOutput.methods);
		}
		if(ReportsConfig.includeFields) {
			scanner.config().addOutput(IConfig.IncludedOutput.fields);
		}
		if(ReportsConfig.includeAnnotations) {
			scanner.config().addOutput(IConfig.IncludedOutput.annotations);
		}
		if(ReportsConfig.includeTags) {
			scanner.config().addOutput(IConfig.IncludedOutput.tags);
		}
		if(ReportsConfig.includeSummary) {
			scanner.config().addOutput(IConfig.IncludedOutput.summary);
		}

		scanner.config().setModsBlacklist(ReportsConfig.modBlacklist);

		Set<Path> modJars = new HashSet<>();
		for(IModFileInfo mod : ModList.get().getModFiles()) {
			Path jarPath = mod.getFile().getFilePath();
			if(jarPath.toString().endsWith(".jar")) {
				modJars.add(jarPath);
			}
		}
		scanner.addAnalysisPath(modJars);

		for(String pathSpec : PathConfig.additionalPaths) {
			Path path = Path.of(pathSpec);
			if(Files.exists(path)) {
				scanner.addAnalysisPath(path);
			} else {
				WhoDoesThat.LOGGER.warn("Path {} does not exist, skipping.", pathSpec);
			}
		}

		scanner.addDescriptorConfigPath(PathConfig.configPath);

		scanner.addProgressListener((progress, event) -> {
			scanProgress = progress;
			WhoDoesThat.LOGGER.info(String.format(
				"%5.01f%% jars=%3d/%-3d, mods=%-3d: %s",
				progress.getProgress() * 100,
				progress.analyzedJars(), progress.scannedJars(),
				progress.foundMods(), event
			));
			findScreenAddon().ifPresent(addon -> {
				addon.getOrCreateGui().fireEvent(new ProgressEvent(progress, event));
			});
		});

		try {
			scanner.loadDescriptors();
		} catch (IOException e) {
			WhoDoesThat.LOGGER.info("Unable to load descriptors: {}", e.getMessage());
		}

		final long scanStartTime = System.nanoTime();
		try {
			WhoDoesThat.LAST_ANALYSIS = scanner.process();
			findScreenAddon().ifPresent(addon -> {
				addon.getOrCreateGui().fireEvent(new ProgressEvent(scanProgress, "Done"));
			});
		} catch (IOException e) {
			WhoDoesThat.LOGGER.info("Unable to process jars: {}", e.getMessage());
		}
		WhoDoesThat.LOGGER.info("Processed {} jars in {} ms", WhoDoesThat.LAST_ANALYSIS.jars().size(), (System.nanoTime() - scanStartTime) / 1000000);

		if(WhoDoesThat.LAST_ANALYSIS == null) {
			return Optional.empty();
		}

		var jsonResult = WhoDoesThat.LAST_ANALYSIS.asJson();
		jsonResult.ifPresent(json -> {
			try {
				Files.writeString(PathConfig.outputFileJson, GsonHelper.toStableString(json));
			} catch (IOException e) {
				WhoDoesThat.LOGGER.warn("Could not write json: {}", e.getMessage());
			}
		});


		WhoDoesThat.reverseDependencies.clear();
		for(var jarInfo : WhoDoesThat.LAST_ANALYSIS.jars()) {
			if(jarInfo instanceof IModdedJarInfo<?> moddedJarInfo) {
				for(IModInfo mod : moddedJarInfo.mods()) {
					for(IDependencyInfo dependency : mod.dependencies()) {
						WhoDoesThat.reverseDependencies.computeIfAbsent(dependency.modId(), k -> new HashSet<>()).add(mod.modId());
					}
				}
			}
		}

		return Optional.ofNullable(WhoDoesThat.LAST_ANALYSIS);
	}

	private static void extractDefaultConfigs() {
		try {
			JarHelper.extractDefaultConfigs();
		} catch (IOException e) {
			WhoDoesThat.LOGGER.warn("Unable to extract default configs: {}", e.getMessage());
		}
	}

	public static Optional<IScanResult> generateModInfoFiles() {
		WhoDoesThat.LOGGER.info("Starting mod analysis in main thread.");
		extractDefaultConfigs();
		return runScan();
	}

	public static CompletableFuture<Optional<IScanResult>> generateModInfoFilesAsync() {
		return CompletableFuture.supplyAsync(
			() -> {
				WhoDoesThat.LOGGER.info("Starting mod analysis in background thread.");
				extractDefaultConfigs();
				return runScan();
			}, Executors.newSingleThreadExecutor()
		);
	}
}
