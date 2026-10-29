package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.TrimMenu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.util.Items;
import com.meowduels.util.Trims;
import com.meowduels.util.Text;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TrimKitMenu
extends Menu {
    private final MeowDuels plugin;
    private static final String MENU = "trimkit";
    private static final int ROWS = 6;

    public TrimKitMenu(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 6);
        this.createRaw(n, guiConfigManager.title(MENU, "&d&l\u1d1b\u0280\u026a\u1d0d\ua731 &7- \u1d18\u026a\u1d04\u1d0b \u1d00 \u1d0b\u026a\u1d1b"));
        guiConfigManager.frame(MENU, this.inventory, n);
        int n2 = n * 9;
        int n3 = 0;
        for (Kit kit : this.plugin.getKitManager().all()) {
            if (n3 >= n2) break;
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("kit", kit.getName());
            Items items = Items.of(guiConfigManager.material(MENU, "kit", kit.getIcon()), guiConfigManager.amount(MENU, "kit", 1)).tag(this.plugin.keyKit(), kit.getName());
            String string = guiConfigManager.name(MENU, "kit", hashMap);
            if (string != null) {
                items.rawName(string);
            } else if (kit.getDisplayName() != null && !kit.getDisplayName().isEmpty()) {
                items.miniName(kit.withIcon(kit.getDisplayName()));
            } else {
                items.rawName("&e" + Text.smallCaps(kit.getName()));
            }
            items.rawLore(guiConfigManager.lore(MENU, "kit", hashMap, "&7\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d07\u1d05\u026a\u1d1b \u1d1b\u029c\u026a\ua731 \u1d0b\u026a\u1d1b'\ua731 \u1d00\u0280\u1d0d\u1d0f\u0280 \u1d1b\u0280\u026a\u1d0d\ua731.").toArray(new String[0]));
            this.inventory.setItem(n3++, items.build());
        }
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        if (!Trims.canEdit(player)) {
            player.sendMessage(this.plugin.messages().get("general.no-permission", new String[0]));
            return;
        }
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyKit());
        if (string != null && this.plugin.getKitManager().exists(string)) {
            new TrimMenu(this.plugin, string, player.getUniqueId()).open(player);
        }
    }
}

