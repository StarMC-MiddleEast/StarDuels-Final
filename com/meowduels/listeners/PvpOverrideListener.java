package com.meowduels.listeners;

import com.meowduels.MeowDuels;
import com.meowduels.managers.EventManager;
import com.meowduels.model.ActiveDuel;
import com.meowduels.model.Party;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class PvpOverrideListener
implements Listener {
    private final MeowDuels plugin;

    public PvpOverrideListener(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    // Apply the configured in-match PvP policy after region/protection handlers.
    @EventHandler(priority=EventPriority.MONITOR)
    public void onPvp(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        this.revive(entityDamageByEntityEvent);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPrePlayerAttack(PrePlayerAttackEntityEvent event) {
        if (!(event.getAttacked() instanceof Player target) || !event.willAttack()) return;
        UUID attackerId = event.getPlayer().getUniqueId();
        UUID targetId = target.getUniqueId();
        if (this.isActiveDuel(attackerId, targetId)
            || this.plugin.getConfig().getBoolean("pvp.force-in-match", true) && this.allowed(attackerId, targetId)) {
            event.setCancelled(false);
        }
    }

    private void revive(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        if (!this.plugin.getConfig().getBoolean("pvp.force-in-match", true)) {
            return;
        }
        if (!(entityDamageByEntityEvent.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player)entityDamageByEntityEvent.getEntity();
        Player player2 = PvpOverrideListener.shooter(entityDamageByEntityEvent.getDamager());
        if (player2 == null || player2.getUniqueId().equals(player.getUniqueId())) {
            return;
        }
        UUID attackerId = player2.getUniqueId();
        UUID targetId = player.getUniqueId();
        if (this.isActiveDuel(attackerId, targetId)
            || this.plugin.getConfig().getBoolean("pvp.force-in-match", true) && this.allowed(attackerId, targetId)) {
            entityDamageByEntityEvent.setCancelled(false);
        }
    }

    private boolean isActiveDuel(UUID attacker, UUID target) {
        ActiveDuel duel = this.plugin.getDuelManager().getDuel(target);
        return duel != null && duel.getState() == ActiveDuel.State.FIGHTING && duel.involves(attacker);
    }

    private static Player shooter(Entity entity) {
        ProjectileSource projectileSource;
        if (entity instanceof Player) {
            return (Player)entity;
        }
        if (entity instanceof Projectile && (projectileSource = ((Projectile)entity).getShooter()) instanceof Player) {
            return (Player)projectileSource;
        }
        return null;
    }

    private boolean allowed(UUID uUID, UUID uUID2) {
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(uUID2);
        if (activeDuel != null) {
            return activeDuel.getState() == ActiveDuel.State.FIGHTING && activeDuel.involves(uUID);
        }
        Party party = this.plugin.getPartyManager().partyOf(uUID2);
        if (party != null && party.isFighting() && !party.isFinished()) {
            return this.allowedInParty(party, uUID, uUID2);
        }
        EventManager eventManager = this.plugin.getEventManager();
        return eventManager.isRunning() && eventManager.isPlaying(uUID2) && eventManager.isPlaying(uUID);
    }

    private boolean allowedInParty(Party party, UUID uUID, UUID uUID2) {
        if (party.isCountingDown()) {
            return false;
        }
        if (!party.getAlive().contains(uUID) || !party.getAlive().contains(uUID2)) {
            return false;
        }
        if (!party.isTeamMode()) {
            return true;
        }
        Party.Team team = party.teamOf(uUID);
        return team == null || team != party.teamOf(uUID2);
    }
}

