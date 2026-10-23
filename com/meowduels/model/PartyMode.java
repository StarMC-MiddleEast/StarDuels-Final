package com.meowduels.model;

public enum PartyMode {
    FFA("\u1d18\u1d00\u0280\u1d1b\u028f \ua730\ua730\u1d00", "\u1d07\u1d20\u1d07\u0280\u028f\u1d0f\u0274\u1d07 \ua730\u1d0f\u0280 \u1d1b\u029c\u1d07\u1d0d\ua731\u1d07\u029f\u1d20\u1d07\ua731, \u029f\u1d00\ua731\u1d1b \u1d0f\u0274\u1d07 \ua731\u1d1b\u1d00\u0274\u1d05\u026a\u0274\u0262", true),
    SPLIT("\u1d18\u1d00\u0280\u1d1b\u028f \ua731\u1d18\u029f\u026a\u1d1b", "\u1d1b\u1d21\u1d0f \u1d1b\u1d07\u1d00\u1d0d\ua731, \u1d00\ua7af\u1d1c\u1d00 \u1d00\u0262\u1d00\u026a\u0274\ua731\u1d1b \u0280\u1d07\u1d05", true),
    DUELS("\u1d18\u1d00\u0280\u1d1b\u028f \u1d05\u1d1c\u1d07\u029f\ua731", "\u028f\u1d0f\u1d1c\u0280 \u1d18\u1d00\u0280\u1d1b\u028f \u1d20\ua731 \u1d00\u0274\u1d0f\u1d1b\u029c\u1d07\u0280, \u1d1b\u1d07\u1d00\u1d0d \u1d20\ua731 \u1d1b\u1d07\u1d00\u1d0d", true);

    private final String label;
    private final String description;
    private final boolean ready;

    private PartyMode(String string2, String string3, boolean bl) {
        this.label = string2;
        this.description = string3;
        this.ready = bl;
    }

    public String getLabel() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isReady() {
        return this.ready;
    }
}

