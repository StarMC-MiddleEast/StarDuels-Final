package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.ArenaMenu;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Arena;
import com.meowduels.model.Kit;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class KitSelectMenu
extends Menu {
    private final MeowDuels plugin;
    private final Arena arena;
    private List<Kit> kits;
    private static final String MENU = "kitselect";

    public KitSelectMenu(MeowDuels meowDuels, Arena arena) {
        this.plugin = meowDuels;
        this.arena = arena;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("arena", this.arena.getName());
        this.createRaw(6, guiConfigManager.title(MENU, "&8\u1d04\u1d0f\u1d0d\u1d18\u1d00\u1d1b\u026a\u0299\u029f\u1d07 \u1d0b\u026a\u1d1b\ua731: &d{arena}", hashMap));
        this.kits = this.plugin.getKitManager().all();
        if (this.kits.isEmpty()) {
            this.inventory.setItem(22, guiConfigManager.face(MENU, "empty", null, GuiConfigManager.Face.of(Material.BARRIER, "&c\u0274\u1d0f \u1d0b\u026a\u1d1b\ua731 \u1d04\u0280\u1d07\u1d00\u1d1b\u1d07\u1d05 \u028f\u1d07\u1d1b", "&7\u1d1c\ua731\u1d07 &e/\u1d0b\u026a\u1d1b\u1d04\u0280\u1d07\u1d00\u1d1b\u1d07 <\u0274\u1d00\u1d0d\u1d07>&7 \ua730\u026a\u0280\ua731\u1d1b.")).build());
        }
        for (int i = 0; i < this.kits.size() && i < 45; ++i) {
            Kit kit = this.kits.get(i);
            HashMap<String, String> hashMap2 = new HashMap<String, String>();
            hashMap2.put("kit", kit.getName());
            this.inventory.setItem(i, guiConfigManager.toggle(MENU, "kit", this.arena.supportsKitExplicit(kit.getName()), hashMap2, GuiConfigManager.Face.of(guiConfigManager.material(MENU, "kit", kit.getIcon()), "&a{kit}", "", "&a\u1d07\u0274\u1d00\u0299\u029f\u1d07\u1d05 &8(\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d05\u026a\ua731\u1d00\u0299\u029f\u1d07)").glow(), GuiConfigManager.Face.of(guiConfigManager.material(MENU, "kit", kit.getIcon()), "&7{kit}", "", "&c\u1d05\u026a\ua731\u1d00\u0299\u029f\u1d07\u1d05 &8(\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d07\u0274\u1d00\u0299\u029f\u1d07)")).build());
        }
        ItemStack itemStack = guiConfigManager.pane(MENU);
        for (int i = 45; i < 54; ++i) {
            this.inventory.setItem(i, itemStack);
        }
        this.inventory.setItem(49, guiConfigManager.face(MENU, "back", null, GuiConfigManager.Face.of(Material.ARROW, "&e\u0299\u1d00\u1d04\u1d0b", "&7\u0280\u1d07\u1d1b\u1d1c\u0280\u0274 \u1d1b\u1d0f \u1d1b\u029c\u1d07 \u1d00\u0280\u1d07\u0274\u1d00 \u1d0d\u1d07\u0274\u1d1c.")).build());
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        int n = inventoryClickEvent.getRawSlot();
        if (n == 49) {
            new ArenaMenu(this.plugin, this.arena).open(player);
            return;
        }
        if (n < 0 || n >= 45 || this.kits == null || n >= this.kits.size()) {
            return;
        }
        Kit kit = this.kits.get(n);
        if (this.arena.supportsKitExplicit(kit.getName())) {
            this.arena.getKits().removeIf(string -> string.equalsIgnoreCase(kit.getName()));
        } else {
            this.arena.getKits().add(kit.getName());
        }
        this.plugin.getArenaManager().save();
        this.build();
        player.openInventory(this.inventory);
    }
}

