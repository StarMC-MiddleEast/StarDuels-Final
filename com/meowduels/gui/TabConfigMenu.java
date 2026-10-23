package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.managers.TabEditManager;
import com.meowduels.util.Items;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TabConfigMenu
extends Menu {
    private final MeowDuels plugin;
    private static final String MENU = "tabconfig";
    private static final int ROWS = 3;
    private static final int SLOT_TITLE = 11;
    private static final int SLOT_DISCORD = 13;
    private static final int SLOT_STORE = 15;

    public TabConfigMenu(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 3);
        this.createRaw(n, guiConfigManager.title(MENU, "&7&l\u1d1b\u1d00\u0299 \ua731\u1d07\u1d1b\u1d1b\u026a\u0274\u0262\ua731"));
        guiConfigManager.frame(MENU, this.inventory, n);
        this.put(n, "title", guiConfigManager.slot(MENU, "title", 11, n), guiConfigManager.item(MENU, "title", this.tokens(this.plugin.getTabTitle()), Material.BOOK, "&e\u1d1b\u026a\u1d1b\u029f\u1d07", "&7\u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b:", "&f{current}", "", "&a\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d04\u029c\u1d00\u0274\u0262\u1d07 &7(\u1d0d\u026a\u0274\u026a\u1d0d\u1d07\ua731\ua731\u1d00\u0262\u1d07 / \u0262\u0280\u1d00\u1d05\u026a\u1d07\u0274\u1d1b\ua731)"));
        this.put(n, "discord", guiConfigManager.slot(MENU, "discord", 13, n), guiConfigManager.item(MENU, "discord", this.tokens(this.plugin.getTabDiscord()), Material.PAPER, "&d\u1d05\u026a\ua731\u1d04\u1d0f\u0280\u1d05", "&7\u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b: &f{current}", "", "&a\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d04\u029c\u1d00\u0274\u0262\u1d07"));
        this.put(n, "store", guiConfigManager.slot(MENU, "store", 15, n), guiConfigManager.item(MENU, "store", this.tokens(this.plugin.getTabStore()), Material.GOLDEN_APPLE, "&6\ua731\u1d1b\u1d0f\u0280\u1d07", "&7\u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b: &f{current}", "", "&a\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d04\u029c\u1d00\u0274\u0262\u1d07"));
    }

    private Map<String, String> tokens(String string) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("current", string == null ? "" : string);
        return hashMap;
    }

    private void put(int n, String string, int n2, Items items) {
        this.inventory.setItem(Math.max(0, Math.min(n * 9 - 1, n2)), items.tag(this.plugin.keyButton(), string).build());
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        TabEditManager.Field field = switch (string) {
            case "title" -> TabEditManager.Field.TITLE;
            case "discord" -> TabEditManager.Field.DISCORD;
            case "store" -> TabEditManager.Field.STORE;
            default -> null;
        };
        if (field != null) {
            player.closeInventory();
            this.plugin.getTabEditManager().begin(player, field);
        }
    }
}

