package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.DuelConfirmMenu;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Arena;
import com.meowduels.util.Items;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MapSelectMenu
extends Menu {
    private final MeowDuels plugin;
    private final DuelConfirmMenu confirm;
    private static final String MENU = "mapselect";
    private static final int ROWS = 4;
    private static final int SLOT_RANDOM = 35;

    public MapSelectMenu(MeowDuels meowDuels, DuelConfirmMenu duelConfirmMenu) {
        this.plugin = meowDuels;
        this.confirm = duelConfirmMenu;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 4);
        int n2 = n * 9;
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("target", this.confirm.getTarget().getName());
        this.createRaw(n, guiConfigManager.title(MENU, "&8\u1d05\u1d1c\u1d07\u029f \u1d0d\u1d00\u1d18: &d{target}", hashMap));
        String string = this.confirm.getSelectedKit();
        if (this.plugin.getGuiLayoutManager().has(MENU)) {
            for (Map.Entry<Integer, ItemStack> entry : this.plugin.getGuiLayoutManager().get(MENU).entrySet()) {
                if (entry.getKey() >= n2) continue;
                if ("random".equals(Items.readTag(entry.getValue(), this.plugin.keyButton()))) {
                    this.inventory.setItem(entry.getKey().intValue(), this.randomButton());
                    continue;
                }
                String string2 = Items.readTag(entry.getValue(), this.plugin.keyArena());
                if (string2 != null) {
                    ItemStack itemStack = this.liveArenaIcon(string2, string);
                    if (itemStack == null) continue;
                    this.inventory.setItem(entry.getKey().intValue(), itemStack);
                    continue;
                }
                this.inventory.setItem(entry.getKey().intValue(), entry.getValue());
            }
        } else {
            int n3 = this.inventory.getSize() / 9;
            this.plugin.gui().frame(MENU, this.inventory, n3);
            int n4 = 0;
            int n5 = this.inventory.getSize() - 1;
            for (Arena arena : this.plugin.getArenaManager().all()) {
                if (n4 >= n5) break;
                if (!arena.isConfigured() || string != null && !arena.supportsKit(string)) continue;
                this.inventory.setItem(n4++, this.liveArenaIcon(arena.getName(), string));
            }
            this.inventory.setItem(Math.min(n5, this.plugin.gui().slot(MENU, "random", 35, n3)), this.randomButton());
        }
    }

    private ItemStack randomButton() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        boolean bl = this.confirm.getSelectedArena() == null;
        return guiConfigManager.toggle(MENU, "random", bl, null, GuiConfigManager.Face.of(Material.ENDER_PEARL, "&a\u0280\u1d00\u0274\u1d05\u1d0f\u1d0d", "", "&a\ua731\u1d07\u029f\u1d07\u1d04\u1d1b\u1d07\u1d05").glow(), GuiConfigManager.Face.of(Material.ENDER_PEARL, "&e\u0280\u1d00\u0274\u1d05\u1d0f\u1d0d", "", "&f\u1d18\u026a\u1d04\u1d0b \u1d00\u0274\u028f \ua730\u0280\u1d07\u1d07 \u1d04\u1d0f\u1d0d\u1d18\u1d00\u1d1b\u026a\u0299\u029f\u1d07 \u1d00\u0280\u1d07\u0274\u1d00")).tag(this.plugin.keyButton(), "random").build();
    }

    private ItemStack liveArenaIcon(String string, String string2) {
        Arena arena = this.plugin.getArenaManager().get(string);
        if (arena == null || !arena.isConfigured()) {
            return null;
        }
        GuiConfigManager guiConfigManager = this.plugin.gui();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("arena", arena.getName());
        Items items = string.equalsIgnoreCase(this.confirm.getSelectedArena()) ? guiConfigManager.face(MENU, "arena-selected", hashMap, GuiConfigManager.Face.of(Material.FILLED_MAP, "&a{arena}", "", "&a\ua731\u1d07\u029f\u1d07\u1d04\u1d1b\u1d07\u1d05").glow()) : (string2 == null || arena.supportsKit(string2) ? guiConfigManager.face(MENU, "arena-free", hashMap, GuiConfigManager.Face.of(Material.FILLED_MAP, "&e{arena}", "", "&f\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d18\u026a\u1d04\u1d0b \u1d1b\u029c\u026a\ua731 \u1d0d\u1d00\u1d18")) : guiConfigManager.face(MENU, "arena-incompatible", hashMap, GuiConfigManager.Face.of(Material.FILLED_MAP, "&c{arena}", "", "&c\u026a\u0274\u1d04\u1d0f\u1d0d\u1d18\u1d00\u1d1b\u026a\u0299\u029f\u1d07 \u1d21\u026a\u1d1b\u029c \u1d1b\u029c\u026a\ua731 \u1d0b\u026a\u1d1b")));
        return items.tag(this.plugin.keyArena(), arena.getName()).build();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
        if ("random".equals(Items.readTag(itemStack, this.plugin.keyButton()))) {
            this.confirm.setSelectedArena(null);
            this.confirm.reopen(player);
            return;
        }
        String string = Items.readTag(itemStack, this.plugin.keyArena());
        if (string == null) {
            return;
        }
        Arena arena = this.plugin.getArenaManager().get(string);
        String string2 = this.confirm.getSelectedKit();
        if (arena == null || !arena.isConfigured()) {
            return;
        }
        if (string2 != null && !arena.supportsKit(string2)) {
            player.sendMessage(this.plugin.messages().get("menu.arena-incompatible", new String[0]));
            return;
        }
        this.confirm.setSelectedArena(arena.getName());
        this.confirm.reopen(player);
    }
}

