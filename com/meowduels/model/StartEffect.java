package com.meowduels.model;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public enum StartEffect {
    STRENGTH2_90(PotionEffectType.STRENGTH, 1, 1800, "Strength II (1:30)"),
    SPEED2_90(PotionEffectType.SPEED, 1, 1800, "Speed II (1:30)"),
    FIRE_RESISTANCE2_90(PotionEffectType.FIRE_RESISTANCE, 1, 1800, "Fire Resistance II (1:30)"),
    SPEED2_INFINITE(PotionEffectType.SPEED, 1, -1, "Speed II (infinite)"),
    REGEN2_90(PotionEffectType.REGENERATION, 1, 1800, "Regeneration II (1:30)");

    private final PotionEffectType type;
    private final int amplifier;
    private final int durationTicks;
    private final String label;

    private StartEffect(PotionEffectType potionEffectType, int n2, int n3, String string2) {
        this.type = potionEffectType;
        this.amplifier = n2;
        this.durationTicks = n3;
        this.label = string2;
    }

    public String getLabel() {
        return this.label;
    }

    public PotionEffect toPotionEffect() {
        return new PotionEffect(this.type, this.durationTicks, this.amplifier);
    }
}

