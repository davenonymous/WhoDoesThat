package com.davenonymous.whodoesthat.data.result;

import com.mojang.serialization.Codec;

import java.util.List;

public record InheritanceResult(List<String> classes) implements WithCodec<InheritanceResult> {
	public static final Codec<InheritanceResult> CODEC = Codec.STRING.listOf().xmap(InheritanceResult::new, InheritanceResult::classes);

	public boolean isEmpty() {
		return classes.isEmpty();
	}

	public Codec<InheritanceResult> codec() {
		return CODEC;
	}
}
