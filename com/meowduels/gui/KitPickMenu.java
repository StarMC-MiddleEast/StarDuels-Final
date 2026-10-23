package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.DuelConfirmMenu;
import com.meowduels.gui.Menu;
import com.meowduels.managers.CategoryManager;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.util.Items;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class KitPickMenu
extends Menu {
    private static final String MENU = "kitpick";
    private static final int ROWS = 4;
    private static final int ARROW_SLOT = 35;
    private static final int[] KIT_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private final MeowDuels plugin;
    private final DuelConfirmMenu confirm;
    private int categoryIndex = 0;

    public KitPickMenu(MeowDuels meowDuels, DuelConfirmMenu duelConfirmMenu) {
        this.plugin = meowDuels;
        this.confirm = duelConfirmMenu;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void build() {
        List<String> list;
        Object object;
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 4);
        int n2 = n * 9;
        List<CategoryManager.Category> list2 = this.plugin.getCategoryManager().all();
        boolean bl = false;
        if (list2.isEmpty()) {
            object = guiConfigManager.titleOf(MENU, "plain", "<bold><gradient:#FF465E:#FFAA55>\u1d05\u1d1c\u1d07\u029f \u0280\u1d07Q\u1d1c\u1d07\ua731\u1d1b</gradient></bold>", null);
            if (((String)object).startsWith("&7&l")) {
                object = "<bold><gradient:#FF465E:#FFAA55>\u1d05\u1d1c\u1d07\u029f \u0280\u1d07Q\u1d1c\u1d07\ua731\u1d1b</gradient></bold>";
            }
            this.createRaw(n, (String)object);
            list = new ArrayList<String>();
            for (Kit object2 : this.plugin.getKitManager().all()) {
                list.add(object2.getName());
            }
        } else {
            if (this.categoryIndex >= list2.size()) {
                this.categoryIndex = 0;
            }
            object = list2.get(this.categoryIndex);
            bl = list2.size() > 1;
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("category", ((CategoryManager.Category)object).getHeader());
            String string = guiConfigManager.titleOf(MENU, "category", "<bold><gradient:#FF465E:#FFAA55>\u1d05\u1d1c\u1d07\u029f \u0280\u1d07Q\u1d1c\u1d07\ua731\u1d1b</gradient></bold> <dark_gray>\u203a <bold><gradient:#FF465E:#FFAA55>{category}</gradient></bold>", (Map<String, String>)hashMap);
            if (string.startsWith("&7&l")) {
                string = "<bold><gradient:#FF465E:#FFAA55>\u1d05\u1d1c\u1d07\u029f \u0280\u1d07Q\u1d1c\u1d07\ua731\u1d1b</gradient></bold> <dark_gray>\u203a <bold><gradient:#FF465E:#FFAA55>" + ((CategoryManager.Category)object).getHeader() + "</gradient></bold>";
            }
            this.createRaw(n, string);
            list = this.plugin.getCategoryManager().kitsFor((CategoryManager.Category)object);
        }
        guiConfigManager.frame(MENU, this.inventory, n);
        for (int i = 0; i < KIT_SLOTS.length && i < list.size() && KIT_SLOTS[i] < n2; ++i) {
            object = this.kitIcon(guiConfigManager, (String)list.get(i));
            if (object == null) continue;
            this.inventory.setItem(KIT_SLOTS[i], (ItemStack)object);
        }
        if (bl) {
            this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "next-category", 35, n)), this.arrowItem(guiConfigManager));
        }
    }

    private ItemStack kitIcon(GuiConfigManager guiConfigManager, String string) {
        Kit kit = this.plugin.getKitManager().get(string);
        if (kit == null) {
            return null;
        }
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("kit", kit.getName());
        String[] stringArray = KitPickMenu.kitGradient(kit.getName());
        String string2 = "<bold><gradient:" + stringArray[0] + ":" + stringArray[1] + ">" + kit.getName().toUpperCase(Locale.ROOT) + "</gradient></bold>";
        Items items = guiConfigManager.item(MENU, "kit", hashMap, guiConfigManager.material(MENU, "kit", kit.getIcon()), string2, new String[0]).hideTooltip().tag(this.plugin.keyKit(), kit.getName());
        String string3 = guiConfigManager.name(MENU, "kit", hashMap);
        if (string3 != null && string3.trim().equalsIgnoreCase("&e" + kit.getName())) {
            items.miniName(string2);
        } else if (string3 == null && kit.getDisplayName() != null && !kit.getDisplayName().isEmpty()) {
            items.miniName(string2);
        }
        return items.build();
    }

    private static String[] kitGradient(String string) {
        String string2 = string.toLowerCase(Locale.ROOT);
        if (string2.contains("sword")) {
            return new String[]{"#62F5F5", "#15A8E8"};
        }
        if (string2.contains("mace")) {
            return new String[]{"#FFF36A", "#F39A22"};
        }
        if (string2.contains("crystal")) {
            return new String[]{"#FF82E8", "#9D55FF"};
        }
        if (string2.contains("axe")) {
            return new String[]{"#FF9A62", "#E83D4F"};
        }
        if (string2.contains("bow")) {
            return new String[]{"#A4FF73", "#13C887"};
        }
        return new String[]{"#72D8FF", "#B35CFF"};
    }

    private ItemStack arrowItem(GuiConfigManager guiConfigManager) {
        return guiConfigManager.item(MENU, "next-category", Material.ARROW, "&e\u0274\u1d07x\u1d1b \u1d04\u1d00\u1d1b\u1d07\u0262\u1d0f\u0280\u028f", "&f\u2192 \u1d00\u0274\u1d0f\u1d1b\u029c\u1d07\u0280 \u1d04\u1d00\u1d1b\u1d07\u0262\u1d0f\u0280\u028f").tag(this.plugin.keyButton(), "next-cat").build();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
        if ("next-cat".equals(Items.readTag(itemStack, this.plugin.keyButton()))) {
            int n = this.plugin.getCategoryManager().all().size();
            if (n > 0) {
                this.categoryIndex = (this.categoryIndex + 1) % n;
                this.build();
                player.openInventory(this.inventory);
            }
            return;
        }
        String string = Items.readTag(itemStack, this.plugin.keyKit());
        if (string != null && this.plugin.getKitManager().exists(string)) {
            this.confirm.setSelectedKit(string);
            this.confirm.reopen(player);
        }
    }
}

