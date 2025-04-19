package com.davenonymous.whodoesthat.gui.overview;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.lib.gui.*;
import com.davenonymous.whodoesthat.lib.gui.event.VisibilityChangedEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetSizeChangeEvent;
import com.davenonymous.whodoesthat.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetImage;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetSelectablePanelWithValue;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetSprite;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTextBox;
import com.davenonymous.whodoesthatlib.api.result.IDependencyInfo;
import com.davenonymous.whodoesthatlib.api.result.IModInfo;
import com.davenonymous.whodoesthatlib.api.result.IModdedJarInfo;
import com.davenonymous.whodoesthatlib.api.result.fabric.IFabricModInfo;
import com.davenonymous.whodoesthatlib.api.result.neoforge.INeoForgeJarInfo;
import com.davenonymous.whodoesthatlib.api.result.neoforge.INeoForgeModInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;

public class ModWidget extends WidgetSelectablePanelWithValue<IModdedJarInfo<?>> implements ISelectable {
	public static final ResourceLocation BACKGROUND = WhoDoesThat.resource("textures/gui/window.png");
	public static final ResourceLocation BACKGROUND_SELECTED = WhoDoesThat.resource("textures/gui/window_pushed.png");

	public IModdedJarInfo<?> jarInfo;
	public IModInfo modInfo;
	WidgetTextBox modName;
	WidgetTextBox version;
	WidgetTextBox description;
	WidgetSprite missingSprite;
	WidgetImage modImage;
	ResourceLocation loadedImage;
	WidgetImage modLoaderImage;
	WidgetImage dependenciesImage;
	WidgetImage jarInJarImage;
	WidgetImage closedSourceImage;

	int fullWidth;
	boolean imageLoaded = false;

