package com.meowduels.listeners;

import com.meowduels.MeowDuels;
import com.meowduels.model.ActiveDuel;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class ChatListener
implements Listener {
    private static final int MAX_NOTICES = 3;
    private static final AtomicBoolean FLATTEN_FAILED = new AtomicBoolean(false);
    private final MeowDuels plugin;

    public ChatListener(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onChat(AsyncChatEvent asyncChatEvent) {
        if (!this.plugin.getConfig().getBoolean("chat.isolated", true)) {
            return;
        }
        Player player = asyncChatEvent.getPlayer();
        Object object = this.matchOf(player.getUniqueId());
        Set<UUID> set = this.namedIn(asyncChatEvent.message(), player);
        Set set2 = asyncChatEvent.viewers();
        if (set2 != null) {
            try {
                Iterator iterator = set2.iterator();
                while (iterator.hasNext()) {
                    if (this.canRead(player, object, set, (Audience)iterator.next())) continue;
                    iterator.remove();
                }
            }
            catch (UnsupportedOperationException unsupportedOperationException) {
                // If another chat plugin supplied an immutable audience set,
                // fail closed instead of accidentally broadcasting match chat.
                asyncChatEvent.setCancelled(true);
                return;
            }
        }
        this.noticeMentions(player, object, set);
    }

    private boolean canRead(Player player, Object object, Set<UUID> set, Audience audience) {
        if (!(audience instanceof Player)) {
            return true;
        }
        Player player2 = (Player)audience;
        if (player2.getUniqueId().equals(player.getUniqueId())) {
            return true;
        }
        Object object2 = this.matchOf(player2.getUniqueId());
        // Match, party, and lobby are separate chat scopes. Explicitly naming
        // someone does not bypass this boundary; all recipients must share the
        // sender's exact scope (including the null lobby scope).
        return object == object2;
    }

    private Object matchOf(UUID uUID) {
        ActiveDuel activeDuel = this.plugin.getDuelManager().getDuel(uUID);
        if (activeDuel != null) {
            return activeDuel;
        }
        Object object = this.plugin.getPartyManager().matchOf(uUID);
        if (object != null) {
            return object;
        }
        UUID uUID2 = this.plugin.getSpectateManager().getWatchedTarget(uUID);
        if (uUID2 == null) {
            return null;
        }
        ActiveDuel activeDuel2 = this.plugin.getDuelManager().getDuel(uUID2);
        return activeDuel2 != null ? activeDuel2 : this.plugin.getPartyManager().matchOf(uUID2);
    }

    private boolean isolated(UUID id) {
        return this.plugin.getPlayerSettings().isIsolatedChat(id);
    }

    private Set<UUID> namedIn(Component component, Player player) {
        if (component == null) {
            return Collections.emptySet();
        }
        String string = this.flatten(component);
        if (string == null || string.isEmpty()) {
            return Collections.emptySet();
        }
        String string2 = string.toLowerCase(Locale.ROOT);
        HashSet<UUID> hashSet = new HashSet<UUID>();
        for (Player player2 : Bukkit.getOnlinePlayers()) {
            UUID uUID = player2.getUniqueId();
            if (uUID.equals(player.getUniqueId()) || !ChatListener.mentions(string2, player2.getName().toLowerCase(Locale.ROOT))) continue;
            hashSet.add(uUID);
        }
        return hashSet;
    }

    private void noticeMentions(Player player, Object object, Set<UUID> set) {
        if (set.isEmpty()) {
            return;
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (UUID uUID : set) {
            Object object2;
            if (arrayList.size() >= 3) break;
            Player player2 = Bukkit.getPlayer((UUID)uUID);
            if (player2 == null || !this.isolated(uUID) || (object2 = this.matchOf(uUID)) == null || object2 == object) continue;
            arrayList.add(this.plugin.messages().get("chat.isolated-notice", "target", player2.getName(), "kind", this.plugin.messages().raw(object2 instanceof ActiveDuel ? "chat.kind-duel" : "chat.kind-party")));
        }
        if (arrayList.isEmpty()) {
            return;
        }
        ArrayList<String> arrayList2 = arrayList;
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            for (String string : arrayList2) {
                player.sendMessage(string);
            }
        });
    }

    private String flatten(Component component) {
        try {
            Object object = MiniMessage.miniMessage().serialize(component);
            return object instanceof String ? (String)object : null;
        }
        catch (Throwable throwable) {
            if (FLATTEN_FAILED.compareAndSet(false, true)) {
                this.plugin.getLogger().warning("Could not read chat text for mention detection (" + throwable.getClass().getSimpleName() + ": " + throwable.getMessage() + "). Isolated chat still works; naming someone will not reach them.");
            }
            return null;
        }
    }

    private static boolean mentions(String string, String string2) {
        if (string2.isEmpty()) {
            return false;
        }
        int n = 0;
        int n2;
        while ((n2 = string.indexOf(string2, n)) >= 0) {
            boolean bl;
            int n3 = n2 + string2.length();
            boolean bl2 = n2 == 0 || !ChatListener.isNameChar(string.charAt(n2 - 1));
            boolean bl3 = bl = n3 >= string.length() || !ChatListener.isNameChar(string.charAt(n3));
            if (bl2 && bl) {
                return true;
            }
            n = n2 + 1;
        }
        return false;
    }

    private static boolean isNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}

