package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.model.Arena;
import com.meowduels.model.Kit;
import com.meowduels.model.Party;
import com.meowduels.util.Items;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** Party Hunger Games arenas, kits, hosting screens, and chest loot. */
public final class HungerGamesManager {
    private static final String PERMISSION = "hg.host.party";
    private final MeowDuels plugin;
    private final File arenaFile;
    private final File kitFile;
    private final Map<String, HgArena> arenas = new LinkedHashMap<>();
    private final Map<String, Kit> kits = new LinkedHashMap<>();
    private final Map<UUID, Setup> setups = new LinkedHashMap<>();
    private final Random random = new Random();

    public HungerGamesManager(MeowDuels plugin) {
        this.plugin = plugin;
        this.arenaFile = new File(plugin.getDataFolder(), "hungergames.yml");
        this.kitFile = new File(plugin.getDataFolder(), "hungergames-kits.yml");
        this.loadArenas();
        this.loadKits();
    }

    public boolean hasHostPermission(Player player) {
        return player.hasPermission(PERMISSION) || player.hasPermission("starduels.hungergames.host");
    }

    public void openHostMenu(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cYou must be in a party to host Hunger Games.");
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cOnly your party leader can host Hunger Games.");
            return;
        }
        if (!this.hasHostPermission(player)) {
            player.sendMessage("§cYou need permission §f" + PERMISSION + "§c to host Hunger Games.");
            return;
        }
        new HostMenu(this, party).open(player);
    }

    private boolean requireHost(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cOnly your party leader can host Hunger Games.");
            return false;
        }
        if (!this.hasHostPermission(player)) {
            player.sendMessage("§cYou need permission §f" + PERMISSION + "§c to host Hunger Games.");
            return false;
        }
        return true;
    }

    public boolean inSetup(UUID id) { return this.setups.containsKey(id); }

    public boolean beginSetup(Player player, String name) {
        if (name == null || !name.matches("[A-Za-z0-9_-]{1,32}") || this.arenas.containsKey(name.toLowerCase(Locale.ROOT))
                || this.plugin.getArenaManager().exists(name)) {
            player.sendMessage("§cChoose a new arena name using only letters, numbers, underscores, or hyphens.");
            return false;
        }
        Arena arena = this.plugin.getArenaManager().create(name);
        this.setups.put(player.getUniqueId(), new Setup(arena));
        player.sendMessage("§aHunger Games setup for §f" + name + "§a started.");
        player.sendMessage("§eStand at the shared player spawn and type §f done §e. Then mark arena corner 1, corner 2, chest group A pos1/pos2, and chest group B pos1/pos2. Type §fcancel §eto abort.");
        return true;
    }

    /** Called on the server thread after SetupChatListener captures chat. */
    public void handleSetupInput(Player player, String input) {
        Setup setup = this.setups.get(player.getUniqueId());
        if (setup == null) return;
        if (input.equalsIgnoreCase("cancel")) {
            this.setups.remove(player.getUniqueId());
            this.plugin.getArenaManager().delete(setup.arena.getName());
            this.arenas.remove(setup.arena.getName().toLowerCase(Locale.ROOT));
            this.saveArenas();
            player.sendMessage("§cHunger Games arena setup cancelled.");
            return;
        }
        if (!input.equalsIgnoreCase("done")) {
            player.sendMessage("§eType §f done §ewhen standing at the requested location, or §f cancel §eto abort.");
            return;
        }
        Location here = player.getLocation().clone();
        if (setup.step > 0 && !setup.arena.getWorld().equals(here.getWorld())) {
            player.sendMessage("§cAll Hunger Games setup points must be in the same world.");
            return;
        }
        switch (setup.step) {
            case 0 -> {
                setup.spawn = here;
                setup.arena.setSpawn1(here);
                setup.arena.setSpawn2(here);
                this.nextSetup(player, setup, "Arena corner 1");
            }
            case 1 -> {
                setup.arena.setCorner1(here);
                this.nextSetup(player, setup, "Arena corner 2");
            }
            case 2 -> {
                setup.arena.setCorner2(here);
                this.nextSetup(player, setup, "Chest group A pos1");
            }
            case 3 -> {
                setup.zoneA1 = here;
                this.nextSetup(player, setup, "Chest group A pos2");
            }
            case 4 -> {
                setup.zoneA2 = here;
                this.nextSetup(player, setup, "Chest group B pos1");
            }
            case 5 -> {
                setup.zoneB1 = here;
                this.nextSetup(player, setup, "Chest group B pos2");
            }
            case 6 -> {
                setup.zoneB2 = here;
                this.finishSetup(player, setup);
            }
            default -> { }
        }
    }

    private void nextSetup(Player player, Setup setup, String point) {
        setup.step++;
        player.sendMessage("§eGo to §f" + point + "§e and type §f done§e.");
    }

    private void finishSetup(Player player, Setup setup) {
        Arena arena = setup.arena;
        if (!arena.isConfigured() || !inside(arena, setup.spawn) || !inside(arena, setup.zoneA1)
                || !inside(arena, setup.zoneA2) || !inside(arena, setup.zoneB1) || !inside(arena, setup.zoneB2)) {
            player.sendMessage("§cEvery setup point must be inside the arena corners. Setup remains active; set the points again or type cancel.");
            setup.step = 0;
            player.sendMessage("§eStand at the shared player spawn and type §f done§e.");
            return;
        }
        this.plugin.getArenaManager().save();
        this.arenas.put(arena.getName().toLowerCase(Locale.ROOT), new HgArena(arena.getName(), setup.zoneA1, setup.zoneA2, setup.zoneB1, setup.zoneB2));
        this.saveArenas();
        this.plugin.getArenaManager().snapshotArena(arena);
        this.setups.remove(player.getUniqueId());
        player.sendMessage("§aHunger Games arena §f" + arena.getName() + "§a is ready. The arena baseline is saved.");
    }

    private static boolean inside(Arena arena, Location point) {
        return point != null && arena.contains(point);
    }

    public void removeArena(String name) {
        if (name == null) return;
        this.arenas.remove(name.toLowerCase(Locale.ROOT));
        this.saveArenas();
    }

    public List<HgArena> availableArenas() {
        ArrayList<HgArena> result = new ArrayList<>();
        for (HgArena entry : this.arenas.values()) {
            Arena arena = this.plugin.getArenaManager().get(entry.name);
            if (arena != null && arena.isConfigured()) result.add(entry);
        }
        return result;
    }

    public HgArena arena(String name) { return name == null ? null : this.arenas.get(name.toLowerCase(Locale.ROOT)); }
    public Kit kit(String name) { return name == null ? null : this.kits.get(name.toLowerCase(Locale.ROOT)); }
    public List<Kit> kits() { return new ArrayList<>(this.kits.values()); }

    public void startSelected(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId()) || !this.hasHostPermission(player)) {
            player.sendMessage("§cOnly a permitted party leader can start Hunger Games.");
            return;
        }
        HgArena selected = this.arena(party.getHungerGamesArena());
        Arena arena = selected == null ? null : this.plugin.getArenaManager().get(selected.name);
        Kit kit = this.kit(party.getHungerGamesKit());
        if (arena == null || !arena.isConfigured()) {
            player.sendMessage("§cChoose an available Hunger Games arena first.");
            return;
        }
        if (kit == null) {
            player.sendMessage("§cChoose a Hunger Games kit first.");
            return;
        }
        this.plugin.getPartyManager().startHungerGames(player, arena, kit, selected);
    }

    public void generateChests(HgArena arena) {
        if (arena == null) return;
        // Keep generated stores compact. Older configs defaulted to eight
        // blocks, which commonly produced just one double chest per region.
        int spacing = Math.max(3, Math.min(6, this.plugin.getConfig().getInt("hungergames.chest-spacing", 3)));
        ArrayList<ChestPair> placed = new ArrayList<>();
        Arena baseArena = this.plugin.getArenaManager().get(arena.name);
        if (baseArena == null || !baseArena.isConfigured()) return;
        this.placeZone(arena.zoneA1, arena.zoneA2, spacing, baseArena, placed);
        this.placeZone(arena.zoneB1, arena.zoneB2, spacing, baseArena, placed);
        if (!placed.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                for (ChestPair pair : placed) this.fillPair(pair);
            }, 1L);
        }
    }

    private void placeZone(Location first, Location second, int spacing, Arena arena, List<ChestPair> placed) {
        if (first == null || second == null || first.getWorld() == null || !first.getWorld().equals(second.getWorld())) return;
        World world = first.getWorld();
        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());
        int lowY = arena.minY();
        int highY = arena.maxY();
        boolean alongX = maxX - minX >= 1;
        if (!alongX && maxZ - minZ < 1) return;
        int xStep = alongX ? spacing : 1;
        int zStep = spacing;
        int xLimit = alongX ? maxX - 1 : maxX;
        int zLimit = alongX ? maxZ : maxZ - 1;
        for (int x = minX; x <= xLimit; x += xStep) {
            for (int z = minZ; z <= zLimit; z += zStep) {
                int otherX = alongX ? x + 1 : x;
                int otherZ = alongX ? z : z + 1;
                int y = chestY(world, x, z, lowY, highY);
                int y2 = chestY(world, otherX, otherZ, lowY, highY);
                if (y < 0 || y2 != y) continue;
                Block one = world.getBlockAt(x, y, z);
                Block two = world.getBlockAt(otherX, y, otherZ);
                if (!one.isPassable() || !two.isPassable()) continue;
                org.bukkit.block.data.type.Chest left = (org.bukkit.block.data.type.Chest)Bukkit.createBlockData(Material.CHEST);
                org.bukkit.block.data.type.Chest right = (org.bukkit.block.data.type.Chest)Bukkit.createBlockData(Material.CHEST);
                BlockFace facing = alongX ? BlockFace.NORTH : BlockFace.EAST;
                left.setFacing(facing);
                right.setFacing(facing);
                left.setType(org.bukkit.block.data.type.Chest.Type.LEFT);
                right.setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
                one.setBlockData(left, true);
                two.setBlockData(right, true);
                placed.add(new ChestPair(world, x, y, z));
            }
        }
    }

    private static int chestY(World world, int x, int z, int low, int high) {
        for (int y = Math.max(world.getMinHeight(), low); y <= Math.min(world.getMaxHeight() - 3, high); y++) {
            if (world.getBlockAt(x, y, z).getType().isSolid()
                    && world.getBlockAt(x, y + 1, z).isPassable()
                    && world.getBlockAt(x, y + 2, z).isPassable()) return y + 1;
        }
        return -1;
    }

    private void fillPair(ChestPair pair) {
        Block block = pair.world.getBlockAt(pair.x, pair.y, pair.z);
        if (!(block.getState() instanceof Chest chest)) return;
        Inventory inventory = chest.getInventory();
        inventory.clear();
        List<ItemStack> loot = this.getMassiveHGLoot(inventory.getSize());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (this.random.nextDouble() < 0.72) inventory.setItem(slot, loot.get(slot));
        }
        chest.update(true, false);
    }

    /** Builds typed item stacks with exactly 25% of draws from each loot tier. */
    public List<ItemStack> getMassiveHGLoot() { return this.getMassiveHGLoot(54); }

    private List<ItemStack> getMassiveHGLoot(int size) {
        ArrayList<ItemStack> items = new ArrayList<>(size);
        for (int slot = 0; slot < size; slot++) items.add(this.randomTierLoot());
        return items;
    }

    private ItemStack randomTierLoot() {
        return switch (this.random.nextInt(4)) {
            case 0 -> this.weaponLoot();
            case 1 -> this.armorLoot();
            case 2 -> this.utilityLoot();
            default -> this.foodLoot();
        };
    }

    private ItemStack weaponLoot() {
        Material[] choices = {Material.WOODEN_SWORD, Material.STONE_SWORD, Material.WOODEN_AXE, Material.STONE_AXE, Material.BOW, Material.FLINT_AND_STEEL, Material.IRON_AXE};
        return new ItemStack(choices[this.random.nextInt(choices.length)]);
    }

    private ItemStack armorLoot() {
        if (this.random.nextInt(20) == 0) return new ItemStack(Material.DIAMOND_CHESTPLATE);
        Material[] choices = {Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
                Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
                Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS};
        return new ItemStack(choices[this.random.nextInt(choices.length)]);
    }

    private ItemStack utilityLoot() {
        Material[] choices = {Material.WATER_BUCKET, Material.LAVA_BUCKET, Material.COBBLESTONE, Material.COBWEB, Material.EGG, Material.SNOWBALL, Material.FISHING_ROD};
        Material material = choices[this.random.nextInt(choices.length)];
        int amount = material == Material.COBBLESTONE ? 16 : material == Material.EGG || material == Material.SNOWBALL ? 8 : 1;
        return new ItemStack(material, amount);
    }

    private ItemStack foodLoot() {
        Material[] choices = {Material.GOLDEN_APPLE, Material.GOLDEN_APPLE, Material.APPLE, Material.COOKED_BEEF, Material.COOKIE, Material.BREAD, Material.MELON_SLICE};
        Material material = choices[this.random.nextInt(choices.length)];
        int amount = material == Material.GOLDEN_APPLE ? (this.random.nextBoolean() ? 1 : 3)
                : material == Material.APPLE || material == Material.COOKIE || material == Material.MELON_SLICE ? 4 : 2;
        return new ItemStack(material, amount);
    }

    public void loadArenas() {
        this.arenas.clear();
        if (!this.arenaFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.arenaFile);
        ConfigurationSection section = yaml.getConfigurationSection("arenas");
        if (section == null) return;
        for (String name : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(name);
            if (entry == null) continue;
            Location a1 = entry.getLocation("group-a.pos1"), a2 = entry.getLocation("group-a.pos2");
            Location b1 = entry.getLocation("group-b.pos1"), b2 = entry.getLocation("group-b.pos2");
            if (a1 != null && a2 != null && b1 != null && b2 != null) this.arenas.put(name.toLowerCase(Locale.ROOT), new HgArena(name, a1, a2, b1, b2));
        }
    }

    public void saveArenas() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (HgArena entry : this.arenas.values()) {
            yaml.set("arenas." + entry.name + ".group-a.pos1", entry.zoneA1);
            yaml.set("arenas." + entry.name + ".group-a.pos2", entry.zoneA2);
            yaml.set("arenas." + entry.name + ".group-b.pos1", entry.zoneB1);
            yaml.set("arenas." + entry.name + ".group-b.pos2", entry.zoneB2);
        }
        try { yaml.save(this.arenaFile); }
        catch (IOException exception) { this.plugin.getLogger().severe("Could not save hungergames.yml: " + exception.getMessage()); }
    }

    private void loadKits() {
        this.kits.clear();
        if (!this.kitFile.exists()) this.createDefaultKits();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.kitFile);
        ConfigurationSection section = yaml.getConfigurationSection("kits");
        if (section != null) for (String name : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(name);
            if (entry != null) this.kits.put(name.toLowerCase(Locale.ROOT), Kit.load(name, entry));
        }
        if (this.kits.isEmpty()) this.createDefaultKits();
    }

    private void createDefaultKits() {
        Kit classic = new Kit("HG Classic");
        classic.setDisplayName("<gold>HG Classic</gold>"); classic.setIcon(Material.WOODEN_SWORD);
        ItemStack[] classicItems = new ItemStack[36]; classicItems[0] = new ItemStack(Material.WOODEN_SWORD); classicItems[1] = new ItemStack(Material.BREAD, 3); classic.setContents(classicItems);
        Kit archer = new Kit("HG Archer");
        archer.setDisplayName("<aqua>HG Archer</aqua>"); archer.setIcon(Material.BOW);
        ItemStack[] archerItems = new ItemStack[36]; archerItems[0] = new ItemStack(Material.WOODEN_SWORD); archerItems[1] = new ItemStack(Material.BOW); archerItems[2] = new ItemStack(Material.ARROW, 16); archer.setContents(archerItems);
        ItemStack[] leather = new ItemStack[4]; leather[3] = new ItemStack(Material.LEATHER_CHESTPLATE); archer.setArmor(leather);
        Kit scout = new Kit("HG Scout");
        scout.setDisplayName("<green>HG Scout</green>"); scout.setIcon(Material.STONE_SWORD);
        ItemStack[] scoutItems = new ItemStack[36]; scoutItems[0] = new ItemStack(Material.STONE_SWORD); scoutItems[1] = new ItemStack(Material.COOKED_BEEF, 3); scout.setContents(scoutItems);
        this.kits.put(classic.getName().toLowerCase(Locale.ROOT), classic);
        this.kits.put(archer.getName().toLowerCase(Locale.ROOT), archer);
        this.kits.put(scout.getName().toLowerCase(Locale.ROOT), scout);
        this.saveKits();
    }

    public void saveKits() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Kit kit : this.kits.values()) kit.save(yaml.createSection("kits." + kit.getName()));
        try { yaml.save(this.kitFile); }
        catch (IOException exception) { this.plugin.getLogger().severe("Could not save hungergames-kits.yml: " + exception.getMessage()); }
    }

    private static final class Setup {
        final Arena arena;
        int step;
        Location spawn, zoneA1, zoneA2, zoneB1, zoneB2;
        Setup(Arena arena) { this.arena = arena; }
    }

    private record ChestPair(World world, int x, int y, int z) { }

    public static final class HgArena {
        public final String name;
        public final Location zoneA1, zoneA2, zoneB1, zoneB2;
        HgArena(String name, Location a1, Location a2, Location b1, Location b2) {
            this.name = name; this.zoneA1 = a1; this.zoneA2 = a2; this.zoneB1 = b1; this.zoneB2 = b2;
        }
    }

    private static final class HostMenu extends Menu {
        private final HungerGamesManager hg;
        private final UUID leader;
        HostMenu(HungerGamesManager hg, Party party) { this.hg = hg; this.leader = party.getLeader(); }
        public void build() {
            this.createRaw(3, "<dark_gray>Hunger Games Host Menu");
            Party party = hg.plugin.getPartyManager().partyOf(leader);
            String kitName = party == null ? "none" : String.valueOf(party.getHungerGamesKit());
            String arenaName = party == null ? "none" : String.valueOf(party.getHungerGamesArena());
            this.inventory.setItem(11, Items.of(Material.GRASS_BLOCK).rawName("<green>Choose Arena").rawLore("<gray>Selected: <white>" + arenaName, "<gray>Click to browse registered arenas").tag(hg.plugin.keyButton(), "hg:arenas").build());
            this.inventory.setItem(13, Items.of(Material.CHEST).rawName("<gold>Select Kit").rawLore("<gray>Choose an exclusive Hunger Games kit", "<dark_gray>Selected: <white>" + kitName).tag(hg.plugin.keyButton(), "hg:kits").build());
            this.inventory.setItem(15, Items.of(Material.LIME_CONCRETE).rawName("<green>Start Match").rawLore("<gray>Start Hunger Games for your party").tag(hg.plugin.keyButton(), "hg:start").build());
        }
        public void onClick(Player player, InventoryClickEvent event) {
            if (!hg.requireHost(player)) { player.closeInventory(); return; }
            Party party = hg.plugin.getPartyManager().partyOf(player.getUniqueId());
            if (party == null || !party.isLeader(player.getUniqueId())) { player.closeInventory(); return; }
            String tag = Items.readTag(event.getCurrentItem(), hg.plugin.keyButton());
            if ("hg:arenas".equals(tag)) new ArenaMenuHg(hg).open(player);
            else if ("hg:kits".equals(tag)) new KitMenuHg(hg).open(player);
            else if ("hg:start".equals(tag)) hg.startSelected(player);
        }
    }

    private static final class ArenaMenuHg extends Menu {
        private final HungerGamesManager hg;
        ArenaMenuHg(HungerGamesManager hg) { this.hg = hg; }
        public void build() {
            this.createRaw(6, "<dark_gray>Choose Hunger Games Arena");
            int slot = 10;
            for (HgArena entry : hg.availableArenas()) {
                if (slot >= this.inventory.getSize() - 9) break;
                Arena arena = hg.plugin.getArenaManager().get(entry.name);
                boolean disabled = arena == null || !arena.isEnabled();
                boolean busy = !disabled && hg.plugin.getDuelManager().isArenaInUse(entry.name);
                this.inventory.setItem(slot++, Items.of(busy ? Material.RED_CONCRETE : Material.GRASS_BLOCK)
                        .rawName((busy || disabled ? "<red>" : "<green>") + entry.name)
                        .rawLore(disabled ? "<red>Disabled" : busy ? "<red>In use" : "<green>Available", "<gray>Click to select")
                        .tag(hg.plugin.keyButton(), "hg:arena:" + entry.name).build());
            }
        }
        public void onClick(Player player, InventoryClickEvent event) {
            if (!hg.requireHost(player)) { player.closeInventory(); return; }
            String tag = Items.readTag(event.getCurrentItem(), hg.plugin.keyButton());
            if (tag == null || !tag.startsWith("hg:arena:")) return;
            String name = tag.substring("hg:arena:".length());
            Arena arena = hg.plugin.getArenaManager().get(name);
            if (arena == null || !arena.isEnabled()) { player.sendMessage("§cThat arena is disabled or unavailable."); return; }
            if (hg.plugin.getDuelManager().isArenaInUse(name)) { player.sendMessage("§cThat arena is currently in use."); return; }
            Party party = hg.plugin.getPartyManager().partyOf(player.getUniqueId());
            if (party != null && party.isLeader(player.getUniqueId())) party.setHungerGamesArena(name);
            hg.openHostMenu(player);
        }
    }

    private static final class KitMenuHg extends Menu {
        private final HungerGamesManager hg;
        KitMenuHg(HungerGamesManager hg) { this.hg = hg; }
        public void build() {
            this.createRaw(4, "<dark_gray>Select Hunger Games Kit");
            int slot = 10;
            for (Kit kit : hg.kits()) {
                if (slot >= 26) break;
                this.inventory.setItem(slot++, Items.of(kit.getIcon()).rawName(kit.getDisplayName() == null ? "<gold>" + kit.getName() : kit.getDisplayName())
                        .rawLore("<gray>Click to preview this exclusive HG kit").tag(hg.plugin.keyButton(), "hg:kit:" + kit.getName()).build());
            }
        }
        public void onClick(Player player, InventoryClickEvent event) {
            if (!hg.requireHost(player)) { player.closeInventory(); return; }
            String tag = Items.readTag(event.getCurrentItem(), hg.plugin.keyButton());
            if (tag != null && tag.startsWith("hg:kit:")) new KitPreviewHg(hg, tag.substring("hg:kit:".length())).open(player);
        }
    }

    private static final class KitPreviewHg extends Menu {
        private final HungerGamesManager hg; private final String name;
        KitPreviewHg(HungerGamesManager hg, String name) { this.hg = hg; this.name = name; }
        public void build() {
            Kit kit = hg.kit(name); this.createRaw(6, "<dark_gray>HG Kit Preview"); if (kit == null) return;
            int slot = 0; for (ItemStack item : kit.getContents()) { if (item != null && item.getType() != Material.AIR && slot < 45) this.inventory.setItem(slot++, item.clone()); }
            for (ItemStack item : kit.getArmor()) if (item != null && item.getType() != Material.AIR && slot < 45) this.inventory.setItem(slot++, item.clone());
            this.inventory.setItem(49, Items.of(Material.LIME_DYE).rawName("<green>Select " + name).tag(hg.plugin.keyButton(), "hg:kit-select:" + name).build());
        }
        public void onClick(Player player, InventoryClickEvent event) {
            if (!hg.requireHost(player)) { player.closeInventory(); return; }
            String tag = Items.readTag(event.getCurrentItem(), hg.plugin.keyButton());
            if (tag == null || !tag.startsWith("hg:kit-select:")) return;
            Party party = hg.plugin.getPartyManager().partyOf(player.getUniqueId());
            if (party != null && party.isLeader(player.getUniqueId())) party.setHungerGamesKit(name);
            hg.openHostMenu(player);
        }
    }
}
