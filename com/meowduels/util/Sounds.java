package com.meowduels.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class Sounds {
    private static volatile Predicate<Player> muted;
    private static volatile Map<String, Cue> cues;

    private Sounds() {
    }

    public static void mutedWhen(Predicate<Player> predicate) {
        muted = predicate;
    }

    public static void load(ConfigurationSection configurationSection) {
        HashMap<String, Cue> hashMap = new HashMap<String, Cue>();
        if (configurationSection != null) {
            for (String string : configurationSection.getKeys(false)) {
                ConfigurationSection configurationSection2 = configurationSection.getConfigurationSection(string);
                if (configurationSection2 == null) continue;
                Cue cue = new Cue(configurationSection2.getString("key", null), (float)configurationSection2.getDouble("volume", Double.NaN), (float)configurationSection2.getDouble("pitch", Double.NaN), (float)configurationSection2.getDouble("pitch-to", Double.NaN));
                hashMap.put(string, Sounds.upgradeStockCue(string, cue));
            }
        }
        cues = hashMap;
    }

    private static Cue upgradeStockCue(String string, Cue cue) {
        if (cue.key == null || Float.isNaN(cue.volume) || Float.isNaN(cue.pitch)) {
            return cue;
        }
        if ("queueJoin".equals(string) && ("entity.experience_orb.pickup".equals(cue.key) || "block.note_block.chime".equals(cue.key) && Sounds.close(cue.volume, 0.75f) && Sounds.close(cue.pitch, 1.65f))) {
            return new Cue("block.note_block.chime", 0.9f, 1.5f, Float.NaN);
        }
        if ("queueLeave".equals(string) && "block.note_block.bass".equals(cue.key) && Sounds.close(cue.volume, 0.55f) && Sounds.close(cue.pitch, 1.25f)) {
            return new Cue("block.note_block.harp", 0.8f, 1.2f, Float.NaN);
        }
        if ("ready".equals(string) && "block.note_block.pling".equals(cue.key) && Sounds.close(cue.volume, 1.0f) && Sounds.close(cue.pitch, 1.8f)) {
            return new Cue("block.note_block.banjo", 0.9f, 1.2f, Float.NaN);
        }
        if ("matchFound".equals(string) && ("block.note_block.bell".equals(cue.key) && Sounds.close(cue.volume, 1.0f) && Sounds.close(cue.pitch, 1.2f) || "ui.toast.in".equals(cue.key) && Sounds.close(cue.volume, 0.9f) && Sounds.close(cue.pitch, 1.0f))) {
            return new Cue("entity.player.levelup", 0.9f, 1.0f, Float.NaN);
        }
        if ("countdown".equals(string) && "block.note_block.pling".equals(cue.key) && (Sounds.close(cue.volume, 1.0f) && Sounds.close(cue.pitch, 1.0f) && Float.isNaN(cue.pitchTo) || Sounds.close(cue.volume, 0.9f) && Sounds.close(cue.pitch, 0.9f) && Sounds.close(cue.pitchTo, 1.7f))) {
            return new Cue("block.note_block.xylophone", 0.9f, 0.9f, 1.7f);
        }
        if ("fight".equals(string) && ("block.note_block.pling".equals(cue.key) && Sounds.close(cue.volume, 1.0f) && Sounds.close(cue.pitch, 2.0f) || "block.note_block.bell".equals(cue.key) && Sounds.close(cue.volume, 0.9f) && Sounds.close(cue.pitch, 1.5f))) {
            return new Cue("block.note_block.bell", 0.9f, 1.7f, Float.NaN);
        }
        return cue;
    }

    private static boolean close(float f, float f2) {
        return Math.abs(f - f2) < 0.001f;
    }

    private static float ramp(String string, float f, float f2, float f3) {
        Cue cue = cues.get(string);
        if (cue != null && !Float.isNaN(cue.pitch)) {
            f = cue.pitch;
        }
        if (cue != null && !Float.isNaN(cue.pitchTo)) {
            f2 = cue.pitchTo;
        }
        return f + (f2 - f) * Math.max(0.0f, Math.min(1.0f, f3));
    }

    private static void play(Player player, String string, String string2, float f, float f2) {
        if (player == null) {
            return;
        }
        Predicate<Player> predicate = muted;
        if (predicate != null && predicate.test(player)) {
            return;
        }
        Cue cue = cues.get(string);
        if (cue != null) {
            if (cue.key != null) {
                string2 = cue.key;
            }
            if (!Float.isNaN(cue.volume)) {
                f = cue.volume;
            }
            if (!Float.isNaN(cue.pitch)) {
                f2 = cue.pitch;
            }
        }
        if (string2 == null || string2.isEmpty()) {
            return;
        }
        player.playSound(player.getLocation(), string2, f, f2);
    }

    private static void playAtPitch(Player player, String string, String string2, float f, float f2) {
        if (player == null) {
            return;
        }
        Predicate<Player> predicate = muted;
        if (predicate != null && predicate.test(player)) {
            return;
        }
        Cue cue = cues.get(string);
        if (cue != null) {
            if (cue.key != null) {
                string2 = cue.key;
            }
            if (!Float.isNaN(cue.volume)) {
                f = cue.volume;
            }
        }
        if (string2 != null && !string2.isEmpty()) {
            player.playSound(player.getLocation(), string2, f, f2);
        }
    }

    public static void click(Player player) {
        Sounds.play(player, "click", "ui.button.click", 0.5f, 1.3f);
    }

    public static void request(Player player) {
        Sounds.play(player, "request", "entity.experience_orb.pickup", 1.0f, 1.4f);
    }

    public static void accept(Player player) {
        Sounds.play(player, "accept", "block.note_block.chime", 1.0f, 1.2f);
    }

    public static void ready(Player player) {
        Sounds.play(player, "ready", "block.note_block.banjo", 0.9f, 1.2f);
    }

    public static void matchFound(Player player) {
        Sounds.play(player, "matchFound", "entity.player.levelup", 0.9f, 1.0f);
    }

    public static void countdown(Player player, int n, int n2) {
        int n3 = Math.max(1, n2);
        int n4 = Math.max(0, Math.min(n3, n));
        float f = n3 <= 1 ? 1.0f : (float)(n3 - n4) / (float)(n3 - 1);
        float f2 = Sounds.ramp("countdown", 0.9f, 1.7f, f);
        Sounds.playAtPitch(player, "countdown", "block.note_block.xylophone", 0.9f, f2);
    }

    public static void fight(Player player) {
        Sounds.play(player, "fight", "block.note_block.bell", 0.9f, 1.7f);
    }

    public static void roundWon(Player player) {
        Sounds.play(player, "roundWon", "block.note_block.pling", 1.0f, 1.6f);
    }

    public static void roundLost(Player player) {
        Sounds.play(player, "roundLost", "block.note_block.bass", 1.0f, 0.8f);
    }

    public static void victory(Player player) {
        Sounds.play(player, "victory", "ui.toast.challenge_complete", 1.0f, 1.0f);
    }

    public static void defeat(Player player) {
        Sounds.play(player, "defeat", "entity.villager.no", 1.0f, 1.0f);
    }

    public static void open(Player player) {
        Sounds.play(player, "open", "block.barrel.open", 0.35f, 1.6f);
    }

    public static void close(Player player) {
        Sounds.play(player, "close", "block.barrel.close", 0.35f, 1.6f);
    }

    public static void deny(Player player) {
        Sounds.play(player, "deny", "block.note_block.bass", 0.7f, 0.6f);
    }

    public static void select(Player player) {
        Sounds.play(player, "select", "block.note_block.hat", 0.7f, 1.5f);
    }

    public static void queueClick(Player player) {
        Sounds.play(player, "queueClick", "block.amethyst_block.chime", 0.7f, 1.25f);
    }

    public static void duelMenuClick(Player player) {
        Sounds.play(player, "duelMenuClick", "block.amethyst_block.chime", 0.8f, 1.7f);
    }

    public static void queueJoin(Player player) {
        Sounds.play(player, "queueJoin", "block.note_block.chime", 0.9f, 1.5f);
    }

    public static void queueLeave(Player player) {
        Sounds.play(player, "queueLeave", "block.note_block.harp", 0.8f, 1.2f);
    }

    public static void partyCreate(Player player) {
        Sounds.play(player, "partyCreate", "item.goat_horn.sound.0", 0.7f, 1.0f);
    }

    public static void invite(Player player) {
        Sounds.play(player, "invite", "block.note_block.chime", 1.0f, 1.6f);
    }

    public static void join(Player player) {
        Sounds.play(player, "join", "entity.experience_orb.pickup", 0.8f, 1.6f);
    }

    public static void leave(Player player) {
        Sounds.play(player, "leave", "block.note_block.bass", 0.8f, 1.0f);
    }

    public static void kill(Player player) {
        Sounds.play(player, "kill", "entity.player.attack.crit", 1.0f, 1.2f);
    }

    public static void death(Player player) {
        Sounds.play(player, "death", "entity.wither.spawn", 0.4f, 1.8f);
    }

    public static void eliminated(Player player) {
        Sounds.play(player, "eliminated", "block.note_block.bass", 0.8f, 0.7f);
    }

    public static void tick(Player player, int n, int n2) {
        int n3 = Math.max(1, n2);
        float f = 1.0f - (float)Math.max(0, Math.min(n3, n)) / (float)n3;
        Sounds.play(player, "tick", "block.note_block.pling", 1.0f, Sounds.ramp("tick", 0.9f, 1.6f, f));
    }

    public static void border(Player player, int n) {
        float f = (float)Math.min(10, n) / 10.0f;
        Sounds.play(player, "border", "block.note_block.didgeridoo", 0.7f, Sounds.ramp("border", 0.8f, 1.4f, f));
    }

    public static void eat(Player player) {
        Sounds.play(player, "eat", "entity.player.burp", 0.7f, 1.0f);
    }

    public static void cooling(Player player) {
        Sounds.play(player, "cooling", "block.note_block.bass", 0.6f, 1.2f);
    }

    public static void refreshed(Player player) {
        Sounds.play(player, "refreshed", "block.note_block.bell", 0.5f, 1.9f);
    }

    static {
        cues = Collections.emptyMap();
    }

    private static final class Cue {
        final String key;
        final float volume;
        final float pitch;
        final float pitchTo;

        Cue(String string, float f, float f2, float f3) {
            this.key = string;
            this.volume = f;
            this.pitch = f2;
            this.pitchTo = f3;
        }
    }
}

