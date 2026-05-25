package org.abjurae.timetools;

import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivsupport.gui.overlay.OverlayConfig;
import gg.xp.xivsupport.gui.overlay.RefreshLoop;
import gg.xp.xivsupport.gui.overlay.RefreshType;
import gg.xp.xivsupport.gui.overlay.XivOverlay;
import gg.xp.xivsupport.gui.tables.CustomColumn;
import gg.xp.xivsupport.gui.tables.CustomTableModel;
import gg.xp.xivsupport.persistence.PersistenceProvider;
import gg.xp.xivsupport.persistence.settings.BooleanSetting;
import gg.xp.xivsupport.persistence.settings.IntSetting;
import gg.xp.xivsupport.timelines.TimelineManager;
import gg.xp.xivsupport.timelines.TimelineOverlay;
import gg.xp.xivsupport.timelines.VisualTimelineEntry;
import gg.xp.xivsupport.timelines.gui.TimelineBarRenderer;

import javax.swing.JTable;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@ScanMe
public class EmphasizedBarOverlay extends XivOverlay {
	private final IntSetting numberOfRows;
	private final BooleanSetting reverse;
	private final IntSetting barWidth;
	private final TimelineManager timeline;
	private volatile List<VisualTimelineEntry> current = Collections.emptyList();
	private final CustomTableModel<VisualTimelineEntry> tableModel;
	private final JTable table;

	public EmphasizedBarOverlay(OverlayConfig oc, PersistenceProvider persistence, TimelineManager timeline, TimelineToolsSettings settings, TimelineToolsColorProvider ttcp) {
		super("Emphasized Bar (Timeline)", "overlays.emphasized-bar", oc, persistence);
		numberOfRows = settings.getMaxDisplayedBars();
		reverse = settings.getEmphasizedReverse();
		this.timeline = timeline;
		numberOfRows.addListener(this::repackSize);
		barWidth = settings.getBarWidth();
		barWidth.addListener(this::repackSize);
		tableModel = CustomTableModel.builder(() -> current)
				.addColumn(new CustomColumn<>("Bar", Function.identity(),
						c -> {
							c.setCellRenderer(new TimelineBarRenderer(ttcp.getConvertedColorProvider()));
						}))
				.build();
		table = new JTable(tableModel);
		table.setOpaque(false);
		tableModel.configureColumns(table);
		table.setCellSelectionEnabled(false);
		getPanel().add(table);
	}

	@Override
	public void finishInit() {
		super.finishInit();
		repackSize();
		RefreshLoop<EmphasizedBarOverlay> refresher = new RefreshLoop<>("Emphasized Bar Overlay", this, EmphasizedBarOverlay::refresh, dt -> dt.calculateScaledFrameTime(200));
		refresher.start();
	}

	@Override
	protected void repackSize() {
		table.setPreferredSize(new Dimension(barWidth.get(), table.getRowHeight() * numberOfRows.get()));
		super.repackSize();
	}

	private RefreshType getAndSort() {
		if (!getEnabled().get()) {
			current = Collections.emptyList();
			return RefreshType.NONE;
		}
		current = timeline.getCurrentDisplayEntries().stream().filter(entry -> !entry.originalTimelineEntry().callout()).limit(numberOfRows.get()).collect(Collectors.toList());
		if (reverse.get()) {
			Collections.reverse(current);
		}
		return RefreshType.FULL;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			refresh();
		}
	}

	private void refresh() {
		if (isVisible()) {
			RefreshType refreshTypeNeeded = getAndSort();
			switch (refreshTypeNeeded) {
				case FULL -> tableModel.fullRefresh();
				case REPAINT -> table.repaint();
			}
		}
	}
}
