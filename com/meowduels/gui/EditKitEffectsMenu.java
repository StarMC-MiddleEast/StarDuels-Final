package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.KitItemsEditMenu;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.model.StartEffect;
import com.meowduels.util.Items;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EditKitEffectsMenu
extends Menu {
    private final MeowDuels plugin;
    private final Kit kit;
    private static final String MENU = "editkiteffects";
    private static final int ROWS = 3;
    private static final String ON = "&a\u1d07\u0274\u1d00\u0299\u029f\u1d07\u1d05";
    private static final String OFF = "&c\u1d05\u026a\ua731\u1d00\u0299\u029f\u1d07\u1d05";
    private static final String TOGGLE_HINT = "&7\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d1b\u1d0f\u0262\u0262\u029f\u1d07.";

    public EditKitEffectsMenu(MeowDuels meowDuels, Kit kit) {
        this.plugin = meowDuels;
        this.kit = kit;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 3);
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("kit", this.kit.getName());
        this.createRaw(n, guiConfigManager.title(MENU, "&7&l\u1d07\u1d05\u026a\u1d1b \u1d0b\u026a\u1d1b \u2192 &7{kit}", hashMap));
        guiConfigManager.frame(MENU, this.inventory, n);
        this.put(n, guiConfigManager.slot(MENU, "strength", 10, n), guiConfigManager.toggle(MENU, "strength", this.kit.hasStartEffect(StartEffect.STRENGTH2_90), hashMap, GuiConfigManager.Face.of(Material.BLAZE_POWDER, "&c\ua731\u1d1b\u0280\u1d07\u0274\u0262\u1d1b\u029c \u026a\u026a &7(1:30)", "", ON, TOGGLE_HINT).glow(), GuiConfigManager.Face.of(Material.BLAZE_POWDER, "&c\ua731\u1d1b\u0280\u1d07\u0274\u0262\u1d1b\u029c \u026a\u026a &7(1:30)", "", OFF, TOGGLE_HINT)), StartEffect.STRENGTH2_90.name());
        this.put(n, guiConfigManager.slot(MENU, "speed", 12, n), guiConfigManager.toggle(MENU, "speed", this.kit.hasStartEffect(StartEffect.SPEED2_90), hashMap, GuiConfigManager.Face.of(Material.SUGAR, "&d\ua731\u1d18\u1d07\u1d07\u1d05 \u026a\u026a &7(1:30)", "", ON, TOGGLE_HINT).glow(), GuiConfigManager.Face.of(Material.SUGAR, "&d\ua731\u1d18\u1d07\u1d07\u1d05 \u026a\u026a &7(1:30)", "", OFF, TOGGLE_HINT)), StartEffect.SPEED2_90.name());
        this.put(n, guiConfigManager.slot(MENU, "speed-infinite", 14, n), guiConfigManager.toggle(MENU, "speed-infinite", this.kit.hasStartEffect(StartEffect.SPEED2_INFINITE), hashMap, GuiConfigManager.Face.of(Material.FEATHER, "&d\ua731\u1d18\u1d07\u1d07\u1d05 \u026a\u026a &7(\u026a\u0274\ua730\u026a\u0274\u026a\u1d1b\u1d07)", "", ON, TOGGLE_HINT).glow(), GuiConfigManager.Face.of(Material.FEATHER, "&d\ua731\u1d18\u1d07\u1d07\u1d05 \u026a\u026a &7(\u026a\u0274\ua730\u026a\u0274\u026a\u1d1b\u1d07)", "", OFF, TOGGLE_HINT)), StartEffect.SPEED2_INFINITE.name());
        this.put(n, guiConfigManager.slot(MENU, "regeneration", 16, n), guiConfigManager.toggle(MENU, "regeneration", this.kit.hasStartEffect(StartEffect.REGEN2_90), hashMap, GuiConfigManager.Face.of(Material.GHAST_TEAR, "&a\u0280\u1d07\u0262\u1d07\u0274\u1d07\u0280\u1d00\u1d1b\u026a\u1d0f\u0274 \u026a\u026a &7(1:30)", "", ON, TOGGLE_HINT).glow(), GuiConfigManager.Face.of(Material.GHAST_TEAR, "&a\u0280\u1d07\u0262\u1d07\u0274\u1d07\u0280\u1d00\u1d1b\u026a\u1d0f\u0274 \u026a\u026a &7(1:30)", "", OFF, TOGGLE_HINT)), StartEffect.REGEN2_90.name());
        this.inventory.setItem(Math.min(n * 9 - 1, guiConfigManager.slot(MENU, "edit-items", 21, n)), guiConfigManager.item(MENU, "edit-items", hashMap, Material.CHEST, "&d&l\u1d07\u1d05\u026a\u1d1b \u026a\u1d1b\u1d07\u1d0d\ua731", "&7\u1d0f\u1d18\u1d07\u0274 \u1d1b\u029c\u1d07 \u1d0b\u026a\u1d1b'\ua731 \u026a\u1d1b\u1d07\u1d0d \u0262\u0280\u026a\u1d05.", "", "&c\u1d04\u029c\u1d00\u0274\u0262\u1d07\ua731 \u1d00\u1d18\u1d18\u029f\u028f \u1d1b\u1d0f \u1d07\u1d20\u1d07\u0280\u028f\u1d0f\u0274\u1d07.").tag(this.plugin.keyButton(), "edit-items").build());
        this.inventory.setItem(Math.min(n * 9 - 1, guiConfigManager.slot(MENU, "capture", 23, n)), guiConfigManager.item(MENU, "capture", hashMap, Material.PLAYER_HEAD, "&a&l\u1d04\u029c\u1d00\u0274\u0262\u1d07 \u1d0b\u026a\u1d1b", "&7\u1d0d\u1d00\u1d0b\u1d07 \u1d1b\u029c\u026a\ua731 \u1d0b\u026a\u1d1b \u1d07x\u1d00\u1d04\u1d1b\u029f\u028f \u1d21\u029c\u1d00\u1d1b \u028f\u1d0f\u1d1c", "&7\u1d00\u0280\u1d07 \u029c\u1d0f\u029f\u1d05\u026a\u0274\u0262 \u0280\u026a\u0262\u029c\u1d1b \u0274\u1d0f\u1d21.", "", "&c\u0280\u1d07\u1d18\u029f\u1d00\u1d04\u1d07\ua731 \u1d1b\u029c\u1d07 \u1d0b\u026a\u1d1b \ua730\u1d0f\u0280 \u1d07\u1d20\u1d07\u0280\u028f\u1d0f\u0274\u1d07.", "&e\ua731\u029c\u026a\ua730\u1d1b-\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d04\u1d0f\u0274\ua730\u026a\u0280\u1d0d.").tag(this.plugin.keyButton(), "capture").build());
    }

    private void put(int n, int n2, Items items, String string) {
        this.inventory.setItem(Math.max(0, Math.min(n * 9 - 1, n2)), items.hideTooltip().tag(this.plugin.keyButton(), string).build());
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        if ("edit-items".equals(string)) {
            player.closeInventory();
            new KitItemsEditMenu(this.plugin, this.kit).open(player);
            return;
        }
        if ("capture".equals(string)) {
            if (!inventoryClickEvent.isShiftClick()) {
                player.sendMessage(this.plugin.messages().get("notice.kit.shift-click-replace-kit", "kit", this.kit.getName()));
                return;
            }
            player.closeInventory();
            KitItemsEditMenu editor = new KitItemsEditMenu(this.plugin, this.kit);
            editor.open(player);
            editor.copyInventoryIntoEditor(player);
            return;
        }
        try {
            this.kit.toggleStartEffect(StartEffect.valueOf(string));
            this.plugin.getKitManager().save();
            this.build();
            player.openInventory(this.inventory);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
    }
}

