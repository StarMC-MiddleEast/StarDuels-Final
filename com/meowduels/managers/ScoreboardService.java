package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Style;
import com.meowduels.managers.EventManager;
import com.meowduels.managers.QueueManager;
import com.meowduels.model.ActiveDuel;
import com.meowduels.model.Kit;
import com.meowduels.model.Party;
import com.meowduels.model.PartyMode;
import com.meowduels.util.Health;
import com.meowduels.util.Marks;
import com.meowduels.util.Tokens;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardService {
    private static final int MAX_LINES = 15;
    private static final ChatColor[] LINE_KEYS = ChatColor.values();
    private final MeowDuels plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, Board> boards = new HashMap<UUID, Board>();
    private final Map<UUID, UUID> spectatorOf = new HashMap<UUID, UUID>();
    private final Set<UUID> tabOverridden = new HashSet<UUID>();
    private final Set<UUID> tabReleased = new HashSet<UUID>();
    private final Map<UUID, String> lastBelow = new HashMap<UUID, String>();
    private final Set<UUID> queueBarShown = new HashSet<UUID>();
    private static final long SCORE_BAR_MS = 10000L;
    private boolean masterEnabled = true;
    private Layout global = new Layout();
    private Layout ffa = new Layout();
    private Layout duel = new Layout();
    private Layout party = new Layout();
    private Layout partyFfa = new Layout();
    private final Map<UUID, Integer> rankTeamIndex = new HashMap<UUID, Integer>();
    private int rankTeamCounter = 0;
    private boolean rankNametags = false;
    private boolean externalNametags = false;
    private boolean tabPluginPresent = false;
    private boolean healthBelowName = true;
    private static final char[] SMALL = new char[]{'\u1d00', '\u0299', '\u1d04', '\u1d05', '\u1d07', '\ua730', '\u0262', '\u029c', '\u026a', '\u1d0a', '\u1d0b', '\u029f', '\u1d0d', '\u0274', '\u1d0f', '\u1d18', '\ua7af', '\u0280', '\ua731', '\u1d1b', '\u1d1c', '\u1d20', '\u1d21', 'x', '\u028f', '\u1d22'};

    public ScoreboardService(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.reload();
        if (Bukkit.getScoreboardManager() != null) {
            this.setupHealthBelowName(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void reload() {
        ConfigurationSection configurationSection = this.plugin.getConfig().getConfigurationSection("scoreboard");
        if (configurationSection == null) {
            this.masterEnabled = false;
            this.global = new Layout();
            this.ffa = new Layout();
            this.duel = new Layout();
            this.party = new Layout();
            this.partyFfa = new Layout();
            return;
        }
        this.masterEnabled = configurationSection.getBoolean("enabled", true);
        this.rankNametags = false;
        this.externalNametags = configurationSection.getBoolean("external-nametags", true)
            && this.plugin.getTabHook() != null && this.plugin.getTabHook().isAvailable();
        this.tabPluginPresent = this.plugin.getServer().getPluginManager().getPlugin("TAB") != null;
        this.healthBelowName = configurationSection.getBoolean("health-below-name", true);
        if (Bukkit.getScoreboardManager() != null) {
            this.setupHealthBelowName(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        this.global = this.loadLayout(configurationSection.getConfigurationSection("global"));
        this.ffa = this.loadLayout(configurationSection.getConfigurationSection("ffa"));
        this.duel = this.loadLayout(configurationSection.getConfigurationSection("duel"));
        this.party = this.loadLayout(configurationSection.getConfigurationSection("party"));
        this.partyFfa = this.loadLayout(configurationSection.getConfigurationSection("party-ffa"));
        this.warnIfEmpty("duel", this.duel, "duellists and spectators");
        this.warnIfEmpty("global", this.global, "everyone else");
    }

    private void warnIfEmpty(String string, Layout layout, String string2) {
        if (!layout.lines.isEmpty()) {
            return;
        }
        this.plugin.getLogger().warning("scoreboard." + string + " has no lines, so " + string2 + " fall through to the next board down. Add lines under scoreboard." + string + " in config.yml, or delete that section and restart to get the defaults back.");
    }

    private Layout loadLayout(ConfigurationSection configurationSection) {
        Layout layout = new Layout();
        if (configurationSection == null) {
            return layout;
        }
        layout.enabled = configurationSection.getBoolean("enabled", true);
        layout.title = configurationSection.getString("title", "");
        ArrayList<String> arrayList = new ArrayList<String>(configurationSection.getStringList("lines"));
        while (arrayList.size() > 15) {
            arrayList.remove(arrayList.size() - 1);
        }
        layout.lines = arrayList;
        layout.titleStatic = layout.title.indexOf(123) < 0 ? this.deserialize(layout.title) : null;
        layout.lineStatic = new Component[arrayList.size()];
        for (int i = 0; i < arrayList.size(); ++i) {
            layout.lineStatic[i] = arrayList.get(i).indexOf(123) < 0 ? this.deserialize(arrayList.get(i)) : null;
        }
        return layout;
    }

    private Component deserialize(String string) {
        try {
            return this.mm.deserialize(string);
        }
        catch (Exception exception) {
            return this.mm.deserialize("");
        }
    }

    public void attach(Player player, ActiveDuel activeDuel) {
        this.refresh(player);
    }

    public void attachSpectator(Player player, ActiveDuel activeDuel, UUID uUID) {
        this.spectatorOf.put(player.getUniqueId(), uUID);
        this.refresh(player);
    }

    public void detach(UUID uUID) {
        this.spectatorOf.remove(uUID);
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            this.refresh(player);
        } else {
            this.removeBoard(uUID, null);
        }
    }

    public void handleJoin(Player player) {
        this.lastBelow.clear();
        this.refresh(player);
    }

    public void tick() {
        for (Player object : Bukkit.getOnlinePlayers()) {
            this.refresh(object);
        }
        if (!this.boards.isEmpty()) {
            for (UUID uUID : new ArrayList<UUID>(this.boards.keySet())) {
                if (Bukkit.getPlayer((UUID)uUID) != null) continue;
                this.boards.remove(uUID);
            }
        }
        if (this.rankNametags) {
            this.applyNametagTeams();
        }
    }

    private void applyNametagTeams() {
        try {
            Object object;
            ArrayList<Scoreboard> arrayList = new ArrayList<Scoreboard>();
            ArrayList<Object> arrayList2 = new ArrayList<Object>();
            if (Bukkit.getScoreboardManager() != null) {
                arrayList.add(Bukkit.getScoreboardManager().getMainScoreboard());
                arrayList2.add(Collections.emptySet());
            }
            for (Board board : this.boards.values()) {
                arrayList.add(board.scoreboard);
                object = new HashSet();
                if (board.hasNametags) {
                    if (board.allyEntry != null) {
                        ((HashSet)object).add(board.allyEntry);
                    }
                    if (board.enemyEntry != null) {
                        ((HashSet)object).add(board.enemyEntry);
                    }
                }
                arrayList2.add(object);
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                object = player.getName();
                String string = "mdr" + this.rankTeamFor(player.getUniqueId());
                Component component = this.deserialize("");
                for (int i = 0; i < arrayList.size(); ++i) {
                    if (((Set)arrayList2.get(i)).contains(object)) continue;
                    Scoreboard scoreboard = (Scoreboard)arrayList.get(i);
                    Team team = scoreboard.getTeam(string);
                    if (team == null) {
                        team = scoreboard.registerNewTeam(string);
                    }
                    team.addEntry((String)object);
                    team.prefix(component);
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private int rankTeamFor(UUID uUID) {
        Integer n = this.rankTeamIndex.get(uUID);
        if (n == null) {
            n = this.rankTeamCounter++;
            this.rankTeamIndex.put(uUID, n);
        }
        return n;
    }

    private void refresh(Player player) {
        Object object;
        boolean bl;
        Object object2;
        UUID uUID = player.getUniqueId();
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(uUID);
        UUID uUID2 = uUID;
        boolean bl2 = false;
        if (activeDuel == null) {
            UUID uUID3 = this.spectatorOf.get(uUID);
            ActiveDuel activeDuel2 = uUID3 == null ? null : this.plugin.getDuelManager().getDuel(uUID3);
            object2 = activeDuel2;
            if (activeDuel2 != null) {
                activeDuel = activeDuel2;
                uUID2 = uUID3;
                bl2 = true;
            } else if (uUID3 != null) {
                this.spectatorOf.remove(uUID);
            }
        }
        boolean bl3 = activeDuel != null;
        boolean bl4 = bl3 && activeDuel.isArenaEntered();
        object2 = null;
        boolean bl5 = this.plugin.getPlayerSettings().isScoreboard(uUID);
        boolean bl6 = this.plugin.getEventManager().isInvolved(uUID);
        Party party = this.plugin.getPartyManager().partyOf(uUID);
        if (this.masterEnabled && bl5) {
            if (bl3 && this.duel.enabled && !this.duel.lines.isEmpty()) {
                object2 = this.duel;
            } else if (party != null && party.isFighting() && party.getMode() == PartyMode.FFA && this.partyFfa.enabled && !this.partyFfa.lines.isEmpty()) {
                object2 = this.partyFfa;
            } else if (bl6 && this.ffa.enabled && !this.ffa.lines.isEmpty()) {
                object2 = this.ffa;
            } else if (party != null && this.party.enabled && !this.party.lines.isEmpty()) {
                object2 = this.party;
            } else if (this.global.enabled && !this.global.lines.isEmpty()) {
                object2 = this.global;
            }
        }
        int n = object2 == null ? 0 : ((Layout)object2).lines.size();
        boolean bl7 = bl4 && !this.externalNametags;
        boolean bl8 = bl7;
        boolean bl9 = bl = !bl4 && bl6 && !this.externalNametags;
        // Red and Aqua nametags in Split and party-vs-party matches. TAB draws
        // its own from %rel_starduels_tagprefix% when it is installed and
        // trusted with nametags; otherwise nothing coloured them at all.
        boolean partyTeams = party != null && party.isFighting() && party.isTeamMode() && (!this.externalNametags || !this.tabPluginPresent);
        if (object2 == null && !bl7 && !bl && !partyTeams) {
            if (!bl3) {
                this.sendQueueBar(player);
            }
            this.removeBoard(uUID, player);
            return;
        }
        Board board = this.boards.get(uUID);
        if (board == null || board.sidebarLines != n || board.hasNametags != bl7 || board.hasFfaTeam != bl) {
            board = new Board(n, bl7, bl);
            this.boards.put(uUID, board);
            player.setScoreboard(board.scoreboard);
            this.lastBelow.clear();
        } else if (player.getScoreboard() != board.scoreboard) {
            player.setScoreboard(board.scoreboard);
        }
        if (object2 != null) {
            object = this.tokens(player, activeDuel, uUID2, bl2);
            // Each line is only re-parsed and re-sent when its text changed.
            // Team prefix and objective name updates always send a packet, so
            // this used to be one packet per line per player every second,
            // plus a MiniMessage parse for each, for lines that mostly never
            // change.
            Layout layout = (Layout)object2;
            Map<String, String> tokens = (Map<String, String>)object;
            String title = this.lineKey(layout.titleStatic, layout.title, tokens);
            if (!title.equals(board.lastTitle)) {
                board.lastTitle = title;
                board.setTitle(this.render(layout.titleStatic, layout.title, tokens));
            }
            for (int i = 0; i < n; ++i) {
                String line = this.lineKey(layout.lineStatic[i], layout.lines.get(i), tokens);
                if (line.equals(board.lastLines[i])) continue;
                board.lastLines[i] = line;
                board.setLine(i, layout.lineStatic[i] != null ? layout.lineStatic[i] : this.deserialize(line));
            }
        }
        if (bl7) {
            object = activeDuel.getOpponent(uUID2);
            board.setNametags(uUID2, (UUID)object, activeDuel.isAqua(uUID2));
        }
        if (bl) {
            board.setFfaMembers(this.plugin.getEventManager().involved());
        }
        board.setPartyTeams(partyTeams ? party : null);
        if (bl4 && activeDuel.getState() == ActiveDuel.State.FIGHTING && activeDuel.sinceFightStart() < 10000L) {
            this.sendActionBar(player, activeDuel, uUID2);
        } else if (!bl3) {
            this.sendQueueBar(player);
        }
    }

    private void setupHealthBelowName(Scoreboard scoreboard) {
        if (!this.healthBelowName) {
            ScoreboardService.removeHealthBelowName(scoreboard);
            return;
        }
        try {
            Objective objective = scoreboard.registerNewObjective("mdhealth", Criteria.DUMMY, "");
            objective.displayName(this.deserialize(""));
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            try {
                objective.numberFormat(NumberFormat.blank());
            }
            catch (Throwable throwable) {}
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void removeHealthBelowName(Scoreboard scoreboard) {
        try {
            Objective objective = scoreboard.getObjective("mdhealth");
            if (objective != null) {
                objective.unregister();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private final Map<String, NumberFormat> belowFormats = new HashMap<String, NumberFormat>();

    /**
     * Pushes changed health values to the below-name objective.
     *
     * <p>Every board is a separate scoreboard with one viewer, so one health
     * change used to be written to every online player's board - 200 players
     * trading hits meant tens of thousands of score packets a second, almost
     * all to players in another world who could not see the player at all.
     * Now a change only goes to boards whose owner is in the same world, and a
     * board whose owner changes world is brought up to date in one pass. The
     * formatted heart is cached per value instead of re-parsed each time.
     */
    public void updateHealthBelowName() {
        if (!this.healthBelowName) {
            return;
        }
        try {
            boolean resync = false;
            for (Map.Entry<UUID, Board> entry : this.boards.entrySet()) {
                Player owner = Bukkit.getPlayer((UUID)entry.getKey());
                if (owner == null) continue;
                World world = owner.getWorld();
                if (entry.getValue().healthWorld == world) continue;
                entry.getValue().healthWorld = world;
                resync = true;
            }
            if (resync) {
                this.lastBelow.clear();
            }
            Scoreboard main = Bukkit.getScoreboardManager() == null ? null : Bukkit.getScoreboardManager().getMainScoreboard();
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uUID = player.getUniqueId();
                String string = Health.nametag(player.getHealth());
                if (string.equals(this.lastBelow.get(uUID))) continue;
                this.lastBelow.put(uUID, string);
                NumberFormat numberFormat = this.belowFormats.get(string);
                if (numberFormat == null) {
                    numberFormat = NumberFormat.fixed((ComponentLike)((ComponentLike)this.deserialize("<red>\u2764 " + string)));
                    this.belowFormats.put(string, numberFormat);
                }
                String name = player.getName();
                if (main != null) {
                    this.setBelowRank(main, name, numberFormat, 0);
                }
                World world = player.getWorld();
                for (Board board : this.boards.values()) {
                    if (board.healthWorld != null && board.healthWorld != world) continue;
                    this.setBelowRank(board.scoreboard, name, numberFormat, 0);
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void applyBelowRank(String string, NumberFormat numberFormat, int n) {
        if (Bukkit.getScoreboardManager() != null) {
            this.setBelowRank(Bukkit.getScoreboardManager().getMainScoreboard(), string, numberFormat, n);
        }
        for (Board board : this.boards.values()) {
            this.setBelowRank(board.scoreboard, string, numberFormat, n);
        }
    }

    private void setBelowRank(Scoreboard scoreboard, String string, NumberFormat numberFormat, int n) {
        try {
            Objective objective = scoreboard.getObjective("mdhealth");
            if (objective == null) {
                return;
            }
            Score score = objective.getScore(string);
            score.numberFormat(numberFormat);
            score.setScore(n);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void sendQueueBar(Player player) {
        UUID uUID = player.getUniqueId();
        QueueManager queueManager = this.plugin.getQueueManager();
        int n = queueManager.queuedKits(uUID).size();
        if (n <= 0) {
            if (this.queueBarShown.remove(uUID)) {
                player.sendActionBar(this.deserialize(""));
            }
            return;
        }
        this.queueBarShown.add(uUID);
        long l = Math.max(0L, queueManager.waitingSeconds(uUID));
        String string = String.format("%d:%02d", l / 60L, l % 60L);
        player.sendActionBar(this.deserialize("<gradient:" + Style.ACCENT_A + ":" + Style.ACCENT_B + ">\u01eb\u1d1c\u1d07\u1d1c\u1d07\u1d05 \ua730\u1d0f\u0280 " + n + " " + (n == 1 ? "\u1d0b\u026a\u1d1b" : "\u1d0b\u026a\u1d1b\ua731") + "</gradient> <#6B7079>\u2503 <#FF7FC4>\u231a <#E6E8EB>" + string));
    }

    private void sendActionBar(Player player, ActiveDuel activeDuel, UUID uUID) {
        String string = this.plugin.getConfig().getString("scoreboard.action-bar", "{score} <gray>-</gray> {opponent_score}");
        string = string.replace("{score}", "<green>" + activeDuel.getScoreFor(uUID) + "</green>").replace("{opponent_score}", "<red>" + activeDuel.getScoreAgainst(uUID) + "</red>");
        player.sendActionBar(this.deserialize(string));
    }

    private void removeBoard(UUID uUID, Player player) {
        if (this.boards.remove(uUID) != null && player != null && Bukkit.getScoreboardManager() != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    /** The text a line will show; a pre-rendered static line keys on its identity. */
    private String lineKey(Component component, String string, Map<String, String> map) {
        if (component != null) {
            return "\u0000static#" + System.identityHashCode(component);
        }
        return Tokens.apply(string, map);
    }

    private Component render(Component component, String string, Map<String, String> map) {
        if (component != null) {
            return component;
        }
        return this.deserialize(Tokens.apply(string, map));
    }

    private static String clock(long l) {
        return String.format("%02d:%02d", l / 60L, l % 60L);
    }

    private static String joinKits(ScoreboardService scoreboardService, List<String> list) {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        int n = Math.min(3, list.size());
        for (int i = 0; i < n; ++i) {
            if (i > 0) {
                stringBuilder.append("<dark_gray>, ");
            }
            stringBuilder.append(scoreboardService.kitLabel(list.get(i)));
        }
        if (list.size() > n) {
            stringBuilder.append("<dark_gray> +").append(list.size() - n);
        }
        return stringBuilder.toString();
    }

    private Map<String, String> tokens(Player player, ActiveDuel activeDuel, UUID uUID, boolean bl) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        UUID uUID2 = player.getUniqueId();
        hashMap.put("player", bl ? this.nameOf(uUID) : player.getName());
        hashMap.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
        hashMap.put("in_duels", String.valueOf(this.plugin.getDuelManager().playersInDuels()));
        hashMap.put("server_ip", this.plugin.getScoreboardIp());
        hashMap.put("ip_color", this.plugin.getConfig().getString("scoreboard-ip-color", "<gradient:#FFF1B3:#E6A700>"));
        hashMap.put("ping", String.valueOf(player.getPing()));
        hashMap.put("kills", String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS)));
        hashMap.put("deaths", String.valueOf(player.getStatistic(Statistic.DEATHS)));
        hashMap.put("playtime", this.playtime(player));
        hashMap.put("kdr", this.kdr(player));
        hashMap.put("streak", String.valueOf(this.plugin.getStatsManager().getStreak(uUID2)));
        UUID uUID3 = bl ? uUID : uUID2;
        hashMap.put("wins", String.valueOf(this.plugin.getStatsManager().getWins(uUID3)));
        hashMap.put("losses", String.valueOf(this.plugin.getStatsManager().getLosses(uUID3)));
        hashMap.put("elo", "");
        hashMap.put("rank", "");
        hashMap.put("server_name", this.plugin.getServerName());
        hashMap.put("date", this.date());
        hashMap.put("server_ip_sc", ScoreboardService.smallCaps(this.plugin.getScoreboardIp()));
        EventManager eventManager = this.plugin.getEventManager();
        hashMap.put("alive", String.valueOf(eventManager.aliveCount()));
        hashMap.put("ffa_players", String.valueOf(eventManager.playerCount()));
        hashMap.put("ffa_spectators", String.valueOf(eventManager.spectatorCount()));
        hashMap.put("ffa_kills", String.valueOf(eventManager.killsOf(uUID3)));
        hashMap.put("ffa_kit", this.kitLabel(eventManager.getKit()));
        hashMap.put("ffa_time", ScoreboardService.clock(eventManager.runningSeconds()));
        QueueManager queueManager = this.plugin.getQueueManager();
        ArrayList<String> arrayList = new ArrayList<String>(queueManager.queuedKits(uUID2));
        Collections.sort(arrayList);
        int n = 0;
        int n2 = 0;
        for (String string : arrayList) {
            n = Math.max(n, queueManager.queued(string));
            n2 += queueManager.dueling(string);
        }
        hashMap.put("queue_kits", String.valueOf(arrayList.size()));
        hashMap.put("queue_list", ScoreboardService.joinKits(this, arrayList));
        hashMap.put("queue_kit", arrayList.isEmpty() ? "" : this.kitLabel((String)arrayList.get(0)));
        hashMap.put("queue_searching", String.valueOf(n));
        hashMap.put("queue_dueling", String.valueOf(n2));
        hashMap.put("queue_time", ScoreboardService.clock(Math.max(0L, queueManager.waitingSeconds(uUID2))));
        Party party = this.plugin.getPartyManager().partyOf(uUID2);
        hashMap.put("party_leader", party == null ? "" : this.nameOf(party.getLeader()));
        int n3 = party == null ? 0 : (party.isFighting() && party.getMode() == PartyMode.FFA ? party.involved().size() : party.size());
        hashMap.put("party_size", String.valueOf(n3));
        hashMap.put("party_kit", party == null || party.getKit() == null ? "" : this.kitLabel(party.getKit()));
        hashMap.put("party_status", party == null ? "" : (party.isFighting() ? "Fighting" : "Waiting"));
        hashMap.put("party_alive", party == null ? "0" : String.valueOf(party.getAlive().size()));
        hashMap.put("party_spectators", party == null ? "0" : String.valueOf(party.getWatching().size()));
        hashMap.put("party_mode", party == null || !party.isFighting() || party.getMode() == null ? "" : party.getMode().name());
        hashMap.put("party_access", party == null ? "" : (party.isOpenToAll() ? "Public" : "Private"));
        hashMap.put("party_role", party == null ? "" : (party.isLeader(uUID2) ? "Leader" : "Member"));
        hashMap.put("party_kills", party == null ? "0" : String.valueOf(party.killsOf(uUID2)));
        hashMap.put("party_time", ScoreboardService.clock(party == null || party.getStartedAt() == 0L ? 0L : Math.max(0L, (System.currentTimeMillis() - party.getStartedAt()) / 1000L)));
        String[] stringArray = new String[]{"score", "opponent_score", "opponent", "kit", "arena", "round", "rounds_to_win", "time", "team", "team_color", "opponent_color", "game", "spectators"};
        if (activeDuel == null) {
            for (String string : stringArray) {
                hashMap.put(string, "");
            }
            return hashMap;
        }
        hashMap.put("spectators", String.valueOf(this.plugin.getSpectateManager().countWatchers(activeDuel.getPlayer1(), activeDuel.getPlayer2())));
        boolean bl2 = activeDuel.isAqua(uUID);
        UUID uUID4 = activeDuel.getOpponent(uUID);
        hashMap.put("score", String.valueOf(activeDuel.getScoreFor(uUID)));
        hashMap.put("opponent_score", String.valueOf(activeDuel.getScoreAgainst(uUID)));
        hashMap.put("opponent", this.nameOf(uUID4));
        hashMap.put("kit", this.kitLabel(activeDuel.getKit()));
        hashMap.put("arena", activeDuel.getArena() == null ? "" : activeDuel.getArena().getName());
        hashMap.put("round", String.valueOf(activeDuel.getCurrentRound()));
        hashMap.put("rounds_to_win", String.valueOf(activeDuel.getRoundsToWin()));
        hashMap.put("game", String.format("%04d", activeDuel.getGameNumber()));
        long l = Math.max(0L, (System.currentTimeMillis() - activeDuel.getStartedAt()) / 1000L);
        hashMap.put("time", String.format("%02d:%02d", l / 60L, l % 60L));
        hashMap.put("team", bl ? "SPEC" : (bl2 ? "AQUA" : "RED"));
        hashMap.put("team_color", bl ? "<gray>" : (bl2 ? "<blue>" : "<red>"));
        hashMap.put("opponent_color", bl ? "<gray>" : (bl2 ? "<red>" : "<blue>"));
        return hashMap;
    }

    private static String smallCaps(String string) {
        if (string == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c >= 'a' && c <= 'z') {
                stringBuilder.append(SMALL[c - 97]);
                continue;
            }
            if (c >= 'A' && c <= 'Z') {
                stringBuilder.append(SMALL[c - 65]);
                continue;
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    private String date() {
        String string = this.plugin.getConfig().getString("scoreboard.date-format", "dd/MM/yyyy");
        try {
            return LocalDate.now().format(DateTimeFormatter.ofPattern(string, Locale.ENGLISH));
        }
        catch (Exception exception) {
            return LocalDate.now().toString();
        }
    }

    private String playtime(Player player) {
        long l = (long)player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20L / 60L;
        long l2 = l / 60L;
        long l3 = l % 60L;
        return l2 > 0L ? l2 + "h " + l3 + "m" : l3 + "m";
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

    private String kdr(Player player) {
        int n = player.getStatistic(Statistic.PLAYER_KILLS);
        int n2 = player.getStatistic(Statistic.DEATHS);
        double d = n2 <= 0 ? (double)n : (double)n / (double)n2;
        return String.format(Locale.US, "%.2f", d);
    }

    private String hpText(double d) {
        return Health.text(d);
    }

    private String hpTextOf(UUID uUID) {
        Player player = uUID == null ? null : Bukkit.getPlayer((UUID)uUID);
        return player == null ? "0" : this.hpText(player.getHealth());
    }

    private String kitLabel(String string) {
        Kit kit = this.plugin.getKitManager().get(string);
        if (kit != null && kit.getDisplayName() != null && !kit.getDisplayName().isEmpty()) {
            return kit.withIcon(kit.getDisplayName());
        }
        return string;
    }

    private static final class Layout {
        private boolean enabled = false;
        private String title = "";
        private List<String> lines = new ArrayList<String>();
        private Component titleStatic;
        private Component[] lineStatic = new Component[0];

        private Layout() {
        }
    }

    private final class Board {
        private final Scoreboard scoreboard;
        private World healthWorld;
        private final Objective sidebarObjective;
        private final Team[] teams;
        private final int sidebarLines;
        private final boolean hasNametags;
        private final Team allyTeam;
        private final Team enemyTeam;
        private UUID allyId;
        private UUID enemyId;
        private String allyEntry;
        private String enemyEntry;
        private final boolean hasFfaTeam;
        private Team ffaTeam;
        private final Set<String> ffaEntries = new HashSet<String>();

        private Board(int n, boolean bl, boolean bl2) {
            this.hasFfaTeam = bl2;
            this.sidebarLines = n;
            this.hasNametags = bl;
            this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            this.teams = new Team[n];
            this.lastLines = new String[n];
            if (n > 0) {
                Objective objective = this.scoreboard.registerNewObjective("mdside", Criteria.DUMMY, "");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                try {
                    objective.numberFormat(NumberFormat.blank());
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                for (int i = 0; i < n; ++i) {
                    String string = LINE_KEYS[i % LINE_KEYS.length].toString();
                    Team team = this.scoreboard.registerNewTeam("line" + i);
                    team.addEntry(string);
                    objective.getScore(string).setScore(n - i);
                    this.teams[i] = team;
                }
                this.sidebarObjective = objective;
            } else {
                this.sidebarObjective = null;
            }
            if (bl) {
                this.allyTeam = this.scoreboard.registerNewTeam("md_ally");
                this.enemyTeam = this.scoreboard.registerNewTeam("md_enemy");
                this.allyTeam.setColor(ChatColor.WHITE);
                this.enemyTeam.setColor(ChatColor.WHITE);
            } else {
                this.allyTeam = null;
                this.enemyTeam = null;
            }
            if (bl2) {
                this.ffaTeam = this.scoreboard.registerNewTeam("md_ffa");
                this.ffaTeam.setColor(ChatColor.WHITE);
            } else {
                this.ffaTeam = null;
            }
            ScoreboardService.this.setupHealthBelowName(this.scoreboard);
        }

        private String lastTitle;
        private String[] lastLines;

        private void setTitle(Component component) {
            if (this.sidebarObjective != null) {
                this.sidebarObjective.displayName(component);
            }
        }

        private void setLine(int n, Component component) {
            if (n >= 0 && n < this.teams.length && this.teams[n] != null) {
                this.teams[n].prefix(component);
            }
        }

        private void setFfaMembers(Set<UUID> set) {
            if (this.ffaTeam == null) {
                return;
            }
            HashSet<String> hashSet = new HashSet<String>();
            for (UUID object : set) {
                String string = ScoreboardService.this.nameOf(object);
                if (string.isEmpty()) continue;
                hashSet.add(string);
            }
            for (String string : hashSet) {
                if (!this.ffaEntries.add(string)) continue;
                this.ffaTeam.addEntry(string);
            }
            Iterator<String> iterator = this.ffaEntries.iterator();
            while (iterator.hasNext()) {
                String string = iterator.next();
                if (hashSet.contains(string)) continue;
                try {
                    this.ffaTeam.removeEntry(string);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                iterator.remove();
            }
        }

        private Team partyAqua;
        private Team partyRed;
        private Team partyOut;
        private Team partyNeutral;
        private final Map<String, Team> partyEntries = new HashMap<String, Team>();

        /**
         * Puts every fighter in a team match on this viewer's board in Red,
         * Aqua or (once out) grey. Only sends anything when someone's team
         * changes, so a running match costs nothing per second.
         */
        private void setPartyTeams(Party party) {
            if (party == null) {
                if (this.partyEntries.isEmpty()) {
                    return;
                }
                for (Map.Entry<String, Team> entry : this.partyEntries.entrySet()) {
                    try {
                        entry.getValue().removeEntry(entry.getKey());
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                }
                this.partyEntries.clear();
                return;
            }
            if (this.partyAqua == null) {
                this.partyAqua = this.teamFor("md_paqua", ChatColor.AQUA, Marks.tag(Marks.AQUA));
                this.partyRed = this.teamFor("md_pred", ChatColor.RED, Marks.tag(Marks.RED));
                this.partyOut = this.teamFor("md_pout", ChatColor.DARK_GRAY, Marks.tagOut());
                this.partyNeutral = this.teamFor("md_pneutral", ChatColor.WHITE, "");
            }
            HashMap<String, Team> want = new HashMap<String, Team>();
            if (party.isFighting() && party.isTeamMode()) {
                for (Map.Entry<UUID, Party.Team> entry : party.getTeams().entrySet()) {
                    String name = ScoreboardService.this.nameOf(entry.getKey());
                    if (name.isEmpty() || entry.getValue() == null) continue;
                    Team team = party.getWatching().contains(entry.getKey()) ? this.partyOut : (entry.getValue() == Party.Team.AQUA ? this.partyAqua : this.partyRed);
                    want.put(name, team);
                }
            } else {
                Set<UUID> members = party.isFighting() ? party.involved() : party.getMembers();
                for (UUID id : members) {
                    String name = ScoreboardService.this.nameOf(id);
                    if (!name.isEmpty()) want.put(name, this.partyNeutral);
                }
            }
            for (Map.Entry<String, Team> entry : want.entrySet()) {
                if (this.partyEntries.get(entry.getKey()) == entry.getValue()) continue;
                entry.getValue().addEntry(entry.getKey());
                this.partyEntries.put(entry.getKey(), entry.getValue());
            }
            Iterator<Map.Entry<String, Team>> iterator = this.partyEntries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Team> entry = iterator.next();
                if (want.containsKey(entry.getKey())) continue;
                try {
                    entry.getValue().removeEntry(entry.getKey());
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                iterator.remove();
            }
        }

        private Team teamFor(String string, ChatColor chatColor, String prefix) {
            Team team = this.scoreboard.getTeam(string);
            if (team == null) {
                team = this.scoreboard.registerNewTeam(string);
            }
            team.setColor(chatColor);
            team.setPrefix(prefix);
            team.setSuffix("");
            return team;
        }

        private void setNametags(UUID uUID, UUID uUID2, boolean bl) {
            if (this.allyTeam == null) {
                return;
            }
            this.allyId = uUID;
            this.enemyId = uUID2;
            String string = ScoreboardService.this.nameOf(uUID);
            String string2 = ScoreboardService.this.nameOf(uUID2);
            if (!string.equals(this.allyEntry)) {
                this.allyTeam.addEntry(string);
                this.allyEntry = string;
            }
            if (!string2.equals(this.enemyEntry)) {
                this.enemyTeam.addEntry(string2);
                this.enemyEntry = string2;
            }
            this.allyTeam.setColor(ChatColor.WHITE);
            this.enemyTeam.setColor(ChatColor.WHITE);
            this.allyTeam.prefix(ScoreboardService.this.deserialize(""));
            this.enemyTeam.prefix(ScoreboardService.this.deserialize(""));
            this.allyTeam.suffix(ScoreboardService.this.deserialize(""));
            this.enemyTeam.suffix(ScoreboardService.this.deserialize(""));
        }
    }
}

