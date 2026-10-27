package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Style;
import com.meowduels.util.Items;
import com.meowduels.util.Tokens;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiConfigManager {
    private static final List<String> FIXED_SLOTS = Arrays.asList("arena", "guieditor", "trim", "kitselect", "kititems");
    private final MeowDuels plugin;
    private final File folder;
    private final Map<String, YamlConfiguration> files = new LinkedHashMap<String, YamlConfiguration>();
    public static final String BACK_NAME = "{value}\u0299\u1d00\u1d04\u1d0b";
    public static final String[] BACK_LORE = new String[]{"", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u0262\u1d0f \u0299\u1d00\u1d04\u1d0b"};
    public static final List<String> MENUS = Arrays.asList("arena", "duelconfirm", "editkiteffects", "editkitlist", "eventmap", "guieditor", "kiteditor", "kititems", "kitpick", "kitselect", "mapselect", "matchsummary", "partyconfirm", "partyduelsconfirm", "partyinvite", "partykit", "party", "partymode", "partyopponent", "partysettings", "partystartconfirm", "partyteam", "queuepick", "settings", "trimkit", "trim");

    public GuiConfigManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.folder = new File(meowDuels.getDataFolder(), "gui");
        this.reload();
    }

    public static boolean slotsAreFixed(String string) {
        return FIXED_SLOTS.contains(string);
    }

    public void reload() {
        this.files.clear();
        this.copyShipped();
        File[] fileArray = this.folder.listFiles();
        if (fileArray == null) {
            return;
        }
        for (File file : fileArray) {
            String string = file.getName();
            if (!string.endsWith(".yml")) continue;
            String string2 = string.substring(0, string.length() - 4).toLowerCase();
            try {
                this.files.put(string2, YamlConfiguration.loadConfiguration((File)file));
            }
            catch (Throwable throwable) {
                this.plugin.getLogger().warning("gui/" + string + " could not be read, so the " + string2 + " menu will use its built-in layout: " + throwable.getMessage());
            }
        }
    }

    private void copyShipped() {
        for (String string : MENUS) {
            File file = new File(this.folder, string + ".yml");
            if (file.exists()) continue;
            try {
                this.plugin.saveResource("gui/" + string + ".yml", false);
            }
            catch (Throwable throwable) {}
        }
        this.mergeButtonsGuiSetting(new File(this.folder, "settings.yml"));
    }

    private void mergeButtonsGuiSetting(File settingsFile) {
        if (!settingsFile.isFile()) return;
        try (InputStream resource = this.plugin.getResource("gui/settings.yml")) {
            if (resource == null) return;
            YamlConfiguration shipped = new YamlConfiguration();
            shipped.load(new InputStreamReader(resource, StandardCharsets.UTF_8));
            ConfigurationSection defaults = shipped.getConfigurationSection("items.buttons-gui");
            if (defaults == null) return;
            YamlConfiguration current = YamlConfiguration.loadConfiguration(settingsFile);
            if (current.contains("items.buttons-gui")) return;
            for (String key : defaults.getKeys(false)) {
                current.set("items.buttons-gui." + key, defaults.get(key));
            }
            current.save(settingsFile);
        } catch (Exception exception) {
            this.plugin.getLogger().warning("Could not add the Buttons GUI item to gui/settings.yml: " + exception.getMessage());
        }
    }

    public void frame(String string, Inventory inventory, int n) {
        YamlConfiguration yamlConfiguration = this.files.get(string);
        if (yamlConfiguration == null || !yamlConfiguration.contains("filler")) {
            Style.frame(inventory, n);
            return;
        }
        Style.frame(inventory, n, Style.filler(yamlConfiguration.getString("filler"), yamlConfiguration.getString("filler-name", Style.FILLER_NAME)));
    }

    private Map<String, String> withTheme(Map<String, String> map) {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
        linkedHashMap.put("value", Style.VALUE);
        linkedHashMap.put("label", Style.LABEL);
        linkedHashMap.put("muted", Style.MUTED);
        linkedHashMap.put("good", Style.GOOD);
        linkedHashMap.put("bad", Style.BAD);
        linkedHashMap.put("warn", Style.WARN);
        linkedHashMap.put("sep", Style.SEP);
        linkedHashMap.put("hint", Style.HINT);
        linkedHashMap.put("tick", Style.TICK);
        linkedHashMap.put("cross", Style.CROSS);
        linkedHashMap.put("go-from", Style.GO_A);
        linkedHashMap.put("go-to", Style.GO_B);
        linkedHashMap.put("no-from", Style.NO_A);
        linkedHashMap.put("no-to", Style.NO_B);
        linkedHashMap.put("accent-from", Style.ACCENT_A);
        linkedHashMap.put("accent-to", Style.ACCENT_B);
        if (map != null) {
            linkedHashMap.putAll(map);
        }
        return linkedHashMap;
    }

    public ItemStack pane(String string) {
        YamlConfiguration yamlConfiguration = this.files.get(string);
        if (yamlConfiguration == null || !yamlConfiguration.contains("filler")) {
            return Style.filler(Style.FILLER, Style.FILLER_NAME);
        }
        return Style.filler(yamlConfiguration.getString("filler"), yamlConfiguration.getString("filler-name", Style.FILLER_NAME));
    }

    private ConfigurationSection itemSection(String string, String string2) {
        YamlConfiguration yamlConfiguration = this.files.get(string);
        return yamlConfiguration == null ? null : yamlConfiguration.getConfigurationSection("items." + string2);
    }

    public String title(String string, String string2) {
        YamlConfiguration yamlConfiguration = this.files.get(string);
        if (yamlConfiguration == null) {
            return string2;
        }
        String string3 = yamlConfiguration.getString("title");
        if ("duelconfirm".equals(string) && "Round Selection".equals(string3)) {
            return string2;
        }
        return string3 == null ? string2 : string3;
    }

    public String title(String string, String string2, Map<String, String> map) {
        return Tokens.apply(this.title(string, string2), this.withTheme(map));
    }

    public String titleOf(String string, String string2, String string3, Map<String, String> map) {
        YamlConfiguration yamlConfiguration = this.files.get(string);
        if (yamlConfiguration == null) {
            return Tokens.apply(string3, this.withTheme(map));
        }
        String string4 = yamlConfiguration.getString("title-" + string2, yamlConfiguration.getString("title", string3));
        if ("kitpick".equals(string) && string4 != null && string4.startsWith("&7&l")) {
            return Tokens.apply(string3, this.withTheme(map));
        }
        return Tokens.apply(string4, this.withTheme(map));
    }

    public int rows(String string, int n) {
        YamlConfiguration yamlConfiguration = this.files.get(string);
        if (yamlConfiguration == null || GuiConfigManager.slotsAreFixed(string)) {
            return n;
        }
        int n2 = yamlConfiguration.getInt("rows", n);
        if (n2 >= 1 && n2 <= 6) {
            return n2;
        }
        this.plugin.getLogger().warning("gui/" + string + ".yml: rows is " + n2 + ", which is not between 1 and 6 - using " + n + ".");
        return n;
    }

    public int slot(String string, String string2, int n, int n2) {
        if (GuiConfigManager.slotsAreFixed(string)) {
            return n;
        }
        ConfigurationSection configurationSection = this.itemSection(string, string2);
        if (configurationSection == null || !configurationSection.contains("slot")) {
            return n;
        }
        int n3 = configurationSection.getInt("slot", n);
        if (n3 >= 0 && n3 < n2 * 9) {
            return n3;
        }
        this.plugin.getLogger().warning("gui/" + string + ".yml: " + string2 + " has slot " + n3 + ", which is outside this " + n2 + "-row menu - using " + n + ".");
        return n;
    }

    public Items item(String string, String string2, Material material, String string3, String ... stringArray) {
        return this.item(string, string2, null, material, string3, stringArray);
    }

    public Items item(String string, String string2, Map<String, String> map, Material material, String string3, String ... stringArray) {
        String string4;
        ConfigurationSection configurationSection = this.itemSection(string, string2);
        Material material2 = this.material(string, string2, configurationSection, material);
        int n = configurationSection == null ? 1 : Math.max(1, Math.min(64, configurationSection.getInt("amount", 1)));
        String string5 = string4 = configurationSection == null ? string3 : configurationSection.getString("name", string3);
        if ("duelconfirm".equals(string) && GuiConfigManager.isLegacyDuelConfirmName(string2, string4)) {
            string4 = string3;
        }
        List<String> list = this.lore(configurationSection, stringArray);
        Map<String, String> map2 = this.withTheme(map);
        Items items = Items.of(material2, n).rawName(Tokens.apply(string4, map2));
        ArrayList<String> arrayList = new ArrayList<String>(list.size());
        for (String string6 : list) {
            arrayList.add(Tokens.apply(string6, map2));
        }
        items.rawLore(arrayList.toArray(new String[0]));
        if (configurationSection != null && configurationSection.contains("glow")) {
            items.glint(configurationSection.getBoolean("glow"));
        }
        return items;
    }

    private static boolean isLegacyDuelConfirmName(String string, String string2) {
        if (string2 == null) {
            return false;
        }
        switch (string) {
            case "info": {
                return "<blue>\u25c6 <aqua>Information <blue>\u25c6".equals(string2);
            }
            case "clock": {
                return "<gold>\u2726 <yellow>Rounds <gold>\u2726".equals(string2);
            }
            case "map": {
                return "<aqua>\u21c4 Map Selection \u21c4".equals(string2);
            }
            case "confirm": {
                return "<aqua>\u2714 Send Duel \u2714".equals(string2);
            }
            case "cancel": {
                return "<red>\u2716 Go Back \u2716".equals(string2);
            }
        }
        return false;
    }

    public Items toggle(String string, String string2, boolean bl, Map<String, String> map, Face face, Face face2) {
        return this.toggle(string, string2, bl, map, null, face, face2);
    }

    public Items toggle(String string, String string2, boolean bl, Map<String, String> map, Map<String, List<String>> map2, Face face, Face face2) {
        Boolean bl2;
        Face face3 = bl ? face : face2;
        String string3 = bl ? "-on" : "-off";
        ConfigurationSection configurationSection = this.itemSection(string, string2);
        Material material = face3.material;
        String string4 = face3.name;
        List list = Arrays.asList(face3.lore);
        Boolean bl3 = bl2 = face3.glow ? Boolean.TRUE : null;
        if (configurationSection != null) {
            String string5 = configurationSection.contains("material" + string3) ? configurationSection.getString("material" + string3) : configurationSection.getString("material");
            material = this.material(string, string2, string5, material);
            string4 = configurationSection.getString("name" + string3, configurationSection.getString("name", string4));
            if (configurationSection.contains("lore" + string3)) {
                list = configurationSection.getStringList("lore" + string3);
            } else if (configurationSection.contains("lore")) {
                list = configurationSection.getStringList("lore");
            }
            if (configurationSection.contains("glow" + string3)) {
                bl2 = configurationSection.getBoolean("glow" + string3);
            } else if (configurationSection.contains("glow")) {
                bl2 = configurationSection.getBoolean("glow");
            }
        }
        return this.assemble(material, this.amount(string, string2, face3.amount), string4, list, bl2, map, map2);
    }

    public Items back(String string, String string2) {
        return this.face(string, string2, null, Face.of(Material.ARROW, BACK_NAME, BACK_LORE));
    }

    public Items face(String string, String string2, Map<String, String> map, Face face) {
        Boolean bl;
        ConfigurationSection configurationSection = this.itemSection(string, string2);
        Material material = this.material(string, string2, configurationSection, face.material);
        String string3 = configurationSection == null ? face.name : configurationSection.getString("name", face.name);
        List<String> list = this.lore(configurationSection, face.lore);
        Boolean bl2 = bl = face.glow ? Boolean.TRUE : null;
        if (configurationSection != null && configurationSection.contains("glow")) {
            bl = configurationSection.getBoolean("glow");
        }
        return this.assemble(material, this.amount(string, string2, face.amount), string3, list, bl, map, null);
    }

    private Items assemble(Material material, int n, String string, List<String> list, Boolean bl, Map<String, String> map, Map<String, List<String>> map2) {
        Map<String, String> map3 = this.withTheme(map);
        Items items = Items.of(material, n).rawName(Tokens.apply(string, map3));
        ArrayList<String> arrayList = new ArrayList<String>(list.size());
        for (String string2 : list) {
            List<String> list2;
            String string3;
            String string4 = string3 = string2 == null ? "" : string2.trim();
            if (map2 != null && string3.length() > 2 && string3.charAt(0) == '{' && string3.charAt(string3.length() - 1) == '}' && (list2 = map2.get(string3.substring(1, string3.length() - 1))) != null) {
                for (String string5 : list2) {
                    arrayList.add(Tokens.apply(string5, map3));
                }
                continue;
            }
            arrayList.add(Tokens.apply(string2, map3));
        }
        items.rawLore(arrayList.toArray(new String[0]));
        return items.glint(bl);
    }

    public String name(String string, String string2, Map<String, String> map) {
        ConfigurationSection configurationSection = this.itemSection(string, string2);
        if (configurationSection == null) {
            return null;
        }
        return Tokens.apply(configurationSection.getString("name"), this.withTheme(map));
    }

    public String miniName(String string, String string2, String string3, Map<String, String> map) {
        ConfigurationSection configurationSection = this.itemSection(string, string2);
        String string4 = configurationSection == null ? string3 : configurationSection.getString("name", string3);
        return Tokens.apply(string4, this.withTheme(map));
    }

    public List<String> lore(String string, String string2, Map<String, String> map, String ... stringArray) {
        return this.lore(string, string2, map, (Map<String, List<String>>)null, stringArray);
    }

    public List<String> lore(String string, String string2, Map<String, String> map, Map<String, List<String>> map2, String ... stringArray) {
        List<String> list = this.lore(this.itemSection(string, string2), stringArray);
        Map<String, String> map3 = this.withTheme(map);
        ArrayList<String> arrayList = new ArrayList<String>(list.size());
        for (String string3 : list) {
            List<String> list2;
            String string4;
            String string5 = string4 = string3 == null ? "" : string3.trim();
            if (map2 != null && string4.length() > 2 && string4.charAt(0) == '{' && string4.charAt(string4.length() - 1) == '}' && (list2 = map2.get(string4.substring(1, string4.length() - 1))) != null) {
                for (String string6 : list2) {
                    arrayList.add(Tokens.apply(string6, map3));
                }
                continue;
            }
            arrayList.add(Tokens.apply(string3, map3));
        }
        return arrayList;
    }

    public String line(String string, String string2, String string3, String string4, Map<String, String> map) {
        ConfigurationSection configurationSection = this.itemSection(string, string2);
        String string5 = configurationSection == null ? string4 : configurationSection.getString(string3, string4);
        return Tokens.apply(string5, this.withTheme(map));
    }

    public Material material(String string, String string2, Material material) {
        return this.material(string, string2, this.itemSection(string, string2), material);
    }

    public int amount(String string, String string2, int n) {
        ConfigurationSection configurationSection = this.itemSection(string, string2);
        if (configurationSection == null || !configurationSection.contains("amount")) {
            return n;
        }
        return Math.max(1, Math.min(64, configurationSection.getInt("amount", n)));
    }

    private List<String> lore(ConfigurationSection configurationSection, String[] stringArray) {
        if (configurationSection == null || !configurationSection.contains("lore")) {
            return Arrays.asList(stringArray);
        }
        return configurationSection.getStringList("lore");
    }

    private Material material(String string, String string2, ConfigurationSection configurationSection, Material material) {
        return configurationSection == null ? material : this.material(string, string2, configurationSection.getString("material"), material);
    }

    private Material material(String string, String string2, String string3, Material material) {
        if (string3 == null || string3.isEmpty()) {
            return material;
        }
        Material material2 = Material.matchMaterial((String)string3);
        if (material2 != null && material2.isItem()) {
            return material2;
        }
        this.plugin.getLogger().warning("gui/" + string + ".yml: " + string2 + " has material '" + string3 + "', which is not an item - using " + String.valueOf(material) + ".");
        return material;
    }

    public static final class Face {
        private final Material material;
        private final String name;
        private final String[] lore;
        private boolean glow;
        private int amount = 1;

        private Face(Material material, String string, String[] stringArray) {
            this.material = material;
            this.name = string;
            this.lore = stringArray;
        }

        public static Face of(Material material, String string, String ... stringArray) {
            return new Face(material, string, stringArray);
        }

        public Face glow() {
            this.glow = true;
            return this;
        }

        public Face amount(int n) {
            this.amount = Math.max(1, Math.min(64, n));
            return this;
        }
    }
}

