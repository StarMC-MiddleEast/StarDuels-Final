package com.meowduels.model;

import com.meowduels.model.Arena;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class ActiveDuel {
    private final UUID player1;
    private final UUID player2;
    private final Arena arena;
    private final String kit;
    private final int roundsToWin;
    private int score1;
    private int score2;
    private int currentRound = 1;
    private int gameNumber = 0;
    private boolean arenaEntered = false;
    private boolean finished = false;
    private long fightStartedAt = 0L;
    private final Set<UUID> ready = new HashSet<UUID>();
    private State state = State.STARTING;
    private final long startedAt = System.currentTimeMillis();
    private final Map<Location, BlockData> changedBlocks = new HashMap<Location, BlockData>();
    private final Map<UUID, Double> damageDealt = new HashMap<UUID, Double>();
    private final Map<UUID, Integer> hits = new HashMap<UUID, Integer>();
    private long countdownEndsAt;
    private long countdownTotalMs;

    public void recordHit(UUID uUID, double d) {
        if (uUID == null) {
            return;
        }
        Double d2 = this.damageDealt.get(uUID);
        this.damageDealt.put(uUID, (d2 == null ? 0.0 : d2) + Math.max(0.0, d));
        Integer n = this.hits.get(uUID);
        this.hits.put(uUID, (n == null ? 0 : n) + 1);
    }

    public double getDamageDealt(UUID uUID) {
        Double d = this.damageDealt.get(uUID);
        return d == null ? 0.0 : d;
    }

    public int getHits(UUID uUID) {
        Integer n = this.hits.get(uUID);
        return n == null ? 0 : n;
    }

    public ActiveDuel(UUID uUID, UUID uUID2, Arena arena, String string, int n) {
        this.player1 = uUID;
        this.player2 = uUID2;
        this.arena = arena;
        this.kit = string;
        this.roundsToWin = n;
    }

    public UUID getPlayer1() {
        return this.player1;
    }

    public UUID getPlayer2() {
        return this.player2;
    }

    public UUID getOpponent(UUID uUID) {
        return uUID.equals(this.player1) ? this.player2 : this.player1;
    }

    public boolean involves(UUID uUID) {
        return this.player1.equals(uUID) || this.player2.equals(uUID);
    }

    public Arena getArena() {
        return this.arena;
    }

    public String getKit() {
        return this.kit;
    }

    public int getRoundsToWin() {
        return this.roundsToWin;
    }

    public int getScore1() {
        return this.score1;
    }

    public int getScore2() {
        return this.score2;
    }

    public int getCurrentRound() {
        return this.currentRound;
    }

    public void nextRound() {
        ++this.currentRound;
    }

    public long getStartedAt() {
        return this.startedAt;
    }

    public int getGameNumber() {
        return this.gameNumber;
    }

    public void setGameNumber(int n) {
        this.gameNumber = n;
    }

    public void startCountdown(long l) {
        this.countdownTotalMs = Math.max(1L, l);
        this.countdownEndsAt = System.currentTimeMillis() + this.countdownTotalMs;
    }

    public long getCountdownRemainingMs() {
        return Math.max(0L, this.countdownEndsAt - System.currentTimeMillis());
    }

    public long getCountdownTotalMs() {
        return Math.max(1L, this.countdownTotalMs);
    }

    public boolean isArenaEntered() {
        return this.arenaEntered;
    }

    public void setArenaEntered(boolean bl) {
        this.arenaEntered = bl;
    }

    public boolean addReady(UUID uUID) {
        return this.ready.add(uUID);
    }

    public int getReadyCount() {
        return this.ready.size();
    }

    public void clearReady() {
        this.ready.clear();
    }

    public void markFightStart() {
        this.fightStartedAt = System.currentTimeMillis();
    }

    public long sinceFightStart() {
        return this.fightStartedAt == 0L ? Long.MAX_VALUE : System.currentTimeMillis() - this.fightStartedAt;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void setFinished(boolean bl) {
        this.finished = bl;
    }

    public boolean isAqua(UUID uUID) {
        return uUID.equals(this.player1);
    }

    public int getScoreFor(UUID uUID) {
        return uUID.equals(this.player1) ? this.score1 : this.score2;
    }

    public int getScoreAgainst(UUID uUID) {
        return uUID.equals(this.player1) ? this.score2 : this.score1;
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean awardRound(UUID uUID) {
        if (uUID.equals(this.player1)) {
            ++this.score1;
        } else {
            ++this.score2;
        }
        return this.score1 >= this.roundsToWin || this.score2 >= this.roundsToWin;
    }

    public UUID getMatchWinner() {
        if (this.score1 >= this.roundsToWin) {
            return this.player1;
        }
        if (this.score2 >= this.roundsToWin) {
            return this.player2;
        }
        return null;
    }

    public void recordChange(Location location, BlockData blockData) {
        Location location2 = new Location(location.getWorld(), (double)location.getBlockX(), (double)location.getBlockY(), (double)location.getBlockZ());
        this.changedBlocks.putIfAbsent(location2, blockData);
    }

    public Map<Location, BlockData> getChangedBlocks() {
        return this.changedBlocks;
    }

    public static enum State {
        STARTING,
        FIGHTING,
        ENDING;

    }
}

