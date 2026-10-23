package com.meowduels.util;

import com.meowduels.MeowDuels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

public final class AntiCheatBypass {
    private static final List<String> DEFAULT_WORLD_NODES = Arrays.asList("worldguard.region.bypass.{world}");
    private static final List<String> DEFAULT_NODES = Arrays.asList("grim.exempt", "grim.nosetback", "vulcan.bypass", "spartan.bypass", "matrix.bypass", "nocheatplus.bypass", "themis.bypass", "polar.bypass", "aac.bypass");
    private static final Map<UUID, List<PermissionAttachment>> ACTIVE = new HashMap<UUID, List<PermissionAttachment>>();

    private AntiCheatBypass() {
    }

    public static void grant(MeowDuels meowDuels, Player player, String ... stringArray) {
        if (player == null) {
            return;
        }
        AntiCheatBypass.release(meowDuels, player);
        ArrayList<PermissionAttachment> arrayList = new ArrayList<PermissionAttachment>();
        for (String string : AntiCheatBypass.nodes(meowDuels)) {
            AntiCheatBypass.attach(arrayList, meowDuels, player, string);
        }
        if (stringArray != null) {
            for (String string : stringArray) {
                AntiCheatBypass.attach(arrayList, meowDuels, player, string);
            }
        }
        if (!arrayList.isEmpty()) {
            ACTIVE.put(player.getUniqueId(), arrayList);
        }
        try {
            player.recalculatePermissions();
            player.updateCommands();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        AntiCheatBypass.runHooks(meowDuels, player, "anticheat.commands-on-start");
    }

    public static void release(MeowDuels meowDuels, Player player) {
        if (player == null) {
            return;
        }
        List<PermissionAttachment> list = ACTIVE.remove(player.getUniqueId());
        if (list == null) {
            return;
        }
        for (PermissionAttachment permissionAttachment : list) {
            try {
                player.removeAttachment(permissionAttachment);
            }
            catch (Throwable throwable) {}
        }
        try {
            player.recalculatePermissions();
            player.updateCommands();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        AntiCheatBypass.runHooks(meowDuels, player, "anticheat.commands-on-end");
    }

    private static void attach(List<PermissionAttachment> list, MeowDuels meowDuels, Player player, String string) {
        if (string == null || string.trim().isEmpty()) {
            return;
        }
        try {
            list.add(player.addAttachment((Plugin)meowDuels, string.trim(), true));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static String[] worldNodes(MeowDuels meowDuels, String string) {
        if (string == null || string.isEmpty()) {
            return new String[0];
        }
        List<String> list = meowDuels.getConfig().getStringList("anticheat.world-bypass-nodes");
        if (list == null || list.isEmpty()) {
            list = DEFAULT_WORLD_NODES;
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string2 : list) {
            if (string2 == null || string2.trim().isEmpty()) continue;
            arrayList.add(string2.trim().replace("{world}", string));
        }
        return arrayList.toArray(new String[0]);
    }

    private static List<String> nodes(MeowDuels meowDuels) {
        List<String> list = meowDuels.getConfig().getStringList("anticheat.bypass-nodes");
        return list == null || list.isEmpty() ? DEFAULT_NODES : list;
    }

    private static void runHooks(MeowDuels meowDuels, Player player, String string) {
        List<String> list = meowDuels.getConfig().getStringList(string);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (String string2 : list) {
            if (string2 == null || string2.trim().isEmpty()) continue;
            String string3 = string2.replace("{player}", player.getName()).replace("%player%", player.getName());
            try {
                Bukkit.dispatchCommand((CommandSender)((CommandSender)Bukkit.getConsoleSender()), (String)string3);
            }
            catch (Throwable throwable) {
                meowDuels.getLogger().warning("Anti-cheat hook failed: " + string3 + " (" + String.valueOf(throwable) + ")");
            }
        }
    }
}

