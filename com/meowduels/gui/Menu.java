package com.meowduels.gui;

import com.meowduels.util.Colors;
import com.meowduels.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class Menu
implements InventoryHolder {
    protected Inventory inventory;

    protected Inventory create(int n, String string) {
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (int)(n * 9), (String)Text.color(string));
        return this.inventory;
    }

    protected Inventory createRaw(int n, String string) {
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (int)(n * 9), (String)Colors.toSection(string));
        return this.inventory;
    }

    protected Inventory createHopper(String string) {
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (InventoryType)InventoryType.HOPPER, (String)Colors.toSection(string));
        return this.inventory;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void open(Player player) {
        this.build();
        player.openInventory(this.inventory);
    }

    public abstract void build();

    public abstract void onClick(Player var1, InventoryClickEvent var2);

    public void onClose(Player player, InventoryCloseEvent inventoryCloseEvent) {
    }

    public boolean cancelClicks() {
        return true;
    }

    public boolean isEditable() {
        return false;
    }

    public boolean isProtectedSlot(int n) {
        return true;
    }
}

