package com.davenonymous.whodoesthat.gui.overview.parts;

public enum ModListSearchables {
	ID("id"),
	NAME("name"),
	AUTHORS("authors"),
	DESCRIPTION("description"),
	TRANSLATIONS("translations"),
	JAR_NAME("jar_name"),
	FILE_NAMES("file_names"),
	CLASS_NAMES("class_names"),
	;

	public String key;

	ModListSearchables(String key) {
		this.key = key;
	}
}
