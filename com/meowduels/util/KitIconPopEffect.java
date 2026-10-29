package com.meowduels.util;

import com.meowduels.MeowDuels;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Shows a short, centered pop of the queued kit's item icon with no totem visual. */
public final class KitIconPopEffect {
    private KitIconPopEffect() {}

    public static void play(Player player, Material icon, MeowDuels plugin) {
        if (player == null || !player.isOnline() || icon == null || icon.isAir()) return;

        Location eye = player.getEyeLocation();
        Location effectLocation = eye.clone().add(eye.getDirection().multiply(1.5));
        ItemStack iconStack = new ItemStack(icon, 1);
        player.spawnParticle(Particle.ITEM, player.getLocation().add(0.0, 1.0, 0.0), 32, 0.38, 0.48, 0.38, 0.14, iconStack);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.85f, 0.8f);

        try {
            ItemDisplay display = effectLocation.getWorld().spawn(effectLocation, ItemDisplay.class, entity -> {
                entity.setItemStack(iconStack);
                entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GUI);
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setGravity(false);
                entity.setPersistent(false);
                entity.setInterpolationDuration(1);
                entity.setTransformation(transformation(0.15f));
            });
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.getUniqueId().equals(player.getUniqueId())) other.hideEntity(plugin, display);
            }
            player.showEntity(plugin, display);

            final int[] tick = {0};
            Bukkit.getScheduler().runTaskTimer((Plugin) plugin, task -> {
                if (!player.isOnline() || !display.isValid() || tick[0] >= 60) {
                    task.cancel();
                    if (display.isValid()) display.remove();
                    return;
                }
                tick[0]++;
                float scale;
                if (tick[0] <= 6) {
                    scale = 0.15f + tick[0] * 0.2f;
                } else if (tick[0] <= 12) {
                    scale = 1.35f - (tick[0] - 6) * (0.35f / 6.0f);
                } else {
                    scale = 1.0f;
                }
                Location view = player.getEyeLocation();
                display.teleport(view.add(view.getDirection().multiply(1.5)));
                display.setTransformation(transformation(scale));
            }, 1L, 1L);
        } catch (LinkageError | RuntimeException error) {
            // The item particles above are a visual fallback if display entities are unavailable.
            plugin.getLogger().warning("Could not show the kit icon pop for " + player.getName() + ": " + error.getMessage());
        }
    }

    private static Transformation transformation(float scale) {
        return new Transformation(new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(),
            new Vector3f(scale, scale, scale), new Quaternionf());
    }
}