	public ModWidget(IModdedJarInfo<?> jarInfo, IModInfo modInfo, int width) {
		super();
		this.jarInfo = jarInfo;
		this.modInfo = modInfo;
		this.height = 44;
		this.width = width;
		this.fullWidth = width;
		this.setVisible(false);


		modName = new WidgetTextBox(modInfo.displayName());
		modName.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		modName.setPosition(6, 7);
		modName.setWordWrap(true);
		modName.setStyle(style -> style.withBold(true));
		modName.autoWidth();
		List<TooltipComponent> nameTooltip = new ArrayList<>();
		nameTooltip.add(StringTooltipComponent.white(modInfo.displayName()));
		if(modInfo.authors().isPresent()) {
			nameTooltip.add(StringTooltipComponent.white(I18n.get("whodoesthat.gui.modwidget.authors", modInfo.authors().get())));
		}
		nameTooltip.add(StringTooltipComponent.white(I18n.get("whodoesthat.gui.modwidget.modid", modInfo.modId())));

		if(jarInfo.parentJarPath().isPresent()) {
			nameTooltip.add(StringTooltipComponent.white(I18n.get("whodoesthat.gui.modwidget.file", jarInfo.parentJarPath().get().getFileName())));
			nameTooltip.add(StringTooltipComponent.white(I18n.get("whodoesthat.gui.modwidget.sha1", jarInfo.parentJar().getSHA1())));
			nameTooltip.add(StringTooltipComponent.white(I18n.get("whodoesthat.gui.modwidget.jarinjar", jarInfo.jar().getFileName())));
		} else {
			nameTooltip.add(StringTooltipComponent.white(I18n.get("whodoesthat.gui.modwidget.file", jarInfo.jar().getFileName())));
			nameTooltip.add(StringTooltipComponent.white(I18n.get("whodoesthat.gui.modwidget.sha1", jarInfo.getSHA1())));
		}

		modName.setTooltipElements(nameTooltip);
		this.add(modName);

		if(modInfo instanceof IFabricModInfo) {
			modLoaderImage = new WidgetImage(Icons.javaFabricMod);
			modLoaderImage.setSize(16, 16);
			modLoaderImage.setTooltipLines(Component.translatable("whodoesthat.gui.modloader.fabric"));
			this.add(modLoaderImage);
		} else if(modInfo instanceof INeoForgeModInfo) {
			modLoaderImage = new WidgetImage(Icons.javaForgeMod);
			modLoaderImage.setSize(16, 16);
			modLoaderImage.setTooltipLines(Component.translatable("whodoesthat.gui.modloader.forge"));
			this.add(modLoaderImage);
		}

		var dependencies = modInfo.dependencies().stream()
			.filter(IDependencyInfo::mandatory)
			.sorted(Comparator.comparing(IDependencyInfo::modId, Comparator.naturalOrder())).toList();

		var suggestedDependencies = modInfo.dependencies().stream()
			.filter(depInfo -> !depInfo.mandatory())
			.sorted(Comparator.comparing(IDependencyInfo::modId, Comparator.naturalOrder())).toList();

		var reverseDependencies = WhoDoesThat.reverseDependencies.getOrDefault(modInfo.modId(), new HashSet<>())
			.stream()
			.sorted(Comparator.comparing(String::toString, Comparator.naturalOrder())).toList();


		if(!dependencies.isEmpty() || !suggestedDependencies.isEmpty() || !reverseDependencies.isEmpty()) {
			boolean isDependent = dependencies.size() + suggestedDependencies.size() > 0;
			boolean isProvider = !reverseDependencies.isEmpty();

			ResourceLocation depImage;
			if(isDependent && isProvider) {
				depImage = Icons.dependenciesBoth;
			} else if(isDependent) {
				depImage = Icons.dependenciesDependsOn;
			} else {
				depImage = Icons.dependenciesProvidesFor;
			}

			dependenciesImage = new WidgetImage(depImage);
			dependenciesImage.setSize(16, 16);

			if(!dependencies.isEmpty()) {
				dependenciesImage.addTooltipElement(WrappedStringTooltipComponent.yellow(I18n.get("whodoesthat.gui.dependencies")));
				for(IDependencyInfo dependency : dependencies) {
					String version = dependency.versionRange().orElse("*");
					dependenciesImage.addTooltipLine(Component.literal(dependency.modId() + ": §7" + version + "§r"));
				}

				if(!suggestedDependencies.isEmpty() || !reverseDependencies.isEmpty()) {
					dependenciesImage.addTooltipLine(Component.literal(" "));
				}
			}

			if(!suggestedDependencies.isEmpty()) {
				dependenciesImage.addTooltipElement(WrappedStringTooltipComponent.yellow(I18n.get("whodoesthat.gui.dependencies_suggested")));
				for(IDependencyInfo dependency : suggestedDependencies) {
					String version = dependency.versionRange().orElse("*");
					dependenciesImage.addTooltipLine(Component.literal(dependency.modId() + ": §7" + version + "§r"));
				}

				if(!reverseDependencies.isEmpty()) {
					dependenciesImage.addTooltipLine(Component.literal(" "));
				}
			}

			if(!reverseDependencies.isEmpty()) {
				dependenciesImage.addTooltipElement(WrappedStringTooltipComponent.yellow(I18n.get("whodoesthat.gui.lib_for")));
				for(String dependency : reverseDependencies) {
					dependenciesImage.addTooltipLine(Component.literal(dependency));
				}
			}


			this.add(dependenciesImage);
		}

		if(jarInfo.parentJarPath().isPresent() || !jarInfo.getNestedJars().isEmpty()) {
			boolean isEmbedded = jarInfo.parentJarPath().isPresent();
			boolean isEmbedding = !jarInfo.getNestedJars().isEmpty();
			ResourceLocation jarImage;
			if(isEmbedding && isEmbedded) {
				jarImage = Icons.nestedBoth;
			} else if(isEmbedding) {
				jarImage = Icons.nestedEmbeds;
			} else {
				jarImage = Icons.nestedEmbedded;
			}

			jarInJarImage = new WidgetImage(jarImage);
			jarInJarImage.setSize(21, 11);
			jarInJarImage.setTextureSize(21, 11);
			if(isEmbedded) {
				jarInJarImage.addTooltipElement(WrappedStringTooltipComponent.yellow(I18n.get("whodoesthat.gui.jarinjar.tooltip")));
				jarInJarImage.addTooltipElement(WrappedStringTooltipComponent.white(Path.of(jarInfo.actualJar().jar().toString()).getFileName().toString()));
			}
			if(isEmbedding && isEmbedded) {
				jarInJarImage.addTooltipLine(Component.empty());
			}
			if(isEmbedding) {
				jarInJarImage.addTooltipElement(WrappedStringTooltipComponent.yellow(I18n.get("whodoesthat.gui.embeddedjar.tooltip")));
				for(var nestedJar : jarInfo.getNestedJars()) {
					jarInJarImage.addTooltipElement(WrappedStringTooltipComponent.white(nestedJar.jar().getFileName().toString()));
				}
			}
			this.add(jarInJarImage);
		}

		boolean openSource = false;
		String licenseInput;
		if(jarInfo instanceof INeoForgeJarInfo neoJar) {
			licenseInput = neoJar.license();
			openSource = neoJar.isOpenSource();
		} else if(modInfo instanceof IFabricModInfo fabricModInfo) {
			licenseInput = fabricModInfo.license();
			openSource = fabricModInfo.isOpenSource();
		} else {
			licenseInput = "All Rights Reserved";
		}

		String versionString = modInfo.version();
		if(versionString.equals("${file.jarVersion}")) {
			var attribute = Attributes.Name.IMPLEMENTATION_VERSION;
			var mainAttributes = jarInfo.getManifest().getMainAttributes();
			if(mainAttributes.containsKey(attribute)) {
				versionString = mainAttributes.getValue(attribute);
			} else {
				versionString = "?";
			}
		}

		version = new WidgetTextBox(versionString);
		version.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		version.addTooltipElement(WrappedStringTooltipComponent.yellow(licenseInput));
		if(openSource) {
			version.addTooltipLine(Component.translatable("whodoesthat.gui.opensource"));
		}
		version.autoWidth();

		this.add(version);

		if(!openSource) {
			closedSourceImage = new WidgetImage(Icons.keys);
			closedSourceImage.setSize(16, 16);
			closedSourceImage.setTooltipLines(Component.translatable("whodoesthat.gui.closedsource"));
			this.add(closedSourceImage);
		}


		String cleanedDescription = modInfo.description()
			.replaceAll("\r\n", "\n")
			.replaceAll("\t", " ")
			.replaceAll("\\s{2,}", " ")
			.trim();

		description = new WidgetTextBox(cleanedDescription);
		description.setWordWrap(true);
		description.setStyle(style -> style.withItalic(true));
		description.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		description.setPosition(90, 21);
		description.setHeight(19);
		this.add(description);

		missingSprite = new WidgetSprite(GUI.tabIcons, 0, 84, 14, 14);
		missingSprite.setSize(14, 14);
		missingSprite.setPosition(6 + 34, 22);
		missingSprite.setTooltipLines(Component.literal(cleanedDescription));
		this.add(missingSprite);

		this.addListener(
			WidgetSizeChangeEvent.class, (event, widget) -> {
				version.setPosition(this.width - version.width - 18, 8);
				modLoaderImage.setPosition(this.width - 20, 3);
				description.setWidth(this.width - 112);

				if(closedSourceImage != null) {
					closedSourceImage.setPosition(this.width - version.width - 36, 3);
				}
				if(dependenciesImage != null) {
					dependenciesImage.setPosition(this.width - 20, 20);
				}
				if(jarInJarImage != null) {
					jarInJarImage.setPosition(modName.width + 8, 4);
				}

				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		String logoFile = null;
		if(modInfo.logoFile().isPresent()) {
			logoFile = modInfo.logoFile().get();
		} else if(modInfo instanceof INeoForgeModInfo neoForgeModInfo) {
			if(neoForgeModInfo.customProperties().isPresent()) {
				var properties = neoForgeModInfo.customProperties().get();
				if(properties.containsKey("catalogueImageIcon")) {
					String iconPath = (String) properties.get("catalogueImageIcon");
					logoFile = iconPath;
				}
			}
		}

		if(logoFile == null) {
			var byFile =
				jarInfo.files().stream()
					.filter(path ->
						path.toString().equals("/logo.png") ||
							path.toString().equals("/pack.png") ||
							path.toString().equals("/assets/" + modInfo.modId() + "/logo.png")
					)
					.findFirst();
			if(byFile.isPresent()) {
				logoFile = byFile.get().toString();
			}
		}

		if(logoFile != null) {
			String finalLogoFile = logoFile;
			this.addListener(
				VisibilityChangedEvent.class, (event, widget) -> {
					boolean isVisible = this.isVisible();
					if(imageLoaded) {
						if(!isVisible) {
							this.remove(modImage);
							this.add(missingSprite);
							TextureManager tm = Minecraft.getInstance().getTextureManager();
							tm.release(loadedImage);
							loadedImage = null;
							imageLoaded = false;
						}
						return WidgetEventResult.CONTINUE_PROCESSING;
					}

					if(!isVisible) {
						return WidgetEventResult.CONTINUE_PROCESSING;
					}

					imageLoaded = true;
					Path modLogoPath = Path.of(finalLogoFile);
					Optional<byte[]> imageContent = jarInfo.getFileContent(modLogoPath);
					if(imageContent.isPresent() && imageContent.get().length > 0) {
						Optional<DynamicImageResources.ModLogo> loadedImage = DynamicImageResources.getImage(modLogoPath.toString(), imageContent.get());
						if(loadedImage.isPresent()) {
							this.loadedImage = loadedImage.get().resource();
							this.remove(missingSprite);

							modImage = new WidgetImage(loadedImage.get());
							modImage.setPosition(6, 19);
							modImage.setSize(84, 21);
							modImage.setTooltipLines(Component.literal(cleanedDescription));
							this.add(modImage);
						}
					}

					return WidgetEventResult.CONTINUE_PROCESSING;
				}
			);

		}

	}

	public ModWidget setMinimalMode(boolean minimalMode) {
		description.setVisible(!minimalMode);
		version.setVisible(!minimalMode);

		if(modLoaderImage != null) {
			modLoaderImage.setVisible(!minimalMode);
		}
		if(dependenciesImage != null) {
			dependenciesImage.setVisible(!minimalMode);
		}
		if(jarInJarImage != null) {
			jarInJarImage.setVisible(!minimalMode);
		}
		if(closedSourceImage != null) {
			closedSourceImage.setVisible(!minimalMode);
		}
		modName.autoWidth();
		if(isSelected()) {
			modName.setTextColor(ChatFormatting.WHITE.getColor());
		} else {
			modName.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		}
		if(minimalMode) {
			setWidth(100);
			if(modName.width > 100) {
				modName.setWidth(100);
			}
		} else {
			setWidth(fullWidth);
		}
		return this;
	}

	public IModInfo modInfo() {
		return modInfo;
	}

	@Override
	public IModdedJarInfo<?> getValue() {
		return jarInfo;
	}

	@Override
	public void setValue(IModdedJarInfo<?> value) {
		this.jarInfo = value;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, Screen screen) {
		//GUIHelper.drawWindow(guiGraphics, this.width, this.height, false);
		if(isSelected()) {
			GUIHelper.drawEmbossedWindow(guiGraphics, BACKGROUND_SELECTED, this.width, this.height);
		} else {
			GUIHelper.drawEmbossedWindow(guiGraphics, BACKGROUND, this.width, this.height);
		}

		super.draw(guiGraphics, screen);
	}
}
