package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.managers.GuiLayoutManager;
import com.meowduels.model.Arena;
import com.meowduels.util.Items;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class GuiEditorMenu
extends Menu {
    private static final int CANCEL_SLOT = 45;
    private static final int PALETTE_SLOT = 47;
    private static final int SAVE_SLOT = 49;
    private static final int RESET_SLOT = 51;
    private final MeowDuels plugin;
    private final String menuId;
    private final int editableSize;
    private boolean done = false;
    private static final String MENU = "guieditor";

    public GuiEditorMenu(MeowDuels meowDuels, String string) {
        this.plugin = meowDuels;
        this.menuId = string;
        this.editableSize = GuiLayoutManager.editableSize(string);
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public boolean isProtectedSlot(int n) {
        return n >= this.editableSize;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("menu", this.menuId);
        this.createRaw(6, guiConfigManager.title(MENU, "&8\u1d07\u1d05\u026a\u1d1b\u026a\u0274\u0262: &d{menu}", hashMap));
        if (this.plugin.getGuiLayoutManager().has(this.menuId)) {
            for (Map.Entry<Integer, ItemStack> entry : this.plugin.getGuiLayoutManager().get(this.menuId).entrySet()) {
                if (entry.getKey() >= this.editableSize) continue;
                this.inventory.setItem(entry.getKey().intValue(), entry.getValue());
            }
        } else {
            this.fillDefaults();
        }
        ItemStack itemStack = guiConfigManager.face(MENU, "locked", null, GuiConfigManager.Face.of(Material.GRAY_STAINED_GLASS_PANE, "&8(\u1d0f\u1d1c\u1d1b\ua731\u026a\u1d05\u1d07 \u1d1b\u029c\u1d07 \u1d0d\u1d07\u0274\u1d1c)", new String[0])).build();
        for (int i = this.editableSize; i < 45; ++i) {
            this.inventory.setItem(i, itemStack);
        }
        ItemStack itemStack2 = guiConfigManager.pane(MENU);
        for (int i = 45; i < 54; ++i) {
            this.inventory.setItem(i, itemStack2);
        }
        this.inventory.setItem(45, guiConfigManager.face(MENU, "cancel", null, GuiConfigManager.Face.of(Material.BARRIER, "&c\u1d04\u1d00\u0274\u1d04\u1d07\u029f", "&7\u1d04\u029f\u1d0f\ua731\u1d07 \u1d21\u026a\u1d1b\u029c\u1d0f\u1d1c\u1d1b \ua731\u1d00\u1d20\u026a\u0274\u0262.")).build());
        this.inventory.setItem(47, guiConfigManager.face(MENU, "decoration", null, GuiConfigManager.Face.of(Material.GRAY_STAINED_GLASS_PANE, "&8\u1d05\u1d07\u1d04\u1d0f\u0280\u1d00\u1d1b\u026a\u1d0f\u0274", "&7\u1d04\u029f\u026a\u1d04\u1d0b \ua730\u1d0f\u0280 \u1d05\u1d07\u1d04\u1d0f\u0280\u1d00\u1d1b\u026a\u1d0f\u0274 \u1d18\u1d00\u0274\u1d07\ua731 \u1d1b\u1d0f \u1d18\u029f\u1d00\u1d04\u1d07.")).build());
        this.inventory.setItem(49, guiConfigManager.face(MENU, "save", null, GuiConfigManager.Face.of(Material.LIME_DYE, "&a&l\ua731\u1d00\u1d20\u1d07 \u029f\u1d00\u028f\u1d0f\u1d1c\u1d1b", "&7\ua731\u1d00\u1d20\u1d07 \u1d1b\u029c\u026a\ua731 \u1d00\u0280\u0280\u1d00\u0274\u0262\u1d07\u1d0d\u1d07\u0274\u1d1b.")).build());
        this.inventory.setItem(51, guiConfigManager.face(MENU, "reset", null, GuiConfigManager.Face.of(Material.TNT, "&c\u0280\u1d07\ua731\u1d07\u1d1b \u1d1b\u1d0f \u1d05\u1d07\ua730\u1d00\u1d1c\u029f\u1d1b", "&7\u0280\u1d07\ua731\u1d1b\u1d0f\u0280\u1d07 \u1d1b\u029c\u1d07 \u1d05\u1d07\ua730\u1d00\u1d1c\u029f\u1d1b \u029f\u1d00\u028f\u1d0f\u1d1c\u1d1b.")).build());
    }

    private void fillDefaults() {
        switch (this.menuId) {
            case "duelconfirm": {
                ItemStack itemStack = Items.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
                for (int i = 0; i < this.editableSize; ++i) {
                    this.inventory.setItem(i, itemStack);
                }
                this.inventory.setItem(10, this.buttonMarker("info", Material.ITEM_FRAME, "&bInformation"));
                this.inventory.setItem(11, this.buttonMarker("clock", Material.CLOCK, "&eRounds button"));
                this.inventory.setItem(12, this.buttonMarker("map", Material.PAPER, "&bMap Selection"));
                this.inventory.setItem(14, this.buttonMarker("confirm", Material.LIME_DYE, "&aSend Duel"));
                this.inventory.setItem(22, this.buttonMarker("cancel", Material.BARRIER, "&cGo Back"));
                break;
            }
            case "mapselect": {
                int n = 0;
                for (Arena arena : this.plugin.getArenaManager().all()) {
                    if (n >= this.editableSize - 1) break;
                    if (!arena.isConfigured()) continue;
                    this.inventory.setItem(n++, Items.of(Material.FILLED_MAP).name("&e" + arena.getName()).lore("&fArena icon - drag to arrange.").tag(this.plugin.keyArena(), arena.getName()).build());
                }
                this.inventory.setItem(this.editableSize - 1, this.buttonMarker("random", Material.ENDER_PEARL, "&eRandom button"));
                break;
            }
        }
    }

    private ItemStack buttonMarker(String string, Material material, String string2) {
        return Items.of(material).name(string2).lore("&7Menu button - drag to arrange.").tag(this.plugin.keyButton(), string).build();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        switch (inventoryClickEvent.getRawSlot()) {
            case 49: {
                this.persist();
                this.done = true;
                player.sendMessage(this.plugin.messages().get("notice.editgui.saved-menu-layout-2", "value", String.valueOf(this.menuId)));
                player.closeInventory();
                break;
            }
            case 45: {
                this.done = true;
                player.sendMessage(this.plugin.messages().get("notice.editgui.editor-closed-without-saving", new String[0]));
                player.closeInventory();
                break;
            }
            case 47: {
                player.getInventory().addItem(new ItemStack[]{Items.of(Material.GRAY_STAINED_GLASS_PANE, 16).name("&8Decoration").build()});
                player.sendMessage(this.plugin.messages().get("notice.editgui.added-decoration-panes-inventory", new String[0]));
                break;
            }
            case 51: {
                this.plugin.getGuiLayoutManager().clear(this.menuId);
                this.fillDefaultsFresh();
                player.openInventory(this.inventory);
                player.sendMessage(this.plugin.messages().get("notice.editgui.reset-default", "value", String.valueOf(this.menuId)));
            }
        }
    }

    private void fillDefaultsFresh() {
        for (int i = 0; i < this.editableSize; ++i) {
            this.inventory.setItem(i, null);
        }
        this.fillDefaults();
    }

    @Override
    public void onClose(Player player, InventoryCloseEvent inventoryCloseEvent) {
        if (this.done) {
            return;
        }
        this.persist();
        player.sendMessage(this.plugin.messages().get("notice.editgui.saved-menu-layout", "value", String.valueOf(this.menuId)));
    }

    private void persist() {
        LinkedHashMap<Integer, ItemStack> linkedHashMap = new LinkedHashMap<Integer, ItemStack>();
        for (int i = 0; i < this.editableSize; ++i) {
            ItemStack itemStack = this.inventory.getItem(i);
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            linkedHashMap.put(i, itemStack.clone());
        }
        switch (this.menuId) {
            case "duelconfirm": {
                this.ensureButton(linkedHashMap, "info", Material.ITEM_FRAME, "&bInformation", 10);
                this.ensureButton(linkedHashMap, "clock", Material.CLOCK, "&eRounds button", 11);
                this.ensureButton(linkedHashMap, "map", Material.PAPER, "&bMap Selection", 12);
                this.ensureButton(linkedHashMap, "confirm", Material.LIME_DYE, "&aSend Duel", 14);
                this.ensureButton(linkedHashMap, "cancel", Material.BARRIER, "&cGo Back", 22);
                break;
            }
            case "mapselect": {
                this.ensureButton(linkedHashMap, "random", Material.ENDER_PEARL, "&eRandom button", this.editableSize - 1);
            }
        }
        this.plugin.getGuiLayoutManager().set(this.menuId, linkedHashMap);
    }

    private void ensureButton(Map<Integer, ItemStack> map, String string, Material material, String string2, int n) {
        for (ItemStack itemStack : map.values()) {
            if (!string.equals(Items.readTag(itemStack, this.plugin.keyButton()))) continue;
            return;
        }
        int n2 = map.containsKey(n) ? this.firstFree(map) : n;
        int n3 = n2;
        if (n2 >= 0) {
            map.put(n2, this.buttonMarker(string, material, string2));
        }
    }

    private int firstFree(Map<Integer, ItemStack> map) {
        for (int i = 0; i < this.editableSize; ++i) {
            if (map.containsKey(i)) continue;
            return i;
        }
        return -1;
    }
}

