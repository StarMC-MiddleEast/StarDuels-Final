package com.meowduels.listeners;

import com.meowduels.MeowDuels;
import com.meowduels.model.Arena;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class EventListener
implements Listener {
    private final MeowDuels plugin;

    public EventListener(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @EventHandler(ignoreCancelled=true)
    public void onDamage(EntityDamageEvent entityDamageEvent) {
        Entity entity = entityDamageEvent.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (!this.plugin.getEventManager().isPlaying(player.getUniqueId())) {
            return;
        }
        if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.VOID) {
            entityDamageEvent.setCancelled(true);
            Player player2 = player;
            Arena arena = this.plugin.getEventManager().getArena();
            if (arena != null) {
                Location location = arena.getSpawn1() != null ? arena.getSpawn1() : arena.getSpawn2();
                Location location2 = location;
                if (location != null) {
                    player2.setFallDistance(0.0f);
                    player2.teleport(location);
                }
            }
            return;
        }
        if (!this.plugin.getEventManager().isRunning()) {
            entityDamageEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent playerDeathEvent) {
        Player player = playerDeathEvent.getEntity();
        UUID uUID = player.getUniqueId();
        if (!this.plugin.getEventManager().isPlaying(uUID) || !this.plugin.getEventManager().isRunning()) {
            return;
        }
        playerDeathEvent.setKeepInventory(false);
        playerDeathEvent.setDeathMessage(null);
        Player player2 = player.getKiller();
        UUID uUID2 = player2 != null ? player2.getUniqueId() : null;
        this.plugin.getEventManager().announceKill(uUID, uUID2);
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            player.spigot().respawn();
            this.plugin.getEventManager().onDeath(uUID);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        this.plugin.getEventManager().handleQuit(playerQuitEvent.getPlayer().getUniqueId());
    }
}

