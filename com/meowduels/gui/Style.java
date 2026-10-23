package com.meowduels.gui;

import com.meowduels.util.Items;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class Style {
    public static String VALUE = "<#E6E8EB>";
    public static String LABEL = "<white>";
    public static String MUTED = "<#6B7079>";
    public static String GOOD = "<#7CFF6B>";
    public static String BAD = "<#FF6B6B>";
    public static String WARN = "<#FFD65C>";
    public static String SEP = "<dark_gray>\u203a ";
    public static String HINT = "<dark_gray>\u25b8 <#8E959D>";
    public static String TICK = "\u2714 ";
    public static String CROSS = "\u2716 ";
    public static String GO_A = "#7CFF6B";
    public static String GO_B = "#1FA32F";
    public static String NO_A = "#FF8A8A";
    public static String NO_B = "#C0392B";
    public static String ACCENT_A = "#FFF1B3";
    public static String ACCENT_B = "#E6A700";
    public static String FILLER = "GRAY_STAINED_GLASS_PANE";
    public static String FILLER_NAME = " ";
    public static final int PER_ROW = 7;

    public static String accent(String string) {
        return Style.gradient(ACCENT_A, ACCENT_B, string);
    }

    public static void load(ConfigurationSection configurationSection) {
        if (configurationSection == null) {
            return;
        }
        VALUE = configurationSection.getString("value", VALUE);
        LABEL = configurationSection.getString("label", LABEL);
        MUTED = configurationSection.getString("muted", MUTED);
        GOOD = configurationSection.getString("good", GOOD);
        BAD = configurationSection.getString("bad", BAD);
        WARN = configurationSection.getString("warn", WARN);
        SEP = configurationSection.getString("separator", SEP);
        HINT = configurationSection.getString("hint", HINT);
        TICK = configurationSection.getString("tick", TICK);
        CROSS = configurationSection.getString("cross", CROSS);
        GO_A = configurationSection.getString("confirm-from", GO_A);
        GO_B = configurationSection.getString("confirm-to", GO_B);
        NO_A = configurationSection.getString("cancel-from", NO_A);
        NO_B = configurationSection.getString("cancel-to", NO_B);
        ACCENT_A = configurationSection.getString("accent-from", ACCENT_A);
        ACCENT_B = configurationSection.getString("accent-to", ACCENT_B);
        FILLER = configurationSection.getString("filler", FILLER);
        FILLER_NAME = configurationSection.getString("filler-name", FILLER_NAME);
    }

    public static ItemStack filler(String string, String string2) {
        Material material;
        Material material2 = material = string == null ? null : Material.matchMaterial((String)string);
        if (material == null || !material.isItem()) {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }
        return Items.of(material).rawName(string2 == null ? " " : string2).build();
    }

    private Style() {
    }

    public static String gradient(String string, String string2, String string3) {
        return "<gradient:" + string + ":" + string2 + ">" + string3 + "</gradient>";
    }

    public static String title(String string, String string2, String string3) {
        return "<dark_gray>\u258f " + Style.gradient(string, string2, string3);
    }

    public static String title(String string, String string2, String string3, String string4) {
        return Style.title(string, string2, string3) + " " + SEP + VALUE + string4;
    }

    public static void frame(Inventory inventory, int n) {
        Style.frame(inventory, n, Style.filler(FILLER, FILLER_NAME));
    }

    public static void frame(Inventory inventory, int n, ItemStack itemStack) {
        int n2 = n * 9;
        for (int i = 0; i < n2; ++i) {
            inventory.setItem(i, itemStack);
        }
    }

    public static int backSlot(int n) {
        return (n - 1) * 9;
    }

    public static ItemStack confirm(NamespacedKey namespacedKey, String string, boolean bl, String string2, String string3, String ... stringArray) {
        return Items.of(bl ? Material.LIME_DYE : Material.GRAY_DYE).rawName((String)(bl ? Style.gradient(GO_A, GO_B, TICK + string2) : MUTED + CROSS + string3)).rawLore(stringArray).glow(bl).hideTooltip().tag(namespacedKey, string).build();
    }

    public static ItemStack cancel(NamespacedKey namespacedKey, String string, String string2, String ... stringArray) {
        return Items.of(Material.RED_DYE).rawName(Style.gradient(NO_A, NO_B, CROSS + string2)).rawLore(stringArray).hideTooltip().tag(namespacedKey, string).build();
    }

    public static ItemStack back(NamespacedKey namespacedKey, String string) {
        return Items.of(Material.ARROW).rawName(VALUE + "\u0299\u1d00\u1d04\u1d0b").rawLore("", HINT + "\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u0262\u1d0f \u0299\u1d00\u1d04\u1d0b").hideTooltip().tag(namespacedKey, string).build();
    }

    public static int gridSlot(int n) {
        return (n / 7 + 1) * 9 + 1 + n % 7;
    }

    public static int gridRows(int n, int n2) {
        int n3 = Math.max(1, Math.min(n2, (n + 7 - 1) / 7));
        return Math.min(6, n3 + 2);
    }

    public static String state(boolean bl, String string, String string2) {
        return bl ? GOOD + TICK + string : BAD + CROSS + string2;
    }
}

