package com.meowduels.listeners;

import com.meowduels.MeowDuels;
import com.meowduels.managers.EventManager;
import com.meowduels.model.ActiveDuel;
import com.meowduels.model.Arena;
import com.meowduels.model.Party;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.AbstractWindCharge;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

public class ArenaProtectionListener
implements Listener {
    private final MeowDuels plugin;

    public ArenaProtectionListener(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    private boolean eventArenaAllow(Location location, BlockData blockData) {
        EventManager eventManager = this.plugin.getEventManager();
        if (location != null && eventManager.isRunning() && eventManager.getArena() != null && eventManager.getArena().contains(location)) {
            eventManager.recordChange(location, blockData);
            return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled=true)
    public void onBreak(BlockBreakEvent blockBreakEvent) {
        Player player = blockBreakEvent.getPlayer();
        Location location = blockBreakEvent.getBlock().getLocation();
        if (this.eventArenaAllow(location, blockBreakEvent.getBlock().getBlockData())) {
            return;
        }
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
        if (activeDuel == null) {
            Party party = this.partyMatchOf(player);
            if (party != null) {
                this.partyBlock(party, blockBreakEvent.getBlock().getBlockData(), location, bl -> blockBreakEvent.setCancelled(bl.booleanValue()), true);
                return;
            }
            this.protectIdleArena(player, location, bl -> blockBreakEvent.setCancelled(bl.booleanValue()));
            return;
        }
        Arena arena = activeDuel.getArena();
        if (!arena.contains(location)) {
            blockBreakEvent.setCancelled(true);
            return;
        }
        boolean bl2 = this.wasPlacedThisMatch(activeDuel, location);
        if (arena.canBreak(location) || bl2 && arena.isAllowRemoveAdded()) {
            activeDuel.recordChange(location, blockBreakEvent.getBlock().getBlockData());
        } else {
            blockBreakEvent.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlace(BlockPlaceEvent blockPlaceEvent) {
        Player player = blockPlaceEvent.getPlayer();
        Location location = blockPlaceEvent.getBlock().getLocation();
        if (this.eventArenaAllow(location, blockPlaceEvent.getBlockReplacedState().getBlockData())) {
            return;
        }
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
        if (activeDuel == null) {
            Party party = this.partyMatchOf(player);
            if (party != null) {
                this.partyBlock(party, blockPlaceEvent.getBlockReplacedState().getBlockData(), location, bl -> blockPlaceEvent.setCancelled(bl.booleanValue()), false);
                return;
            }
            this.protectIdleArena(player, location, bl -> blockPlaceEvent.setCancelled(bl.booleanValue()));
            return;
        }
        Arena arena = activeDuel.getArena();
        if (!arena.contains(location)) {
            blockPlaceEvent.setCancelled(true);
            return;
        }
        if (arena.canPlace(location)) {
            activeDuel.recordChange(location, blockPlaceEvent.getBlockReplacedState().getBlockData());
        } else {
            blockPlaceEvent.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onExplode(EntityExplodeEvent entityExplodeEvent) {
        Party party;
        if (entityExplodeEvent.getEntity() instanceof AbstractWindCharge) {
            return;
        }
        Location location = entityExplodeEvent.getLocation();
        if (!this.plugin.getArenaManager().hasArenaInWorld(location.getWorld())) {
            return;
        }
        Arena arena = this.plugin.getArenaManager().findArenaAt(location);
        if (arena == null) {
            return;
        }
        EventManager eventManager = this.plugin.getEventManager();
        if (eventManager.isRunning() && eventManager.getArena() != null && eventManager.getArena().getName().equalsIgnoreCase(arena.getName())) {
            for (Block block : entityExplodeEvent.blockList()) {
                eventManager.recordChange(block.getLocation(), block.getBlockData());
            }
            return;
        }
        ActiveDuel activeDuel = this.activeDuelFor(arena);
        Party party2 = party = activeDuel == null ? this.partyFor(arena) : null;
        if (activeDuel == null && party == null || !arena.isAllowExplosions()) {
            entityExplodeEvent.blockList().clear();
            entityExplodeEvent.setCancelled(true);
            return;
        }
        for (Block block : entityExplodeEvent.blockList()) {
            if (activeDuel != null) {
                activeDuel.recordChange(block.getLocation(), block.getBlockData());
                continue;
            }
            party.recordChange(block.getLocation(), block.getBlockData());
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onBurn(BlockBurnEvent blockBurnEvent) {
        Party party;
        if (!this.plugin.getArenaManager().hasArenaInWorld(blockBurnEvent.getBlock().getLocation().getWorld())) {
            return;
        }
        Arena arena = this.plugin.getArenaManager().findArenaAt(blockBurnEvent.getBlock().getLocation());
        if (arena == null) {
            return;
        }
        ActiveDuel activeDuel = this.activeDuelFor(arena);
        Party party2 = party = activeDuel == null ? this.partyFor(arena) : null;
        if (activeDuel == null && party == null || !arena.isAllowFireSpread()) {
            blockBurnEvent.setCancelled(true);
            return;
        }
        if (activeDuel != null) {
            activeDuel.recordChange(blockBurnEvent.getBlock().getLocation(), blockBurnEvent.getBlock().getBlockData());
        } else {
            party.recordChange(blockBurnEvent.getBlock().getLocation(), blockBurnEvent.getBlock().getBlockData());
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onIgnite(BlockIgniteEvent blockIgniteEvent) {
        if (!this.plugin.getArenaManager().hasArenaInWorld(blockIgniteEvent.getBlock().getLocation().getWorld())) {
            return;
        }
        Arena arena = this.plugin.getArenaManager().findArenaAt(blockIgniteEvent.getBlock().getLocation());
        if (arena == null) {
            return;
        }
        if (!arena.isAllowFireSpread()) {
            blockIgniteEvent.setCancelled(true);
            return;
        }
        if (this.activeDuelFor(arena) == null && this.partyFor(arena) == null) {
            blockIgniteEvent.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onLiquidFlow(BlockFromToEvent blockFromToEvent) {
        if (!this.plugin.getArenaManager().hasArenaInWorld(blockFromToEvent.getToBlock().getLocation().getWorld())) {
            return;
        }
        Location location = blockFromToEvent.getToBlock().getLocation();
        Arena arena = this.plugin.getArenaManager().findArenaAt(location);
        if (arena == null) {
            return;
        }
        if (this.eventArenaAllow(location, blockFromToEvent.getToBlock().getBlockData())) {
            return;
        }
        if (!arena.isAllowLiquidFlow()) {
            blockFromToEvent.setCancelled(true);
            return;
        }
        if (this.activeDuelFor(arena) == null && this.partyFor(arena) == null) {
            blockFromToEvent.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onMobSpawn(CreatureSpawnEvent creatureSpawnEvent) {
        if (creatureSpawnEvent.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }
        if (!this.plugin.getArenaManager().hasArenaInWorld(creatureSpawnEvent.getLocation().getWorld())) {
            return;
        }
        Arena arena = this.plugin.getArenaManager().findArenaAt(creatureSpawnEvent.getLocation());
        if (arena == null) {
            return;
        }
        if (!arena.isAllowMobSpawns()) {
            creatureSpawnEvent.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onBucketEmpty(PlayerBucketEmptyEvent playerBucketEmptyEvent) {
        Block block = playerBucketEmptyEvent.getBlock();
        if (block == null) {
            return;
        }
        Location location = block.getLocation();
        if (!this.plugin.getArenaManager().hasArenaInWorld(location.getWorld())) {
            return;
        }
        if (this.eventArenaAllow(location, block.getBlockData())) {
            return;
        }
        Player player = playerBucketEmptyEvent.getPlayer();
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
        if (activeDuel == null) {
            Party party = this.partyMatchOf(player);
            if (party != null) {
                this.partyBlock(party, block.getBlockData(), location, bl -> playerBucketEmptyEvent.setCancelled(bl.booleanValue()), false);
                return;
            }
            this.protectIdleArena(player, location, bl -> playerBucketEmptyEvent.setCancelled(bl.booleanValue()));
            return;
        }
        Arena arena = activeDuel.getArena();
        if (!arena.contains(location)) {
            playerBucketEmptyEvent.setCancelled(true);
            return;
        }
        if (arena.canPlace(location)) {
            activeDuel.recordChange(location, block.getBlockData());
        } else {
            playerBucketEmptyEvent.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onEntityBurn(EntityDamageEvent entityDamageEvent) {
        Entity entity = entityDamageEvent.getEntity();
        if (!(entity instanceof Item) && !(entity instanceof Projectile)) {
            return;
        }
        EntityDamageEvent.DamageCause damageCause = entityDamageEvent.getCause();
        if (damageCause != EntityDamageEvent.DamageCause.FIRE && damageCause != EntityDamageEvent.DamageCause.FIRE_TICK && damageCause != EntityDamageEvent.DamageCause.LAVA) {
            return;
        }
        Location location = entity.getLocation();
        if (!this.plugin.getArenaManager().hasArenaInWorld(location.getWorld())) {
            return;
        }
        if (this.plugin.getArenaManager().findArenaAt(location) != null) {
            entityDamageEvent.setCancelled(true);
        }
    }

    private Party partyFor(Arena arena) {
        return this.plugin.getPartyManager().matchInArena(arena);
    }

    private ActiveDuel activeDuelFor(Arena arena) {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
            if (activeDuel == null || activeDuel.getArena() != arena) continue;
            return activeDuel;
        }
        return null;
    }

    private boolean wasPlacedThisMatch(ActiveDuel activeDuel, Location location) {
        Location location2 = new Location(location.getWorld(), (double)location.getBlockX(), (double)location.getBlockY(), (double)location.getBlockZ());
        BlockData blockData = activeDuel.getChangedBlocks().get(location2);
        return blockData != null && blockData.getMaterial() == Material.AIR;
    }

    private Party partyMatchOf(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isFighting() || party.getArena() == null) {
            return null;
        }
        return party.getAlive().contains(player.getUniqueId()) ? party : null;
    }

    private void partyBlock(Party party, BlockData blockData, Location location, Consumer<Boolean> consumer, boolean bl) {
        Arena arena = party.getArena();
        if (!arena.contains(location)) {
            consumer.accept(true);
            return;
        }
        if (bl ? arena.canBreak(location) : arena.canPlace(location)) {
            this.plugin.getPartyManager().recordPartyChange(party, location, blockData);
        } else {
            consumer.accept(true);
        }
    }

    private void protectIdleArena(Player player, Location location, Consumer<Boolean> consumer) {
        if (player.hasPermission("meowduels.admin")) {
            return;
        }
        if (!this.plugin.getArenaManager().hasArenaInWorld(location.getWorld())) {
            return;
        }
        Arena arena = this.plugin.getArenaManager().findArenaAt(location);
        if (arena != null) {
            consumer.accept(true);
            player.sendMessage(this.plugin.messages().get("arena.cannot-edit", new String[0]));
        }
    }
}

