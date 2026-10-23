package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.EditKitEffectsMenu;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class KitItemsEditMenu
extends Menu {
    private static final int OFFHAND_SLOT = 45;
    private static final int[] ARMOR_SLOTS = new int[]{46, 47, 48, 49};
    private static final int EFFECTS_SLOT = 43;
    private static final int NAME_SLOT = 44;
    private static final int RESET_SLOT = 51;
    private static final int SAVE_SLOT = 52;
    private static final int EXIT_SLOT = 53;
    private final MeowDuels plugin;
    private final Kit kit;
    private final boolean personal;
    private final UUID owner;
    private static final String MENU = "kititems";

    public KitItemsEditMenu(MeowDuels meowDuels, Kit kit) {
        this(meowDuels, kit, false, null);
    }

    public KitItemsEditMenu(MeowDuels meowDuels, Kit kit, boolean bl, UUID uUID) {
        this.plugin = meowDuels;
        this.kit = kit;
        this.personal = bl;
        this.owner = uUID;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("kit", this.kit.getName());
        this.createRaw(6, this.personal ? guiConfigManager.titleOf(MENU, "personal", "&d&l\u028f\u1d0f\u1d1c\u0280 \u029f\u1d00\u028f\u1d0f\u1d1c\u1d1b \u2192 &5{kit}", hashMap) : guiConfigManager.titleOf(MENU, "global", "&d&l\u1d07\u1d05\u026a\u1d1b \u026a\u1d1b\u1d07\u1d0d\ua731 \u2192 &5{kit}", hashMap));
        ItemStack itemStack = guiConfigManager.pane(MENU);
        for (int i = 36; i < 54; ++i) {
            this.inventory.setItem(i, itemStack);
        }
        this.restoreOriginalLayout();
        if (this.personal) {
            this.inventory.setItem(43, itemStack);
            this.inventory.setItem(44, guiConfigManager.face(MENU, "about-personal", hashMap, GuiConfigManager.Face.of(Material.NAME_TAG, "&f{kit}", "&7\u1d00\u0280\u0280\u1d00\u0274\u0262\u1d07 \u1d1b\u029c\u1d07 \u1d0b\u026a\u1d1b \u029c\u1d0f\u1d21\u1d07\u1d20\u1d07\u0280 \u028f\u1d0f\u1d1c \u029f\u026a\u1d0b\u1d07.", "&7\u1d0f\u0274\u029f\u028f \u028f\u1d0f\u1d1c \u0262\u1d07\u1d1b \u1d1b\u029c\u026a\ua731 \u029f\u1d00\u028f\u1d0f\u1d1c\u1d1b.", "&8\u026a\u1d1b\u1d07\u1d0d\ua731 \u1d00\u0280\u1d07 \ua731\u1d07\u1d1b \u0299\u028f \u1d1b\u029c\u1d07 \ua731\u1d07\u0280\u1d20\u1d07\u0280.")).build());
            this.inventory.setItem(51, guiConfigManager.face(MENU, "reset-personal", hashMap, GuiConfigManager.Face.of(Material.RED_STAINED_GLASS_PANE, "&c&l\u0280\u1d07\ua731\u1d07\u1d1b", "&7\u0299\u1d00\u1d04\u1d0b \u1d1b\u1d0f \u1d1b\u029c\u1d07 \ua731\u1d07\u0280\u1d20\u1d07\u0280'\ua731 \u1d05\u1d07\ua730\u1d00\u1d1c\u029f\u1d1b \u029f\u1d00\u028f\u1d0f\u1d1c\u1d1b.")).build());
            this.inventory.setItem(52, guiConfigManager.face(MENU, "save-personal", hashMap, GuiConfigManager.Face.of(Material.LIME_STAINED_GLASS_PANE, "&a&l\ua731\u1d00\u1d20\u1d07", "&7\ua731\u1d00\u1d20\u1d07 \u1d1b\u029c\u026a\ua731 \u029f\u1d00\u028f\u1d0f\u1d1c\u1d1b \ua730\u1d0f\u0280 \u028f\u1d0f\u1d1c\u0280\ua731\u1d07\u029f\ua730.")).build());
        } else {
            this.inventory.setItem(43, guiConfigManager.face(MENU, "start-effects", hashMap, GuiConfigManager.Face.of(Material.POTION, "&d&l\ua731\u1d1b\u1d00\u0280\u1d1b \u1d07\ua730\ua730\u1d07\u1d04\u1d1b\ua731", "&7\u1d04\u1d0f\u0274\ua730\u026a\u0262\u1d1c\u0280\u1d07 \u1d18\u1d0f\u1d1b\u026a\u1d0f\u0274 \u1d07\ua730\ua730\u1d07\u1d04\u1d1b\ua731 \u1d00\u1d18\u1d18\u029f\u026a\u1d07\u1d05", "&7\u1d00\u1d1b \u1d1b\u029c\u1d07 \ua731\u1d1b\u1d00\u0280\u1d1b \u1d0f\ua730 \u1d1b\u029c\u1d07 \ua730\u026a\u0262\u029c\u1d1b.")).build());
            this.inventory.setItem(44, guiConfigManager.face(MENU, "about-global", hashMap, GuiConfigManager.Face.of(Material.NAME_TAG, "&f{kit}", "&7\u1d05\u0280\u1d00\u0262 \u026a\u1d1b\u1d07\u1d0d\ua731 \u026a\u0274\u1d1b\u1d0f \u1d1b\u029c\u1d07 \u0262\u0280\u026a\u1d05 \u1d00\u0299\u1d0f\u1d20\u1d07,", "&7\ua730\u026a\u029f\u029f \u1d00\u0280\u1d0d\u1d0f\u0280 + \u1d0f\ua730\ua730\u029c\u1d00\u0274\u1d05 \u0299\u1d07\u029f\u1d0f\u1d21.", "&c\u1d1b\u029c\u026a\ua731 \u1d04\u029c\u1d00\u0274\u0262\u1d07\ua731 \u1d1b\u029c\u1d07 \u1d0b\u026a\u1d1b \ua730\u1d0f\u0280 \u1d07\u1d20\u1d07\u0280\u028f\u1d0f\u0274\u1d07.")).build());
            this.inventory.setItem(51, guiConfigManager.face(MENU, "reset-global", hashMap, GuiConfigManager.Face.of(Material.RED_STAINED_GLASS_PANE, "&c&l\u0280\u1d07\ua731\u1d07\u1d1b", "&7\u0280\u1d07\ua731\u1d1b\u1d0f\u0280\u1d07\ua731 \u1d1b\u029c\u1d07 \u1d0b\u026a\u1d1b'\ua731 \ua731\u1d00\u1d20\u1d07\u1d05 \u029f\u1d00\u028f\u1d0f\u1d1c\u1d1b.")).build());
            this.inventory.setItem(52, guiConfigManager.face(MENU, "save-global", hashMap, GuiConfigManager.Face.of(Material.LIME_STAINED_GLASS_PANE, "&a&l\ua731\u1d00\u1d20\u1d07", "&7\ua731\u1d00\u1d20\u1d07 \u1d1b\u029c\u026a\ua731 \u029f\u1d00\u028f\u1d0f\u1d1c\u1d1b \u1d1b\u1d0f \u1d1b\u029c\u1d07 \u1d0b\u026a\u1d1b.")).build());
        }
        this.inventory.setItem(53, guiConfigManager.face(MENU, "exit", hashMap, GuiConfigManager.Face.of(Material.BARRIER, "&c&l\u1d07x\u026a\u1d1b", "&7\u1d04\u029f\u1d0f\ua731\u1d07 \u1d21\u026a\u1d1b\u029c\u1d0f\u1d1c\u1d1b \ua731\u1d00\u1d20\u026a\u0274\u0262.")).build());
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public boolean isProtectedSlot(int n) {
        if (n >= 0 && n < 36) {
            return false;
        }
        if (n == 45) {
            return false;
        }
        for (int n2 : ARMOR_SLOTS) {
            if (n != n2) continue;
            return false;
        }
        return true;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        int n = inventoryClickEvent.getRawSlot();
        if (n == 43) {
            if (this.personal || !player.hasPermission("meowduels.admin")) {
                return;
            }
            player.closeInventory();
            new EditKitEffectsMenu(this.plugin, this.kit).open(player);
            return;
        }
        if (n == 51) {
            if (this.personal) {
                this.plugin.getKitLayouts().clear(player.getUniqueId(), this.kit.getName());
                player.sendMessage(this.plugin.messages().get("notice.kit.layout-was-reset-server", "kit", this.kit.getName()));
            } else {
                player.sendMessage(this.plugin.messages().get("notice.kit.reset-kit-s-saved", new String[0]));
            }
            this.restoreOriginalLayout();
            return;
        }
        if (n == 52) {
            if (this.personal && !this.plugin.getKitLayouts().matchesKit(this.kit, this.grid(), this.gridArmor(), this.inventory.getItem(45))) {
                player.sendMessage(this.plugin.messages().get("notice.kit.isn-t-same-set", new String[0]));
                return;
            }
            this.saveToKit();
            player.sendMessage(this.personal ? this.plugin.messages().get("notice.kit.saved-own-layout", "kit", this.kit.getName()) : this.plugin.messages().get("notice.kit.saved-s-items-everyone", "kit", this.kit.getName()));
            player.closeInventory();
            return;
        }
        if (n == 53) {
            player.closeInventory();
        }
    }

    @Override
    public void onClose(Player player, InventoryCloseEvent inventoryCloseEvent) {
    }

    private void restoreOriginalLayout() {
        int n;
        UUID uUID = this.owner;
        boolean bl = this.personal && uUID != null && this.plugin.getKitLayouts().has(uUID, this.kit.getName());
        ItemStack[] itemStackArray = bl ? this.plugin.getKitLayouts().contents(uUID, this.kit.getName()) : this.kit.getContents();
        ItemStack[] itemStackArray2 = bl ? this.plugin.getKitLayouts().armor(uUID, this.kit.getName()) : this.kit.getArmor();
        ItemStack itemStack = bl ? this.plugin.getKitLayouts().offhand(uUID, this.kit.getName()) : this.kit.getOffhand();
        for (n = 0; n < 36; ++n) {
            this.inventory.setItem(n, itemStackArray != null && n < itemStackArray.length ? itemStackArray[n] : null);
        }
        this.inventory.setItem(45, itemStack);
        for (n = 0; n < ARMOR_SLOTS.length; ++n) {
            this.inventory.setItem(ARMOR_SLOTS[n], itemStackArray2 != null && n < itemStackArray2.length ? itemStackArray2[n] : null);
        }
    }

    private ItemStack[] grid() {
        ItemStack[] itemStackArray = new ItemStack[36];
        for (int i = 0; i < 36; ++i) {
            itemStackArray[i] = this.inventory.getItem(i);
        }
        return itemStackArray;
    }

    private ItemStack[] gridArmor() {
        ItemStack[] itemStackArray = new ItemStack[4];
        for (int i = 0; i < ARMOR_SLOTS.length; ++i) {
            itemStackArray[i] = this.inventory.getItem(ARMOR_SLOTS[i]);
        }
        return itemStackArray;
    }

    private void saveToKit() {
        ItemStack[] itemStackArray = this.grid();
        ItemStack[] itemStackArray2 = this.gridArmor();
        if (this.personal && this.owner != null) {
            this.plugin.getKitLayouts().store(this.owner, this.kit.getName(), itemStackArray, itemStackArray2, this.inventory.getItem(45));
            return;
        }
        this.kit.setContents(itemStackArray);
        this.kit.setArmor(itemStackArray2);
        this.kit.setOffhand(this.inventory.getItem(45));
        this.plugin.getKitManager().save();
    }
}

