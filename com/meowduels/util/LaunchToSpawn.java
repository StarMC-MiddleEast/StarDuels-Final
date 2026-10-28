package com.meowduels.util;

import com.meowduels.MeowDuels;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/** Smoothly moves duelists through the air while temporarily bypassing blocks. */
public final class LaunchToSpawn {
    private static final Set<UUID> LAUNCHING = ConcurrentHashMap.newKeySet();
    private static final int FLIGHT_TICKS = 16;

    private LaunchToSpawn() {}

    public static boolean launch(Player player, MeowDuels plugin, Location destination, Runnable completion) {
        double ceiling = destination == null || destination.getWorld() == null ? 0.0 : destination.getWorld().getMaxHeight() - 2.0;
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
        Location start = player.getLocation().clone();
        Location control = center == null ? null : center.clone();
        try {
            CompletableFuture<?> startChunk = start.getWorld() == null ? CompletableFuture.completedFuture(null)
                : start.getWorld().getChunkAtAsync(start.getBlockX() >> 4, start.getBlockZ() >> 4);
            CompletableFuture<?> targetChunk = target.getWorld().getChunkAtAsync(target.getBlockX() >> 4, target.getBlockZ() >> 4);
            CompletableFuture<?> controlChunk = control == null || control.getWorld() == null
                ? CompletableFuture.completedFuture(null)
                : control.getWorld().getChunkAtAsync(control.getBlockX() >> 4, control.getBlockZ() >> 4);
            CompletableFuture.allOf(startChunk, targetChunk, controlChunk).whenComplete((ignored, error) ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) {
                        LAUNCHING.remove(player.getUniqueId());
                        completion.run();
                    } else if (error != null) {
                        player.teleport(target);
                        LAUNCHING.remove(player.getUniqueId());
                        completion.run();
                    } else {
                        begin(player, plugin, start, control, target, arenaCeiling, completion);
                    }
                }));
        } catch (Throwable error) {
            LAUNCHING.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    private static void begin(Player player, MeowDuels plugin, Location start, Location control, Location destination,
                              double arenaCeiling, Runnable completion) {
        UUID id = player.getUniqueId();
        boolean originallyInvulnerable = player.isInvulnerable();
        boolean originallyNoPhysics = player.hasNoPhysics();
        boolean originallyAllowFlight = player.getAllowFlight();
        boolean originallyFlying = player.isFlying();
        boolean canCurve = start.getWorld() != null && start.getWorld().equals(destination.getWorld());
        boolean canUseControl = canCurve && control != null && control.getWorld() != null && control.getWorld().equals(start.getWorld());
        Location target = destination.clone();
        target.setPitch(0.0f);

        player.setInvulnerable(true);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setNoPhysics(true);
        player.setFallDistance(0.0f);
        player.playSound(player.getLocation(), "minecraft:block.conduit.deactivate", SoundCategory.PLAYERS, 1.0f, 1.0f);

        final int[] tick = {0};
        Bukkit.getScheduler().runTaskTimer((Plugin) plugin, task -> {
            if (!player.isOnline() || !LAUNCHING.contains(id)) {
                task.cancel();
                LAUNCHING.remove(id);
                return;
            }
            tick[0]++;
            double progress = (double) tick[0] / FLIGHT_TICKS;
            Location next;
            if (canCurve) {
                double x;
                double z;
                if (canUseControl) {
                    double inverse = 1.0 - progress;
                    x = inverse * inverse * start.getX() + 2.0 * inverse * progress * control.getX() + progress * progress * target.getX();
                    z = inverse * inverse * start.getZ() + 2.0 * inverse * progress * control.getZ() + progress * progress * target.getZ();
                } else {
                    x = start.getX() + (target.getX() - start.getX()) * progress;
                    z = start.getZ() + (target.getZ() - start.getZ()) * progress;
                }
                double baseY = start.getY() + (target.getY() - start.getY()) * progress;
                double worldCeiling = start.getWorld().getMaxHeight() - 2.0;
                double pathCeiling = Math.min(worldCeiling, Math.max(arenaCeiling, target.getY() + 3.0));
                double availableLift = Math.max(0.0, Math.min(3.0, pathCeiling - Math.max(start.getY(), target.getY())));
                double y = baseY + Math.sin(Math.PI * progress) * availableLift;
                next = new Location(start.getWorld(), x, y, z, target.getYaw(), 0.0f);
            } else if (tick[0] < FLIGHT_TICKS) {
                // Keep a visible lift in the arena before changing worlds to the configured spawn.
                double availableLift = Math.max(0.0, Math.min(3.0, arenaCeiling - start.getY()));
                double progressUp = Math.min(1.0, (double) tick[0] / (FLIGHT_TICKS / 2.0));
                next = start.clone().add(0.0, Math.sin(progressUp * Math.PI / 2.0) * availableLift, 0.0);
            } else {
                next = target;
            }
            player.teleport(next);
            player.setFallDistance(0.0f);
            player.spawnParticle(org.bukkit.Particle.CLOUD, next, 5, 0.2, 0.12, 0.2, 0.01);
            player.spawnParticle(org.bukkit.Particle.END_ROD, next, 2, 0.1, 0.1, 0.1, 0.0);

            if (tick[0] >= FLIGHT_TICKS) {
                task.cancel();
                player.teleport(target);
                player.setVelocity(new Vector(0.0, 0.0, 0.0));
                player.setNoPhysics(originallyNoPhysics);
                player.setInvulnerable(originallyInvulnerable);
                player.setAllowFlight(originallyAllowFlight);
                if (originallyFlying && originallyAllowFlight) player.setFlying(true);
                player.setFallDistance(0.0f);
                player.setNoDamageTicks(0);
                LAUNCHING.remove(id);
                completion.run();
            }
        }, 1L, 1L);
    }
}
