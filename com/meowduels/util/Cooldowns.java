package com.meowduels.util;

import com.meowduels.util.Sounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class Cooldowns {
    private static final Map<String, Map<UUID, Long>> ENTRIES = new HashMap<String, Map<UUID, Long>>();
    private static final Map<String, Map<UUID, Material>> ICONS = new HashMap<String, Map<UUID, Material>>();

    private Cooldowns() {
    }

    public static void start(Player player, String string, Material material, long l) {
        if (player == null || l <= 0L) {
            return;
        }
        Cooldowns.entries(string).put(player.getUniqueId(), System.currentTimeMillis() + l);
        if (material != null) {
            Cooldowns.icons(string).put(player.getUniqueId(), material);
            Cooldowns.sweep(player, material, l);
        }
    }

    public static long remaining(Player player, String string) {
        if (player == null) {
            return 0L;
        }
        Long l = Cooldowns.entries(string).get(player.getUniqueId());
        if (l == null) {
            return 0L;
        }
        long l2 = l - System.currentTimeMillis();
        if (l2 <= 0L) {
            Cooldowns.entries(string).remove(player.getUniqueId());
            return 0L;
        }
        return l2;
    }

    public static boolean active(Player player, String string) {
        return Cooldowns.remaining(player, string) > 0L;
    }

    public static void clear(Player player, String string) {
        if (player != null) {
            Cooldowns.entries(string).remove(player.getUniqueId());
        }
    }

    public static void clearAll(UUID uUID) {
        for (Map<UUID, Long> map : ENTRIES.values()) {
            map.remove(uUID);
        }
        for (Map<UUID, Material> map : ICONS.values()) {
            map.remove(uUID);
        }
    }

    public static void tick(Player player) {
        if (player == null) {
            return;
        }
        UUID uUID = player.getUniqueId();
        for (Map.Entry<String, Map<UUID, Long>> entry : ENTRIES.entrySet()) {
            Long l = entry.getValue().get(uUID);
            if (l == null) continue;
            long l2 = l - System.currentTimeMillis();
            if (l2 <= 0L) {
                entry.getValue().remove(uUID);
                Cooldowns.icons(entry.getKey()).remove(uUID);
                Sounds.refreshed(player);
                continue;
            }
            Material material = Cooldowns.icons(entry.getKey()).get(uUID);
            if (material == null) continue;
            Cooldowns.sweep(player, material, l2);
        }
    }

    public static String bar(long l, long l2, int n) {
        int n2;
        int n3;
        long l3 = Math.max(1L, l2);
        int n4 = n3 = Math.max(1, n);
        int n5 = n2 = (int)Math.round((double)Math.max(0L, l) / (double)l3 * (double)n4);
        if (n2 > n4) {
            n2 = n4;
        }
        StringBuilder stringBuilder = new StringBuilder(n4);
        for (int i = 0; i < n4; ++i) {
            stringBuilder.append(i < n2 ? (char)'\u2588' : '\u2591');
        }
        return stringBuilder.toString();
    }

    public static String seconds(long l) {
        long l2 = (l + 99L) / 100L;
        return l2 / 10L + "." + l2 % 10L;
    }

    private static void sweep(Player player, Material material, long l) {
        try {
            int n = (int)Math.max(1L, (l + 49L) / 50L);
            if (player.getCooldown(material) < n) {
                player.setCooldown(material, n);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Map<UUID, Material> icons(String string) {
        Map<UUID, Material> map = ICONS.get(string);
        if (map == null) {
            map = new HashMap<UUID, Material>();
            ICONS.put(string, map);
        }
        return map;
    }

    private static Map<UUID, Long> entries(String string) {
        Map<UUID, Long> map = ENTRIES.get(string);
        if (map == null) {
            map = new HashMap<UUID, Long>();
            ENTRIES.put(string, map);
        }
        return map;
    }
}

