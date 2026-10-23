package com.meowduels.commands;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Style;
import com.meowduels.managers.StatsManager;
import com.meowduels.util.Colors;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaderboardCommand
implements CommandExecutor {
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 25;
    private final MeowDuels plugin;

    public LeaderboardCommand(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        StatsManager statsManager = this.plugin.getStatsManager();
        int n = 10;
        if (stringArray.length > 0) {
            try {
                n = Math.max(1, Math.min(25, Integer.parseInt(stringArray[0])));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        List<Map.Entry<UUID, Integer>> list = statsManager.topWins(n);
        commandSender.sendMessage("");
        commandSender.sendMessage(LeaderboardCommand.mmc("  <gradient:" + Style.ACCENT_A + ":" + Style.ACCENT_B + ">\u1d21\u026a\u0274\ua731 \u029f\u1d07\u1d00\u1d05\u1d07\u0280\u0299\u1d0f\u1d00\u0280\u1d05</gradient>"));
        commandSender.sendMessage("");
        if (list.isEmpty()) {
            commandSender.sendMessage(LeaderboardCommand.mmc("  <#8E959D>\u0274\u1d0f \u1d05\u1d1c\u1d07\u029f\ua731 \u1d21\u1d0f\u0274 \u028f\u1d07\u1d1b <dark_gray>- <#8E959D>win one to appear here."));
            commandSender.sendMessage("");
            return true;
        }
        for (int i = 0; i < list.size(); ++i) {
            Map.Entry<UUID, Integer> entry = list.get(i);
            commandSender.sendMessage(LeaderboardCommand.mmc("  " + this.position(i + 1) + " <#E6E8EB>" + this.nameOf(entry.getKey()) + " <dark_gray>\u00b7 <#FF8A93>" + String.valueOf(entry.getValue()) + " <#8E959D>" + (entry.getValue() == 1 ? "win" : "wins")));
        }
        this.appendViewer(commandSender, statsManager, list);
        commandSender.sendMessage("");
        return true;
    }

    private void appendViewer(CommandSender commandSender, StatsManager statsManager, List<Map.Entry<UUID, Integer>> list) {
        if (!(commandSender instanceof Player)) {
            return;
        }
        UUID uUID = ((Player)commandSender).getUniqueId();
        for (Map.Entry<UUID, Integer> entry : list) {
            if (!entry.getKey().equals(uUID)) continue;
            return;
        }
        int n = statsManager.getWins(uUID);
        if (n <= 0) {
            return;
        }
        commandSender.sendMessage("");
        commandSender.sendMessage(LeaderboardCommand.mmc("  <#8E959D>\u028f\u1d0f\u1d1c <dark_gray>\u00b7 <#E6E8EB>#" + this.rankOf(statsManager, uUID) + " <dark_gray>\u00b7 <#FF8A93>" + n + " <#8E959D>" + (n == 1 ? "win" : "wins")));
    }

    private int rankOf(StatsManager statsManager, UUID uUID) {
        List<Map.Entry<UUID, Integer>> list = statsManager.topWins(Integer.MAX_VALUE);
        for (int i = 0; i < list.size(); ++i) {
            if (!list.get(i).getKey().equals(uUID)) continue;
            return i + 1;
        }
        return list.size() + 1;
    }

    private String position(int n) {
        switch (n) {
            case 1: {
                return "<#FFD65C>#1";
            }
            case 2: {
                return "<#D8DEE6>#2";
            }
            case 3: {
                return "<#E0954A>#3";
            }
        }
        return "<#6B7079>#" + n;
    }

    private String nameOf(UUID uUID) {
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            return player.getName();
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((UUID)uUID);
        return offlinePlayer.getName() == null ? "Unknown" : offlinePlayer.getName();
    }

    private static String mmc(String string) {
        return Colors.toSection(string);
    }
}

