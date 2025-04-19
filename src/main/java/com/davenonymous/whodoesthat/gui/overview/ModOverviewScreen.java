package com.davenonymous.whodoesthat.gui.overview;

import com.davenonymous.whodoesthat.WhoDoesThat;
import com.davenonymous.whodoesthat.gui.overview.detail.classes.ClassWidget;
import com.davenonymous.whodoesthat.gui.overview.detail.files.FileContentWidget;
import com.davenonymous.whodoesthat.gui.overview.detail.files.FileSelectedEvent;
import com.davenonymous.whodoesthat.gui.overview.detail.files.FileWidget;
import com.davenonymous.whodoesthat.gui.overview.parts.DetailWidget;
import com.davenonymous.whodoesthat.gui.overview.parts.FilterListWidget;
import com.davenonymous.whodoesthat.gui.overview.parts.ModListWidget;
import com.davenonymous.whodoesthat.gui.widgets.CloseButtonWidget;
import com.davenonymous.whodoesthat.lib.gui.GUI;
import com.davenonymous.whodoesthat.lib.gui.WidgetScreen;
import com.davenonymous.whodoesthat.lib.gui.event.KeyReleasedEvent;
import com.davenonymous.whodoesthat.lib.gui.event.UpdateScreenEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetTreeNode;
import com.davenonymous.whodoesthatlib.api.descriptors.ISummaryDescription;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;

public class ModOverviewScreen extends WidgetScreen {
	Window window;
	int lastWindowWidth;
	int lastWindowHeight;
	public Set<ISummaryDescription> mustHave = new HashSet<>();
	public Set<ISummaryDescription> mustNotHave = new HashSet<>();
	public Set<String> mustHaveTags = new HashSet<>();
	public Set<String> mustNotHaveTags = new HashSet<>();

