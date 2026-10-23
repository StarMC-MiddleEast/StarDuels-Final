package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Style;
import com.meowduels.model.Arena;
import com.meowduels.model.Kit;
import com.meowduels.model.PlayerSnapshot;
import com.meowduels.util.AntiCheatBypass;
import com.meowduels.util.GameModeGuard;
import com.meowduels.util.Sounds;
import com.meowduels.util.Trims;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

public class EventManager {
    private final MeowDuels plugin;
    private State state = State.NONE;
    private UUID host;
    private String kit;
    private Arena arena;
    private long startAtMs;
    private int maxSlots = 0;
    private final Set<UUID> players = ConcurrentHashMap.newKeySet();
    private final Set<UUID> alive = ConcurrentHashMap.newKeySet();
    private final Set<UUID> spectators = ConcurrentHashMap.newKeySet();
    private final Map<UUID, PlayerSnapshot> snapshots = new HashMap<UUID, PlayerSnapshot>();
    private final Map<Location, BlockData> changedBlocks = new HashMap<Location, BlockData>();
    private final Map<UUID, Integer> kills = new ConcurrentHashMap<UUID, Integer>();
    private long runningSinceMs = 0L;
    private int borderGen = 0;
    private final Map<UUID, Integer> borderSeconds = new HashMap<UUID, Integer>();
    private final MiniMessage borderMm = MiniMessage.miniMessage();
    private WorldBorder eventBorder;
    private double borderCenterX;
    private double borderCenterZ;

    public EventManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    public State getState() {
        return this.state;
    }

    public boolean isWaiting() {
        return this.state == State.WAITING;
    }

    public boolean isRunning() {
        return this.state == State.RUNNING;
    }

    public boolean isPlaying(UUID uUID) {
        return this.players.contains(uUID);
    }

    public boolean isAlive(UUID uUID) {
        return this.alive.contains(uUID);
    }

    public Arena getArena() {
        return this.arena;
    }

    public int aliveCount() {
        return this.alive.size();
    }

    public int playerCount() {
        return this.players.size();
    }

    public int spectatorCount() {
        return this.spectators.size();
    }

    public String getKit() {
        return this.kit == null ? "" : this.kit;
    }

    public int killsOf(UUID uUID) {
        Integer n = this.kills.get(uUID);
        return n == null ? 0 : n;
    }

    public long runningSeconds() {
        return this.runningSinceMs == 0L ? 0L : Math.max(0L, (System.currentTimeMillis() - this.runningSinceMs) / 1000L);
    }

    public Set<UUID> involved() {
        HashSet<UUID> hashSet = new HashSet<UUID>(this.players);
        hashSet.addAll(this.spectators);
        return hashSet;
    }

    public void recordChange(Location location, BlockData blockData) {
        if (location != null && blockData != null && !this.changedBlocks.containsKey(location)) {
            this.changedBlocks.put(location, blockData);
        }
    }

    public void host(Player player, String string, int n, int n2, Arena arena) {
        if (this.state != State.NONE) {
            player.sendMessage(this.msg("event.already-running", new String[0]));
            return;
        }
        Kit kit = this.plugin.getKitManager().get(string);
        if (kit == null) {
            player.sendMessage(this.msg("event.no-kit", new String[0]));
            return;
        }
        if (arena == null) {
            player.sendMessage(this.msg("event.no-arena", new String[0]));
            return;
        }
        if (!arena.isEventReady()) {
            player.sendMessage(this.msg("event.no-event-spawn", "arena", arena.getName()));
            return;
        }
        if (this.plugin.getDuelManager().isArenaBusy(arena)) {
            player.sendMessage(this.msg("event.arena-busy", "arena", arena.getName()));
            return;
        }
        this.arena = arena;
        this.kit = kit.getName();
        this.host = player.getUniqueId();
        this.state = State.WAITING;
        this.maxSlots = Math.max(0, n2);
        this.players.clear();
        this.alive.clear();
        this.spectators.clear();
        this.snapshots.clear();
        this.plugin.getDuelManager().markArenaInUse(arena.getName());
        this.startAtMs = System.currentTimeMillis() + (long)Math.max(1, n) * 60000L;
        this.startBorder();
        this.joinInternal(player);
        this.announceTick();
    }

