package com.davenonymous.whodoesthat.gui.overview.detail.files;

import com.davenonymous.whodoesthat.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.whodoesthat.lib.gui.widgets.Widget;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTree;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTreeNode;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;

import java.nio.file.Path;
import java.util.*;

public class FileTree extends WidgetTree {
	IJarInfo jarInfo;

	public FileTree(IJarInfo jarInfo) {
		super();
		this.jarInfo = jarInfo;
		this.setDrawBackground(false);
	}

	public Widget makeFileWidget(String path, boolean isFolder) {
		return new FileWidget(jarInfo, path, isFolder);
	}

	public void addFiles() {
		Map<String, WidgetTreeNode> nodes = new HashMap<>();
		var rootNode = new FileWidget(jarInfo, "/", true).setText("/");
		WidgetTreeNode root = addRootNode(rootNode);
		nodes.put("/", root);

		List<Path> allPaths = new ArrayList<>(jarInfo.files());
		allPaths.sort(Comparator.comparing(Path::toString, Comparator.naturalOrder()));

		for(Path path : allPaths) {
			List<String> pathParts = new ArrayList<>(List.of(path.toString().split("/")));

			WidgetTreeNode parent = root;
			for(int partIndex = 1; partIndex < pathParts.size(); partIndex++) {
				String parentPath = String.join("/", pathParts.subList(0, partIndex + 1));

				if(!nodes.containsKey(parentPath)) {
					WidgetTreeNode pathChild = parent.addChild(makeFileWidget(parentPath, partIndex != pathParts.size() - 1));
					pathChild.setExpanded(partIndex <= 2 && !parentPath.endsWith(".cache"));
					nodes.put(parentPath, pathChild);
				}

				parent = nodes.get(parentPath);

				if(partIndex == pathParts.size() - 1) {
					FileWidget fileWidget = (FileWidget) parent.widget();
					fileWidget.getLabelWidget().addTooltipElement(WrappedStringTooltipComponent.white(parentPath));
				}
			}
		}

		buildListFromRootNodes();
	}
}
