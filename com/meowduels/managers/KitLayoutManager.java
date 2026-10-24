package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.model.Kit;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class KitLayoutManager {
    private final MeowDuels plugin;
    private final File file;
    private YamlConfiguration data;

    public KitLayoutManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.file = new File(meowDuels.getDataFolder(), "kitlayouts.yml");
        this.reload();
    }

    public void reload() {
        this.data = this.file.exists() ? YamlConfiguration.loadConfiguration((File)this.file) : new YamlConfiguration();
    }

    public void save() {
        try {
            this.data.save(this.file);
        }
        catch (IOException iOException) {
            this.plugin.getLogger().warning("Failed to save kitlayouts.yml: " + iOException.getMessage());
        }
    }

    private static String path(UUID uUID, String string) {
        return "layouts." + String.valueOf(uUID) + "." + string.toLowerCase(Locale.ROOT);
    }

    public boolean has(UUID uUID, String string) {
        return uUID != null && string != null && this.data.getConfigurationSection(KitLayoutManager.path(uUID, string)) != null;
    }

    public void store(UUID uUID, String string, ItemStack[] itemStackArray, ItemStack[] itemStackArray2, ItemStack itemStack) {
        String string2 = KitLayoutManager.path(uUID, string);
        this.data.set(string2 + ".contents", Arrays.asList(itemStackArray == null ? new ItemStack[]{} : itemStackArray));
        this.data.set(string2 + ".armor", Arrays.asList(itemStackArray2 == null ? new ItemStack[]{} : itemStackArray2));
        this.data.set(string2 + ".offhand", (Object)itemStack);
        this.save();
    }

    public void clear(UUID uUID, String string) {
        this.data.set(KitLayoutManager.path(uUID, string), null);
        this.save();
    }

    private ItemStack[] read(UUID uUID, String string, String string2, int n) {
        ConfigurationSection configurationSection = this.data.getConfigurationSection(KitLayoutManager.path(uUID, string));
        if (configurationSection == null) {
            return null;
        }
        List list = configurationSection.getList(string2);
        if (list == null) {
            return null;
        }
        ItemStack[] itemStackArray = new ItemStack[n];
        for (int i = 0; i < n && i < list.size(); ++i) {
            Object e = list.get(i);
            itemStackArray[i] = e instanceof ItemStack ? (ItemStack)e : null;
        }
        return itemStackArray;
    }

    public ItemStack[] contents(UUID uUID, String string) {
        return this.read(uUID, string, "contents", 36);
    }

    public ItemStack[] armor(UUID uUID, String string) {
        return this.read(uUID, string, "armor", 4);
    }

    public ItemStack offhand(UUID uUID, String string) {
        ConfigurationSection configurationSection = this.data.getConfigurationSection(KitLayoutManager.path(uUID, string));
        if (configurationSection == null) {
            return null;
        }
        return configurationSection.getItemStack("offhand");
    }

    public boolean matchesKit(Kit kit, ItemStack[] itemStackArray, ItemStack[] itemStackArray2, ItemStack itemStack) {
        HashMap<ItemStack, Integer> hashMap = new HashMap<ItemStack, Integer>();
        KitLayoutManager.tally(hashMap, kit.getContents());
        KitLayoutManager.tally(hashMap, kit.getArmor());
        KitLayoutManager.tally(hashMap, new ItemStack[]{kit.getOffhand()});
        HashMap<ItemStack, Integer> hashMap2 = new HashMap<ItemStack, Integer>();
        KitLayoutManager.tally(hashMap2, itemStackArray);
        KitLayoutManager.tally(hashMap2, itemStackArray2);
        KitLayoutManager.tally(hashMap2, new ItemStack[]{itemStack});
        return hashMap.equals(hashMap2);
    }

    private static void tally(Map<ItemStack, Integer> map, ItemStack[] itemStackArray) {
        if (itemStackArray == null) {
            return;
        }
        for (ItemStack itemStack : itemStackArray) {
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            ItemStack itemStack2 = itemStack.clone();
            itemStack2.setAmount(1);
            Integer n = map.get(itemStack2);
            map.put(itemStack2, (n == null ? 0 : n) + Math.max(1, itemStack.getAmount()));
        }
    }

    private boolean usable(UUID uUID, Kit kit) {
        if (!this.has(uUID, kit.getName())) {
            return false;
        }
        if (this.matchesKit(kit, this.contents(uUID, kit.getName()), this.armor(uUID, kit.getName()), this.offhand(uUID, kit.getName()))) {
            return true;
        }
        this.clear(uUID, kit.getName());
        return false;
    }

    public void applyTo(Player player, Kit kit) {
        ItemStack[] itemStackArray;
        if (player == null || kit == null) {
            return;
        }
        UUID uUID = player.getUniqueId();
        if (!this.usable(uUID, kit)) {
            kit.applyTo(player);
            return;
        }
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.clear();
        ItemStack[] itemStackArray2 = this.contents(uUID, kit.getName());
        if (itemStackArray2 != null) {
            playerInventory.setStorageContents(itemStackArray2);
        }
        if ((itemStackArray = this.armor(uUID, kit.getName())) != null) {
            playerInventory.setArmorContents(itemStackArray);
        }
        playerInventory.setItemInOffHand(this.offhand(uUID, kit.getName()));
        kit.selectMainHand(player);
        player.updateInventory();
    }

    public void applyOffhand(Player player, Kit kit) {
        if (player == null || kit == null) {
            return;
        }
        if (!this.usable(player.getUniqueId(), kit)) {
            kit.applyOffhand(player);
            return;
        }
        player.getInventory().setItemInOffHand(this.offhand(player.getUniqueId(), kit.getName()));
        player.updateInventory();
    }
}

