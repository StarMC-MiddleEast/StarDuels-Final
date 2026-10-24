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
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

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
        boolean migratedModernSmpShield = false;
        boolean migratedModernSmpIcon = false;
        boolean migratedGuiVisibility = false;
        for (String string : configurationSection.getKeys(false)) {
            ConfigurationSection configurationSection2 = configurationSection.getConfigurationSection(string);
            if (configurationSection2 == null) continue;
            Kit kit = Kit.load(string, configurationSection2);
            if (!configurationSection2.contains("gui-visible")) {
                kit.setGuiVisible(com.meowduels.util.KitCatalog.defaultVisible(string));
                migratedGuiVisibility = true;
            }
            if (string.equalsIgnoreCase("ModernSMP") && (kit.getOffhand() == null || kit.getOffhand().getType() == Material.AIR)) {
                kit.setOffhand(new ItemStack(Material.SHIELD));
                migratedModernSmpShield = true;
            }
            if (string.equalsIgnoreCase("ModernSMP") && kit.getIcon() != Material.SHIELD) {
                kit.setIcon(Material.SHIELD);
                migratedModernSmpIcon = true;
            }
            if (string.equalsIgnoreCase("ModernSMP") && "item/totem_of_undying".equalsIgnoreCase(kit.getNameIcon())) {
                kit.setNameIcon("none");
                migratedModernSmpIcon = true;
            }
            this.kits.put(string.toLowerCase(Locale.ROOT), kit);
        }
        if (!this.exists("SpearMace") && this.exists("Trident")) {
            Kit legacy = this.get("Trident");
            Kit spearMace = new Kit("SpearMace");
            spearMace.setContents(legacy.getContents());
            spearMace.setArmor(legacy.getArmor());
            spearMace.setOffhand(legacy.getOffhand());
            spearMace.setDisplayName("<gradient:#FFF36A:#F39A22><bold>SPEAR MACE</bold></gradient>");
            spearMace.setGuiVisible(true);
            Material spear = Material.matchMaterial("IRON_SPEAR");
            if (spear == null) spear = Material.matchMaterial("SPEAR");
            spearMace.setIcon(spear == null ? Material.TRIDENT : spear);
            spearMace.getStartEffects().addAll(legacy.getStartEffects());
            this.kits.put(spearMace.getName().toLowerCase(Locale.ROOT), spearMace);
            migratedGuiVisibility = true;
        }
        if (migratedModernSmpShield || migratedModernSmpIcon || migratedGuiVisibility) {
            this.save();
            if (migratedModernSmpShield) this.plugin.getLogger().info("Added the missing offhand shield to the ModernSMP kit.");
            if (migratedModernSmpIcon) this.plugin.getLogger().info("Updated the ModernSMP menu icon to a shield.");
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

