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
    private final Map<String, RegionClipboard> snapshots = new HashMap<String, RegionClipboard>();
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

    private void saveDirty() {
        try {
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.set("dirty", new ArrayList<String>(this.dirty));
            yamlConfiguration.save(this.dirtyFile);
        }
        catch (Throwable throwable) {
            this.plugin.getLogger().warning("Could not write arenas-dirty.yml: " + String.valueOf(throwable));
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
            if (!arena.isConfigured() || !arena.isEnabled() || predicate.test(arena)) continue;
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
        this.writeSnapshotToDisk(arena.getName(), regionClipboard);
        return true;
    }

    public int regenArena(Arena arena) {
        if (!arena.isConfigured() || arena.getWorld() == null) {
            return -1;
        }
        RegionClipboard regionClipboard = this.snapshots.get(arena.getName().toLowerCase(Locale.ROOT));
        if (regionClipboard == null && (regionClipboard = this.readSnapshotFromDisk(arena.getName())) != null) {
            this.snapshots.put(arena.getName().toLowerCase(Locale.ROOT), regionClipboard);
        }
        if (regionClipboard == null) {
            return -1;
        }
        return regionClipboard.pasteAt(arena.getWorld(), arena.getMin());
    }

    private void writeSnapshotToDisk(String string, RegionClipboard regionClipboard) {
        File file = this.snapshotFile(string);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))));){
            bufferedWriter.write(regionClipboard.sizeX + " " + regionClipboard.sizeY + " " + regionClipboard.sizeZ);
            bufferedWriter.newLine();
            for (String string2 : regionClipboard.data) {
                bufferedWriter.write(string2 == null ? "" : string2);
                bufferedWriter.newLine();
            }
        }
        catch (IOException iOException) {
            this.plugin.getLogger().severe("Failed to write snapshot for arena " + string + ": " + iOException.getMessage());
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private RegionClipboard readSnapshotFromDisk(String string) {
        File file = this.snapshotFile(string);
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));){
            RegionClipboard regionClipboard;
            String object;
            String string2 = bufferedReader.readLine();
            if (string2 == null) {
                RegionClipboard regionClipboard2;
                RegionClipboard regionClipboard3 = null;
                RegionClipboard regionClipboard4 = regionClipboard2 = null;
                return regionClipboard4;
            }
            String[] stringArray = string2.split(" ");
            int n = Integer.parseInt(stringArray[0]);
            int n2 = Integer.parseInt(stringArray[1]);
            int n3 = Integer.parseInt(stringArray[2]);
            String[] stringArray2 = new String[n * n2 * n3];
            for (int i = 0; i < stringArray2.length; ++i) {
                object = bufferedReader.readLine();
                stringArray2[i] = object == null || object.isEmpty() ? null : object;
            }
            RegionClipboard regionClipboard5 = regionClipboard = new RegionClipboard(n, n2, n3, stringArray2);
            return regionClipboard5;
        }
        catch (IOException iOException) {
            this.plugin.getLogger().severe("Failed to read snapshot for arena " + string + ": " + iOException.getMessage());
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

    public static final class RegionClipboard {
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;
        private final String[] data;
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

        private RegionClipboard(int n, int n2, int n3, String[] stringArray) {
            this.sizeX = n;
            this.sizeY = n2;
            this.sizeZ = n3;
            this.data = stringArray;
        }

        static RegionClipboard capture(World world, Location location, Location location2) {
            int n = location2.getBlockX() - location.getBlockX() + 1;
            int n2 = location2.getBlockY() - location.getBlockY() + 1;
            int n3 = location2.getBlockZ() - location.getBlockZ() + 1;
            String[] stringArray = new String[n * n2 * n3];
            int n4 = 0;
            for (int i = 0; i < n2; ++i) {
                for (int j = 0; j < n; ++j) {
                    for (int k = 0; k < n3; ++k) {
                        Block block = world.getBlockAt(location.getBlockX() + j, location.getBlockY() + i, location.getBlockZ() + k);
                        stringArray[n4++] = block.getBlockData().getAsString();
                    }
                }
            }
            return new RegionClipboard(n, n2, n3, stringArray);
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

        int pasteAt(World world, Location location) {
            return this.pasteAt(world, location, false);
        }

        int pasteAt(World world, Location location, boolean bl) {
            int n = location.getBlockX();
            int n2 = location.getBlockY();
            int n3 = location.getBlockZ();
            int n4 = 0;
            int n5 = 0;
            HashMap<String, BlockData> hashMap = new HashMap<String, BlockData>();
            for (int i = 0; i < this.sizeY; ++i) {
                for (int j = 0; j < this.sizeX; ++j) {
                    for (int k = 0; k < this.sizeZ; ++k) {
                        String string;
                        if ((string = this.data[n5++]) == null) continue;
                        Block block = world.getBlockAt(n + j, n2 + i, n3 + k);
                        if (!bl && string.equals(block.getBlockData().getAsString())) continue;
                        BlockData blockData = (BlockData)hashMap.get(string);
                        if (blockData == null) {
                            try {
                                blockData = Bukkit.createBlockData((String)string);
                            }
                            catch (IllegalArgumentException illegalArgumentException) {
                                blockData = Material.AIR.createBlockData();
                            }
                            hashMap.put(string, blockData);
                        }
                        block.setBlockData(blockData, false);
                        ++n4;
                    }
                }
            }
            return n4;
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

