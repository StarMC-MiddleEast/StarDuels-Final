package com.meowduels.listeners;

import com.meowduels.MeowDuels;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandGuardListener
implements Listener {
    private static final String BYPASS = "meowduels.bypass.commands";
    private final MeowDuels plugin;

    public CommandGuardListener(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
    public void onCommand(PlayerCommandPreprocessEvent playerCommandPreprocessEvent) {
        if (!this.plugin.getConfig().getBoolean("commands.block-in-match", true)) {
            return;
        }
        Player player = playerCommandPreprocessEvent.getPlayer();
        if (player.hasPermission(BYPASS)) {
            return;
        }
        String string = this.matchKind(player);
        if (string == null) {
            return;
        }
        String string2 = CommandGuardListener.normalise(playerCommandPreprocessEvent.getMessage());
        if (string2.isEmpty() || this.allowed(string2)) {
            return;
        }
        playerCommandPreprocessEvent.setCancelled(true);
        player.sendMessage(this.plugin.messages().get("notice.guard.t-use-commands", "what", string));
        player.sendMessage(this.plugin.messages().get("notice.guard.use-get-out-first", "value", String.valueOf(this.exitHint(player))));
    }

    private String matchKind(Player player) {
        UUID uUID = player.getUniqueId();
        if (this.plugin.getDuelManager().isInDuel(uUID)) {
            return "during a duel";
        }
        if (this.plugin.getPartyManager().inPartyMatch(uUID)) {
            return "during a party match";
        }
        if (this.plugin.getEventManager().isPlaying(uUID)) {
            return "during an event";
        }
        return null;
    }

    private String exitHint(Player player) {
        return this.plugin.getEventManager().isPlaying(player.getUniqueId()) ? "/eventleave" : "/leave";
    }

    private static String normalise(String string) {
        String string2;
        String string3 = string2 = string == null ? "" : string.trim();
        if (string2.startsWith("/")) {
            string2 = string2.substring(1);
        }
        string2 = string2.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
        int n = string2.indexOf(58);
        int n2 = string2.indexOf(32);
        if (n > 0 && (n2 < 0 || n < n2)) {
            string2 = string2.substring(n + 1);
        }
        return string2;
    }

    private boolean allowed(String string) {
        for (String string2 : this.allowList()) {
            String string3 = CommandGuardListener.normalise(string2);
            if (string3.isEmpty() || !string.equals(string3) && !string.startsWith(string3 + " ")) continue;
            return true;
        }
        return false;
    }

    private List<String> allowList() {
        List list = this.plugin.getConfig().getStringList("commands.allowed-in-match");
        if (list != null && !list.isEmpty()) {
            return list;
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("leave");
        arrayList.add("party leave");
        arrayList.add("party force_end");
        arrayList.add("ff");
        arrayList.add("eventleave");
        return arrayList;
    }
}

