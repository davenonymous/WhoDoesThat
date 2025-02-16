package com.davenonymous.whodoesthat.data;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.util.*;
import java.util.stream.Stream;

public class ScanDataCache {
	private static final Map<String, Set<String>> parentToChildren;
	private static final Map<String, Set<String>> classToParents;
	private static final List<String> allEvents;

	static {
		parentToChildren = new HashMap<>();
		classToParents = new HashMap<>();
		allEvents = new ArrayList<>();

		// First pass: collect direct relationships
		ModList.get().getAllScanData().forEach(scanData -> {

			// Build direct inheritance maps
			for(ModFileScanData.ClassData classData : scanData.getClasses()) {
				if(classData.clazz() == null || classData.parent() == null) {
					continue;
				}

				String className = classData.clazz().getClassName();
				String parentName = classData.parent().getClassName();

				// Add direct parent
				classToParents.computeIfAbsent(className, k -> new HashSet<>()).add(parentName);
				parentToChildren.computeIfAbsent(parentName, k -> new HashSet<>()).add(className);

				// Add interfaces
				for(Type interfaceType : classData.interfaces()) {
					String interfaceName = interfaceType.getClassName();
					classToParents.computeIfAbsent(className, k -> new HashSet<>()).add(interfaceName);
					parentToChildren.computeIfAbsent(interfaceName, k -> new HashSet<>()).add(className);
				}
			}
		});

		// Second pass: add all ancestor relationships
		boolean changed;
		do {
			changed = false;
			for(Map.Entry<String, Set<String>> entry : classToParents.entrySet()) {
				String className = entry.getKey();
				Set<String> parents = new HashSet<>(entry.getValue());

				for(String parent : parents) {
					Set<String> grandParents = classToParents.getOrDefault(parent, Collections.emptySet());
					if(entry.getValue().addAll(grandParents)) {
						changed = true;

						// Update parentToChildren for reverse lookup
						for(String grandParent : grandParents) {
							parentToChildren.computeIfAbsent(grandParent, k -> new HashSet<>()).add(className);
						}
					}
				}
			}
		} while(changed);

		ModFileScanData neoforgeScanResult = ModList.get().getModFileById("neoforge").getFile().getScanResult();
		neoforgeScanResult.getClasses().stream()
			.filter(classData -> doesInheritFrom(classData, "net.neoforged.bus.api.Event"))
			.forEach(classData -> allEvents.add(classData.clazz().getClassName()));
	}

	public static Stream<ModFileScanData.AnnotationData> getAnnotatedBy(ModFileScanData scanData, String annotationClassName, ElementType elementType) {
		return scanData.getAnnotations().stream()
			.filter(ad -> ad.targetType() == elementType && ad.annotationType().getClassName().equals(annotationClassName));
	}

	public static List<String> getKnownEvents() {
		return allEvents;
	}

	public static boolean doesInheritFrom(ModFileScanData.ClassData classData, String targetClassName) {
		String className = classData.clazz().getClassName();

		// Check if it's the same class
		if(className.equals(targetClassName)) {
			return true;
		}

		// Check direct and indirect parents
		Set<String> parents = classToParents.get(className);
		return parents != null && parents.contains(targetClassName);
	}
}
