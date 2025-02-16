package com.davenonymous.whodoesthat.data.result;

import com.mojang.serialization.Codec;

import java.util.List;

public record EventResult(List<String> events) implements WithCodec<EventResult> {
	public static final Codec<EventResult> CODEC = Codec.STRING.listOf().xmap(EventResult::new, EventResult::events);

	public boolean isEmpty() {
		return events.isEmpty();
	}

	public Codec<EventResult> codec() {
		return CODEC;
	}
}
