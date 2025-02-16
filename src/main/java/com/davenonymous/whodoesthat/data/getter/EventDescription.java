package com.davenonymous.whodoesthat.data.getter;

public record EventDescription(String id, String description) implements ISortingValueProvider {

	public static EventDescription forClass(Class<?> eventClass, String description) {
		return new EventDescription(eventClass.getName(), description);
	}

	@Override
	public void appendYaml(StringBuilder yaml, String indent) {
		yaml.append(indent).append("- id: ").append(id).append("\n");
		yaml.append(indent).append("  description: ").append(description).append("\n");
	}
}
