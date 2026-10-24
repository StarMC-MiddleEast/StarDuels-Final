package com.meowduels.listeners;

import com.meowduels.gui.DuelConfirmMenu;
import com.meowduels.gui.KitPickMenu;
import com.meowduels.gui.Menu;
import com.meowduels.gui.QueueCategoriesMenu;
import com.meowduels.gui.QueuePickMenu;
import com.meowduels.util.Sounds;
import java.util.Iterator;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GuiListener
implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        HumanEntity humanEntity = inventoryClickEvent.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Inventory inventory = inventoryClickEvent.getInventory();
        InventoryHolder inventoryHolder = inventory.getHolder();
        if (!(inventoryHolder instanceof Menu)) {
            return;
        }
        Menu menu = (Menu)inventoryHolder;
        int n = inventoryClickEvent.getRawSlot();
        boolean bl = n >= 0 && n < inventory.getSize();
        boolean bl2 = bl;
        if (!menu.isEditable()) {
            inventoryClickEvent.setCancelled(true);
            if (bl && inventoryClickEvent.getCurrentItem() != null) {
                if (menu instanceof DuelConfirmMenu || menu instanceof KitPickMenu) {
                    Sounds.duelMenuClick(player);
                } else if (menu instanceof QueuePickMenu || menu instanceof QueueCategoriesMenu) {
                    Sounds.queueClick(player);
                } else {
                    Sounds.click(player);
                }
                menu.onClick(player, inventoryClickEvent);
            }
            return;
        }
        if (bl && menu.isProtectedSlot(n)) {
            inventoryClickEvent.setCancelled(true);
            menu.onClick(player, inventoryClickEvent);
            return;
        }
        InventoryAction inventoryAction = inventoryClickEvent.getAction();
        if (inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY || inventoryAction == InventoryAction.COLLECT_TO_CURSOR) {
            inventoryClickEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        Inventory inventory = inventoryDragEvent.getInventory();
        InventoryHolder inventoryHolder = inventory.getHolder();
        if (!(inventoryHolder instanceof Menu)) {
            return;
        }
        Menu menu = (Menu)inventoryHolder;
        if (!menu.isEditable()) {
            inventoryDragEvent.setCancelled(true);
            return;
        }
        int n = inventory.getSize();
        Iterator iterator = inventoryDragEvent.getRawSlots().iterator();
        while (iterator.hasNext()) {
            int n2 = (Integer)iterator.next();
            if (n2 >= n || !menu.isProtectedSlot(n2)) continue;
            inventoryDragEvent.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent inventoryOpenEvent) {
        HumanEntity humanEntity = inventoryOpenEvent.getPlayer();
        if (humanEntity instanceof Player && inventoryOpenEvent.getInventory().getHolder() instanceof Menu) {
            Sounds.open((Player)humanEntity);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        HumanEntity humanEntity = inventoryCloseEvent.getPlayer();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        InventoryHolder inventoryHolder = inventoryCloseEvent.getInventory().getHolder();
        if (inventoryHolder instanceof Menu) {
            Menu menu = (Menu)inventoryHolder;
            menu.onClose(player, inventoryCloseEvent);
        }
    }
}

