package com.davenonymous.whodoesthat.lib.gui;

import com.davenonymous.whodoesthat.gui.menu.ContentAlignment;
import com.davenonymous.whodoesthat.lib.gui.widgets.Widget;

public record CellData(Widget widget, ContentAlignment contentAlignment, boolean maximizeContent) {
	public CellData(Widget widget) {
		this(widget, ContentAlignment.MIDDLE_CENTER, false);
	}

	public CellData(Widget widget, ContentAlignment contentAlignment) {
		this(widget, contentAlignment, false);
	}

	public CellData(Widget widget, boolean maximizeContent) {
		this(widget, ContentAlignment.MIDDLE_CENTER, maximizeContent);
	}

	public CellData(Widget widget, ContentAlignment contentAlignment, boolean maximizeContent) {
		this.widget = widget;
		this.contentAlignment = contentAlignment;
		this.maximizeContent = maximizeContent;
	}

	public CellData withContentAlignment(ContentAlignment contentAlignment) {
		return new CellData(this.widget, contentAlignment, this.maximizeContent);
	}

	public CellData withMaximizeContent(boolean maximizeContent) {
		return new CellData(this.widget, this.contentAlignment, maximizeContent);
	}
}
