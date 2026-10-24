package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.model.Arena;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;

public class ArenaManager {
    private static final Pattern TRAILING_NUMBER = Pattern.compile("^(.*?)(\\d+)$");
    private final MeowDuels plugin;
    private final File file;
    private final File snapshotDir;
    private final Set<String> dirty = new HashSet<String>();
    private final File dirtyFile;
    private final Map<String, Arena> arenas = new LinkedHashMap<String, Arena>();
    private final Map<UUID, RegionClipboard> clipboards = new HashMap<UUID, RegionClipboard>();
    /** Concurrent: the startup preload fills it from a background thread. */
    private final Map<String, RegionClipboard> snapshots = new java.util.concurrent.ConcurrentHashMap<String, RegionClipboard>();
    /**
     * One thread for every disk write this class makes, so they land in the
     * order they were asked for - two snapshots of one arena taken in quick
     * succession must not finish in the wrong order.
     */
    private final java.util.concurrent.ExecutorService io = java.util.concurrent.Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "StarDuels-ArenaIO");
        thread.setDaemon(true);
        return thread;
    });
    /** Arena regenerations waiting their turn, worked through a slice per tick. */
    private final java.util.ArrayDeque<RegenJob> regenJobs = new java.util.ArrayDeque<RegenJob>();
    /**
     * Arenas that may not be handed to a new match until their regen
     * finishes, with when they were held - see {@link #sweepStaleHolds}.
     */
    private final Map<String, Long> regenerating = new HashMap<String, Long>();
    private int sweepCountdown = 20;
    private volatile Set<String> arenaWorlds = new HashSet<String>();

    public ArenaManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.file = new File(meowDuels.getDataFolder(), "arenas.yml");
        this.snapshotDir = new File(meowDuels.getDataFolder(), "arena-snapshots");
        this.dirtyFile = new File(meowDuels.getDataFolder(), "arenas-dirty.yml");
        if (!this.snapshotDir.exists()) {
            this.snapshotDir.mkdirs();
        }
        this.load();
        this.loadDirty();
        this.preloadSnapshots();
        // Always on, but returns at once when there is nothing queued - one
        // isEmpty() a tick - which is cheaper than starting and cancelling a
        // task around every match.
        Bukkit.getScheduler().runTaskTimer((org.bukkit.plugin.Plugin)meowDuels, this::tickRegen, 1L, 1L);
    }

    public void clearLooseEntities(Arena arena) {
        if (arena == null) {
            return;
        }
        World world = arena.getWorld();
        Location location = arena.getMin();
        Location location2 = arena.getMax();
        if (world == null || location == null || location2 == null) {
            return;
        }
        Location location3 = new Location(world, (double)(location.getBlockX() + location2.getBlockX()) / 2.0 + 0.5, (double)(location.getBlockY() + location2.getBlockY()) / 2.0 + 0.5, (double)(location.getBlockZ() + location2.getBlockZ()) / 2.0 + 0.5);
        double d = (double)Math.abs(location2.getBlockX() - location.getBlockX()) / 2.0 + 2.0;
        double d2 = (double)Math.abs(location2.getBlockY() - location.getBlockY()) / 2.0 + 2.0;
        double d3 = (double)Math.abs(location2.getBlockZ() - location.getBlockZ()) / 2.0 + 2.0;
        for (Entity entity : world.getNearbyEntities(location3, d, d2, d3)) {
            if (!(entity instanceof Projectile) && !(entity instanceof Item) && !(entity instanceof TNTPrimed) && !(entity instanceof ExperienceOrb)) continue;
            entity.remove();
        }
    }

    public void markDirty(String string) {
        if (string != null && this.dirty.add(string.toLowerCase(Locale.ROOT))) {
            this.saveDirty();
        }
    }

    public void clearDirty(String string) {
        if (string != null && this.dirty.remove(string.toLowerCase(Locale.ROOT))) {
            this.saveDirty();
        }
    }

    public void recoverDirtyArenas() {
        if (this.dirty.isEmpty()) {
            return;
        }
        ArrayList<String> arrayList = new ArrayList<String>(this.dirty);
        int n = 0;
        for (String string : arrayList) {
            Arena arena = this.get(string);
            if (arena == null || !arena.isAutoRegenerate()) {
                this.dirty.remove(string);
                continue;
            }
            if (this.regenArena(arena) >= 0) {
                ++n;
            }
            this.dirty.remove(string);
        }
        this.saveDirty();
        if (n > 0) {
            this.plugin.getLogger().info("Regenerated " + n + " arena(s) left dirty by the last shutdown.");
        }
    }

    private void loadDirty() {
        if (!this.dirtyFile.exists()) {
            return;
        }
        try {
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration((File)this.dirtyFile);
            for (String string : yamlConfiguration.getStringList("dirty")) {
                if (string == null || string.isEmpty()) continue;
                this.dirty.add(string.toLowerCase(Locale.ROOT));
            }
        }
        catch (Throwable throwable) {
            this.plugin.getLogger().warning("Could not read arenas-dirty.yml: " + String.valueOf(throwable));
        }
    }

    /**
     * Writes arenas-dirty.yml off the main thread.
     *
     * <p>This used to save synchronously at the start and end of every single
     * match - two file writes on the tick thread per match, which is a
     * measurable MSPT spike on a busy server. The set is copied here on the
     * main thread, so the writer never sees it change underneath it.
     */
    private void saveDirty() {
        List<String> copy = new ArrayList<String>(this.dirty);
        this.io.execute(() -> {
            try {
                YamlConfiguration yamlConfiguration = new YamlConfiguration();
                yamlConfiguration.set("dirty", copy);
                yamlConfiguration.save(this.dirtyFile);
            }
            catch (Throwable throwable) {
                this.plugin.getLogger().warning("Could not write arenas-dirty.yml: " + String.valueOf(throwable));
            }
        });
    }

    /** Lets pending writes finish. Called from onDisable. */
    public void shutdown() {
        this.io.shutdown();
        try {
            if (!this.io.awaitTermination(10L, java.util.concurrent.TimeUnit.SECONDS)) {
                this.plugin.getLogger().warning("Arena snapshot writes were still running at shutdown.");
            }
        }
        catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    public void load() {
        this.arenas.clear();
        if (!this.file.exists()) {
            return;
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration((File)this.file);
        ConfigurationSection configurationSection = yamlConfiguration.getConfigurationSection("arenas");
        if (configurationSection == null) {
            return;
        }
        for (String string : configurationSection.getKeys(false)) {
            ConfigurationSection configurationSection2 = configurationSection.getConfigurationSection(string);
            if (configurationSection2 == null) continue;
            this.arenas.put(string.toLowerCase(Locale.ROOT), Arena.load(string, configurationSection2));
        }
        this.rebuildWorldIndex();
        this.plugin.getLogger().info("Loaded " + this.arenas.size() + " arena(s).");
    }

    public void save() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        for (Arena arena : this.arenas.values()) {
            arena.save(yamlConfiguration.createSection("arenas." + arena.getName()));
        }
        try {
            yamlConfiguration.save(this.file);
        }
        catch (IOException iOException) {
            this.plugin.getLogger().severe("Failed to save arenas.yml: " + iOException.getMessage());
        }
        this.rebuildWorldIndex();
    }

    private void rebuildWorldIndex() {
        HashSet<String> hashSet = new HashSet<String>();
        for (Arena arena : this.arenas.values()) {
            String string = arena.getWorldName();
            if (string == null) continue;
            hashSet.add(string.toLowerCase(Locale.ROOT));
        }
        this.arenaWorlds = hashSet;
    }

    public boolean hasArenaInWorld(World world) {
        return world != null && this.arenaWorlds.contains(world.getName().toLowerCase(Locale.ROOT));
    }

    public Arena get(String string) {
        return string == null ? null : this.arenas.get(string.toLowerCase(Locale.ROOT));
    }

    public boolean exists(String string) {
        return this.get(string) != null;
    }

    public Arena create(String string) {
        Arena arena = new Arena(string);
        this.arenas.put(string.toLowerCase(Locale.ROOT), arena);
        this.save();
        return arena;
    }

    public void delete(String string) {
        this.arenas.remove(string.toLowerCase(Locale.ROOT));
        this.snapshots.remove(string.toLowerCase(Locale.ROOT));
        this.snapshotFile(string).delete();
        this.save();
    }

    public List<Arena> all() {
        return new ArrayList<Arena>(this.arenas.values());
    }

    public Arena findFreeArena(Predicate<Arena> predicate) {
        for (Arena arena : this.arenas.values()) {
            if (!arena.isConfigured() || !arena.isEnabled() || this.isRegenerating(arena.getName()) || predicate.test(arena)) continue;
            return arena;
        }
        return null;
    }

    public Arena findArenaAt(Location location) {
        for (Arena arena : this.arenas.values()) {
            if (!arena.contains(location)) continue;
            return arena;
        }
        return null;
    }

    public void restoreBlocks(Map<Location, BlockData> map) {
        for (Map.Entry<Location, BlockData> entry : map.entrySet()) {
            Location location = entry.getKey();
            if (location.getWorld() == null) continue;
            location.getBlock().setBlockData(entry.getValue(), false);
        }
    }

    private File snapshotFile(String string) {
        return new File(this.snapshotDir, string.toLowerCase(Locale.ROOT) + ".snapshot.gz");
    }

    public boolean hasSnapshot(Arena arena) {
        return this.snapshots.containsKey(arena.getName().toLowerCase(Locale.ROOT)) || this.snapshotFile(arena.getName()).exists();
    }

    public boolean snapshotArena(Arena arena) {
        if (!arena.isConfigured() || arena.getWorld() == null) {
            return false;
        }
        RegionClipboard regionClipboard = RegionClipboard.capture(arena.getWorld(), arena.getMin(), arena.getMax());
        this.snapshots.put(arena.getName().toLowerCase(Locale.ROOT), regionClipboard);
        // The capture has to read blocks, so it stays on the main thread; the
        // gzip write does not, and used to be the slow half.
        String name = arena.getName();
        this.io.execute(() -> this.writeSnapshotToDisk(name, regionClipboard));
        return true;
    }

    /**
     * The whole arena in one tick. Kept for admin actions - /arena regen and
     * the menu button - and for startup recovery, where a pause is expected.
     * Matches never call this; they use {@link #regenAfterMatch}.
     */
    public int regenArena(Arena arena) {
        if (!arena.isConfigured() || arena.getWorld() == null) {
            return -1;
        }
        RegionClipboard regionClipboard = this.snapshotFor(arena);
        if (regionClipboard == null) {
            return -1;
        }
        return regionClipboard.pasteAt(arena.getWorld(), arena.getMin());
    }

    private RegionClipboard snapshotFor(Arena arena) {
        String key = arena.getName().toLowerCase(Locale.ROOT);
        RegionClipboard regionClipboard = this.snapshots.get(key);
        if (regionClipboard == null && (regionClipboard = this.readSnapshotFromDisk(arena.getName())) != null) {
            // Only if the background preload has not got there first.
            RegionClipboard raced = this.snapshots.putIfAbsent(key, regionClipboard);
            if (raced != null) {
                regionClipboard = raced;
            }
        }
        return regionClipboard;
    }

    /**
     * Reads every snapshot from disk in the background at startup.
     *
     * <p>Without this the first match in each arena after a restart parsed a
     * multi-megabyte gzip on the tick thread. putIfAbsent, so a snapshot an
     * admin takes while this is still running is never overwritten by the
     * older file.
     */
    private void preloadSnapshots() {
        List<String> names = new ArrayList<String>();
        for (Arena arena : this.arenas.values()) {
            names.add(arena.getName());
        }
        this.io.execute(() -> {
            int loaded = 0;
            for (String name : names) {
                RegionClipboard regionClipboard = this.readSnapshotFromDisk(name);
                if (regionClipboard == null) continue;
                this.snapshots.putIfAbsent(name.toLowerCase(Locale.ROOT), regionClipboard);
                ++loaded;
            }
            if (loaded > 0) {
                this.plugin.getLogger().info("Preloaded " + loaded + " arena snapshot(s).");
            }
        });
    }

    // ---------------------------------------------------------------- regen

    /**
     * Regenerates an arena after a match, without stalling the server.
     *
     * <p>This used to repaint the entire bounding box in one tick - every
     * block read, compared and possibly written, for every arena, at the end
     * of every round and every match. On a 100x60x100 arena that is 600,000
     * blocks inside a single 50 ms tick, and it ran first, falling back to the
     * cheap list of blocks players actually changed only when there was no
     * snapshot. Backwards on both counts.
     *
     * <p>Now: the tracked changes are put back immediately, which is what
     * players built and broke and is usually a few hundred blocks. Then the
     * snapshot is checked across the following ticks, a few milliseconds at a
     * time, to catch what tracking cannot see - flowing water and lava, fire
     * spread, cobble and obsidian forming. The arena is not handed to another
     * match until that pass finishes, so a new fight never starts in a
     * half-restored arena or has its own blocks reverted underneath it.
     */
    public void regenAfterMatch(Arena arena, Map<Location, BlockData> changed, Runnable whenDone) {
        if (arena == null) {
            if (whenDone != null) {
                whenDone.run();
            }
            return;
        }
        this.cancelRegen(arena);
        if (changed != null && !changed.isEmpty()) {
            this.restoreBlocks(changed);
        }
        String key = arena.getName().toLowerCase(Locale.ROOT);
        RegionClipboard regionClipboard = arena.isConfigured() && arena.getWorld() != null ? this.snapshotFor(arena) : null;
        if (regionClipboard == null) {
            this.regenerating.remove(key);
            this.clearDirty(arena.getName());
            if (whenDone != null) {
                whenDone.run();
            }
            return;
        }
        this.regenerating.put(key, System.currentTimeMillis());
        Location min = arena.getMin();
        this.regenJobs.addLast(new RegenJob(key, arena.getName(), arena.getWorld(), regionClipboard,
                min.getBlockX(), min.getBlockY(), min.getBlockZ(), true, whenDone));
    }

    /**
     * The same between rounds, but the arena stays with its match: the
     * snapshot pass is cancelled by {@link #cancelRegen} when the next round
     * starts, so it can never revert blocks placed in that round.
     */
    public void regenBetweenRounds(Arena arena, Map<Location, BlockData> changed) {
        if (arena == null || this.isRegenerating(arena.getName())) {
            return;
        }
        this.cancelRegen(arena);
        if (changed != null && !changed.isEmpty()) {
            this.restoreBlocks(changed);
        }
        RegionClipboard regionClipboard = arena.isConfigured() && arena.getWorld() != null ? this.snapshotFor(arena) : null;
        if (regionClipboard == null) {
            return;
        }
        Location min = arena.getMin();
        this.regenJobs.addLast(new RegenJob(arena.getName().toLowerCase(Locale.ROOT), arena.getName(), arena.getWorld(),
                regionClipboard, min.getBlockX(), min.getBlockY(), min.getBlockZ(), false, null));
    }

    /**
     * Holds an arena through a configured regen delay.
     *
     * <p>Without this the arena was released the moment the match ended, the
     * delay ran out later, and the delayed regen then fired in the middle of
     * whichever match had been put there meanwhile - wiping its blocks.
     */
    public void holdForRegen(Arena arena) {
        if (arena != null) {
            this.regenerating.put(arena.getName().toLowerCase(Locale.ROOT), System.currentTimeMillis());
        }
    }

    /** Drops any queued or running pass for this arena. */
    public void cancelRegen(Arena arena) {
        if (arena == null) {
            return;
        }
        String key = arena.getName().toLowerCase(Locale.ROOT);
        java.util.Iterator<RegenJob> iterator = this.regenJobs.iterator();
        while (iterator.hasNext()) {
            RegenJob job = iterator.next();
            if (!job.key.equals(key)) continue;
            iterator.remove();
            if (job.holdsArena) {
                // Whoever cancelled it is about to queue a fresh pass or has no
                // further use for the hold; either way it must not outlive the job.
                this.regenerating.remove(key);
            }
        }
    }

    public boolean isRegenerating(String string) {
        return string != null && this.regenerating.containsKey(string.toLowerCase(Locale.ROOT));
    }

    /** Milliseconds of each tick regen may use, from config; 3 if unset. */
    private long budgetNanos() {
        double ms = this.plugin.getConfig().getDouble("regen.ms-per-tick", 3.0);
        return (long)(Math.max(0.5, Math.min(25.0, ms)) * 1_000_000.0);
    }

    /**
     * Works through queued regenerations, oldest first, inside the budget.
     *
     * <p>Oldest first so the arena whose match ended earliest is the first one
     * back in rotation.
     */
    private void tickRegen() {
        if (--this.sweepCountdown <= 0) {
            this.sweepCountdown = 20;
            this.sweepStaleHolds();
        }
        if (this.regenJobs.isEmpty()) {
            return;
        }
        long deadline = System.nanoTime() + this.budgetNanos();
        while (!this.regenJobs.isEmpty() && System.nanoTime() < deadline) {
            RegenJob job = this.regenJobs.peekFirst();
            try {
                if (!job.step(deadline)) {
                    return;
                }
            }
            catch (Throwable throwable) {
                this.plugin.getLogger().warning("Regenerating arena '" + job.name + "' failed: " + throwable);
            }
            this.regenJobs.pollFirst();
            this.finish(job);
        }
    }

    /**
     * Releases holds nothing is going to finish.
     *
     * <p>A held arena is out of rotation, so a hold that outlives its job - a
     * regen delay whose task never ran, say, because the arena was deleted in
     * the meantime - would quietly remove an arena from the server for good.
     * Two minutes is far longer than any real pass or delay.
     */
    private void sweepStaleHolds() {
        if (this.regenerating.isEmpty()) {
            return;
        }
        long cutoff = System.currentTimeMillis() - 120_000L;
        java.util.Iterator<Map.Entry<String, Long>> iterator = this.regenerating.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (entry.getValue() > cutoff) continue;
            boolean queued = false;
            for (RegenJob job : this.regenJobs) {
                if (job.key.equals(entry.getKey())) {
                    queued = true;
                    break;
                }
            }
            if (queued) continue;
            iterator.remove();
            this.plugin.getLogger().info("Released arena '" + entry.getKey() + "' - it was held for a regen that never ran.");
        }
    }

    private void finish(RegenJob job) {
        if (!job.holdsArena) {
            return;
        }
        this.regenerating.remove(job.key);
        this.clearDirty(job.name);
        if (job.whenDone != null) {
            try {
                job.whenDone.run();
            }
            catch (Throwable throwable) {
                this.plugin.getLogger().warning("After regenerating '" + job.name + "': " + throwable);
            }
        }
    }

    /** One arena's snapshot pass, resumable across ticks. */
    private static final class RegenJob {
        final String key;
        final String name;
        final World world;
        final RegionClipboard clip;
        final int ox;
        final int oy;
        final int oz;
        final boolean holdsArena;
        final Runnable whenDone;
        int cursor;
        final int[] written = new int[1];

        RegenJob(String key, String name, World world, RegionClipboard clip, int ox, int oy, int oz,
                 boolean holdsArena, Runnable whenDone) {
            this.key = key;
            this.name = name;
            this.world = world;
            this.clip = clip;
            this.ox = ox;
            this.oy = oy;
            this.oz = oz;
            this.holdsArena = holdsArena;
            this.whenDone = whenDone;
        }

        /** True once the whole region has been checked. */
        boolean step(long deadline) {
            this.cursor = this.clip.pasteSome(this.world, this.ox, this.oy, this.oz, this.cursor, deadline, this.written);
            return this.cursor >= this.clip.volume();
        }
    }

    private void writeSnapshotToDisk(String string, RegionClipboard regionClipboard) {
        File file = this.snapshotFile(string);
        // Write to a temp file and move it into place, so a crash mid-write
        // cannot leave a truncated snapshot that regenerates half an arena.
        File temp = new File(file.getParentFile(), file.getName() + ".tmp");
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(temp))));){
            regionClipboard.write(bufferedWriter);
        }
        catch (IOException iOException) {
            this.plugin.getLogger().severe("Failed to write snapshot for arena " + string + ": " + iOException.getMessage());
            temp.delete();
            return;
        }
        try {
            java.nio.file.Files.move(temp.toPath(), file.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException atomicUnsupported) {
            try {
                java.nio.file.Files.move(temp.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException iOException) {
                this.plugin.getLogger().severe("Failed to save snapshot for arena " + string + ": " + iOException.getMessage());
            }
        }
    }

    private RegionClipboard readSnapshotFromDisk(String string) {
        File file = this.snapshotFile(string);
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));){
            return RegionClipboard.read(bufferedReader);
        }
        catch (IOException | RuntimeException exception) {
            this.plugin.getLogger().severe("Failed to read snapshot for arena " + string + ": " + exception.getMessage());
            return null;
        }
    }

    public boolean copyToClipboard(UUID uUID, Arena arena) {
        if (!arena.isConfigured()) {
            return false;
        }
        this.clipboards.put(uUID, RegionClipboard.captureArena(arena));
        return true;
    }

    public boolean hasClipboard(UUID uUID) {
        return this.clipboards.containsKey(uUID);
    }

    public PasteResult pasteAsNewArena(UUID uUID, Location location) {
        int n;
        RegionClipboard regionClipboard = this.clipboards.get(uUID);
        if (regionClipboard == null) {
            return null;
        }
        Arena arena = this.get(regionClipboard.sourceName);
        if (arena == null || !arena.isConfigured() || arena.getWorld() == null) {
            return null;
        }
        String string = this.nextCloneName(regionClipboard.sourceName);
        World world = arena.getWorld();
        int n2 = this.nextCloneX(regionClipboard, world);
        int n3 = regionClipboard.originY;
        try {
            int n4 = world.getMaxHeight() - 1;
            n = world.getMinHeight();
            if (n3 + regionClipboard.sizeY - 1 > n4) {
                n3 = n4 - (regionClipboard.sizeY - 1);
            }
            if (n3 < n) {
                n3 = n;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        Location location2 = new Location(world, (double)n2, (double)n3, (double)regionClipboard.originZ);
        n = regionClipboard.pasteAt(world, location2, true);
        Arena arena2 = new Arena(string);
        int n5 = location2.getBlockX() - regionClipboard.originX;
        int n6 = location2.getBlockY() - regionClipboard.originY;
        int n7 = location2.getBlockZ() - regionClipboard.originZ;
        arena2.setCorner1(ArenaManager.offset(regionClipboard.corner1, world, n5, n6, n7));
        arena2.setCorner2(ArenaManager.offset(regionClipboard.corner2, world, n5, n6, n7));
        arena2.setSpawn1(ArenaManager.offsetWithRotation(regionClipboard.spawn1, world, n5, n6, n7));
        arena2.setSpawn2(ArenaManager.offsetWithRotation(regionClipboard.spawn2, world, n5, n6, n7));
        if (regionClipboard.eventSpawn != null) {
            arena2.setEventSpawn(ArenaManager.offsetWithRotation(regionClipboard.eventSpawn, world, n5, n6, n7));
        }
        if (regionClipboard.borderCorner1 != null) {
            arena2.setBorderCorner1(ArenaManager.offset(regionClipboard.borderCorner1, world, n5, n6, n7));
        }
        if (regionClipboard.borderCorner2 != null) {
            arena2.setBorderCorner2(ArenaManager.offset(regionClipboard.borderCorner2, world, n5, n6, n7));
        }
        if (arena != null) {
            arena2.copySettingsFrom(arena);
        }
        this.arenas.put(string.toLowerCase(Locale.ROOT), arena2);
        this.snapshotArena(arena2);
        this.save();
        return new PasteResult(string, n);
    }

    private int nextCloneX(RegionClipboard regionClipboard, World world) {
        boolean bl;
        int n = Math.max(1, regionClipboard.sizeX);
        int n2 = regionClipboard.originX + n + 16;
        block0: do {
            bl = false;
            int n3 = n2 + n - 1;
            int n4 = regionClipboard.originY;
            int n5 = n4 + regionClipboard.sizeY - 1;
            int n6 = regionClipboard.originZ;
            int n7 = n6 + regionClipboard.sizeZ - 1;
            for (Arena arena : this.arenas.values()) {
                boolean bl2;
                if (arena == null || !arena.isConfigured() || arena.getWorld() == null || !world.getName().equals(arena.getWorld().getName())) continue;
                Location location = arena.getMin();
                Location location2 = arena.getMax();
                if (location == null || location2 == null || !(bl2 = n2 <= location2.getBlockX() && n3 >= location.getBlockX() && n4 <= location2.getBlockY() && n5 >= location.getBlockY() && n6 <= location2.getBlockZ() && n7 >= location.getBlockZ())) continue;
                n2 = location2.getBlockX() + 16 + 1;
                bl = true;
                continue block0;
            }
        } while (bl);
        return n2;
    }

    private static Location offset(Location location, World world, int n, int n2, int n3) {
        return new Location(world, (double)(location.getBlockX() + n), (double)(location.getBlockY() + n2), (double)(location.getBlockZ() + n3));
    }

    private static Location offsetWithRotation(Location location, World world, int n, int n2, int n3) {
        Location location2 = new Location(world, (double)(location.getBlockX() + n) + 0.5, (double)(location.getBlockY() + n2), (double)(location.getBlockZ() + n3) + 0.5);
        location2.setYaw(location.getYaw());
        location2.setPitch(location.getPitch());
        return location2;
    }

    private String nextCloneName(String string) {
        String string2 = string;
        Matcher matcher = TRAILING_NUMBER.matcher(string);
        if (matcher.matches() && !matcher.group(1).isEmpty()) {
            string2 = matcher.group(1);
        }
        int n = 2;
        while (this.exists(string2 + n)) {
            ++n;
        }
        return string2 + n;
    }

    /**
     * A region's blocks, stored as a palette rather than one string per block.
     *
     * <p>The old form kept a {@code String} for every block - six hundred
     * thousand of them for a 100x60x100 arena, each its own object, read back
     * in from disk as distinct copies. Across a few dozen arenas that is
     * gigabytes of heap the garbage collector has to keep walking, which is
     * lag that no amount of RAM fixes. Here each distinct block state is held
     * once and every block is an int index into that list.
     *
     * <p>The on-disk format is unchanged - one line per block - so snapshots
     * written by earlier builds load as they are.
     */
    public static final class RegionClipboard {
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;
        /** Distinct block states; an index of -1 means "leave this block alone". */
        private final String[] palette;
        private final int[] indices;
        /** Resolved lazily, on the main thread, the first time this is pasted. */
        private BlockData[] resolved;
        private Material[] materials;
        private boolean[] stateful;
        private String sourceName;
        private int originX;
        private int originY;
        private int originZ;
        private Location corner1;
        private Location corner2;
        private Location spawn1;
        private Location spawn2;
        private Location eventSpawn;
        private Location borderCorner1;
        private Location borderCorner2;

        private RegionClipboard(int n, int n2, int n3, String[] palette, int[] indices) {
            this.sizeX = n;
            this.sizeY = n2;
            this.sizeZ = n3;
            this.palette = palette;
            this.indices = indices;
        }

        int volume() {
            return this.indices.length;
        }

        /** Builds the palette while blocks are read, in the same y/x/z order as always. */
        private static final class Builder {
            private final Map<String, Integer> seen = new HashMap<String, Integer>();
            private final List<String> palette = new ArrayList<String>();

            int indexOf(String state) {
                if (state == null || state.isEmpty()) {
                    return -1;
                }
                Integer known = this.seen.get(state);
                if (known != null) {
                    return known;
                }
                int index = this.palette.size();
                this.palette.add(state);
                this.seen.put(state, index);
                return index;
            }

            String[] palette() {
                return this.palette.toArray(new String[0]);
            }
        }

        static RegionClipboard capture(World world, Location location, Location location2) {
            int n = location2.getBlockX() - location.getBlockX() + 1;
            int n2 = location2.getBlockY() - location.getBlockY() + 1;
            int n3 = location2.getBlockZ() - location.getBlockZ() + 1;
            int[] indices = new int[n * n2 * n3];
            Builder builder = new Builder();
            int n4 = 0;
            for (int i = 0; i < n2; ++i) {
                for (int j = 0; j < n; ++j) {
                    for (int k = 0; k < n3; ++k) {
                        Block block = world.getBlockAt(location.getBlockX() + j, location.getBlockY() + i, location.getBlockZ() + k);
                        indices[n4++] = builder.indexOf(block.getBlockData().getAsString());
                    }
                }
            }
            return new RegionClipboard(n, n2, n3, builder.palette(), indices);
        }

        /** Reads the line-per-block form. Safe off the main thread. */
        static RegionClipboard read(BufferedReader reader) throws IOException {
            String header = reader.readLine();
            if (header == null) {
                return null;
            }
            String[] size = header.trim().split(" ");
            int n = Integer.parseInt(size[0]);
            int n2 = Integer.parseInt(size[1]);
            int n3 = Integer.parseInt(size[2]);
            int[] indices = new int[n * n2 * n3];
            Builder builder = new Builder();
            for (int i = 0; i < indices.length; ++i) {
                indices[i] = builder.indexOf(reader.readLine());
            }
            return new RegionClipboard(n, n2, n3, builder.palette(), indices);
        }

        /** Writes the line-per-block form. Safe off the main thread: nothing here mutates. */
        void write(BufferedWriter writer) throws IOException {
            writer.write(this.sizeX + " " + this.sizeY + " " + this.sizeZ);
            writer.newLine();
            for (int index : this.indices) {
                if (index >= 0) {
                    writer.write(this.palette[index]);
                }
                writer.newLine();
            }
        }

        static RegionClipboard captureArena(Arena arena) {
            RegionClipboard regionClipboard = RegionClipboard.capture(arena.getWorld(), arena.getMin(), arena.getMax());
            regionClipboard.sourceName = arena.getName();
            Location location = arena.getMin();
            regionClipboard.originX = location.getBlockX();
            regionClipboard.originY = location.getBlockY();
            regionClipboard.originZ = location.getBlockZ();
            regionClipboard.corner1 = arena.getCorner1().clone();
            regionClipboard.corner2 = arena.getCorner2().clone();
            regionClipboard.spawn1 = arena.getSpawn1().clone();
            regionClipboard.spawn2 = arena.getSpawn2().clone();
            regionClipboard.eventSpawn = arena.getEventSpawn() == null ? null : arena.getEventSpawn().clone();
            regionClipboard.borderCorner1 = arena.getBorderCorner1() == null ? null : arena.getBorderCorner1().clone();
            regionClipboard.borderCorner2 = arena.getBorderCorner2() == null ? null : arena.getBorderCorner2().clone();
            return regionClipboard;
        }

        /** Turns the palette into BlockData once. Main thread only. */
        private void resolve() {
            if (this.resolved != null) {
                return;
            }
            BlockData[] blockDatas = new BlockData[this.palette.length];
            Material[] mats = new Material[this.palette.length];
            boolean[] states = new boolean[this.palette.length];
            for (int i = 0; i < this.palette.length; ++i) {
                BlockData blockData;
                try {
                    blockData = Bukkit.createBlockData((String)this.palette[i]);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    blockData = Material.AIR.createBlockData();
                }
                blockDatas[i] = blockData;
                mats[i] = blockData.getMaterial();
                // A state with no [properties] is fully described by its
                // material, so a matching type means a matching block and the
                // full BlockData comparison - which allocates - can be skipped.
                states[i] = this.palette[i].indexOf('[') >= 0;
            }
            this.materials = mats;
            this.stateful = states;
            this.resolved = blockDatas;
        }

        /**
         * Puts one block back if it differs from the snapshot.
         *
         * <p>Compares the cheap way first: Block.getType() reads the material
         * without building a BlockData object. Only blocks with properties -
         * stairs, slabs, doors - need the full comparison, and only when the
         * type already matches. Most of an arena is untouched stone or air,
         * which this settles without allocating at all.
         */
        private boolean restoreAt(World world, int x, int y, int z, int index, boolean force) {
            Block block = world.getBlockAt(x, y, z);
            BlockData want = this.resolved[index];
            if (!force && block.getType() == this.materials[index]
                    && (!this.stateful[index] || want.equals(block.getBlockData()))) {
                return false;
            }
            block.setBlockData(want, false);
            return true;
        }

        int pasteAt(World world, Location location) {
            return this.pasteAt(world, location, false);
        }

        /** The whole region in one go. For admin actions and startup recovery only. */
        int pasteAt(World world, Location location, boolean force) {
            this.resolve();
            int ox = location.getBlockX();
            int oy = location.getBlockY();
            int oz = location.getBlockZ();
            int written = 0;
            int cursor = 0;
            for (int i = 0; i < this.sizeY; ++i) {
                for (int j = 0; j < this.sizeX; ++j) {
                    for (int k = 0; k < this.sizeZ; ++k) {
                        int index = this.indices[cursor++];
                        if (index < 0) continue;
                        if (this.restoreAt(world, ox + j, oy + i, oz + k, index, force)) {
                            ++written;
                        }
                    }
                }
            }
            return written;
        }

        /**
         * Restores from {@code cursor} until the deadline, and returns where
         * it stopped - {@code volume()} once the region is done.
         *
         * <p>The time check runs every 256 blocks rather than every block:
         * System.nanoTime() is not free, and 256 cheap compares is still far
         * finer than the budget it is measuring.
         */
        int pasteSome(World world, int ox, int oy, int oz, int cursor, long deadlineNanos, int[] written) {
            this.resolve();
            int layer = this.sizeX * this.sizeZ;
            int total = this.indices.length;
            while (cursor < total) {
                int index = this.indices[cursor];
                if (index >= 0) {
                    int y = cursor / layer;
                    int rem = cursor - y * layer;
                    int x = rem / this.sizeZ;
                    int z = rem - x * this.sizeZ;
                    if (this.restoreAt(world, ox + x, oy + y, oz + z, index, false)) {
                        ++written[0];
                    }
                }
                ++cursor;
                if ((cursor & 255) == 0 && System.nanoTime() >= deadlineNanos) {
                    break;
                }
            }
            return cursor;
        }
    }

    public static final class PasteResult {
        public final String arenaName;
        public final int blocksWritten;

        PasteResult(String string, int n) {
            this.arenaName = string;
            this.blocksWritten = n;
        }
    }
}

