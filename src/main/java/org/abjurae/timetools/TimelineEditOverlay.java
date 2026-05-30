package org.abjurae.timetools;

import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivsupport.events.actlines.events.AbilityUsedEvent;
import gg.xp.xivsupport.events.actlines.events.ZoneChangeEvent;
import gg.xp.xivsupport.events.state.InCombatChangeEvent;
import gg.xp.xivsupport.events.state.XivState;
import gg.xp.xivsupport.gui.overlay.OverlayConfig;
import gg.xp.xivsupport.gui.overlay.RefreshLoop;
import gg.xp.xivsupport.gui.overlay.ScalableJFrame;
import gg.xp.xivsupport.gui.overlay.XivOverlay;
import gg.xp.xivsupport.models.CombatantType;
import gg.xp.xivsupport.models.XivAbility;
import gg.xp.xivsupport.models.XivCombatant;
import gg.xp.xivsupport.models.XivZone;
import gg.xp.xivsupport.persistence.PersistenceProvider;
import gg.xp.xivsupport.timelines.CustomEventSyncController;
import gg.xp.xivsupport.timelines.CustomTimelineEntry;
import gg.xp.xivsupport.timelines.CustomTimelineItem;
import gg.xp.xivsupport.timelines.TimelineCustomizations;
import gg.xp.xivsupport.timelines.TimelineManager;
import gg.xp.xivsupport.timelines.cbevents.CbEventType;
import gg.xp.xivsupport.timelines.icon.ActionTimelineIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@ScanMe
public class TimelineEditOverlay extends XivOverlay {
	private static final Logger log = LoggerFactory.getLogger(TimelineEditOverlay.class);
	private final static Set<Long> DEFENSIVE_WHITELIST = new HashSet<>(Arrays.asList(
			17L, // Sentinel
			22L, // Bulwark
			27L, // Cover
			30L, // Hallowed Ground
			40L, // Thrill of Battle
			43L, // Holmgang
			44L, // Vengeance
			157L, // Manaward
			735L, // Raw Intuition
			2241L, // Shade Shift
			2887L, // Dismantle
			3540L, // Divine Veil
			3542L, // Sheltron
			3613L, // Collective Unconscious
			3634L, // Dark Mind
			3636L, // Shadow Wall
			7382L, // Intervention
			7385L, // Passage of Arms
			7388L, // Shake it Off
			7393L, // The Blackest Night
			7394L, // Riddle of Earth
			7405L, // Troubadour
			7408L, // Nature's Minne
			7432L, // Divine Benison
			7433L, // Plenary Indulgence
			7498L, // Third Eye
			7531L, // Rampart
			7535L, // Reprisal
			7548L, // Arm's Length
			7549L, // Feint
			7560L, // Addle
			16012L, // Shield Samba
			16014L, // Improvisation
			16015L, // Curing Waltz
			16140L, // Camouflage
			16148L, // Nebula
			16160L, // Heart of Light
			16161L, // Heart of Stone
			16464L, // Nascent Glint
			16471L, // Dark Missionary
			16556L, // Celestial Intersection
			16559L, // Neutral Sect
			16889L, // Tactician
			25746L, // Holy Sheltron
			25751L, // Bloodwhetting
			25754L, // Oblation
			25758L, // Heart of Corundum
			25789L, // Improvised Finish
			25799L, // Radiant Aegis
			25857L, // Magick Barrier
			25861L, // Aquaveil
			34685L, // Tempera Coat
			34686L, // Tempera Grassa
			36920L, // Guardian
			36923L, // Damnation
			36927L, // Shadowed Vigil
			36935L, // Great Nebula
			36962L, // Tengentsu
			37031L // Sun Sign
	));
	private final static Set<Long> HEALING_WHITELIST = new HashSet<>(Arrays.asList(
			140L, // Benediction
			3552L, // Equilibrium
			3569L, // Asylum
			3570L, // Tetragrammaton
			3571L, // Assize
			3614L, // Essential Dignity
			7439L, // Earthly Star
			7541L, // Second Wind
			7542L, // Bloodbath
			8324L, // Stellar Detonation
			16151L, // Aurora
			16553L, // Celestial Opposition
			16557L, // Horoscope
			16558L, // Horoscope (Part 2)
			25830L, // Rekindle
			25862L, // Liturgy of the Bell
			28509L, // Liturgy of the Bell (Part 2)
			36944L, // Earth's Reply
			36997L // Lux Solaris
	));

