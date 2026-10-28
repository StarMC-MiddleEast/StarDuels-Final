package com.meowduels.util;

import com.meowduels.MeowDuels;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Shows the queued kit's icon as an item particle burst for that player. */
public final class TotemPopEffect {
    private TotemPopEffect() {}

    public static void play(Player player, Material icon, MeowDuels plugin) {
        if (player == null || !player.isOnline() || icon == null || icon.isAir()) return;

        Location pop = player.getLocation().add(0.0, 1.0, 0.0);
        ItemStack iconStack = new ItemStack(icon, 1);
        player.spawnParticle(Particle.ITEM, pop, 18, 0.32, 0.42, 0.32, 0.12, iconStack);
        player.spawnParticle(Particle.SWEEP_ATTACK, pop, 1, 0.0, 0.0, 0.0, 0.0);
        player.playSound(pop, Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.15f);
    }
}
