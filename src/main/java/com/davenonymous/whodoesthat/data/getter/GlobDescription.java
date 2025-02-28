package com.davenonymous.whodoesthat.data.getter;

public record GlobDescription(String id, String glob, String description) implements ISortingValueProvider {
	@Override
	public String id() {
		return id;
	}

	@Override
	public void appendYaml(StringBuilder yaml, String indent) {
		yaml.append(indent).append("- id: ").append(id).append("\n");
		yaml.append(indent).append("  glob: \"").append(glob).append("\"\n");
		yaml.append(indent).append("  description: ").append(description).append("\n");
	}
}
