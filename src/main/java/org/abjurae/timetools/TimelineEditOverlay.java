package org.abjurae.timetools;

import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivdata.data.ZoneInfo;
import gg.xp.xivdata.data.ZoneLibrary;
import gg.xp.xivsupport.events.actlines.events.AbilityUsedEvent;
import gg.xp.xivsupport.events.actlines.events.ZoneChangeEvent;
import gg.xp.xivsupport.events.state.XivState;
import gg.xp.xivsupport.events.state.combatstate.ActiveCastRepository;
import gg.xp.xivsupport.events.state.combatstate.CastTracker;
import gg.xp.xivsupport.gui.overlay.OverlayConfig;
import gg.xp.xivsupport.gui.overlay.RefreshLoop;
import gg.xp.xivsupport.gui.overlay.ScalableJFrame;
import gg.xp.xivsupport.gui.overlay.XivOverlay;
import gg.xp.xivsupport.models.CombatantType;
import gg.xp.xivsupport.models.XivAbility;
import gg.xp.xivsupport.models.XivCombatant;
import gg.xp.xivsupport.models.XivZone;
import gg.xp.xivsupport.persistence.PersistenceProvider;
import gg.xp.xivsupport.timelines.*;
import gg.xp.xivsupport.timelines.cbevents.CbEventType;
import gg.xp.xivsupport.timelines.gui.TimelinesTab;
import gg.xp.xivsupport.timelines.icon.ActionTimelineIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.util.*;
import java.util.regex.Pattern;

@ScanMe
public class TimelineEditOverlay extends XivOverlay {
	private static final Logger log = LoggerFactory.getLogger(TimelineEditOverlay.class);
	private final static Set<Long> DEFENSIVE_WHITELIST = new HashSet<>(Arrays.asList(
			22L, // Bulwark
			30L, // Hallowed Ground
			3540L, // Divine Veil
			7385L, // Passage of Arms
			7531L, // Rampart
			7535L, // Reprisal
			25746L, // Holy Sheltron
			36920L // Guardian
	));

	private final JButton bossButton;
	private final JButton phaseButton;
	private final JButton playerButton;
	private final JPanel panel;
	private final TimelineManager manager;
	private final ActiveCastRepository acr;
	private final XivState state;
	private final TimelineToolsSettings settings;
	private boolean addingBossAbility = false;
	private boolean addingBossPhaseAbility = false;
	private boolean addingPlayerAbility = false;
	private XivZone zone;
	private double lastAddedTime;

	public TimelineEditOverlay(
			OverlayConfig oc,
			PersistenceProvider persistence,
			TimelineManager manager,
			ActiveCastRepository acr,
			XivState state,
			TimelineToolsSettings settings,
			TimelineToolsColorProvider ttcp
	) {
		super("Timeline Tools", "overlays.timeline-tools", oc, persistence);
		this.manager = manager;
		this.acr = acr;
		this.state = state;
		this.settings = settings;
		addingBossAbility = false;
		addingPlayerAbility = false;

		panel = new JPanel();
		bossButton = new JButton("Boss");
		bossButton.setBackground(new Color(120, 0, 0));
		bossButton.addActionListener(l -> this.addBossAbilityToTimeline());
		bossButton.setFocusable(false);
		if (settings.getShowBossButton().get()) {
			panel.add(bossButton);
		}
		phaseButton = new JButton("Phase");
		phaseButton.setBackground(new Color(0, 0, 120));
		phaseButton.addActionListener(l -> this.addBossPhaseToTimeline());
		phaseButton.setFocusable(false);
		if (settings.getShowPhaseButton().get()) {
			panel.add(phaseButton);
		}
		playerButton = new JButton("Player");
		playerButton.setBackground(new Color(0, 120, 0));
		playerButton.addActionListener(l -> this.addPlayerAbilityToTimeline());
		playerButton.setFocusable(false);
		if (settings.getShowPlayerButton().get()) {
			panel.add(playerButton);
		}
		getPanel().add(panel);
		settings.getShowBossButton().addListener(this::toggleShowBoss);
		settings.getShowPhaseButton().addListener(this::toggleShowPhase);
		settings.getShowPlayerButton().addListener(this::toggleShowPlayer);
	}

	@Override
	public void finishInit() {
		super.finishInit();
		repackSize();
		RefreshLoop<TimelineEditOverlay> refresher = new RefreshLoop<>("TimelineEditOverlay", this, TimelineEditOverlay::refresh, dt -> dt.calculateScaledFrameTime(500));
		refresher.start();
	}

	private void refresh() {
		((ScalableJFrame) getFrame()).setClickThrough(false);
		getFrame().setFocusableWindowState(false);
	}

	private void toggleShowBoss() {
		panel.remove(bossButton);
		if (settings.getShowBossButton().get()) {
			panel.add(bossButton);
		}
		repackSize();
	}

	private void toggleShowPhase() {
		panel.remove(phaseButton);
		if (settings.getShowPhaseButton().get()) {
			panel.add(phaseButton);
		}
		repackSize();
	}

