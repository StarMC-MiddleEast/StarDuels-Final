package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.util.Colors;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TipService {
    private final MeowDuels plugin;
    private int secondsLeft;
    private int globalIndex = 0;
    private int matchIndex = 0;

    public TipService(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.secondsLeft = this.intervalSeconds();
    }

    private boolean enabled() {
        return this.plugin.getConfig().getBoolean("tips.enabled", true);
    }

    private int intervalSeconds() {
        return Math.max(30, this.plugin.getConfig().getInt("tips.interval-seconds", 180));
    }

    public void tick() {
        if (!this.enabled()) {
            return;
        }
        if (--this.secondsLeft > 0) {
            return;
        }
        this.secondsLeft = this.intervalSeconds();
        this.send();
    }

    public void reload() {
        this.secondsLeft = this.intervalSeconds();
    }

    private void send() {
        List list = this.plugin.getConfig().getStringList("tips.global");
        List list2 = this.plugin.getConfig().getStringList("tips.match");
        String string = TipService.pick(list, this.globalIndex);
        String string2 = TipService.pick(list2, this.matchIndex);
        if (string != null) {
            this.globalIndex = (this.globalIndex + 1) % list.size();
        }
        if (string2 != null) {
            this.matchIndex = (this.matchIndex + 1) % list2.size();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            String string3 = this.inMatch(player) ? string2 : string;
            if (string3 == null || string3.isEmpty()) continue;
            player.sendMessage(Colors.toSection(string3));
        }
    }

    private boolean inMatch(Player player) {
        UUID uUID = player.getUniqueId();
        return this.plugin.getDuelManager().isInDuel(uUID) || this.plugin.getPartyManager().inPartyMatch(uUID) || this.plugin.getSpectateManager().getWatchedTarget(uUID) != null;
    }

    private static String pick(List<String> list, int n) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(n % list.size());
    }
}

