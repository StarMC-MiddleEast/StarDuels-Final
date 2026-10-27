package com.meowduels.dialog;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.QueueCategoriesMenu;
import com.meowduels.gui.QueuePickMenu;
import com.meowduels.gui.DuelConfirmMenu;
import com.meowduels.gui.KitPickMenu;
import com.meowduels.gui.MatchSummaryMenu;
import com.meowduels.gui.PartyDuelsConfirmMenu;
import com.meowduels.model.Kit;
import com.meowduels.model.Party;
import com.meowduels.util.Items;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Renders the existing player menu inventory as a native Paper dialog. */
public final class DialogMenu {
    private static final int PAGE_SIZE = 6;
    private static final int QUEUE_PAGE_SIZE = 5;
    private static final int CONTROL_PAGE_SIZE = 8;
    private static final AtomicBoolean FALLBACK_WARNED = new AtomicBoolean();
    private static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
        .uses(1).lifetime(Duration.ofMinutes(10)).build();

    private DialogMenu() {}

    static void open(MeowDuels plugin, Player player, Menu menu, InventoryView view, int page, boolean playOpenSound) {
        List<ButtonEntry> kits = new ArrayList<>();
        List<ButtonEntry> controls = new ArrayList<>();
        for (int slot = 0; slot < view.getTopInventory().getSize(); slot++) {
            ItemStack item = view.getTopInventory().getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;
            String kitTag = Items.readTag(item, plugin.keyKit());
            String buttonTag = Items.readTag(item, plugin.keyButton());
            if (kitTag == null && buttonTag == null) continue;
            ButtonEntry entry = new ButtonEntry(slot, item, kitTag != null);
            if (entry.kit) kits.add(entry); else controls.add(entry);
        }

        boolean kitPicker = !kits.isEmpty();
        if (menu instanceof QueuePickMenu && kits.isEmpty()) controls.clear();
        boolean queuePicker = menu instanceof QueuePickMenu;
        int kitPageSize = queuePicker ? QUEUE_PAGE_SIZE : PAGE_SIZE;
        int pages = Math.max(1, kitPicker
            ? (kits.size() + kitPageSize - 1) / kitPageSize
            : (controls.size() + CONTROL_PAGE_SIZE - 1) / CONTROL_PAGE_SIZE);
        int safePage = Math.max(0, Math.min(page, pages - 1));
        List<ActionButton> actions = new ArrayList<>();
        if (kitPicker) {
            int start = safePage * kitPageSize;
            for (int i = start; i < Math.min(kits.size(), start + kitPageSize); i++) {
                actions.add(button(plugin, player, menu, view, kits.get(i), safePage));
            }
            for (ButtonEntry entry : controls) {
                actions.add(button(plugin, player, menu, view, entry, safePage));
            }
        } else {
            int start = safePage * CONTROL_PAGE_SIZE;
            for (int i = start; i < Math.min(controls.size(), start + CONTROL_PAGE_SIZE); i++) {
                actions.add(button(plugin, player, menu, view, controls.get(i), safePage));
            }
        }
        if (pages > 1) {
            if (safePage > 0) actions.add(nav(plugin, player, menu, view, safePage - 1, "‹ Previous page"));
            if (safePage + 1 < pages) actions.add(nav(plugin, player, menu, view, safePage + 1, "Next page ›"));
        }
        boolean emptyActions = actions.isEmpty();
        if (emptyActions) {
            actions.add(ActionButton.builder(Component.text("Close")).width(150).build());
        }

        DialogBase base = DialogBase.builder(view.title()).body(body(plugin, player, menu, safePage, pages, menu instanceof QueuePickMenu && kits.isEmpty())).canCloseWithEscape(true).pause(false).build();
        Dialog dialog = Dialog.create(builder -> builder.empty()
            .base(base)
            .type((menu instanceof MatchSummaryMenu || emptyActions)
                ? DialogType.notice(actions.get(0))
                : DialogType.multiAction(actions).columns(queuePicker ? 1 : 2).build()));
        try {
            player.showDialog(dialog);
            if (playOpenSound) com.meowduels.util.Sounds.open(player);
        } catch (LinkageError | RuntimeException ex) {
            if (FALLBACK_WARNED.compareAndSet(false, true)) {
                plugin.getLogger().warning("Paper Dialog API unavailable; falling back to the chest menu: " + ex.getMessage());
            }
            DialogListener.allowNextChest(player);
            player.openInventory(view.getTopInventory());
        }
    }

