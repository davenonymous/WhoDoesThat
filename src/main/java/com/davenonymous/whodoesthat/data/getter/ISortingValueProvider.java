package com.davenonymous.whodoesthat.data.getter;

public interface ISortingValueProvider {
	String id();

	void appendYaml(StringBuilder yaml, String indent);
}
