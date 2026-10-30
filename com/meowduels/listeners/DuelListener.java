package com.meowduels.listeners;

import com.meowduels.MeowDuels;
import com.meowduels.managers.DuelManager;
import com.meowduels.model.ActiveDuel;
import com.meowduels.model.Arena;
import com.meowduels.model.Party;
import com.meowduels.util.AntiCheatBypass;
import com.meowduels.util.GameModeGuard;
import com.meowduels.util.Sounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;

public class DuelListener
implements Listener {
    private final MeowDuels plugin;
    private static final double OUT_OF_BOUNDS_MARGIN = 4.0;
    private static final double BOUNDS_INSET = 1.5;
    private static final long BOUNDS_PULL_COOLDOWN_MS = 1000L;
    private final Map<UUID, Long> lastBoundsPull = new HashMap<UUID, Long>();

    public DuelListener(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    private void guardPartyFighter(EntityDamageEvent entityDamageEvent, Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isFighting() || !party.getAlive().contains(player.getUniqueId())) {
            return;
        }
        if (party.isCountingDown()) {
            entityDamageEvent.setCancelled(true);
            return;
        }
        if (party.isFinished()) {
            entityDamageEvent.setCancelled(true);
            return;
        }
        if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.VOID) {
            Location location;
            entityDamageEvent.setCancelled(true);
            Location location2 = location = party.getArena() == null ? null : party.getArena().getEventSpawn();
            if (location != null) {
                player.setFallDistance(0.0f);
                player.teleport(location);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onDamage(EntityDamageEvent entityDamageEvent) {
        if (DuelManager.cancelRoundLaunchFallDamage(entityDamageEvent)) return;
        Entity entity = entityDamageEvent.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
        if (activeDuel == null) {
            this.guardPartyFighter(entityDamageEvent, player);
            return;
        }
        if (activeDuel.getState() != ActiveDuel.State.FIGHTING) {
            entityDamageEvent.setCancelled(true);
            return;
        }
        if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.FALL && !activeDuel.getArena().isAllowFallDamage()) {
            entityDamageEvent.setCancelled(true);
            return;
        }
        if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.VOID) {
            entityDamageEvent.setCancelled(true);
            Location location = player.getUniqueId().equals(activeDuel.getPlayer1()) ? activeDuel.getArena().getSpawn1() : activeDuel.getArena().getSpawn2();
            Location location2 = location;
            if (location != null) {
                player.setFallDistance(0.0f);
                player.teleport(location);
            }
            return;
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent playerDeathEvent) {
        Player player = playerDeathEvent.getEntity();
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
        if (activeDuel == null) {
            if (this.plugin.getPartyManager().inPartyMatch(player.getUniqueId())) {
                this.suppressMatchDrops(playerDeathEvent);
                Player player2 = player.getKiller();
                UUID uUID = player2 == null ? null : player2.getUniqueId();
                UUID uUID2 = player.getUniqueId();
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                    player.spigot().respawn();
                    this.plugin.getPartyManager().onDeath(uUID2, uUID);
                });
            } else if (this.plugin.getEventManager().isPlaying(player.getUniqueId())) {
                this.suppressMatchDrops(playerDeathEvent);
            } else {
                this.applyNormalDeathMessage(playerDeathEvent, player);
            }
            return;
        }
        this.suppressMatchDrops(playerDeathEvent);
        playerDeathEvent.setDeathMessage(null);
        UUID uUID = player.getUniqueId();
        Location location = player.getLocation().clone();
        this.plugin.getDuelManager().setPendingRoundRespawn(uUID, location);
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            player.spigot().respawn();
            this.plugin.getDuelManager().handleRoundLoss(uUID, location);
        });
    }

    private void suppressMatchDrops(PlayerDeathEvent playerDeathEvent) {
        playerDeathEvent.setKeepInventory(true);
        playerDeathEvent.getDrops().clear();
        playerDeathEvent.setDroppedExp(0);
    }

    private void applyNormalDeathMessage(PlayerDeathEvent playerDeathEvent, Player player) {
        playerDeathEvent.setDeathMessage(null);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent playerChangedWorldEvent) {
        this.plugin.getScoreboardService().handleJoin(playerChangedWorldEvent.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent playerRespawnEvent) {
        Player player = playerRespawnEvent.getPlayer();
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
        if (activeDuel != null) {
            Location location = this.plugin.getDuelManager().consumePendingRoundRespawn(player.getUniqueId());
            if (location == null) {
                location = player.getUniqueId().equals(activeDuel.getPlayer1()) ? activeDuel.getArena().getSpawn1() : activeDuel.getArena().getSpawn2();
            }
            if (location != null) {
                playerRespawnEvent.setRespawnLocation(location);
            }
            return;
        }
        if (!this.plugin.getConfig().getBoolean("on-death-spawn", true) || this.plugin.getEventManager().isInvolved(player.getUniqueId()) || this.plugin.getPartyManager().inPartyMatch(player.getUniqueId())) {
            return;
        }
        Location location = this.spawnPoint();
        if (location != null) {
            playerRespawnEvent.setRespawnLocation(location);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (player.isOnline() && !this.plugin.getDuelManager().isInDuel(player.getUniqueId())) {
                this.plugin.giveSpawnItems(player);
            }
        }, 1L);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onTeleportMonitor(PlayerTeleportEvent playerTeleportEvent) {
        if (!playerTeleportEvent.isCancelled()) {
            return;
        }
        Player player = playerTeleportEvent.getPlayer();
        if (!this.plugin.getDuelManager().isInDuel(player.getUniqueId()) && !this.plugin.getEventManager().isInvolved(player.getUniqueId())) {
            return;
        }
        this.plugin.getLogger().warning("Another plugin CANCELLED a teleport for " + player.getName() + " while they were in a MeowDuels match (cause: " + String.valueOf(playerTeleportEvent.getCause()) + "). That will strand them - check your world manager's access/gamemode enforcement for non-OP players.");
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onGameModeChange(PlayerGameModeChangeEvent playerGameModeChangeEvent) {
        if (GameModeGuard.isApplying()) {
            return;
        }
        GameMode gameMode = GameModeGuard.required(playerGameModeChangeEvent.getPlayer().getUniqueId());
        if (gameMode != null && playerGameModeChangeEvent.getNewGameMode() != gameMode) {
            playerGameModeChangeEvent.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onMove(PlayerMoveEvent playerMoveEvent) {
        if (playerMoveEvent instanceof PlayerTeleportEvent) {
            return;
        }
        Player player = playerMoveEvent.getPlayer();
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
        if (activeDuel == null) {
            return;
        }
        Location location = playerMoveEvent.getTo();
        if (location == null) {
            return;
        }
        if (activeDuel.getState() == ActiveDuel.State.STARTING && !activeDuel.isArenaEntered()) {
            Location location2 = playerMoveEvent.getFrom();
            if (location2.getX() != location.getX() || location2.getZ() != location.getZ()) {
                Location location3 = new Location(location2.getWorld(), location2.getX(), location.getY(), location2.getZ(), location.getYaw(), location.getPitch());
                playerMoveEvent.setTo(location3);
            }
            return;
        }
        if (activeDuel.getState() == ActiveDuel.State.FIGHTING) {
            Arena arena = activeDuel.getArena();
            // Runs on every move packet of every fighter: read the corners
            // straight, instead of building two Locations each time.
            if (arena == null || !arena.hasCorners()) {
                return;
            }
            double d = arena.minX();
            double d2 = (double)arena.maxX() + 1.0;
            double d3 = arena.minZ();
            double d4 = (double)arena.maxZ() + 1.0;
            if (location.getX() >= d - 4.0 && location.getX() <= d2 + 4.0 && location.getZ() >= d3 - 4.0 && location.getZ() <= d4 + 4.0) {
                return;
            }
            UUID uUID = player.getUniqueId();
            long l = System.currentTimeMillis();
            Long l2 = this.lastBoundsPull.get(uUID);
            if (l2 != null && l - l2 < 1000L) {
                return;
            }
            this.lastBoundsPull.put(uUID, l);
            double d5 = Math.max(d + 1.5, Math.min(d2 - 1.5, location.getX()));
            double d6 = Math.max(d3 + 1.5, Math.min(d4 - 1.5, location.getZ()));
            Location location7 = new Location(location.getWorld(), d5, location.getY(), d6, location.getYaw(), location.getPitch());
            player.setFallDistance(0.0f);
            player.teleport(location7);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onResurrect(EntityResurrectEvent entityResurrectEvent) {
        if (!(entityResurrectEvent.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player)entityResurrectEvent.getEntity();
        if (!this.plugin.getDuelManager().isInDuel(player.getUniqueId())) {
            return;
        }
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            if (player.isOnline()) {
                player.updateInventory();
            }
        });
    }

    private boolean countingDown(Player player) {
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
        return activeDuel != null && activeDuel.getState() == ActiveDuel.State.STARTING;
    }

    private static boolean isPotion(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        Material material = itemStack.getType();
        return material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION;
    }

    @EventHandler(ignoreCancelled=true)
    public void onConsume(PlayerItemConsumeEvent playerItemConsumeEvent) {
        Player player = playerItemConsumeEvent.getPlayer();
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
        if (activeDuel != null && this.countingDown(player) && DuelListener.isPotion(playerItemConsumeEvent.getItem())) {
            playerItemConsumeEvent.setCancelled(true);
            player.sendMessage(this.plugin.messages().get("notice.duel.no-potions-until-fight-2", new String[0]));
            Sounds.deny(player);
            return;
        }
        Arena arena = playerItemConsumeEvent.getItem().getType().isEdible() ? this.saturationArena(player) : null;
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            if (player.isOnline()) {
                if (activeDuel != null || this.plugin.getPartyManager().inPartyMatch(player.getUniqueId())) {
                    player.updateInventory();
                }
                if (arena != null && this.saturationArena(player) == arena && arena.isSaturationOnEat()) {
                    player.setSaturation(20.0f);
                }
            }
        });
    }

    private Arena saturationArena(Player player) {
        UUID uUID = player.getUniqueId();
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(uUID);
        if (activeDuel != null && activeDuel.getState() == ActiveDuel.State.FIGHTING && !activeDuel.isFinished()) {
            return activeDuel.getArena();
        }
        Party party = this.plugin.getPartyManager().partyOf(uUID);
        if (party != null && party.isFighting() && !party.isCountingDown() && !party.isFinished() && party.getAlive().contains(uUID)) {
            return party.getArena();
        }
        if (this.plugin.getEventManager().isRunning() && this.plugin.getEventManager().isAlive(uUID)) {
            return this.plugin.getEventManager().getArena();
        }
        return null;
    }

    @EventHandler(ignoreCancelled=true)
    public void onLaunch(ProjectileLaunchEvent projectileLaunchEvent) {
        ProjectileSource projectileSource = projectileLaunchEvent.getEntity().getShooter();
        if (!(projectileSource instanceof Player)) {
            return;
        }
        Player player = (Player)projectileSource;
        if (!this.plugin.getDuelManager().isInDuel(player.getUniqueId())) {
            return;
        }
        if (this.countingDown(player) && projectileLaunchEvent.getEntity() instanceof ThrownPotion) {
            projectileLaunchEvent.setCancelled(true);
            player.sendMessage(this.plugin.messages().get("notice.duel.no-potions-until-fight", new String[0]));
            Sounds.deny(player);
            return;
        }
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            if (player.isOnline()) {
                player.updateInventory();
            }
        });
    }

    @EventHandler(ignoreCancelled=true)
    public void onPartyFriendlyFire(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        ProjectileSource projectileSource;
        if (!(entityDamageByEntityEvent.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player)entityDamageByEntityEvent.getEntity();
        Entity entity = entityDamageByEntityEvent.getDamager();
        Player player2 = null;
        if (entity instanceof Player) {
            player2 = (Player)entity;
        } else if (entity instanceof Projectile && (projectileSource = ((Projectile)entity).getShooter()) instanceof Player) {
            player2 = (Player)projectileSource;
        }
        if (player2 == null || player2.getUniqueId().equals(player.getUniqueId())) {
            return;
        }
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null) {
            return;
        }
        boolean sameParty = party.has(player2.getUniqueId());
        // In a party-vs-party match the other side is not in this party, and
        // used to fall straight through here - so they could hit each other
        // during the countdown.
        boolean opponent = !sameParty && party.isFighting() && party.getOpponent() != null && party.getOpponent().has(player2.getUniqueId());
        if (!sameParty && !opponent) {
            return;
        }
        if (party.isCountingDown()) {
            entityDamageByEntityEvent.setCancelled(true);
            return;
        }
        if (party.isTeamMode() && party.isFighting()) {
            Party.Team team = party.teamOf(player2.getUniqueId());
            if (team != null && team == party.teamOf(player.getUniqueId())) {
                entityDamageByEntityEvent.setCancelled(true);
            }
            return;
        }
        if (sameParty && !party.isFighting() && !party.isFriendlyFire()) {
            entityDamageByEntityEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        ProjectileSource projectileSource;
        if (!(entityDamageByEntityEvent.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player)entityDamageByEntityEvent.getEntity();
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
        if (activeDuel == null || activeDuel.getState() != ActiveDuel.State.FIGHTING) {
            return;
        }
        Entity entity = entityDamageByEntityEvent.getDamager();
        Player player2 = null;
        if (entity instanceof Player) {
            player2 = (Player)entity;
        } else if (entity instanceof Projectile && (projectileSource = ((Projectile)entity).getShooter()) instanceof Player) {
            player2 = (Player)projectileSource;
        }
        if (player2 == null || player2.getUniqueId().equals(player.getUniqueId())) {
            return;
        }
        if (!activeDuel.involves(player2.getUniqueId())) {
            return;
        }
        activeDuel.recordHit(player2.getUniqueId(), entityDamageByEntityEvent.getFinalDamage());
    }

    @EventHandler(ignoreCancelled=true)
    public void onHunger(FoodLevelChangeEvent foodLevelChangeEvent) {
        HumanEntity humanEntity = foodLevelChangeEvent.getEntity();
        if (humanEntity instanceof Player) {
            Player player = (Player)humanEntity;
            ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(player.getUniqueId());
            if (activeDuel != null && foodLevelChangeEvent.getFoodLevel() < player.getFoodLevel() && !activeDuel.getArena().isAllowHungerLoss()) {
                foodLevelChangeEvent.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onDrop(PlayerDropItemEvent playerDropItemEvent) {
        UUID uUID = playerDropItemEvent.getPlayer().getUniqueId();
        if (this.plugin.getDuelManager().isInDuel(uUID) || this.plugin.getPartyManager().inPartyMatch(uUID)) {
            playerDropItemEvent.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onSneak(PlayerToggleSneakEvent playerToggleSneakEvent) {
        if (playerToggleSneakEvent.isSneaking()) {
            this.plugin.getDuelManager().markReady(playerToggleSneakEvent.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        this.plugin.getScoreboardService().handleJoin(player);
        if (this.plugin.getConfig().getBoolean("on-join-reset", true)) {
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                player.removePotionEffect(potionEffect.getType());
            }
            try {
                player.setHealth(player.getMaxHealth());
            }
            catch (Throwable throwable) {
                player.setHealth(20.0);
            }
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
            player.setExhaustion(0.0f);
            player.setFireTicks(0);
        }
        this.sendToSpawn(player);
    }

    private void sendToSpawn(Player player) {
        Location location = this.spawnPoint();
        if (location == null) {
            this.plugin.giveSpawnItems(player);
            return;
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!player.isOnline() || this.plugin.getDuelManager().isInDuel(player.getUniqueId())) {
                return;
            }
            this.unstickSpectator(player);
            player.teleport(location);
            this.plugin.giveSpawnItems(player);
        }, 2L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!player.isOnline() || this.plugin.getDuelManager().isInDuel(player.getUniqueId()) || this.plugin.getEventManager().isInvolved(player.getUniqueId()) || this.plugin.getSpectateManager().isSpectating(player.getUniqueId())) {
                return;
            }
            if (player.getLocation().getWorld() != location.getWorld()) {
                player.teleport(location);
            }
            this.plugin.giveSpawnItems(player);
        }, 22L);
    }

    private void unstickSpectator(Player player) {
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }
        if (this.plugin.getEventManager().isInvolved(player.getUniqueId()) || this.plugin.getPartyManager().inPartyMatch(player.getUniqueId()) || this.plugin.getSpectateManager().isSpectating(player.getUniqueId())) {
            return;
        }
        GameModeGuard.setFreely(player, GameMode.SURVIVAL);
    }

    private Location spawnPoint() {
        Location location = this.plugin.getDuelSpawn();
        if (location != null) {
            return location;
        }
        if (Bukkit.getWorlds().isEmpty()) {
            return null;
        }
        return ((World)Bukkit.getWorlds().get(0)).getSpawnLocation();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getPlayer();
        this.plugin.getSetupManager().cancel(player);
        this.plugin.getSpectateManager().clearOnQuit(player);
        this.plugin.getQueueManager().remove(player.getUniqueId());
        this.plugin.getPartyManager().handleQuit(player.getUniqueId());
        if (this.plugin.getDuelManager().isInDuel(player.getUniqueId())) {
            this.plugin.getDuelManager().handleDisconnect(player.getUniqueId());
        }
        GameModeGuard.release(player.getUniqueId());
        AntiCheatBypass.release(this.plugin, player);
    }
}

