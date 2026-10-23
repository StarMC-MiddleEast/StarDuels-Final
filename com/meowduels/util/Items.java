package com.meowduels.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.meowduels.util.Colors;
import com.meowduels.util.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

public final class Items {
    private final ItemStack item;
    private final ItemMeta meta;

    private Items(Material material, int n) {
        this.item = new ItemStack(material, n);
        this.meta = this.item.getItemMeta();
    }

    public static Items of(Material material) {
        return new Items(material, 1);
    }

    public static Items of(Material material, int n) {
        return new Items(material, n);
    }

    public Items name(String string) {
        if (this.meta != null) {
            this.meta.setDisplayName(Text.color(string));
        }
        return this;
    }

    public Items lore(String ... stringArray) {
        if (this.meta != null) {
            ArrayList<String> arrayList = new ArrayList<String>();
            for (String string : stringArray) {
                arrayList.add(Text.color(string));
            }
            this.meta.setLore(arrayList);
        }
        return this;
    }

    public Items lore(List<String> list) {
        if (this.meta != null) {
            ArrayList<String> arrayList = new ArrayList<String>();
            for (String string : list) {
                arrayList.add(Text.color(string));
            }
            this.meta.setLore(arrayList);
        }
        return this;
    }

    public Items rawName(String string) {
        if (this.meta != null) {
            this.meta.setDisplayName(Colors.toSection(string));
        }
        return this;
    }

    public Items rawLore(String ... stringArray) {
        if (this.meta != null) {
            ArrayList<String> arrayList = new ArrayList<String>();
            for (String string : stringArray) {
                arrayList.add(Colors.toSection(string));
            }
            this.meta.setLore(arrayList);
        }
        return this;
    }

    public Items skull(OfflinePlayer offlinePlayer) {
        if (offlinePlayer != null && this.meta instanceof SkullMeta) {
            try {
                ((SkullMeta)this.meta).setOwningPlayer(offlinePlayer);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return this;
    }

    public Items texture(String string) {
        if (this.meta instanceof SkullMeta && string != null && !string.isBlank()) {
            try {
                PlayerProfile playerProfile = Bukkit.createProfile((UUID)UUID.randomUUID());
                playerProfile.setProperty(new ProfileProperty("textures", string.trim()));
                ((SkullMeta)this.meta).setPlayerProfile(playerProfile);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return this;
    }

    public Items glow(boolean bl) {
        if (this.meta != null && bl) {
            this.meta.setEnchantmentGlintOverride(Boolean.valueOf(true));
        }
        return this;
    }

    public Items glint(Boolean bl) {
        if (this.meta != null && bl != null) {
            this.meta.setEnchantmentGlintOverride(bl);
        }
        return this;
    }

    public Items hideTooltip() {
        if (this.meta != null) {
            this.meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DYE});
        }
        return this;
    }

    public Items miniName(String string) {
        if (this.meta != null && string != null) {
            this.meta.displayName(MiniMessage.miniMessage().deserialize(string).decoration(TextDecoration.ITALIC, false));
        }
        return this;
    }

    public Items tag(NamespacedKey namespacedKey, String string) {
        if (this.meta != null) {
            this.meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, string);
        }
        return this;
    }

    public ItemStack build() {
        if (this.meta != null) {
            this.item.setItemMeta(this.meta);
        }
        return this.item;
    }

    public static String readTag(ItemStack itemStack, NamespacedKey namespacedKey) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        return (String)itemMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
    }

    public static List<String> lines(String ... stringArray) {
        return new ArrayList<String>(Arrays.asList(stringArray));
    }
}

