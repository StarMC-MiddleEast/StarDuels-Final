package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.model.Kit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class CategoryManager {
    private final MeowDuels plugin;
    private final File file;
    private final String resourceName;
    private final Map<String, Category> categories = new LinkedHashMap<String, Category>();

    public CategoryManager(MeowDuels meowDuels) {
        this(meowDuels, "categories.yml");
    }

    public CategoryManager(MeowDuels meowDuels, String string) {
        this.plugin = meowDuels;
        this.resourceName = string;
        this.file = new File(meowDuels.getDataFolder(), string);
        if (!this.file.exists()) {
            meowDuels.saveResource(string, false);
        }
        this.load();
        if ("queuecategories.yml".equalsIgnoreCase(string)) {
            this.ensureQueueCategories();
        }
    }

    private void ensureQueueCategories() {
        boolean bl = false;
        String[][] stringArrayArray = new String[][]{{"pvptiers", "<gradient:#FFE27A:#F2A900>PVP TIERS</gradient>", "Sword", "ModernAxe", "UHC", "ModernUHC"}, {"mctiers", "<gradient:#FFE27A:#F2A900>MC TIERS</gradient>", "NetheriteOP", "Potion", "ModernSMP", "PotionIO", "MoneySMP"}, {"subtiers", "<gradient:#FFE27A:#F2A900>SUB TIERS</gradient>", "Speed", "Creeper", "CartHT", "CartLT", "OGV", "Bow"}, {"startiers", "<gradient:#FFE27A:#F2A900>STAR TIERS</gradient>", "Mace", "CrystalIO", "Elytra", "Trident"}};
        for (String[] object : stringArrayArray) {
            String string3 = object[0];
            Category category = this.categories.get(string3);
            if (category == null) {
                category = new Category(string3, object[1]);
                category.getKits().addAll(Arrays.asList(object).subList(2, object.length));
                this.categories.put(string3, category);
                bl = true;
                continue;
            }
            if (!"MCTIERS.COM".equalsIgnoreCase(category.getHeader()) && !"SUBTIERS".equalsIgnoreCase(category.getHeader())) continue;
            category.setHeader(object[1]);
            bl = true;
        }
        Category category = this.categories.get("pvptiers");
        if (category != null) {
            for (Kit kit : this.plugin.getKitManager().all()) {
                if (!category.getKits().stream().noneMatch(string -> string.equalsIgnoreCase(kit.getName()))) continue;
                category.getKits().add(kit.getName());
                bl = true;
            }
            Category category2 = this.categories.get("startiers");
            if (category2 != null) {
                for (String string4 : category.getKits()) {
                    if (!category2.getKits().stream().noneMatch(string2 -> string2.equalsIgnoreCase(string4))) continue;
                    category2.getKits().add(string4);
                    bl = true;
                }
            }
        }
        if (bl) {
            this.save();
        }
    }

    private static String safeId(String string) {
        String string2 = string.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        return string2.isEmpty() ? "category" : string2;
    }

    public void load() {
        YamlConfiguration yamlConfiguration;
        ConfigurationSection configurationSection;
        this.categories.clear();
        if (this.file.exists() && (configurationSection = (yamlConfiguration = YamlConfiguration.loadConfiguration((File)this.file)).getConfigurationSection("categories")) != null) {
            for (String string : configurationSection.getKeys(false)) {
                ConfigurationSection configurationSection2 = configurationSection.getConfigurationSection(string);
                if (configurationSection2 == null) continue;
                Category category = new Category(string, configurationSection2.getString("header", string.toUpperCase(Locale.ROOT)));
                category.getKits().addAll(configurationSection2.getStringList("kits"));
                this.categories.put(string.toLowerCase(Locale.ROOT), category);
            }
        }
    }

    public void save() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        for (Category category : this.categories.values()) {
            ConfigurationSection configurationSection = yamlConfiguration.createSection("categories." + category.getId());
            configurationSection.set("header", (Object)category.getHeader());
            configurationSection.set("kits", new ArrayList<String>(category.getKits()));
        }
        try {
            yamlConfiguration.save(this.file);
        }
        catch (IOException iOException) {
            this.plugin.getLogger().severe("Failed to save categories.yml: " + iOException.getMessage());
        }
    }

    public List<Category> all() {
        return new ArrayList<Category>(this.categories.values());
    }

    public Category get(String string) {
        return string == null ? null : this.categories.get(CategoryManager.safeId(string));
    }

    public boolean create(String string, String string2) {
        String string3 = CategoryManager.safeId(string);
        if (this.categories.containsKey(string3)) {
            return false;
        }
        this.categories.put(string3, new Category(string3, string2 == null ? string.toUpperCase(Locale.ROOT) : string2));
        this.save();
        return true;
    }

    public void delete(String string) {
        this.categories.remove(CategoryManager.safeId(string));
        this.save();
    }

    public List<String> kitsFor(Category category) {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String object : category.getKits()) {
            if (!this.plugin.getKitManager().exists(object)) continue;
            arrayList.add(object);
        }
        if ("startiers".equalsIgnoreCase(category.getId())) {
            for (Kit kit : this.plugin.getKitManager().all()) {
                boolean bl = false;
                for (Category category2 : this.categories.values()) {
                    if (!category2.getKits().stream().anyMatch(string -> string.equalsIgnoreCase(kit.getName()))) continue;
                    bl = true;
                    break;
                }
                if (bl || !arrayList.stream().noneMatch(string -> string.equalsIgnoreCase(kit.getName()))) continue;
                arrayList.add(kit.getName());
            }
        }
        return arrayList;
    }

    public static final class Category {
        private final String id;
        private String header;
        private final List<String> kits = new ArrayList<String>();

        Category(String string, String string2) {
            this.id = string;
            this.header = string2;
        }

        public String getId() {
            return this.id;
        }

        public String getHeader() {
            return this.header;
        }

        public void setHeader(String string) {
            this.header = string;
        }

        public List<String> getKits() {
            return this.kits;
        }
    }
}

