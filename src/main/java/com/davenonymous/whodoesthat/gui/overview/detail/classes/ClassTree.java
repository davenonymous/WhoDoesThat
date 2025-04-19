package com.davenonymous.whodoesthat.gui.overview.detail.classes;

import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetSizeChangeEvent;
import com.davenonymous.whodoesthat.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetPanel;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTree;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTreeNode;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import com.davenonymous.whodoesthatlib.api.result.IModInfo;
import com.davenonymous.whodoesthatlib.api.result.asm.IClassInfo;
import com.davenonymous.whodoesthatlib.api.result.fabric.IFabricModInfo;
import com.davenonymous.whodoesthatlib.api.result.neoforge.INeoForgeJarInfo;

import java.util.*;

public class ClassTree extends WidgetPanel {
	IJarInfo jarInfo;
	IModInfo modInfo;
	WidgetTree classTree;
	DisclaimerPanel disclaimerPanel;

	public ClassTree(IJarInfo jarInfo, IModInfo modInfo) {
		super();
		this.jarInfo = jarInfo;
		this.modInfo = modInfo;

		this.disclaimerPanel = new DisclaimerPanel(jarInfo, modInfo);
		this.disclaimerPanel.addListener(
			DisclaimerConfirmedEvent.class, (event, widget) -> {
				this.setShowDisclaimer(false);
				return WidgetEventResult.HANDLED;
			}
		);
		this.add(disclaimerPanel);

		this.classTree = new WidgetTree();
		this.classTree.setDrawBackground(false);
		this.add(this.classTree);

		boolean openSource = false;
		if(jarInfo instanceof INeoForgeJarInfo neoJar) {
			openSource = neoJar.isOpenSource();
		} else if(modInfo instanceof IFabricModInfo fabricModInfo) {
			openSource = fabricModInfo.isOpenSource();
		}

		this.setShowDisclaimer(!openSource);

		this.addListener(
			WidgetSizeChangeEvent.class, (event, widget) -> {
				this.classTree.setSize(this.width, this.height);
				this.disclaimerPanel.setSize(this.width, this.height);

				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

	}

	public void setShowDisclaimer(boolean show) {
		this.disclaimerPanel.setVisible(show);
		this.classTree.setVisible(!show);
	}

	public WidgetTree tree() {
		return this.classTree;
	}

	public void addClasses() {
		Map<String, WidgetTreeNode> nodes = new HashMap<>();
		var rootNode = new ClassWidget(jarInfo, null, ".", true).setText(".");
		WidgetTreeNode root = this.classTree.addRootNode(rootNode);
		nodes.put(".", root);

		List<IClassInfo> allClasses = new ArrayList<>(jarInfo.getClasses());
		allClasses.sort(Comparator.comparing(IClassInfo::getClassName, Comparator.naturalOrder()));

		for(IClassInfo classInfo : allClasses) {
			List<String> pathParts = new ArrayList<>(List.of(classInfo.getClassName().split("\\.")));

			WidgetTreeNode parent = root;
			for(int partIndex = 1; partIndex < pathParts.size(); partIndex++) {
				String parentPackage = String.join(".", pathParts.subList(0, partIndex + 1));

				if(!nodes.containsKey(parentPackage)) {
					WidgetTreeNode pathChild = parent.addChild(
						new ClassWidget(jarInfo, classInfo, parentPackage, partIndex != pathParts.size() - 1));
					pathChild.setExpanded(partIndex <= 2);
					nodes.put(parentPackage, pathChild);
				}

				parent = nodes.get(parentPackage);

				if(partIndex == pathParts.size() - 1) {
					ClassWidget classWidget = (ClassWidget) parent.widget();
					classWidget.getLabelWidget().addTooltipElement(WrappedStringTooltipComponent.white(parentPackage));
				}
			}

		}
		this.classTree.buildListFromRootNodes();
	}
}
