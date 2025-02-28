package com.davenonymous.whodoesthat.config;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.data.getter.FullConfig;
import com.davenonymous.whodoesthat.util.JarHelper;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;

public class DescriptionConfig {
	public static final DescriptionConfig INSTANCE = new DescriptionConfig();

	private Yaml yaml;
	private FullConfig fullConfig = FullConfig.empty();

	public static FullConfig get() {
		return INSTANCE.fullConfig;
	}

	private DescriptionConfig() {
		var loaderOptions = new LoaderOptions();
		loaderOptions.setMergeOnCompose(true);

		yaml = new Yaml(loaderOptions);
		try {
			JarHelper.extractDefaultConfigs();
		} catch (IOException e) {
			WhoDoesThat.LOGGER.error("Failed to extract default configs: {}", e.getMessage());
		}

		load();
	}

	public void load() {
		// Load the config files
		try(var paths = Files.walk(PathConfig.configPath)) {
			fullConfig = FullConfig.empty();
			paths
				.filter(Files::isRegularFile)
				.sorted(Comparator.comparing(Path::getFileName))
				.forEach(this::loadFile);
			WhoDoesThat.LOGGER.info("Config: {}", fullConfig);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadFile(Path path) {
		WhoDoesThat.LOGGER.info("Loading config file: {}", path);
		try {
			var content = Files.readString(path);
			Map<String, Object> config = yaml.load(content);
			FullConfig loaded = FullConfig.fromLoadedData(config);
			fullConfig = fullConfig.merge(loaded);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
