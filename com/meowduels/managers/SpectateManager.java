package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.model.ActiveDuel;
import com.meowduels.model.Arena;
import com.meowduels.model.Party;
import com.meowduels.util.GameModeGuard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SpectateManager {
    private final MeowDuels plugin;
    private final Map<UUID, Location> returnLocation = new ConcurrentHashMap<UUID, Location>();
    private final Map<UUID, GameMode> returnMode = new HashMap<UUID, GameMode>();
    private final Map<UUID, UUID> target = new ConcurrentHashMap<UUID, UUID>();
    private final Set<UUID> watchingFight = new HashSet<UUID>();

    public SpectateManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    public boolean isSpectating(UUID uUID) {
        return this.returnLocation.containsKey(uUID);
    }

    public int countWatchers(UUID uUID, UUID uUID2) {
        int n = 0;
        for (UUID uUID3 : this.target.values()) {
            if (uUID3 == null || !uUID3.equals(uUID) && !uUID3.equals(uUID2)) continue;
            ++n;
        }
        return n;
    }

    public UUID getWatchedTarget(UUID uUID) {
        return this.target.get(uUID);
    }

    public void spectate(Player player, Player player2) {
        Arena arena;
        Party party;
        UUID uUID = player.getUniqueId();
        if (this.plugin.getDuelManager().isInDuel(uUID)) {
            player.sendMessage(this.plugin.messages().get("spectate.in-duel", new String[0]));
            return;
        }
        if (this.plugin.getEventManager().isInvolved(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.spectate.t-spectate-duel-while", new String[0]));
            return;
        }
        if (uUID.equals(player2.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("spectate.self", new String[0]));
            return;
        }
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player2.getUniqueId());
        Party party2 = party = activeDuel != null ? null : this.plugin.getPartyManager().partyOf(player2.getUniqueId());
        if (party != null && !this.plugin.getPartyManager().inPartyMatch(player2.getUniqueId())) {
            party = null;
        }
        if (activeDuel == null && party == null) {
            player.sendMessage(this.plugin.messages().get("spectate.target-not-in-fight", "target", player2.getName()));
            return;
        }
        if (this.plugin.getPartyManager().inPartyMatch(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.spectate.t-spectate-while-re", new String[0]));
            return;
        }
        Arena arena2 = arena = activeDuel != null ? activeDuel.getArena() : party.getArena();
        if (arena != null && !arena.isAllowSpectators() && !player.hasPermission("meowduels.admin")) {
            player.sendMessage(this.plugin.messages().get("spectate.not-allowed", new String[0]));
            return;
        }
        if (!this.plugin.getPlayerSettings().isSpectators(player2.getUniqueId()) && !player.hasPermission("meowduels.admin")) {
            player.sendMessage(this.plugin.messages().get("notice.spectate.spectators-turned-off", "targetplayer", player2.getName()));
            return;
        }
        UUID uUID2 = this.target.get(uUID);
        if (uUID2 != null && !uUID2.equals(player2.getUniqueId())) {
            this.notify(uUID2, "spectate.stopped-watching", "name", player.getName());
            this.dropFightView(uUID);
        }
        if (!this.returnLocation.containsKey(uUID)) {
            this.returnLocation.put(uUID, player.getLocation());
            GameMode gameMode = player.getGameMode();
            this.returnMode.put(uUID, gameMode == GameMode.SPECTATOR ? GameMode.SURVIVAL : gameMode);
        }
        GameModeGuard.pin(player, GameMode.SPECTATOR);
        player.teleport(player2.getLocation());
        this.target.put(uUID, player2.getUniqueId());
        player.sendMessage(this.plugin.messages().get("spectate.now", "target", player2.getName()));
        this.notify(player2.getUniqueId(), "spectate.started-watching", "name", player.getName());
        if (activeDuel != null) {
            this.plugin.getScoreboardService().attachSpectator(player, activeDuel, player2.getUniqueId());
            this.plugin.getTabService().attachSpectator(player, activeDuel);
        } else {
            this.plugin.getTabService().attachSpectator(player, party);
        }
        this.watchingFight.add(uUID);
    }

    public void stop(Player player) {
        UUID uUID = player.getUniqueId();
        if (!this.returnLocation.containsKey(uUID)) {
            player.sendMessage(this.plugin.messages().get("spectate.not-spectating", new String[0]));
            return;
        }
        UUID uUID2 = this.target.get(uUID);
        if (uUID2 != null) {
            this.notify(uUID2, "spectate.stopped-watching", "name", player.getName());
        }
        this.dropFightView(uUID);
        this.restore(player);
        player.sendMessage(this.plugin.messages().get("spectate.stopped", new String[0]));
    }

    public void shutdown() {
        for (UUID uUID : new ArrayList<UUID>(this.returnLocation.keySet())) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            this.dropFightView(uUID);
            this.restore(player);
        }
    }

    public void clearOnQuit(Player player) {
        UUID uUID = player.getUniqueId();
        if (!this.returnLocation.containsKey(uUID)) {
            return;
        }
        UUID uUID2 = this.target.get(uUID);
        if (uUID2 != null) {
            this.notify(uUID2, "spectate.stopped-watching", "name", player.getName());
        }
        this.dropFightView(uUID);
        GameMode gameMode = this.returnMode.remove(uUID);
        this.returnLocation.remove(uUID);
        this.target.remove(uUID);
        GameModeGuard.setFreely(player, gameMode == null ? GameMode.SURVIVAL : gameMode);
    }

    public void followRoundStart(UUID uUID, UUID uUID2) {
        for (Map.Entry<UUID, UUID> entry : this.target.entrySet()) {
            UUID uUID3 = entry.getValue();
            if (uUID3 == null || !uUID3.equals(uUID) && !uUID3.equals(uUID2)) continue;
            Player player = Bukkit.getPlayer((UUID)entry.getKey());
            Player player2 = Bukkit.getPlayer((UUID)uUID3);
            if (player == null || player2 == null) continue;
            player.teleport(player2.getLocation());
        }
    }

    public void tick() {
        if (this.watchingFight.isEmpty()) {
            return;
        }
        for (UUID uUID : new HashSet<UUID>(this.watchingFight)) {
            UUID uUID2 = this.target.get(uUID);
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) {
                this.watchingFight.remove(uUID);
                continue;
            }
            if (uUID2 != null && this.stillFighting(uUID2)) {
                Player player2 = Bukkit.getPlayer((UUID)uUID2);
                if (player2 == null || player2.getLocation().getWorld() == player.getLocation().getWorld()) continue;
                player.teleport(player2.getLocation());
                continue;
            }
            this.restore(player);
            this.dropFightView(uUID);
            player.sendMessage(this.plugin.messages().get("spectate.fight-ended", new String[0]));
        }
    }

    private boolean stillFighting(UUID uUID) {
        return this.plugin.getDuelManager().isInDuel(uUID) || this.plugin.getPartyManager().inPartyMatch(uUID);
    }

    private void dropFightView(UUID uUID) {
        this.watchingFight.remove(uUID);
        this.plugin.getScoreboardService().detach(uUID);
        this.plugin.getTabService().detachSpectator(uUID);
    }

    private void restore(Player player) {
        UUID uUID = player.getUniqueId();
        GameMode gameMode = this.returnMode.remove(uUID);
        Location location = this.returnLocation.remove(uUID);
        this.target.remove(uUID);
        GameModeGuard.setFreely(player, gameMode == null ? GameMode.SURVIVAL : gameMode);
        if (location != null) {
            player.teleport(location);
        }
    }

    private void notify(UUID uUID, String string, String ... stringArray) {
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player == null) {
            return;
        }
        String string2 = this.plugin.messages().raw(string);
        int n = 0;
        while (n + 1 < stringArray.length) {
            string2 = string2.replace("{" + stringArray[n] + "}", stringArray[n + 1]);
            n += 2;
        }
        player.sendMessage(MiniMessage.miniMessage().deserialize(("<gray>" + string2 + "</gray>")));
    }
}

