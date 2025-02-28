package com.davenonymous.whodoesthat.data.result;

import com.davenonymous.whodoesthat.config.ActionConfig;
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
				if(value.isBlank()) {
					return;
				}

				if(key.equals("description") && !value.matches("^[a-zA-Z0-9?!._\\-\\s]+$")) {
					List<String> descriptionLines = Arrays.stream(value.split("\n"))
						.map(String::trim)
						.filter(s -> !s.isBlank())
						.toList();

					if(descriptionLines.isEmpty()) {
						return;
					}

					yaml.append("  ").append(key).append(": |-\n");
					descriptionLines.forEach(line -> yaml.append("    ").append(line).append("\n"));
					return;
				}
				yaml.append("  ").append(key).append(": ").append(value.replaceAll("\n", "").trim()).append("\n");
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

	private String quote(String value) {
		boolean alwaysQuote = ActionConfig.csvAlwaysQuote;
		String delimiter = ActionConfig.csvDelimiter;

		if(alwaysQuote || value.contains(delimiter) || value.contains("\"") || value.contains("\n")) {
			return "\"" + value.replaceAll("\"", "\"\"") + "\"";
		} else {
			return value;
		}
	}

	public Optional<String> csvTable() {
		List<String> columns = getSummaryColumns();
		String delimiter = ActionConfig.csvDelimiter;

		StringBuilder csv = new StringBuilder();
		csv.append(quote("Mod"));
		csv.append(delimiter);
		csv.append(String.join(delimiter, columns.stream().map(this::quote).toList()));
		csv.append("\n");

		forEachMod((modId, modAnalysisResult) -> {
			csv.append(quote(modId));
			csv.append(delimiter);
			Set<String> modSummary = new HashSet<>(modAnalysisResult.summary().orElseGet(Collections::emptyList));
			columns.forEach(column -> {
				csv.append(quote(modSummary.contains(column) ? "X" : ""));
				csv.append(delimiter);
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
