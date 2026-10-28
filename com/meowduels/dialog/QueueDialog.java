package com.meowduels.dialog;

import com.meowduels.MeowDuels;
import com.meowduels.managers.QueueManager;
import com.meowduels.model.Kit;
import com.meowduels.util.Items;
import com.meowduels.util.KitCatalog;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/** The category-free native dialog used by the queue menu. */
public final class QueueDialog {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int PAGE_SIZE_DEFAULT = 6;

    private QueueDialog() {}

    /** Returns false when disabled or unavailable so the caller can open the chest menu. */
    public static boolean open(MeowDuels plugin, Player player, int page) {
        if (player == null || !player.isOnline() || !apiPresent()) return false;
        YamlConfiguration cfg = load(plugin);
        if (!cfg.getBoolean("enabled", true)) return false;

        List<KitCatalog.Entry> kits = KitCatalog.available(plugin);
        int pageSize = Math.max(1, Math.min(12, cfg.getInt("page-size", PAGE_SIZE_DEFAULT)));
        int pages = Math.max(1, (kits.size() + pageSize - 1) / pageSize);
        int safePage = Math.max(0, Math.min(page, pages - 1));
        int width = clamp(cfg.getInt("width", 300), 1, 1024);
        int rowWidth = clamp(cfg.getInt("row-width", 260), 1, 1024);
        UUID owner = player.getUniqueId();
        QueueManager queue = plugin.getQueueManager();

        ArrayList<DialogBody> body = new ArrayList<>();
        String header = cfg.getString("header", "<gray>ᴄʟɪᴄᴋ ᴀ ᴋɪᴛ ᴛᴏ ǫᴜᴇᴜᴇ");
        if (header != null && !header.isBlank()) body.add(DialogBody.plainMessage(MM.deserialize(header), width));

        List<String> kitNames = kits.stream().map(KitCatalog.Entry::kit).toList();
        boolean allQueued = !kitNames.isEmpty() && kitNames.stream().allMatch(name -> queue.isQueuedFor(owner, name));
        boolean keepQueuing = queue.isKeepQueuing(owner);
        String checked = cfg.getString("toggles.on", "<green>☑</green>");
        String unchecked = cfg.getString("toggles.off", "<gray>☐</gray>");
        Component queueAll = MM.deserialize(cfg.getString("toggles.queue-all", "{box} <white>Queue All")
            .replace("{box}", allQueued ? checked : unchecked))
            .clickEvent(click(plugin, owner, clicker -> {
                queue.queueAll(clicker, kitNames);
                reopen(plugin, clicker, safePage);
            }))
            .hoverEvent(MM.deserialize(cfg.getString(allQueued ? "toggles.queue-all-hover-on" : "toggles.queue-all-hover-off",
                allQueued ? "<gray>Leave every visible queue" : "<gray>Join every visible queue")));
        Component keep = MM.deserialize(cfg.getString("toggles.keep-queuing", "{box} <white>Keep Queuing")
            .replace("{box}", keepQueuing ? checked : unchecked))
            .clickEvent(click(plugin, owner, clicker -> {
                queue.toggleKeepQueuing(owner);
                reopen(plugin, clicker, safePage);
            }))
            .hoverEvent(MM.deserialize(cfg.getString("toggles.keep-queuing-hover", "<gray>Rejoin the same queues after your match")));
        body.add(DialogBody.plainMessage(queueAll
            .append(MM.deserialize(cfg.getString("toggles.gap", "      ")))
            .append(keep), width));

        int start = safePage * pageSize;
        for (int i = start; i < Math.min(kits.size(), start + pageSize); i++) {
            KitCatalog.Entry entry = kits.get(i);
            Kit kit = plugin.getKitManager().get(entry.kit());
            if (kit == null || !kit.isGuiVisible()) continue;

            boolean queued = queue.isQueuedFor(owner, kit.getName());
            int fighting = queue.dueling(kit.getName()) * QueueManager.NEEDED;
            int waiting = queue.queued(kit.getName());
            int players = fighting + waiting;
            String name = kit.getQueueDisplayName();
            String format = cfg.getString(queued ? "kit.queued" : "kit.line",
                "{name} <dark_gray>→</dark_gray> <gray>{players} {players_word}");
            String playerWord = cfg.getString(players == 1 ? "kit.player-word" : "kit.players-word", players == 1 ? "Player" : "Players");
            Component row = formatRow(format, name, players, playerWord, fighting, waiting);
            String hoverFormat = cfg.getString(queued ? "kit.hover-queued" : "kit.hover",
                queued ? "<red>ᴄʟɪᴄᴋ ᴛᴏ ʟᴇᴀᴠᴇ ᴛʜᴇ ǫᴜᴇᴜᴇ" : "<yellow>ᴄʟɪᴄᴋ ᴛᴏ ᴊᴏɪɴ ᴛʜᴇ ǫᴜᴇᴜᴇ");
            String hover = fill(hoverFormat, name, players, fighting, waiting);
            String kitName = kit.getName();
            ClickEvent click = click(plugin, owner, clicker -> {
                queue.toggle(clicker, kitName);
                reopen(plugin, clicker, safePage);
            });
            row = row.clickEvent(click).hoverEvent(MM.deserialize(hover));
            ItemStack icon = Items.of(kit.getIcon()).glint(queued ? Boolean.TRUE : null).build();
            body.add(DialogBody.item(icon)
                .description(DialogBody.plainMessage(row, rowWidth))
                .showDecorations(false)
                .showTooltip(false)
                .build());
        }

        if (kits.isEmpty()) {
            body.add(DialogBody.plainMessage(MM.deserialize(cfg.getString("empty", "<gray>ɴᴏ ǫᴜᴇᴜᴇ ᴋɪᴛꜱ ᴀᴠᴀɪʟᴀʙʟᴇ")), width));
        } else if (pages > 1) {
            Component controls = Component.empty();
            if (safePage > 0) {
                controls = controls.append(MM.deserialize(cfg.getString("previous-page", "<yellow>‹ Previous page</yellow>"))
                    .clickEvent(click(plugin, owner, clicker -> reopen(plugin, clicker, safePage - 1))));
            }
            if (safePage > 0) controls = controls.append(MM.deserialize(" <dark_gray>·</dark_gray> "));
            controls = controls.append(MM.deserialize(cfg.getString("page-label", "<gray>Page {page} of {pages}</gray>")
                .replace("{page}", String.valueOf(safePage + 1)).replace("{pages}", String.valueOf(pages))));
            if (safePage + 1 < pages) {
                controls = controls.append(MM.deserialize(" <dark_gray>·</dark_gray> "))
                    .append(MM.deserialize(cfg.getString("next-page", "<yellow>Next page ›</yellow>")
                        .replace("{page}", String.valueOf(safePage + 2)))
                        .clickEvent(click(plugin, owner, clicker -> reopen(plugin, clicker, safePage + 1))));
            }
            body.add(DialogBody.plainMessage(controls, width));
        }

        DialogBase base = DialogBase.builder(MM.deserialize(cfg.getString("title", "<gradient:#FFF1B3:#E6A700><b>ǫᴜᴇᴜᴇ</b></gradient>")))
            .body(body)
            .canCloseWithEscape(true)
            .pause(false)
            .afterAction(DialogBase.DialogAfterAction.CLOSE)
            .build();
        ActionButton close = ActionButton.create(MM.deserialize(cfg.getString("close", "<white>ᴄʟᴏꜱᴇ")), null,
            clamp(cfg.getInt("close-width", 200), 1, 1024), null);
        try {
            player.showDialog(Dialog.create(builder -> builder.empty().base(base).type(DialogType.notice(close))));
            return true;
        } catch (LinkageError | RuntimeException ex) {
            plugin.getLogger().warning("Could not open the queue dialog; using the chest menu: " + ex.getMessage());
            return false;
        }
    }