	private final JButton bossButton;
	private final JButton phaseButton;
	private final JButton playerButton;
	private final JPanel panel;
	private final TimelineManager manager;
	private final XivState state;
	private final TimelineToolsSettings settings;
	private boolean addingBossAbility = false;
	private boolean addingBossPhaseAbility = false;
	private boolean addingPlayerAbility = false;
	private XivZone zone;
	private double lastAddedTime;
	private final Map<Long, List<Double>> abilityRecords;

	public TimelineEditOverlay(
			OverlayConfig oc,
			PersistenceProvider persistence,
			TimelineManager manager,
			XivState state,
			TimelineToolsSettings settings,
			TimelineToolsColorProvider ttcp
	) {
		super("Timeline Tools", "overlays.timeline-tools", oc, persistence);
		this.manager = manager;
		this.state = state;
		this.settings = settings;
		addingBossAbility = false;
		addingPlayerAbility = false;
		abilityRecords = new HashMap<>();

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
		panel.setBackground(new Color(0, 0, 0, 0));
		panel.setFocusable(false);
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
		if (!abilityPassesBossFilters(ability, source)) {
			return false;
		}
		// Check for duplicates for auto add
		if (!addingBossAbility && !addingBossPhaseAbility) {
			if (abilityRecords.containsKey(ability.getId())) {
				long count = abilityRecords.get(ability.getId()).stream().filter(time -> manager.getCurrentProcessor().getEffectiveTime() - time < settings.getThrottleTime().get()).count();
				if (count >= settings.getThrottleLimit().get()) {
					return false;
				}
			}
			return Math.abs(manager.getCurrentProcessor().getEffectiveTime() - lastAddedTime) > 1 && manager.getCurrentProcessor().getEntries().stream().noneMatch(entry -> !entry.callout() &&
					Math.abs(entry.time() - manager.getCurrentProcessor().getEffectiveTime()) < 2);
		} else {
			return manager.getCurrentProcessor().getEntries().stream().noneMatch(entry -> !entry.callout() &&
					Math.abs(entry.time() - manager.getCurrentProcessor().getEffectiveTime()) < 2 &&
					entry.name().equals(ability.getName()));
		}
	}

	private boolean abilityPassesBossFilters(XivAbility ability, XivCombatant source) {
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
		return true;
	}

	private boolean shouldTrackPlayerAbility(AbilityUsedEvent abilityEvent) {
		if (!abilityEvent.getSource().isThePlayer()) {
			return false;
		}
		if (DEFENSIVE_WHITELIST.contains(abilityEvent.getAbility().getId())) {
			return true;
		}
		if (HEALING_WHITELIST.contains(abilityEvent.getAbility().getId())) {
			return true;
		}
		return false;
	}

	@HandleEvents
	public void handleZoneChange(EventContext context, ZoneChangeEvent zoneChangeEvent) {
		this.zone = zoneChangeEvent.getZone();
	}

	@HandleEvents(order = 60_000)
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
		if (abilityPassesBossFilters(abilityEvent.getAbility(), abilityEvent.getSource())) {
			if (abilityRecords.containsKey(abilityEvent.getAbility().getId())) {
				if (abilityRecords.get(abilityEvent.getAbility().getId()).stream().noneMatch(time -> manager.getCurrentProcessor().getEffectiveTime() - time < 1)) {
					abilityRecords.get(abilityEvent.getAbility().getId()).add(manager.getCurrentProcessor().getEffectiveTime());
				}
			} else {
				abilityRecords.put(abilityEvent.getAbility().getId(), new ArrayList<>(List.of(manager.getCurrentProcessor().getEffectiveTime())));
			}
		}
	}

	@HandleEvents
	public void handleCombatChange(EventContext context, InCombatChangeEvent combatChangeEvent) {
		abilityRecords.clear();
	}
}
