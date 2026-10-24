package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.KitItemsEditMenu;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.util.Items;
import com.meowduels.util.KitCatalog;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class KitEditorMenu
extends Menu {
    private static final int[] SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private final MeowDuels plugin;
    private final boolean personal;
    private static final String MENU = "kiteditor";
    private static final int ROWS = 6;

    public KitEditorMenu(MeowDuels meowDuels) {
        this(meowDuels, false);
    }

    public KitEditorMenu(MeowDuels meowDuels, boolean bl) {
        this.personal = bl;
        this.plugin = meowDuels;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 6);
        this.createRaw(n, this.personal ? guiConfigManager.titleOf(MENU, "personal", "&d&l\u028f\u1d0f\u1d1c\u0280 \u1d0b\u026a\u1d1b\ua731 &7- \u1d18\u026a\u1d04\u1d0b \u1d00 \u0262\u1d00\u1d0d\u1d07\u1d0d\u1d0f\u1d05\u1d07", null) : guiConfigManager.titleOf(MENU, "global", "&d&l\u1d0b\u026a\u1d1b \u1d07\u1d05\u026a\u1d1b\u1d0f\u0280 &7- \u1d18\u026a\u1d04\u1d0b \u1d00 \u0262\u1d00\u1d0d\u1d07\u1d0d\u1d0f\u1d05\u1d07", null));
        guiConfigManager.frame(MENU, this.inventory, n);
        int n2 = n * 9;
        List<Kit> list = KitCatalog.visibleKits(this.plugin);
        if (list.isEmpty()) {
            this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "empty", 22, n)), guiConfigManager.item(MENU, "empty", Material.BARRIER, "&c\u0274\u1d0f \u1d0b\u026a\u1d1b\ua731 \u028f\u1d07\u1d1b", "&7\u1d04\u0280\u1d07\u1d00\u1d1b\u1d07 \u1d0f\u0274\u1d07 \u1d21\u026a\u1d1b\u029c &e/\u1d0b\u026a\u1d1b\u1d04\u0280\u1d07\u1d00\u1d1b\u1d07 <\u0274\u1d00\u1d0d\u1d07>&7.").build());
            return;
        }
        for (int i = 0; i < SLOTS.length && i < list.size() && SLOTS[i] < n2; ++i) {
            this.inventory.setItem(SLOTS[i], this.icon(guiConfigManager, list.get(i)));
        }
    }

    private ItemStack icon(GuiConfigManager guiConfigManager, Kit kit) {
        String string;
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("kit", kit.getName());
        Items items = this.personal ? guiConfigManager.item(MENU, "kit-personal", hashMap, guiConfigManager.material(MENU, "kit-personal", kit.getIcon()), "&d{kit}", "", "&7\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d00\u0280\u0280\u1d00\u0274\u0262\u1d07 \u1d1b\u029c\u026a\ua731 \u1d0b\u026a\u1d1b", "&7\u1d1b\u029c\u1d07 \u1d21\u1d00\u028f \u028f\u1d0f\u1d1c \u1d21\u1d00\u0274\u1d1b \u026a\u1d1b.") : guiConfigManager.item(MENU, "kit-global", hashMap, guiConfigManager.material(MENU, "kit-global", kit.getIcon()), "&d{kit}", "", "&7\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d07\u1d05\u026a\u1d1b \u1d1b\u029c\u026a\ua731 \u0262\u1d00\u1d0d\u1d07\u1d0d\u1d0f\u1d05\u1d07'\ua731 \u026a\u1d1b\u1d07\u1d0d\ua731,", "&7\u1d00\u0280\u1d0d\u1d0f\u0280 \u1d00\u0274\u1d05 \u1d0f\ua730\ua730\u029c\u1d00\u0274\u1d05 \u026a\u1d1b\u1d07\u1d0d.", "&c\u1d04\u029c\u1d00\u0274\u0262\u1d07\ua731 \u1d00\u1d18\u1d18\u029f\u028f \u1d1b\u1d0f \u1d07\u1d20\u1d07\u0280\u028f\u1d0f\u0274\u1d07.");
        items.hideTooltip().tag(this.plugin.keyKit(), kit.getName());
        String string2 = string = this.personal ? guiConfigManager.name(MENU, "kit-personal", hashMap) : guiConfigManager.name(MENU, "kit-global", hashMap);
        if (string == null && kit.getDisplayName() != null && !kit.getDisplayName().isEmpty()) {
            items.miniName(kit.withIcon(kit.getDisplayName()));
        }
        return items.build();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyKit());
        if (string == null) {
            return;
        }
        Kit kit = this.plugin.getKitManager().get(string);
        if (kit != null) {
            new KitItemsEditMenu(this.plugin, kit, this.personal, player.getUniqueId()).open(player);
        }
    }
}

