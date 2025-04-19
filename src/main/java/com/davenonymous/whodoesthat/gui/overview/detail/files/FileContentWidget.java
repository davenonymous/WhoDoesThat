package com.davenonymous.whodoesthat.gui.overview.detail.files;

import com.davenonymous.whodoesthat.gui.widgets.SelectableTextWidget;
import com.davenonymous.whodoesthat.lib.gui.GUIHelper;
import com.davenonymous.whodoesthat.lib.gui.event.VisibilityChangedEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetSizeChangeEvent;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetList;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetPanel;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTextBox;
import com.davenonymous.whodoesthatlib.api.result.IScanResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;

public class FileContentWidget extends WidgetPanel {
	private IScanResult scanResult;
	private WidgetTextBox pathLabel;
	private WidgetList contentList;
	private ImageViewerWidget image;

	public FileContentWidget(IScanResult scanResult) {
		super();
		this.setVisible(false);
		this.scanResult = scanResult;

		pathLabel = new WidgetTextBox("");
		pathLabel.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		pathLabel.setPosition(6, 6);
		this.add(pathLabel);

		contentList = new WidgetList();
		contentList.setPosition(4, 15);
		contentList.setSize(50, 50);
		contentList.setVisible(false);
		contentList.padding = 4;
		this.add(contentList);

		image = new ImageViewerWidget();
		image.setPosition(4, 15);
		image.setSize(50, 50);
		image.setVisible(false);
		this.add(image);

		this.addListener(
			VisibilityChangedEvent.class, (event, widget) -> {
				boolean isVisible = this.isVisible();
				if(!isVisible) {
					image.unloadImage();
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		this.addListener(
			WidgetSizeChangeEvent.class, (event, widget) -> {
				pathLabel.autoWidth();
				contentList.setSize(this.width, this.height - 12);
				image.setSize(this.width, this.height - 12);
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
	}

	public void setFile(String path, byte[] bytes) {
		pathLabel.setText(path);
		pathLabel.autoWidth();

		contentList.setVisible(false);

		image.unloadImage();
		image.setImage(path, bytes);
		image.setVisible(true);
	}

	public void setFile(String path, String content) {
		pathLabel.setText(path);
		pathLabel.autoWidth();

		image.setVisible(false);

		contentList.clear();
		contentList.scrollToTop();
		contentList.setSize(this.width, this.height - 12);
		contentList.setVisible(true);

		content = content
			.replaceAll("\r", "")
			.replaceAll("\t", "  ");


		if(path.endsWith(".json") || path.endsWith(".mcmeta")) {
			content = content
				.replaceAll("([{}])", "§a$1§r")
				.replaceAll("([\\[\\]])", "§a$1§r")
				.replaceAll("\"([^\"]*)\"\\s*:", "§6\"$1\"§r:");
		}

		if(path.endsWith(".toml")) {
			content = content
				.replaceAll("(.*?)\\s*=\\s*\"(.*?)\"", "§6$1§r = §b\"$2\"§r")
				.replaceAll("(.*?)\\s*=\\s*'''(.*?)'''", "§6$1§r = §b\"$2\"§r")
				.replaceAll("\\[\\[(.*?)\\]\\]", "§7[[§f$1§7]]§r")
			;
		}

		if(path.endsWith("MANIFEST.MF")) {
			content = content
				.replaceAll("(.*?)\\s*:\\s*(.*)", "§6$1:§r §b$2§r");
		}

		String[] lines = content.split("\n");
		int yOff = 0;
		int lineNum = 0;
		int lineMagnitude = (int) Math.log10(lines.length) + 1;
		for(String line : lines) {
			String lineNumPrefix = String.format("§7%" + lineMagnitude + "d§r  ", lineNum++);
			var lineTextWidget = new SelectableTextWidget(lineNumPrefix + line);
			lineTextWidget.setTextColor(ChatFormatting.AQUA.getColor());
			lineTextWidget.setStyle(style -> style.withColor(ChatFormatting.AQUA));
			lineTextWidget.setWordWrap(true);
			int height = Math.max(12, GUIHelper.wordWrapHeight(Minecraft.getInstance().font, FormattedText.of(lineNumPrefix + line), this.width - 15));
			lineTextWidget.height = height;
			lineTextWidget.width = this.width - 15;
			lineTextWidget.setPosition(0, yOff);
			contentList.addListEntry(lineTextWidget);
			yOff += height;
		}
	}
}
