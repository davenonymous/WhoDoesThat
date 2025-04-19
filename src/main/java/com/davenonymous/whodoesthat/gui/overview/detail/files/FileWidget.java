package com.davenonymous.whodoesthat.gui.overview.detail.files;

import com.davenonymous.whodoesthat.gui.overview.ModOverviewScreen;
import com.davenonymous.whodoesthat.lib.gui.ISelectable;
import com.davenonymous.whodoesthat.lib.gui.Icons;
import com.davenonymous.whodoesthat.lib.gui.event.ListEntrySelectionEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetImage;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetPanel;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTextBox;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Optional;

public class FileWidget extends WidgetPanel implements ISelectable {
	private boolean selected = false;
	private boolean isFolder = false;
	private IJarInfo jar;
	private String path;
	private WidgetTextBox label;
	private WidgetImage icon;

	public FileWidget(IJarInfo jar, String path, boolean isFolder) {
		super();
		this.jar = jar;
		this.path = path;
		this.isFolder = isFolder;

		this.label = new WidgetTextBox(path.substring(path.lastIndexOf("/") + 1))
			.setStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));
		this.label.autoWidth();
		this.label.setX(10);

		int textureWidth = 16;
		int textureHeight = 16;
		ResourceLocation iconId = isFolder ? Icons.folderClosed : Icons.fileBlank;
		if(path.lastIndexOf(".") != -1) {
			String extension = path.substring(path.lastIndexOf("."));
			if(ModOverviewScreen.allowedTextTypes.contains(extension)) {
				iconId = Icons.notepad;
			} else if(extension.equals(".png")) {
				iconId = Icons.paint;
			} else if(extension.equals(".ogg")) {
				iconId = Icons.music;
			} else if(extension.equals(".jar")) {
				iconId = Icons.folderZipper;
			}
		}
		this.icon = new WidgetImage(iconId);
		this.icon.setY(0);
		this.icon.setSize(8, 8);
		this.icon.setTextureSize(textureWidth, textureHeight);
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

	public FileWidget setText(String text) {
		label.setText(text);
		label.autoWidth();
		return this;
	}

	public String getContent() {
		Optional<byte[]> content = jar.getFileContent(Path.of(path));
		return content.map(String::new).orElse("");
	}

	public byte[] getContentBytes() {
		Optional<byte[]> content = jar.getFileContent(Path.of(path));
		return content.orElse(new byte[0]);
	}

	public boolean isFolder() {
		return isFolder;
	}

	public IJarInfo jar() {
		return jar;
	}

	public String path() {
		return path;
	}

	public String getLabel() {
		return label.getText();
	}

	public WidgetTextBox getLabelWidget() {
		return label;
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
