package com.meowduels.listeners;

import com.meowduels.MeowDuels;
import com.meowduels.util.Cooldowns;
import com.meowduels.util.GoldenHead;
import com.meowduels.util.Sounds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GoldenHeadListener
implements Listener {
    private static final int REGEN_TICKS = 200;
    private static final int REGEN_AMPLIFIER = 1;
    private static final int ABSORPTION_TICKS = 2400;
    private static final int ABSORPTION_AMPLIFIER = 0;
    private static final int HUNGER_RESTORE = 4;
    private static final double SATURATION_RESTORE = 9.6;
    private final MeowDuels plugin;
    private static final String COOLDOWN = "golden-head";

    public GoldenHeadListener(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent playerInteractEvent) {
        Action action = playerInteractEvent.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (playerInteractEvent.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (!GoldenHead.isGoldenHead((Plugin)this.plugin, playerInteractEvent.getItem())) {
            return;
        }
        playerInteractEvent.setCancelled(true);
        this.eat(playerInteractEvent.getPlayer());
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onConsume(PlayerItemConsumeEvent playerItemConsumeEvent) {
        if (!GoldenHead.isGoldenHead((Plugin)this.plugin, playerItemConsumeEvent.getItem())) {
            return;
        }
        playerItemConsumeEvent.setCancelled(true);
        this.eat(playerItemConsumeEvent.getPlayer());
    }

    private void eat(Player player) {
        long l = (long)(this.plugin.getConfig().getDouble("golden-head.cooldown-seconds", 5.0) * 1000.0);
        if (l > 0L) {
            long l2 = Cooldowns.remaining(player, COOLDOWN);
            if (l2 > 0L) {
                player.sendActionBar(GoldenHeadListener.mm("<#6B7079>" + Cooldowns.bar(l2, l, 10) + " <#FF8A93>\u0262\u1d0f\u029f\u1d05\u1d07\u0274 \u029c\u1d07\u1d00\u1d05 <#E6E8EB>" + Cooldowns.seconds(l2) + "s"));
                Sounds.cooling(player);
                return;
            }
            Cooldowns.start(player, COOLDOWN, this.heldHeadType(player), l);
        }
        FileConfiguration fileConfiguration = this.plugin.getConfig();
        int n = fileConfiguration.getInt("golden-head.regen-seconds", 10) * 20;
        int n2 = fileConfiguration.getInt("golden-head.absorption-seconds", 120) * 20;
        if (n > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, n, Math.max(0, fileConfiguration.getInt("golden-head.regen-level", 2) - 1)));
        }
        if (n2 > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, n2, Math.max(0, fileConfiguration.getInt("golden-head.absorption-level", 1) - 1)));
        }
        int n3 = Math.min(20, player.getFoodLevel() + fileConfiguration.getInt("golden-head.hunger-restore", 4));
        player.setFoodLevel(n3);
        player.setSaturation(Math.min((float)n3, player.getSaturation() + (float)fileConfiguration.getDouble("golden-head.saturation-restore", 9.6)));
        Sounds.eat(player);
        this.consumeOne(player);
    }

    private Material heldHeadType(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        if (GoldenHead.isGoldenHead((Plugin)this.plugin, playerInventory.getItemInMainHand())) {
            return playerInventory.getItemInMainHand().getType();
        }
        if (GoldenHead.isGoldenHead((Plugin)this.plugin, playerInventory.getItemInOffHand())) {
            return playerInventory.getItemInOffHand().getType();
        }
        return Material.PLAYER_HEAD;
    }

    private static Component mm(String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }

    private void consumeOne(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        if (GoldenHead.isGoldenHead((Plugin)this.plugin, playerInventory.getItemInMainHand())) {
            playerInventory.setItemInMainHand(GoldenHeadListener.shrink(playerInventory.getItemInMainHand()));
        } else if (GoldenHead.isGoldenHead((Plugin)this.plugin, playerInventory.getItemInOffHand())) {
            playerInventory.setItemInOffHand(GoldenHeadListener.shrink(playerInventory.getItemInOffHand()));
        }
    }

    private static ItemStack shrink(ItemStack itemStack) {
        if (itemStack.getAmount() <= 1) {
            return new ItemStack(Material.AIR);
        }
        itemStack.setAmount(itemStack.getAmount() - 1);
        return itemStack;
    }
}

