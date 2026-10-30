package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.model.Arena;
import com.meowduels.model.Kit;
import com.meowduels.model.Party;
import com.meowduels.model.PartyDuelRequest;
import com.meowduels.model.PartyMode;
import com.meowduels.model.PlayerSnapshot;
import com.meowduels.util.AntiCheatBypass;
import com.meowduels.util.Colors;
import com.meowduels.util.Cooldowns;
import com.meowduels.util.GameModeGuard;
import com.meowduels.util.MiniMessageUtil;
import com.meowduels.util.Sounds;
import com.meowduels.util.SpawnItems;
import com.meowduels.util.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Duration;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

public class PartyManager {
    private final MeowDuels plugin;
    private final Map<UUID, Party> byPlayer = new ConcurrentHashMap<UUID, Party>();
    private final Set<Party> pendingFinish = new HashSet<Party>();
    private static final String INVITE_COOLDOWN = "party-invite";
    private static final String CHALLENGE_COOLDOWN = "party-duel-challenge";
    private final Map<UUID, Map<UUID, PartyDuelRequest>> duelRequests = new HashMap<UUID, Map<UUID, PartyDuelRequest>>();

    public PartyManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    public Party partyOf(UUID uUID) {
        return uUID == null ? null : this.byPlayer.get(uUID);
    }

    public boolean inParty(UUID uUID) {
        return this.byPlayer.containsKey(uUID);
    }

    public boolean inPartyMatch(UUID uUID) {
        Party party = this.partyOf(uUID);
        return party != null && party.isFighting() && party.involved().contains(uUID);
    }

    public Object matchOf(UUID uUID) {
        Party party = this.partyOf(uUID);
        if (party == null || !party.isFighting() || !party.involved().contains(uUID)) {
            return null;
        }
        return this.matchHostOf(party);
    }

    public boolean isWatching(UUID uUID) {
        Party party = this.partyOf(uUID);
        return party != null && party.getWatching().contains(uUID);
    }

    public Set<String> claimedArenas() {
        HashSet<String> hashSet = new HashSet<String>();
        for (Party party : this.byPlayer.values()) {
            if (!party.isFighting() || party.getArena() == null) continue;
            hashSet.add(party.getArena().getName().toLowerCase(Locale.ROOT));
        }
        return hashSet;
    }

    public List<String> invitersOf(UUID uUID) {
        ArrayList<String> arrayList = new ArrayList<String>();
        if (uUID == null) {
            return arrayList;
        }
        for (Party party : new HashSet<Party>(this.byPlayer.values())) {
            Player player;
            if (!party.isInvited(uUID) || (player = Bukkit.getPlayer((UUID)party.getLeader())) == null) continue;
            arrayList.add(player.getName());
        }
        return arrayList;
    }

    public List<String> publicPartyLeaders() {
        java.util.TreeSet<String> names = new java.util.TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (Party party : new HashSet<Party>(this.byPlayer.values())) {
            if (!party.isOpenToAll() || party.isFighting() || party.isFinished()) {
                continue;
            }
            Player leader = Bukkit.getPlayer(party.getLeader());
            if (leader == null || party.size() >= this.maxPartySize(leader)) {
                continue;
            }
            names.add(leader.getName());
        }
        return new ArrayList<String>(names);
    }

    public boolean togglePublic(Player player) {
        Party party = this.partyOf(player.getUniqueId());
        if (party == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-not-party", new String[0]));
            return false;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-change", new String[0]));
            return false;
        }
        if (party.isFighting()) {
            player.sendMessage(this.plugin.messages().get("notice.party.t-invite-during-party", new String[0]));
            return false;
        }
        party.setOpenToAll(!party.isOpenToAll());
        this.broadcast(party, party.isOpenToAll()
            ? "&aThis party is now public. Use &f/party join " + player.getName() + "&a to join."
            : "&eThis party is now private.");
        return true;
    }

