package com.davenonymous.whodoesthat.util;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.config.PathConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class JarHelper {
	public static void extractDefaultConfigs() throws IOException {
		if(!Files.exists(PathConfig.configPath)) {
			Files.createDirectories(PathConfig.configPath);
		}

		var customContentPath = PathConfig.configPath.resolve("custom_content.yaml");
		var systemsPath = PathConfig.configPath.resolve("systems.yaml");
		var modificationsPath = PathConfig.configPath.resolve("modifications.yaml");

		extractResource("default_configs/custom_content.yaml", customContentPath);
		extractResource("default_configs/systems.yaml", systemsPath);
		extractResource("default_configs/modifications.yaml", modificationsPath);
	}

	private static void extractResource(String resource, Path path) {
		try(InputStream is = JarHelper.class.getClassLoader().getResourceAsStream(resource)) {
			if(is == null) {
				WhoDoesThat.LOGGER.error("Failed to extract resource \"{}\": Resource not found", resource);
				return;
			}

			Files.copy(is, path);
		} catch (Exception e) {
			WhoDoesThat.LOGGER.error("Failed to extract resource \"{}\": {}", resource, e.getMessage());
		}

	}
}
