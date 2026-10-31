package com.meowduels.listeners;

import com.meowduels.MeowDuels;
import com.meowduels.gui.ArenaMenu;
import com.meowduels.model.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

public class SetupChatListener
implements Listener {
    private final MeowDuels plugin;

    public SetupChatListener(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
    public void onChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {
        Player player = asyncPlayerChatEvent.getPlayer();
        String string = asyncPlayerChatEvent.getMessage();
        if (this.plugin.getSpectateManager().isSpectating(player.getUniqueId()) && string.equalsIgnoreCase(".leave")) {
            asyncPlayerChatEvent.setCancelled(true);
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.plugin.getSpectateManager().stop(player));
            return;
        }
        if (this.plugin.getHungerGamesManager().inSetup(player.getUniqueId())) {
            asyncPlayerChatEvent.setCancelled(true);
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.plugin.getHungerGamesManager().handleSetupInput(player, string));
            return;
        }
        if (this.plugin.getSetupManager().inEventSpawn(player.getUniqueId())) {
            asyncPlayerChatEvent.setCancelled(true);
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                Arena arena = this.plugin.getSetupManager().handleEventSpawnInput(player, string);
                if (arena != null) {
                    new ArenaMenu(this.plugin, arena).open(player);
                }
            });
            return;
        }
        if (this.plugin.getSetupManager().inBorder(player.getUniqueId())) {
            asyncPlayerChatEvent.setCancelled(true);
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                Arena arena = this.plugin.getSetupManager().handleBorderInput(player, string);
                if (arena != null) {
                    new ArenaMenu(this.plugin, arena).open(player);
                }
            });
            return;
        }
        if (!this.plugin.getSetupManager().inSetup(player.getUniqueId())) {
            return;
        }
        asyncPlayerChatEvent.setCancelled(true);
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            Arena arena = this.plugin.getSetupManager().handleInput(player, string);
            if (arena != null) {
                new ArenaMenu(this.plugin, arena).open(player);
            }
        });
    }
}

