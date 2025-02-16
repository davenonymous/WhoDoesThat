package com.davenonymous.whodoesthat.data;

import com.davenonymous.whodoesthat.data.getter.*;

import java.util.LinkedList;
import java.util.List;

public class FullConfigBuilder {
	private final List<AnnotationDescription> annotations;
	private final List<EventDescription> events;
	private final List<InheritanceDescription> inheritance;
	private final List<MixinDescription> mixins;
	private final List<GlobDescription> globs;

	public FullConfigBuilder() {
		this.annotations = new LinkedList<>();
		this.events = new LinkedList<>();
		this.inheritance = new LinkedList<>();
		this.mixins = new LinkedList<>();
		this.globs = new LinkedList<>();
	}

	public FullConfigBuilder mixin(Class<?> mixinClass, String description) {
		mixins.add(new MixinDescription(mixinClass.getName(), description));
		return this;
	}

	public FullConfigBuilder event(Class<?> eventClass, String description) {
		events.add(new EventDescription(eventClass.getName(), description));
		return this;
	}

	public FullConfigBuilder annotation(Class<?> annotationClass, StringyElementType type, String description, String... params) {
		annotations.add(new AnnotationDescription(annotationClass.getName(), type.getSerializedName(), description, List.of(params)));
		return this;
	}

	public FullConfigBuilder inheritance(Class<?> parent, String description) {
		inheritance.add(new InheritanceDescription(parent.getName(), description));
		return this;
	}

	public FullConfigBuilder hasFile(String glob, String description) {
		globs.add(new GlobDescription(glob, description));
		return this;
	}

	public FullConfig build() {
		return new FullConfig(annotations, events, inheritance, mixins, globs);
	}
}
