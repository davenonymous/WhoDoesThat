package com.davenonymous.whodoesthat.util;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class OptionalHelper {
	public static <T extends Map<?, ?>> Optional<T> optionalOfMap(T map) {
		if(map == null || map.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(map);
	}

	public static <T extends Collection<?>> Optional<T> optionalOfCollection(T collection) {
		if(collection == null || collection.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(collection);
	}

	public static <T> Optional<T> firstNonEmpty(Optional<T>... optionals) {
		for(Optional<T> optional : optionals) {
			if(optional.isPresent()) {
				return optional;
			}
		}
		return Optional.empty();
	}
}