    private static List<DialogBody> body(MeowDuels plugin, Player viewer, Menu menu, int page, int pages, boolean noQueueKits) {
        List<DialogBody> body = new ArrayList<>();
        if (noQueueKits) {
            body.add(DialogBody.plainMessage(MiniMessage.miniMessage().deserialize("<dark_gray>▏ No kits available")));
        } else if (menu instanceof DuelConfirmMenu confirm) {
            Player sender = confirm.getSender();
            Player target = confirm.getTarget();
            body.add(DialogBody.plainMessage(MiniMessage.miniMessage().deserialize(
                "<gradient:#FFF1B3:#E6A700><bold>" + sender.getName() + "</bold></gradient> <dark_gray>vs</dark_gray> <gradient:#FFF1B3:#E6A700><bold>" + target.getName() + "</bold></gradient>")));
            body.add(head(sender));
            body.add(head(target));
        } else if (menu instanceof PartyDuelsConfirmMenu) {
            Party ours = plugin.getPartyManager().partyOf(viewer.getUniqueId());
            Party theirs = ours == null ? null : plugin.getPartyManager().partyLedBy(ours.getDuelTarget());
            if (ours != null && theirs != null) {
                OfflinePlayer left = Bukkit.getOfflinePlayer(ours.getLeader());
                OfflinePlayer right = Bukkit.getOfflinePlayer(theirs.getLeader());
                String leftName = left.getName() == null ? "Player" : left.getName();
                String rightName = right.getName() == null ? "Player" : right.getName();
                body.add(DialogBody.plainMessage(MiniMessage.miniMessage().deserialize(
                    "<gradient:#FFF1B3:#E6A700><bold>" + leftName + "</bold></gradient> <dark_gray>vs</dark_gray> <gradient:#FFF1B3:#E6A700><bold>" + rightName + "</bold></gradient>")));
                body.add(head(left));
                body.add(head(right));
            }
        } else if (menu instanceof QueuePickMenu) {
            body.add(DialogBody.plainMessage(MiniMessage.miniMessage().deserialize("<#66e4dc>Click a kit to queue")));
            if (pages > 1) body.add(DialogBody.plainMessage(MiniMessage.miniMessage().deserialize("<dark_gray>Page " + (page + 1) + " of " + pages)));
        } else if (pages > 1) {
            body.add(DialogBody.plainMessage(MiniMessage.miniMessage().deserialize("<dark_gray>▏ Page " + (page + 1) + " of " + pages)));
        } else if (menu instanceof QueuePickMenu || menu instanceof KitPickMenu) {
            body.add(DialogBody.plainMessage(MiniMessage.miniMessage().deserialize("<dark_gray>▏ Select a kit")));
        } else {
            body.add(DialogBody.plainMessage(MiniMessage.miniMessage().deserialize("<dark_gray>▏ No options available")));
        }
        return body;
    }

    private static DialogBody head(OfflinePlayer player) {
        return DialogBody.item(Items.of(Material.PLAYER_HEAD).skull(player).build(), null, false, false, 32, 32);
    }

    private static ActionButton button(MeowDuels plugin, Player player, Menu menu, InventoryView view, ButtonEntry entry, int page) {
        ItemMeta meta = entry.item.getItemMeta();
        Component label;
        if (entry.kit) {
            Kit kit = plugin.getKitManager().get(Items.readTag(entry.item, plugin.keyKit()));
            label = kit == null ? (meta != null && meta.hasDisplayName() ? meta.displayName() : Component.translatable(entry.item.getType().translationKey()))
                : MiniMessage.miniMessage().deserialize(kit.withIcon(kit.getQueueDisplayName()));
            if (menu instanceof QueuePickMenu && kit != null) {
                int queued = plugin.getQueueManager().queued(kit.getName());
                Component arrow = Component.text(" → ", net.kyori.adventure.text.format.NamedTextColor.GRAY)
                    .decoration(TextDecoration.BOLD, false);
                Component count = Component.text(queued + (queued == 1 ? " Player" : " Players"), net.kyori.adventure.text.format.NamedTextColor.GRAY)
                    .decoration(TextDecoration.BOLD, false);
                label = label.append(arrow).append(count);
            }
        } else {
            label = meta != null && meta.hasDisplayName() ? meta.displayName() : Component.translatable(entry.item.getType().translationKey());
        }
        label = label.decoration(TextDecoration.ITALIC, false);
        List<Component> lore = meta == null || meta.lore() == null ? List.of() : meta.lore();
        Component tooltip = lore.isEmpty() ? null : Component.join(JoinConfiguration.separator(Component.newline()), lore);
        final int slot = entry.slot;
        return ActionButton.builder(label)
            .tooltip(tooltip)
            .width(150)
            .action(DialogAction.customClick((response, audience) -> {
                if (!(audience instanceof Player clicker) || !clicker.getUniqueId().equals(player.getUniqueId())) return;
                DialogListener.beginAction(clicker);
                clicker.closeDialog();
                playClickSound(menu, clicker);
                InventoryClickEvent click = new InventoryClickEvent(view, view.getSlotType(slot), slot, ClickType.LEFT, InventoryAction.PICKUP_ALL);
                menu.onClick(clicker, click);
                // Queue selection updates its inventory in place rather than reopening it.
                if (menu instanceof QueuePickMenu && !plugin.getDuelManager().isInDuel(clicker.getUniqueId())) {
                    DialogListener.renderAgain(plugin, clicker, menu, view, page, false);
                }
            }, CALLBACK_OPTIONS))
            .build();
    }

    private static ActionButton nav(MeowDuels plugin, Player player, Menu menu, InventoryView view, int page, String label) {
        return ActionButton.builder(MiniMessage.miniMessage().deserialize("<yellow>" + label).decoration(TextDecoration.ITALIC, false)).width(150)
            .action(DialogAction.customClick((response, audience) -> {
                if (audience instanceof Player clicker && clicker.getUniqueId().equals(player.getUniqueId())) {
                    playClickSound(menu, clicker);
                    DialogListener.renderAgain(plugin, clicker, menu, view, page, true);
                }
            }, CALLBACK_OPTIONS)).build();
    }

    private static void playClickSound(Menu menu, Player player) {
        if (menu instanceof QueuePickMenu || menu instanceof QueueCategoriesMenu) {
            com.meowduels.util.Sounds.queueClick(player);
        } else if (menu instanceof DuelConfirmMenu || menu instanceof KitPickMenu) {
            com.meowduels.util.Sounds.duelMenuClick(player);
        } else {
            com.meowduels.util.Sounds.click(player);
        }
    }

    private static final class ButtonEntry {
        private final int slot;
        private final ItemStack item;
        private final boolean kit;
        private ButtonEntry(int slot, ItemStack item, boolean kit) {
            this.slot = slot;
            this.item = item.clone();
            this.kit = kit;
        }
    }
}
