package com.meowduels.managers;

import com.meowduels.MeowDuels;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class TabEditManager {
    private final MeowDuels plugin;
    private final Map<UUID, Field> pending = new HashMap<UUID, Field>();

    public TabEditManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    public boolean isEditing(UUID uUID) {
        return this.pending.containsKey(uUID);
    }

    public void begin(Player player, Field field) {
        this.pending.put(player.getUniqueId(), field);
        player.sendMessage(this.plugin.messages().get("notice.tab.type-new-chat", "label", String.valueOf(field.getLabel())));
        player.sendMessage(this.plugin.messages().get("notice.tab.type-cancel-abort", new String[0]));
    }

    public void cancel(Player player) {
        this.pending.remove(player.getUniqueId());
    }

    public void handleInput(Player player, String string) {
        Field field = this.pending.remove(player.getUniqueId());
        if (field == null) {
            return;
        }
        if (string.equalsIgnoreCase("cancel")) {
            player.sendMessage(this.plugin.messages().get("notice.tab.cancelled", new String[0]));
            return;
        }
        switch (field.ordinal()) {
            case 0: {
                this.plugin.setTabTitle(string);
                break;
            }
            case 1: {
                this.plugin.setTabDiscord(string);
                break;
            }
            case 2: {
                this.plugin.setTabStore(string);
            }
        }
        player.sendMessage(this.plugin.messages().get("notice.tab.tab-updated", "value", String.valueOf(field.name().toLowerCase())));
    }

    public static enum Field {
        TITLE("tab title (MiniMessage, gradients allowed)"),
        DISCORD("discord line"),
        STORE("store line");

        private final String label;

        private Field(String string2) {
            this.label = string2;
        }

        public String getLabel() {
            return this.label;
        }
    }
}

