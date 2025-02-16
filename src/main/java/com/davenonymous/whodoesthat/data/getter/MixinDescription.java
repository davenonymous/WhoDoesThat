package com.davenonymous.whodoesthat.data.getter;

public record MixinDescription(String id, String description) implements ISortingValueProvider {
	public static MixinDescription forClass(Class<?> mixinTargetClass, String description) {
		return new MixinDescription(mixinTargetClass.getName(), description);
	}

	@Override
	public void appendYaml(StringBuilder yaml, String indent) {
		yaml.append(indent).append("- id: ").append(id).append("\n");
		yaml.append(indent).append("  description: ").append(description).append("\n");
	}
}