    public void forceStart(Player player) {
        if (this.state != State.WAITING) {
            player.sendMessage(this.msg("event.none", new String[0]));
            return;
        }
        this.start();
    }

    public void forceEnd(Player player) {
        if (this.state == State.NONE) {
            player.sendMessage(this.msg("event.none", new String[0]));
            return;
        }
        this.broadcast(this.msg("event.force-ended", new String[0]));
        this.end();
    }

    public void join(Player player) {
        UUID uUID = player.getUniqueId();
        if (this.plugin.getPartyManager().busy(player)) {
            return;
        }
        if (this.state == State.RUNNING) {
            player.sendMessage(this.msg("event.started-cannot-join", new String[0]));
            return;
        }
        if (this.state != State.WAITING) {
            player.sendMessage(this.msg("event.none", new String[0]));
            return;
        }
        if (this.players.contains(uUID)) {
            player.sendMessage(this.msg("event.already-joined", new String[0]));
            return;
        }
        if (this.maxSlots > 0 && this.players.size() >= this.maxSlots) {
            player.sendMessage(this.msg("event.full", "slots", String.valueOf(this.maxSlots)));
            return;
        }
        if (this.plugin.getDuelManager().isInDuel(uUID) || this.plugin.getSpectateManager().isSpectating(uUID) || this.plugin.getQueueManager().isQueued(uUID)) {
            player.sendMessage(this.msg("event.busy", new String[0]));
            return;
        }
        this.spectators.remove(uUID);
        this.joinInternal(player);
        player.sendMessage(this.msg("event.joined", new String[0]));
    }

    private Location eventSpawnLoc() {
        if (this.arena == null) {
            return null;
        }
        if (this.arena.getEventSpawn() != null) {
            return this.arena.getEventSpawn();
        }
        return this.arena.getSpawn1() != null ? this.arena.getSpawn1() : this.arena.getSpawn2();
    }

    public boolean isSpectating(UUID uUID) {
        return this.spectators.contains(uUID);
    }

    public boolean isInvolved(UUID uUID) {
        return this.players.contains(uUID) || this.spectators.contains(uUID);
    }

    public void spectate(Player player) {
        UUID uUID = player.getUniqueId();
        if (this.state == State.NONE) {
            player.sendMessage(this.msg("event.none", new String[0]));
            return;
        }
        if (this.players.contains(uUID)) {
            player.sendMessage(this.msg("event.spectate-playing", new String[0]));
            return;
        }
        if (this.spectators.contains(uUID)) {
            player.sendMessage(this.msg("event.already-spectating", new String[0]));
            return;
        }
        if (this.plugin.getDuelManager().isInDuel(uUID) || this.plugin.getSpectateManager().isSpectating(uUID) || this.plugin.getQueueManager().isQueued(uUID)) {
            player.sendMessage(this.msg("event.busy", new String[0]));
            return;
        }
        this.snapshots.put(uUID, PlayerSnapshot.capture(player));
        this.spectators.add(uUID);
        this.forceSpectator(player, this.eventSpawnLoc());
        this.applyBorderTo(player);
        player.sendMessage(this.msg("event.spectate-start", new String[0]));
    }

    private void forceSpectator(Player player, Location location) {
        GameModeGuard.pin(player, GameMode.SPECTATOR);
        if (location != null) {
            player.teleport(location);
        }
    }

    private boolean stopSpectating(Player player) {
        UUID uUID = player.getUniqueId();
        if (!this.spectators.remove(uUID)) {
            return false;
        }
        PlayerSnapshot playerSnapshot = this.snapshots.remove(uUID);
        GameModeGuard.release(uUID);
        if (playerSnapshot != null) {
            playerSnapshot.restore(player);
            Location location = this.plugin.getDuelSpawn() != null ? this.plugin.getDuelSpawn() : playerSnapshot.getLocation();
            player.teleport(location);
        }
        return true;
    }

