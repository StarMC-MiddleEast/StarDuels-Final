package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.model.Kit;
import com.meowduels.util.Sounds;
import com.meowduels.util.SpawnItems;
import com.meowduels.util.KitCatalog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class QueueManager {
    public static final int NEEDED = 2;
    private final MeowDuels plugin;
    private final Map<String, LinkedHashSet<UUID>> queues = new LinkedHashMap<String, LinkedHashSet<UUID>>();
    private final Map<UUID, Long> waitingSince = new HashMap<UUID, Long>();

    public QueueManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    public int queued(String string) {
        LinkedHashSet<UUID> linkedHashSet = this.queues.get(QueueManager.key(string));
        return linkedHashSet == null ? 0 : linkedHashSet.size();
    }

    public void quickJoin(Player player) {
        if (this.plugin.getPartyManager().busy(player)) {
            return;
        }
        UUID uUID = player.getUniqueId();
        ArrayList<Kit> arrayList = new ArrayList<Kit>();
        for (KitCatalog.Entry entry : KitCatalog.available(this.plugin)) {
            Kit kit = this.plugin.getKitManager().get(entry.kit());
            if (kit == null || !this.plugin.getDuelManager().hasUsableArena(kit.getName()) || this.isQueuedFor(uUID, kit.getName())) continue;
            arrayList.add(kit);
        }
        if (arrayList.isEmpty()) {
            Sounds.deny(player);
            player.sendMessage(this.msg("queue.no-arena", new String[0]));
            return;
        }
        Object object = null;
        for (Kit kit : arrayList) {
            if (this.queued(kit.getName()) <= 0) continue;
            object = kit;
            break;
        }
        if (object == null) {
            object = (Kit)arrayList.get(ThreadLocalRandom.current().nextInt(arrayList.size()));
        }
        this.join(player, ((Kit)object).getName());
    }

    public int dueling(String string) {
        return this.plugin.getDuelManager().fightsWithKit(string);
    }

    public boolean isQueuedFor(UUID uUID, String string) {
        LinkedHashSet<UUID> linkedHashSet = this.queues.get(QueueManager.key(string));
        return linkedHashSet != null && linkedHashSet.contains(uUID);
    }

    public boolean isQueued(UUID uUID) {
        for (LinkedHashSet<UUID> linkedHashSet : this.queues.values()) {
            if (!linkedHashSet.contains(uUID)) continue;
            return true;
        }
        return false;
    }

    public Set<String> queuedKits(UUID uUID) {
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
        for (Map.Entry<String, LinkedHashSet<UUID>> entry : this.queues.entrySet()) {
            if (!entry.getValue().contains(uUID)) continue;
            linkedHashSet.add(entry.getKey());
        }
        return linkedHashSet;
    }

    public void toggle(Player player, String string) {
        if (this.isQueuedFor(player.getUniqueId(), string)) {
            this.leaveKit(player, string);
        } else {
            this.join(player, string);
        }
    }

    public void join(Player player, String string2) {
        if (this.plugin.getPartyManager().busy(player)) {
            return;
        }
        UUID uUID = player.getUniqueId();
        if (this.plugin.getDuelManager().isInDuel(uUID)) {
            player.sendMessage(this.msg("queue.in-duel", new String[0]));
            return;
        }
        if (this.plugin.getEventManager().isInvolved(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.queue.t-queue-while-re", new String[0]));
            return;
        }
        if (!this.plugin.getKitManager().exists(string2)) {
            player.sendMessage(this.msg("queue.kit-gone", new String[0]));
            return;
        }
        if (!this.plugin.getDuelManager().hasUsableArena(string2)) {
            player.sendMessage(this.msg("queue.no-arena", new String[0]));
            this.plugin.getDuelManager().explainNoArena(player, string2);
            return;
        }
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        this.queues.computeIfAbsent(QueueManager.key(string2), string -> new LinkedHashSet()).add(uUID);
        if (!this.waitingSince.containsKey(uUID)) {
            this.waitingSince.put(uUID, System.currentTimeMillis());
        }
        this.tryMatch(QueueManager.key(string2));
        if (this.isQueued(uUID) && player.isOnline()) {
            player.sendMessage(this.queuedLine("queue.joined-text", string2));
            Sounds.queueJoin(player);
            SpawnItems.setLeaveQueue(this.plugin, player, true);
        }
    }

    public void leaveKit(Player player, String string) {
        LinkedHashSet<UUID> linkedHashSet = this.queues.get(QueueManager.key(string));
        if (linkedHashSet == null || !linkedHashSet.remove(player.getUniqueId())) {
            return;
        }
        if (linkedHashSet.isEmpty()) {
            this.queues.remove(QueueManager.key(string));
        }
        player.sendMessage(this.queuedLine("queue.left-text", string));
        Sounds.queueLeave(player);
        if (!this.isQueued(player.getUniqueId())) {
            this.waitingSince.remove(player.getUniqueId());
            SpawnItems.setLeaveQueue(this.plugin, player, false);
        }
    }

    public void remove(UUID uUID) {
        Iterator<LinkedHashSet<UUID>> iterator = this.queues.values().iterator();
        while (iterator.hasNext()) {
            LinkedHashSet<UUID> queue = iterator.next();
            if (!queue.remove(uUID) || !queue.isEmpty()) continue;
            iterator.remove();
        }
        this.waitingSince.remove(uUID);
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null && player.isOnline()) {
            SpawnItems.setLeaveQueue(this.plugin, player, false);
        }
    }

    public long waitingSeconds(UUID uUID) {
        Long l = this.waitingSince.get(uUID);
        if (l == null) {
            return 0L;
        }
        return Math.max(0L, (System.currentTimeMillis() - l) / 1000L);
    }

    public void leaveAll(Player player) {
        if (!this.isQueued(player.getUniqueId())) {
            player.sendMessage(this.msg("queue.not-queued", new String[0]));
            return;
        }
        this.remove(player.getUniqueId());
        player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_gray>[<yellow>ⓘ</yellow>]</dark_gray> <gray>You left <yellow>all</yellow> the queues!</gray>"));
        Sounds.queueLeave(player);
    }

    private void tryMatch(String string) {
        this.tryMatch(string, true);
    }

    private void tryMatch(String string, boolean bl) {
        Player player;
        Player player2;
        while (true) {
            LinkedHashSet<UUID> linkedHashSet;
            if ((linkedHashSet = this.queues.get(string)) == null || linkedHashSet.size() < 2) {
                return;
            }
            ArrayList<UUID> arrayList = new ArrayList<UUID>(linkedHashSet);
            player2 = Bukkit.getPlayer((UUID)arrayList.get(0));
            if (player2 == null || this.plugin.getDuelManager().isInDuel(arrayList.get(0))) {
                this.remove(arrayList.get(0));
                continue;
            }
            player = Bukkit.getPlayer((UUID)arrayList.get(1));
            if (player == null || this.plugin.getDuelManager().isInDuel(arrayList.get(1))) {
                this.remove(arrayList.get(1));
                continue;
            }
            if (!this.plugin.getDuelManager().startQueuedDuel(player2, player, string)) break;
        }
        if (bl) {
            player2.sendMessage(this.msg("queue.no-arena", new String[0]));
            player.sendMessage(this.msg("queue.no-arena", new String[0]));
        }
    }

    public void tickMatch() {
        if (this.queues.isEmpty()) {
            return;
        }
        for (String string : new ArrayList<String>(this.queues.keySet())) {
            LinkedHashSet<UUID> linkedHashSet = this.queues.get(string);
            if (linkedHashSet == null || linkedHashSet.size() < 2) continue;
            this.tryMatch(string, false);
        }
    }

    private Component queuedLine(String string, String string2) {
        String wording = string.equals("queue.left-text") ? "You have left the queue for " : "You have joined the queue for ";
        return MiniMessage.miniMessage().deserialize("<dark_gray>[<yellow>ⓘ</yellow>]</dark_gray> <gray>" + wording + "</gray>" + this.kitMini(string2));
    }

    private String kitMini(String string) {
        Kit kit = this.plugin.getKitManager().get(string);
        return kit == null ? "<yellow>" + string.replace("_", " ") + "</yellow>" : kit.getQueueDisplayNameWithIcon();
    }

    private static String key(String string) {
        return string.toLowerCase(Locale.ROOT);
    }

    private String msg(String string, String ... stringArray) {
        return this.plugin.messages().get(string, stringArray);
    }
}

