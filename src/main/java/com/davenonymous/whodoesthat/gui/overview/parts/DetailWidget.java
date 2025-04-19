package com.davenonymous.whodoesthat.gui.overview.parts;


import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.config.GuiConfig;
import com.davenonymous.whodoesthat.gui.overview.detail.classes.ClassTree;
import com.davenonymous.whodoesthat.gui.overview.detail.files.FileSelectedEvent;
import com.davenonymous.whodoesthat.gui.overview.detail.files.FileTree;
import com.davenonymous.whodoesthat.gui.overview.detail.simple.EventList;
import com.davenonymous.whodoesthat.gui.overview.detail.simple.InheritanceList;
import com.davenonymous.whodoesthat.gui.overview.detail.simple.MixinList;
import com.davenonymous.whodoesthat.gui.overview.detail.simple.SummaryList;
import com.davenonymous.whodoesthat.lib.gui.Icons;
import com.davenonymous.whodoesthat.lib.gui.event.ListSelectionEvent;
import com.davenonymous.whodoesthat.lib.gui.event.TabChangedEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetSizeChangeEvent;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetImage;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetPanel;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTabsPanel;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTextBox;
import com.davenonymous.whodoesthatlib.api.descriptors.ISummaryDescription;
import com.davenonymous.whodoesthatlib.api.result.*;
import com.davenonymous.whodoesthatlib.api.result.fabric.IFabricModInfo;
import com.davenonymous.whodoesthatlib.api.result.mod.EventResult;
import com.davenonymous.whodoesthatlib.api.result.mod.InheritanceResult;
import com.davenonymous.whodoesthatlib.api.result.mod.MixinResult;
import com.davenonymous.whodoesthatlib.api.result.neoforge.INeoForgeJarInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class DetailWidget extends WidgetPanel {
	public IModInfo modInfo = null;
	public IJarInfo jarInfo = null;
	public WidgetTextBox modName;
	public WidgetTabsPanel tabs;

	private IScanResult scanResult;

	private SummaryList summaryList;
	private EventList eventList;
	private MixinList mixinList;
	private InheritanceList inheritanceList;
	private FileTree fileTree;
	private WidgetPanel selectedTab = null;
	private ClassTree classTree;

	public DetailWidget(IScanResult scanResult) {
		super();
		this.setVisible(false);
		this.scanResult = scanResult;

		tabs = new WidgetTabsPanel();
		tabs.setEdge(WidgetTabsPanel.TabDockEdge.NORTH);
		tabs.setDimensions(5, 9, this.width, this.height);

		var self = this;
		tabs.addListener(
			TabChangedEvent.class, (event, widget) -> {
				self.fireEvent(new FileSelectedEvent(-1, null));
				self.selectedTab = event.newValue;
				return WidgetEventResult.HANDLED;
			}
		);

		this.add(tabs);

		modName = new WidgetTextBox("");
		modName.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		modName.setPosition(6, 0);
		modName.autoWidth();

		this.add(modName);

		this.addListener(
			WidgetSizeChangeEvent.class, (event, widget) -> {
				tabs.setDimensions(5, 9, event.newWidth(), event.newHeight());
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
	}

	private DetailWidget addSummaryTab(IJarInfo jarInfo) {
		boolean copySelected = this.selectedTab != null && this.selectedTab == this.summaryList;
		this.summaryList = null;
		List<ISummaryDescription> sortedDescriptions = jarInfo.getSummaries().keySet()
			.stream().sorted(Comparator.comparing(desc -> desc.description().toLowerCase(Locale.ROOT))).toList();

		Set<IDependencyInfo> dependencies = new HashSet<>();
		Set<String> reverseDependencies = new HashSet<>();
		if(jarInfo instanceof IModdedJarInfo<?> neoJar) {
			for(IModInfo mod : neoJar.mods()) {
				dependencies.addAll(mod.dependencies());

				if(WhoDoesThat.reverseDependencies.containsKey(mod.modId())) {
					reverseDependencies.addAll(WhoDoesThat.reverseDependencies.get(mod.modId()));
				}
			}
		}

		this.summaryList = new SummaryList(jarInfo);
		summaryList.setWidth(this.width - 8);
		summaryList.setHeight(this.height - 40);

		if(copySelected) {
			this.selectedTab = summaryList;
		}

		boolean hasContent = false;
		if(!dependencies.isEmpty() || !reverseDependencies.isEmpty()) {
			summaryList.createEntries(dependencies, reverseDependencies);
			hasContent = true;
		}

		if(!sortedDescriptions.isEmpty()) {
			summaryList.createEntries(sortedDescriptions);
			hasContent = true;
		}

		if(!hasContent && copySelected) {
			this.selectedTab = null;
		}

		ResourceLocation infoLogo = ResourceLocation.withDefaultNamespace("textures/gui/sprites/icon/info.png");
		WidgetImage infoLogoWidget = new WidgetImage(infoLogo);
		infoLogoWidget.setSize(16, 16);
		infoLogoWidget.setTextureSize(20, 20);
		tabs.addPage(summaryList, infoLogoWidget, List.of(Component.translatable("whodoesthat.gui.summary")));

		return this;
	}

	private DetailWidget addEventsTab(IJarInfo jarInfo) {
		boolean copySelected = this.selectedTab != null && this.selectedTab == this.eventList;
		this.eventList = null;
		EventResult eventResult = jarInfo.getAnalysisResult(EventResult.class);
		if(eventResult != null && !eventResult.listeners().isEmpty()) {
			this.eventList = new EventList(jarInfo);
			eventList.setWidth(this.width - 8);
			eventList.setHeight(this.height - 40);
			eventList.createEntries();

			if(copySelected) {
				this.selectedTab = eventList;
			}

			WidgetImage infoLogoWidget = new WidgetImage(Icons.calendar);
			infoLogoWidget.setSize(16, 16);
			infoLogoWidget.setTextureSize(8, 8);
			infoLogoWidget.setScale(0.75f);
			infoLogoWidget.setOffset(3, 3);

			tabs.addPage(eventList, infoLogoWidget, List.of(Component.translatable("whodoesthat.gui.events")));
		} else if(copySelected) {
			this.selectedTab = null;
		}
		return this;
	}

	private DetailWidget addMixinsTab(IJarInfo jarInfo) {
		boolean copySelected = this.selectedTab != null && this.selectedTab == this.mixinList;
		this.mixinList = null;
		MixinResult mixinResult = jarInfo.getAnalysisResult(MixinResult.class);
		if(mixinResult != null && !mixinResult.mixins().isEmpty()) {
			this.mixinList = new MixinList(jarInfo);
			mixinList.setWidth(this.width - 8);
			mixinList.setHeight(this.height - 40);
			mixinList.createEntries();

			if(copySelected) {
				this.selectedTab = mixinList;
			}

			var swordImage = new WidgetImage(ResourceLocation.withDefaultNamespace("textures/item/iron_sword.png"));
			swordImage.setSize(16, 16);

			tabs.addPage(mixinList, swordImage, List.of(Component.translatable("whodoesthat.gui.mixins")));
		} else if(copySelected) {
			this.selectedTab = null;
		}
		return this;
	}

	private DetailWidget addInheritanceTab(IJarInfo jarInfo) {
		boolean copySelected = this.selectedTab != null && this.selectedTab == this.inheritanceList;
		this.inheritanceList = null;

		InheritanceResult inheritanceResult = jarInfo.getAnalysisResult(InheritanceResult.class);
		if(!inheritanceResult.inheritance().isEmpty()) {
			this.inheritanceList = new InheritanceList(jarInfo, scanResult);
			inheritanceList.setWidth(this.width - 8);
			inheritanceList.setHeight(this.height - 40);
			inheritanceList.createEntries();

			if(copySelected) {
				this.selectedTab = inheritanceList;
			}

			var eggImage = new WidgetImage(ResourceLocation.withDefaultNamespace("textures/item/egg.png"));
			eggImage.setSize(16, 16);

			tabs.addPage(inheritanceList, eggImage, List.of(Component.translatable("whodoesthat.gui.inheritance")));
		} else if(copySelected) {
			this.selectedTab = null;
		}

		return this;
	}

	private DetailWidget addClassTreeTab(IJarInfo jarInfo, IModInfo modInfo) {
		boolean isOpenSource = false;
		if(modInfo instanceof IFabricModInfo fabricMod) {
			isOpenSource = fabricMod.isOpenSource();
		} else if(jarInfo instanceof INeoForgeJarInfo neoJar) {
			isOpenSource = neoJar.isOpenSource();
		}

		if(!isOpenSource && !GuiConfig.showClassesForAllRightsReservedMods) {
			return this;
		}

		boolean copySelected = this.selectedTab != null && this.selectedTab == this.classTree;
		this.classTree = new ClassTree(jarInfo, modInfo);
		classTree.setWidth(this.width - 8);
		classTree.setHeight(this.height - 40);
		var self = this;
		classTree.tree().addListener(
			ListSelectionEvent.class, (event, widget) -> {
				self.fireEvent(new FileSelectedEvent(event.selectedEntry, event.selectedWidget));
				return WidgetEventResult.HANDLED;
			}
		);
		classTree.addClasses();

		if(copySelected) {
			this.selectedTab = classTree;
		}

		var documentImage = new WidgetImage(isOpenSource ? Icons.javaCup : Icons.keys);
		documentImage.setSize(16, 16);

		tabs.addPage(classTree, documentImage, List.of(Component.translatable("whodoesthat.gui.classes")));
		classTree.setWidth(this.width - 8);
		classTree.setHeight(this.height - 40);

		return this;
	}

	private DetailWidget addFileTreeTab(IJarInfo jarInfo) {
		boolean copySelected = this.selectedTab != null && this.selectedTab == this.fileTree;
		this.fileTree = new FileTree(jarInfo);
		fileTree.setWidth(this.width - 8);
		fileTree.setHeight(this.height - 40);
		var self = this;
		fileTree.addListener(
			ListSelectionEvent.class, (event, widget) -> {
				self.fireEvent(new FileSelectedEvent(event.selectedEntry, event.selectedWidget));
				return WidgetEventResult.HANDLED;
			}
		);
		fileTree.addFiles();

		if(copySelected) {
			this.selectedTab = fileTree;
		}

		var documentImage = new WidgetImage(Icons.documentIcon);
		documentImage.setSize(16, 16);

		tabs.addPage(fileTree, documentImage, List.of(Component.translatable("whodoesthat.gui.files")));
		fileTree.setWidth(this.width - 8);
		fileTree.setHeight(this.height - 40);
		return this;
	}


	public void setMod(IModInfo modInfo, IJarInfo jarInfo) {
		this.modInfo = modInfo;
		this.jarInfo = jarInfo;

		String oldTab = this.selectedTab != null ? this.selectedTab.getClass().getCanonicalName() : "";
		this.tabs.clear();
		this.modName.setText(modInfo.displayName());
		this.modName.autoWidth();

		this.addSummaryTab(jarInfo);
		this.addFileTreeTab(jarInfo);
		this.addEventsTab(jarInfo);
		this.addInheritanceTab(jarInfo);
		this.addMixinsTab(jarInfo);
		this.addClassTreeTab(jarInfo, modInfo);

		if(this.selectedTab != null) {
			tabs.setActivePage(this.selectedTab);
		} else {
			tabs.setActivePage(0);
		}

		this.setVisible(true);
	}
}
