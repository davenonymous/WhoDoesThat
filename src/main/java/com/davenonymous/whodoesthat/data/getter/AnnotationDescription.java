package com.davenonymous.whodoesthat.data.getter;

import com.davenonymous.whodoesthat.data.ScanDataCache;
import com.davenonymous.whodoesthat.data.StringyElementType;
import com.davenonymous.whodoesthat.data.result.AnnotationResult;
import com.davenonymous.whodoesthat.data.result.AnnotationResultEntry;
import com.davenonymous.whodoesthat.data.result.MethodDataResult;
import com.davenonymous.whodoesthat.util.OptionalHelper;
import net.neoforged.neoforgespi.language.ModFileScanData;

import java.util.*;

public record AnnotationDescription(String id, String on, String description, List<String> params) implements ISortingValueProvider, IDataGetter<AnnotationResult> {
	public static AnnotationDescription forClass(Class<?> annotationClass, String description, StringyElementType elementType, String... params) {
		return new AnnotationDescription(annotationClass.getName(), elementType.getSerializedName(), description, Arrays.asList(params));
	}

	@Override
	public String getKey() {
		return id;
	}

	@Override
	public void appendYaml(StringBuilder yaml, String indent) {
		yaml.append(indent).append("- id: ").append(id).append("\n");
		yaml.append(indent).append("  description: ").append(description).append("\n");
		yaml.append(indent).append("  type: ").append(on).append("\n");
		yaml.append(indent).append("  params:").append("\n");
		for(String param : params) {
			yaml.append(indent).append("    - ").append(param).append("\n");
		}
	}

	@Override
	public AnnotationResult getData(ModFileScanData data) {
		List<AnnotationResultEntry> entries = new ArrayList<>();
		ScanDataCache.getAnnotatedBy(data, id, StringyElementType.byKey(on).getElementType()).forEach(annotationData -> {
			Map<String, String> annotationDataMap = new HashMap<>();
			for(String value : this.params()) {
				if(!annotationData.annotationData().containsKey(value)) {
					continue;
				}

				annotationDataMap.put(value, annotationData.annotationData().get(value).toString());
			}

			String className = annotationData.clazz().getClassName();
			String memberName = annotationData.memberName();
			Optional<MethodDataResult> optMemberData = Optional.empty();
			if(!className.equals(memberName)) {
				optMemberData = Optional.of(MethodDataResult.fromRaw(memberName));
			}

			AnnotationResultEntry resultEntry = new AnnotationResultEntry(
				className,
				optMemberData,
				OptionalHelper.optionalOfMap(annotationDataMap)
			);
			entries.add(resultEntry);
		});

		return new AnnotationResult(entries);
	}
}
