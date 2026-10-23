package com.meowduels.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.util.Arrays;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class GoldenHead {
    public static final String KEY_NAME = "golden_head";
    private static final String KEY_VALUE = "1";
    public static final String DISPLAY_NAME = "Golden Head";

    private GoldenHead() {
    }

    public static NamespacedKey key(Plugin plugin) {
        return new NamespacedKey(plugin, KEY_NAME);
    }

    public static ItemStack create(Plugin plugin, int n) {
        return GoldenHead.create(plugin, n, "");
    }

    public static ItemStack create(Plugin plugin, int n, String string) {
        int n2 = Math.max(1, Math.min(64, n));
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, n2);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(String.valueOf(ChatColor.GOLD) + DISPLAY_NAME);
            itemMeta.setLore(Arrays.asList(String.valueOf(ChatColor.AQUA) + "A powerful competitive UHC healing item."));
            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
            persistentDataContainer.set(GoldenHead.key(plugin), PersistentDataType.STRING, KEY_VALUE);
            GoldenHead.applyTexture(itemMeta, string);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    private static void applyTexture(ItemMeta itemMeta, String string) {
        if (string == null || string.trim().isEmpty() || !(itemMeta instanceof SkullMeta)) {
            return;
        }
        try {
            PlayerProfile playerProfile = Bukkit.createProfile((UUID)UUID.randomUUID());
            playerProfile.setProperty(new ProfileProperty("textures", string.trim()));
            ((SkullMeta)itemMeta).setPlayerProfile(playerProfile);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static boolean isGoldenHead(Plugin plugin, ItemStack itemStack) {
        String string;
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }
        Material material = itemStack.getType();
        if (material != Material.PLAYER_HEAD && material != Material.GOLDEN_APPLE) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }
        String string2 = (String)itemMeta.getPersistentDataContainer().get(GoldenHead.key(plugin), PersistentDataType.STRING);
        if (KEY_VALUE.equals(string2)) {
            return true;
        }
        return itemMeta.hasDisplayName() && (string = ChatColor.stripColor((String)itemMeta.getDisplayName())) != null && string.trim().equalsIgnoreCase(DISPLAY_NAME);
    }
}

