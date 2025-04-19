package com.davenonymous.whodoesthat.gui.overview.detail.classes;

import com.davenonymous.whodoesthat.lib.gui.ISelectable;
import com.davenonymous.whodoesthat.lib.gui.Icons;
import com.davenonymous.whodoesthat.lib.gui.event.ListEntrySelectionEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetImage;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetPanel;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTextBox;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import com.davenonymous.whodoesthatlib.api.result.asm.*;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ClassWidget extends WidgetPanel implements ISelectable {
	private boolean selected = false;
	private boolean isPackage = false;
	private IJarInfo jar;
	private IClassInfo classInfo;
	private String className;
	private WidgetTextBox label;
	private WidgetImage icon;

	public ClassWidget(IJarInfo jar, IClassInfo classInfo, String className, boolean isPackage) {
		super();
		this.jar = jar;
		this.className = className;
		this.isPackage = isPackage;
		this.classInfo = classInfo;

		this.label = new WidgetTextBox(className.substring(className.lastIndexOf(".") + 1))
			.setStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));
		this.label.autoWidth();
		this.label.setX(10);

		ResourceLocation iconId = Icons.javaClasses;
		if(classInfo != null) {
			if(classInfo.isAnnotatedWith("org.spongepowered.asm.mixin.Mixin").isPresent()) {
				iconId = Icons.javaMixin;
			} else if(classInfo.inherits().getClassName().equals("java.lang.Record")) {
				iconId = Icons.javaRecord;
			} else if(classInfo.inherits().getClassName().equals("java.lang.Enum")) {
				iconId = Icons.javaEnum;
			} else if(classInfo.access().isInterface()) {
				iconId = Icons.javaInterface;
			} else if(classInfo.access().isAbstract()) {
				iconId = Icons.javaAbstract;
			} else if(classInfo.isAnnotatedWith("net.neoforged.fml.common.Mod").isPresent()) {
				iconId = Icons.javaForgeMod;
			}
		}
		if(isPackage()) {
			iconId = Icons.folderStar;
		}
		this.icon = new WidgetImage(iconId);
		this.icon.setY(0);
		this.icon.setSize(8, 8);
		this.icon.setTextureSize(16, 16);
		this.add(icon);


		this.height = 12;

		this.add(label);

		this.addListener(
			ListEntrySelectionEvent.class, (event, widget) -> {
				if(event.selected) {
					this.label.setStyle(style -> style.withColor(ChatFormatting.WHITE));
				} else {
					this.label.setStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));
				}
				return WidgetEventResult.HANDLED;
			}
		);
	}

	public ClassWidget setText(String text) {
		label.setText(text);
		label.autoWidth();
		return this;
	}

	public WidgetTextBox getLabelWidget() {
		return label;
	}


	private void appendAnnotations(StringBuilder sb, String indent, Collection<IAnnotationInfo> annotations) {
		for(IAnnotationInfo annotation : annotations) {
			var annotationClassName = annotation.type().getClassName();
			int pos = annotationClassName.lastIndexOf('.');
			sb.append(indent).append("§e@");
			if(pos == -1) {
				sb.append(annotationClassName);
			} else {
				sb.append(annotationClassName.substring(pos + 1));
			}

			if(!annotation.params().isEmpty()) {
				sb.append("§6(§r");
				var params = annotation.params().keySet().stream().sorted().toList();
				for(String param : params) {
					var value = annotation.params().get(param);
					if(value != null) {
						sb.append(param).append("§6=§7\"").append(value).append("\"§r");
					} else {
						sb.append(param);
					}

					if(params.indexOf(param) < params.size() - 1) {
						sb.append("§6,§r ");
					}
				}
				sb.append("§6)§r");
			}


			sb.append("§r\n");
		}
	}

	public String appendAccess(IAccessBits access) {
		StringBuilder sb = new StringBuilder();
		if(access.isPublic()) {
			sb.append("§6public§r ");
		} else if(access.isProtected()) {
			sb.append("§6protected§r ");
		} else if(access.isPrivate()) {
			sb.append("§6private§r ");
		}
		if(access.isStatic()) {
			sb.append("§6static§r ");
		}
		if(access.isFinal()) {
			sb.append("§6final§r ");
		}
		if(access.isAbstract()) {
			sb.append("§6abstract§r ");
		}
		return sb.toString();
	}


	public String getListing() {
		String semicolon = "§7;§r\n";
		StringBuilder sb = new StringBuilder()
			.append(String.format("§6package§r %s", classInfo.getPackageName()))
			.append(semicolon).append("\n");

		Set<String> ignoredTypes = Set.of(
			"java.lang.Object", "java.lang.String", "java.lang.Class",
			"boolean", "byte", "char", "double", "float", "int", "long", "short",
			"void", "java.lang.Void", "java.lang.Boolean", "java.lang.Byte",
			"java.lang.Character", "java.lang.Double", "java.lang.Float",
			"java.lang.Integer", "java.lang.Long", "java.lang.Short",
			"java.lang.annotation.Annotation", "java.lang.Enum", "java.lang.Throwable",
			"java.lang.Exception", "java.lang.RuntimeException", "java.lang.Error",
			"java.lang.Record"
		);

		boolean hasImports = false;
		for(var type : classInfo.usedTypes().stream().sorted(Comparator.comparing(Type::getClassName)).toList()) {
			if(type.getClassName().equals(classInfo.getClassName())) {
				continue;
			}
			if(ignoredTypes.contains(type.getClassName().replaceAll("\\[]$", ""))) {
				continue;
			}

			hasImports = true;
			sb.append("§6import§r ");
			sb.append(type.getClassName())
				.append(semicolon);
		}

		if(hasImports) {
			sb.append("\n");
		}

		appendAnnotations(sb, "", classInfo.annotations());

		boolean isRecord = classInfo.inherits() != null && classInfo.inherits().getClassName().equals("java.lang.Record");
		boolean isEnum = classInfo.inherits() != null && classInfo.inherits().getClassName().equals("java.lang.Enum");
		if(isRecord) {
			sb.append("§6record§r ");
		} else if(isEnum) {
			sb.append("§6enum§r ");
		} else if(classInfo.access().isInterface()) {
			sb.append("§6interface§r ");
		} else if(classInfo.access().isAbstract()) {
			sb.append("§6abstract class§r ");
		} else {
			sb.append("§6class§r ");
		}

		sb.append(classInfo.getSimpleName());
		Set<String> skippedInheritance = Set.of("java.lang.Object", "java.lang.Record", "java.lang.Enum");
		if(classInfo.inherits() != null && !skippedInheritance.contains(classInfo.inherits().getClassName())) {
			sb.append(" §6extends§r §5");
			var superClass = classInfo.inherits().getClassName();
			int pos = superClass.lastIndexOf('.');
			if(pos == -1) {
				sb.append(superClass);
			} else {
				sb.append(superClass.substring(pos + 1));
			}
			sb.append("§r");
		}
		if(!classInfo.interfaces().isEmpty()) {
			sb.append(" §6implements§r ");
			List<Type> interfaces = classInfo.interfaces().stream().sorted(Comparator.comparing(Type::getClassName)).toList();
			for(Type interfaceType : interfaces) {
				var interfaceClassName = interfaceType.getClassName();
				int pos = interfaceClassName.lastIndexOf('.');
				sb.append("§f");
				if(pos == -1) {
					sb.append(interfaceClassName);
				} else {
					sb.append(interfaceClassName.substring(pos + 1));
				}
				sb.append("§r");
				if(interfaces.indexOf(interfaceType) < interfaces.size() - 1) {
					sb.append("§6,§r ");
				}
			}
		}
		sb.append(" §6{§r\n");


		List<IFieldInfo> fields = classInfo.fields().stream().sorted(Comparator.comparing(IFieldInfo::name)).toList();
		for(IFieldInfo field : fields) {
			if(isEnum) {
				Set<String> enumMethods = Set.of("$VALUES");
				if(enumMethods.contains(field.name())) {
					continue;
				}
			}
			appendAnnotations(sb, "  ", field.annotations());

			var fieldClassName = field.type().getClassName();
			int pos = fieldClassName.lastIndexOf('.');
			sb.append("  ").append(appendAccess(field.access()));
			sb.append("§f");
			if(pos == -1) {
				sb.append(fieldClassName);
			} else {
				sb.append(fieldClassName.substring(pos + 1));
			}

			sb.append("§r").append(" §9").append(field.name()).append("§r").append(semicolon);
		}

		if(!fields.isEmpty()) {
			sb.append("\n");
		}

		List<IMethodInfo> methods = classInfo.methods().stream().sorted(Comparator.comparing(IMethodInfo::name)).toList();
		for(IMethodInfo method : methods) {

			if(isRecord) {
				Set<String> recordMethods = Set.of("equals", "hashCode", "toString");
				if(recordMethods.contains(method.name())) {
					continue;
				}
			}
			if(isEnum) {
				Set<String> enumMethods = Set.of("$values", "values", "valueOf", "<clinit>", "<init>");
				if(enumMethods.contains(method.name())) {
					continue;
				}
			}
			appendAnnotations(sb, "  ", method.annotations());
			sb.append("  ");
			sb.append(appendAccess(method.access()));
			var methodName = method.name();
			if(methodName.equals("<init>") || methodName.equals("<clinit>")) {
				methodName = classInfo.getSimpleName();
			} else {
				if(method.returnType() == null) {
					sb.append("§fvoid§r ");
				} else {
					sb.append("§f").append(method.getSimpleReturnTypeName()).append("§r ");
				}
			}
			sb.append("§9").append(methodName).append("§r§6(§r");
			for(int i = 0; i < method.parameters().size(); ++i) {
				var param = method.parameters().get(i);
				var paramClassName = param.getClassName();
				int pos = paramClassName.lastIndexOf('.');
				sb.append("§3");
				if(pos == -1) {
					sb.append(paramClassName);
				} else {
					sb.append(paramClassName.substring(pos + 1));
				}
				sb.append("§r");

				if(i < method.parameters().size() - 1) {
					sb.append(", ");
				}
			}
			sb.append("§6)§r").append(semicolon);
		}

		sb.append("§6}§r\n");
		return sb.toString();
	}

	public boolean isPackage() {
		return isPackage;
	}

	public IJarInfo jar() {
		return jar;
	}

	public String path() {
		return className;
	}

	public String getLabel() {
		return label.getText();
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(boolean state) {
		selected = state;
	}
}
