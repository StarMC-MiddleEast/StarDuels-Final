package com.meowduels.model;

import java.util.UUID;

public class PartyDuelRequest {
    private final UUID from;
    private final UUID to;
    private final String kit;
    private final int rounds;
    private final long createdAt = System.currentTimeMillis();

    public PartyDuelRequest(UUID uUID, UUID uUID2, String string, int n) {
        this.from = uUID;
        this.to = uUID2;
        this.kit = string;
        this.rounds = n;
    }

    public UUID getFrom() {
        return this.from;
    }

    public UUID getTo() {
        return this.to;
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

