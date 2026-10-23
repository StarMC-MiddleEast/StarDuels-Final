package com.meowduels.hook;

import com.meowduels.MeowDuels;
import com.meowduels.model.ActiveDuel;
import com.meowduels.model.Kit;
import com.meowduels.model.Party;
import com.meowduels.util.Colors;
import com.meowduels.util.Health;
import com.meowduels.util.Marks;
import java.util.Locale;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class MeowDuelsPlaceholders
extends PlaceholderExpansion
implements Relational {
    private final MeowDuels plugin;
    private static final String PLAIN_TAG = "\u00a7f";

    public MeowDuelsPlaceholders(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    public String getIdentifier() {
        return "meowduels";
    }

    public String getAuthor() {
        return "nafehawan";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public boolean persist() {
        return true;
    }

    public String onRequest(OfflinePlayer offlinePlayer, String string) {
        UUID uUID;
        if (string == null) {
            return "";
        }
        if (string.regionMatches(true, 0, "tab_header_", 0, 11)) {
            return this.tabLine(offlinePlayer, "header", string.substring(11));
        }
        if (string.regionMatches(true, 0, "tab_footer_", 0, 11)) {
            return this.tabLine(offlinePlayer, "footer", string.substring(11));
        }
        String string2 = string.toLowerCase();
        switch (string2) {
            case "players_in_duels": {
                return String.valueOf(this.plugin.getDuelManager().playersInDuels());
            }
            case "duels": {
                return String.valueOf(this.plugin.getDuelManager().duelsInProgress());
            }
            case "server_ip": {
                return this.plugin.getScoreboardIp();
            }
            case "tab_title": {
                return this.plugin.getTabTitle();
            }
            case "tab_discord": {
                return this.plugin.getTabDiscord();
            }
            case "tab_store": {
                return this.plugin.getTabStore();
            }
        }
        if (offlinePlayer == null) {
            return "";
        }
        Object object = offlinePlayer.getUniqueId();
        int n = this.plugin.getSpectateManager().isSpectating((UUID)object) ? 1 : 0;
        Object object2 = object;
        if (n != 0 && (uUID = this.plugin.getSpectateManager().getWatchedTarget((UUID)object)) != null) {
            object2 = uUID;
        }
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel((UUID)object2);
        switch (string2) {
            case "tabprefix": {
                return this.lpPrefix(offlinePlayer);
            }
            case "tagprefix": {
                if (this.inAFight((UUID)object)) {
                    return PLAIN_TAG;
                }
                return this.lpPrefix(offlinePlayer);
            }
            case "lpprefix": {
                return this.lpPrefix(offlinePlayer);
            }
            case "tagorrank": {
                if (this.plugin.getDuelManager().isInDuel((UUID)object)) {
                    return "";
                }
                String string3 = this.papi(offlinePlayer, "%luckperms_suffix%");
                if (!string3.trim().isEmpty()) {
                    return Colors.toSection(string3);
                }
                return "";
            }
            case "tabsuffix": {
                return this.suffixFor(offlinePlayer, (UUID)object, true);
            }
            case "tag": {
                if (this.plugin.getDuelManager().isInDuel((UUID)object)) {
                    return "";
                }
                return Colors.toSection(this.papi(offlinePlayer, "%luckperms_suffix%"));
            }
            case "health": {
                Player player = Bukkit.getPlayer((UUID)object);
                if (player == null) {
                    return "";
                }
                return "\u00a7c" + Health.text(player.getHealth()) + " \u2764";
            }
            case "in_fight": {
                return this.plugin.getDuelManager().isInDuel((UUID)object) ? "true" : "false";
            }
            case "in_party": {
                return this.plugin.getPartyManager().inParty((UUID)object) ? "true" : "false";
            }
            case "in_party_match": {
                return this.plugin.getPartyManager().inPartyMatch((UUID)object) ? "true" : "false";
            }
            case "party_leader": {
                Party party = this.plugin.getPartyManager().partyOf((UUID)object);
                return party == null ? "" : this.nameOf(party.getLeader());
            }
            case "party_size": {
                Party party = this.plugin.getPartyManager().partyOf((UUID)object);
                return party == null ? "0" : String.valueOf(party.size());
            }
            case "party_kit": {
                Party party = this.plugin.getPartyManager().partyOf((UUID)object);
                return party == null || party.getKit() == null ? "" : this.kitLabel(party.getKit());
            }
            case "party_status": {
                Party party = this.plugin.getPartyManager().partyOf((UUID)object);
                return party == null ? "" : (party.isFighting() ? "Fighting" : "Waiting");
            }
            case "party_alive": {
                Party party = this.plugin.getPartyManager().partyOf((UUID)object);
                return party == null ? "0" : String.valueOf(party.getAlive().size());
            }
            case "party_sort": {
                Party party = this.plugin.getPartyManager().partyOf((UUID)object);
                return party == null ? "ZZZZ" : "A" + this.nameOf(party.getLeader()).toLowerCase(Locale.ROOT);
            }
            case "party_members": {
                Party party = this.plugin.getPartyManager().partyOf((UUID)object);
                if (party == null) {
                    return "";
                }
                StringBuilder stringBuilder = new StringBuilder();
                int n2 = 0;
                for (UUID uUID2 : party.getMembers()) {
                    if (n2 == 5) {
                        stringBuilder.append("\u00a78 +").append(party.size() - n2);
                        break;
                    }
                    if (n2 > 0) {
                        stringBuilder.append("\u00a78, ");
                    }
                    stringBuilder.append(this.memberTag(party, uUID2));
                    ++n2;
                }
                return stringBuilder.toString();
            }
            case "party_role": {
                Party party = this.plugin.getPartyManager().partyOf((UUID)object);
                return party == null ? "" : (party.isLeader((UUID)object) ? "Leader" : "Member");
            }
            case "role": {
                if (n != 0) {
                    return "SPECTATOR";
                }
                return this.plugin.getDuelManager().isInDuel((UUID)object) ? "PLAYER" : "NONE";
            }
            case "status": {
                if (n != 0) {
                    return "Spectating";
                }
                return this.plugin.getDuelManager().isInDuel((UUID)object) ? "Fighting" : "Idle";
            }
        }
        if (activeDuel == null) {
            return "";
        }
        Object object3 = n != 0 ? object2 : object;
        UUID uUID3 = activeDuel.getOpponent((UUID)object3);
        boolean bl = activeDuel.isAqua((UUID)object3);
        switch (string2) {
            case "team": {
                return bl ? "AQUA" : "RED";
            }
            case "team_color": {
                return bl ? "&9" : "&c";
            }
            case "team_prefix": {
                return Marks.tab(bl ? (char)'9' : 'c');
            }
            case "state": {
                return activeDuel.getState().name();
            }
            case "arena": {
                return activeDuel.getArena() == null ? "" : activeDuel.getArena().getName();
            }
            case "kit": {
                return this.kitLabel(activeDuel.getKit());
            }
            case "round": {
                return String.valueOf(activeDuel.getCurrentRound());
            }
            case "rounds_to_win": {
                return String.valueOf(activeDuel.getRoundsToWin());
            }
            case "score": {
                return String.valueOf(activeDuel.getScoreFor((UUID)object3));
            }
            case "opponent_score": {
                return String.valueOf(activeDuel.getScoreAgainst((UUID)object3));
            }
            case "opponent": {
                return this.nameOf(uUID3);
            }
            case "player1": {
                return this.nameOf(activeDuel.getPlayer1());
            }
            case "player2": {
                return this.nameOf(activeDuel.getPlayer2());
            }
            case "time": {
                long l = Math.max(0L, (System.currentTimeMillis() - activeDuel.getStartedAt()) / 1000L);
                return String.format("%02d:%02d", l / 60L, l % 60L);
            }
        }
        return "";
    }

    private String tabLine(OfflinePlayer offlinePlayer, String string, String string2) {
        int n;
        try {
            n = Integer.parseInt(string2.trim());
        }
        catch (NumberFormatException numberFormatException) {
            return "";
        }
        UUID uUID = offlinePlayer == null ? null : offlinePlayer.getUniqueId();
        return Colors.toSection(this.plugin.getTabService().layoutLine(uUID, string, n));
    }

    private boolean inAFight(UUID uUID) {
        return this.plugin.getDuelManager().isInDuel(uUID) || this.plugin.getPartyManager().inPartyMatch(uUID) || this.plugin.getEventManager().isInvolved(uUID);
    }

    public String onPlaceholderRequest(Player player, Player player2, String string) {
        boolean bl;
        if (string == null || player2 == null) {
            return "";
        }
        String string2 = string.toLowerCase();
        UUID uUID = player2.getUniqueId();
        boolean bl2 = bl = player != null && (this.plugin.getDuelManager().isInDuel(player.getUniqueId()) || this.plugin.getSpectateManager().isSpectating(player.getUniqueId()));
        if ("tabprefix".equals(string2)) {
            ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(uUID);
            if (bl && activeDuel != null) {
                return Marks.tab(activeDuel.isAqua(uUID) ? (char)'9' : 'c');
            }
            String string3 = this.partyPrefix(player, player2, true);
            if (string3 != null) {
                return string3;
            }
            return this.lpPrefix((OfflinePlayer)player2);
        }
        if ("tagprefix".equals(string2)) {
            if (this.inAFight(uUID)) {
                return PLAIN_TAG;
            }
            String string4 = this.partyPrefix(player, player2, false);
            if (string4 != null) {
                return string4;
            }
            return this.lpPrefix((OfflinePlayer)player2);
        }
        if ("tabsuffix".equals(string2)) {
            boolean bl3 = player != null && this.plugin.getPartyManager().partyOf(player.getUniqueId()) != null && this.plugin.getPartyManager().partyOf(player.getUniqueId()).has(uUID);
            return this.suffixFor((OfflinePlayer)player2, uUID, !bl && !bl3);
        }
        return this.onRequest((OfflinePlayer)player2, string);
    }

    private String memberTag(Party party, UUID uUID) {
        String string = this.nameOf(uUID);
        if (party.isFighting()) {
            if (party.getAlive().contains(uUID)) {
                return "\u00a7a" + string;
            }
            if (party.getWatching().contains(uUID)) {
                return "\u00a78\u2620 " + string;
            }
        }
        return party.isLeader(uUID) ? "\u00a76\u2605 " + string : PLAIN_TAG + string;
    }

    private String partyPrefix(Player player, Player player2, boolean bl) {
        if (player == null) {
            return null;
        }
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.has(player2.getUniqueId())) {
            return null;
        }
        UUID uUID = player2.getUniqueId();
        if (party.isFighting()) {
            if (party.isTeamMode() && party.teamOf(uUID) != null) {
                if (party.getWatching().contains(uUID)) {
                    return bl ? Marks.tabOut() : Marks.tagOut();
                }
                char c = party.teamOf(uUID) == Party.Team.AQUA ? (char)'9' : 'c';
                return bl ? Marks.tab(c) : Marks.tag(c);
            }
            if (party.getAlive().contains(uUID)) {
                return bl ? Marks.tab('a') : Marks.tag('a');
            }
            if (party.getWatching().contains(uUID)) {
                return bl ? Marks.tabOut() : Marks.tagOut();
            }
        }
        return party.isLeader(uUID) ? "\u00a76\u2605 \u00a76" : "\u00a7d\u25c6 \u00a7d";
    }

    private String suffixFor(OfflinePlayer offlinePlayer, UUID uUID, boolean bl) {
        if (this.plugin.getDuelManager().isInDuel(uUID)) {
            return this.marker(this.plugin.getDuelMarker(), bl);
        }
        if (this.plugin.getPartyManager().inPartyMatch(uUID)) {
            return this.marker(this.plugin.getPartyMarker(), bl);
        }
        String string = Colors.toSection(this.papi(offlinePlayer, "%luckperms_suffix%"));
        return string.trim().isEmpty() ? "" : " \u00a7r" + string;
    }

    private String marker(String string, boolean bl) {
        if (!bl || string == null || string.isEmpty()) {
            return "";
        }
        return " \u00a7r\u00a78" + string;
    }

    private String lpPrefix(OfflinePlayer offlinePlayer) {
        return Colors.toSection(this.papi(offlinePlayer, "%luckperms_prefix%"));
    }

    private String papi(OfflinePlayer offlinePlayer, String string) {
        try {
            String string2 = PlaceholderAPI.setPlaceholders((OfflinePlayer)offlinePlayer, (String)string);
            if (string2 == null || string2.equals(string)) {
                return "";
            }
            return string2;
        }
        catch (Throwable throwable) {
            return "";
        }
    }

    private String nameOf(UUID uUID) {
        if (uUID == null) {
            return "";
        }
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            return player.getName();
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((UUID)uUID);
        return offlinePlayer.getName() == null ? "" : offlinePlayer.getName();
    }

    private String kitLabel(String string) {
        Kit kit = this.plugin.getKitManager().get(string);
        if (kit != null && kit.getDisplayName() != null && !kit.getDisplayName().isEmpty()) {
            return kit.getDisplayName().replaceAll("<[^>]*>", "");
        }
        return string;
    }
}