	private void toggleShowPlayer() {
		panel.remove(playerButton);
		if (settings.getShowPlayerButton().get()) {
			panel.add(playerButton);
		}
		repackSize();
	}

	private void addBossAbilityToTimeline() {
		log.info("Adding Boss Ability to Timeline!");
		addingBossAbility = true;
	}

	private void addBossPhaseToTimeline() {
		addingBossPhaseAbility = true;
		log.info("Adding Boss Ability with Phasing to Timeline!");
	}

	private void addPlayerAbilityToTimeline() {
		addingPlayerAbility = true;
		log.info("Adding Player Ability to Timeline!");
	}

	private void addAbilityToTimeline(AbilityUsedEvent abilityEvent, boolean callout) {
		if (zone == null) {
			log.error("Zone not set, unable to add {} to timeline", abilityEvent.getAbility().getName());
			return;
		}
		if (manager.getTimeline(zone.getId()) == null) {
			manager.addCustomZone(zone.getId());
		}
		if (manager.getCurrentProcessor().getEffectiveTime() == 0.0d) {
			log.warn("Timeline not started, disregarding entry");
			return;
		}
		TimelineCustomizations custom = manager.getCustomSettings(zone.getId());
		CustomTimelineEntry entry = new CustomTimelineEntry();
		entry.name = abilityEvent.getAbility().getName();
		entry.time = manager.getCurrentProcessor().getEffectiveTime();
		if (callout) {
			entry.callout = true;
			entry.iconSpec = new ActionTimelineIcon(abilityEvent.getAbility().getId());
			entry.calloutPreTime = settings.getCalloutAdvanceSeconds().get();
			entry.enabledForJob(state.getPlayerJob());
		} else {
			Map<String, List<String>> conditions = new HashMap<>();
			conditions.put("id", List.of(Long.toHexString(abilityEvent.getAbility().getId())));
			conditions.put("source", List.of(abilityEvent.getSource().getName()));
			entry.esc = new CustomEventSyncController(CbEventType.Ability, conditions);
			if (addingBossPhaseAbility) {
				entry.windowStart = 100d;
				entry.windowEnd = 100d;
			}
		}
		List<CustomTimelineItem> customEntries = new ArrayList<>(custom.getEntries());
		customEntries.add(entry);
		custom.setEntries(customEntries);
	}

	private boolean shouldTrackBossAbility(XivAbility ability, XivCombatant source) {
		if (source.getType() != CombatantType.NPC && source.getType() != CombatantType.FAKE) {
			return false;
		}
		if (ability.getName().startsWith("unknown_") || ability.getName().equals("attack")
				|| ability.getName().equals("攻撃")) {
			// Auto Attack, ignore
			return false;
		}
		if (source.getName().equals("Earthly Star")) {
			return false;
		}
		Pattern jpPattern = Pattern.compile(".*\\p{InHiragana}.*");
		if (jpPattern.matcher(ability.getName()).find()) {
			// JP text = invisible cast, usually repositioning or auto attacks
			return false;
		}
		// Check for duplicates for auto add
		if (!addingBossAbility && !addingBossPhaseAbility) {
			return Math.abs(manager.getCurrentProcessor().getEffectiveTime() - lastAddedTime) > 1 && manager.getCurrentProcessor().getEntries().stream().noneMatch(entry -> !entry.callout() &&
					Math.abs(entry.time() - manager.getCurrentProcessor().getEffectiveTime()) < 2);
		} else {
			return manager.getCurrentProcessor().getEntries().stream().noneMatch(entry -> !entry.callout() &&
					Math.abs(entry.time() - manager.getCurrentProcessor().getEffectiveTime()) < 2 &&
					entry.name().equals(ability.getName()));
		}
	}

	private boolean shouldTrackPlayerAbility(AbilityUsedEvent abilityEvent) {
		if (!abilityEvent.getSource().isThePlayer()) {
			return false;
		}
		if (DEFENSIVE_WHITELIST.contains(abilityEvent.getAbility().getId())) {
			return true;
		}
		return false;
	}

	@HandleEvents
	public void handleZoneChange(EventContext context, ZoneChangeEvent zoneChangeEvent) {
		this.zone = zoneChangeEvent.getZone();
	}

	@HandleEvents
	public void handleAbilityUsedEvent(EventContext context, AbilityUsedEvent abilityEvent) {
		if (manager.getCurrentProcessor() == null) {
			return;
		}
		if (addingPlayerAbility && shouldTrackPlayerAbility(abilityEvent)) {
			log.info("Adding {} to timeline", abilityEvent.getAbility().getName());
			addAbilityToTimeline(abilityEvent, true);
			addingPlayerAbility = false;
		}
		if ((addingBossAbility || addingBossPhaseAbility || settings.getAutoAddBossAbilities().get()) &&
				shouldTrackBossAbility(abilityEvent.getAbility(), abilityEvent.getSource())) {
			log.info("Adding {} to timeline", abilityEvent.getAbility().getName());
			addAbilityToTimeline(abilityEvent, false);
			addingBossAbility = false;
			addingBossPhaseAbility = false;
			lastAddedTime = manager.getCurrentProcessor().getEffectiveTime();
		}
	}
}