	public DetailWidget detailWidget;
	FileContentWidget fileContentWidget;
	ModListWidget modList;
	FilterListWidget filterList;
	CloseButtonWidget closeButton;
	public static Set<String> allowedTextTypes = Set.of(
		".json", ".vsh", ".fsh", ".mcmeta", ".mcfunction", ".txt", ".yaml", ".md",
		".MF", "README", ".js", ".cfg", ".toml", ".yml", ".xml", ".html", ".css"
	);
	public static Set<String> allowedImageTypes = Set.of(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp");

	private void showFileContentView(boolean show) {
		this.fileContentWidget.setVisible(show);
		this.modList.setVisible(!show);
		this.filterList.setVisible(!show);
	}

	public ModOverviewScreen() {
		super(Component.translatable("whodoesthat.gui.overview.title"));
		this.window = Minecraft.getInstance().getWindow();

		this.width = window.getGuiScaledWidth();
		this.height = window.getGuiScaledHeight();
		this.lastWindowWidth = this.width;
		this.lastWindowHeight = this.height;
	}

	public void updateModList() {
		//detailWidget.setVisible(false);

		modList.setVisible(true);
		modList.fillList();
	}

	public void updateFilterList() {
		filterList.fillList();
	}

	private void updateWidgetSizes() {
		int yPadding = 6;
		int listWidth = this.width * 2 / 3;
		int filterWidth = this.width - listWidth - 10;

		closeButton.setPosition(this.width - 14, 4);
		fileContentWidget.setDimensions(0, 0, this.width - listWidth - 15 + 124, this.height - 15);

		detailWidget.setDimensions(this.width - listWidth - 5 + 121, yPadding, listWidth - 125, this.height - 27);

		if(detailWidget.isVisible()) {
			modList.setDimensions(this.width - listWidth - 5, yPadding, 118, this.height - 27);
		} else {
			modList.setDimensions(
				this.width - listWidth - 5, yPadding,
				listWidth, this.height - 27
			);
		}

		filterList.setDimensions(
			4, yPadding,
			filterWidth, this.height - 27
		);
	}

	private void closeLayer() {
		if(fileContentWidget.isVisible()) {
			fileContentWidget.setVisible(false);
			modList.setVisible(true);
			filterList.setVisible(true);
		} else if(detailWidget.isVisible()) {
			modList.deselect();
			detailWidget.setVisible(false);
		} else {
			modList.unloadAll();
			Minecraft.getInstance().popGuiLayer();
		}
	}

	@Override
	protected GUI createGUI() {
		GUI gui = new GUI(0, 0, this.width, this.height);
		if(WhoDoesThat.LAST_ANALYSIS == null) {
			// TODO: Show error message
			return gui;
		}

		gui.addListener(
			UpdateScreenEvent.class, (event, widget) -> {
				if(this.lastWindowWidth != this.window.getGuiScaledWidth() || this.lastWindowHeight != this.window.getGuiScaledHeight()) {
					this.lastWindowWidth = this.window.getGuiScaledWidth();
					this.lastWindowHeight = this.window.getGuiScaledHeight();
					this.width = this.lastWindowWidth;
					this.height = this.lastWindowHeight;
					gui.setSize(this.width, this.height);
					updateWidgetSizes();
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		gui.addListener(
			KeyReleasedEvent.class, (event, widget) -> {
				if(event.keyCode == 256) {
					closeLayer();
					return WidgetEventResult.HANDLED;
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		this.fileContentWidget = new FileContentWidget(WhoDoesThat.LAST_ANALYSIS);
		gui.add(this.fileContentWidget);

		this.filterList = new FilterListWidget(this);
		gui.add(this.filterList);

		this.detailWidget = new DetailWidget(WhoDoesThat.LAST_ANALYSIS);
		this.detailWidget.setVisible(false);
		gui.add(this.detailWidget);

		this.modList = new ModListWidget(this);
		gui.add(this.modList);


		int listWidth = this.width * 2 / 3;
		detailWidget.addListener(
			FileSelectedEvent.class, (event, widget) -> {
				if(event.selectedEntry() == -1) {
					showFileContentView(false);
				} else {
					WidgetTreeNode.TreeNodeEntryPanel selected = (WidgetTreeNode.TreeNodeEntryPanel) event.selectedWidget();
					WidgetTreeNode node = selected.node();

					if(node.widget() instanceof FileWidget entryWidget) {
						if(!entryWidget.isFolder()) {
							var lastDot = Math.max(0, entryWidget.path().lastIndexOf("."));
							String extension = entryWidget.path().substring(lastDot);

							if(allowedTextTypes.contains(extension)) {
								showFileContentView(true);

								this.fileContentWidget.setDimensions(0, 0, this.width - (this.width * 2 / 3) - 15 + 124, this.height - 15);
								this.fileContentWidget.setFile(entryWidget.path(), entryWidget.getContent());
								return WidgetEventResult.HANDLED;
							}

							if(allowedImageTypes.contains(extension)) {
								showFileContentView(true);

								this.fileContentWidget.setDimensions(0, 0, this.width - (this.width * 2 / 3) - 15 + 124, this.height - 15);
								this.fileContentWidget.setFile(entryWidget.path(), entryWidget.getContentBytes());
								return WidgetEventResult.HANDLED;
							}
						}
					} else if(node.widget() instanceof ClassWidget classWidget) {
						if(!classWidget.isPackage()) {
							showFileContentView(true);
							this.fileContentWidget.setDimensions(0, 0, this.width - (this.width * 2 / 3) - 15 + 124, this.height - 15);
							this.fileContentWidget.setFile(classWidget.path(), classWidget.getListing());
							return WidgetEventResult.HANDLED;
						}
					}

					showFileContentView(false);
				}
				return WidgetEventResult.HANDLED;
			}
		);

		closeButton = new CloseButtonWidget((event, widget) -> {
			closeLayer();
			return WidgetEventResult.HANDLED;
		});
		gui.add(closeButton);

		updateWidgetSizes();

		this.filterList.fillList();
		this.modList.fillList();

		return gui;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}
