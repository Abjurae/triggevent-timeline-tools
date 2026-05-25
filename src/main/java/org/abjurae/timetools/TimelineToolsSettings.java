package org.abjurae.timetools;

import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivsupport.persistence.PersistenceProvider;
import gg.xp.xivsupport.persistence.settings.BooleanSetting;
import gg.xp.xivsupport.persistence.settings.DoubleSetting;
import gg.xp.xivsupport.persistence.settings.IntSetting;

@ScanMe
public final class TimelineToolsSettings {
	private final IntSetting maxDisplayedBars;
	private final IntSetting barWidth;
	//private final IntSetting barHeight;
	private final BooleanSetting emphasizedEnabled;
	private final BooleanSetting emphasizedReverse;
	private final BooleanSetting autoAddBossAbilities;
	private final BooleanSetting showBossButton;
	private final BooleanSetting showPhaseButton;
	private final BooleanSetting showPlayerButton;
	private final DoubleSetting calloutAdvanceSeconds;

	public TimelineToolsSettings(PersistenceProvider persistence) {
		emphasizedEnabled = new BooleanSetting(persistence, "timeline-tools.emphasizedEnabled", true);
		maxDisplayedBars = new IntSetting(persistence, "timeline-tools.max-displayed", 1, 1, 5);
		barWidth = new IntSetting(persistence, "timeline-tools.bar-width", 150, 50, 1000);
		//barHeight = new IntSetting(persistence, "timeline-tools.bar-height", 20, 10, 100);
		emphasizedReverse = new BooleanSetting(persistence, "timeline-tools.emphasizedReverse", false);
		autoAddBossAbilities = new BooleanSetting(persistence, "timeline-tools.autoAddBossAbilities", false);
		showBossButton = new BooleanSetting(persistence, "timeline-tools.showBossButton", true);
		showPhaseButton = new BooleanSetting(persistence, "timeline-tools.showPhaseButton", true);
		showPlayerButton = new BooleanSetting(persistence, "timeline-tools.showPlayerButton", true);
		calloutAdvanceSeconds = new DoubleSetting(persistence,"timeline-tools.calloutAdvanceSeconds", 3.0, 0, 15);
	}

	public BooleanSetting getEmphasizedEnabled() {
		return emphasizedEnabled;
	}

	public IntSetting getMaxDisplayedBars() {
		return maxDisplayedBars;
	}

	public IntSetting getBarWidth() {
		return barWidth;
	}

	public BooleanSetting getAutoAddBossAbilities() {
		return autoAddBossAbilities;
	}

	public BooleanSetting getShowBossButton() {
		return showBossButton;
	}

	public BooleanSetting getShowPhaseButton() {
		return showPhaseButton;
	}

	public BooleanSetting getShowPlayerButton() {
		return showPlayerButton;
	}

	public BooleanSetting getEmphasizedReverse() {
		return emphasizedReverse;
	}

	public DoubleSetting getCalloutAdvanceSeconds() {
		return calloutAdvanceSeconds;
	}

	/*public IntSetting getBarHeight() {
		return barHeight;
	}*/
}
