package com.davenonymous.whodoesthat.data.getter;

import com.davenonymous.whodoesthat.util.GlobHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class FullConfig {
	private final List<AnnotationDescription> annotations;
	private final List<EventDescription> events;
	private final List<InheritanceDescription> inheritance;
	private final List<MixinDescription> mixins;
	private final List<GlobDescription> globs;
	private final Map<String, Pattern> compiledGlobs;

	private final Map<String, AnnotationDescription> memoizedAnnotations;
	private final Map<String, EventDescription> memoizedEvents;
	private final Map<String, InheritanceDescription> memoizedInheritance;
	private final Map<String, MixinDescription> memoizedMixins;
	private final Map<String, GlobDescription> memoizedGlobs;

	public FullConfig(List<AnnotationDescription> annotations, List<EventDescription> events, List<InheritanceDescription> inheritance, List<MixinDescription> mixins, List<GlobDescription> globs) {
		this.annotations = annotations;
		this.events = events;
		this.inheritance = inheritance;
		this.mixins = mixins;
		this.globs = globs;

		this.memoizedAnnotations = annotations.stream().collect(Collectors.toMap(AnnotationDescription::id, a -> a));
		this.memoizedEvents = events.stream().collect(Collectors.toMap(EventDescription::id, a -> a));
		this.memoizedInheritance = inheritance.stream().collect(Collectors.toMap(InheritanceDescription::id, a -> a));
		this.memoizedMixins = mixins.stream().collect(Collectors.toMap(MixinDescription::id, a -> a));
		this.memoizedGlobs = globs.stream().collect(Collectors.toMap(GlobDescription::id, a -> a));

		this.compiledGlobs = globs.stream().collect(Collectors.toMap(
			GlobDescription::glob,
			g -> Pattern.compile(GlobHelper.toUnixRegexPattern(g.glob()))
		));
	}


	public Optional<AnnotationDescription> getAnnotation(String id) {
		return Optional.ofNullable(memoizedAnnotations.get(id));
	}

	public Optional<EventDescription> getEvent(String id) {
		return Optional.ofNullable(memoizedEvents.get(id));
	}

	public Optional<InheritanceDescription> getInheritance(String id) {
		return Optional.ofNullable(memoizedInheritance.get(id));
	}

	public Optional<MixinDescription> getMixin(String id) {
		return Optional.ofNullable(memoizedMixins.get(id));
	}

	public static FullConfig empty() {
		return new FullConfig(
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList()
		);
	}

	public FullConfig merge(FullConfig other) {
		var newAnnotations = new ArrayList<>(this.annotations);
		var newEvents = new ArrayList<>(this.events);
		var newInheritance = new ArrayList<>(this.inheritance);
		var newMixins = new ArrayList<>(this.mixins);
		var newGlobs = new ArrayList<>(this.globs);
		merge(other.annotations, newAnnotations);
		merge(other.events, newEvents);
		merge(other.inheritance, newInheritance);
		merge(other.mixins, newMixins);
		merge(other.globs, newGlobs);

		return new FullConfig(
			newAnnotations,
			newEvents,
			newInheritance,
			newMixins,
			newGlobs
		);
	}

	private static <T extends ISortingValueProvider> void merge(List<T> from, List<T> to) {
		Set<String> existingIds = to.stream().map(ISortingValueProvider::id).collect(Collectors.toSet());
		for(var item : from) {
			if(!existingIds.contains(item.id())) {
				to.add(item);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static FullConfig fromLoadedData(Map<String, Object> config) {
		List<AnnotationDescription> annotations = new ArrayList<>();
		if(config.containsKey("annotations")) {
			for(LinkedHashMap<String, Object> raw : (List<LinkedHashMap<String, Object>>) config.get("annotations")) {
				annotations.add(
					new AnnotationDescription(raw.get("id").toString(), raw.get("type").toString(), raw.get("description").toString(), (List<String>) raw.get("params")));
			}
		}
		List<EventDescription> events = new ArrayList<>();
		if(config.containsKey("events")) {
			for(LinkedHashMap<String, String> raw : (List<LinkedHashMap<String, String>>) config.get("events")) {
				events.add(new EventDescription(raw.get("id"), raw.get("description")));
			}
		}
		List<InheritanceDescription> inheritance = new ArrayList<>();
		if(config.containsKey("inheritance")) {
			for(LinkedHashMap<String, String> raw : (List<LinkedHashMap<String, String>>) config.get("inheritance")) {
				inheritance.add(new InheritanceDescription(raw.get("id"), raw.get("description")));
			}
		}
		List<MixinDescription> mixins = new ArrayList<>();
		if(config.containsKey("mixins")) {
			for(LinkedHashMap<String, String> raw : (List<LinkedHashMap<String, String>>) config.get("mixins")) {
				mixins.add(new MixinDescription(raw.get("id"), raw.get("description")));
			}
		}
		List<GlobDescription> globs = new ArrayList<>();
		if(config.containsKey("files")) {
			for(LinkedHashMap<String, String> raw : (List<LinkedHashMap<String, String>>) config.get("files")) {
				globs.add(new GlobDescription(raw.get("id"), raw.get("glob"), raw.get("description")));
			}
		}

		return new FullConfig(
			annotations,
			events,
			inheritance,
			mixins,
			globs
		);
	}

	public List<AnnotationDescription> annotations() {
		return annotations;
	}

	public List<EventDescription> events() {
		return events;
	}

	public List<InheritanceDescription> inheritance() {
		return inheritance;
	}

	public List<MixinDescription> mixins() {
		return mixins;
	}

	public List<GlobDescription> globs() {
		return globs;
	}

	public Pattern getCompiledGlob(GlobDescription glob) {
		return compiledGlobs.get(glob.glob());
	}

	public String asYaml() {
		StringBuilder output = new StringBuilder();
		if(!annotations.isEmpty()) {
			output.append("annotations:\n");
			for(var annotation : annotations) {
				annotation.appendYaml(output, "  ");
			}
			output.append("\n");
		}

		if(!events.isEmpty()) {
			output.append("events:\n");
			for(var event : events) {
				event.appendYaml(output, "  ");
			}
			output.append("\n");
		}

		if(!globs.isEmpty()) {
			output.append("files:\n");
			for(var glob : globs) {
				glob.appendYaml(output, "  ");
			}
			output.append("\n");
		}

		if(!inheritance.isEmpty()) {
			output.append("inheritance:\n");
			for(var inherit : inheritance) {
				inherit.appendYaml(output, "  ");
			}
			output.append("\n");
		}

		if(!mixins.isEmpty()) {
			output.append("mixins:\n");
			for(var mixin : mixins) {
				mixin.appendYaml(output, "  ");
			}
			output.append("\n");
		}

		return output.toString();
	}

	public boolean writeYaml(Path dst) {
		try {
			var yaml = asYaml();
			Files.writeString(dst, yaml);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		var that = (FullConfig) obj;
		return Objects.equals(this.annotations, that.annotations) &&
			Objects.equals(this.events, that.events) &&
			Objects.equals(this.inheritance, that.inheritance) &&
			Objects.equals(this.mixins, that.mixins) &&
			Objects.equals(this.globs, that.globs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(annotations, events, inheritance, mixins, globs);
	}

	@Override
	public String toString() {
		return "Base[" +
			"annotations=" + annotations + ", " +
			"events=" + events + ", " +
			"files=" + globs + ", " +
			"inheritance=" + inheritance + ", " +
			"mixins=" + mixins + ']';
	}


}
