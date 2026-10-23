package com.meowduels.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public final class GameModeGuard {
    private static final Map<UUID, GameMode> PINNED = new HashMap<UUID, GameMode>();
    private static boolean applying = false;

    private GameModeGuard() {
    }

    public static void pin(Player player, GameMode gameMode) {
        if (player == null || gameMode == null) {
            return;
        }
        PINNED.put(player.getUniqueId(), gameMode);
        applying = true;
        try {
            player.setGameMode(gameMode);
        }
        finally {
            applying = false;
        }
    }

    public static void release(UUID uUID) {
        if (uUID != null) {
            PINNED.remove(uUID);
        }
    }

    public static GameMode required(UUID uUID) {
        return uUID == null ? null : PINNED.get(uUID);
    }

    public static boolean isApplying() {
        return applying;
    }

    public static void setFreely(Player player, GameMode gameMode) {
        if (player == null || gameMode == null) {
            return;
        }
        GameModeGuard.release(player.getUniqueId());
        applying = true;
        try {
            player.setGameMode(gameMode);
        }
        finally {
            applying = false;
        }
    }
}

