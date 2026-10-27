package com.meowduels.listeners;

import com.meowduels.MeowDuels;
import com.meowduels.gui.KitEditorMenu;
import com.meowduels.gui.PartyConfirmMenu;
import com.meowduels.gui.PartyMenu;
import com.meowduels.gui.PartyModeMenu;
import com.meowduels.gui.PartySettingsMenu;
import com.meowduels.gui.QueuePickMenu;
import com.meowduels.gui.SettingsMenu;
import com.meowduels.model.Party;
import com.meowduels.model.ActiveDuel;
import com.meowduels.util.Items;
import com.meowduels.util.SpawnItems;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class SpawnItemsListener
implements Listener {
    private final MeowDuels plugin;

    public SpawnItemsListener(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    private boolean isSpawnItem(ItemStack itemStack) {
        return SpawnItems.isManagedItem(this.plugin, itemStack);
    }

    @EventHandler(ignoreCancelled=true)
    public void onDrop(PlayerDropItemEvent playerDropItemEvent) {
        if (playerDropItemEvent.getItemDrop() != null && this.isSpawnItem(playerDropItemEvent.getItemDrop().getItemStack())) {
            playerDropItemEvent.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onLobbyElytraGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack chestItem = player.getInventory().getChestplate();
        if (event.isGliding() && !SpawnItems.canUseLobbyElytra(player)
                && SpawnItems.LOBBY_ELYTRA.equals(Items.readTag(chestItem, this.plugin.keyButton()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onLobbyElytraDamage(PlayerItemDamageEvent event) {
        if (SpawnItems.LOBBY_ELYTRA.equals(Items.readTag(event.getItem(), this.plugin.keyButton()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        if (inventoryClickEvent.getWhoClicked() instanceof Player player
                && inventoryClickEvent.getClickedInventory() == player.getInventory()
                && inventoryClickEvent.getSlot() == 40
                && SpawnItems.LOBBY_WIND_CHARGE.equals(Items.readTag(player.getInventory().getItemInOffHand(), this.plugin.keyButton()))) {
            inventoryClickEvent.setCancelled(true);
            return;
        }
        if (this.isSpawnItem(inventoryClickEvent.getCurrentItem())) {
            inventoryClickEvent.setCancelled(true);
            return;
        }
        if ((inventoryClickEvent.getAction() == InventoryAction.HOTBAR_SWAP || inventoryClickEvent.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) && inventoryClickEvent.getHotbarButton() == 3 && inventoryClickEvent.getWhoClicked() instanceof Player && this.isSpawnItem(((Player)inventoryClickEvent.getWhoClicked()).getInventory().getItem(3))) {
            inventoryClickEvent.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)
                || !SpawnItems.LOBBY_WIND_CHARGE.equals(Items.readTag(player.getInventory().getItemInOffHand(), this.plugin.keyButton()))) {
            return;
        }
        int topSize = event.getView().getTopInventory().getSize();
        if (event.getRawSlots().stream().anyMatch(slot -> slot >= topSize && event.getView().convertSlot(slot) == 40)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        if (this.isSpawnItem(event.getMainHandItem()) || this.isSpawnItem(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent playerInteractEvent) {
        Action action = playerInteractEvent.getAction();
        boolean bl = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        boolean bl2 = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        boolean bl3 = bl2;
        if (!bl && !bl2) {
            return;
        }
        if (playerInteractEvent.getItem() == null) {
            return;
        }
        Player player = playerInteractEvent.getPlayer();
        if (playerInteractEvent.getItem().getType() == org.bukkit.Material.WIND_CHARGE
                && bl && playerInteractEvent.getHand() == EquipmentSlot.HAND && this.isMaceMode(player)
                && !player.isOnGround() && player.getVelocity().getY() < -0.1
                && player.getLocation().getPitch() >= 75.0f) {
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (!player.isOnline() || !this.isMaceMode(player)) return;
                Vector velocity = player.getVelocity();
                // Brake only a fall that is still in progress. If a real hit
                // supplied upward knockback after the throw, preserve it.
                if (velocity.getY() < -0.05) {
                    player.setVelocity(new Vector(velocity.getX(), 0.0, velocity.getZ()));
                }
                player.setFallDistance(0.0f);
                player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD,
                        player.getLocation().add(0.0, 0.9, 0.0), 12, 0.22, 0.28, 0.22, 0.02);
            }, 1L);
        }
        String string = Items.readTag(playerInteractEvent.getItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        if ("lobby-wind-charge".equals(string)) {
            if (bl) {
                this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    UUID playerId = player.getUniqueId();
                    if (player.isOnline() && !this.plugin.getDuelManager().isInDuel(playerId)
                            && !this.plugin.getPartyManager().inPartyMatch(playerId)
                            && (this.plugin.getEventManager() == null || !this.plugin.getEventManager().isInvolved(playerId))
                            && (this.plugin.getSpectateManager() == null || !this.plugin.getSpectateManager().isSpectating(playerId))) {
                        player.getInventory().setItemInOffHand(SpawnItems.windCharge(this.plugin));
                    }
                }, 1L);
            }
            return;
        }
        if ("spawn-leavequeue".equals(string)) {
            playerInteractEvent.setCancelled(true);
            this.plugin.getQueueManager().leaveAll(player);
            return;
        }
        if (!bl) {
            return;
        }
        if ("spawn-createparty".equals(string)) {
            playerInteractEvent.setCancelled(true);
            this.plugin.getPartyManager().create(player);
            return;
        }
        if ("party-match".equals(string)) {
            playerInteractEvent.setCancelled(true);
            new PartyModeMenu(this.plugin).open(player);
            return;
        }
        if ("party-info".equals(string)) {
            playerInteractEvent.setCancelled(true);
            new PartyMenu(this.plugin).openFor(player);
            return;
        }
        if ("party-spectate".equals(string)) {
            playerInteractEvent.setCancelled(true);
            this.plugin.getPartyManager().spectate(player);
            return;
        }
        if ("party-leave".equals(string)) {
            playerInteractEvent.setCancelled(true);
            new PartyConfirmMenu(this.plugin, player).open(player);
            return;
        }
        if ("party-settings".equals(string)) {
            playerInteractEvent.setCancelled(true);
            if (!this.partyLeader(player)) {
                return;
            }
            new PartySettingsMenu(this.plugin).open(player);
            return;
        }
        if ("spawn-queue".equals(string)) {
            playerInteractEvent.setCancelled(true);
            if (this.busy(player)) {
                return;
            }
            new QueuePickMenu(this.plugin).open(player);
        } else if ("spawn-quick".equals(string)) {
            playerInteractEvent.setCancelled(true);
            if (this.busy(player)) {
                return;
            }
            this.plugin.getQueueManager().quickJoin(player);
        } else if ("spawn-kiteditor".equals(string)) {
            playerInteractEvent.setCancelled(true);
            if (this.busy(player)) {
                return;
            }
            new KitEditorMenu(this.plugin, true).open(player);
        } else if ("spawn-settings".equals(string)) {
            playerInteractEvent.setCancelled(true);
            new SettingsMenu(this.plugin, player).open(player);
        } else if ("spawn-rematch".equals(string)) {
            playerInteractEvent.setCancelled(true);
            if (this.busy(player)) {
                return;
            }
            this.plugin.getDuelManager().requestRematch(player);
        }
    }

    private boolean isMaceMode(Player player) {
        String kit = null;
        ActiveDuel duel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel != null) {
            kit = duel.getKit();
        } else {
            Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
            if (party != null && this.plugin.getPartyManager().inPartyMatch(player.getUniqueId())) kit = party.getKit();
        }
        if (kit == null) return false;
        String normalized = kit.replaceAll("[^A-Za-z0-9]", "").toLowerCase(java.util.Locale.ROOT);
        return Set.of("mace", "modernmace", "spearmace").contains(normalized);
    }

    private boolean partyLeader(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null) {
            player.sendMessage(this.plugin.messages().get("notice.spawn.re-not-party", new String[0]));
            return false;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.spawn.only-party-leader-do", new String[0]));
            return false;
        }
        return true;
    }

    private boolean busy(Player player) {
        UUID uUID = player.getUniqueId();
        if (this.plugin.getPartyManager().busy(player)) {
            return true;
        }
        if (this.plugin.getDuelManager().isInDuel(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.spawn.re-already-duel", new String[0]));
            return true;
        }
        if (this.plugin.getSpectateManager().isSpectating(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.spawn.re-spectating-type-leave", new String[0]));
            return true;
        }
        if (this.plugin.getEventManager().isInvolved(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.spawn.re-event-type-leave", new String[0]));
            return true;
        }
        return false;
    }
}

