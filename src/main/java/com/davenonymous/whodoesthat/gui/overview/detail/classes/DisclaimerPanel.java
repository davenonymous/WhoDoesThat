package com.davenonymous.whodoesthat.gui.overview.detail.classes;

import com.davenonymous.whodoesthat.lib.gui.GUIHelper;
import com.davenonymous.whodoesthat.lib.gui.Icons;
import com.davenonymous.whodoesthat.lib.gui.event.MouseClickEvent;
import com.davenonymous.whodoesthat.lib.gui.event.ValueChangedEvent;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetEventResult;
import com.davenonymous.whodoesthat.lib.gui.event.WidgetSizeChangeEvent;
import com.davenonymous.whodoesthat.lib.gui.widgets.*;
import com.davenonymous.whodoesthatlib.api.result.IJarInfo;
import com.davenonymous.whodoesthatlib.api.result.IModInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;

public class DisclaimerPanel extends WidgetPanel {
	WidgetImage stopIcon;
	WidgetTextBox stopLabel;
	WidgetTextBox stopDisclaimerA;
	WidgetTextBox stopDisclaimerB;
	WidgetCheckboxWithLabel readDisclaimer;
	WidgetCheckboxWithLabel awareOfConsequences;
	WidgetCheckboxWithLabel gotPermission;
	WidgetCheckboxWithLabel noReverseEngineering;
	WidgetButton continueButton;
	int buttonBits = 0;

	public DisclaimerPanel(IJarInfo jarInfo, IModInfo modInfo) {
		super();

		this.stopIcon = new WidgetImage(Icons.keys);
		this.stopIcon.setSize(32, 32);
		this.add(this.stopIcon);

		this.stopLabel = new WidgetTextBox(I18n.get("whodoesthat.gui.closedsource"));
		this.stopLabel.setPosition(10, 10);
		this.stopLabel.setWordWrap(true);
		this.stopLabel.setStyle(style -> style.withColor(ChatFormatting.DARK_RED).withBold(true));
		this.stopLabel.setHeight(20);
		this.stopLabel.scale = 2.0f;
		this.add(this.stopLabel);

		this.continueButton = new WidgetButton(I18n.get("whodoesthat.gui.closedsource.disclaimer_confirm"));
		this.continueButton.setEnabled(false);
		this.continueButton.addListener(
			MouseClickEvent.class, (event, widget) -> {
				if(this.buttonBits == 7) {
					this.fireEvent(new DisclaimerConfirmedEvent());
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
		this.add(this.continueButton);

		this.stopDisclaimerA = new WidgetTextBox(I18n.get("whodoesthat.gui.closedsource.disclaimer_1", modInfo.displayName()));
		this.stopDisclaimerA.setWordWrap(true);
		this.stopDisclaimerA.setStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));
		this.stopDisclaimerA.setX(6);
		this.add(this.stopDisclaimerA);

		this.stopDisclaimerB = new WidgetTextBox(I18n.get("whodoesthat.gui.closedsource.disclaimer_2"));
		this.stopDisclaimerB.setWordWrap(true);
		this.stopDisclaimerB.setStyle(style -> style.withColor(ChatFormatting.DARK_RED));
		this.stopDisclaimerB.setX(6);
		this.add(this.stopDisclaimerB);

		this.readDisclaimer = new WidgetCheckboxWithLabel(I18n.get("whodoesthat.gui.closedsource.disclaimer_checkbox_1"));
		this.readDisclaimer.addListener(
			ValueChangedEvent.class, (event, widget) -> {
				if(readDisclaimer.getValue()) {
					this.buttonBits |= 1;
				} else {
					this.buttonBits &= ~1;
				}
				this.continueButton.setEnabled(this.buttonBits == 7);
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
		this.add(readDisclaimer);

		this.gotPermission = new WidgetCheckboxWithLabel(I18n.get("whodoesthat.gui.closedsource.disclaimer_checkbox_3"));
		this.gotPermission.addListener(
			ValueChangedEvent.class, (event, widget) -> {
				if(gotPermission.getValue()) {
					this.buttonBits |= 2;
				} else {
					this.buttonBits &= ~2;
				}
				this.continueButton.setEnabled(this.buttonBits == 7);
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
		this.add(gotPermission);

		this.noReverseEngineering = new WidgetCheckboxWithLabel(I18n.get("whodoesthat.gui.closedsource.disclaimer_checkbox_4"));
		this.noReverseEngineering.addListener(
			ValueChangedEvent.class, (event, widget) -> {
				if(noReverseEngineering.getValue()) {
					this.buttonBits |= 4;
				} else {
					this.buttonBits &= ~4;
				}
				this.continueButton.setEnabled(this.buttonBits == 7);
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
		this.add(noReverseEngineering);

		this.addListener(
			WidgetSizeChangeEvent.class, (event, widget) -> {
				this.stopIcon.setPosition(this.width - 40, 4);
				this.stopLabel.setWidth(this.width - 20);
				this.stopDisclaimerA.setWidth(this.width - 20);
				this.stopDisclaimerB.setWidth(this.width - 20);

				this.readDisclaimer.setWidth(this.width - 20);
				this.gotPermission.setWidth(this.width - 20);
				this.noReverseEngineering.setWidth(this.width - 20);

				int aHeight = Math.max(12, GUIHelper.wordWrapHeight(Minecraft.getInstance().font, FormattedText.of(stopDisclaimerA.getText()), this.width - 20));
				int bHeight = Math.max(12, GUIHelper.wordWrapHeight(Minecraft.getInstance().font, FormattedText.of(stopDisclaimerB.getText()), this.width - 20));

				this.stopDisclaimerA.setY(12 + this.stopLabel.height);
				this.stopDisclaimerA.setHeight(aHeight);

				this.stopDisclaimerB.setY(12 + this.stopLabel.height + aHeight + 12);
				this.stopDisclaimerB.setHeight(bHeight);

				this.readDisclaimer.setPosition(6, this.stopDisclaimerB.y + bHeight + 12);
				this.gotPermission.setPosition(6, this.readDisclaimer.y + this.readDisclaimer.height + 2);
				this.noReverseEngineering.setPosition(6, this.gotPermission.y + this.gotPermission.height + 2);

				this.continueButton.autoWidth();
				int centredX = this.width / 2 - this.continueButton.width / 2;
				this.continueButton.setPosition(centredX, this.noReverseEngineering.y + this.noReverseEngineering.height + 12);

				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
	}
}
