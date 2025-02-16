package com.davenonymous.whodoesthat.data.result;

import com.mojang.serialization.Codec;
import net.neoforged.fml.VersionChecker;
import net.neoforged.neoforgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record StringMapResult(Map<String, String> entries) implements WithCodec<StringMapResult> {
	public static final Codec<StringMapResult> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(StringMapResult::new, StringMapResult::entries);

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public Codec<StringMapResult> codec() {
		return CODEC;
	}

	public static StringMapResult dependencies(IModInfo modInfo) {
		Map<String, String> entries = new HashMap<>();
		for(IModInfo.ModVersion dependency : modInfo.getDependencies()) {
			entries.put(dependency.getModId(), dependency.getVersionRange().toString());
		}
		return new StringMapResult(entries);
	}

	public static StringMapResult modInfo(IModInfo modInfo) {
		Map<String, String> entries = new HashMap<>();
		Path filePath = modInfo.getOwningFile().getFile().getFilePath();
		if(filePath == null || filePath.toString().isEmpty()) {
			var nestedPath = modInfo.getOwningFile().getFile().getSecureJar().moduleDataProvider().uri().toString();

			Pattern pathMatcher = Pattern.compile("([^/]+\\.jar)");
			Matcher matcher = pathMatcher.matcher(nestedPath);
			if(matcher.find()) {
				String parentJar = matcher.group();
				entries.put("file", parentJar);
				entries.put("jarInJar", modInfo.getOwningFile().getFile().getFileName());
			} else {
				entries.put("file", nestedPath);
			}
		} else {
			entries.put("file", modInfo.getOwningFile().getFile().getFileName());
		}


		modInfo.getConfig().getConfigElement(new String[]{"authors"}).ifPresent(authorsObject -> {
			String authorsString = authorsObject.toString()
				.replaceAll("\n", ", ");

			entries.put("authors", authorsString);
		});

		entries.put("displayName", modInfo.getDisplayName());
		entries.put("version", modInfo.getVersion().toString());
		VersionChecker.CheckResult vercheck = VersionChecker.getResult(modInfo);
		if(vercheck.status() == VersionChecker.Status.OUTDATED || vercheck.status() == VersionChecker.Status.BETA_OUTDATED) {
			entries.put("latest", vercheck.target().toString());
		}

		entries.put("license", modInfo.getOwningFile().getLicense());
		entries.put("description", modInfo.getDescription());
		entries.put("classes", modInfo.getOwningFile().getFile().getScanResult().getClasses().size() + "");

		if(!modInfo.getLoader().name().equals("javafml")) {
			entries.put("loader", modInfo.getLoader().name());
		}

		modInfo.getModURL().ifPresent(url -> entries.put("url", url.toString()));
		modInfo.getUpdateURL().ifPresent(url -> entries.put("updateUrl", url.toString()));

		return new StringMapResult(entries);
	}
}
