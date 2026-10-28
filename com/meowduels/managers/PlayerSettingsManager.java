package com.meowduels.managers;

import com.meowduels.MeowDuels;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerSettingsManager {
    private final MeowDuels plugin;
    private final File file;
    private final Map<String, Map<UUID, Boolean>> flags = new ConcurrentHashMap<String, Map<UUID, Boolean>>();
    private static final String[] KEYS = new String[]{"duel-requests", "scoreboard", "party-invites", "spectators", "sounds", "isolated-chat", "keep-queuing"};
    private static final Set<String> OFF_BY_DEFAULT = new HashSet<String>(Arrays.asList("isolated-chat", "keep-queuing"));

    public PlayerSettingsManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.file = new File(meowDuels.getDataFolder(), "settings.yml");
        this.load();
    }

    private void load() {
        if (!this.file.exists()) {
            return;
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration((File)this.file);
        for (String string : KEYS) {
            this.loadSection(yamlConfiguration.getConfigurationSection(string), this.map(string));
        }
    }

    private void loadSection(ConfigurationSection configurationSection, Map<UUID, Boolean> map) {
        if (configurationSection == null) {
            return;
        }
        for (String string : configurationSection.getKeys(false)) {
            try {
                map.put(UUID.fromString(string), configurationSection.getBoolean(string));
            }
            catch (Exception exception) {}
        }
    }

    public void save() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        for (String string : KEYS) {
            for (Map.Entry<UUID, Boolean> entry : this.map(string).entrySet()) {
                yamlConfiguration.set(string + "." + String.valueOf(entry.getKey()), (Object)entry.getValue());
            }
        }
        try {
            yamlConfiguration.save(this.file);
        }
        catch (IOException iOException) {
            this.plugin.getLogger().warning("Failed to save settings.yml: " + iOException.getMessage());
        }
    }

    private Map<UUID, Boolean> map(String string2) {
        return this.flags.computeIfAbsent(string2, string -> new ConcurrentHashMap());
    }

    public static boolean defaultOf(String string) {
        return !OFF_BY_DEFAULT.contains(string);
    }

    public boolean is(String string, UUID uUID) {
        Boolean bl = this.map(string).get(uUID);
        return bl == null ? PlayerSettingsManager.defaultOf(string) : bl;
    }

    public void toggle(String string, UUID uUID) {
        this.map(string).put(uUID, !this.is(string, uUID));
        this.save();
    }

    public boolean isDuelRequests(UUID uUID) {
        return this.is("duel-requests", uUID);
    }

    public boolean isScoreboard(UUID uUID) {
        return this.is("scoreboard", uUID);
    }

    public boolean isPartyInvites(UUID uUID) {
        return this.is("party-invites", uUID);
    }

    public boolean isSpectators(UUID uUID) {
        return this.is("spectators", uUID);
    }

    public boolean isSounds(UUID uUID) {
        return this.is("sounds", uUID);
    }

    public boolean isIsolatedChat(UUID uUID) {
        return this.is("isolated-chat", uUID);
    }

    public boolean isButtonsGui(UUID uuid) {
        // Native Paper dialogs are the fixed player-facing menu style.
        return true;
    }

    public void toggleDuelRequests(UUID uUID) {
        this.toggle("duel-requests", uUID);
    }

    public void toggleScoreboard(UUID uUID) {
        this.toggle("scoreboard", uUID);
    }
}

