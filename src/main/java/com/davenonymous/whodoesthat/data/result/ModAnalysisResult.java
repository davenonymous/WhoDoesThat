package com.davenonymous.whodoesthat.data.result;

import com.davenonymous.whodoesthat.config.ActionConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;

public record ModAnalysisResult(
	Optional<Map<String, List<String>>> events,
	Optional<Map<String, List<String>>> inheritance,
	Optional<Map<String, List<AnnotationResultEntry>>> annotations,
	Optional<List<String>> summary,
	Optional<List<String>> modifiedClasses,
	StringMapResult modInfoResult,
	StringMapResult dependencies
) implements WithCodec<ModAnalysisResult>
{

	public static final MapCodec<ModAnalysisResult> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
		return instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()).optionalFieldOf("events").forGetter(ModAnalysisResult::events),
			Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()).optionalFieldOf("inheritance").forGetter(ModAnalysisResult::inheritance),
			Codec.unboundedMap(Codec.STRING, AnnotationResultEntry.CODEC.codec().listOf()).optionalFieldOf("annotations").forGetter(ModAnalysisResult::annotations),
			Codec.STRING.listOf().optionalFieldOf("summary").forGetter(ModAnalysisResult::summary),
			Codec.STRING.listOf().optionalFieldOf("modifies").forGetter(ModAnalysisResult::modifiedClasses),
			StringMapResult.CODEC.fieldOf("info").forGetter(ModAnalysisResult::modInfoResult),
			StringMapResult.CODEC.fieldOf("dependencies").forGetter(ModAnalysisResult::dependencies)
		).apply(instance, ModAnalysisResult::new);
	});

	public Map<String, String> dependenciesWithoutDisabledMods() {
		Map<String, String> result = new HashMap<>(dependencies.entries());
		ActionConfig.modBlacklist.forEach(result::remove);
		return result;
	}

	public Set<String> summarySet() {
		return summary.map(HashSet::new).orElseGet(HashSet::new);
	}

	public Codec<ModAnalysisResult> codec() {
		return CODEC.codec();
	}
}
