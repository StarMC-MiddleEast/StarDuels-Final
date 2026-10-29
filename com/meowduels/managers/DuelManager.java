package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.gui.DuelConfirmMenu;
import com.meowduels.gui.KitPickMenu;
import com.meowduels.gui.MatchSummaryMenu;
import com.meowduels.model.ActiveDuel;
import com.meowduels.model.Arena;
import com.meowduels.model.DuelRequest;
import com.meowduels.model.Kit;
import com.meowduels.model.PlayerSnapshot;
import com.meowduels.util.AntiCheatBypass;
import com.meowduels.util.Colors;
import com.meowduels.util.Cooldowns;
import com.meowduels.util.GameModeGuard;
import com.meowduels.util.MiniMessageUtil;
import com.meowduels.util.LaunchToSpawn;
import com.meowduels.util.Sounds;
import com.meowduels.util.SpawnItems;
import com.meowduels.util.Text;
import com.meowduels.util.Trims;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class DuelManager {
    private final MeowDuels plugin;
    private final long requestTtlMs;
    private final int countdownSeconds;
    private final long transitionTicks;
    private final long matchEndTicks;
    private final long deathSpectateTicks;
    private final long winEndTicks;
    private final long matchFoundTicks;
    private final Map<UUID, Map<UUID, DuelRequest>> requests = new HashMap<UUID, Map<UUID, DuelRequest>>();
    private final Map<UUID, ActiveDuel> playerDuels = new ConcurrentHashMap<UUID, ActiveDuel>();
    private final Map<UUID, Location> pendingRoundRespawns = new ConcurrentHashMap<UUID, Location>();
    private final Map<UUID, PlayerSnapshot> snapshots = new HashMap<UUID, PlayerSnapshot>();
    private final Map<UUID, BukkitTask> roundLaunchTasks = new ConcurrentHashMap<UUID, BukkitTask>();
    private static final Set<UUID> ROUND_LAUNCH_FALL_IMMUNITY = ConcurrentHashMap.newKeySet();
    private final Set<String> arenasInUse = new HashSet<String>();
    private final Map<UUID, UUID> lastOpponent = new HashMap<UUID, UUID>();
    private static final String REQUEST_COOLDOWN = "duel-request";
    private int gameCounter = 0;
    private volatile int fightingPlayers = 0;
    private volatile int fightingMatches = 0;

    public DuelManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.requestTtlMs = (long)Math.max(1, meowDuels.getConfig().getInt("duel.request-expiry-seconds", 60)) * 1000L;
        this.countdownSeconds = Math.max(1, meowDuels.getConfig().getInt("rounds.countdown-seconds", 5));
        this.transitionTicks = (long)Math.max(0.0, meowDuels.getConfig().getDouble("rounds.transition-seconds", 3.5) * 20.0);
        this.matchEndTicks = (long)Math.max(0.0, meowDuels.getConfig().getDouble("rounds.end-seconds", 3.5) * 20.0);
        this.deathSpectateTicks = (long)Math.max(0.0, meowDuels.getConfig().getDouble("rounds.death-spectate-seconds", 2.0) * 20.0);
        this.winEndTicks = (long)Math.max(0.0, meowDuels.getConfig().getDouble("rounds.win-teleport-seconds", 2.0) * 20.0);
        this.matchFoundTicks = (long)Math.max(0.0, meowDuels.getConfig().getDouble("rounds.match-found-seconds", 4.0) * 20.0);
    }

    public void sendRequest(Player player, Player player2, String string, int n, String string2) {
        DuelRequest duelRequest;
        if (player.equals((Object)player2)) {
            player.sendMessage(this.msg("duel.cannot-duel-self", new String[0]));
            return;
        }
        if (this.isInDuel(player.getUniqueId())) {
            player.sendMessage(this.msg("duel.already-in-duel", new String[0]));
            return;
        }
        if (this.isInDuel(player2.getUniqueId())) {
            player.sendMessage(this.msg("duel.target-in-duel", "target", player2.getName()));
            return;
        }
        if (this.plugin.getEventManager().isInvolved(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.duel.t-duel-while-re-2", new String[0]));
            return;
        }
        if (this.plugin.getEventManager().isInvolved(player2.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.duel.event-right-now", "target", player2.getName()));
            return;
        }
        if (!this.plugin.getPlayerSettings().isDuelRequests(player2.getUniqueId())) {
            player.sendMessage(this.msg("duel.requests-off", "target", player2.getName()));
            return;
        }
        if (this.plugin.getPartyManager().inParty(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.duel.re-party-leave-first-2", new String[0]));
            return;
        }
        if (this.plugin.getPartyManager().inParty(player2.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.duel.party-right-now", "target", player2.getName()));
            return;
        }
        Map<UUID, DuelRequest> map = this.requests.get(player2.getUniqueId());
        DuelRequest duelRequest2 = duelRequest = map == null ? null : map.get(player.getUniqueId());
        if (duelRequest != null && !duelRequest.isExpired(this.requestTtlMs)) {
            player.sendMessage(this.plugin.messages().get("notice.duel.already-challenge-give-them", "target", player2.getName()));
            Sounds.deny(player);
            return;
        }
        long l = Cooldowns.remaining(player, REQUEST_COOLDOWN);
        if (l > 0L) {
            player.sendMessage(this.plugin.messages().get("notice.duel.wait-s-before-sending", "value", String.valueOf(Cooldowns.seconds(l))));
            Sounds.deny(player);
            return;
        }
        Cooldowns.start(player, REQUEST_COOLDOWN, null, (long)(this.plugin.getConfig().getDouble("duel.request-cooldown-seconds", 5.0) * 1000.0));
        if (this.plugin.getKitManager().get(string) == null) {
            player.sendMessage(this.msg("duel.kit-gone", new String[0]));
            return;
        }
        if (!this.hasFreeArenaFor(string)) {
            player.sendMessage(this.msg("duel.no-arenas", new String[0]));
            this.explainNoArena(player, string);
            return;
        }
        DuelRequest duelRequest3 = new DuelRequest(player.getUniqueId(), player2.getUniqueId(), string, n, string2);
        this.requests.computeIfAbsent(player2.getUniqueId(), uUID -> new HashMap()).put(player.getUniqueId(), duelRequest3);
        player.sendMessage(this.msg("duel.sent", "target", player2.getName(), "kit", string, "rounds", String.valueOf(n)));
        this.sendRequestCard(player2, player, string, n);
    }

    private void sendRequestCard(Player player, Player player2, String string, int n) {
        String string2 = string.replace('_', ' ').toUpperCase(Locale.ROOT);
        player.sendMessage("");
        player.sendMessage(this.msg("request.header", "sender", player2.getName()));
        player.sendMessage(this.msg("request.kit", "kit", string2));
        player.sendMessage(this.msg("request.rounds", "rounds", String.valueOf(n)));
        player.sendMessage("");
        TextComponent textComponent = new TextComponent(this.msg("request.click", new String[0]));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept " + player2.getName()));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.msg("request.click-hover", "sender", player2.getName())).create()));
        player.spigot().sendMessage(new BaseComponent[]{textComponent});
        player.sendMessage("");
        Sounds.request(player);
    }

    private String msg(String string, String ... stringArray) {
        return this.plugin.messages().get(string, stringArray);
    }

    public void acceptRequest(Player player, UUID uUID) {
        Map<UUID, DuelRequest> map = this.requests.get(player.getUniqueId());
        DuelRequest duelRequest = map == null ? null : map.get(uUID);
        DuelRequest duelRequest2 = duelRequest;
        if (duelRequest == null || duelRequest.isExpired(this.requestTtlMs)) {
            player.sendMessage(this.msg("accept.expired", new String[0]));
            if (map != null) {
                map.remove(uUID);
            }
            return;
        }
        Player player2 = Bukkit.getPlayer((UUID)uUID);
        if (player2 == null) {
            player.sendMessage(this.msg("accept.sender-offline", new String[0]));
            map.remove(uUID);
            return;
        }
        if (this.isInDuel(player2.getUniqueId()) || this.isInDuel(player.getUniqueId())) {
            player.sendMessage(this.msg("accept.one-in-duel", new String[0]));
            return;
        }
        if (this.plugin.getEventManager().isInvolved(player2.getUniqueId()) || this.plugin.getEventManager().isInvolved(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.duel.duel-t-start-someone", new String[0]));
            return;
        }
        Arena arena = this.resolveArena(duelRequest);
        if (arena == null) {
            player.sendMessage(this.msg("accept.no-arena", new String[0]));
            player2.sendMessage(this.msg("accept.no-arena", new String[0]));
            return;
        }
        map.remove(uUID);
        Sounds.accept(player2);
        Sounds.accept(player);
        this.startDuel(player2, player, arena, duelRequest.getKit(), duelRequest.getRounds());
    }

    private Arena resolveArena(DuelRequest duelRequest) {
        Arena arena2;
        if (duelRequest.getArena() != null && (arena2 = this.plugin.getArenaManager().get(duelRequest.getArena())) != null && arena2.isConfigured() && arena2.isEnabled() && arena2.supportsKit(duelRequest.getKit()) && !this.isArenaInUse(arena2.getName())) {
            return arena2;
        }
        return this.plugin.getArenaManager().findFreeArena(arena -> this.isArenaInUse(arena.getName()) || !arena.supportsKit(duelRequest.getKit()));
    }

    public DuelRequest getMostRecentRequest(Player player) {
        Map<UUID, DuelRequest> map = this.requests.get(player.getUniqueId());
        if (map == null || map.isEmpty()) {
            return null;
        }
        DuelRequest duelRequest = null;
        for (DuelRequest duelRequest2 : map.values()) {
            if (duelRequest2.isExpired(this.requestTtlMs) || duelRequest != null && duelRequest2.getCreatedAt() <= duelRequest.getCreatedAt()) continue;
            duelRequest = duelRequest2;
        }
        return duelRequest;
    }

    private void startDuel(Player player, Player player2, Arena arena, String string, int n) {
        if (this.isArenaBusy(arena)) {
            String string2 = this.msg("duel.arena-booked", new String[0]);
            player.sendMessage(string2);
            player2.sendMessage(string2);
            return;
        }
        boolean queuedMatch = this.plugin.getQueueManager().isQueuedFor(player.getUniqueId(), string)
            && this.plugin.getQueueManager().isQueuedFor(player2.getUniqueId(), string);
        this.plugin.getQueueManager().remove(player.getUniqueId());
        this.plugin.getQueueManager().remove(player2.getUniqueId());
        this.markArenaInUse(arena.getName());
        this.snapshots.put(player.getUniqueId(), PlayerSnapshot.capture(player));
        this.snapshots.put(player2.getUniqueId(), PlayerSnapshot.capture(player2));
        ActiveDuel activeDuel = new ActiveDuel(player.getUniqueId(), player2.getUniqueId(), arena, string, n);
        activeDuel.setGameNumber(++this.gameCounter);
        this.playerDuels.put(player.getUniqueId(), activeDuel);
        this.playerDuels.put(player2.getUniqueId(), activeDuel);
        this.plugin.getScoreboardService().attach(player, activeDuel);
        this.plugin.getScoreboardService().attach(player2, activeDuel);
        this.plugin.getTabService().attach(activeDuel);
        this.sendStartCard(player, player2.getName(), activeDuel);
        this.sendStartCard(player2, player.getName(), activeDuel);
        if (arena.isGlowingOpponent()) {
            this.applyGlow(player, player2, true);
        }
        this.matchFound(activeDuel, queuedMatch);
    }

    private void matchFound(ActiveDuel activeDuel, boolean queuedMatch) {
        activeDuel.setState(ActiveDuel.State.STARTING);
        Player player = Bukkit.getPlayer((UUID)activeDuel.getPlayer1());
        Player player2 = Bukkit.getPlayer((UUID)activeDuel.getPlayer2());
        if (player == null || player2 == null) {
            this.handleDisconnect(player == null ? activeDuel.getPlayer1() : activeDuel.getPlayer2());
            return;
        }
        this.playMatchFound(player);
        this.playMatchFound(player2);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (this.isStill(activeDuel)) {
                this.startRound(activeDuel);
            }
        }, this.matchFoundTicks);
    }

    private void playMatchFound(Player player) {
        String string = this.msg("titles.match-found.title", new String[0]);
        Sounds.matchFound(player);
        long l = Math.max(20L, this.matchFoundTicks);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, (int)l, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int)l, 0));
        String[] stringArray = new String[]{
                this.msg("titles.match-found.progress-1", new String[0]),
                this.msg("titles.match-found.progress-2", new String[0]),
                this.msg("titles.match-found.progress-3", new String[0]),
                this.msg("titles.match-found.progress-4", new String[0])};
        this.sendTitle(player, string, this.msg("titles.match-found.subtitle", new String[0]), 6, 20, 0);
        UUID uUID = player.getUniqueId();
        for (int i = 1; i < stringArray.length; ++i) {
            String string2 = stringArray[i];
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                Player current = Bukkit.getPlayer((UUID)uUID);
                if (current != null && this.playerDuels.containsKey(uUID)) {
                    this.sendTitle(current, string, string2, 0, 20, 0);
                }
            }, (long)i * 10L);
        }
    }

    private void applyGlow(Player player, Player player2, boolean bl) {
        try {
            player.setGlowing(bl);
            player2.setGlowing(bl);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void sendStartCard(Player player, String string, ActiveDuel activeDuel) {
        String string2 = this.kitLabel(activeDuel.getKit());
        String string3 = String.valueOf(activeDuel.getRoundsToWin());
        player.sendMessage(this.msg("duel.start.header", "opponent", string));
        player.sendMessage(this.msg("duel.start.info", "kit", string2, "rounds", string3));
        player.sendMessage(this.msg("duel.start.leave", new String[0]));
    }

    private String kitLabel(String string) {
        Kit kit = this.plugin.getKitManager().get(string);
        if (kit != null && kit.getDisplayName() != null && !kit.getDisplayName().isEmpty()) {
            return kit.getDisplayName().replaceAll("<[^>]*>", "");
        }
        return string;
    }

    private void startRound(ActiveDuel activeDuel) {
        Location location;
        // Anything the between-rounds pass has not reached by now stays as it
        // is - better than letting it revert blocks placed in this round.
        this.plugin.getArenaManager().cancelRegen(activeDuel.getArena());
        activeDuel.setState(ActiveDuel.State.STARTING);
        Player player = Bukkit.getPlayer((UUID)activeDuel.getPlayer1());
        Player player2 = Bukkit.getPlayer((UUID)activeDuel.getPlayer2());
        if (player == null || player2 == null) {
            this.handleDisconnect(player == null ? activeDuel.getPlayer1() : activeDuel.getPlayer2());
            return;
        }
        Arena arena = activeDuel.getArena();
        Location location2 = arena == null ? null : arena.getSpawn1();
        Location location3 = location = arena == null ? null : arena.getSpawn2();
        if (arena == null || !arena.isConfigured() || location2 == null || location == null || location2.getWorld() == null || location.getWorld() == null) {
            String string = arena == null ? "?" : arena.getName();
            this.plugin.getLogger().warning("Cancelled duel start: arena '" + string + "' has a missing or unloaded world on a spawn. Check its world name and both spawn points.");
            this.forceCancelBooked(activeDuel, this.msg("duel.arena-world-unavailable", "arena", string));
            return;
        }
        Kit kit = this.plugin.getKitManager().get(activeDuel.getKit());
        activeDuel.setArenaEntered(true);
        this.prepare(player, arena, location2, kit);
        this.prepare(player2, arena, location, kit);
        this.verifyArrival(player, location2);
        this.verifyArrival(player2, location);
        String string = String.valueOf(activeDuel.getCurrentRound());
        String string2 = String.valueOf(activeDuel.getRoundsToWin());
        player.sendMessage(this.msg("duel.round", "round", string, "roundsToWin", string2));
        player2.sendMessage(this.msg("duel.round", "round", string, "roundsToWin", string2));
        activeDuel.clearReady();
        this.plugin.getSpectateManager().followRoundStart(activeDuel.getPlayer1(), activeDuel.getPlayer2());
        int n = activeDuel.getArena().getCountdownOverride();
        int n2 = (n > 0 ? n : this.countdownSeconds) * 20;
        activeDuel.startCountdown((long)n2 * 50L);
        this.countdownTick(activeDuel, n2, n2);
    }

    private void verifyArrival(Player player, Location location) {
        if (player == null || location == null || location.getWorld() == null) {
            return;
        }
        Location location2 = player.getLocation();
        boolean bl = location2.getWorld() != null && location2.getWorld().equals((Object)location.getWorld());
        double d = location2.getX() - location.getX();
        double d2 = location2.getY() - location.getY();
        double d3 = location2.getZ() - location.getZ();
        if (!bl || d * d + d2 * d2 + d3 * d3 > 9.0) {
            this.plugin.getLogger().warning("Teleport into the arena did not take effect for " + player.getName() + " - they are at " + DuelManager.describe(location2) + " instead of " + DuelManager.describe(location) + ". Something else is blocking or moving them.");
        }
    }

    private static String describe(Location location) {
        if (location == null || location.getWorld() == null) {
            return "unknown";
        }
        return location.getWorld().getName() + " " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private void prepare(Player player, Arena arena, Location location, Kit kit) {
        String string = location != null && location.getWorld() != null ? location.getWorld().getName() : null;
        AntiCheatBypass.grant(this.plugin, player, AntiCheatBypass.worldNodes(this.plugin, string));
        try {
            player.addScoreboardTag("meowduel");
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        DuelManager.ground(player);
        player.teleport(location);
        player.setVelocity(new Vector(0.0, 0.0, 0.0));
        player.setFallDistance(0.0f);
        player.setNoDamageTicks(0);
        player.setInvulnerable(false);
        GameModeGuard.pin(player, GameMode.SURVIVAL);
        try {
            player.setHealth(player.getMaxHealth());
        }
        catch (Throwable throwable) {
            player.setHealth(20.0);
        }
        player.setFoodLevel(20);
        player.setSaturation(0.0f);
        player.setExhaustion(0.0f);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        try {
            player.setAbsorptionAmount(0.0);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (kit != null) {
            Object object;
            this.plugin.getKitLayouts().applyTo(player, kit);
            this.applyPlayerTrims(player, kit.getName());
            Kit kit2 = kit;
            object = player.getUniqueId();
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.lambda$prepare$4((UUID)object, kit2), 1L);
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.lambda$prepare$5((UUID)object, kit2), 3L);
        }
    }

    private void giveStartEffects(ActiveDuel activeDuel, Player player, Player player2) {
        Kit kit = this.plugin.getKitManager().get(activeDuel.getKit());
        if (kit == null) {
            return;
        }
        kit.applyStartEffects(player);
        kit.applyStartEffects(player2);
    }

    private void countdownTick(ActiveDuel activeDuel, int n, int n2) {
        if (activeDuel.getState() != ActiveDuel.State.STARTING) {
            return;
        }
        Player player = Bukkit.getPlayer((UUID)activeDuel.getPlayer1());
        Player player2 = Bukkit.getPlayer((UUID)activeDuel.getPlayer2());
        if (player == null || player2 == null) {
            return;
        }
        if (activeDuel.getReadyCount() >= 2 || n <= 0) {
            boolean bl = activeDuel.getReadyCount() >= 2 && n > 0;
            this.finishCountdown(activeDuel, bl);
            return;
        }
        Component component = this.readyBar(activeDuel);
        player.sendActionBar(component);
        player2.sendActionBar(component);
        if (n % 20 == 0) {
            int n3 = n / 20;
            this.showCountdownNumber(player, n3);
            this.showCountdownNumber(player2, n3);
            int n4 = Math.max(1, (n2 + 19) / 20);
            Sounds.countdown(player, n3, n4);
            Sounds.countdown(player2, n3, n4);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.countdownTick(activeDuel, n - 10, n2), 10L);
    }

    private void showCountdownNumber(Player player, int n) {
        this.sendTitle(player, this.msg("titles.countdown.title", "seconds", String.valueOf(n)), this.msg("titles.countdown.subtitle", "seconds", String.valueOf(n)), 0, 12, 8);
    }

    private void finishCountdown(ActiveDuel activeDuel, boolean bl) {
        Object object;
        if (activeDuel.getState() != ActiveDuel.State.STARTING) {
            return;
        }
        Player player = Bukkit.getPlayer((UUID)activeDuel.getPlayer1());
        Player player2 = Bukkit.getPlayer((UUID)activeDuel.getPlayer2());
        if (player == null || player2 == null) {
            return;
        }
        activeDuel.setState(ActiveDuel.State.FIGHTING);
        activeDuel.markFightStart();
        player.setNoDamageTicks(0);
        player2.setNoDamageTicks(0);
        player.setInvulnerable(false);
        player2.setInvulnerable(false);
        this.giveStartEffects(activeDuel, player, player2);
        Component component = DuelManager.mm("");
        player.sendActionBar(component);
        player2.sendActionBar(component);
        if (bl) {
            object = this.msg("countdown.forced", new String[0]);
            player.sendMessage((String)object);
            player2.sendMessage((String)object);
        }
        this.sendTitle(player, this.msg("titles.fight.title", new String[0]),
                this.msg("titles.fight.subtitle", "yourScore", String.valueOf(activeDuel.getScoreFor(activeDuel.getPlayer1())), "theirScore", String.valueOf(activeDuel.getScoreFor(activeDuel.getPlayer2()))), 0, 100, 10);
        this.sendTitle(player2, this.msg("titles.fight.title", new String[0]),
                this.msg("titles.fight.subtitle", "yourScore", String.valueOf(activeDuel.getScoreFor(activeDuel.getPlayer2())), "theirScore", String.valueOf(activeDuel.getScoreFor(activeDuel.getPlayer1()))), 0, 100, 10);
        Sounds.fight(player);
        Sounds.fight(player2);
    }

    public void markReady(UUID uUID) {
        ActiveDuel activeDuel = this.playerDuels.get(uUID);
        if (activeDuel == null || activeDuel.getState() != ActiveDuel.State.STARTING || !activeDuel.isArenaEntered()) {
            return;
        }
        if (!activeDuel.addReady(uUID)) {
            return;
        }
        Player player = Bukkit.getPlayer((UUID)uUID);
        String string = player != null ? player.getName() : "A player";
        String string2 = this.msg("countdown.player-ready", "player", string);
        Component component = this.readyBar(activeDuel);
        Player player2 = Bukkit.getPlayer((UUID)activeDuel.getPlayer1());
        Player player3 = Bukkit.getPlayer((UUID)activeDuel.getPlayer2());
        if (player2 != null) {
            player2.sendMessage(string2);
            player2.sendActionBar(component);
            Sounds.ready(player2);
        }
        if (player3 != null) {
            player3.sendMessage(string2);
            player3.sendActionBar(component);
            Sounds.ready(player3);
        }
        if (activeDuel.getReadyCount() >= 2) {
            this.finishCountdown(activeDuel, true);
        }
    }

    private Component readyBar(ActiveDuel activeDuel) {
        return DuelManager.mm("<yellow>◎ Sneak to skip countdown " + activeDuel.getReadyCount() + "</yellow><gray>/</gray><white>2</white>");
    }

    private static Component mm(String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }

    public void handleRoundLoss(UUID uUID, Location location) {
        Object object;
        ActiveDuel activeDuel = this.playerDuels.get(uUID);
        if (activeDuel == null || activeDuel.getState() != ActiveDuel.State.FIGHTING) {
            return;
        }
        activeDuel.setState(ActiveDuel.State.ENDING);
        UUID uUID2 = activeDuel.getOpponent(uUID);
        Player player = Bukkit.getPlayer((UUID)uUID);
        Player player2 = Bukkit.getPlayer((UUID)uUID2);
        boolean bl = activeDuel.awardRound(uUID2);
        this.plugin.getStatsManager().addWin(uUID2);
        this.plugin.getStatsManager().resetStreak(uUID);
        this.plugin.getStatsManager().save();
        String string = player2 != null ? player2.getName() : "A player";
        String string2 = player != null ? player.getName() : "A player";
        // Round result titles and messages below replace the generic death/defeat
        // announcement, which otherwise obscures the round score for players.
        object = String.valueOf(activeDuel.getCurrentRound());
        if (player2 != null) {
            this.sendTitle(player2, this.msg("titles.round-won.title", new String[0]), this.msg("titles.round-won.subtitle", "yourScore", String.valueOf(activeDuel.getScoreFor(uUID2)), "theirScore", String.valueOf(activeDuel.getScoreAgainst(uUID2))));
            player2.sendMessage(this.msg("round.won-chat", new String[]{"round", String.valueOf(object), "score", String.valueOf(activeDuel.getScoreFor(uUID2)), "opponent_score", String.valueOf(activeDuel.getScoreAgainst(uUID2))}));
        }
        if (player != null) {
            this.sendTitle(player, this.msg("titles.round-lost.title", new String[0]), this.msg("titles.round-lost.subtitle", "yourScore", String.valueOf(activeDuel.getScoreFor(uUID)), "theirScore", String.valueOf(activeDuel.getScoreAgainst(uUID))));
            player.sendMessage(this.msg("round.lost-chat", new String[]{"round", String.valueOf(object), "score", String.valueOf(activeDuel.getScoreFor(uUID)), "opponent_score", String.valueOf(activeDuel.getScoreAgainst(uUID))}));
        }
        if (bl) {
            object = activeDuel.getKit() == null ? "Unknown" : activeDuel.getKit();
            this.sendResultToParticipants(activeDuel, this.msg("duel.match-result", new String[]{"killer", string, "victim", string2, "winner", string, "kit", String.valueOf(object), "score", activeDuel.getScoreFor(uUID2) + " - " + activeDuel.getScoreFor(uUID)}));
        }
        if (bl) {
            // The match-end launch is intentionally free of chat and title overlays.
            if (player2 != null) {
                player2.getInventory().clear();
            }
            UUID winner = uUID2;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.finishRoundLoss(activeDuel, winner), this.winEndTicks);
        } else {
            Arena roundArena = activeDuel.getArena();
            Location center = this.arenaTransitionCenter(roundArena);
            if (roundArena != null && center != null) {
                Player teamOne = Bukkit.getPlayer(activeDuel.getPlayer1());
                Player teamTwo = Bukkit.getPlayer(activeDuel.getPlayer2());
                long flightTicks = 0L;
                if (teamOne != null) flightTicks = Math.max(flightTicks, this.launchPlayerToTarget(teamOne, roundArena.getSpawn1()));
                if (teamTwo != null) flightTicks = Math.max(flightTicks, this.launchPlayerToTarget(teamTwo, roundArena.getSpawn2()));
                long nextRoundDelay = Math.max(Math.max(this.deathSpectateTicks, 22L), flightTicks + 2L);
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    if (this.isStill(activeDuel)) this.startRound(activeDuel);
                }, nextRoundDelay);
            }
            activeDuel.nextRound();
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.regenArena(activeDuel, false), 2L);
            if (roundArena == null || this.arenaTransitionCenter(roundArena) == null) {
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    if (this.isStill(activeDuel)) this.startRound(activeDuel);
                }, Math.max(this.deathSpectateTicks, 22L));
            }
        }
    }

    /** Preserve the death position until Bukkit fires PlayerRespawnEvent. */
    public void setPendingRoundRespawn(UUID playerId, Location location) {
        if (playerId != null && location != null && location.getWorld() != null) {
            this.pendingRoundRespawns.put(playerId, location.clone());
        }
    }

    /** Consume the saved death position once the player's duel respawn is processed. */
    public Location consumePendingRoundRespawn(UUID playerId) {
        Location location = playerId == null ? null : this.pendingRoundRespawns.remove(playerId);
        return location == null ? null : location.clone();
    }

    /**
     * Regenerates a duel's arena, between rounds or after the match.
     *
     * <p>The configured regen delay now applies after the match only. Between
     * rounds it could outlast the death-spectate pause, which meant the
     * "previous" round's regen fired after the next round had started and
     * deleted the blocks players had just placed.
     */
    private void regenArena(ActiveDuel activeDuel, boolean matchOver) {
        Arena arena = activeDuel.getArena();
        if (arena == null || !arena.isAutoRegenerate()) {
            return;
        }
        if (matchOver && arena.getRegenDelayTicks() > 0) {
            // Held from now, not from when the delay runs out: otherwise the
            // arena is free during the delay, a new match is put there, and the
            // delayed regen then fires in the middle of that match.
            this.plugin.getArenaManager().holdForRegen(arena);
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.regenNow(activeDuel, true), (long)arena.getRegenDelayTicks());
            return;
        }
        this.regenNow(activeDuel, matchOver);
    }

    private void regenNow(ActiveDuel activeDuel, boolean matchOver) {
        Arena arena = activeDuel.getArena();
        if (arena == null || !arena.isAutoRegenerate()) {
            return;
        }
        if (!matchOver && activeDuel.isFinished()) {
            // The round-end regen is scheduled two ticks out; if the match
            // ended in between, the match-end regen already owns the arena and
            // a round pass here would cancel it.
            return;
        }
        HashMap<Location, BlockData> changed = new HashMap<Location, BlockData>(activeDuel.getChangedBlocks());
        activeDuel.getChangedBlocks().clear();
        if (matchOver) {
            this.plugin.getArenaManager().regenAfterMatch(arena, changed, null);
        } else {
            this.plugin.getArenaManager().regenBetweenRounds(arena, changed);
        }
        this.clearArenaEntities(arena);
    }

    private void clearArenaEntities(Arena arena) {
        World world = arena.getWorld();
        if (world == null || arena.getMin() == null || arena.getMax() == null) {
            return;
        }
        Location location = arena.getMin();
        Location location2 = arena.getMax();
        Location location3 = new Location(world, (double)(location.getBlockX() + location2.getBlockX()) / 2.0 + 0.5, (double)(location.getBlockY() + location2.getBlockY()) / 2.0 + 0.5, (double)(location.getBlockZ() + location2.getBlockZ()) / 2.0 + 0.5);
        double d = (double)(location2.getBlockX() - location.getBlockX()) / 2.0 + 2.0;
        double d2 = (double)(location2.getBlockY() - location.getBlockY()) / 2.0 + 2.0;
        double d3 = (double)(location2.getBlockZ() - location.getBlockZ()) / 2.0 + 2.0;
        for (Entity entity : world.getNearbyEntities(location3, d, d2, d3)) {
            if (!(entity instanceof Projectile) && !(entity instanceof Item) && !(entity instanceof TNTPrimed) && !(entity instanceof ExperienceOrb)) continue;
            entity.remove();
        }
    }

    private boolean isStill(ActiveDuel activeDuel) {
        return !activeDuel.isFinished() && (this.playerDuels.get(activeDuel.getPlayer1()) == activeDuel || this.playerDuels.get(activeDuel.getPlayer2()) == activeDuel);
    }

    private void endMatch(ActiveDuel activeDuel, UUID uUID, EndReason endReason) {
        Player player;
        Object object;
        if (activeDuel.isFinished()) {
            return;
        }
        if (endReason != EndReason.ROUND_WIN) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), "minecraft:block.beacon.deactivate", org.bukkit.SoundCategory.PLAYERS, 1.0f, 1.6f);
            }
        }
        activeDuel.setFinished(true);
        activeDuel.setState(ActiveDuel.State.ENDING);
        this.regenArena(activeDuel, true);
        // Restore inventories and modes in the arena first; finishEndMatch runs
        // the flight and then returns both players to the configured duel spawn.
        this.restorePlayer(activeDuel.getPlayer1(), false);
        this.restorePlayer(activeDuel.getPlayer2(), false);
        this.giveSpawnItemsTo(activeDuel.getPlayer1());
        this.giveSpawnItemsTo(activeDuel.getPlayer2());
        this.lastOpponent.put(activeDuel.getPlayer1(), activeDuel.getPlayer2());
        this.lastOpponent.put(activeDuel.getPlayer2(), activeDuel.getPlayer1());
        this.addRematchItem(activeDuel.getPlayer1());
        this.addRematchItem(activeDuel.getPlayer2());
        UUID endedPlayer = uUID;
        ActiveDuel endedDuel = activeDuel;
        EndReason endedReason = endReason;
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.finishEndMatch(endedDuel, endedPlayer, endedReason), 2L);
        this.plugin.getTabService().detach(activeDuel.getPlayer1());
        this.plugin.getTabService().detach(activeDuel.getPlayer2());
        this.plugin.getScoreboardService().detach(activeDuel.getPlayer1());
        this.plugin.getScoreboardService().detach(activeDuel.getPlayer2());
        if (activeDuel.getArena().isGlowingOpponent()) {
            object = Bukkit.getPlayer((UUID)activeDuel.getPlayer1());
            player = Bukkit.getPlayer((UUID)activeDuel.getPlayer2());
            if (object != null) {
                try {
                    ((Player)object).setGlowing(false);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            if (player != null) {
                try {
                    player.setGlowing(false);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        if (activeDuel.getArena() != null) {
            this.arenasInUse.remove(activeDuel.getArena().getName().toLowerCase());
        }
        this.playerDuels.remove(activeDuel.getPlayer1());
        this.playerDuels.remove(activeDuel.getPlayer2());
        object = activeDuel.getOpponent(uUID);
        this.plugin.getStatsManager().addDuelWin(uUID);
        this.plugin.getStatsManager().addDuelLoss((UUID)object);
        this.plugin.getStatsManager().save();
        this.plugin.getStatsManager().addPlayed(uUID);
        this.plugin.getStatsManager().addPlayed((UUID)object);
    }

    private static void ground(Player player) {
        if (player == null) {
            return;
        }
        try {
            if (player.isGliding()) {
                player.setGliding(false);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        player.setFallDistance(0.0f);
    }

    private void sendResultToParticipants(ActiveDuel duel, String message) {
        Player first = Bukkit.getPlayer(duel.getPlayer1());
        Player second = Bukkit.getPlayer(duel.getPlayer2());
        if (first != null) {
            first.sendMessage(message);
        }
        if (second != null && second != first) {
            second.sendMessage(message);
        }
    }

    private void restorePlayer(UUID uUID, boolean bl) {
        GameModeGuard.release(uUID);
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            AntiCheatBypass.release(this.plugin, player);
            try {
                player.removeScoreboardTag("meowduel");
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            try {
                player.setWorldBorder(null);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        PlayerSnapshot playerSnapshot = this.snapshots.remove(uUID);
        if (player != null && playerSnapshot != null) {
            playerSnapshot.restore(player);
            DuelManager.ground(player);
        }
        if (player != null && bl) {
            Location location = this.plugin.getDuelSpawn();
            if (location == null || location.getWorld() == null) {
                // A duel snapshot is the arena location. Falling back to it
                // leaves players inside the match, so use a server world spawn.
                World world = player.getWorld();
                location = world == null ? null : world.getSpawnLocation();
            }
            if (location != null && location.getWorld() != null) {
                player.teleport(location);
            }
        }
    }

    public void handleDisconnect(UUID uUID) {
        ActiveDuel activeDuel = this.playerDuels.get(uUID);
        if (activeDuel == null) {
            return;
        }
        UUID uUID2 = activeDuel.getMatchWinner();
        UUID uUID3 = uUID2 != null ? uUID2 : activeDuel.getOpponent(uUID);
        this.restorePlayer(uUID, true);
        this.plugin.getScoreboardService().detach(uUID);
        this.awardRemainingRounds(activeDuel, uUID3);
        this.endMatch(activeDuel, uUID3, EndReason.DISCONNECT);
    }

    public boolean isInDuel(UUID uUID) {
        return this.playerDuels.containsKey(uUID);
    }

    public boolean isArenaInUse(String string) {
        return this.arenasInUse.contains(string.toLowerCase()) || this.plugin.getArenaManager().isRegenerating(string);
    }

    public void markArenaInUse(String string) {
        this.arenasInUse.add(string.toLowerCase());
        this.plugin.getArenaManager().markDirty(string);
    }

    public void freeArena(String string) {
        this.arenasInUse.remove(string.toLowerCase());
    }

    public void tickArenaReservations() {
        this.tickAbandonedDuels();
        if (this.arenasInUse.isEmpty()) {
            return;
        }
        HashSet<String> hashSet = new HashSet<String>();
        for (ActiveDuel object2 : this.playerDuels.values()) {
            if (object2 == null || object2.isFinished() || object2.getArena() == null) continue;
            hashSet.add(object2.getArena().getName().toLowerCase());
        }
        Arena arena = this.plugin.getEventManager().getArena();
        if (arena != null) {
            hashSet.add(arena.getName().toLowerCase());
        }
        hashSet.addAll(this.plugin.getPartyManager().claimedArenas());
        Iterator<String> iterator = this.arenasInUse.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            if (hashSet.contains(string)) continue;
            iterator.remove();
            this.plugin.getLogger().info("Released arena '" + string + "' - it was still reserved but no duel or event is using it.");
        }
    }

    public void explainNoArena(Player player, String string) {
        if (player != null && player.hasPermission("meowduels.admin")) {
            player.sendMessage(this.plugin.messages().get("notice.duel.message", "value", String.valueOf(this.arenaAvailability(string))));
        }
    }

    public String arenaAvailability(String string) {
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        int n5 = 0;
        for (Arena arena : this.plugin.getArenaManager().all()) {
            ++n;
            if (!arena.isConfigured()) continue;
            ++n2;
            if (!arena.isEnabled()) continue;
            ++n3;
            if (!arena.supportsKit(string)) continue;
            ++n4;
            if (this.isArenaInUse(arena.getName())) continue;
            ++n5;
        }
        return n + " arena(s): " + n2 + " configured, " + n3 + " enabled, " + n4 + " support this kit, " + n5 + " free.";
    }

    public void tickCounts() {
        if (this.playerDuels.isEmpty()) {
            this.fightingPlayers = 0;
            this.fightingMatches = 0;
            return;
        }
        Set set = Collections.newSetFromMap(new IdentityHashMap());
        Iterator<Map.Entry<UUID, ActiveDuel>> iterator = this.playerDuels.entrySet().iterator();
        int n = 0;
        while (iterator.hasNext()) {
            Map.Entry<UUID, ActiveDuel> entry = iterator.next();
            ActiveDuel activeDuel = entry.getValue();
            if (activeDuel == null || activeDuel.isFinished() || Bukkit.getPlayer((UUID)entry.getKey()) == null) {
                iterator.remove();
                continue;
            }
            ++n;
            set.add(activeDuel);
        }
        this.fightingPlayers = n;
        this.fightingMatches = set.size();
    }

    public int playersInDuels() {
        return this.fightingPlayers;
    }

    public int duelsInProgress() {
        return this.fightingMatches;
    }

    public ActiveDuel getDuel(UUID uUID) {
        return this.playerDuels.get(uUID);
    }

    public boolean isArenaBusy(Arena arena) {
        if (arena == null) {
            return true;
        }
        if (this.isArenaInUse(arena.getName())) {
            return true;
        }
        for (ActiveDuel activeDuel : this.playerDuels.values()) {
            if (activeDuel.getArena() == null || activeDuel.getArena() == arena || !arena.overlaps(activeDuel.getArena())) continue;
            return true;
        }
        Arena arena2 = this.plugin.getEventManager().getArena();
        return arena2 != null && this.plugin.getEventManager().isRunning() && arena.overlaps(arena2);
    }

    private void giveSpawnItemsTo(UUID uUID) {
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            this.plugin.giveSpawnItems(player);
        }
    }

    private static String mmc(String string) {
        return Colors.toSection(string);
    }

    private void addRematchItem(UUID uUID) {
        Player player = Bukkit.getPlayer((UUID)uUID);
        UUID uUID2 = this.lastOpponent.get(uUID);
        if (player != null && uUID2 != null) {
            SpawnItems.addRematch(this.plugin, player, uUID2, this.rematchName(uUID2));
        }
    }

    private String rematchName(UUID uUID) {
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            return player.getName();
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((UUID)uUID);
        return offlinePlayer.getName() == null ? "Opponent" : offlinePlayer.getName();
    }

    private void openSummary(ActiveDuel activeDuel, UUID uUID, UUID uUID2) {
        Player player = Bukkit.getPlayer((UUID)uUID2);
        if (player != null) {
            try {
                new MatchSummaryMenu(this.plugin, activeDuel, uUID, uUID2).open(player);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public void requestRematch(Player player) {
        UUID uUID = this.lastOpponent.get(player.getUniqueId());
        if (uUID == null) {
            player.sendMessage(this.plugin.messages().get("notice.duel.no-recent-opponent-rematch", new String[0]));
            return;
        }
        Player player2 = Bukkit.getPlayer((UUID)uUID);
        if (player2 == null || !player2.isOnline()) {
            player.sendMessage(this.plugin.messages().get("notice.duel.player-no-longer-online", new String[0]));
            return;
        }
        if (this.isInDuel(player2.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.duel.they-re-already-duel", new String[0]));
            return;
        }
        if (this.isInDuel(player.getUniqueId())) {
            player.sendMessage(this.msg("duel.already-in-duel", new String[0]));
            return;
        }
        if (this.plugin.getPartyManager().inParty(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.duel.re-party-leave-first", new String[0]));
            return;
        }
        if (this.plugin.getEventManager().isInvolved(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.duel.t-duel-while-re", new String[0]));
            return;
        }
        new KitPickMenu(this.plugin, new DuelConfirmMenu(this.plugin, player, player2)).open(player);
    }

    private void applyDuelBorder(Player player, Arena arena) {
        if (player == null) {
            return;
        }
        if (arena == null || arena.getMin() == null || arena.getMax() == null) {
            return;
        }
        try {
            Location location = arena.getMin();
            Location location2 = arena.getMax();
            double d = (double)(location.getBlockX() + location2.getBlockX()) / 2.0 + 0.5;
            double d2 = (double)(location.getBlockZ() + location2.getBlockZ()) / 2.0 + 0.5;
            double d3 = Math.max(location2.getBlockX() - location.getBlockX() + 1, location2.getBlockZ() - location.getBlockZ() + 1);
            WorldBorder worldBorder = Bukkit.createWorldBorder();
            worldBorder.setCenter(d, d2);
            worldBorder.setWarningDistance(0);
            worldBorder.setWarningTime(0);
            worldBorder.setSize(Math.max(1.0, d3));
            player.setWorldBorder(worldBorder);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public boolean hasFreeArenaFor(String string) {
        return this.plugin.getArenaManager().findFreeArena(arena -> this.isArenaInUse(arena.getName()) || !arena.supportsKit(string)) != null;
    }

    public boolean hasUsableArena(String string) {
        return this.plugin.getArenaManager().findFreeArena(arena -> this.isArenaInUse(arena.getName()) || !arena.supportsKit(string)) != null;
    }

    public void tickArenaSafety() {
        if (this.playerDuels.isEmpty()) {
            return;
        }
        ArrayList<ActiveDuel> arrayList = new ArrayList<ActiveDuel>();
        Set set = Collections.newSetFromMap(new IdentityHashMap());
        for (ActiveDuel activeDuel : this.playerDuels.values()) {
            if (!set.add(activeDuel)) continue;
            arrayList.add(activeDuel);
        }
        Arena arena = this.plugin.getEventManager().getArena();
        boolean bl = arena != null && this.plugin.getEventManager().isRunning();
        for (int i = 0; i < arrayList.size(); ++i) {
            ActiveDuel activeDuel = (ActiveDuel)arrayList.get(i);
            if (!this.playerDuels.containsKey(activeDuel.getPlayer1())) continue;
            if (bl && activeDuel.getArena() != null && activeDuel.getArena().overlaps(arena)) {
                this.forceCancelBooked(activeDuel);
                continue;
            }
            for (int j = i + 1; j < arrayList.size(); ++j) {
                ActiveDuel activeDuel2 = (ActiveDuel)arrayList.get(j);
                if (!this.playerDuels.containsKey(activeDuel2.getPlayer1()) || activeDuel.getArena() == null || activeDuel2.getArena() == null || activeDuel.getArena() != activeDuel2.getArena() && !activeDuel.getArena().overlaps(activeDuel2.getArena())) continue;
                this.forceCancelBooked(activeDuel2.getGameNumber() >= activeDuel.getGameNumber() ? activeDuel2 : activeDuel);
            }
        }
    }

    private void forceCancelBooked(ActiveDuel activeDuel) {
        this.forceCancelBooked(activeDuel, this.msg("duel.arena-booked", new String[0]));
    }

    private void forceCancelBooked(ActiveDuel activeDuel, String string) {
        activeDuel.setFinished(true);
        activeDuel.setState(ActiveDuel.State.ENDING);
        Player player = Bukkit.getPlayer((UUID)activeDuel.getPlayer1());
        Player player2 = Bukkit.getPlayer((UUID)activeDuel.getPlayer2());
        this.regenArena(activeDuel, true);
        if (player != null) {
            player.setGlowing(false);
        }
        if (player2 != null) {
            player2.setGlowing(false);
        }
        this.restorePlayer(activeDuel.getPlayer1(), true);
        this.restorePlayer(activeDuel.getPlayer2(), true);
        this.giveSpawnItemsTo(activeDuel.getPlayer1());
        this.giveSpawnItemsTo(activeDuel.getPlayer2());
        this.plugin.getTabService().detach(activeDuel.getPlayer1());
        this.plugin.getTabService().detach(activeDuel.getPlayer2());
        this.plugin.getScoreboardService().detach(activeDuel.getPlayer1());
        this.plugin.getScoreboardService().detach(activeDuel.getPlayer2());
        if (activeDuel.getArena() != null) {
            this.arenasInUse.remove(activeDuel.getArena().getName().toLowerCase());
        }
        this.playerDuels.remove(activeDuel.getPlayer1());
        this.playerDuels.remove(activeDuel.getPlayer2());
        if (player != null) {
            player.sendMessage(string);
        }
        if (player2 != null) {
            player2.sendMessage(string);
        }
    }

    public void tickWorldLocks() {
        Set<World> worlds = Collections.newSetFromMap(new IdentityHashMap<World, Boolean>());
        for (Arena arena : this.plugin.getArenaManager().all()) {
            World world = arena == null ? null : arena.getWorld();
            if (world != null && worlds.add(world)) {
                DuelManager.applyWorldLocks(arena);
            }
        }
        for (ActiveDuel activeDuel : this.playerDuels.values()) {
            Arena arena = activeDuel.getArena();
            World world = arena == null ? null : arena.getWorld();
            if (world != null && worlds.add(world)) {
                DuelManager.applyWorldLocks(arena);
            }
        }
    }

    /**
     * Keeps an arena world clear and at noon.
     *
     * <p>Runs every second for every busy arena. setTime sends a time packet
     * to every player in the world, so with many arenas in one world this was
     * dozens of packets per player per second for a value nobody could see
     * change. Now each call only writes what has actually drifted: weather
     * when it is not clear, the clock when it has moved off noon.
     */
    public static void applyWorldLocks(Arena arena) {
        if (arena == null || arena.getWorld() == null) {
            return;
        }
        World world = arena.getWorld();
        try {
            if (arena.isLockClearWeather()) {
                if (world.hasStorm()) {
                    world.setStorm(false);
                }
                if (world.isThundering()) {
                    world.setThundering(false);
                }
                if (world.getWeatherDuration() < 1_000_000) {
                    world.setWeatherDuration(Integer.MAX_VALUE);
                }
            }
            if (world.getGameRuleValue(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE)) {
                world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
            }
            if (Math.abs(world.getTime() - 6000L) > 200L) {
                world.setTime(6000L);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void applyPlayerTrims(Player player, String string) {
        ItemStack[] itemStackArray;
        for (ItemStack itemStack : itemStackArray = player.getInventory().getArmorContents()) {
            Trims.clear(itemStack);
        }
        this.plugin.getTrimPreferences().applyToArmor(player.getUniqueId(), string, itemStackArray);
        player.getInventory().setArmorContents(itemStackArray);
        player.updateInventory();
    }

    public int fightsWithKit(String string) {
        Set set = Collections.newSetFromMap(new IdentityHashMap());
        int n = 0;
        for (ActiveDuel activeDuel : this.playerDuels.values()) {
            if (!set.add(activeDuel) || !activeDuel.getKit().equalsIgnoreCase(string)) continue;
            ++n;
        }
        return n;
    }

    public boolean startQueuedDuel(Player player, Player player2, String string) {
        if (this.isInDuel(player.getUniqueId()) || this.isInDuel(player2.getUniqueId())) {
            return false;
        }
        Arena arena2 = this.plugin.getArenaManager().findFreeArena(arena -> this.isArenaInUse(arena.getName()) || !arena.supportsKit(string));
        if (arena2 == null) {
            return false;
        }
        this.startDuel(player, player2, arena2, string, 2);
        return this.isInDuel(player.getUniqueId()) && this.isInDuel(player2.getUniqueId());
    }

    /** Carries a duelist through a smooth, collision-free arc to their next-round spawn. */
    private long launchPlayerToTarget(Player player, Location target) {
        if (player == null || !player.isOnline() || target == null || target.getWorld() == null) return 0L;
        UUID id = player.getUniqueId();
        Location start = player.getLocation().clone();
        if (start.getWorld() == null || !start.getWorld().equals(target.getWorld())) {
            player.teleport(target);
            return 0L;
        }
        if (start.distanceSquared(target) < 0.25) {
            player.teleport(target);
            return 0L;
        }

        // A previous transition is already driving this player. Let it finish
        // instead of stacking teleports or changing the saved player flags.
        if (this.roundLaunchTasks.containsKey(id)) return 40L;
        ROUND_LAUNCH_FALL_IMMUNITY.add(id);

        double safeLift = Math.max(0.0, start.getWorld().getMaxHeight() - Math.max(start.getY(), target.getY()) - 2.0);
        double lift = Math.min(5.0, safeLift);
        Location control = start.clone().add(target).multiply(0.5);
        control.setY(Math.max(start.getY(), target.getY()) + 2.0 * lift);
        double pathLengthEstimate = start.distance(control) + control.distance(target);
        final int targetFlightTicks = 80; // About four seconds on ordinary arenas.
        final double maxStepPerTick = Math.min(0.8, Math.max(0.45, pathLengthEstimate / targetFlightTicks));
        final int flightTicks = Math.max(targetFlightTicks, (int)Math.ceil(pathLengthEstimate / maxStepPerTick));

        boolean originallyInvulnerable = player.isInvulnerable();
        boolean originallyNoPhysics = player.hasNoPhysics();
        boolean originallyGravity = player.hasGravity();
        boolean originallyAllowFlight = player.getAllowFlight();
        boolean originallyFlying = player.isFlying();
        player.setInvulnerable(true);
        player.setFlying(false);
        // Prevent the server's flight detector from kicking the player while
        // gravity and block collision are deliberately disabled for this arc.
        player.setAllowFlight(true);
        player.setNoPhysics(true);
        player.setGravity(false);
        player.setFallDistance(0.0f);
        player.setVelocity(new Vector());
        player.playSound(start, "minecraft:block.conduit.deactivate", SoundCategory.PLAYERS, 1.0f, 1.0f);

        final int[] elapsed = {0};
        final BukkitTask[] scheduledTask = {null};
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, (Runnable)() -> {
            if (!player.isOnline()) {
                scheduledTask[0].cancel();
                this.roundLaunchTasks.remove(id);
                ROUND_LAUNCH_FALL_IMMUNITY.remove(id);
                return;
            }
            if (player.isFlying()) player.setFlying(false);
            elapsed[0]++;
            double progress = Math.min(1.0, (double)elapsed[0] / flightTicks);
            double inverse = 1.0 - progress;
            double x = inverse * inverse * start.getX() + 2.0 * inverse * progress * control.getX() + progress * progress * target.getX();
            double y = inverse * inverse * start.getY() + 2.0 * inverse * progress * control.getY() + progress * progress * target.getY();
            double z = inverse * inverse * start.getZ() + 2.0 * inverse * progress * control.getZ() + progress * progress * target.getZ();
            Location current = player.getLocation();
            Location next = new Location(start.getWorld(), x, y, z, current.getYaw(), current.getPitch());
            Vector steeringVelocity = new Vector(x - current.getX(), y - current.getY(), z - current.getZ());
            if (steeringVelocity.length() > maxStepPerTick) steeringVelocity.normalize().multiply(maxStepPerTick);
            // Let normal movement packets carry the player smoothly through
            // open air. When a solid block would trap them, step over that
            // collision with a short plugin teleport instead.
            if (this.insideSolidBlock(current) || this.insideSolidBlock(next)) {
                player.teleport(next);
                player.setVelocity(new Vector());
            } else {
                player.setVelocity(steeringVelocity);
            }
            player.setFallDistance(0.0f);

            if (elapsed[0] >= flightTicks) {
                Location arrival = target.clone();
                arrival.setYaw(next.getYaw());
                arrival.setPitch(next.getPitch());
                player.teleport(arrival);
                scheduledTask[0].cancel();
                this.roundLaunchTasks.remove(id);
                ROUND_LAUNCH_FALL_IMMUNITY.remove(id);
                this.restoreRoundLaunch(player, id, scheduledTask[0], originallyNoPhysics, originallyGravity, originallyInvulnerable, originallyAllowFlight, originallyFlying);
            }
        }, 1L, 1L);
        scheduledTask[0] = task;
        this.roundLaunchTasks.put(id, task);
        return flightTicks;
    }

    private boolean insideSolidBlock(Location feet) {
        if (feet == null || feet.getWorld() == null) return false;
        int minX = (int)Math.floor(feet.getX() - 0.3);
        int maxX = (int)Math.floor(feet.getX() + 0.3);
        int minY = (int)Math.floor(feet.getY() + 0.05);
        int maxY = (int)Math.floor(feet.getY() + 1.75);
        int minZ = (int)Math.floor(feet.getZ() - 0.3);
        int maxZ = (int)Math.floor(feet.getZ() + 0.3);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (feet.getWorld().getBlockAt(x, y, z).getType().isSolid()) return true;
                }
            }
        }
        return false;
    }

    private void restoreRoundLaunch(Player player, UUID id, BukkitTask task, boolean noPhysics, boolean gravity,
                                    boolean invulnerable, boolean allowFlight, boolean flying) {
        if (task != null) task.cancel();
        ROUND_LAUNCH_FALL_IMMUNITY.remove(id);
        if (player == null || !player.isOnline()) return;
        player.setVelocity(new Vector());
        player.setNoPhysics(noPhysics);
        player.setGravity(gravity);
        player.setInvulnerable(invulnerable);
        player.setAllowFlight(allowFlight);
        if (flying && allowFlight) player.setFlying(true);
        player.setFallDistance(0.0f);
        player.setNoDamageTicks(0);
    }

    public static boolean cancelRoundLaunchFallDamage(EntityDamageEvent event) {
        if (event == null || event.getCause() != EntityDamageEvent.DamageCause.FALL
            || !(event.getEntity() instanceof Player player)) return false;
        if (!ROUND_LAUNCH_FALL_IMMUNITY.remove(player.getUniqueId())) return false;
        event.setCancelled(true);
        return true;
    }

    public void shutdown() {
        for (ActiveDuel object : new HashSet<ActiveDuel>(this.playerDuels.values())) {
            if (object == null || object.getArena() == null) continue;
            try {
                this.regenNow(object, true);
            }
            catch (Throwable throwable) {
                this.plugin.getLogger().warning("Failed to regenerate arena '" + object.getArena().getName() + "' during shutdown: " + String.valueOf(throwable));
            }
        }
        for (UUID uUID : new HashSet<UUID>(this.snapshots.keySet())) {
            this.restorePlayer(uUID, true);
            this.plugin.getScoreboardService().detach(uUID);
        }
        this.playerDuels.clear();
        this.arenasInUse.clear();
        this.requests.clear();
    }

    private void tickAbandonedDuels() {
        if (this.playerDuels.isEmpty()) {
            return;
        }
        for (ActiveDuel activeDuel : new HashSet<ActiveDuel>(this.playerDuels.values())) {
            if (activeDuel == null || activeDuel.isFinished() || Bukkit.getPlayer((UUID)activeDuel.getPlayer1()) != null || Bukkit.getPlayer((UUID)activeDuel.getPlayer2()) != null) continue;
            this.plugin.getLogger().info("Ending duel in arena '" + (activeDuel.getArena() == null ? "?" : activeDuel.getArena().getName()) + "' - both players are offline.");
            try {
                this.endMatch(activeDuel, activeDuel.getPlayer1(), EndReason.DISCONNECT);
            }
            catch (Throwable throwable) {
                this.plugin.getLogger().warning("Abandoned-duel teardown threw: " + String.valueOf(throwable));
                activeDuel.setFinished(true);
                this.playerDuels.remove(activeDuel.getPlayer1());
                this.playerDuels.remove(activeDuel.getPlayer2());
                if (activeDuel.getArena() == null) continue;
                this.arenasInUse.remove(activeDuel.getArena().getName().toLowerCase());
                this.regenNow(activeDuel, true);
            }
        }
    }

    private void sendTitle(Player player, String string, String string2) {
        this.sendTitle(player, string, string2, 5, 30, 10);
    }

    private void sendTitle(Player player, String string, String string2, int n, int n2, int n3) {
        Title.Times times = Title.Times.times(Duration.ofMillis(n * 50L), Duration.ofMillis(n2 * 50L), Duration.ofMillis(n3 * 50L));
        player.showTitle(Title.title(MiniMessageUtil.deserialize(string), MiniMessageUtil.deserialize(string2), times));
    }

    public void leave(Player player) {
        UUID uUID = player.getUniqueId();
        ActiveDuel activeDuel = this.playerDuels.get(uUID);
        if (activeDuel == null) {
            player.sendMessage(this.msg("leave.not-in-duel", new String[0]));
            return;
        }
        UUID uUID2 = activeDuel.getMatchWinner();
        UUID uUID3 = uUID2 != null ? uUID2 : activeDuel.getOpponent(uUID);
        activeDuel.setState(ActiveDuel.State.ENDING);
        this.awardRemainingRounds(activeDuel, uUID3);
        this.endMatch(activeDuel, uUID3, uUID2 != null ? EndReason.ROUND_WIN : EndReason.LEAVE);
    }

    private void awardRemainingRounds(ActiveDuel activeDuel, UUID uUID) {
        for (int i = 0; i < 64 && activeDuel.getMatchWinner() == null; ++i) {
            if (!activeDuel.awardRound(uUID)) continue;
            return;
        }
    }

    private void finishEndMatch(ActiveDuel activeDuel, UUID uUID, EndReason endReason) {
        Arena arena = activeDuel.getArena();
        Location center = this.arenaTransitionCenter(arena);
        Location configuredSpawn = this.plugin.getDuelSpawn();
        Player firstPlayer = Bukkit.getPlayer(activeDuel.getPlayer1());
        Player secondPlayer = Bukkit.getPlayer(activeDuel.getPlayer2());
        Location firstSpawn = configuredSpawn != null && configuredSpawn.getWorld() != null
            ? configuredSpawn.clone() : firstPlayer == null ? null : firstPlayer.getWorld().getSpawnLocation();
        Location secondSpawn = configuredSpawn != null && configuredSpawn.getWorld() != null
            ? configuredSpawn.clone() : secondPlayer == null ? null : secondPlayer.getWorld().getSpawnLocation();
        double flightCeiling = this.arenaTransitionCeiling(arena);
        java.util.concurrent.atomic.AtomicInteger pendingReturns = new java.util.concurrent.atomic.AtomicInteger(2);
        Runnable completed = () -> {
            if (pendingReturns.decrementAndGet() != 0) return;
            this.sendEndMessages(activeDuel, uUID, endReason);
            this.openSummary(activeDuel, uUID, activeDuel.getPlayer1());
            this.openSummary(activeDuel, uUID, activeDuel.getPlayer2());
        };
        this.returnToSpawn(activeDuel.getPlayer1(), center, firstSpawn, flightCeiling, completed);
        this.returnToSpawn(activeDuel.getPlayer2(), center, secondSpawn, flightCeiling, completed);
    }

    private Location arenaTransitionCenter(Arena arena) {
        if (arena == null || arena.getSpawn1() == null || arena.getSpawn2() == null) return null;
        Location first = arena.getSpawn1();
        Location second = arena.getSpawn2();
        if (first.getWorld() == null || !first.getWorld().equals(second.getWorld())) return null;
        double x = (first.getX() + second.getX()) / 2.0;
        double z = (first.getZ() + second.getZ()) / 2.0;
        double y = Math.max(first.getY(), second.getY()) + 2.0;
        y = Math.min(y, first.getWorld().getMaxHeight() - 2.0);
        float yaw = first.getYaw();
        return new Location(first.getWorld(), x, y, z, yaw, 0.0f);
    }

    private double arenaTransitionCeiling(Arena arena) {
        Location max = arena == null ? null : arena.getMax();
        Location first = arena == null ? null : arena.getSpawn1();
        Location second = arena == null ? null : arena.getSpawn2();
        double fallback = first == null || second == null ? 0.0 : Math.max(first.getY(), second.getY()) + 5.0;
        return max == null ? fallback : max.getY() + 1.0;
    }

    private void sendEndMessages(ActiveDuel duel, UUID winnerId, EndReason reason) {
        UUID loserId = duel.getOpponent(winnerId);
        Player winner = Bukkit.getPlayer(winnerId);
        Player loser = Bukkit.getPlayer(loserId);
        String winnerName = winner == null ? "A player" : winner.getName();
        String loserName = loser == null ? "A player" : loser.getName();
        String score = duel.getScoreFor(winnerId) + " - " + duel.getScoreAgainst(winnerId);
        if (reason == EndReason.LEAVE) {
            if (winner != null) {
                winner.sendMessage(this.msg("duel.victory-opponent-left", new String[0]));
                Sounds.victory(winner);
                winner.sendMessage(this.msg("duel.forfeit-broadcast", new String[0]));
            }
            if (loser != null) {
                loser.sendMessage(this.msg("duel.left-confirm", new String[0]));
                loser.sendMessage(this.msg("duel.forfeit-broadcast", new String[0]));
            }
            this.sendResultToParticipants(duel, this.msg("duel.match-result", new String[]{"killer", winnerName, "victim", loserName, "winner", winnerName, "kit", String.valueOf(duel.getKit()), "score", score}));
        } else if (reason == EndReason.DISCONNECT) {
            if (winner != null) {
                winner.sendMessage(this.msg("duel.victory-opponent-disconnected", new String[0]));
                Sounds.victory(winner);
            }
            this.sendResultToParticipants(duel, this.msg("duel.match-result", new String[]{"killer", winnerName, "victim", loserName, "winner", winnerName, "kit", String.valueOf(duel.getKit()), "score", score}));
        } else if (reason != EndReason.ROUND_WIN) {
            // A round-ending death already sent personalized round-won/round-lost
            // results. Do not follow those with a generic match result line.
            int line = 1 + ThreadLocalRandom.current().nextInt(10);
            this.sendResultToParticipants(duel, this.msg("match.toxic." + line, "winner", winnerName, "loser", loserName, "winner_score", String.valueOf(duel.getScoreFor(winnerId)), "loser_score", String.valueOf(duel.getScoreAgainst(winnerId))));
        }
    }

    private void returnToSpawn(UUID playerId, Location center, Location spawn, double flightCeiling, Runnable completed) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline() || this.isInDuel(playerId)
            || this.plugin.getEventManager().isInvolved(playerId)
            || this.plugin.getPartyManager().inPartyMatch(playerId)
            || this.plugin.getSpectateManager().isSpectating(playerId)) {
            completed.run();
            return;
        }
        if (spawn == null || spawn.getWorld() == null) {
            completed.run();
            return;
        }
        if (!LaunchToSpawn.launch(player, this.plugin, center, spawn, flightCeiling, completed)) {
            player.teleport(spawn);
            completed.run();
        }
    }

    private void finishRoundLoss(ActiveDuel activeDuel, UUID uUID) {
        if (this.isStill(activeDuel)) {
            this.endMatch(activeDuel, uUID, EndReason.ROUND_WIN);
        }
    }

    private void lambda$prepare$5(UUID uUID, Kit kit) {
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null && this.playerDuels.containsKey(uUID)) {
            this.plugin.getKitLayouts().applyOffhand(player, kit);
        }
    }

    private void lambda$prepare$4(UUID uUID, Kit kit) {
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null && this.playerDuels.containsKey(uUID)) {
            this.plugin.getKitLayouts().applyOffhand(player, kit);
        }
    }

    private static enum EndReason {
        ROUND_WIN,
        LEAVE,
        DISCONNECT;

    }
}

