package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.QueuePickMenu;
import com.meowduels.managers.CategoryManager;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.util.Items;
import com.meowduels.util.Text;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class QueueCategoriesMenu
extends Menu {
    private static final String MENU = "queuecategories";
    private static final int[] SLOTS = new int[]{10, 12, 14, 16};
    private static final String[] ORDER = new String[]{"pvptiers", "mctiers", "subtiers", "startiers"};
    private static final String TOPAZ_GEM = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM3NjY3ZGJlMjZmNjA3YjY5OWM5MzAzZTc0Zjg5NTEwZGFjNzU0M2UwYzkyY2VmOTliMzM0NTQ5YzU3YzQ4YSJ9fX0=";
    private static final String RED_DUST_GEM = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGEzOTNmYzA0MTFiM2RkNjM3MTUzMTFiNTUxZGVkYzk5ZmIxZmJhNzk0ODY3ODFmODJhZjVmMWViNjdlMjA5YSJ9fX0=";
    private static final String BLUE_DUST_GEM = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY5NTQyZDg3YjRmNzJhYTJhYjVjZTRjYzk0ZGM5MWRiYzRhYTMyMzJlM2RlY2ZhN2Q4YTcwODIyNDRmZWQ4ZSJ9fX0=";
    private static final String GREEN_DUST_GEM = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ4OWY3Y2UzNWM0ZTI4MjE4NzkxMGUxMTI5NjQ0MDc5MjgwMDIwNTQ4YTIwYTExYzI5MTkxN2MyYjAyMzNiOSJ9fX0=";
    private final MeowDuels plugin;

    public QueueCategoriesMenu(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 3);
        this.createRaw(n, guiConfigManager.title(MENU, "<dark_gray>\u258f <gradient:#FF2525:#FFB3B3:#FF2525><bold>\u01eb\u1d1c\u1d07\u1d1c\u1d07</bold></gradient> <dark_gray>\u00b7 <white><bold>\u1d04\u029c\u1d0f\u1d0f\ua731\u1d07 \u1d00 \u1d04\u1d00\u1d1b\u1d07\u0262\u1d0f\u0280\u028f</bold>"));
        guiConfigManager.frame(MENU, this.inventory, n);
        ArrayList<CategoryManager.Category> arrayList = new ArrayList<CategoryManager.Category>(this.plugin.getQueueCategoryManager().all());
        arrayList.sort(Comparator.comparingInt(category -> {
            for (int i = 0; i < ORDER.length; ++i) {
                if (!ORDER[i].equalsIgnoreCase(category.getId())) continue;
                return i;
            }
            return ORDER.length;
        }));
        for (int i = 0; i < arrayList.size() && i < SLOTS.length; ++i) {
            CategoryManager.Category category2 = (CategoryManager.Category)arrayList.get(i);
            String string = Text.smallCaps(category2.getId());
            String string2 = category2.getId().toLowerCase(Locale.ROOT);
            String string3 = string2.equals("pvptiers") ? "\u1d18\u1d20\u1d18 \u1d1b\u026a\u1d07\u0280\ua731" : (string2.equals("mctiers") ? "\u1d0d\u1d04 \u1d1b\u026a\u1d07\u0280\ua731" : (string2.equals("subtiers") ? "\ua731\u1d1c\u0299 \u1d1b\u026a\u1d07\u0280\ua731" : (string = string2.equals("startiers") ? "\ua731\u1d1b\u1d00\u0280 \u1d1b\u026a\u1d07\u0280\ua731" : Text.smallCaps(category2.getId()))));
            String string4 = string2.equals("pvptiers") ? "<gradient:#FF2020:#FFB3B3:#FF3030>" : (string2.equals("mctiers") ? "<gradient:#54FF45:#D8FFB3:#18E85B>" : (string2.equals("subtiers") ? "<gradient:#27F5FF:#D2FFFF:#18C8FF>" : (string2.equals("startiers") ? "<gradient:#FFD91A:#FFFAC2:#FFB800>" : "<gradient:#FFF1B3:#FFFFFF:#E6A700>")));
            String string5 = string4 + "<bold>" + string + "</bold></gradient>";
            String string6 = string2.equals("startiers") ? TOPAZ_GEM : (string2.equals("pvptiers") ? RED_DUST_GEM : (string2.equals("subtiers") ? BLUE_DUST_GEM : (string2.equals("mctiers") ? GREEN_DUST_GEM : null)));
            Items items = Items.of(string6 == null ? Material.CHEST : Material.PLAYER_HEAD);
            if (string6 != null) {
                items.texture(string6);
            }
            this.inventory.setItem(SLOTS[i], items.miniName(string5).rawLore("", "<gray>\u24d8 Click to view the Kits</gray>").tag(this.plugin.keyButton(), "queue-category:" + category2.getId()).build());
        }
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (string == null || !string.startsWith("queue-category:")) {
            return;
        }
        String string2 = string.substring("queue-category:".length());
        if (this.plugin.getQueueCategoryManager().get(string2) != null) {
            new QueuePickMenu(this.plugin, string2).open(player);
        }
    }
}

