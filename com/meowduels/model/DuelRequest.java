package com.meowduels.model;

import java.util.UUID;

public class DuelRequest {
    private final UUID sender;
    private final UUID target;
    private final String kit;
    private final int rounds;
    private final String arena;
    private final long createdAt;

    public DuelRequest(UUID uUID, UUID uUID2, String string, int n, String string2) {
        this.sender = uUID;
        this.target = uUID2;
        this.kit = string;
        this.rounds = n;
        this.arena = string2;
        this.createdAt = System.currentTimeMillis();
    }

    public String getArena() {
        return this.arena;
    }

    public UUID getSender() {
        return this.sender;
    }

    public UUID getTarget() {
        return this.target;
    }

    public String getKit() {
        return this.kit;
    }

    public int getRounds() {
        return this.rounds;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public boolean isExpired(long l) {
        return System.currentTimeMillis() - this.createdAt > l;
    }
}

