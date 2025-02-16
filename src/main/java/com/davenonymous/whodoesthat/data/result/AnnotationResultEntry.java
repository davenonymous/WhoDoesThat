package com.davenonymous.whodoesthat.data.result;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.Optional;

public record AnnotationResultEntry(String className, Optional<MethodDataResult> method, Optional<Map<String, String>> params) implements WithCodec<AnnotationResultEntry> {
	public static final MapCodec<AnnotationResultEntry> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
		return instance.group(
			Codec.STRING.fieldOf("class").forGetter(AnnotationResultEntry::className),
			MethodDataResult.CODEC.codec().optionalFieldOf("method").forGetter(AnnotationResultEntry::method),
			Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("params").forGetter(AnnotationResultEntry::params)
		).apply(instance, AnnotationResultEntry::new);
	});

	public Codec<AnnotationResultEntry> codec() {
		return CODEC.codec();
	}
}
