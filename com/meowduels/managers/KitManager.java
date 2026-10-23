package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.model.Kit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class KitManager {
    private final MeowDuels plugin;
    private final File file;
    private final Map<String, Kit> kits = new LinkedHashMap<String, Kit>();

    public KitManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.file = new File(meowDuels.getDataFolder(), "kits.yml");
        if (!this.file.exists()) {
            meowDuels.saveResource("kits.yml", false);
        }
        this.load();
    }

    public void load() {
        this.kits.clear();
        if (!this.file.exists()) {
            return;
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration((File)this.file);
        ConfigurationSection configurationSection = yamlConfiguration.getConfigurationSection("kits");
        if (configurationSection == null) {
            return;
        }
        for (String string : configurationSection.getKeys(false)) {
            ConfigurationSection configurationSection2 = configurationSection.getConfigurationSection(string);
            if (configurationSection2 == null) continue;
            this.kits.put(string.toLowerCase(Locale.ROOT), Kit.load(string, configurationSection2));
        }
        this.plugin.getLogger().info("Loaded " + this.kits.size() + " kit(s).");
    }

    public void save() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        for (Kit kit : this.kits.values()) {
            kit.save(yamlConfiguration.createSection("kits." + kit.getName()));
        }
        try {
            yamlConfiguration.save(this.file);
        }
        catch (IOException iOException) {
            this.plugin.getLogger().severe("Failed to save kits.yml: " + iOException.getMessage());
        }
    }

    public Kit get(String string) {
        return string == null ? null : this.kits.get(string.toLowerCase(Locale.ROOT));
    }

    public boolean exists(String string) {
        return this.get(string) != null;
    }

    public void put(Kit kit) {
        this.kits.put(kit.getName().toLowerCase(Locale.ROOT), kit);
        this.save();
    }

    public void delete(String string) {
        this.kits.remove(string.toLowerCase(Locale.ROOT));
        this.save();
    }

    public List<Kit> all() {
        return new ArrayList<Kit>(this.kits.values());
    }

    public boolean isEmpty() {
        return this.kits.isEmpty();
    }
}

