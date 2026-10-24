package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.ActiveDuel;
import com.meowduels.model.Kit;
import com.meowduels.util.Items;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MatchSummaryMenu
extends Menu {
    private static final String REMATCH = "summary-rematch";
    private final MeowDuels plugin;
    private final ActiveDuel duel;
    private final UUID winnerId;
    private final UUID viewerId;
    private static final String MENU = "matchsummary";
    private static final int ROWS = 3;
    private static final int SLOT_YOU = 11;
    private static final int SLOT_MATCH = 13;
    private static final int SLOT_OPPONENT = 15;
    private static final int SLOT_REMATCH = 22;
    private static final String[] RESULT_LORE = new String[]{"&7\u028f\u1d0f\u1d1c: &f{you}", "", "&7\ua731\u1d04\u1d0f\u0280\u1d07: &f{score}", "&7\u029c\u026a\u1d1b\ua731: &f{hits}", "&7\u1d05\u1d00\u1d0d\u1d00\u0262\u1d07: &f{damage} &c\u2764", "", "&7\ua731\u1d1b\u0280\u1d07\u1d00\u1d0b: &6{streak}", "&7\u1d21\u026a\u0274\ua731: &b{wins}"};

    public MatchSummaryMenu(MeowDuels meowDuels, ActiveDuel activeDuel, UUID uUID, UUID uUID2) {
        this.plugin = meowDuels;
        this.duel = activeDuel;
        this.winnerId = uUID;
        this.viewerId = uUID2;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 3);
        int n2 = n * 9;
        this.createRaw(n, guiConfigManager.title(MENU, "\u1d0d\u1d00\u1d1b\u1d04\u029c \u0280\u1d07\u0455\u1d1c\u029f\u1d1b"));
        guiConfigManager.frame(MENU, this.inventory, n);
        UUID uUID = this.viewerId;
        UUID uUID2 = this.duel.getOpponent(uUID);
        boolean bl = uUID.equals(this.winnerId);
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("you", this.name(uUID));
        hashMap.put("opponent", this.name(uUID2));
        hashMap.put("kit", this.kitLabel());
        hashMap.put("score", String.valueOf(this.duel.getScoreFor(uUID)));
        hashMap.put("oppscore", String.valueOf(this.duel.getScoreFor(uUID2)));
        hashMap.put("hits", String.valueOf(this.duel.getHits(uUID)));
        hashMap.put("opphits", String.valueOf(this.duel.getHits(uUID2)));
        hashMap.put("damage", MatchSummaryMenu.fmt(this.duel.getDamageDealt(uUID)));
        hashMap.put("oppdamage", MatchSummaryMenu.fmt(this.duel.getDamageDealt(uUID2)));
        hashMap.put("rounds", String.valueOf(this.duel.getRoundsToWin()));
        hashMap.put("duration", this.duration());
        hashMap.put("streak", String.valueOf(this.plugin.getStatsManager().getStreak(uUID)));
        hashMap.put("wins", String.valueOf(this.plugin.getStatsManager().getWins(uUID)));
        Items items = bl ? Items.of(guiConfigManager.material(MENU, "result-won", Material.LIME_DYE)).miniName(guiConfigManager.miniName(MENU, "result-won", "<bold><gradient:#8BF58B:#2EB82E>\u1d20\u026a\u1d04\u1d1b\u1d0f\u0280\u028f</gradient></bold>", hashMap)).rawLore(guiConfigManager.lore(MENU, "result-won", hashMap, RESULT_LORE).toArray(new String[0])) : Items.of(guiConfigManager.material(MENU, "result-lost", Material.RED_DYE)).miniName(guiConfigManager.miniName(MENU, "result-lost", "<bold><gradient:#FF6B6B:#8B0000>\u1d05\u1d07\ua730\u1d07\u1d00\u1d1b</gradient></bold>", hashMap)).rawLore(guiConfigManager.lore(MENU, "result-lost", hashMap, RESULT_LORE).toArray(new String[0]));
        int n3 = bl ? guiConfigManager.slot(MENU, "result-won", 11, n) : guiConfigManager.slot(MENU, "result-lost", 11, n);
        this.inventory.setItem(Math.min(n2 - 1, n3), items.hideTooltip().build());
        this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "match", 13, n)), Items.of(guiConfigManager.material(MENU, "match", Material.PAPER)).miniName(guiConfigManager.miniName(MENU, "match", "<gradient:#FFE08A:#E0A13B>\u1d0d\u1d00\u1d1b\u1d04\u029c \u026a\u0274\ua730\u1d0f</gradient>", hashMap)).rawLore(guiConfigManager.lore(MENU, "match", hashMap, "&7\u1d0b\u026a\u1d1b: &f{kit}", "&7\ua730\u026a\u0274\u1d00\u029f: &a{score} &7- &c{oppscore}", "&7\u0280\u1d0f\u1d1c\u0274\u1d05\ua731: &f{rounds} \u1d1b\u1d0f \u1d21\u026a\u0274", "&7\u1d05\u1d1c\u0280\u1d00\u1d1b\u026a\u1d0f\u0274: &f{duration}").toArray(new String[0])).hideTooltip().build());
        this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "opponent", 15, n)), Items.of(guiConfigManager.material(MENU, "opponent", Material.PLAYER_HEAD)).miniName(guiConfigManager.miniName(MENU, "opponent", "<gradient:#C0C0C0:#6E6E6E>\u1d0f\u1d18\u1d18\u1d0f\u0274\u1d07\u0274\u1d1b</gradient>", hashMap)).rawLore(guiConfigManager.lore(MENU, "opponent", hashMap, "&7{opponent}", "", "&7\ua731\u1d04\u1d0f\u0280\u1d07: &f{oppscore}", "&7\u029c\u026a\u1d1b\ua731: &f{opphits}", "&7\u1d05\u1d00\u1d0d\u1d00\u0262\u1d07: &f{oppdamage} &c\u2764").toArray(new String[0])).hideTooltip().build());
        this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "rematch", 22, n)), Items.of(guiConfigManager.material(MENU, "rematch", Material.NETHER_STAR)).miniName(guiConfigManager.miniName(MENU, "rematch", "<bold><gradient:#FF5C5C:#8B0000>\u0280\u1d07\u1d0d\u1d00\u1d1b\u1d04\u029c \u2694\ufe0f</gradient></bold>", hashMap)).rawLore(guiConfigManager.lore(MENU, "rematch", hashMap, "&7\u1d04\u029c\u1d00\u029f\u029f\u1d07\u0274\u0262\u1d07 &f{opponent} &7\u1d00\u0262\u1d00\u026a\u0274", "", "&e\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \ua731\u1d07\u0274\u1d05 \u1d00 \u0280\u1d07\u1d0d\u1d00\u1d1b\u1d04\u029c.").toArray(new String[0])).hideTooltip().tag(this.plugin.keyButton(), REMATCH).build());
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (REMATCH.equals(string)) {
            player.closeInventory();
            this.plugin.getDuelManager().requestRematch(player);
        }
    }

    private String kitLabel() {
        Kit kit = this.plugin.getKitManager().get(this.duel.getKit());
        if (kit != null && kit.getDisplayName() != null && !kit.getDisplayName().isEmpty()) {
            return kit.withIcon(kit.getDisplayName());
        }
        return this.duel.getKit();
    }

    private String duration() {
        long l = Math.max(0L, (System.currentTimeMillis() - this.duel.getStartedAt()) / 1000L);
        return String.format("%02d:%02d", l / 60L, l % 60L);
    }

    private String name(UUID uUID) {
        if (uUID == null) {
            return "?";
        }
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            return player.getName();
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((UUID)uUID);
        return offlinePlayer.getName() == null ? "?" : offlinePlayer.getName();
    }

    private static String fmt(double d) {
        return String.format(Locale.US, "%.1f", d);
    }
}

