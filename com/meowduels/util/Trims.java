package com.meowduels.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public final class Trims {
    public static final String[] PATTERN_KEYS = new String[]{"sentry", "dune", "coast", "wild", "ward", "eye", "vex", "tide", "snout", "rib", "spire", "wayfinder", "shaper", "silence", "raiser", "host", "flow", "bolt"};
    public static final String[] MATERIAL_KEYS = new String[]{"quartz", "iron", "gold", "copper", "netherite", "redstone", "emerald", "diamond", "lapis", "amethyst", "resin"};

    private Trims() {
    }

    public static TrimPattern pattern(String string) {
        try {
            return (TrimPattern)Registry.TRIM_PATTERN.get(NamespacedKey.minecraft((String)string));
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    public static TrimMaterial material(String string) {
        try {
            return (TrimMaterial)Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft((String)string));
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    public static boolean available() {
        return Trims.pattern("sentry") != null && Trims.material("quartz") != null;
    }

    public static boolean apply(ItemStack itemStack, String string, String string2) {
        if (itemStack == null) {
            return false;
        }
        try {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (!(itemMeta instanceof ArmorMeta)) {
                return false;
            }
            TrimPattern trimPattern = Trims.pattern(string);
            TrimMaterial trimMaterial = Trims.material(string2);
            if (trimPattern == null || trimMaterial == null) {
                return false;
            }
            ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(trimMaterial, trimPattern));
            itemStack.setItemMeta(itemMeta);
            return true;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public static boolean hasTrim(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        try {
            ItemMeta itemMeta = itemStack.getItemMeta();
            return itemMeta instanceof ArmorMeta && ((ArmorMeta)itemMeta).hasTrim();
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public static void clear(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }
        try {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta instanceof ArmorMeta) {
                ((ArmorMeta)itemMeta).setTrim(null);
                itemStack.setItemMeta(itemMeta);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static String pretty(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}

