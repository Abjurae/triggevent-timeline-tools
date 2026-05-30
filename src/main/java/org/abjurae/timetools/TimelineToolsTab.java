package org.abjurae.timetools;

import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivsupport.gui.WrapLayout;
import gg.xp.xivsupport.gui.extra.TopDownSimplePluginTab;
import gg.xp.xivsupport.persistence.gui.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.FlowLayout;

@ScanMe
public class TimelineToolsTab extends TopDownSimplePluginTab {
	private final TimelineToolsSettings settings;
	private final TimelineToolsColorProviderImpl ttcp;

	public TimelineToolsTab(TimelineToolsSettings settings, TimelineToolsColorProviderImpl ttcp) {
		super("Timeline Tools", 600);
		this.settings = settings;
		this.ttcp = ttcp;
	}

	@Override
	public int getSortOrder() {
		return 10;
	}

	@Override
	protected Component[] provideChildren(JPanel outer) {
		JLabel emphasizedLabel = new JLabel("Emphasized Bar Settings");
		JPanel emphasizedBarPanel = new JPanel();
		emphasizedBarPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 3, 3));
		emphasizedBarPanel.add(new BooleanSettingGui(settings.getEmphasizedEnabled(), "Enabled").getComponent());
		emphasizedBarPanel.add(new IntSettingSpinner(settings.getMaxDisplayedBars(), "Max Displayed Bars").getComponent());
		emphasizedBarPanel.add(new IntSettingGui(settings.getBarWidth(), "Bar Width").getComponent());
		emphasizedBarPanel.add(new BooleanSettingGui(settings.getEmphasizedReverse(), "Reverse List").getComponent());

		JLabel colorsLabel = new JLabel("Color Settings");
		JPanel colorsPanel = new JPanel();
		colorsPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 3, 3));
		colorsPanel.add(new ColorSettingGui(ttcp.getEmphasizedBarFontSetting(), "Emphasized Bar Font Color", () -> true).getComponent());
		colorsPanel.add(new ColorSettingGui(ttcp.getEmphasizedBarUpcomingSetting(), "Emphasized Bar Upcoming Color", () -> true).getComponent());
		colorsPanel.add(new ColorSettingGui(ttcp.getEmphasizedBarActiveSetting(), "Emphasized Bar Active Color", () -> true).getComponent());
		colorsPanel.add(new ColorSettingGui(ttcp.getEmphasizedBarExpiredSetting(), "Emphasized Bar Expired Color", () -> true).getComponent());

		JLabel editToolsLabel = new JLabel("Edit Tools");
		JPanel editToolsPanel = new JPanel();
		editToolsPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 3, 3));
		editToolsPanel.add(new BooleanSettingGui(settings.getAutoAddBossAbilities(), "Automatically Add Boss Abilities").getComponent());
		editToolsPanel.add(new IntSettingGui(settings.getThrottleLimit(), "Throttle Limit (Stop counting an ability after this many occurences)").getComponent());
		editToolsPanel.add(new IntSettingGui(settings.getThrottleTime(), "Throttle Time (Window in which occurences will be counted over)").getComponent());
		editToolsPanel.add(new BooleanSettingGui(settings.getShowBossButton(), "Show Boss Ability Button").getComponent());
		editToolsPanel.add(new BooleanSettingGui(settings.getShowPhaseButton(), "Show Boss Ability (With Phase) Button").getComponent());
		editToolsPanel.add(new BooleanSettingGui(settings.getShowPlayerButton(), "Show Player Callout Button").getComponent());
		editToolsPanel.add(new DoubleSettingGui(settings.getCalloutAdvanceSeconds(), "Callout Seconds In Advance").getComponent());

		return new Component[]{emphasizedLabel, emphasizedBarPanel, colorsLabel, colorsPanel, editToolsLabel, editToolsPanel};
	}
}
