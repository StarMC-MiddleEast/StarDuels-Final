package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.QueueCategoriesMenu;
import com.meowduels.managers.CategoryManager;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.util.Items;
import com.meowduels.util.Text;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class QueuePickMenu
extends Menu {
    private static final Set<QueuePickMenu> OPEN = Collections.newSetFromMap(new ConcurrentHashMap());
    private static final int ARROW_SLOT = 35;
    private static final int[] KIT_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private final MeowDuels plugin;
    private Player viewer;
    private String categoryId;
    private int kitPage = 0;
    private static final String MENU = "queuepick";
    private static final int ROWS = 4;
    private static final int SLOT_BACK = 35;
    private static final int SLOT_PREV_PAGE = 27;
    private static final int SLOT_NEXT_PAGE = 28;

    public QueuePickMenu(MeowDuels meowDuels) {
        this(meowDuels, null);
    }

    public QueuePickMenu(MeowDuels meowDuels, String string) {
        this.plugin = meowDuels;
        this.categoryId = string;
    }

    public static void refreshAll() {
        for (QueuePickMenu queuePickMenu : OPEN) {
            if (queuePickMenu.inventory == null) continue;
            queuePickMenu.populate();
        }
    }

    @Override
    public void open(Player player) {
        this.viewer = player;
        this.build();
        player.openInventory(this.inventory);
        OPEN.add(this);
    }

    @Override
    public void onClose(Player player, InventoryCloseEvent inventoryCloseEvent) {
        OPEN.remove(this);
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = 4;
        List<CategoryManager.Category> list = this.plugin.getQueueCategoryManager().all();
        this.createRaw(n, "<gray>\u1d1c\u0274\u0280\u1d00\u0274\u1d0b\u1d07\u1d05 \u01eb\u1d1c\u1d07\u1d1c\u1d07</gray>");
        this.populate();
    }

    private void populate() {
        int n;
        List<String> list;
        if (this.inventory == null) {
            return;
        }
        List<CategoryManager.Category> list2 = this.plugin.getQueueCategoryManager().all();
        if (list2.isEmpty()) {
            list = new ArrayList();
            for (Kit object2 : this.plugin.getKitManager().all()) {
                list.add(object2.getName());
            }
        } else {
            list = this.plugin.getQueueCategoryManager().kitsFor(this.selectedCategory(list2));
        }
        if (this.kitPage >= (n = Math.max(1, (list.size() + KIT_SLOTS.length - 1) / KIT_SLOTS.length))) {
            this.kitPage = 0;
        }
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n2 = this.inventory.getSize() / 9;
        int n3 = this.inventory.getSize() - 1;
        guiConfigManager.frame(MENU, this.inventory, n2);
        for (int n4 : KIT_SLOTS) {
            if (n4 > n3) continue;
            this.inventory.setItem(n4, null);
        }
        if (27 <= n3) {
            this.inventory.setItem(27, null);
        }
        if (28 <= n3) {
            this.inventory.setItem(28, null);
        }
        int n5 = this.kitPage * KIT_SLOTS.length;
        for (int i = 0; i < KIT_SLOTS.length && n5 + i < list.size() && KIT_SLOTS[i] <= n3; ++i) {
            ItemStack itemStack = this.kitIcon(guiConfigManager, (String)list.get(n5 + i));
            if (itemStack == null) continue;
            this.inventory.setItem(KIT_SLOTS[i], itemStack);
        }
        if (n > 1) {
            if (this.kitPage > 0) {
                this.inventory.setItem(27, guiConfigManager.face(MENU, "prev-page", null, GuiConfigManager.Face.of(Material.ARROW, "<bold><gradient:#FFF1B3:#E6A700>\u1d18\u0280\u1d07\u1d20\u026a\u1d0f\u1d1c\ua731 \u1d18\u1d00\u0262\u1d07</gradient></bold>", "<white>\u279c \u1d18\u1d00\u0262\u1d07 " + Text.smallCaps(String.valueOf(this.kitPage)))).tag(this.plugin.keyButton(), "prev-page").build());
            }
            if (this.kitPage + 1 < n) {
                this.inventory.setItem(28, guiConfigManager.face(MENU, "next-page", null, GuiConfigManager.Face.of(Material.ARROW, "<bold><gradient:#FFF1B3:#E6A700>\u0274\u1d07x\u1d1b \u1d18\u1d00\u0262\u1d07</gradient></bold>", "<white>\u279c \u1d18\u1d00\u0262\u1d07 " + Text.smallCaps(String.valueOf(this.kitPage + 2)))).tag(this.plugin.keyButton(), "next-page").build());
            }
        }
        this.inventory.setItem(Math.min(n3, 35), guiConfigManager.face(MENU, "back-categories", null, GuiConfigManager.Face.of(Material.ARROW, "<bold><gradient:#FFF1B3:#E6A700>\u1d04\u1d00\u1d1b\u1d07\u0262\u1d0f\u0280\u026a\u1d07\ua731</gradient></bold>", "<white>\u2190 \u1d04\u029c\u1d0f\u1d0f\ua731\u1d07 \u1d00 \u1d04\u1d00\u1d1b\u1d07\u0262\u1d0f\u0280\u028f")).tag(this.plugin.keyButton(), "back-categories").build());
    }

    private CategoryManager.Category selectedCategory(List<CategoryManager.Category> list) {
        if (list.isEmpty()) {
            return null;
        }
        if (this.categoryId != null) {
            for (CategoryManager.Category category : list) {
                if (!category.getId().equalsIgnoreCase(this.categoryId)) continue;
                return category;
            }
        }
        this.categoryId = list.get(0).getId();
        return list.get(0);
    }

    private ItemStack kitIcon(GuiConfigManager guiConfigManager, String string) {
        Kit kit = this.plugin.getKitManager().get(string);
        if (kit == null) {
            return null;
        }
        int n = this.plugin.getQueueManager().dueling(string);
        int n2 = this.plugin.getQueueManager().queued(string);
        HashMap<String, String> hashMap = new HashMap<String, String>();
        String string2 = kit.getName().replaceAll("([a-z])([A-Z])", "$1 $2");
        String[] stringArray = this.kitGradient(kit);
        String string3 = this.kitLogo(kit);
        hashMap.put("kit", "<gradient:" + stringArray[0] + ":" + stringArray[1] + ":" + stringArray[2] + ">" + string3 + " <bold>" + Text.smallCaps(string2) + "</bold></gradient>");
        hashMap.put("queued", String.valueOf(n2));
        hashMap.put("fights", String.valueOf(n));
        int n3 = Math.max(1, Math.min(64, n));
        boolean bl = this.viewer != null && this.plugin.getQueueManager().isQueuedFor(this.viewer.getUniqueId(), string);
        Items items = guiConfigManager.toggle(MENU, "kit", bl, hashMap, GuiConfigManager.Face.of(guiConfigManager.material(MENU, "kit", kit.getIcon()), "{kit}", "<color:#" + stringArray[0] + ">\u2726</color> <white>\u1d05\u1d1c\u1d07\u029f\u026a\u0274\u0262 <#" + stringArray[1] + ">{fights}</color>", "<color:#" + stringArray[0] + ">\u2726</color> <white>\u026a\u0274 \u01eb\u1d1c\u1d07\u1d1c\u1d07 <#" + stringArray[1] + ">{queued}</color>", "", "<color:#" + stringArray[0] + ">\u279c <underlined>\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u01eb\u1d1c\u1d07\u1d1c\u1d07</underlined></color>").amount(n3), GuiConfigManager.Face.of(guiConfigManager.material(MENU, "kit", kit.getIcon()), "{kit}", "<color:#" + stringArray[0] + ">\u2726</color> <white>\u1d05\u1d1c\u1d07\u029f\u026a\u0274\u0262 <#" + stringArray[1] + ">{fights}</color>", "<color:#" + stringArray[0] + ">\u2726</color> <white>\u026a\u0274 \u01eb\u1d1c\u1d07\u1d1c\u1d07 <#" + stringArray[1] + ">{queued}</color>", "", "<color:#" + stringArray[0] + ">\u279c <underlined>\u029f\u1d07\u1d00\u1d20\u1d07 \u01eb\u1d1c\u1d07\u1d1c\u1d07</underlined></color>").amount(n3)).tag(this.plugin.keyKit(), kit.getName()).glint(Boolean.FALSE);
        items.miniName((String)hashMap.get("kit"));
        items.rawLore("<color:#" + stringArray[0] + ">\u2726</color> <white>\u1d05\u1d1c\u1d07\u029f\u026a\u0274\u0262 <#" + stringArray[1] + ">" + n + "</color>", "<color:#" + stringArray[0] + ">\u2726</color> <white>\u026a\u0274 \u01eb\u1d1c\u1d07\u1d1c\u1d07 <#" + stringArray[1] + ">" + n2 + "</color>", "", bl ? "<color:#" + stringArray[0] + ">\u279c <underlined>\u029f\u1d07\u1d00\u1d20\u1d07 \u01eb\u1d1c\u1d07\u1d1c\u1d07</underlined></color>" : "<color:#" + stringArray[0] + ">\u279c <underlined>\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u01eb\u1d1c\u1d07\u1d1c\u1d07</underlined></color>");
        return items.build();
    }

    private String kitLogo(Kit kit) {
        Material material = kit.getIcon();
        String string = "\u2726";
        if (material != null && (material.name().equals("BOW") || material.name().equals("CROSSBOW"))) {
            string = "\u27b6";
        } else if (material != null && material.name().equals("TRIDENT")) {
            string = "\u2692";
        } else if (material != null && material.name().endsWith("_SWORD")) {
            string = "\u2694";
        } else if (material != null && material.name().endsWith("_AXE")) {
            string = "\u2692";
        } else if (material != null && material.name().contains("SHIELD")) {
            string = "\u2b1f";
        } else if (material != null && material.name().contains("POTION")) {
            string = "\u25c9";
        } else if (material != null && material.name().contains("PEARL")) {
            string = "\u25c6";
        }
        String string2 = material == null ? "minecraft:iron_sword" : "minecraft:" + material.name().toLowerCase(Locale.ROOT);
        return "<hover:show_item:'" + string2 + "'>" + string + "</hover>";
    }

    private String[] kitGradient(Kit kit) {
        Material material = kit.getIcon();
        if (material != null && material.name().endsWith("_SWORD")) {
            return new String[]{"#00DFFF", "#B7FFFF", "#00A6D6"};
        }
        if (material != null && (material.name().endsWith("_AXE") || material.name().contains("GOLD"))) {
            return new String[]{"#FF9B35", "#FFF0A8", "#FFB000"};
        }
        String[][] stringArrayArray = new String[][]{{"#FF6B6B", "#FFD0A6", "#E63946"}, {"#8B5CF6", "#E9D5FF", "#A855F7"}, {"#20C997", "#B7FFE6", "#00A878"}, {"#3B82F6", "#C7E5FF", "#2563EB"}, {"#F472B6", "#FFE0F0", "#DB2777"}, {"#FF8C42", "#FFE0B2", "#E85D04"}, {"#A3E635", "#F0FFC2", "#65A30D"}, {"#22D3EE", "#C8FBFF", "#0891B2"}};
        int n = Math.floorMod(kit.getName().toLowerCase(Locale.ROOT).hashCode(), stringArrayArray.length);
        return stringArrayArray[n];
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
        if ("back-categories".equals(Items.readTag(itemStack, this.plugin.keyButton()))) {
            new QueueCategoriesMenu(this.plugin).open(player);
            return;
        }
        String string = Items.readTag(itemStack, this.plugin.keyButton());
        if ("prev-page".equals(string) || "next-page".equals(string)) {
            this.kitPage += "next-page".equals(string) ? 1 : -1;
            this.build();
            player.openInventory(this.inventory);
            return;
        }
        String string2 = Items.readTag(itemStack, this.plugin.keyKit());
        if (string2 != null && this.plugin.getKitManager().exists(string2)) {
            this.plugin.getQueueManager().toggle(player, string2);
            if (this.plugin.getDuelManager().isInDuel(player.getUniqueId())) {
                player.closeInventory();
            } else {
                this.populate();
            }
        }
    }
}

