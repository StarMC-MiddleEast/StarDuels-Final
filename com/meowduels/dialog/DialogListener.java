package com.meowduels.dialog;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;

public final class DialogListener implements Listener {
    private static final Set<UUID> ALLOW_CHEST_ONCE = ConcurrentHashMap.newKeySet();
    private static MeowDuels plugin;

    public DialogListener(MeowDuels plugin) {
        DialogListener.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player) || !(event.getInventory().getHolder() instanceof Menu menu)) return;
        if (ALLOW_CHEST_ONCE.remove(player.getUniqueId())) return;
        if (!MenuRouter.usesDialog(plugin, player, menu)) return;
        InventoryView view = event.getView();
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) DialogMenu.open(plugin, player, menu, view, 0, false);
        });
    }

    static void allowNextChest(Player player) {
        ALLOW_CHEST_ONCE.add(player.getUniqueId());
    }

    static void renderAgain(MeowDuels plugin, Player player, Menu menu, InventoryView view, int page, boolean playOpenSound) {
        DialogMenu.open(plugin, player, menu, view, page, playOpenSound);
    }

    static void beginAction(Player player) {
        // The dialog is replaced by the action or the next dialog screen.
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ALLOW_CHEST_ONCE.remove(event.getPlayer().getUniqueId());
    }
}
