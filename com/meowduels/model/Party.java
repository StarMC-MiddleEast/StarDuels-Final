package com.meowduels.model;

import com.meowduels.model.Arena;
import com.meowduels.model.PartyMode;
import com.meowduels.model.PlayerSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class Party {
    private final UUID leader;
    private final Set<UUID> members = new LinkedHashSet<UUID>();
    private final Set<UUID> invited = new HashSet<UUID>();
    private final Set<UUID> alive = ConcurrentHashMap.newKeySet();
    private final Set<UUID> watching = ConcurrentHashMap.newKeySet();
    private final Map<UUID, PlayerSnapshot> snapshots = new HashMap<UUID, PlayerSnapshot>();
    private final Map<UUID, Integer> kills = new HashMap<UUID, Integer>();
    private final Map<Location, BlockData> changedBlocks = new HashMap<Location, BlockData>();
    private final Map<UUID, Team> teams = new HashMap<UUID, Team>();
    private PartyMode mode = PartyMode.FFA;
    private UUID duelTarget;
    private int duelRounds = 3;
    private Party opponent;
    private boolean matchHost;
    private int roundsToWin = 1;
    private int round = 1;
    private int scoreAqua;
    private int scoreRed;
    private State state = State.IDLE;
    private String kit;
    private String hungerGamesArena;
    private String hungerGamesKit;
    private String kitBeforeHungerGames;
    private Arena arena;
    private long startedAt;
    private long fightStartsAt;
    private boolean friendlyFire = true;
    private boolean openToAll = false;
    private boolean finished = false;

    public Party(UUID uUID) {
        this.leader = uUID;
        this.members.add(uUID);
    }

    public UUID getLeader() {
        return this.leader;
    }

    public boolean isLeader(UUID uUID) {
        return this.leader.equals(uUID);
    }

    public Set<UUID> getMembers() {
        return this.members;
    }

    public int size() {
        return this.members.size();
    }

    public boolean has(UUID uUID) {
        return this.members.contains(uUID);
    }

    public void add(UUID uUID) {
        this.members.add(uUID);
        this.invited.remove(uUID);
    }

    public void remove(UUID uUID) {
        this.members.remove(uUID);
        this.teams.remove(uUID);
        this.invited.remove(uUID);
        this.alive.remove(uUID);
        this.watching.remove(uUID);
        this.snapshots.remove(uUID);
        this.kills.remove(uUID);
    }

    public Set<UUID> getInvited() {
        return this.invited;
    }

    public void invite(UUID uUID) {
        this.invited.add(uUID);
    }

    public boolean isInvited(UUID uUID) {
        return this.invited.contains(uUID);
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isFighting() {
        return this.state == State.FIGHTING;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void setFinished(boolean bl) {
        this.finished = bl;
    }

    public String getKit() {
        return this.kit;
    }

    public void setKit(String string) {
        this.kit = string;
    }

    public String getHungerGamesArena() { return this.hungerGamesArena; }

    public void setHungerGamesArena(String name) { this.hungerGamesArena = name; }

    public String getHungerGamesKit() { return this.hungerGamesKit; }

    public void setHungerGamesKit(String name) { this.hungerGamesKit = name; }

    public void setKitBeforeHungerGames(String name) { this.kitBeforeHungerGames = name; }

    public Arena getArena() {
        return this.arena;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public long getStartedAt() {
        return this.startedAt;
    }

    public long getFightStartsAt() {
        return this.fightStartsAt;
    }

    public void setFightStartsAt(long l) {
        this.fightStartsAt = l;
    }

    public boolean isCountingDown() {
        return this.state == State.FIGHTING && System.currentTimeMillis() < this.fightStartsAt;
    }

    public void setStartedAt(long l) {
        this.startedAt = l;
    }

    public Set<UUID> getAlive() {
        return this.alive;
    }

    public Set<UUID> getWatching() {
        return this.watching;
    }

    public Map<UUID, PlayerSnapshot> getSnapshots() {
        return this.snapshots;
    }

    public int killsOf(UUID uUID) {
        Integer n = this.kills.get(uUID);
        return n == null ? 0 : n;
    }

    public void addKill(UUID uUID) {
        if (this.members.contains(uUID)) {
            this.kills.merge(uUID, 1, Integer::sum);
        }
    }

    public boolean isFriendlyFire() {
        return this.friendlyFire;
    }

    public void setFriendlyFire(boolean bl) {
        this.friendlyFire = bl;
    }

    public boolean isOpenToAll() {
        return this.openToAll;
    }

    public void setOpenToAll(boolean bl) {
        this.openToAll = bl;
    }

    public Set<UUID> involved() {
        HashSet<UUID> hashSet = new HashSet<UUID>(this.alive);
        hashSet.addAll(this.watching);
        return hashSet;
    }

    public void recordChange(Location location, BlockData blockData) {
        Location location2 = new Location(location.getWorld(), (double)location.getBlockX(), (double)location.getBlockY(), (double)location.getBlockZ());
        this.changedBlocks.putIfAbsent(location2, blockData);
    }

    public Map<Location, BlockData> getChangedBlocks() {
        return this.changedBlocks;
    }

    public UUID getDuelTarget() {
        return this.duelTarget;
    }

    public void setDuelTarget(UUID uUID) {
        this.duelTarget = uUID;
    }

    public int getDuelRounds() {
        return this.duelRounds;
    }

    public void setDuelRounds(int n) {
        this.duelRounds = Math.max(1, Math.min(9, n));
    }

    public PartyMode getMode() {
        return this.mode;
    }

    public void setMode(PartyMode partyMode) {
        this.mode = partyMode == null ? PartyMode.FFA : partyMode;
    }

    public Party getOpponent() {
        return this.opponent;
    }

    public void setOpponent(Party party) {
        this.opponent = party;
    }

    public int getRoundsToWin() {
        return this.roundsToWin;
    }

    public void setRoundsToWin(int n) {
        this.roundsToWin = Math.max(1, Math.min(9, n));
    }

    public int getRound() {
        return this.round;
    }

    public void setRound(int n) {
        this.round = Math.max(1, n);
    }

    public int scoreOf(Team team) {
        return team == Team.AQUA ? this.scoreAqua : this.scoreRed;
    }

    public void addScore(Team team) {
        if (team == Team.AQUA) {
            ++this.scoreAqua;
        } else {
            ++this.scoreRed;
        }
    }

    public boolean isMatchHost() {
        return this.matchHost;
    }

    public void setMatchHost(boolean bl) {
        this.matchHost = bl;
    }

    public boolean isTeamMode() {
        return this.mode == PartyMode.SPLIT || this.mode == PartyMode.DUELS;
    }

    public boolean isSplit() {
        return this.mode == PartyMode.SPLIT;
    }

    public Map<UUID, Team> getTeams() {
        return this.teams;
    }

    public Team teamOf(UUID uUID) {
        return this.teams.get(uUID);
    }

    public void setTeam(UUID uUID, Team team) {
        if (this.members.contains(uUID)) {
            this.teams.put(uUID, team);
        }
    }

    public List<UUID> teamMembers(Team team) {
        ArrayList<UUID> arrayList = new ArrayList<UUID>();
        for (UUID uUID : this.members) {
            if (this.teams.get(uUID) != team) continue;
            arrayList.add(uUID);
        }
        return arrayList;
    }

    public void shuffleTeams() {
        ArrayList<UUID> arrayList = new ArrayList<UUID>(this.members);
        Collections.shuffle(arrayList);
        this.teams.clear();
        int n = (arrayList.size() + 1) / 2;
        for (int i = 0; i < arrayList.size(); ++i) {
            this.teams.put(arrayList.get(i), i < n ? Team.AQUA : Team.RED);
        }
    }

    public int aliveOn(Team team) {
        int n = 0;
        for (UUID uUID : this.alive) {
            if (this.teams.get(uUID) != team) continue;
            ++n;
        }
        return n;
    }

    public int aliveOrMembers(Team team) {
        return this.state == State.FIGHTING ? this.aliveOn(team) : this.teamMembers(team).size();
    }

    public void resetMatch() {
        if (this.mode == PartyMode.HUNGERGAMES) {
            this.kit = this.kitBeforeHungerGames;
            this.kitBeforeHungerGames = null;
        }
        this.state = State.IDLE;
        this.finished = false;
        this.alive.clear();
        this.watching.clear();
        this.snapshots.clear();
        this.kills.clear();
        this.arena = null;
        this.startedAt = 0L;
        this.fightStartsAt = 0L;
        this.changedBlocks.clear();
        this.teams.clear();
        this.mode = PartyMode.FFA;
        this.hungerGamesArena = null;
        this.duelTarget = null;
        this.opponent = null;
        this.matchHost = false;
        this.roundsToWin = 1;
        this.round = 1;
        this.scoreAqua = 0;
        this.scoreRed = 0;
    }

    public static enum State {
        IDLE,
        FIGHTING;

    }

    public static enum Team {
        AQUA,
        RED;

    }
}