    private void joinInternal(Player player) {
        UUID uUID = player.getUniqueId();
        this.players.add(uUID);
        this.alive.add(uUID);
        this.snapshots.put(uUID, PlayerSnapshot.capture(player));
        Location location = this.eventSpawnLoc();
        String string = location != null && location.getWorld() != null ? location.getWorld().getName() : null;
        AntiCheatBypass.grant(this.plugin, player, AntiCheatBypass.worldNodes(this.plugin, string));
        Location location2 = this.eventSpawnLoc();
        if (location2 != null) {
            player.teleport(location2);
        }
        this.applyBorderTo(player);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
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
        Kit kit = this.plugin.getKitManager().get(this.kit);
        if (kit != null) {
            ItemStack[] itemStackArray;
            kit.applyTo(player);
            for (ItemStack itemStack : itemStackArray = player.getInventory().getArmorContents()) {
                Trims.clear(itemStack);
            }
            this.plugin.getTrimPreferences().applyToArmor(uUID, kit.getName(), itemStackArray);
            player.getInventory().setArmorContents(itemStackArray);
            player.updateInventory();
            Object object = kit;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> EventManager.lambda$joinInternal$0(uUID, (Kit)object), 1L);
        }
    }

    private void announceTick() {
        if (this.state != State.WAITING) {
            return;
        }
        long l = (this.startAtMs - System.currentTimeMillis() + 999L) / 1000L;
        if (l <= 0L) {
            this.start();
            return;
        }
        if (l % 10L == 0L || l <= 10L) {
            Player player = Bukkit.getPlayer((UUID)this.host);
            String string = player != null ? player.getName() : "Someone";
            this.broadcast(this.msg("event.announce", "host", string, "kit", this.kitLabel(), "time", EventManager.formatTime(l), "slots", this.slotsLabel()));
        }
        if (l <= 10L) {
            int n = (int)l;
            for (UUID uUID : new HashSet<UUID>(this.players)) {
                Player player = Bukkit.getPlayer((UUID)uUID);
                if (player == null) continue;
                player.sendTitle(this.msg("event.countdown-title", "seconds", String.valueOf(n)), this.msg("event.countdown-subtitle", new String[0]), 0, 25, 0);
                Sounds.tick(player, n, 10);
            }
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, this::announceTick, 20L);
    }

    private String slotsLabel() {
        if (this.maxSlots > 0) {
            return this.players.size() + "/" + this.maxSlots;
        }
        return String.valueOf(this.players.size());
    }

    public void announceKill(UUID uUID, UUID uUID2) {
        if (this.state != State.RUNNING) {
            return;
        }
        Player player = Bukkit.getPlayer((UUID)uUID);
        String string = player != null ? player.getName() : "A player";
        int n = this.alive.size();
        if (this.alive.contains(uUID)) {
            --n;
        }
        Player player2 = uUID2 == null ? null : Bukkit.getPlayer((UUID)uUID2);
        Player player3 = player2;
        if (uUID2 != null && !uUID2.equals(uUID) && this.players.contains(uUID2)) {
            this.kills.merge(uUID2, 1, Integer::sum);
        }
        if (player2 != null && !uUID2.equals(uUID)) {
            this.broadcast(this.msg("event.kill.pvp", "killer", player2.getName(), "victim", string, "alive", String.valueOf(n)));
            Sounds.kill(player2);
        } else {
            this.broadcast(this.msg("event.kill.generic", "victim", string, "alive", String.valueOf(n)));
        }
        if (player != null) {
            Sounds.death(player);
            player.sendTitle(this.msg("event.out-title", new String[0]), this.msg("event.out-subtitle", "kills", String.valueOf(this.killsOf(uUID))), 5, 40, 10);
        }
        for (UUID uUID3 : new HashSet<UUID>(this.alive)) {
            Player player4;
            if (uUID3.equals(uUID) || uUID3.equals(uUID2) || (player4 = Bukkit.getPlayer((UUID)uUID3)) == null) continue;
            Sounds.eliminated(player4);
        }
    }

    private void start() {
        Player player;
        if (this.state != State.WAITING) {
            return;
        }
        this.state = State.RUNNING;
        this.runningSinceMs = System.currentTimeMillis();
        this.kills.clear();
        Kit kit = this.plugin.getKitManager().get(this.kit);
        if (kit != null) {
            for (UUID uUID : this.alive) {
                player = Bukkit.getPlayer((UUID)uUID);
                if (player == null) continue;
                kit.applyStartEffects(player);
            }
        }
        for (UUID uUID : new HashSet<UUID>(this.alive)) {
            player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            player.sendTitle(this.msg("event.start-title", new String[0]), this.msg("event.start-subtitle", new String[0]), 0, 40, 10);
            Sounds.fight(player);
        }
        this.broadcast(this.msg("event.started", new String[0]));
        this.startBorder();
        this.beginBorderShrink();
        this.checkWin();
    }

    private void startBorder() {
        double d;
        if (this.eventBorder != null) {
            return;
        }
        if (this.arena == null || this.arena.getMin() == null || this.arena.getMax() == null) {
            return;
        }
        if (!this.plugin.getConfig().getBoolean("event.border.enabled", true)) {
            return;
        }
        this.borderCenterX = this.arena.getBorderCenterX();
        this.borderCenterZ = this.arena.getBorderCenterZ();
        double d2 = this.arena.getBorderStartSize();
        double d3 = Math.max(1, this.arena.getEventBorderEnd());
        if (d2 < d3) {
            d2 = d3;
        }
        try {
            this.eventBorder = Bukkit.createWorldBorder();
            this.eventBorder.setCenter(this.borderCenterX, this.borderCenterZ);
            this.eventBorder.setWarningDistance(0);
            this.eventBorder.setWarningTime(0);
            this.eventBorder.setSize(d2);
            this.eventBorder.setDamageAmount(0.0);
            this.eventBorder.setDamageBuffer(0.0);
        }
        catch (Throwable throwable) {
            this.eventBorder = null;
            return;
        }
        this.applyBorderToAll();
    }

    private void beginBorderShrink() {
        if (this.eventBorder == null || this.arena == null) {
            return;
        }
        ++this.borderGen;
        this.scheduleBorderShrink(this.borderGen, this.eventBorder.getSize());
    }

    private void applyBorderTo(Player player) {
        if (this.eventBorder != null && player != null) {
            try {
                player.setWorldBorder(this.eventBorder);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    private void clearBorderFor(Player player) {
        if (player != null) {
            try {
                player.setWorldBorder(null);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    private void applyBorderToAll() {
        if (this.eventBorder == null) {
            return;
        }
        HashSet<UUID> hashSet = new HashSet<UUID>(this.players);
        hashSet.addAll(this.spectators);
        for (UUID uUID : hashSet) {
            this.applyBorderTo(Bukkit.getPlayer((UUID)uUID));
        }
    }

    private void scheduleBorderShrink(int n, double d) {
        double d2 = this.arena != null ? (double)Math.max(1, this.arena.getEventBorderEnd()) : 30.0;
        double d3 = d2;
        if (d <= d2) {
            return;
        }
        int n2 = this.arena != null ? this.arena.getEventBorderInterval() : 120;
        long l = (long)Math.max(1, n2) * 20L;
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (this.state != State.RUNNING || n != this.borderGen || this.eventBorder == null || this.arena == null) {
                return;
            }
            double step = Math.max(1, this.arena.getEventBorderStep());
            double d4 = Math.max(d2, d - step);
            long shrinkSeconds = this.plugin.getConfig().getInt("event.border.shrink-seconds", 8);
            this.eventBorder.setSize(d4, Math.max(1L, shrinkSeconds));
            this.applyBorderToAll();
            this.scheduleBorderShrink(n, d4);
        }, l);
    }

    public void tickBorderDamage() {
        if (this.state != State.RUNNING || this.eventBorder == null || this.alive.isEmpty()) {
            this.borderSeconds.clear();
            return;
        }
        double d = this.eventBorder.getSize() / 2.0;
        double d2 = this.plugin.getConfig().getDouble("event.border.damage.base", 2.0);
        double d3 = this.plugin.getConfig().getDouble("event.border.damage.per-second", 1.0);
        double d4 = this.plugin.getConfig().getDouble("event.border.damage.max", 20.0);
        this.borderSeconds.keySet().retainAll(this.alive);
        for (UUID uUID : new HashSet<UUID>(this.alive)) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) {
                this.borderSeconds.remove(uUID);
                continue;
            }
            Location location = player.getLocation();
            if (!(Math.abs(location.getX() - this.borderCenterX) > d) && !(Math.abs(location.getZ() - this.borderCenterZ) > d)) {
                this.borderSeconds.remove(uUID);
                continue;
            }
            Integer n = this.borderSeconds.get(uUID);
            int n2 = (n == null ? 0 : n) + 1;
            this.borderSeconds.put(uUID, n2);
            double d5 = Math.min(d4, d2 + d3 * (double)(n2 - 1));
            try {
                player.damage(d5);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.warnOutside(player, n2, d5);
            Sounds.border(player, n2);
        }
    }

    private void warnOutside(Player player, int n, double d) {
        try {
            String string = EventManager.oneDecimal(d / 2.0);
            player.sendActionBar(this.borderMm.deserialize(("<gradient:" + Style.ACCENT_A + ":" + Style.ACCENT_B + ">\u1d0f\u1d1c\u1d1b\ua731\u026a\u1d05\u1d07 \u1d1b\u029c\u1d07 \u0299\u1d0f\u0280\u1d05\u1d07\u0280</gradient> <#6B7079>\u2503 <#FF8A93>-" + string + " \u2764 <#6B7079>\u2503 <#E6E8EB>" + n + "s")));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static String oneDecimal(double d) {
        long l = Math.round(d * 10.0);
        return l % 10L == 0L ? String.valueOf(l / 10L) : l / 10L + "." + l % 10L;
    }

    private void resetBorder() {
        ++this.borderGen;
        HashSet<UUID> hashSet = new HashSet<UUID>(this.players);
        hashSet.addAll(this.spectators);
        for (UUID uUID : hashSet) {
            this.clearBorderFor(Bukkit.getPlayer((UUID)uUID));
        }
        this.eventBorder = null;
    }

    public void onDeath(UUID uUID) {
        if (this.state != State.RUNNING || !this.players.contains(uUID)) {
            return;
        }
        this.players.remove(uUID);
        this.alive.remove(uUID);
        this.spectators.add(uUID);
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            Location location = this.eventSpawnLoc();
            this.forceSpectator(player, location != null ? location : player.getLocation());
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.eliminate(uUID), 40L);
        this.checkWin();
    }

    private void eliminate(UUID uUID) {
        if (!this.spectators.remove(uUID)) {
            return;
        }
        this.alive.remove(uUID);
        this.players.remove(uUID);
        Player player = Bukkit.getPlayer((UUID)uUID);
        PlayerSnapshot playerSnapshot = this.snapshots.remove(uUID);
        if (player != null && playerSnapshot != null) {
            AntiCheatBypass.release(this.plugin, player);
            GameModeGuard.release(player.getUniqueId());
            GameModeGuard.release(player.getUniqueId());
            this.clearBorderFor(player);
            playerSnapshot.restore(player);
            Location location = this.plugin.getDuelSpawn() != null ? this.plugin.getDuelSpawn() : playerSnapshot.getLocation();
            player.teleport(location);
            this.plugin.giveSpawnItems(player);
        }
    }

    public void leave(Player player) {
        UUID uUID = player.getUniqueId();
        boolean bl = this.alive.remove(uUID);
        boolean bl2 = this.players.remove(uUID);
        boolean bl3 = this.spectators.remove(uUID);
        if (!bl2 && !bl3) {
            return;
        }
        PlayerSnapshot playerSnapshot = this.snapshots.remove(uUID);
        AntiCheatBypass.release(this.plugin, player);
        GameModeGuard.release(player.getUniqueId());
        this.clearBorderFor(player);
        if (playerSnapshot != null) {
            playerSnapshot.restore(player);
            Location location = this.plugin.getDuelSpawn() != null ? this.plugin.getDuelSpawn() : playerSnapshot.getLocation();
            player.teleport(location);
        }
        this.plugin.giveSpawnItems(player);
        player.sendMessage(this.msg("event.left", new String[0]));
        if (this.state == State.RUNNING && bl) {
            this.checkWin();
        }
    }

    public void handleQuit(UUID uUID) {
        boolean bl = this.alive.remove(uUID);
        boolean bl2 = this.players.remove(uUID);
        boolean bl3 = this.spectators.remove(uUID);
        if (!bl2 && !bl3) {
            return;
        }
        this.snapshots.remove(uUID);
        if (this.state == State.RUNNING && bl) {
            this.checkWin();
        }
    }

    private void checkWin() {
        if (this.state != State.RUNNING) {
            return;
        }
        if (this.alive.size() <= 1) {
            UUID uUID = this.alive.isEmpty() ? null : this.alive.iterator().next();
            UUID uUID2 = uUID;
            if (uUID != null) {
                Player player = Bukkit.getPlayer((UUID)uUID);
                String string = player != null ? player.getName() : "A player";
                this.broadcast(this.msg("event.win", "winner", string));
                if (player != null) {
                    player.sendTitle(this.msg("event.win-title", new String[0]), this.msg("event.win-subtitle", new String[0]), 5, 50, 10);
                    Sounds.victory(player);
                }
            }
            this.end();
        }
    }

    private void end() {
        HashSet<UUID> hashSet = new HashSet<UUID>(this.players);
        hashSet.addAll(this.spectators);
        for (UUID uUID : hashSet) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            PlayerSnapshot playerSnapshot = this.snapshots.remove(uUID);
            if (player == null || playerSnapshot == null) continue;
            AntiCheatBypass.release(this.plugin, player);
            GameModeGuard.release(player.getUniqueId());
            GameModeGuard.release(player.getUniqueId());
            player.getInventory().clear();
            playerSnapshot.restore(player);
            Location location = this.plugin.getDuelSpawn() != null ? this.plugin.getDuelSpawn() : playerSnapshot.getLocation();
            player.teleport(location);
            this.plugin.giveSpawnItems(player);
        }
        this.resetBorder();
        if (this.arena != null) {
            this.clearDroppedItems(this.arena);
            int n = this.plugin.getArenaManager().regenArena(this.arena);
            if (n < 0 && !this.changedBlocks.isEmpty()) {
                this.plugin.getArenaManager().restoreBlocks(this.changedBlocks);
            }
            this.plugin.getArenaManager().clearDirty(this.arena.getName());
            this.plugin.getDuelManager().freeArena(this.arena.getName());
        }
        this.changedBlocks.clear();
        this.state = State.NONE;
        this.runningSinceMs = 0L;
        this.host = null;
        this.kit = null;
        this.arena = null;
        this.maxSlots = 0;
        this.players.clear();
        this.alive.clear();
        this.spectators.clear();
        this.snapshots.clear();
    }

    private void clearDroppedItems(Arena arena) {
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
            if (!(entity instanceof Item) && !(entity instanceof ExperienceOrb)) continue;
            entity.remove();
        }
    }

    public void shutdown() {
        if (this.state != State.NONE) {
            this.end();
        }
    }

    private String kitLabel() {
        Kit kit = this.plugin.getKitManager().get(this.kit);
        if (kit != null && kit.getDisplayName() != null && !kit.getDisplayName().isEmpty()) {
            return kit.getDisplayName().replaceAll("<[^>]*>", "");
        }
        return this.kit;
    }

    private static String formatTime(long l) {
        if (l >= 60L) {
            long l2 = l / 60L;
            long l3 = l % 60L;
            return l3 == 0L ? l2 + "m" : l2 + "m " + l3 + "s";
        }
        return l + "s";
    }

    private void broadcast(String string) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(string);
        }
    }

    private String msg(String string, String ... stringArray) {
        return this.plugin.messages().get(string, stringArray);
    }

    private static void lambda$joinInternal$0(UUID uUID, Kit kit) {
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            kit.applyOffhand(player);
        }
    }

    public static enum State {
        NONE,
        WAITING,
        RUNNING;

    }
}

