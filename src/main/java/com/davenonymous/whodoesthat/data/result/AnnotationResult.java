package com.davenonymous.whodoesthat.data.result;

import com.mojang.serialization.Codec;

import java.util.List;

public record AnnotationResult(List<AnnotationResultEntry> annotations) implements WithCodec<AnnotationResult> {
	public static final Codec<AnnotationResult> CODEC = AnnotationResultEntry.CODEC.codec().listOf().xmap(AnnotationResult::new, AnnotationResult::annotations);

	public boolean isEmpty() {
		return annotations.isEmpty();
	}

	public Codec<AnnotationResult> codec() {
		return CODEC;
	}
}
