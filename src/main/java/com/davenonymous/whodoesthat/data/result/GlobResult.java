package com.davenonymous.whodoesthat.data.result;

import com.mojang.serialization.Codec;

import java.util.List;

public record GlobResult(List<String> files) implements WithCodec<GlobResult> {
	public static final Codec<GlobResult> CODEC = Codec.STRING.listOf().xmap(GlobResult::new, GlobResult::files);

	public boolean isEmpty() {
		return files.isEmpty();
	}

	public Codec<GlobResult> codec() {
		return CODEC;
	}
}
