package com.meowduels.managers;

import com.meowduels.MeowDuels;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class GuiLayoutManager {
    public static final String DUEL_CONFIRM = "duelconfirm";
    public static final String MAP_SELECT = "mapselect";
    public static final String KIT_MENU = "kitmenu";
    private final MeowDuels plugin;
    private final File file;
    private final Map<String, Map<Integer, ItemStack>> layouts = new HashMap<String, Map<Integer, ItemStack>>();

    public GuiLayoutManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.file = new File(meowDuels.getDataFolder(), "gui-layouts.yml");
        this.load();
    }

    public static int rows(String string) {
        return 4;
    }

    public static int editableSize(String string) {
        return DUEL_CONFIRM.equals(string) ? 27 : 36;
    }

    public static boolean isValidMenu(String string) {
        return string.equals(DUEL_CONFIRM) || string.equals(MAP_SELECT);
    }

    public static String categoryMenuId(String string) {
        return "category:" + string.toLowerCase(Locale.ROOT);
    }

    public static boolean isCategoryMenu(String string) {
        return string.startsWith("category:");
    }

    public void load() {
        this.layouts.clear();
        if (!this.file.exists()) {
            return;
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration((File)this.file);
        ConfigurationSection configurationSection = yamlConfiguration.getConfigurationSection("layouts");
        if (configurationSection == null) {
            return;
        }
        for (String string : configurationSection.getKeys(false)) {
            ConfigurationSection configurationSection2 = configurationSection.getConfigurationSection(string);
            if (configurationSection2 == null) continue;
            LinkedHashMap<Integer, ItemStack> linkedHashMap = new LinkedHashMap<Integer, ItemStack>();
            for (String string2 : configurationSection2.getKeys(false)) {
                try {
                    int n = Integer.parseInt(string2);
                    ItemStack itemStack = configurationSection2.getItemStack(string2);
                    if (itemStack == null) continue;
                    linkedHashMap.put(n, itemStack);
                }
                catch (NumberFormatException numberFormatException) {}
            }
            if (linkedHashMap.isEmpty()) continue;
            this.layouts.put(string, linkedHashMap);
        }
    }

    public void save() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        for (Map.Entry<String, Map<Integer, ItemStack>> entry : this.layouts.entrySet()) {
            ConfigurationSection configurationSection = yamlConfiguration.createSection("layouts." + entry.getKey());
            for (Map.Entry<Integer, ItemStack> entry2 : entry.getValue().entrySet()) {
                configurationSection.set(String.valueOf(entry2.getKey()), (Object)entry2.getValue());
            }
        }
        try {
            yamlConfiguration.save(this.file);
        }
        catch (IOException iOException) {
            this.plugin.getLogger().severe("Failed to save gui-layouts.yml: " + iOException.getMessage());
        }
    }

    public boolean has(String string) {
        Map<Integer, ItemStack> map = this.layouts.get(string);
        return map != null && !map.isEmpty();
    }

    public Map<Integer, ItemStack> get(String string) {
        Map<Integer, ItemStack> map = this.layouts.get(string);
        return map == null ? new LinkedHashMap<Integer, ItemStack>() : new LinkedHashMap<Integer, ItemStack>(map);
    }

    public void set(String string, Map<Integer, ItemStack> map) {
        this.layouts.put(string, new LinkedHashMap<Integer, ItemStack>(map));
        this.save();
    }

    public void clear(String string) {
        this.layouts.remove(string);
        this.save();
    }
}

