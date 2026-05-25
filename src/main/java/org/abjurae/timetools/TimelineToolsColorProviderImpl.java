package org.abjurae.timetools;

import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivsupport.persistence.PersistenceProvider;
import gg.xp.xivsupport.persistence.settings.ColorSetting;
import gg.xp.xivsupport.timelines.gui.TimelineBarColorProvider;

import java.awt.Color;

@ScanMe
public class TimelineToolsColorProviderImpl implements TimelineToolsColorProvider {
	private static final Color defaultColorEmphasizedUpcomingBar = new Color(32, 68, 101, 192);
	private static final Color defaultColorEmphasizedActiveBar = new Color(72, 24, 133, 192);
	private static final Color defaultColorEmphasizedExpiredBar = new Color(97, 20, 50, 192);
	private static final Color defaultColorEmphasizedBarFont = new Color(250, 250, 250);
	private final ColorSetting emphasizedBarUpcoming;
	private final ColorSetting emphasizedBarActive;
	private final ColorSetting emphasizedBarExpired;
	private final ColorSetting emphasizedBarFont;

	public TimelineToolsColorProviderImpl(PersistenceProvider pers) {
		this.emphasizedBarUpcoming = new ColorSetting(pers, "timeline-tools.colors.emphasizedUpcoming", defaultColorEmphasizedUpcomingBar);
		this.emphasizedBarActive = new ColorSetting(pers, "timeline-tools.colors.emphasizedActive", defaultColorEmphasizedActiveBar);
		this.emphasizedBarExpired = new ColorSetting(pers, "timeline-tools.colors.emphasizedExpired", defaultColorEmphasizedExpiredBar);
		this.emphasizedBarFont = new ColorSetting(pers, "timeline-tools.colors.emphasizedFont", defaultColorEmphasizedBarFont);
	}


	public ColorSetting getEmphasizedBarUpcomingSetting() {
		return emphasizedBarUpcoming;
	}

	public ColorSetting getEmphasizedBarActiveSetting() {
		return emphasizedBarActive;
	}

	public ColorSetting getEmphasizedBarExpiredSetting() {
		return emphasizedBarExpired;
	}

	public ColorSetting getEmphasizedBarFontSetting() {
		return emphasizedBarFont;
	}

	@Override
	public Color getFontColor() {
		return emphasizedBarFont.get();
	}

	@Override
	public Color getUpcomingColor() {
		return emphasizedBarUpcoming.get();
	}

	@Override
	public Color getActiveColor() {
		return emphasizedBarActive.get();
	}

	@Override
	public Color getExpiredColor() {
		return emphasizedBarExpired.get();
	}

	public TimelineBarColorProvider getConvertedColorProvider() {
		return new TimelineBarColorProvider() {
			@Override
			public Color getFontColor() {
				return emphasizedBarFont.get();
			}

			@Override
			public Color getUpcomingColor() {
				return emphasizedBarUpcoming.get();
			}

			@Override
			public Color getActiveColor() {
				return emphasizedBarActive.get();
			}

			@Override
			public Color getExpiredColor() {
				return emphasizedBarExpired.get();
			}
		};
	}
}
