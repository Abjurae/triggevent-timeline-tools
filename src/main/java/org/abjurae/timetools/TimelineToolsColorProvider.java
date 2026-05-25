package org.abjurae.timetools;

import gg.xp.xivsupport.timelines.gui.TimelineBarColorProvider;

import java.awt.Color;

public interface TimelineToolsColorProvider {
	Color getFontColor();
	Color getUpcomingColor();
	Color getActiveColor();
	Color getExpiredColor();

	TimelineBarColorProvider getConvertedColorProvider();
}
