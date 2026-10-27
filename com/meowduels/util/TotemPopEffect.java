package com.meowduels.util;

import com.meowduels.MeowDuels;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/** Sends a private totem-resurrection visual using the queued kit's icon. */
public final class TotemPopEffect {
    private TotemPopEffect() {}

    public static void play(Player player, Material icon, MeowDuels plugin) {
        if (player == null || !player.isOnline() || icon == null || icon.isAir()) return;

        ItemStack oldOffhand = player.getInventory().getItemInOffHand().clone();
        ItemStack visual = new ItemStack(icon, 1);
        player.sendEquipmentChange(player, EquipmentSlot.OFF_HAND, visual);
        player.sendEntityEffect(org.bukkit.EntityEffect.TOTEM_RESURRECT, player);
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.7f, 1.0f);
        Bukkit.getScheduler().runTaskLater((Plugin) plugin, () -> {
            if (player.isOnline()) player.sendEquipmentChange(player, EquipmentSlot.OFF_HAND, oldOffhand);
        }, 1L);
    }
}
