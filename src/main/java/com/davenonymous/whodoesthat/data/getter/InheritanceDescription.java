package com.davenonymous.whodoesthat.data.getter;

import com.davenonymous.whodoesthat.data.ScanDataCache;
import com.davenonymous.whodoesthat.data.result.InheritanceResult;
import net.neoforged.neoforgespi.language.ModFileScanData;

import java.util.ArrayList;
import java.util.List;

public record InheritanceDescription(String id, String description) implements ISortingValueProvider, IDataGetter<InheritanceResult> {
	public static InheritanceDescription forClass(Class<?> superClass, String description) {
		return new InheritanceDescription(superClass.getName(), description);
	}

	@Override
	public String getKey() {
		return id;
	}

	@Override
	public InheritanceResult getData(ModFileScanData data) {
		List<String> classes = new ArrayList<>();
		data.getClasses().stream()
			.filter(classData -> ScanDataCache.doesInheritFrom(classData, this.id()))
			.forEach(classData -> {
				classes.add(classData.clazz().getClassName());
			});

		return new InheritanceResult(classes);
	}

	@Override
	public void appendYaml(StringBuilder yaml, String indent) {
		yaml.append(indent).append("- id: ").append(id).append("\n");
		yaml.append(indent).append("  description: ").append(description).append("\n");
	}
}
