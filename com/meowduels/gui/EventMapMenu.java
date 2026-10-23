package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Arena;
import com.meowduels.util.Items;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EventMapMenu
extends Menu {
    private final MeowDuels plugin;
    private final Player hoster;
    private final String kit;
    private final int minutes;
    private final int slots;
    private static final String MENU = "eventmap";
    private static final int ROWS = 3;

    public EventMapMenu(MeowDuels meowDuels, Player player, String string, int n, int n2) {
        this.plugin = meowDuels;
        this.hoster = player;
        this.kit = string;
        this.minutes = n;
        this.slots = n2;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 3);
        this.createRaw(n, guiConfigManager.title(MENU, "&d&l\u1d07\u1d20\u1d07\u0274\u1d1b \u2192 \u1d18\u026a\u1d04\u1d0b \u1d00 \u1d0d\u1d00\u1d18"));
        guiConfigManager.frame(MENU, this.inventory, n);
        int n2 = n * 9;
        int n3 = 0;
        for (Arena arena : this.plugin.getArenaManager().all()) {
            if (n3 >= n2) break;
            if (!arena.isEventReady() || !arena.isEnabled()) continue;
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("arena", arena.getName());
            hashMap.put("world", arena.getWorldName());
            hashMap.put("border", this.borderLabel(arena));
            Items items = this.plugin.getDuelManager().isArenaInUse(arena.getName()) ? guiConfigManager.item(MENU, "arena-busy", hashMap, Material.BARRIER, "&e{arena}", "&7\u1d21\u1d0f\u0280\u029f\u1d05: &f{world}", "&7\u0299\u1d0f\u0280\u1d05\u1d07\u0280: &f{border}", "", "&c\u026a\u0274 \u1d1c\ua731\u1d07 \u0280\u026a\u0262\u029c\u1d1b \u0274\u1d0f\u1d21") : guiConfigManager.item(MENU, "arena-free", hashMap, Material.FILLED_MAP, "&e{arena}", "&7\u1d21\u1d0f\u0280\u029f\u1d05: &f{world}", "&7\u0299\u1d0f\u0280\u1d05\u1d07\u0280: &f{border}", "", "&a\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u029c\u1d0f\ua731\u1d1b \u029c\u1d07\u0280\u1d07");
            this.inventory.setItem(n3++, items.tag(this.plugin.keyButton(), arena.getName()).build());
        }
        if (n3 == 0) {
            this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "empty", 13, n)), guiConfigManager.item(MENU, "empty", Material.BARRIER, "&c\u0274\u1d0f \u1d07\u1d20\u1d07\u0274\u1d1b \u1d0d\u1d00\u1d18\ua731", "&7\u0274\u1d0f \u1d00\u0280\u1d07\u0274\u1d00 \u029c\u1d00\ua731 \u1d00\u0274 \u1d07\u1d20\u1d07\u0274\u1d1b \ua731\u1d18\u1d00\u1d21\u0274 \ua731\u1d07\u1d1b.", "&7\u1d0f\u1d18\u1d07\u0274 \u1d00\u0274 \u1d00\u0280\u1d07\u0274\u1d00 \u026a\u0274 &e/\u1d00\u0280\u1d07\u0274\u1d00&7 \u1d00\u0274\u1d05 \ua731\u1d07\u1d1b \u026a\u1d1b\ua731", "&7\u1d07\u1d20\u1d07\u0274\u1d1b \ua731\u1d18\u1d00\u1d21\u0274 \ua730\u026a\u0280\ua731\u1d1b.").build());
        }
    }

    private String borderLabel(Arena arena) {
        String string = arena.hasBorderRegion() ? String.valueOf((int)arena.getBorderStartSize()) : "auto";
        return string + " \u2192 " + arena.getEventBorderEnd() + " &7(every " + arena.getEventBorderInterval() + "s)";
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        Arena arena = this.plugin.getArenaManager().get(string);
        if (arena == null) {
            return;
        }
        if (this.plugin.getDuelManager().isArenaInUse(arena.getName())) {
            player.sendMessage(this.plugin.messages().get("notice.event.map-use-right-now", new String[0]));
            return;
        }
        player.closeInventory();
        this.plugin.getEventManager().host(this.hoster, this.kit, this.minutes, this.slots, arena);
    }
}

