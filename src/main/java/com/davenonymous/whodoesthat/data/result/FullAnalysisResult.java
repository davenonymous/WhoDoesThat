package com.davenonymous.whodoesthat.data.result;

import com.davenonymous.whodoesthat.util.GsonHelper;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;
import java.util.function.BiConsumer;

public record FullAnalysisResult(Map<String, ModAnalysisResult> modAnalysisResults) implements WithCodec<FullAnalysisResult> {
	private static final MapCodec<FullAnalysisResult> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
		return instance.group(
			Codec.unboundedMap(Codec.STRING, ModAnalysisResult.CODEC.codec()).fieldOf("mods").forGetter(FullAnalysisResult::modAnalysisResults)
		).apply(instance, FullAnalysisResult::new);
	});

	public Codec<FullAnalysisResult> codec() {
		return CODEC.codec();
	}

	public List<String> getSummaryColumns() {
		Set<String> columns = new HashSet<>();
		modAnalysisResults.values().forEach(modAnalysisResult -> {
			modAnalysisResult.summary().ifPresent(columns::addAll);
		});
		List<String> sortedColumns = new ArrayList<>(columns);
		Collections.sort(sortedColumns);
		return sortedColumns;
	}

	public void forEachMod(BiConsumer<String, ModAnalysisResult> consumer) {
		modAnalysisResults.keySet().stream().sorted().forEach(modId -> consumer.accept(modId, modAnalysisResults.get(modId)));
	}

	public Optional<String> yaml() {
		StringBuilder yaml = new StringBuilder();
		forEachMod((modId, modAnalysisResult) -> {
			yaml.append(modId).append(":\n");
			modAnalysisResult.modInfoResult().entries().forEach((key, value) -> {
				if(value.replaceAll("\\n", "").replaceAll("\\s", "").isEmpty()) {
					return;
				}

				if(key.equals("description") && !value.matches("[a-zA-Z0-9?!._\\-\\s]+")) {
					yaml.append("  ").append(key).append(": |\n");
					for(String line : value.trim().split("\n")) {
						if(line.trim().isEmpty()) {
							continue;
						}
						yaml.append("    ").append(line.trim()).append("\n");
					}
					return;
				}
				yaml.append("  ").append(key).append(": ").append(value).append("\n");
			});
			var dependencies = modAnalysisResult.dependenciesWithoutDisabledMods();
			if(!dependencies.isEmpty()) {
				yaml.append("  dependencies:\n");
				dependencies.forEach((key, value) -> {
					yaml.append("    ").append(key).append(": \"").append(value).append("\"\n");
				});
			}

			modAnalysisResult.summary().ifPresent(summary -> {
				yaml.append("  summary:\n");
				summary.forEach(s -> yaml.append("    - ").append(s).append("\n"));
			});
			yaml.append("\n");
		});

		return Optional.of(yaml.toString());
	}

	public Optional<String> csvTable() {
		List<String> columns = getSummaryColumns();

		StringBuilder csv = new StringBuilder();
		csv.append("\"Mod\";");
		csv.append(String.join(";", columns.stream().map(s -> "\"" + s + "\"").toList()));
		csv.append("\n");

		forEachMod((modId, modAnalysisResult) -> {
			csv.append("\"");
			csv.append(modId);
			csv.append("\"");
			csv.append(";");
			Set<String> modSummary = new HashSet<>(modAnalysisResult.summary().orElseGet(Collections::emptyList));
			columns.forEach(column -> {
				csv.append(modSummary.contains(column) ? "\"X\"" : "\"\"");
				csv.append(";");
			});
			csv.deleteCharAt(csv.length() - 1);
			csv.append("\n");
		});

		return Optional.of(csv.toString());
	}

	public Optional<String> prettyJson() {
		Optional<JsonElement> json = this.codec().encodeStart(JsonOps.INSTANCE, this).result();
		if(json.isPresent()) {
			String prettyResult = GsonHelper.toStableIndentedString(json.get());
			return Optional.of(prettyResult);
		}
		return Optional.empty();
	}
}
