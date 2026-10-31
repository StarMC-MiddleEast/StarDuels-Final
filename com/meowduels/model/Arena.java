package com.meowduels.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class Arena {
    private final String name;
    private String worldName;
    private Location spawn1;
    private Location spawn2;
    private Location corner1;
    private Location corner2;
    private Location eventSpawn;
    private Location borderCorner1;
    private Location borderCorner2;
    private int eventBorderEnd = 30;
    private int eventBorderInterval = 120;
    private int eventBorderStep = 20;
    private boolean autoRegenerate = true;
    private int regenDelayTicks = 0;
    private boolean allowBuild = false;
    private boolean allowBreak = false;
    private boolean allowBreakInner = false;
    private boolean allowBreakWalls = false;
    private boolean allowBreakRoof = false;
    private boolean allowPlaceInner = false;
    private boolean allowPlaceWalls = false;
    private boolean allowPlaceRoof = false;
    private boolean allowRemoveAdded = true;
    private int roofMargin = 1;
    private int wallMargin = 0;
    private boolean allowExplosions = false;
    private boolean allowFireSpread = false;
    private boolean allowLiquidFlow = true;
    private boolean allowMobSpawns = false;
    private boolean allowFallDamage = true;
    private boolean allowHungerLoss = false;
    private boolean saturationOnEat = false;
    private boolean lockClearWeather = false;
    private boolean lockDayTime = false;
    private boolean voidKillEnabled = false;
    private int voidKillY = -64;
    private boolean borderParticles = false;
    private boolean glowingOpponent = false;
    private boolean enabled = true;
    private boolean cloningEnabled = true;
    private boolean allowSpectators = true;
    private int countdownOverride = -1;
    private String description = "";
    private final Set<String> kits = new LinkedHashSet<String>();

    public Arena(String string) {
        this.name = string;
    }

    public String getName() {
        return this.name;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean isConfigured() {
        World world = this.getWorld();
        if (world == null) return false;
        if (!Arena.inWorld(this.getSpawn1(), world)) return false;
        if (!Arena.inWorld(this.getSpawn2(), world)) return false;
        if (!Arena.inWorld(this.getCorner1(), world)) return false;
        if (!Arena.inWorld(this.getCorner2(), world)) return false;
        return true;
    }

    public World getWorld() {
        return this.worldName == null ? null : Bukkit.getWorld((String)this.worldName);
    }

    public String getWorldName() {
        return this.worldName;
    }

    public Location getSpawn1() {
        return this.resolveWorld(this.spawn1);
    }

    public Location getSpawn2() {
        return this.resolveWorld(this.spawn2);
    }

    public Location getEventSpawn() {
        return this.resolveWorld(this.eventSpawn);
    }

    public void setEventSpawn(Location location) {
        this.eventSpawn = location == null ? null : location.clone();
    }

    public boolean isEventReady() {
        return this.isConfigured() && this.eventSpawn != null;
    }

    public Location getBorderCorner1() {
        return this.resolveWorld(this.borderCorner1);
    }

    public Location getBorderCorner2() {
        return this.resolveWorld(this.borderCorner2);
    }

    private Location resolveWorld(Location location) {
        if (location == null || location.getWorld() != null) {
            return location;
        }
        World world = this.getWorld();
        if (world == null) {
            return location;
        }
        return new Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    private static boolean inWorld(Location location, World world) {
        return location != null && location.getWorld() != null && location.getWorld().equals((Object)world);
    }

    public void setBorderCorner1(Location location) {
        this.borderCorner1 = location == null ? null : location.clone();
    }

    public void setBorderCorner2(Location location) {
        this.borderCorner2 = location == null ? null : location.clone();
    }

    public boolean hasBorderRegion() {
        return this.borderCorner1 != null && this.borderCorner2 != null;
    }

    public double getBorderCenterX() {
        if (this.hasBorderRegion()) {
            return (double)(this.borderCorner1.getBlockX() + this.borderCorner2.getBlockX()) / 2.0 + 0.5;
        }
        Location location = this.getMin();
        Location location2 = this.getMax();
        return (double)(location.getBlockX() + location2.getBlockX()) / 2.0 + 0.5;
    }

    public double getBorderCenterZ() {
        if (this.hasBorderRegion()) {
            return (double)(this.borderCorner1.getBlockZ() + this.borderCorner2.getBlockZ()) / 2.0 + 0.5;
        }
        Location location = this.getMin();
        Location location2 = this.getMax();
        return (double)(location.getBlockZ() + location2.getBlockZ()) / 2.0 + 0.5;
    }

    public double getBorderStartSize() {
        if (this.hasBorderRegion()) {
            double d = Math.abs(this.borderCorner1.getBlockX() - this.borderCorner2.getBlockX()) + 1;
            double d2 = Math.abs(this.borderCorner1.getBlockZ() - this.borderCorner2.getBlockZ()) + 1;
            return Math.max(d, d2);
        }
        Location location = this.getMin();
        Location location2 = this.getMax();
        double d = Math.abs(location2.getBlockX() - location.getBlockX()) + 1;
        double d3 = Math.abs(location2.getBlockZ() - location.getBlockZ()) + 1;
        return Math.max(d, d3);
    }

    public int getEventBorderEnd() {
        return this.eventBorderEnd;
    }

    public void setEventBorderEnd(int n) {
        this.eventBorderEnd = Math.max(1, n);
    }

    public int getEventBorderInterval() {
        return this.eventBorderInterval;
    }

    public void setEventBorderInterval(int n) {
        this.eventBorderInterval = Math.max(5, n);
    }

    public int getEventBorderStep() {
        return this.eventBorderStep;
    }

    public void setEventBorderStep(int n) {
        this.eventBorderStep = Math.max(1, n);
    }

    public boolean overlaps(Arena arena) {
        if (arena == null || arena == this || this.worldName == null || !this.worldName.equals(arena.worldName)) {
            return false;
        }
        Location location = this.getMin();
        Location location2 = this.getMax();
        Location location3 = arena.getMin();
        Location location4 = arena.getMax();
        if (location == null || location2 == null || location3 == null || location4 == null) {
            return false;
        }
        return location.getBlockX() <= location4.getBlockX() && location2.getBlockX() >= location3.getBlockX() && location.getBlockY() <= location4.getBlockY() && location2.getBlockY() >= location3.getBlockY() && location.getBlockZ() <= location4.getBlockZ() && location2.getBlockZ() >= location3.getBlockZ();
    }

    public Location getCorner1() {
        return this.resolveWorld(this.corner1);
    }

    public Location getCorner2() {
        return this.resolveWorld(this.corner2);
    }

    public void setSpawn1(Location location) {
        this.spawn1 = location.clone();
        this.worldName = location.getWorld().getName();
    }

    public void setSpawn2(Location location) {
        this.spawn2 = location.clone();
        this.worldName = location.getWorld().getName();
    }

    public void setCorner1(Location location) {
        this.corner1 = location.clone();
        this.worldName = location.getWorld().getName();
    }

    public void setCorner2(Location location) {
        this.corner2 = location.clone();
        this.worldName = location.getWorld().getName();
    }

    /** True once both corners are set. With minX/maxX/minZ/maxZ, for hot paths that must not allocate. */
    public boolean hasCorners() {
        return this.corner1 != null && this.corner2 != null;
    }

    public int minX() {
        return Math.min(this.corner1.getBlockX(), this.corner2.getBlockX());
    }

    public int maxX() {
        return Math.max(this.corner1.getBlockX(), this.corner2.getBlockX());
    }

    public int minY() { return Math.min(this.corner1.getBlockY(), this.corner2.getBlockY()); }

    public int maxY() { return Math.max(this.corner1.getBlockY(), this.corner2.getBlockY()); }

    public int minZ() {
        return Math.min(this.corner1.getBlockZ(), this.corner2.getBlockZ());
    }

    public int maxZ() {
        return Math.max(this.corner1.getBlockZ(), this.corner2.getBlockZ());
    }

    public Location getMin() {
        if (this.corner1 == null || this.corner2 == null) {
            return null;
        }
        World world = this.getWorld();
        return new Location(world, (double)Math.min(this.corner1.getBlockX(), this.corner2.getBlockX()), (double)Math.min(this.corner1.getBlockY(), this.corner2.getBlockY()), (double)Math.min(this.corner1.getBlockZ(), this.corner2.getBlockZ()));
    }

    public Location getMax() {
        if (this.corner1 == null || this.corner2 == null) {
            return null;
        }
        World world = this.getWorld();
        return new Location(world, (double)Math.max(this.corner1.getBlockX(), this.corner2.getBlockX()), (double)Math.max(this.corner1.getBlockY(), this.corner2.getBlockY()), (double)Math.max(this.corner1.getBlockZ(), this.corner2.getBlockZ()));
    }

    /**
     * Resolves a configured spawn to a safe player standing position. Older
     * arena configs often record the floor block's Y as the spawn Y, which
     * places the player inside the floor and lets vanilla collision push them
     * onto the arena roof. Keep the configured X/Z and facing, correcting Y
     * only when the exact location has no solid floor and two clear blocks.
     */
    public Location safeSpawn(Location configured) {
        if (configured == null || configured.getWorld() == null || !this.hasCorners()) {
            return configured == null ? null : configured.clone();
        }
        World world = this.getWorld();
        if (world == null || !world.equals(configured.getWorld())) {
            return configured.clone();
        }
        int x = configured.getBlockX();
        int z = configured.getBlockZ();
        int lowY = Math.min(this.corner1.getBlockY(), this.corner2.getBlockY());
        int highY = Math.min(Math.max(this.corner1.getBlockY(), this.corner2.getBlockY()), world.getMaxHeight() - 3);
        int feetY = configured.getBlockY();
        if (isSafeStandingColumn(world, x, feetY, z)) {
            return configured.clone();
        }
        for (int floorY = lowY; floorY <= highY; floorY++) {
            if (!isSafeStandingColumn(world, x, floorY + 1, z)) continue;
            Location safe = configured.clone();
            safe.setY(floorY + 1.0);
            return safe;
        }
        return configured.clone();
    }

    private static boolean isSafeStandingColumn(World world, int x, int feetY, int z) {
        if (feetY <= world.getMinHeight() || feetY + 1 >= world.getMaxHeight()) return false;
        return world.getBlockAt(x, feetY - 1, z).getType().isSolid()
                && world.getBlockAt(x, feetY, z).isPassable()
                && world.getBlockAt(x, feetY + 1, z).isPassable();
    }

    /**
     * Whether a location is inside this arena's box.
     *
     * <p>Called for every arena on every block break, place, burn and - most
     * of all - every liquid flow tick in an arena world, so the cheap checks
     * go first and nothing is allocated. It used to run isConfigured() (two
     * world lookups and two Location clones) and build two new Locations
     * before comparing a single coordinate.
     */
    public boolean contains(Location location) {
        Location a = this.corner1;
        Location b = this.corner2;
        if (a == null || b == null || this.worldName == null || location == null) {
            return false;
        }
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        int ax = a.getBlockX();
        int bx = b.getBlockX();
        if (x < Math.min(ax, bx) || x > Math.max(ax, bx)) {
            return false;
        }
        int az = a.getBlockZ();
        int bz = b.getBlockZ();
        if (z < Math.min(az, bz) || z > Math.max(az, bz)) {
            return false;
        }
        int ay = a.getBlockY();
        int by = b.getBlockY();
        if (y < Math.min(ay, by) || y > Math.max(ay, by)) {
            return false;
        }
        World world = location.getWorld();
        if (world == null || !world.getName().equals(this.worldName)) {
            return false;
        }
        return this.isConfigured();
    }

    public Zone classify(Location location) {
        boolean bl;
        Location location2 = this.getMin();
        Location location3 = this.getMax();
        if (location2 == null || location3 == null) {
            return Zone.INNER;
        }
        int n = location.getBlockX();
        int n2 = location.getBlockY();
        int n3 = location.getBlockZ();
        int n4 = location3.getBlockY() - Math.max(0, this.roofMargin) + 1;
        if (n2 >= n4) {
            return Zone.ROOF;
        }
        int n5 = Math.max(0, this.wallMargin);
        boolean bl2 = bl = n <= location2.getBlockX() + n5 || n >= location3.getBlockX() - n5 || n3 <= location2.getBlockZ() + n5 || n3 >= location3.getBlockZ() - n5;
        if (bl) {
            return Zone.WALL;
        }
        return Zone.INNER;
    }

    public boolean canBreak(Location location) {
        return this.allowBreak;
    }

    public boolean canPlace(Location location) {
        return this.allowBuild;
    }

    public boolean isAllowBuild() {
        return this.allowBuild;
    }

    public void setAllowBuild(boolean bl) {
        this.allowBuild = bl;
    }

    public boolean isAllowBreak() {
        return this.allowBreak;
    }

    public void setAllowBreak(boolean bl) {
        this.allowBreak = bl;
    }

    public void copySettingsFrom(Arena arena) {
        this.autoRegenerate = arena.autoRegenerate;
        this.regenDelayTicks = arena.regenDelayTicks;
        this.eventBorderEnd = arena.eventBorderEnd;
        this.eventBorderInterval = arena.eventBorderInterval;
        this.eventBorderStep = arena.eventBorderStep;
        this.allowBuild = arena.allowBuild;
        this.allowBreak = arena.allowBreak;
        this.allowBreakInner = arena.allowBreakInner;
        this.allowBreakWalls = arena.allowBreakWalls;
        this.allowBreakRoof = arena.allowBreakRoof;
        this.allowPlaceInner = arena.allowPlaceInner;
        this.allowPlaceWalls = arena.allowPlaceWalls;
        this.allowPlaceRoof = arena.allowPlaceRoof;
        this.allowRemoveAdded = arena.allowRemoveAdded;
        this.roofMargin = arena.roofMargin;
        this.wallMargin = arena.wallMargin;
        this.allowExplosions = arena.allowExplosions;
        this.allowFireSpread = arena.allowFireSpread;
        this.allowLiquidFlow = arena.allowLiquidFlow;
        this.allowMobSpawns = arena.allowMobSpawns;
        this.allowFallDamage = arena.allowFallDamage;
        this.saturationOnEat = arena.saturationOnEat;
        this.allowHungerLoss = arena.allowHungerLoss;
        this.lockClearWeather = arena.lockClearWeather;
        this.lockDayTime = arena.lockDayTime;
        this.voidKillEnabled = arena.voidKillEnabled;
        this.voidKillY = arena.voidKillY;
        this.borderParticles = arena.borderParticles;
        this.glowingOpponent = arena.glowingOpponent;
        this.enabled = arena.enabled;
        this.cloningEnabled = arena.cloningEnabled;
        this.allowSpectators = arena.allowSpectators;
        this.countdownOverride = arena.countdownOverride;
        this.description = arena.description;
        this.kits.clear();
        this.kits.addAll(arena.kits);
    }

    public boolean isAutoRegenerate() {
        return this.autoRegenerate;
    }

    public void setAutoRegenerate(boolean bl) {
        this.autoRegenerate = bl;
    }

    public int getRegenDelayTicks() {
        return this.regenDelayTicks;
    }

    public void setRegenDelayTicks(int n) {
        this.regenDelayTicks = Math.max(0, n);
    }

    public boolean isAllowBreakInner() {
        return this.allowBreakInner;
    }

    public void setAllowBreakInner(boolean bl) {
        this.allowBreakInner = bl;
    }

    public boolean isAllowBreakWalls() {
        return this.allowBreakWalls;
    }

    public void setAllowBreakWalls(boolean bl) {
        this.allowBreakWalls = bl;
    }

    public boolean isAllowBreakRoof() {
        return this.allowBreakRoof;
    }

    public void setAllowBreakRoof(boolean bl) {
        this.allowBreakRoof = bl;
    }

    public boolean isAllowPlaceInner() {
        return this.allowPlaceInner;
    }

    public void setAllowPlaceInner(boolean bl) {
        this.allowPlaceInner = bl;
    }

    public boolean isAllowPlaceWalls() {
        return this.allowPlaceWalls;
    }

    public void setAllowPlaceWalls(boolean bl) {
        this.allowPlaceWalls = bl;
    }

    public boolean isAllowPlaceRoof() {
        return this.allowPlaceRoof;
    }

    public void setAllowPlaceRoof(boolean bl) {
        this.allowPlaceRoof = bl;
    }

    public boolean isAllowRemoveAdded() {
        return this.allowRemoveAdded;
    }

    public void setAllowRemoveAdded(boolean bl) {
        this.allowRemoveAdded = bl;
    }

    public int getRoofMargin() {
        return this.roofMargin;
    }

    public void setRoofMargin(int n) {
        this.roofMargin = Math.max(0, n);
    }

    public int getWallMargin() {
        return this.wallMargin;
    }

    public void setWallMargin(int n) {
        this.wallMargin = Math.max(0, n);
    }

    public boolean isAllowExplosions() {
        return this.allowExplosions;
    }

    public void setAllowExplosions(boolean bl) {
        this.allowExplosions = bl;
    }

    public boolean isAllowFireSpread() {
        return this.allowFireSpread;
    }

    public void setAllowFireSpread(boolean bl) {
        this.allowFireSpread = bl;
    }

    public boolean isAllowLiquidFlow() {
        return this.allowLiquidFlow;
    }

    public void setAllowLiquidFlow(boolean bl) {
        this.allowLiquidFlow = bl;
    }

    public boolean isAllowMobSpawns() {
        return this.allowMobSpawns;
    }

    public void setAllowMobSpawns(boolean bl) {
        this.allowMobSpawns = bl;
    }

    public boolean isAllowFallDamage() {
        return this.allowFallDamage;
    }

    public void setAllowFallDamage(boolean bl) {
        this.allowFallDamage = bl;
    }

    public boolean isAllowHungerLoss() {
        return this.allowHungerLoss;
    }

    public void setAllowHungerLoss(boolean bl) {
        this.allowHungerLoss = bl;
    }

    public boolean isSaturationOnEat() {
        return this.saturationOnEat;
    }

    public void setSaturationOnEat(boolean bl) {
        this.saturationOnEat = bl;
    }

    public boolean isLockClearWeather() {
        return this.lockClearWeather;
    }

    public void setLockClearWeather(boolean bl) {
        this.lockClearWeather = bl;
    }

    public boolean isLockDayTime() {
        return this.lockDayTime;
    }

    public void setLockDayTime(boolean bl) {
        this.lockDayTime = bl;
    }

    public boolean isVoidKillEnabled() {
        return this.voidKillEnabled;
    }

    public void setVoidKillEnabled(boolean bl) {
        this.voidKillEnabled = bl;
    }

    public int getVoidKillY() {
        return this.voidKillY;
    }

    public void setVoidKillY(int n) {
        this.voidKillY = n;
    }

    public boolean isBorderParticles() {
        return this.borderParticles;
    }

    public void setBorderParticles(boolean bl) {
        this.borderParticles = bl;
    }

    public boolean isGlowingOpponent() {
        return this.glowingOpponent;
    }

    public void setGlowingOpponent(boolean bl) {
        this.glowingOpponent = bl;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean bl) {
        this.enabled = bl;
    }

    public boolean isCloningEnabled() {
        return this.cloningEnabled;
    }

    public void setCloningEnabled(boolean bl) {
        this.cloningEnabled = bl;
    }

    public boolean isAllowSpectators() {
        return this.allowSpectators;
    }

    public void setAllowSpectators(boolean bl) {
        this.allowSpectators = bl;
    }

    public int getCountdownOverride() {
        return this.countdownOverride;
    }

    public void setCountdownOverride(int n) {
        this.countdownOverride = n;
    }

    public String getDescription() {
        return this.description == null ? "" : this.description;
    }

    public void setDescription(String string) {
        this.description = string;
    }

    public Set<String> getKits() {
        return this.kits;
    }

    public boolean supportsKit(String string) {
        return this.supportsKitExplicit(string);
    }

    public boolean supportsKitExplicit(String string) {
        return this.kits.stream().anyMatch(string2 -> string2.equalsIgnoreCase(string));
    }

    public void save(ConfigurationSection configurationSection) {
        configurationSection.set("world", (Object)this.worldName);
        Arena.writeLoc(configurationSection, "spawn1", this.spawn1, true);
        Arena.writeLoc(configurationSection, "spawn2", this.spawn2, true);
        Arena.writeLoc(configurationSection, "corner1", this.corner1, false);
        Arena.writeLoc(configurationSection, "corner2", this.corner2, false);
        Arena.writeLoc(configurationSection, "event-spawn", this.eventSpawn, true);
        Arena.writeLoc(configurationSection, "border-corner1", this.borderCorner1, false);
        Arena.writeLoc(configurationSection, "border-corner2", this.borderCorner2, false);
        configurationSection.set("settings.event-border-end", (Object)this.eventBorderEnd);
        configurationSection.set("settings.event-border-interval", (Object)this.eventBorderInterval);
        configurationSection.set("settings.event-border-step", (Object)this.eventBorderStep);
        configurationSection.set("settings.auto-regenerate", (Object)this.autoRegenerate);
        configurationSection.set("settings.regen-delay-ticks", (Object)this.regenDelayTicks);
        configurationSection.set("settings.allow-build", (Object)this.allowBuild);
        configurationSection.set("settings.allow-break-all", (Object)this.allowBreak);
        configurationSection.set("settings.allow-break-inner", (Object)this.allowBreakInner);
        configurationSection.set("settings.allow-break-walls", (Object)this.allowBreakWalls);
        configurationSection.set("settings.allow-break-roof", (Object)this.allowBreakRoof);
        configurationSection.set("settings.allow-place-inner", (Object)this.allowPlaceInner);
        configurationSection.set("settings.allow-place-walls", (Object)this.allowPlaceWalls);
        configurationSection.set("settings.allow-place-roof", (Object)this.allowPlaceRoof);
        configurationSection.set("settings.allow-remove-added", (Object)this.allowRemoveAdded);
        configurationSection.set("settings.roof-margin", (Object)this.roofMargin);
        configurationSection.set("settings.wall-margin", (Object)this.wallMargin);
        configurationSection.set("settings.allow-explosions", (Object)this.allowExplosions);
        configurationSection.set("settings.allow-fire-spread", (Object)this.allowFireSpread);
        configurationSection.set("settings.allow-liquid-flow", (Object)this.allowLiquidFlow);
        configurationSection.set("settings.allow-mob-spawns", (Object)this.allowMobSpawns);
        configurationSection.set("settings.allow-fall-damage", (Object)this.allowFallDamage);
        configurationSection.set("settings.allow-hunger-loss", (Object)this.allowHungerLoss);
        configurationSection.set("settings.saturation-on-eat", (Object)this.saturationOnEat);
        configurationSection.set("settings.lock-clear-weather", (Object)this.lockClearWeather);
        configurationSection.set("settings.lock-day-time", (Object)this.lockDayTime);
        configurationSection.set("settings.void-kill-enabled", (Object)this.voidKillEnabled);
        configurationSection.set("settings.void-kill-y", (Object)this.voidKillY);
        configurationSection.set("settings.border-particles", (Object)this.borderParticles);
        configurationSection.set("settings.glowing-opponent", (Object)this.glowingOpponent);
        configurationSection.set("settings.enabled", (Object)this.enabled);
        configurationSection.set("settings.cloning-enabled", (Object)this.cloningEnabled);
        configurationSection.set("settings.allow-spectators", (Object)this.allowSpectators);
        configurationSection.set("settings.countdown-override", (Object)this.countdownOverride);
        configurationSection.set("settings.description", (Object)this.description);
        configurationSection.set("kits", new ArrayList<String>(this.kits));
    }

    public static Arena load(String string, ConfigurationSection configurationSection) {
        Arena arena = new Arena(string);
        arena.worldName = configurationSection.getString("world");
        arena.spawn1 = Arena.readLoc(configurationSection, "spawn1", arena.worldName, true);
        arena.spawn2 = Arena.readLoc(configurationSection, "spawn2", arena.worldName, true);
        arena.corner1 = Arena.readLoc(configurationSection, "corner1", arena.worldName, false);
        arena.corner2 = Arena.readLoc(configurationSection, "corner2", arena.worldName, false);
        arena.eventSpawn = Arena.readLoc(configurationSection, "event-spawn", arena.worldName, true);
        arena.borderCorner1 = Arena.readLoc(configurationSection, "border-corner1", arena.worldName, false);
        arena.borderCorner2 = Arena.readLoc(configurationSection, "border-corner2", arena.worldName, false);
        arena.eventBorderEnd = configurationSection.getInt("settings.event-border-end", 30);
        arena.eventBorderInterval = configurationSection.getInt("settings.event-border-interval", 120);
        arena.eventBorderStep = configurationSection.getInt("settings.event-border-step", 20);
        arena.autoRegenerate = configurationSection.getBoolean("settings.auto-regenerate", true);
        arena.regenDelayTicks = configurationSection.getInt("settings.regen-delay-ticks", 0);
        boolean bl = configurationSection.getBoolean("settings.allow-place", false);
        arena.allowBreakInner = configurationSection.getBoolean("settings.allow-break-inner", false);
        arena.allowBreakWalls = configurationSection.getBoolean("settings.allow-break-walls", false);
        arena.allowBreakRoof = configurationSection.getBoolean("settings.allow-break-roof", false);
        arena.allowPlaceInner = configurationSection.getBoolean("settings.allow-place-inner", bl);
        arena.allowPlaceWalls = configurationSection.getBoolean("settings.allow-place-walls", false);
        arena.allowPlaceRoof = configurationSection.getBoolean("settings.allow-place-roof", false);
        arena.allowRemoveAdded = configurationSection.getBoolean("settings.allow-remove-added", true);
        boolean bl2 = arena.allowBreakInner || arena.allowBreakWalls || arena.allowBreakRoof || configurationSection.getBoolean("settings.allow-break", false);
        boolean bl3 = arena.allowPlaceInner || arena.allowPlaceWalls || arena.allowPlaceRoof || bl;
        arena.allowBreak = configurationSection.getBoolean("settings.allow-break-all", bl2);
        arena.allowBuild = configurationSection.getBoolean("settings.allow-build", bl3);
        arena.roofMargin = configurationSection.getInt("settings.roof-margin", 1);
        arena.wallMargin = configurationSection.getInt("settings.wall-margin", 0);
        arena.allowExplosions = configurationSection.getBoolean("settings.allow-explosions", false);
        arena.allowFireSpread = configurationSection.getBoolean("settings.allow-fire-spread", false);
        arena.allowLiquidFlow = configurationSection.getBoolean("settings.allow-liquid-flow", true);
        arena.allowMobSpawns = configurationSection.getBoolean("settings.allow-mob-spawns", false);
        arena.allowFallDamage = configurationSection.getBoolean("settings.allow-fall-damage", true);
        arena.allowHungerLoss = configurationSection.getBoolean("settings.allow-hunger-loss", false);
        arena.saturationOnEat = configurationSection.getBoolean("settings.saturation-on-eat", false);
        arena.lockClearWeather = configurationSection.getBoolean("settings.lock-clear-weather", false);
        arena.lockDayTime = configurationSection.getBoolean("settings.lock-day-time", false);
        arena.voidKillEnabled = configurationSection.getBoolean("settings.void-kill-enabled", false);
        arena.voidKillY = configurationSection.getInt("settings.void-kill-y", -64);
        arena.borderParticles = configurationSection.getBoolean("settings.border-particles", false);
        arena.glowingOpponent = configurationSection.getBoolean("settings.glowing-opponent", false);
        arena.enabled = configurationSection.getBoolean("settings.enabled", true);
        boolean bl4 = configurationSection.getBoolean("settings.duplicator", true);
        arena.cloningEnabled = configurationSection.getBoolean("settings.cloning-enabled", bl4);
        arena.allowSpectators = configurationSection.getBoolean("settings.allow-spectators", true);
        arena.countdownOverride = configurationSection.getInt("settings.countdown-override", -1);
        arena.description = configurationSection.getString("settings.description", "");
        arena.kits.addAll(configurationSection.getStringList("kits"));
        return arena;
    }

    private static void writeLoc(ConfigurationSection configurationSection, String string, Location location, boolean bl) {
        if (location == null) {
            configurationSection.set(string, null);
            return;
        }
        configurationSection.set(string + ".x", (Object)(bl ? location.getX() : (double)location.getBlockX()));
        configurationSection.set(string + ".y", (Object)(bl ? location.getY() : (double)location.getBlockY()));
        configurationSection.set(string + ".z", (Object)(bl ? location.getZ() : (double)location.getBlockZ()));
        if (bl) {
            configurationSection.set(string + ".yaw", (Object)Float.valueOf(location.getYaw()));
            configurationSection.set(string + ".pitch", (Object)Float.valueOf(location.getPitch()));
        }
    }

    private static Location readLoc(ConfigurationSection configurationSection, String string, String string2, boolean bl) {
        if (!configurationSection.isConfigurationSection(string)) {
            return null;
        }
        World world = string2 == null ? null : Bukkit.getWorld((String)string2);
        Location location = new Location(world, configurationSection.getDouble(string + ".x"), configurationSection.getDouble(string + ".y"), configurationSection.getDouble(string + ".z"));
        if (bl) {
            location.setYaw((float)configurationSection.getDouble(string + ".yaw"));
            location.setPitch((float)configurationSection.getDouble(string + ".pitch"));
        }
        return location;
    }

    public static enum Zone {
        ROOF,
        WALL,
        INNER;

    }
}

