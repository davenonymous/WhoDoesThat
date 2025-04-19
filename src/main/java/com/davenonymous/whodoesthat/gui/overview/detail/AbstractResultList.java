package com.davenonymous.whodoesthat.gui.overview.detail;

import com.davenonymous.whodoesthat.lib.gui.widgets.WidgetList;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import com.davenonymous.whodoesthatlib.api.result.mod.IModResult;

public abstract class AbstractResultList<T extends IModResult> extends WidgetList {
	protected final T result;
	protected final IJarInfo jarInfo;

	public AbstractResultList(IJarInfo jarInfo, Class<? extends T> result) {
		super();
		this.jarInfo = jarInfo;
		this.result = jarInfo.getAnalysisResult(result);
		this.padding = 4;
		this.setDrawBackground(false);
		this.setShowSelection(false);
	}

	public abstract void createEntries();
}
