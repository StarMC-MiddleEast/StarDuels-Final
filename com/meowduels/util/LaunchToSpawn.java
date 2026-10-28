package com.meowduels.util;

import com.meowduels.MeowDuels;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/** A short return animation with collision bypass enabled only during flight. */
public final class LaunchToSpawn {
    private static final Set<UUID> LAUNCHING = ConcurrentHashMap.newKeySet();

    private LaunchToSpawn() {}

    public static boolean launch(Player player, MeowDuels plugin, Location destination, Runnable completion) {
        double ceiling = destination == null || destination.getWorld() == null
            ? 0.0 : destination.getWorld().getMaxHeight() - 2.0;
        return launch(player, plugin, null, destination, ceiling, completion);
    }

    public static boolean launch(Player player, MeowDuels plugin, Location center, Location destination, Runnable completion) {
        double ceiling = center != null && center.getWorld() != null
            ? center.getWorld().getMaxHeight() - 2.0
            : destination == null || destination.getWorld() == null ? 0.0 : destination.getWorld().getMaxHeight() - 2.0;
        return launch(player, plugin, center, destination, ceiling, completion);
    }

    public static boolean launch(Player player, MeowDuels plugin, Location center, Location destination, double arenaCeiling, Runnable completion) {
        if (player == null || destination == null || destination.getWorld() == null
            || !LAUNCHING.add(player.getUniqueId())) return false;

        Location target = destination.clone();
        try {
            Location launchPoint = center == null ? null : center.clone();
            Location preload = launchPoint != null ? launchPoint : target;
            preload.getWorld().getChunkAtAsync(preload.getBlockX() >> 4, preload.getBlockZ() >> 4)
                .whenComplete((chunk, error) -> Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) {
                        LAUNCHING.remove(player.getUniqueId());
                        completion.run();
                        return;
                    }
                    if (error != null || chunk == null) {
                        LAUNCHING.remove(player.getUniqueId());
                        player.teleport(target);
                        completion.run();
                        return;
                    }
                    begin(player, plugin, launchPoint, target, arenaCeiling, completion);
                }));
        } catch (Throwable error) {
            LAUNCHING.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    private static void begin(Player player, MeowDuels plugin, Location center, Location destination, double arenaCeiling, Runnable completion) {
        UUID id = player.getUniqueId();
        boolean originallyInvulnerable = player.isInvulnerable();
        boolean originallyNoPhysics = player.hasNoPhysics();
        float yaw = destination.getYaw();
        player.setInvulnerable(true);
        player.setNoPhysics(true);
        player.setFallDistance(0.0f);
        if (center != null && center.getWorld() != null) {
            center.setYaw(yaw);
            center.setPitch(0.0f);
            player.teleport(center);
        }

        // A capped, short lift keeps the effect inside the configured arena.
        double lift = Math.max(0.0, Math.min(3.0, arenaCeiling - player.getLocation().getY()));
        double upwardVelocity = lift < 0.25 ? 0.0 : Math.min(0.42, lift / 7.0);
        player.setVelocity(new Vector(0.0, upwardVelocity, 0.0));
        player.playSound(player.getLocation(), "minecraft:block.conduit.deactivate", SoundCategory.PLAYERS, 1.0f, 1.0f);

        Bukkit.getScheduler().runTaskLater((Plugin) plugin, () -> {
            if (!player.isOnline()) {
                LAUNCHING.remove(id);
                return;
            }
            Location landing = destination.clone();
            landing.setYaw(yaw);
            landing.setPitch(0.0f);
            player.teleport(landing);
            player.setFallDistance(0.0f);
            player.setVelocity(new Vector(0.0, -0.2, 0.0));
            player.setNoPhysics(originallyNoPhysics);
            player.setInvulnerable(originallyInvulnerable);
            Bukkit.getScheduler().runTaskLater((Plugin) plugin, () -> {
                if (player.isOnline()) player.setFallDistance(0.0f);
                LAUNCHING.remove(id);
                completion.run();
            }, 1L);
        }, 10L);
    }
}
