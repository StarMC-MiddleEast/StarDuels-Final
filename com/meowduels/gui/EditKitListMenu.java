package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.EditKitEffectsMenu;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.util.Items;
import com.meowduels.util.KitCatalog;
import com.meowduels.util.Text;
import java.util.HashMap;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EditKitListMenu
extends Menu {
    private static final int[] SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private final MeowDuels plugin;
    private static final String MENU = "editkitlist";
    private static final int ROWS = 6;

    public EditKitListMenu(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 6);
        this.createRaw(n, guiConfigManager.title(MENU, "&7&l\u1d07\u1d05\u026a\u1d1b \u1d0b\u026a\u1d1b"));
        guiConfigManager.frame(MENU, this.inventory, n);
        List<Kit> list = KitCatalog.visibleKits(this.plugin);
        int n2 = n * 9;
        for (int i = 0; i < SLOTS.length && i < list.size() && SLOTS[i] < n2; ++i) {
            this.inventory.setItem(SLOTS[i], this.icon(guiConfigManager, list.get(i)));
        }
    }

    private ItemStack icon(GuiConfigManager guiConfigManager, Kit kit) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("kit", kit.getName());
        hashMap.put("effects", String.valueOf(kit.getStartEffects().size()));
        Items items = Items.of(guiConfigManager.material(MENU, "kit", kit.getIcon()), guiConfigManager.amount(MENU, "kit", 1)).rawLore(guiConfigManager.lore(MENU, "kit", hashMap, "", "&7\ua731\u1d1b\u1d00\u0280\u1d1b \u1d07\ua730\ua730\u1d07\u1d04\u1d1b\ua731: &f{effects}", "&a\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d07\u1d05\u026a\u1d1b.").toArray(new String[0])).hideTooltip().tag(this.plugin.keyKit(), kit.getName());
        String string = guiConfigManager.name(MENU, "kit", hashMap);
        if (string != null) {
            items.rawName(string);
        } else if (kit.getDisplayName() != null && !kit.getDisplayName().isEmpty()) {
            items.miniName(kit.withIcon(kit.getDisplayName()));
        } else {
            items.rawName("&e" + Text.smallCaps(kit.getName()));
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
            new EditKitEffectsMenu(this.plugin, kit).open(player);
        }
    }
}

