package com.meowduels.managers;

import com.meowduels.MeowDuels;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class StatsManager {
    private final MeowDuels plugin;
    private final File file;
    private final Map<UUID, Integer> streak = new ConcurrentHashMap<UUID, Integer>();
    private final Map<UUID, Integer> wins = new ConcurrentHashMap<UUID, Integer>();
    private final Map<UUID, Integer> losses = new ConcurrentHashMap<UUID, Integer>();
    private final Map<UUID, Integer> played = new ConcurrentHashMap<UUID, Integer>();

    public StatsManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.file = new File(meowDuels.getDataFolder(), "stats.yml");
        this.load();
    }

    private void load() {
        if (!this.file.exists()) {
            return;
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration((File)this.file);
        this.loadSection(yamlConfiguration.getConfigurationSection("streak"), this.streak);
        this.loadSection(yamlConfiguration.getConfigurationSection("wins"), this.wins);
        this.loadSection(yamlConfiguration.getConfigurationSection("losses"), this.losses);
        this.loadSection(yamlConfiguration.getConfigurationSection("played"), this.played);
    }

    private void loadSection(ConfigurationSection configurationSection, Map<UUID, Integer> map) {
        if (configurationSection == null) {
            return;
        }
        for (String string : configurationSection.getKeys(false)) {
            try {
                map.put(UUID.fromString(string), configurationSection.getInt(string, 0));
            }
            catch (Exception exception) {}
        }
    }

    public void save() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        for (Map.Entry<UUID, Integer> entry : this.streak.entrySet()) {
            yamlConfiguration.set("streak." + String.valueOf(entry.getKey()), (Object)entry.getValue());
        }
        for (Map.Entry<UUID, Integer> entry : this.wins.entrySet()) {
            yamlConfiguration.set("wins." + String.valueOf(entry.getKey()), (Object)entry.getValue());
        }
        for (Map.Entry<UUID, Integer> entry : this.losses.entrySet()) {
            yamlConfiguration.set("losses." + String.valueOf(entry.getKey()), (Object)entry.getValue());
        }
        for (Map.Entry<UUID, Integer> entry : this.played.entrySet()) {
            yamlConfiguration.set("played." + String.valueOf(entry.getKey()), (Object)entry.getValue());
        }
        try {
            yamlConfiguration.save(this.file);
        }
        catch (IOException iOException) {
            this.plugin.getLogger().warning("Failed to save stats.yml: " + iOException.getMessage());
        }
    }

    public void addWin(UUID uUID) {
        this.streak.merge(uUID, 1, Integer::sum);
    }

    public void resetStreak(UUID uUID) {
        this.streak.put(uUID, 0);
    }

    public int getStreak(UUID uUID) {
        Integer n = this.streak.get(uUID);
        return n == null ? 0 : n;
    }

    public void addDuelWin(UUID uUID) {
        this.wins.merge(uUID, 1, Integer::sum);
    }

    public void addDuelLoss(UUID uUID) {
        this.losses.merge(uUID, 1, Integer::sum);
    }

    public int getWins(UUID uUID) {
        Integer n = this.wins.get(uUID);
        return n == null ? 0 : n;
    }

    public int getLosses(UUID uUID) {
        Integer n = this.losses.get(uUID);
        return n == null ? 0 : n;
    }

    public int getPlayed(UUID uUID) {
        Integer n = this.played.get(uUID);
        return n == null ? 0 : n;
    }

    public void addPlayed(UUID uUID) {
        this.played.merge(uUID, 1, Integer::sum);
    }

    public List<Map.Entry<UUID, Integer>> topWins(int n) {
        ArrayList<Map.Entry<UUID, Integer>> arrayList = new ArrayList<Map.Entry<UUID, Integer>>();
        for (Map.Entry<UUID, Integer> entry : this.wins.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) continue;
            arrayList.add(entry);
        }
        Collections.sort(arrayList, new Comparator<Map.Entry<UUID, Integer>>(){

            @Override
            public int compare(Map.Entry<UUID, Integer> entry, Map.Entry<UUID, Integer> entry2) {
                return entry2.getValue() - entry.getValue();
            }
        });
        return arrayList.size() > n ? arrayList.subList(0, n) : arrayList;
    }
}

