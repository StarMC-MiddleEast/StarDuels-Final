package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.model.ActiveDuel;
import com.meowduels.model.Kit;
import com.meowduels.model.Party;
import com.meowduels.util.Marks;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
        this.applyTeams(player, activeDuel);
        this.applyTeams(player2, activeDuel);
        this.sendHeaderFooter(player);
        this.sendHeaderFooter(player2);
        this.reconcileAll();
    }

    public void attachSpectator(Player player, ActiveDuel activeDuel) {
        this.memberGroup.put(player.getUniqueId(), activeDuel);
        this.applyTeams(player, activeDuel);
        this.sendHeaderFooter(player);
        this.reconcileAll();
    }

    public void attachSpectator(Player player, Party party) {
        if (party == null) {
            return;
        }
        this.memberGroup.put(player.getUniqueId(), this.hostOf(party));
        this.sendHeaderFooter(player);
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
            return;
        }
        for (Map.Entry<UUID, Object> object : new HashMap<UUID, Object>(this.memberGroup).entrySet()) {
            if (object.getValue() != party) continue;
            this.release(object.getKey());
        }
        for (UUID uUID : party.getMembers()) {
            if (Bukkit.getPlayer((UUID)uUID) == null) continue;
            this.memberGroup.put(uUID, party);
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
        if (!this.plugin.getConfig().getBoolean("party.separate-tab", true)) {
            return;
        }
        for (Party party3 : new Party[]{party, party2}) {
            for (UUID uUID : party3.getMembers()) {
                this.memberGroup.put(uUID, party);
            }
        }
        for (UUID uUID : party.getMembers()) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            this.sendHeaderFooter(player);
        }
        for (UUID uUID : party2.getMembers()) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            this.sendHeaderFooter(player);
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
        if (object == null) {
            return;
        }
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player == null) {
            return;
        }
        for (Player player2 : Bukkit.getOnlinePlayers()) {
            if (player2.getUniqueId().equals(uUID)) continue;
            this.setVisible(player, player2, true, false);
            this.setVisible(player, player2, true, true);
        }
    }

    public void onPlayerJoin(Player player) {
        this.sendHeaderFooter(player);
        this.reconcileAll();
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

    private void applyTeams(Player player, ActiveDuel activeDuel) {
        if (this.external) {
            return;
        }
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            return;
        }
        Team team = this.team(scoreboard, AQUA_TEAM, ChatColor.BLUE, Marks.tab('9'));
        Team team2 = this.team(scoreboard, RED_TEAM, ChatColor.RED, Marks.tab('c'));
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
        return team;
    }

    public String layoutLine(UUID uUID, String string, int n) {
        String string2 = this.layoutContext(uUID);
        List list = this.plugin.getConfig().getStringList("tab." + string2 + "." + string);
        if (list == null || n < 1 || n > list.size()) {
            return "";
        }
        return this.fillTokens((String)list.get(n - 1), uUID);
    }

    private String layoutContext(UUID uUID) {
        if (uUID == null) {
            return "global";
        }
        if (this.plugin.getDuelManager().isInDuel(uUID) || this.plugin.getSpectateManager().isSpectating(uUID)) {
            return "duel";
        }
        if (this.plugin.getPartyManager().inParty(uUID)) {
            return "party";
        }
        return "global";
    }

    private String fillTokens(String string, UUID uUID) {
        if (string == null || string.indexOf(123) < 0) {
            return string == null ? "" : string;
        }
        String string2 = string;
        int n = Bukkit.getOnlinePlayers().size();
        int n2 = this.plugin.getDuelManager().playersInDuels();
        string2 = string2.replace("{online}", String.valueOf(n));
        string2 = string2.replace("{practice}", String.valueOf(Math.max(0, n - n2)));
        string2 = string2.replace("{fighting}", String.valueOf(n2));
        string2 = string2.replace("{in_duels}", String.valueOf(this.plugin.getDuelManager().duelsInProgress()));
        string2 = string2.replace("{server_ip}", this.plugin.getScoreboardIp());
        string2 = string2.replace("{server_name}", this.plugin.getServerName());
        string2 = string2.replace("{discord}", this.plugin.getTabDiscord());
        string2 = string2.replace("{store}", this.plugin.getTabStore());
        if (uUID == null) {
            return string2;
        }
        Player player = Bukkit.getPlayer((UUID)uUID);
        string2 = string2.replace("{player}", player == null ? "" : player.getName());
        string2 = string2.replace("{ping}", player == null ? "0" : String.valueOf(player.getPing()));
        string2 = string2.replace("{rank}", "");
        string2 = string2.replace("{elo}", "");
        string2 = this.fillDuelTokens(string2, uUID);
        string2 = this.fillPartyTokens(string2, uUID);
        return string2;
    }

    private String fillDuelTokens(String string, UUID uUID) {
        Object object;
        Object object2 = uUID;
        if (this.plugin.getSpectateManager().isSpectating(uUID) && (object = this.plugin.getSpectateManager().getWatchedTarget(uUID)) != null) {
            object2 = object;
        }
        if ((object = this.plugin.getDuelManager().getDuel((UUID)object2)) == null) {
            return string.replace("{opponent}", "").replace("{score}", "").replace("{opponent_score}", "").replace("{kit}", "").replace("{arena}", "").replace("{round}", "").replace("{rounds_to_win}", "").replace("{time}", "").replace("{team_color}", "<gray>");
        }
        long l = Math.max(0L, (System.currentTimeMillis() - ((ActiveDuel)object).getStartedAt()) / 1000L);
        return string.replace("{opponent}", this.nameOf(((ActiveDuel)object).getOpponent((UUID)object2))).replace("{score}", String.valueOf(((ActiveDuel)object).getScoreFor((UUID)object2))).replace("{opponent_score}", String.valueOf(((ActiveDuel)object).getScoreAgainst((UUID)object2))).replace("{kit}", this.kitLabel(((ActiveDuel)object).getKit())).replace("{arena}", ((ActiveDuel)object).getArena() == null ? "" : ((ActiveDuel)object).getArena().getName()).replace("{round}", String.valueOf(((ActiveDuel)object).getCurrentRound())).replace("{rounds_to_win}", String.valueOf(((ActiveDuel)object).getRoundsToWin())).replace("{time}", String.format("%02d:%02d", l / 60L, l % 60L)).replace("{team_color}", ((ActiveDuel)object).isAqua((UUID)object2) ? "<aqua>" : "<red>");
    }

    private String fillPartyTokens(String string, UUID uUID) {
        Party party = this.plugin.getPartyManager().partyOf(uUID);
        if (party == null) {
            return string.replace("{party_leader}", "").replace("{party_size}", "0").replace("{party_kit}", "").replace("{party_status}", "").replace("{party_alive}", "0").replace("{party_role}", "");
        }
        return string.replace("{party_leader}", this.nameOf(party.getLeader())).replace("{party_size}", String.valueOf(party.size())).replace("{party_kit}", party.getKit() == null ? "" : this.kitLabel(party.getKit())).replace("{party_status}", party.isFighting() ? "Fighting" : "Waiting").replace("{party_alive}", String.valueOf(party.getAlive().size())).replace("{party_role}", party.isLeader(uUID) ? "Leader" : "Member");
    }

    private String nameOf(UUID uUID) {
        if (uUID == null) {
            return "";
        }
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            return player.getName();
        }
        String string = Bukkit.getOfflinePlayer((UUID)uUID).getName();
        return string == null ? "" : string;
    }

    private String kitLabel(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        Kit kit = this.plugin.getKitManager().get(string);
        if (kit != null && kit.getDisplayName() != null && !kit.getDisplayName().isEmpty()) {
            return kit.getDisplayName().replaceAll("<[^>]*>", "");
        }
        return string;
    }

    private void sendHeaderFooter(Player player) {
        if (this.external) {
            return;
        }
        UUID uUID = player.getUniqueId();
        player.sendPlayerListHeaderAndFooter(TabService.mm(this.joinLayout(uUID, "header")), TabService.mm(this.joinLayout(uUID, "footer")));
    }

    private String joinLayout(UUID uUID, String string) {
        List list = this.plugin.getConfig().getStringList("tab." + this.layoutContext(uUID) + "." + string);
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); ++i) {
            String string2;
            if (i > 0) {
                stringBuilder.append('\n');
            }
            stringBuilder.append((string2 = this.fillTokens((String)list.get(i), uUID)).isEmpty() ? " " : string2);
        }
        return stringBuilder.toString();
    }

    public void tick() {
        if (this.external) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.sendHeaderFooter(player);
        }
    }

    public void reassertBubbles() {
        if (this.memberGroup.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, Object> entry : this.memberGroup.entrySet()) {
            Player player = Bukkit.getPlayer((UUID)entry.getKey());
            if (player == null) continue;
            Object object = entry.getValue();
            for (Map.Entry<UUID, Object> entry2 : this.memberGroup.entrySet()) {
                Player other = Bukkit.getPlayer((UUID)entry2.getKey());
                if (entry2.getValue() != object || entry2.getKey().equals(entry.getKey()) || other == null) continue;
                this.setVisible(player, other, true, false);
                this.setVisible(player, other, true, true);
            }
            if (this.hidesWorld(object)) continue;
            for (Player player2 : Bukkit.getOnlinePlayers()) {
                UUID uUID = player2.getUniqueId();
                if (uUID.equals(entry.getKey()) || this.memberGroup.get(uUID) == object) continue;
                try {
                    player.listPlayer(player2);
                    player.unlistPlayer(player2);
                }
                catch (Throwable throwable) {}
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

    private static Component mm(String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }
}

