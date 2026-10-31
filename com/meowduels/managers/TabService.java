package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.model.ActiveDuel;
import com.meowduels.model.Party;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TabService {
    private static final String AQUA_TEAM = "vdaqua";
    private static final String RED_TEAM = "vdred";
    private final MeowDuels plugin;
    private final boolean external;
    private final Map<UUID, Object> memberGroup = new HashMap<UUID, Object>();
    private final Map<UUID, Object> rankGroup = new HashMap<UUID, Object>();

    public TabService(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.external = meowDuels.getConfig().getBoolean("external-tab", true) && meowDuels.getServer().getPluginManager().getPlugin("TAB") != null;
    }

    public boolean isExternal() {
        return this.external;
    }

    public void attach(ActiveDuel activeDuel) {
        Player player = Bukkit.getPlayer((UUID)activeDuel.getPlayer1());
        Player player2 = Bukkit.getPlayer((UUID)activeDuel.getPlayer2());
        if (player == null || player2 == null) {
            return;
        }
        this.memberGroup.put(activeDuel.getPlayer1(), activeDuel);
        this.memberGroup.put(activeDuel.getPlayer2(), activeDuel);
        this.hideRank(player, activeDuel);
        this.hideRank(player2, activeDuel);
        this.applyTeams(player, activeDuel);
        this.applyTeams(player2, activeDuel);
        this.reconcileAll();
    }

    public void attachSpectator(Player player, ActiveDuel activeDuel) {
        this.memberGroup.put(player.getUniqueId(), activeDuel);
        this.hideRank(player, activeDuel);
        this.applyTeams(player, activeDuel);
        this.reconcileAll();
    }

    public void attachSpectator(Player player, Party party) {
        if (party == null) {
            return;
        }
        this.memberGroup.put(player.getUniqueId(), this.hostOf(party));
        this.reconcileAll();
    }

    private Party hostOf(Party party) {
        return party.isMatchHost() || party.getOpponent() == null ? party : party.getOpponent();
    }

    public void attachParty(Party party) {
        if (party == null) {
            return;
        }
        if (!this.plugin.getConfig().getBoolean("party.separate-tab", true)) {
            this.detachParty(party);
            for (UUID id : party.getMembers()) {
                Player player = Bukkit.getPlayer(id);
                if (player != null) this.hideRank(player, party);
            }
            return;
        }
        for (Map.Entry<UUID, Object> object : new HashMap<UUID, Object>(this.memberGroup).entrySet()) {
            if (object.getValue() != party) continue;
            this.release(object.getKey());
        }
        for (UUID uUID : party.getMembers()) {
            Player player = Bukkit.getPlayer(uUID);
            if (player == null) continue;
            this.memberGroup.put(uUID, party);
            this.hideRank(player, party);
        }
        this.reconcileAll();
    }

    public void refreshParty(Party party) {
        if (party == null) {
            return;
        }
        this.reconcile(party);
    }

    public void attachMatch(Party party, Party party2) {
        if (party == null || party2 == null) {
            return;
        }
        Party host = this.hostOf(party);
        for (Party side : new Party[]{party, party2}) {
            for (UUID id : side.getMembers()) {
                Player player = Bukkit.getPlayer(id);
                if (player != null) this.hideRank(player, host);
            }
        }
        if (!this.plugin.getConfig().getBoolean("party.separate-tab", true)) {
            return;
        }
        for (Party party3 : new Party[]{party, party2}) {
            for (UUID uUID : party3.getMembers()) {
                this.memberGroup.put(uUID, party);
            }
        }
        this.reconcileAll();
    }

    public void detachParty(Party party) {
        if (party == null) {
            return;
        }
        for (Map.Entry<UUID, Object> entry : new HashMap<UUID, Object>(this.memberGroup).entrySet()) {
            if (entry.getValue() != party) continue;
            this.release(entry.getKey());
        }
        for (UUID id : party.getMembers()) this.restoreRank(id);
        this.reconcileAll();
    }

    public void detach(UUID uUID) {
        this.leave(uUID);
    }

    public void detachSpectator(UUID uUID) {
        this.leave(uUID);
    }

    private void leave(UUID uUID) {
        this.release(uUID);
        this.reconcileAll();
    }

    private void release(UUID uUID) {
        Object object = this.memberGroup.remove(uUID);
        Player player = Bukkit.getPlayer((UUID)uUID);
        this.restoreRank(uUID);
        if (object == null) return;
        if (player == null) {
            return;
        }
        for (Player player2 : Bukkit.getOnlinePlayers()) {
            if (player2.getUniqueId().equals(uUID)) continue;
            this.setVisible(player, player2, true, false);
            this.setVisible(player, player2, true, true);
        }
    }

    private void reconcileAll() {
        HashSet<Object> hashSet = new HashSet<Object>(this.memberGroup.values());
        for (Object object : hashSet) {
            this.reconcile(object);
        }
    }

    private void reconcile(Object object) {
        HashSet<UUID> hashSet = new HashSet<UUID>();
        for (Map.Entry<UUID, Object> entry : this.memberGroup.entrySet()) {
            if (entry.getValue() != object) continue;
            hashSet.add((UUID)entry.getKey());
        }
        boolean bl = !this.hidesWorld(object);
        for (UUID uUID : hashSet) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            for (Player player2 : Bukkit.getOnlinePlayers()) {
                if (player2.getUniqueId().equals(uUID)) continue;
                this.setVisible(player, player2, hashSet.contains(player2.getUniqueId()), bl);
            }
        }
        if (this.debug()) {
            this.plugin.getLogger().info("tab: " + (object instanceof Party ? "party" : "duel") + " bubble of " + hashSet.size() + ", " + (bl ? "tab-only" : "world+tab") + ", " + Math.max(0, Bukkit.getOnlinePlayers().size() - hashSet.size()) + " hidden from each member");
        }
    }

    private void setVisible(Player player, Player player2, boolean bl, boolean bl2) {
        try {
            if (bl2) {
                if (bl) {
                    player.listPlayer(player2);
                } else {
                    player.unlistPlayer(player2);
                }
                return;
            }
            if (bl) {
                player.showPlayer((Plugin)this.plugin, player2);
            } else {
                player.hidePlayer((Plugin)this.plugin, player2);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void hideRank(Player player, Object group) {
        if (player != null) {
            this.rankGroup.put(player.getUniqueId(), group);
        }
        if (player != null && this.plugin.getTabHook() != null) {
            this.plugin.getTabHook().applyDuel(player, "", "");
        }
    }

    private void restoreRank(UUID id) {
        if (this.rankGroup.remove(id) == null) return;
        Player player = Bukkit.getPlayer(id);
        if (player != null && this.plugin.getTabHook() != null) this.plugin.getTabHook().clearDuel(player);
    }

    private void applyTeams(Player player, ActiveDuel activeDuel) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            return;
        }
        // These teams are written to each duelist's private scoreboard. This
        // keeps the flags and name colors visible to the two match players,
        // without changing the global TAB prefix seen by lobby players.
        Team team = this.team(scoreboard, AQUA_TEAM, ChatColor.BLUE, ChatColor.BLUE + "⚑ ");
        Team team2 = this.team(scoreboard, RED_TEAM, ChatColor.DARK_RED, ChatColor.DARK_RED + "⚑ ");
        for (UUID uUID : new UUID[]{activeDuel.getPlayer1(), activeDuel.getPlayer2()}) {
            Player player2 = Bukkit.getPlayer((UUID)uUID);
            if (player2 == null) continue;
            (activeDuel.isAqua(uUID) ? team : team2).addEntry(player2.getName());
        }
    }

    private Team team(Scoreboard scoreboard, String string, ChatColor chatColor, String string2) {
        Team team = scoreboard.getTeam(string);
        if (team == null) {
            team = scoreboard.registerNewTeam(string);
        }
        team.setColor(chatColor);
        team.setPrefix(string2);
        team.setSuffix("");
        return team;
    }

    /** Ticks between {@link #reassertBubbles} runs; MeowDuels schedules it at this period. */
    public static final long REASSERT_PERIOD = 10L;
    private long resyncCountdown;

    /**
     * Keeps each bubble's tab list and visibility right as players come and go.
     *
     * <p>This used to run every 5 ticks and, for every bubble member and every
     * online player outside the bubble, send a list packet followed by an
     * unlist packet - unconditionally. With 100 players in matches and 150
     * online that is about 30,000 packets every quarter second, each one a
     * player-info update the client has to apply, and it never stopped while
     * a party sat in the lobby. It was the single biggest steady cost in the
     * plugin.
     *
     * <p>Now every call is the server's own state-checked one: listPlayer,
     * unlistPlayer, showPlayer and hidePlayer send nothing when the viewer is
     * already in the wanted state, so a quiet server sends nothing at all and
     * a player who just joined is fixed up within half a second. The forced
     * re-send only exists for the TAB plugin, which can re-list players with
     * its own packets behind the server's back, and only every
     * {@code tab.force-resync-seconds} (10 by default, 0 turns it off).
     */
    public void reassertBubbles() {
        if (this.memberGroup.isEmpty()) {
            return;
        }
        boolean force = false;
        if (this.external) {
            long every = (long)Math.max(0, this.plugin.getConfig().getInt("tab.force-resync-seconds", 10)) * 20L;
            if (every > 0L) {
                this.resyncCountdown -= REASSERT_PERIOD;
                if (this.resyncCountdown <= 0L) {
                    this.resyncCountdown = every;
                    force = true;
                }
            }
        }
        boolean duelHides = this.plugin.getConfig().getBoolean("tab.duel-hides-world", true);
        boolean partyHides = this.plugin.getConfig().getBoolean("tab.party-hides-world", false);
        java.util.Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        // Group members resolved once, instead of once per member per member.
        java.util.IdentityHashMap<Object, java.util.List<Player>> groups = new java.util.IdentityHashMap<Object, java.util.List<Player>>();
        for (Map.Entry<UUID, Object> entry : this.memberGroup.entrySet()) {
            Player player = Bukkit.getPlayer((UUID)entry.getKey());
            if (player == null) continue;
            groups.computeIfAbsent(entry.getValue(), k -> new java.util.ArrayList<Player>()).add(player);
        }
        for (Map.Entry<Object, java.util.List<Player>> group : groups.entrySet()) {
            Object object = group.getKey();
            java.util.List<Player> members = group.getValue();
            boolean hides = object instanceof Party ? ((Party)object).isFighting() || partyHides : duelHides;
            for (Player player : members) {
                for (Player other : members) {
                    if (other == player) continue;
                    this.setVisible(player, other, true, false);
                    if (player.canSee(other)) {
                        this.setVisible(player, other, true, true);
                    }
                }
                for (Player other : online) {
                    if (other == player || this.memberGroup.get(other.getUniqueId()) == object) continue;
                    if (hides) {
                        this.setVisible(player, other, false, false);
                        continue;
                    }
                    if (!player.canSee(other)) continue;
                    try {
                        if (force) {
                            player.listPlayer(other);
                        }
                        player.unlistPlayer(other);
                    }
                    catch (Throwable throwable) {}
                }
            }
        }
    }

    private boolean hidesWorld(Object object) {
        if (object instanceof Party) {
            if (((Party)object).isFighting()) {
                return true;
            }
            return this.plugin.getConfig().getBoolean("tab.party-hides-world", false);
        }
        return this.plugin.getConfig().getBoolean("tab.duel-hides-world", true);
    }

    private boolean debug() {
        return this.plugin.getConfig().getBoolean("tab.debug", false);
    }

}