    private static ClickEvent click(MeowDuels plugin, UUID owner, Consumer<Player> action) {
        ClickCallback<Audience> callback = audience -> {
            if (!(audience instanceof Player clicker) || !clicker.getUniqueId().equals(owner)) return;
            Bukkit.getScheduler().runTask((Plugin) plugin, () -> {
                Player current = Bukkit.getPlayer(owner);
                if (current != null && current.isOnline()) action.accept(current);
            });
        };
        return ClickEvent.callback(callback, options -> options.uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(10)));
    }

    private static void reopen(MeowDuels plugin, Player player, int page) {
        if (plugin.getDuelManager().isInDuel(player.getUniqueId())) {
            try { player.closeDialog(); } catch (Throwable ignored) {}
            return;
        }
        open(plugin, player, page);
    }

    private static String fill(String text, String name, int players, int fighting, int waiting) {
        return text.replace("{name}", name)
            .replace("{players}", String.valueOf(players))
            .replace("{fighting}", String.valueOf(fighting))
            .replace("{queued}", String.valueOf(waiting));
    }

    private static Component formatRow(String format, String name, int players, String playerWord, int fighting, int waiting) {
        int marker = format.indexOf("{players}");
        if (marker < 0) return MM.deserialize(fill(format, name, players, fighting, waiting));
        String before = fill(format.substring(0, marker), name, players, fighting, waiting);
        String after = format.substring(marker + "{players}".length()).replace("{players_word}", playerWord)
            .replace("{fighting}", String.valueOf(fighting)).replace("{queued}", String.valueOf(waiting));
        Component count = Component.text(String.valueOf(players), NamedTextColor.GRAY)
            .decoration(TextDecoration.BOLD, false).decoration(TextDecoration.ITALIC, false);
        return MM.deserialize(before).append(count).append(MM.deserialize("<gray>" + after)
            .decoration(TextDecoration.BOLD, false).decoration(TextDecoration.ITALIC, false));
    }

    private static boolean apiPresent() {
        try {
            Class.forName("io.papermc.paper.dialog.Dialog");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static YamlConfiguration load(MeowDuels plugin) {
        File file = new File(plugin.getDataFolder(), "queuedialog.yml");
        if (!file.exists()) {
            try { plugin.saveResource("queuedialog.yml", false); }
            catch (IllegalArgumentException ignored) { /* Built-in defaults below remain usable. */ }
        }
        return file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
