package com.davenonymous.whodoesthat.data.result;

import com.davenonymous.whodoesthat.util.OptionalHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public record MethodDataResult(String raw, String method, Optional<String> returnType, Optional<List<String>> argumentTypes) implements WithCodec<MethodDataResult> {
	public static final MapCodec<MethodDataResult> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
		return instance.group(
			Codec.STRING.fieldOf("raw").forGetter(MethodDataResult::raw),
			Codec.STRING.fieldOf("name").forGetter(MethodDataResult::method),
			Codec.STRING.optionalFieldOf("returnType").forGetter(MethodDataResult::returnType),
			Codec.STRING.listOf().optionalFieldOf("argTypes").forGetter(MethodDataResult::argumentTypes)
		).apply(instance, MethodDataResult::new);
	});

	private static String toHumanType(char type) {
		return switch(type) {
			case 'V' -> "void";
			case 'Z' -> "boolean";
			case 'B' -> "byte";
			case 'C' -> "char";
			case 'S' -> "short";
			case 'I' -> "int";
			case 'J' -> "long";
			case 'F' -> "float";
			case 'D' -> "double";
			default -> String.valueOf(type);
		};
	}

	private static List<String> getArgumentList(String rawArgsString) {
		int pos = 0;
		int end = rawArgsString.length();
		List<String> args = new ArrayList<>();
		while(pos < end) {
			char c = rawArgsString.charAt(pos);
			if(c == 'L') {
				int endPos = rawArgsString.indexOf(';', pos);
				args.add(rawArgsString.substring(pos + 1, endPos).replaceAll("/", "."));
				pos = endPos + 1;
			} else {
				args.add(toHumanType(c));
				pos++;
			}
		}
		return args;
	}

	public static MethodDataResult fromRaw(String raw) {
		// canEnchant(Lnet/minecraft/world/item/ItemStack;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V
		Pattern pattern = Pattern.compile("(.*)\\((.*)\\)(.*)");
		var matcher = pattern.matcher(raw);
		if(!matcher.matches()) {
			return new MethodDataResult(raw, null, null, Optional.empty());
		}

		String method = matcher.group(1);
		String argumentTypes = matcher.group(2);
		String returnType = matcher.group(3);

		// Split argument types into a human-readable list
		List<String> argumentTypesList = getArgumentList(argumentTypes);

		// Convert return type to a more human-readable format, e.g. V -> void
		if(returnType.startsWith("L")) {
			returnType = returnType.substring(1, returnType.length() - 1).replaceAll("/", ".");
		} else if(returnType.startsWith("V")) {
			returnType = null;
		} else {
			returnType = toHumanType(returnType.charAt(0));
		}

		return new MethodDataResult(raw, method, Optional.ofNullable(returnType), OptionalHelper.optionalOfCollection(argumentTypesList));
	}

	public Codec<MethodDataResult> codec() {
		return CODEC.codec();
	}
}
