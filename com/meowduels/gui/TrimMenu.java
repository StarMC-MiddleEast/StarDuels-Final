package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.util.Colors;
import com.meowduels.util.Items;
import com.meowduels.util.Trims;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TrimMenu
extends Menu {
    private static final int HELMET_SLOT = 20;
    private static final int CHEST_SLOT = 21;
    private static final int LEGS_SLOT = 22;
    private static final int BOOTS_SLOT = 23;
    private static final int PATTERN_SLOT = 38;
    private static final int MATERIAL_SLOT = 42;
    private static final int APPLY_ALL_SLOT = 48;
    private static final int CLEAR_SLOT = 50;
    private final MeowDuels plugin;
    private final String kitName;
    private final UUID owner;
    private int patternIndex = 0;
    private int materialIndex = 0;
    private static final String MENU = "trim";
    private static final String[] CYCLE_LORE = new String[]{"&7\u029f\u1d07\ua730\u1d1b-\u1d04\u029f\u026a\u1d04\u1d0b &8- &f\u0274\u1d07x\u1d1b", "&7\u0280\u026a\u0262\u029c\u1d1b-\u1d04\u029f\u026a\u1d04\u1d0b &8- &f\u1d18\u0280\u1d07\u1d20\u026a\u1d0f\u1d1c\ua731", "", "&8{index}/{total}"};

    public TrimMenu(MeowDuels meowDuels, String string, UUID uUID) {
        this.plugin = meowDuels;
        this.kitName = string;
        this.owner = uUID;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("kit", this.kitName);
        this.createRaw(6, guiConfigManager.title(MENU, "&d&l\u1d1b\u0280\u026a\u1d0d\ua731 &7- &f{kit}", hashMap));
        ItemStack itemStack = guiConfigManager.pane(MENU);
        for (int i = 0; i < 54; ++i) {
            this.inventory.setItem(i, itemStack);
        }
        Kit kit = this.plugin.getKitManager().get(this.kitName);
        if (kit == null) {
            return;
        }
        Player viewer = Bukkit.getPlayer(this.owner);
        if (viewer != null) this.selectFirstAllowed(viewer);
        ItemStack[] itemStackArray = kit.getArmor();
        this.inventory.setItem(20, this.armorDisplay(guiConfigManager, itemStackArray, 3, guiConfigManager.line(MENU, "armor", "piece-helmet", "\u029c\u1d07\u029f\u1d0d\u1d07\u1d1b", null)));
        this.inventory.setItem(21, this.armorDisplay(guiConfigManager, itemStackArray, 2, guiConfigManager.line(MENU, "armor", "piece-chestplate", "\u1d04\u029c\u1d07\ua731\u1d1b\u1d18\u029f\u1d00\u1d1b\u1d07", null)));
        this.inventory.setItem(22, this.armorDisplay(guiConfigManager, itemStackArray, 1, guiConfigManager.line(MENU, "armor", "piece-leggings", "\u029f\u1d07\u0262\u0262\u026a\u0274\u0262\ua731", null)));
        this.inventory.setItem(23, this.armorDisplay(guiConfigManager, itemStackArray, 0, guiConfigManager.line(MENU, "armor", "piece-boots", "\u0299\u1d0f\u1d0f\u1d1b\ua731", null)));
        HashMap<String, String> hashMap2 = new HashMap<String, String>();
        hashMap2.put("pattern", Trims.pretty(Trims.PATTERN_KEYS[this.patternIndex]));
        hashMap2.put("index", String.valueOf(this.patternIndex + 1));
        hashMap2.put("total", String.valueOf(Trims.PATTERN_KEYS.length));
        this.inventory.setItem(38, guiConfigManager.face(MENU, "pattern", hashMap2, GuiConfigManager.Face.of(Material.PAPER, "&d&l\u1d18\u1d00\u1d1b\u1d1b\u1d07\u0280\u0274&7: &f{pattern}", CYCLE_LORE)).tag(this.plugin.keyButton(), "trim-pattern").build());
        HashMap<String, String> hashMap3 = new HashMap<String, String>();
        hashMap3.put("material", Trims.pretty(Trims.MATERIAL_KEYS[this.materialIndex]));
        hashMap3.put("index", String.valueOf(this.materialIndex + 1));
        hashMap3.put("total", String.valueOf(Trims.MATERIAL_KEYS.length));
        this.inventory.setItem(42, guiConfigManager.face(MENU, "material", hashMap3, GuiConfigManager.Face.of(guiConfigManager.material(MENU, "material", this.materialIcon()), "&d&l\u1d0d\u1d00\u1d1b\u1d07\u0280\u026a\u1d00\u029f&7: &f{material}", CYCLE_LORE)).tag(this.plugin.keyButton(), "trim-material").build());
        this.inventory.setItem(48, guiConfigManager.face(MENU, "apply-all", null, GuiConfigManager.Face.of(Material.LIME_DYE, "&a&l\u1d00\u1d18\u1d18\u029f\u028f \u1d1b\u1d0f \u1d00\u029f\u029f \u1d18\u026a\u1d07\u1d04\u1d07\ua731", "&7\ua731\u1d07\u1d1b \u1d1b\u029c\u1d07 \ua731\u1d07\u029f\u1d07\u1d04\u1d1b\u1d07\u1d05 \u1d1b\u0280\u026a\u1d0d \u1d0f\u0274 \u1d07\u1d20\u1d07\u0280\u028f \u1d00\u0280\u1d0d\u1d0f\u0280 \u1d18\u026a\u1d07\u1d04\u1d07.")).tag(this.plugin.keyButton(), "trim-apply-all").build());
        this.inventory.setItem(50, guiConfigManager.face(MENU, "clear", null, GuiConfigManager.Face.of(Material.BARRIER, "&c&l\u1d04\u029f\u1d07\u1d00\u0280 \u1d00\u029f\u029f \u1d1b\u0280\u026a\u1d0d\ua731", "&7\u0280\u1d07\u1d0d\u1d0f\u1d20\u1d07 \u1d1b\u0280\u026a\u1d0d\ua731 \ua730\u0280\u1d0f\u1d0d \u1d07\u1d20\u1d07\u0280\u028f \u1d00\u0280\u1d0d\u1d0f\u0280 \u1d18\u026a\u1d07\u1d04\u1d07.")).tag(this.plugin.keyButton(), "trim-clear").build());
    }

    private ItemStack armorDisplay(GuiConfigManager guiConfigManager, ItemStack[] itemStackArray, int n, String string) {
        ItemStack itemStack;
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("piece", string);
        ItemStack itemStack2 = itemStack = n >= 0 && n < itemStackArray.length ? itemStackArray[n] : null;
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return guiConfigManager.face(MENU, "armor-missing", hashMap, GuiConfigManager.Face.of(Material.GRAY_STAINED_GLASS_PANE, "&7\u0274\u1d0f {piece}", "&8\u1d1b\u029c\u026a\ua731 \u1d0b\u026a\u1d1b \u029c\u1d00\ua731 \u0274\u1d0f {piece}.")).build();
        }
        ItemStack itemStack3 = itemStack.clone();
        String[] stringArray = this.plugin.getTrimPreferences().get(this.owner, this.kitName, n);
        if (stringArray != null) {
            Trims.apply(itemStack3, stringArray[0], stringArray[1]);
        }
        try {
            ItemMeta itemMeta = itemStack3.getItemMeta();
            if (itemMeta != null) {
                ArrayList<String> arrayList = new ArrayList<String>();
                if (stringArray != null) {
                    HashMap<String, String> hashMap2 = new HashMap<String, String>();
                    hashMap2.put(MENU, Trims.pretty(stringArray[1]) + " " + Trims.pretty(stringArray[0]));
                    arrayList.add(Colors.toSection(guiConfigManager.line(MENU, "armor", "your-trim", "&7\u028f\u1d0f\u1d1c\u0280 \u1d1b\u0280\u026a\u1d0d&8: &f{trim}", hashMap2)));
                } else {
                    arrayList.add(Colors.toSection(guiConfigManager.line(MENU, "armor", "no-trim", "&8\u0274\u1d0f \u1d1b\u0280\u026a\u1d0d \ua731\u1d07\u1d1b.", null)));
                }
                arrayList.add(Colors.toSection(guiConfigManager.line(MENU, "armor", "apply-hint", "&e\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d00\u1d18\u1d18\u029f\u028f \u1d1b\u029c\u1d07 \ua731\u1d07\u029f\u1d07\u1d04\u1d1b\u1d07\u1d05 \u1d1b\u0280\u026a\u1d0d.", null)));
                itemMeta.setLore(arrayList);
                itemStack3.setItemMeta(itemMeta);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return itemStack3;
    }

    private Material materialIcon() {
        switch (Trims.MATERIAL_KEYS[this.materialIndex]) {
            case "quartz": {
                return Material.QUARTZ;
            }
            case "iron": {
                return Material.IRON_INGOT;
            }
            case "gold": {
                return Material.GOLD_INGOT;
            }
            case "copper": {
                return Material.COPPER_INGOT;
            }
            case "netherite": {
                return Material.NETHERITE_INGOT;
            }
            case "redstone": {
                return Material.REDSTONE;
            }
            case "emerald": {
                return Material.EMERALD;
            }
            case "diamond": {
                return Material.DIAMOND;
            }
            case "lapis": {
                return Material.LAPIS_LAZULI;
            }
            case "amethyst": {
                return Material.AMETHYST_SHARD;
            }
        }
        return Material.PAPER;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        if (!Trims.canEdit(player)) {
            player.sendMessage(this.plugin.messages().get("general.no-permission", new String[0]));
            return;
        }
        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
        String string = Items.readTag(itemStack, this.plugin.keyButton());
        if ("trim-pattern".equals(string)) {
            int n = inventoryClickEvent.isRightClick() ? -1 : 1;
            if (!this.cyclePattern(player, n)) {
                player.sendMessage(this.plugin.messages().get("general.no-permission", new String[0]));
                return;
            }
            this.reopen(player);
            return;
        }
        if ("trim-material".equals(string)) {
            int n = inventoryClickEvent.isRightClick() ? -1 : 1;
            if (!this.cycleMaterial(player, n)) {
                player.sendMessage(this.plugin.messages().get("general.no-permission", new String[0]));
                return;
            }
            this.reopen(player);
            return;
        }
        if ("trim-apply-all".equals(string)) {
            this.applyAll(player);
            return;
        }
        if ("trim-clear".equals(string)) {
            this.clearAll(player);
            return;
        }
        int n = this.slotToArmor(inventoryClickEvent.getRawSlot());
        if (n >= 0) {
            this.applyOne(player, n);
        }
    }

    private int slotToArmor(int n) {
        switch (n) {
            case 20: {
                return 3;
            }
            case 21: {
                return 2;
            }
            case 22: {
                return 1;
            }
            case 23: {
                return 0;
            }
        }
        return -1;
    }

    private void applyOne(Player player, int n) {
        if (!Trims.canUse(player, Trims.PATTERN_KEYS[this.patternIndex], Trims.MATERIAL_KEYS[this.materialIndex])) {
            player.sendMessage(this.plugin.messages().get("general.no-permission", new String[0]));
            return;
        }
        Kit kit = this.plugin.getKitManager().get(this.kitName);
        if (kit == null) {
            return;
        }
        ItemStack[] itemStackArray = kit.getArmor();
        if (n >= itemStackArray.length || itemStackArray[n] == null || itemStackArray[n].getType() == Material.AIR) {
            player.sendMessage(this.plugin.messages().get("notice.trim.kit-no-armor-slot", new String[0]));
            return;
        }
        if (!Trims.available()) {
            player.sendMessage(this.plugin.messages().get("notice.trim.could-not-apply-trim", new String[0]));
            return;
        }
        this.plugin.getTrimPreferences().set(this.owner, this.kitName, n, Trims.PATTERN_KEYS[this.patternIndex], Trims.MATERIAL_KEYS[this.materialIndex]);
        player.sendMessage(this.plugin.messages().get("notice.trim.applied-trim-only-will", "value", String.valueOf(Trims.pretty(Trims.MATERIAL_KEYS[this.materialIndex])), "value2", String.valueOf(Trims.pretty(Trims.PATTERN_KEYS[this.patternIndex]))));
        this.reopen(player);
    }

    private void applyAll(Player player) {
        if (!Trims.canUse(player, Trims.PATTERN_KEYS[this.patternIndex], Trims.MATERIAL_KEYS[this.materialIndex])) {
            player.sendMessage(this.plugin.messages().get("general.no-permission", new String[0]));
            return;
        }
        Kit kit = this.plugin.getKitManager().get(this.kitName);
        if (kit == null) {
            return;
        }
        if (!Trims.available()) {
            player.sendMessage(this.plugin.messages().get("notice.trim.no-armor-trim-trims", new String[0]));
            return;
        }
        this.plugin.getTrimPreferences().setAll(this.owner, this.kitName, Trims.PATTERN_KEYS[this.patternIndex], Trims.MATERIAL_KEYS[this.materialIndex]);
        player.sendMessage(this.plugin.messages().get("notice.trim.applied-trim-all-armor", new String[0]));
        this.reopen(player);
    }

    private void clearAll(Player player) {
        this.plugin.getTrimPreferences().clear(this.owner, this.kitName);
        player.sendMessage(this.plugin.messages().get("notice.trim.cleared-trims", "value", String.valueOf(this.kitName)));
        this.reopen(player);
    }

    private void reopen(Player player) {
        this.build();
        player.openInventory(this.inventory);
    }

    private boolean cyclePattern(Player player, int direction) {
        for (int patternStep = 1; patternStep <= Trims.PATTERN_KEYS.length; patternStep++) {
            int pattern = Math.floorMod(this.patternIndex + direction * patternStep, Trims.PATTERN_KEYS.length);
            for (int materialStep = 0; materialStep < Trims.MATERIAL_KEYS.length; materialStep++) {
                int material = Math.floorMod(this.materialIndex + direction * materialStep, Trims.MATERIAL_KEYS.length);
                if (!Trims.canUse(player, Trims.PATTERN_KEYS[pattern], Trims.MATERIAL_KEYS[material])) continue;
                this.patternIndex = pattern;
                this.materialIndex = material;
                return true;
            }
        }
        return false;
    }

    private boolean cycleMaterial(Player player, int direction) {
        for (int materialStep = 1; materialStep <= Trims.MATERIAL_KEYS.length; materialStep++) {
            int material = Math.floorMod(this.materialIndex + direction * materialStep, Trims.MATERIAL_KEYS.length);
            for (int patternStep = 0; patternStep < Trims.PATTERN_KEYS.length; patternStep++) {
                int pattern = Math.floorMod(this.patternIndex + direction * patternStep, Trims.PATTERN_KEYS.length);
                if (!Trims.canUse(player, Trims.PATTERN_KEYS[pattern], Trims.MATERIAL_KEYS[material])) continue;
                this.patternIndex = pattern;
                this.materialIndex = material;
                return true;
            }
        }
        return false;
    }

    private void selectFirstAllowed(Player player) {
        if (Trims.canUse(player, Trims.PATTERN_KEYS[this.patternIndex], Trims.MATERIAL_KEYS[this.materialIndex])) return;
        for (int pattern = 0; pattern < Trims.PATTERN_KEYS.length; pattern++) {
            for (int material = 0; material < Trims.MATERIAL_KEYS.length; material++) {
                if (!Trims.canUse(player, Trims.PATTERN_KEYS[pattern], Trims.MATERIAL_KEYS[material])) continue;
                this.patternIndex = pattern;
                this.materialIndex = material;
                return;
            }
        }
    }
}