    public boolean promote(Player player) {
        Party party = this.partyOf(player.getUniqueId());
        if (party == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-not-party", new String[0]));
            return false;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-change", new String[0]));
            return false;
        }
        if (party.isFighting()) {
            player.sendMessage(this.plugin.messages().get("notice.party.t-invite-during-party", new String[0]));
            return false;
        }
        party.setOpenToAll(true);
        TextComponent announcement = new TextComponent("✦ " + Text.smallCaps(player.getName()) + " IS HOSTING A PARTY  •  CLICK TO JOIN ✦");
        announcement.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        announcement.setBold(true);
        announcement.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + player.getName()));
        announcement.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder(Text.smallCaps("JOIN " + player.getName() + "'S PARTY")).color(net.md_5.bungee.api.ChatColor.YELLOW).create()));
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.spigot().sendMessage(announcement);
        }
        return true;
    }

    public void decline(Player player, Player player2) {
        Party party = this.partyOf(player2.getUniqueId());
        if (party == null || !party.getInvited().remove(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.have-no-invite-from", "leader", player2.getName()));
            return;
        }
        Sounds.deny(player);
        player.sendMessage(this.msg("party.invite-declined", "leader", player2.getName()));
        player2.sendMessage(this.msg("party.invite-declined-by", "player", player.getName()));
    }

    public Party matchInArena(Arena arena) {
        if (arena == null || this.byPlayer.isEmpty()) {
            return null;
        }
        for (Party party : this.byPlayer.values()) {
            if (!party.isFighting() || party.isFinished() || party.getArena() != arena) continue;
            return party;
        }
        return null;
    }

    public int partyCount() {
        HashSet<Party> hashSet = new HashSet<Party>(this.byPlayer.values());
        return hashSet.size();
    }

    public void create(Player player) {
        UUID uUID = player.getUniqueId();
        if (this.inParty(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-already-party", new String[0]));
            return;
        }
        if (this.busyElsewhere(player)) {
            return;
        }
        Party party = new Party(uUID);
        this.byPlayer.put(uUID, party);
        player.sendMessage(this.plugin.messages().get("notice.party.party-created-invite-someone", new String[0]));
        Sounds.partyCreate(player);
        this.refreshItems(player);
        this.plugin.getTabService().attachParty(party);
    }

    public void invite(Player player, Player player2) {
        Party party = this.partyOf(player.getUniqueId());
        if (party == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-not-party-6", new String[0]));
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-invite-2", new String[0]));
            return;
        }
        if (party.isFighting()) {
            player.sendMessage(this.plugin.messages().get("notice.party.t-invite-during-party", new String[0]));
            return;
        }
        if (party.size() >= this.maxPartySize(player)) {
            player.sendMessage(Text.color("&eYour party is full. &fYour limit is &6" + this.maxPartySize(player) + "&f players."));
            return;
        }
        UUID uUID = player2.getUniqueId();
        if (party.has(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.they-re-already-party", new String[0]));
            return;
        }
        if (this.inParty(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.already-party", "target", player2.getName()));
            return;
        }
        if (this.busyElsewhere(player)) {
            return;
        }
        if (this.busyTarget(player, player2)) {
            return;
        }
        if (party.isInvited(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.already-invite", "target", player2.getName()));
            return;
        }
        if (!this.plugin.getPlayerSettings().isPartyInvites(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.party-invites-turned-off", "target", player2.getName()));
            Sounds.deny(player);
            return;
        }
        long l = Cooldowns.remaining(player, INVITE_COOLDOWN);
        if (l > 0L) {
            player.sendMessage(this.plugin.messages().get("notice.party.wait-s-before-inviting", "value", String.valueOf(Cooldowns.seconds(l))));
            Sounds.deny(player);
            return;
        }
        Cooldowns.start(player, INVITE_COOLDOWN, null, (long)(this.plugin.getConfig().getDouble("party.invite-cooldown-seconds", 5.0) * 1000.0));
        party.invite(uUID);
        player.sendMessage(this.msg("party.invite-sent", "player", player2.getName()));
        this.sendInviteCard(player2, player, party);
    }

    public void accept(Player player, Player player2) {
        Party party = this.partyOf(player2.getUniqueId());
        UUID uUID = player.getUniqueId();
        if (party == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.party-no-longer-exists", new String[0]));
            return;
        }
        if (this.inParty(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.leave-current-party-first", new String[0]));
            return;
        }
        if (!party.isInvited(uUID) && !party.isOpenToAll()) {
            player.sendMessage(this.plugin.messages().get("notice.party.haven-t-been-invited", new String[0]));
            return;
        }
        if (party.isFighting()) {
            player.sendMessage(this.plugin.messages().get("notice.party.party-mid-match-try", new String[0]));
            return;
        }
        Player player3 = Bukkit.getPlayer((UUID)party.getLeader());
        if (party.size() >= this.maxPartySize(player3)) {
            player.sendMessage(Text.color("&eThat party is full."));
            if (player3 != null) {
                player3.sendMessage(Text.color("&eYour party is full. &fYour limit is &6" + this.maxPartySize(player3) + "&f players."));
            }
            return;
        }
        if (this.busyElsewhere(player)) {
            return;
        }
        party.add(uUID);
        this.byPlayer.put(uUID, party);
        Sounds.join(player);
        for (UUID uUID2 : party.getMembers()) {
            Player player4;
            if (uUID2.equals(uUID) || (player4 = Bukkit.getPlayer((UUID)uUID2)) == null) continue;
            Sounds.join(player4);
        }
        this.broadcast(party, "&f" + player.getName() + "&a joined the party.");
        this.refreshItems(player);
        this.plugin.getTabService().attachParty(party);
        player.sendMessage(this.plugin.messages().get("notice.party.party-run-by-they", "leader", player2.getName()));
    }

    private void sendInviteCard(Player player, Player player2, Party party) {
        player.sendMessage("");
        Sounds.invite(player);
        player.sendMessage(this.msg("party.invite-header", new String[0]));
        player.sendMessage(this.msg("party.invite-from", "leader", player2.getName()));
        player.sendMessage(this.msg("party.invite-info", "members", String.valueOf(party.size())));
        player.sendMessage("");
        TextComponent textComponent = new TextComponent(this.msg("party.invite-accept", new String[0]));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + player2.getName()));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.msg("party.invite-accept-hover", "leader", player2.getName())).create()));
        TextComponent textComponent2 = new TextComponent(this.msg("party.invite-gap", new String[0]));
        TextComponent textComponent3 = new TextComponent(this.msg("party.invite-decline", new String[0]));
        textComponent3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party decline " + player2.getName()));
        textComponent3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.msg("party.invite-decline-hover", "leader", player2.getName())).create()));
        player.spigot().sendMessage(new BaseComponent[]{textComponent, textComponent2, textComponent3});
        player.sendMessage("");
        Sounds.request(player);
    }

    public void leave(Player player) {
        boolean bl;
        UUID uUID = player.getUniqueId();
        Party party = this.partyOf(uUID);
        if (party == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-not-party-5", new String[0]));
            return;
        }
        if (party.isLeader(uUID)) {
            this.disband(party, player.getName() + " disbanded the party.");
            return;
        }
        boolean bl2 = bl = party.isFighting() && party.involved().contains(uUID);
        if (bl) {
            this.dropFromMatch(party, uUID);
        }
        party.remove(uUID);
        this.byPlayer.remove(uUID);
        this.broadcast(party, "&f" + player.getName() + "&7 left the party.");
        Sounds.leave(player);
        player.sendMessage(this.plugin.messages().get("notice.party.left-party", new String[0]));
        this.refreshItems(player);
        this.plugin.getTabService().detach(uUID);
        this.reattachTab(party);
        this.checkWin(party);
    }

    public void disband(Party party, String string) {
        if (party == null) {
            return;
        }
        if (party.isFighting()) {
            this.endMatch(party, null);
        }
        this.plugin.getTabService().detachParty(party);
        for (UUID uUID : new ArrayList<UUID>(party.getMembers())) {
            this.byPlayer.remove(uUID);
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            player.sendMessage(this.plugin.messages().get("notice.party.message-2", "reason", string));
            this.refreshItems(player);
        }
        party.getMembers().clear();
    }

    public void startMatch(Player player) {
        this.startMatch(player, PartyMode.FFA);
    }

    public void startMatch(Player player, PartyMode partyMode) {
        Kit kit;
        Party party = this.partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-start-6", new String[0]));
            return;
        }
        if (party.isFighting()) {
            player.sendMessage(this.plugin.messages().get("notice.party.party-already-fighting-2", new String[0]));
            return;
        }
        if (party.size() < 2) {
            player.sendMessage(this.plugin.messages().get("notice.party.need-at-least-2", new String[0]));
            return;
        }
        Kit kit2 = kit = party.getKit() == null ? null : this.plugin.getKitManager().get(party.getKit());
        if (kit == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.pick-kit-first-2", new String[0]));
            return;
        }
        ArrayList<Player> arrayList = new ArrayList<Player>();
        for (UUID object : party.getMembers()) {
            Player player2 = Bukkit.getPlayer((UUID)object);
            if (player2 == null) continue;
            arrayList.add(player2);
        }
        if (arrayList.size() < 2) {
            player.sendMessage(this.plugin.messages().get("notice.party.not-enough-party-members", new String[0]));
            return;
        }
        Arena arena2 = this.plugin.getArenaManager().findFreeArena(arena -> this.plugin.getDuelManager().isArenaInUse(arena.getName()) || !arena.supportsKit(kit.getName()));
        if (arena2 == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.no-free-arena-supports-2", new String[0]));
            player.sendMessage(this.plugin.messages().get("notice.party.message", "value", String.valueOf(this.plugin.getDuelManager().arenaAvailability(kit.getName()))));
            return;
        }
        if (partyMode == PartyMode.SPLIT) {
            for (Player player2 : arrayList) {
                if (party.teamOf(player2.getUniqueId()) != null) continue;
                party.setTeam(player2.getUniqueId(), party.teamMembers(Party.Team.AQUA).size() <= party.teamMembers(Party.Team.RED).size() ? Party.Team.AQUA : Party.Team.RED);
            }
            if (this.onlineOn(party, arrayList, Party.Team.AQUA) == 0 || this.onlineOn(party, arrayList, Party.Team.RED) == 0) {
                player.sendMessage(this.plugin.messages().get("notice.party.both-teams-need-at", new String[0]));
                return;
            }
        }
        party.setMode(partyMode);
        party.setArena(arena2);
        party.setState(Party.State.FIGHTING);
        party.setFinished(false);
        party.setStartedAt(System.currentTimeMillis());
        party.getAlive().clear();
        party.getWatching().clear();
        party.getSnapshots().clear();
        this.plugin.getDuelManager().markArenaInUse(arena2.getName());
        this.plugin.getArenaManager().clearLooseEntities(arena2);
        for (Player player2 : arrayList) {
            this.sendIn(party, player2, kit, arena2);
        }
        if (partyMode == PartyMode.SPLIT) {
            this.announceTeams(party);
        }
        this.plugin.getTabService().refreshParty(party);
        int n = Math.max(1, this.plugin.getConfig().getInt("party.countdown-seconds", 5));
        party.setFightStartsAt(System.currentTimeMillis() + (long)n * 1000L);
        this.broadcast(party, this.msg("party.match-started", "count", String.valueOf(arrayList.size()), "arena", arena2.getName()));
        this.countdownTick(party, n, n * 20);
    }

    private Location partySpawn(Arena arena) {
        Location location = arena.getEventSpawn();
        if (location != null) {
            return arena.safeSpawn(location);
        }
        World world = arena.getWorld();
        Location location2 = arena.getMin();
        Location location3 = arena.getMax();
        if (world == null || location2 == null || location3 == null) {
            return null;
        }
        int n = (location2.getBlockX() + location3.getBlockX()) / 2;
        int n2 = (location2.getBlockZ() + location3.getBlockZ()) / 2;
        return arena.safeSpawn(new Location(world, n + 0.5, location2.getBlockY(), n2 + 0.5));
    }

    private Location partyFfaSpawn(Arena arena) {
        World world = arena.getWorld();
        Location min = arena.getMin();
        Location max = arena.getMax();
        if (world == null || min == null || max == null) return null;
        int x = (min.getBlockX() + max.getBlockX()) / 2;
        int z = (min.getBlockZ() + max.getBlockZ()) / 2;
        Location configured = arena.getEventSpawn();
        Location center = arena.safeSpawn(new Location(world, x + 0.5, min.getBlockY(), z + 0.5,
                configured == null ? 0.0f : configured.getYaw(),
                configured == null ? 0.0f : configured.getPitch()));
        // If the center is obstructed or has no floor, prefer a configured
        // side spawn over placing the whole FFA on the arena roof.
        return center != null ? center : arena.safeSpawn(arena.getSpawn1());
    }

    private void sendIn(Party party, Player player, Kit kit, Arena arena) {
        this.sendIn(party, player, kit, arena, true);
    }

    private void sendIn(Party party, Player player, Kit kit, Arena arena, boolean bl) {
        Party.Team team;
        UUID uUID = player.getUniqueId();
        int n = party.getAlive().size();
        party.getAlive().add(uUID);
        if (bl) {
            party.getSnapshots().put(uUID, PlayerSnapshot.capture(player));
        }
        Party.Team team2 = team = party.isTeamMode() ? party.teamOf(player.getUniqueId()) : null;
        Location location = party.getMode() == PartyMode.FFA
                ? this.partyFfaSpawn(arena)
                : arena.safeSpawn(team == null ? (n % 2 == 0 ? arena.getSpawn1() : arena.getSpawn2())
                : (team == Party.Team.AQUA ? arena.getSpawn1() : arena.getSpawn2()));
        if (location == null) {
            location = this.partySpawn(arena);
        }
        String string = location != null && location.getWorld() != null ? location.getWorld().getName() : null;
        AntiCheatBypass.grant(this.plugin, player, AntiCheatBypass.worldNodes(this.plugin, string));
        if (location != null) {
            player.teleport(location);
        }
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        GameModeGuard.pin(player, GameMode.SURVIVAL);
        try {
            player.setHealth(player.getMaxHealth());
        }
        catch (Throwable throwable) {
            player.setHealth(20.0);
        }
        player.setFoodLevel(20);
        player.setSaturation(0.0f);
        player.setExhaustion(0.0f);
        player.setFireTicks(0);
        kit.applyTo(player);
        Kit kit2 = kit;
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            Player current = Bukkit.getPlayer((UUID)uUID);
            if (current != null) {
                kit2.applyOffhand(current);
            }
        }, 1L);
    }

    private void countdownTick(Party party, int n, int n2) {
        if (!party.isFighting() || party.isFinished()) {
            return;
        }
        long l = party.getFightStartsAt();
        if (n2 <= 0) {
            Kit kit = party.getKit() == null ? null : this.plugin.getKitManager().get(party.getKit());
            for (UUID uUID : party.getAlive()) {
                Player player = Bukkit.getPlayer((UUID)uUID);
                if (player == null) continue;
                if (kit != null) {
                    kit.applyStartEffects(player);
                }
                this.showTitle(player, this.msg("party.countdown-go", new String[0]), "", 0, 20, 10);
                Sounds.fight(player);
            }
            return;
        }
        int n3 = (n2 + 19) / 20;
        boolean bl = n2 % 20 == 0;
        for (UUID uUID : party.getAlive()) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            if (!bl) continue;
            this.showTitle(player, this.msg("party.countdown-title", "seconds", String.valueOf(n3)), this.countdownSubtitle(party, uUID), 0, 25, 0);
            Sounds.tick(player, n3, n);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (party.getFightStartsAt() == l) {
                this.countdownTick(party, n, n2 - 10);
            }
        }, 10L);
    }

    private String countdownSubtitle(Party party, UUID uUID) {
        if (!party.isTeamMode()) {
            return this.msg("party.countdown-subtitle", new String[0]);
        }
        Party.Team team = party.teamOf(uUID);
        if (team == null) {
            return this.msg("party.countdown-subtitle-split-noteam", new String[0]);
        }
        return this.msg("party.countdown-subtitle-split", "team", this.teamName(team));
    }

    public void onDeath(UUID uUID, UUID uUID2) {
        Party party = this.partyOf(uUID);
        if (party == null || !party.isFighting() || !party.getAlive().contains(uUID)) {
            return;
        }
        if (uUID2 != null && !uUID2.equals(uUID)) {
            party.addKill(uUID2);
        }
        this.markOut(party, uUID);
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            this.watchFrom(party, player);
            player.sendMessage(this.plugin.messages().get("notice.party.re-out-watching-rest", new String[0]));
        }
        String string = String.valueOf(party.getAlive().size());
        if (player != null) {
            Sounds.death(player);
        }
        for (UUID uUID3 : party.getAlive()) {
            Player player2 = Bukkit.getPlayer((UUID)uUID3);
            if (player2 == null) continue;
            if (uUID3.equals(uUID2)) {
                Sounds.kill(player2);
                continue;
            }
            Sounds.eliminated(player2);
        }
        if (uUID2 != null && !uUID2.equals(uUID) && party.has(uUID2)) {
            this.broadcastMatch(party, this.msg("party.kill-pvp", "victim", this.nameOf(uUID), "killer", this.nameOf(uUID2), "alive", string));
        } else {
            this.broadcastMatch(party, this.msg("party.kill-generic", "victim", this.nameOf(uUID), "alive", string));
        }
        this.checkWin(party);
    }

    private void watchFrom(Party party, Player player) {
        Location location;
        GameModeGuard.release(player.getUniqueId());
        Location location2 = location = party.getArena() == null ? null : this.partySpawn(party.getArena());
        if (location != null) {
            player.teleport(location);
        }
        GameModeGuard.setFreely(player, GameMode.SPECTATOR);
        GameModeGuard.pin(player, GameMode.SPECTATOR);
    }

    public void spectate(Player player) {
        Party party = this.partyOf(player.getUniqueId());
        if (party == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-not-party-4", new String[0]));
            return;
        }
        if (!party.isFighting()) {
            player.sendMessage(this.plugin.messages().get("notice.party.party-isn-t-fighting", new String[0]));
            return;
        }
        UUID uUID = player.getUniqueId();
        if (party.getAlive().contains(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-match-t-spectate", new String[0]));
            return;
        }
        if (!party.getWatching().add(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-already-watching-leave", new String[0]));
            return;
        }
        for (Party party2 : this.sides(party)) {
            party2.getWatching().add(uUID);
        }
        this.matchHostOf(party).getSnapshots().put(uUID, PlayerSnapshot.capture(player));
        this.watchFrom(party, player);
        player.sendMessage(this.plugin.messages().get("notice.party.spectating-party-match-leave", new String[0]));
    }

    public void forceEnd(Player player) {
        Party party = this.partyOf(player.getUniqueId());
        if (party == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-not-party-3", new String[0]));
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-end", new String[0]));
            return;
        }
        if (!party.isFighting() || party.isFinished()) {
            player.sendMessage(this.plugin.messages().get("notice.party.party-isn-t-match", new String[0]));
            return;
        }
        this.broadcast(party, this.msg("party.force-ended", "leader", player.getName()));
        this.endMatch(party, null, false);
    }

    public List<Party> opponentParties(Party party) {
        ArrayList<Party> arrayList = new ArrayList<Party>();
        HashSet<Party> hashSet = new HashSet<Party>();
        for (Party party2 : this.byPlayer.values()) {
            if (party2 == party || !hashSet.add(party2) || party2.isFighting() || party2.size() < 1 || Bukkit.getPlayer((UUID)party2.getLeader()) == null) continue;
            arrayList.add(party2);
        }
        return arrayList;
    }

    public Party partyLedBy(UUID uUID) {
        if (uUID == null) {
            return null;
        }
        Party party = this.partyOf(uUID);
        return party != null && party.isLeader(uUID) ? party : null;
    }

    public void challengeDuels(Player player) {
        PartyDuelRequest partyDuelRequest;
        Kit kit;
        Party party = this.partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-start-5", new String[0]));
            return;
        }
        if (party.isFighting()) {
            player.sendMessage(this.plugin.messages().get("notice.party.party-already-fighting", new String[0]));
            return;
        }
        Party party2 = this.partyLedBy(party.getDuelTarget());
        if (party2 == null || party2 == party) {
            player.sendMessage(this.plugin.messages().get("notice.party.party-gone-pick-another-2", new String[0]));
            return;
        }
        if (party2.isFighting()) {
            player.sendMessage(this.plugin.messages().get("notice.party.party-just-started-match", new String[0]));
            return;
        }
        Kit kit2 = kit = party.getKit() == null ? null : this.plugin.getKitManager().get(party.getKit());
        if (kit == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.pick-kit-first", new String[0]));
            return;
        }
        List<Player> list = this.onlineMembers(party);
        List<Player> list2 = this.onlineMembers(party2);
        if (list.isEmpty() || list2.isEmpty()) {
            player.sendMessage(this.plugin.messages().get("notice.party.both-parties-need-someone-2", new String[0]));
            return;
        }
        Player player2 = Bukkit.getPlayer((UUID)party2.getLeader());
        if (player2 == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.their-leader-offline", new String[0]));
            return;
        }
        Map<UUID, PartyDuelRequest> map = this.duelRequests.get(party2.getLeader());
        PartyDuelRequest partyDuelRequest2 = partyDuelRequest = map == null ? null : map.get(player.getUniqueId());
        if (partyDuelRequest != null && !partyDuelRequest.isExpired(this.duelRequestTtl())) {
            player.sendMessage(this.plugin.messages().get("notice.party.they-already-have-challenge", new String[0]));
            Sounds.deny(player);
            return;
        }
        long l = Cooldowns.remaining(player, CHALLENGE_COOLDOWN);
        if (l > 0L) {
            player.sendMessage(this.plugin.messages().get("notice.party.wait-s-before-challenging", "value", String.valueOf(Cooldowns.seconds(l))));
            Sounds.deny(player);
            return;
        }
        Cooldowns.start(player, CHALLENGE_COOLDOWN, null, (long)(this.plugin.getConfig().getDouble("party.invite-cooldown-seconds", 5.0) * 1000.0));
        int n = party.getDuelRounds();
        this.duelRequests.computeIfAbsent(party2.getLeader(), uUID -> new HashMap()).put(player.getUniqueId(), new PartyDuelRequest(player.getUniqueId(), party2.getLeader(), kit.getName(), n));
        int n2 = Math.min(list.size(), list2.size());
        player.sendMessage(this.msg("party.duel-sent", "party", player2.getName(), "kit", this.kitLabel(kit.getName()), "rounds", String.valueOf(n)));
        this.sendDuelChallengeCard(player2, player, party, kit.getName(), n, n2);
    }

    public List<String> duelChallengers(UUID uUID) {
        ArrayList<String> arrayList = new ArrayList<String>();
        Map<UUID, PartyDuelRequest> map = this.duelRequests.get(uUID);
        if (map == null) {
            return arrayList;
        }
        for (PartyDuelRequest partyDuelRequest : map.values()) {
            Player player;
            if (partyDuelRequest.isExpired(this.duelRequestTtl()) || (player = Bukkit.getPlayer((UUID)partyDuelRequest.getFrom())) == null) continue;
            arrayList.add(player.getName());
        }
        return arrayList;
    }

    public void declineDuels(Player player, Player player2) {
        Map<UUID, PartyDuelRequest> map = this.duelRequests.get(player.getUniqueId());
        if (map == null || map.remove(player2.getUniqueId()) == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.no-challenge-from", "from", player2.getName()));
            return;
        }
        Sounds.deny(player);
        player.sendMessage(this.plugin.messages().get("notice.party.turned-down-s-party", "from", player2.getName()));
        player2.sendMessage(this.plugin.messages().get("notice.party.turned-down-party-duel", "leader", player.getName()));
    }

    public void acceptDuels(Player player, Player player2) {
        PartyDuelRequest partyDuelRequest;
        Map<UUID, PartyDuelRequest> map = this.duelRequests.get(player.getUniqueId());
        PartyDuelRequest partyDuelRequest2 = partyDuelRequest = map == null ? null : map.get(player2.getUniqueId());
        if (partyDuelRequest == null || partyDuelRequest.isExpired(this.duelRequestTtl())) {
            player.sendMessage(this.plugin.messages().get("notice.party.challenge-expired", new String[0]));
            return;
        }
        map.remove(player2.getUniqueId());
        Party party = this.partyLedBy(player.getUniqueId());
        Party party2 = this.partyLedBy(player2.getUniqueId());
        if (party == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.need-leading-party-accept", new String[0]));
            return;
        }
        if (party2 == null || party2 == party) {
            player.sendMessage(this.plugin.messages().get("notice.party.their-party-gone", new String[0]));
            return;
        }
        if (party.isFighting() || party2.isFighting()) {
            player.sendMessage(this.plugin.messages().get("notice.party.one-parties-already-match", new String[0]));
            return;
        }
        Kit kit = this.plugin.getKitManager().get(partyDuelRequest.getKit());
        if (kit == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.kit-no-longer-exists", new String[0]));
            return;
        }
        List<Player> list = this.onlineMembers(party);
        List<Player> list2 = this.onlineMembers(party2);
        if (list.isEmpty() || list2.isEmpty()) {
            player.sendMessage(this.plugin.messages().get("notice.party.both-parties-need-someone", new String[0]));
            return;
        }
        Arena arena2 = this.plugin.getArenaManager().findFreeArena(arena -> this.plugin.getDuelManager().isArenaInUse(arena.getName()) || !arena.supportsKit(kit.getName()));
        if (arena2 == null) {
            String string = this.plugin.messages().get("notice.party.no-free-arena-supports", new String[0]);
            player.sendMessage(string);
            player2.sendMessage(string);
            return;
        }
        this.startTeamMatch(party2, party, arena2, kit, partyDuelRequest.getRounds());
    }

    private void startTeamMatch(Party party, Party party2, Arena arena, Kit kit, int n) {
        List<Player> list = this.onlineMembers(party);
        List<Player> list2 = this.onlineMembers(party2);
        HashMap<UUID, Party.Team> hashMap = new HashMap<UUID, Party.Team>();
        for (Player player : list) {
            hashMap.put(player.getUniqueId(), Party.Team.AQUA);
        }
        for (Player player : list2) {
            hashMap.put(player.getUniqueId(), Party.Team.RED);
        }
        HashSet hashSet = new HashSet(hashMap.keySet());
        long l = System.currentTimeMillis();
        int n2 = Math.max(1, this.plugin.getConfig().getInt("party.countdown-seconds", 5));
        for (Party party3 : new Party[]{party, party2}) {
            party3.setMode(PartyMode.DUELS);
            party3.setKit(kit.getName());
            party3.setArena(arena);
            party3.setState(Party.State.FIGHTING);
            party3.setFinished(false);
            party3.setStartedAt(l);
            party3.setFightStartsAt(l + (long)n2 * 1000L);
            party3.setOpponent(party3 == party ? party2 : party);
            party3.setMatchHost(party3 == party);
            party3.setRoundsToWin(n);
            party3.setRound(1);
            party3.getAlive().clear();
            party3.getAlive().addAll(hashSet);
            party3.getWatching().clear();
            party3.getSnapshots().clear();
            party3.getTeams().clear();
            party3.getTeams().putAll(hashMap);
        }
        this.plugin.getDuelManager().markArenaInUse(arena.getName());
        this.plugin.getArenaManager().clearLooseEntities(arena);
        for (Player player : list) {
            this.sendIn(party, player, kit, arena);
        }
        for (Player player : list2) {
            this.sendIn(party, player, kit, arena);
        }
        this.plugin.getTabService().attachMatch(party, party2);
        this.announceTeams(party);
        this.broadcastMatch(party, this.msg("party.match-started", "count", String.valueOf(hashSet.size()), "arena", arena.getName()));
        this.countdownTick(party, n2, n2 * 20);
    }

    private Party matchHostOf(Party party) {
        if (party == null) {
            return null;
        }
        return party.isMatchHost() || party.getOpponent() == null ? party : party.getOpponent();
    }

    private Party[] sides(Party party) {
        Party[] partyArray;
        Party party2;
        Party party3 = party2 = party == null ? null : party.getOpponent();
        if (party2 == null) {
            Party[] partyArray2 = new Party[1];
            partyArray = partyArray2;
            partyArray2[0] = party;
        } else {
            Party[] partyArray3 = new Party[2];
            partyArray3[0] = party;
            partyArray = partyArray3;
            partyArray3[1] = party2;
        }
        return partyArray;
    }

    private void broadcastMatch(Party party, String string) {
        for (Party party2 : this.sides(party)) {
            this.broadcast(party2, string);
        }
    }

    private void markOut(Party party, UUID uUID) {
        for (Party party2 : this.sides(party)) {
            party2.getAlive().remove(uUID);
            party2.getWatching().add(uUID);
        }
    }

    private void sweepDuelRequests() {
        if (this.duelRequests.isEmpty()) {
            return;
        }
        long l = this.duelRequestTtl();
        Iterator<Map.Entry<UUID, Map<UUID, PartyDuelRequest>>> iterator = this.duelRequests.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Map<UUID, PartyDuelRequest>> entry = iterator.next();
            Party party = this.partyLedBy(entry.getKey());
            Iterator<PartyDuelRequest> iterator2 = entry.getValue().values().iterator();
            while (iterator2.hasNext()) {
                PartyDuelRequest partyDuelRequest = iterator2.next();
                Party party2 = this.partyLedBy(partyDuelRequest.getFrom());
                if (!partyDuelRequest.isExpired(l) && party != null && party2 != null && !party.isFighting() && !party2.isFighting()) continue;
                iterator2.remove();
            }
            if (!entry.getValue().isEmpty()) continue;
            iterator.remove();
        }
    }

    private long duelRequestTtl() {
        return (long)Math.max(5.0, this.plugin.getConfig().getDouble("party.duel-request-seconds", 60.0)) * 1000L;
    }

    private void sendDuelChallengeCard(Player player, Player player2, Party party, String string, int n, int n2) {
        player.sendMessage("");
        Sounds.invite(player);
        player.sendMessage(this.msg("party.duel-header", new String[0]));
        player.sendMessage(this.msg("party.duel-from", "leader", player2.getName(), "members", String.valueOf(party.size())));
        player.sendMessage(this.msg("party.duel-terms", "kit", this.kitLabel(string), "rounds", String.valueOf(n), "pairs", String.valueOf(n2)));
        player.sendMessage("");
        TextComponent textComponent = new TextComponent(this.msg("party.duel-accept", new String[0]));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party duel accept " + player2.getName()));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.msg("party.duel-accept-hover", "leader", player2.getName())).create()));
        TextComponent textComponent2 = new TextComponent(this.msg("party.invite-gap", new String[0]));
        TextComponent textComponent3 = new TextComponent(this.msg("party.duel-decline", new String[0]));
        textComponent3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party duel decline " + player2.getName()));
        textComponent3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.msg("party.duel-decline-hover", "leader", player2.getName())).create()));
        player.spigot().sendMessage(new BaseComponent[]{textComponent, textComponent2, textComponent3});
        player.sendMessage("");
        Sounds.request(player);
    }

    private String kitLabel(String string) {
        Kit kit;
        Kit kit2 = kit = string == null ? null : this.plugin.getKitManager().get(string);
        if (kit == null) {
            return string == null ? "?" : string;
        }
        return kit.withIcon(kit.getDisplayName() == null || kit.getDisplayName().isEmpty() ? kit.getName() : kit.getDisplayName());
    }

    private List<Player> onlineMembers(Party party) {
        ArrayList<Player> arrayList = new ArrayList<Player>();
        for (UUID uUID : party.getMembers()) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            arrayList.add(player);
        }
        return arrayList;
    }

    public boolean leaveMatch(Player player) {
        UUID uUID = player.getUniqueId();
        Party party = this.partyOf(uUID);
        if (party == null || !party.isFighting() || !party.involved().contains(uUID)) {
            return false;
        }
        this.dropFromMatch(party, uUID);
        player.sendMessage(this.plugin.messages().get("notice.party.left-party-match", new String[0]));
        this.checkWin(party);
        return true;
    }

    public void recordPartyChange(Party party, Location location, BlockData blockData) {
        Party party2 = this.matchHostOf(party);
        if (party2 != null) {
            party2.recordChange(location, blockData);
        }
    }

    private void reattachTab(Party party) {
        Party party2 = this.matchHostOf(party);
        if (party.isFighting() && party2 != null && party2.getOpponent() != null) {
            this.plugin.getTabService().attachMatch(party2, party2.getOpponent());
        } else {
            this.plugin.getTabService().attachParty(party);
        }
    }

    private void dropFromMatch(Party party, UUID uUID) {
        this.pullOut(this.matchHostOf(party), uUID);
        for (Party party2 : this.sides(party)) {
            if (party2 == null) continue;
            party2.getAlive().remove(uUID);
            party2.getWatching().remove(uUID);
            party2.getTeams().remove(uUID);
        }
    }

    private void pullOut(Party party, UUID uUID) {
        Location location;
        Player player = Bukkit.getPlayer((UUID)uUID);
        PlayerSnapshot playerSnapshot = party.getSnapshots().remove(uUID);
        if (player == null) {
            return;
        }
        AntiCheatBypass.release(this.plugin, player);
        GameModeGuard.release(uUID);
        if (playerSnapshot != null) {
            playerSnapshot.restore(player);
        }
        location = this.plugin.getDuelSpawn();
        if (location == null && !Bukkit.getWorlds().isEmpty()) {
            location = Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        if (location != null) {
            player.teleport(location);
        }
        this.refreshItems(player);
        this.plugin.giveSpawnItems(player);
    }

    private void checkWin(Party party) {
        if (!party.isFighting() || party.isFinished()) {
            return;
        }
        if (party.isTeamMode()) {
            int n = party.aliveOn(Party.Team.AQUA);
            int n2 = party.aliveOn(Party.Team.RED);
            if (n > 0 && n2 > 0) {
                return;
            }
            this.roundOver(party, n > 0 ? Party.Team.AQUA : (n2 > 0 ? Party.Team.RED : null));
            return;
        }
        if (party.getAlive().size() > 1) {
            return;
        }
        UUID uUID = party.getAlive().isEmpty() ? null : party.getAlive().iterator().next();
        this.endMatch(party, uUID);
    }

    private void roundOver(Party party, Party.Team team) {
        Party party2 = this.matchHostOf(party);
        if (team == null) {
            this.endSplit(party2, null);
            return;
        }
        for (Party party3 : this.sides(party2)) {
            party3.addScore(team);
        }
        if (party2.scoreOf(team) >= party2.getRoundsToWin()) {
            this.endSplit(party2, team);
            return;
        }
        this.broadcastMatch(party2, this.msg("party.round-won", "team", this.teamName(team), "aqua", String.valueOf(party2.scoreOf(Party.Team.AQUA)), "red", String.valueOf(party2.scoreOf(Party.Team.RED)), "rounds", String.valueOf(party2.getRoundsToWin())));
        for (UUID uUID : party2.involved()) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            boolean bl = party2.teamOf(uUID) == team;
            this.showTitle(player, this.msg(bl ? "party.round-win-title" : "party.round-lose-title", new String[0]), this.msg("party.round-subtitle", "aqua", String.valueOf(party2.scoreOf(Party.Team.AQUA)), "red", String.valueOf(party2.scoreOf(Party.Team.RED))), 5, 30, 10);
            if (bl) {
                Sounds.roundWon(player);
                continue;
            }
            Sounds.roundLost(player);
        }
        // Party rounds never regenerated: blocks from round one were still
        // standing in round two. Restore them as the round ends, the same way
        // 1v1 duels always have.
        Arena roundArena = party2.getArena();
        if (roundArena != null && roundArena.isAutoRegenerate()) {
            this.plugin.getArenaManager().regenBetweenRounds(roundArena,
                    new HashMap<Location, BlockData>(party2.getChangedBlocks()));
            party2.getChangedBlocks().clear();
        }
        double d = this.plugin.getConfig().getDouble("party.round-seconds", 2.5);
        int n = party2.getRound();
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (party2.isFighting() && !party2.isFinished() && party2.getRound() == n) {
                this.nextRound(party2);
            }
        }, Math.max(1L, Math.round(d * 20.0)));
    }

    private void nextRound(Party party) {
        Kit kit = party.getKit() == null ? null : this.plugin.getKitManager().get(party.getKit());
        Arena arena = party.getArena();
        if (kit == null || arena == null) {
            this.endSplit(party, null);
            return;
        }
        ArrayList<Player> arrayList = new ArrayList<Player>();
        for (UUID uUID : party.getTeams().keySet()) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            arrayList.add(player);
        }
        int n = 0;
        int n2 = 0;
        for (Player player : arrayList) {
            if (party.teamOf(player.getUniqueId()) == Party.Team.AQUA) {
                ++n;
                continue;
            }
            ++n2;
        }
        if (n == 0 || n2 == 0) {
            this.endSplit(party, n > 0 ? Party.Team.AQUA : (n2 > 0 ? Party.Team.RED : null));
            return;
        }
        this.plugin.getArenaManager().cancelRegen(arena);
        this.plugin.getArenaManager().clearLooseEntities(arena);
        long l = System.currentTimeMillis();
        int n3 = Math.max(1, this.plugin.getConfig().getInt("party.countdown-seconds", 5));
        HashSet<UUID> hashSet = new HashSet<UUID>(party.getWatching());
        hashSet.removeAll(party.getTeams().keySet());
        for (Party party2 : this.sides(party)) {
            party2.setRound(party2.getRound() + 1);
            party2.getAlive().clear();
            party2.getWatching().clear();
            party2.getWatching().addAll(hashSet);
            party2.setStartedAt(l);
            party2.setFightStartsAt(l + (long)n3 * 1000L);
        }
        for (Player player : arrayList) {
            this.sendIn(party, player, kit, arena, false);
            party.getOpponent().getAlive().add(player.getUniqueId());
        }
        this.broadcastMatch(party, this.msg("party.round-start", "round", String.valueOf(party.getRound()), "rounds", String.valueOf(party.getRoundsToWin())));
        this.countdownTick(party, n3, n3 * 20);
    }

    private void endSplit(Party party, Party.Team team) {
        if (team != null) {
            this.broadcastMatch(party, this.msg("party.team-winner", "team", this.teamName(team)));
            for (UUID uUID : party.teamMembers(team)) {
                Player player = Bukkit.getPlayer((UUID)uUID);
                if (player == null) continue;
                Sounds.victory(player);
            }
        }
        this.endMatch(party, null, team == null);
    }

    private String teamName(Party.Team team) {
        return this.msg(team == Party.Team.AQUA ? "party.team-aqua" : "party.team-red", new String[0]);
    }

    private void announceTeams(Party party) {
        this.broadcastMatch(party, this.msg("party.teams-line", "aqua", String.valueOf(party.teamMembers(Party.Team.AQUA).size()), "red", String.valueOf(party.teamMembers(Party.Team.RED).size())));
    }

    private int onlineOn(Party party, List<Player> list, Party.Team team) {
        int n = 0;
        for (Player player : list) {
            if (party.teamOf(player.getUniqueId()) != team) continue;
            ++n;
        }
        return n;
    }

    private void endMatch(Party party, UUID uUID) {
        this.endMatch(party, uUID, true);
    }

    private void endMatch(Party party, UUID uUID, boolean bl) {
        Party party2 = this.matchHostOf(party);
        if (party2.isFinished()) {
            return;
        }
        for (Party party3 : this.sides(party2)) {
            party3.setFinished(true);
        }
        this.pendingFinish.add(party2);
        if (bl) {
            if (uUID != null) {
                this.broadcastMatch(party2, this.msg("party.winner", "winner", this.nameOf(uUID)));
                Player player = Bukkit.getPlayer((UUID)uUID);
                if (player != null) {
                    Sounds.victory(player);
                }
            } else {
                this.broadcastMatch(party2, this.msg("party.no-winner", new String[0]));
            }
        }
        double seconds = this.plugin.getConfig().getDouble("party.end-seconds", 3.0);
        int delay = (int)Math.round(Math.max(2.0, Math.min(3.0, seconds)));
        this.showClosingArenaCountdown(party2, delay);
    }

    private void showClosingArenaCountdown(Party party, int seconds) {
        if (party == null || !this.pendingFinish.contains(party)) return;
        String subtitle = "<gray>Returning to the lobby in <white>" + seconds + "</white>...";
        for (UUID id : party.involved()) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) this.showTitle(player, "<yellow>Closing the arena...", subtitle, 0, 25, 0);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (seconds <= 1) this.finishMatch(party);
            else this.showClosingArenaCountdown(party, seconds - 1);
        }, 20L);
    }

    private void finishMatch(Party party) {
        if (!this.pendingFinish.remove(party)) {
            return;
        }
        Arena arena = party.getArena();
        Set<UUID> participants = new HashSet<UUID>(party.involved());
        for (UUID uUID : participants) {
            this.pullOut(party, uUID);
        }
        if (arena != null) {
            if (arena.isAutoRegenerate()) {
                // Restores what was changed now and checks the rest over the
                // next ticks; the arena stays out of rotation until it is done.
                this.plugin.getArenaManager().regenAfterMatch(arena,
                        new HashMap<Location, BlockData>(party.getChangedBlocks()), null);
            }
            party.getChangedBlocks().clear();
            this.plugin.getDuelManager().freeArena(arena.getName());
            this.plugin.getArenaManager().clearLooseEntities(arena);
        }
        Party party2 = party.getOpponent();
        party.resetMatch();
        if (party2 != null) {
            party2.resetMatch();
        }
        this.plugin.getTabService().attachParty(party);
        if (party2 != null) {
            this.plugin.getTabService().attachParty(party2);
        }
        for (UUID id : participants) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) this.plugin.giveSpawnItems(player);
        }
    }

    public void handleQuit(UUID uUID) {
        Party party = this.partyOf(uUID);
        if (party == null) {
            return;
        }
        if (party.isLeader(uUID)) {
            this.disband(party, "The party leader left the server.");
            return;
        }
        if (party.isFighting() && party.involved().contains(uUID)) {
            for (Party party2 : this.sides(party)) {
                party2.getAlive().remove(uUID);
                party2.getWatching().remove(uUID);
                party2.getTeams().remove(uUID);
            }
            this.matchHostOf(party).getSnapshots().remove(uUID);
        }
        party.remove(uUID);
        this.byPlayer.remove(uUID);
        this.plugin.getTabService().detach(uUID);
        this.reattachTab(party);
        this.broadcast(party, "&f" + this.nameOf(uUID) + "&7 left the server.");
        this.checkWin(party);
    }

    public void tick() {
        this.sweepDuelRequests();
        if (this.byPlayer.isEmpty()) {
            return;
        }
        for (Party party : new HashSet<Party>(this.byPlayer.values())) {
            if (!party.isFighting() || party.isFinished()) continue;
            boolean bl = false;
            for (UUID uUID : party.involved()) {
                if (Bukkit.getPlayer((UUID)uUID) == null) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            this.endMatch(party, null);
        }
    }

    public void shutdown() {
        for (Party party : new HashSet<Party>(this.byPlayer.values())) {
            if (!party.isFighting()) continue;
            this.endMatch(party, null);
            this.finishMatch(party);
        }
        this.byPlayer.clear();
    }

    public boolean busy(Player player) {
        Party party = this.partyOf(player.getUniqueId());
        if (party == null) {
            return false;
        }
        if (party.isFighting()) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-party-match-type", new String[0]));
        } else {
            player.sendMessage(this.plugin.messages().get("notice.party.re-party-leave-first", new String[0]));
        }
        return true;
    }

    private boolean busyTarget(Player player, Player player2) {
        UUID uUID = player2.getUniqueId();
        String string = null;
        if (this.plugin.getDuelManager().isInDuel(uUID)) {
            string = "in a duel";
        } else if (this.plugin.getQueueManager().isQueued(uUID)) {
            string = "in a queue";
        } else if (this.plugin.getEventManager().isInvolved(uUID)) {
            string = "in an event";
        }
        if (string == null) {
            return false;
        }
        player.sendMessage(this.plugin.messages().get("notice.party.try-again-after", "target", player2.getName(), "what", string));
        return true;
    }

    private boolean busyElsewhere(Player player) {
        UUID uUID = player.getUniqueId();
        if (this.plugin.getDuelManager().isInDuel(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-duel-finish-first", new String[0]));
            return true;
        }
        if (this.plugin.getEventManager().isInvolved(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-event-type-leave", new String[0]));
            return true;
        }
        if (this.plugin.getSpectateManager().isSpectating(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-spectating-type-leave", new String[0]));
            return true;
        }
        if (this.plugin.getQueueManager().isQueued(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-queue-type-queue", new String[0]));
            return true;
        }
        return false;
    }

    private int maxPartySize(Player player) {
        if (player == null) {
            return 2;
        }
        int n = 2;
        for (int i = 3; i <= 100; ++i) {
            if (!player.hasPermission("starduels.party.size." + i)
                && !player.hasPermission("meowduels.party.size." + i)) continue;
            n = i;
        }
        return n;
    }

    private void refreshItems(Player player) {
        if (player != null) {
            SpawnItems.give(this.plugin, player);
        }
    }

    public void broadcast(Party party, String string) {
        if (party == null) {
            return;
        }
        String string2 = Colors.toSection(string);
        for (UUID uUID : party.getMembers()) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            player.sendMessage(string2);
        }
    }

    private void showTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L));
        player.showTitle(Title.title(MiniMessageUtil.deserialize(title), MiniMessageUtil.deserialize(subtitle), times));
    }

    private String msg(String string, String ... stringArray) {
        return this.plugin.messages().get(string, stringArray);
    }

    private String nameOf(UUID uUID) {
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            return player.getName();
        }
        String string = Bukkit.getOfflinePlayer((UUID)uUID).getName();
        return string == null ? "Someone" : string;
    }
}

