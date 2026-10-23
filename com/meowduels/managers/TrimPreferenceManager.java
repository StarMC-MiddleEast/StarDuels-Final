package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.util.Trims;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class TrimPreferenceManager {
    private final MeowDuels plugin;
    private final File file;
    private final Map<UUID, Map<String, String[]>> prefs = new HashMap<UUID, Map<String, String[]>>();

    public TrimPreferenceManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.file = new File(meowDuels.getDataFolder(), "trims.yml");
        this.load();
    }

    private void load() {
        if (!this.file.exists()) {
            return;
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration((File)this.file);
        for (String string : yamlConfiguration.getKeys(false)) {
            UUID uUID;
            try {
                uUID = UUID.fromString(string);
            }
            catch (Exception exception) {
                continue;
            }
            ConfigurationSection configurationSection = yamlConfiguration.getConfigurationSection(string);
            if (configurationSection == null) continue;
            for (String string2 : configurationSection.getKeys(false)) {
                ConfigurationSection configurationSection2 = configurationSection.getConfigurationSection(string2);
                if (configurationSection2 == null) continue;
                String[] stringArray = new String[4];
                for (String string3 : configurationSection2.getKeys(false)) {
                    try {
                        int n = Integer.parseInt(string3);
                        if (n < 0 || n >= 4) continue;
                        stringArray[n] = configurationSection2.getString(string3);
                    }
                    catch (NumberFormatException numberFormatException) {}
                }
                this.byKit(uUID).put(string2.toLowerCase(Locale.ROOT), stringArray);
            }
        }
    }

    public void save() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        for (Map.Entry<UUID, Map<String, String[]>> entry : this.prefs.entrySet()) {
            for (Map.Entry<String, String[]> entry2 : entry.getValue().entrySet()) {
                String[] stringArray = entry2.getValue();
                for (int i = 0; i < 4; ++i) {
                    if (stringArray[i] == null) continue;
                    yamlConfiguration.set(String.valueOf(entry.getKey()) + "." + entry2.getKey() + "." + i, (Object)stringArray[i]);
                }
            }
        }
        try {
            yamlConfiguration.save(this.file);
        }
        catch (IOException iOException) {
            this.plugin.getLogger().warning("Failed to save trims.yml: " + iOException.getMessage());
        }
    }

    private Map<String, String[]> byKit(UUID uUID2) {
        return this.prefs.computeIfAbsent(uUID2, uUID -> new HashMap());
    }

    private String[] slotsFor(UUID uUID, String string, boolean bl) {
        String[] stringArray;
        Map<String, String[]> map = this.prefs.get(uUID);
        String string2 = string.toLowerCase(Locale.ROOT);
        if (map == null) {
            if (!bl) {
                return null;
            }
            map = this.byKit(uUID);
        }
        if ((stringArray = map.get(string2)) == null && bl) {
            stringArray = new String[4];
            map.put(string2, stringArray);
        }
        return stringArray;
    }

    public void set(UUID uUID, String string, int n, String string2, String string3) {
        if (n < 0 || n > 3) {
            return;
        }
        this.slotsFor((UUID)uUID, (String)string, (boolean)true)[n] = string2 + ":" + string3;
        this.save();
    }

    public void setAll(UUID uUID, String string, String string2, String string3) {
        String[] stringArray = this.slotsFor(uUID, string, true);
        String string4 = string2 + ":" + string3;
        for (int i = 0; i < 4; ++i) {
            stringArray[i] = string4;
        }
        this.save();
    }

    public void clear(UUID uUID, String string) {
        Map<String, String[]> map = this.prefs.get(uUID);
        if (map != null) {
            map.remove(string.toLowerCase(Locale.ROOT));
            this.save();
        }
    }

    public String[] get(UUID uUID, String string, int n) {
        String[] stringArray = this.slotsFor(uUID, string, false);
        if (stringArray == null || n < 0 || n > 3 || stringArray[n] == null) {
            return null;
        }
        int n2 = stringArray[n].indexOf(58);
        if (n2 < 0) {
            return null;
        }
        return new String[]{stringArray[n].substring(0, n2), stringArray[n].substring(n2 + 1)};
    }

    public void applyToArmor(UUID uUID, String string, ItemStack[] itemStackArray) {
        String[] stringArray = this.slotsFor(uUID, string, false);
        if (stringArray == null || itemStackArray == null) {
            return;
        }
        for (int i = 0; i < 4 && i < itemStackArray.length; ++i) {
            int n;
            if (stringArray[i] == null || itemStackArray[i] == null || (n = stringArray[i].indexOf(58)) < 0) continue;
            Trims.apply(itemStackArray[i], stringArray[i].substring(0, n), stringArray[i].substring(n + 1));
        }
    }
}

