package com.meowduels.managers;

import com.meowduels.MeowDuels;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;

/** Loads all title and subtitle text from titles.yml. */
public final class TitlesManager {
    private final MeowDuels plugin;
    private final File file;
    private YamlConfiguration config;

    public TitlesManager(MeowDuels plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "titles.yml");
        this.reload();
    }

    public void reload() {
        boolean migrateLegacyMessages = !this.file.exists();
        YamlConfiguration legacy = migrateLegacyMessages
                ? YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "messages.yml"))
                : null;
        if (migrateLegacyMessages) {
            try {
                this.plugin.saveResource("titles.yml", false);
            } catch (IllegalArgumentException ignored) {
                // A generated default configuration below still supports older JARs.
            }
            this.config = this.file.exists() ? YamlConfiguration.loadConfiguration(this.file) : new YamlConfiguration();
            for (Map.Entry<String, String> entry : this.titleDefaults().entrySet()) {
                String key = entry.getKey();
                if (legacy != null && legacy.contains(key)) {
                    this.config.set(key, legacy.getString(key, entry.getValue()));
                } else if (!this.config.contains(key)) {
                    this.config.set(key, entry.getValue());
                }
            }
            this.save();
        } else {
            this.config = YamlConfiguration.loadConfiguration(this.file);
            boolean changed = false;
            for (Map.Entry<String, String> entry : this.titleDefaults().entrySet()) {
                if (this.config.contains(entry.getKey())) continue;
                this.config.set(entry.getKey(), entry.getValue());
                changed = true;
            }
            if (changed) this.save();
        }
    }

    public String raw(String key) {
        String value = this.config == null ? null : this.config.getString(key);
        if (value != null) return value;
        return this.titleDefaults().getOrDefault(key, "<red>Missing title: " + key + "</red>");
    }

    private Map<String, String> titleDefaults() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : MessageManager.defaultMessages().entrySet()) {
            if (MessageManager.isTitleKey(entry.getKey())) result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private void save() {
        try {
            this.config.save(this.file);
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Failed to write titles.yml: " + exception.getMessage());
        }
    }
}
