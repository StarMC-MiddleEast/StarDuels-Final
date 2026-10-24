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
import com.meowduels.util.Items;
import com.meowduels.util.SpawnItems;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

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
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        if (this.isSpawnItem(inventoryClickEvent.getCurrentItem())) {
            inventoryClickEvent.setCancelled(true);
            return;
        }
        if ((inventoryClickEvent.getAction() == InventoryAction.HOTBAR_SWAP || inventoryClickEvent.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) && inventoryClickEvent.getHotbarButton() == 3 && inventoryClickEvent.getWhoClicked() instanceof Player && this.isSpawnItem(((Player)inventoryClickEvent.getWhoClicked()).getInventory().getItem(3))) {
            inventoryClickEvent.setCancelled(true);
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
        String string = Items.readTag(playerInteractEvent.getItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        Player player = playerInteractEvent.getPlayer();
        if ("lobby-wind-charge".equals(string)) {
            if (bl && playerInteractEvent.getHand() == EquipmentSlot.HAND) {
                this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    if (player.isOnline() && !this.plugin.getDuelManager().isInDuel(player.getUniqueId()) && !this.plugin.getPartyManager().inPartyMatch(player.getUniqueId())) {
                        player.getInventory().setItem(3, SpawnItems.windCharge(this.plugin));
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

