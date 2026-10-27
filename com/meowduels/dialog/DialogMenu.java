package com.meowduels.dialog;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.QueueCategoriesMenu;
import com.meowduels.gui.QueuePickMenu;
import com.meowduels.gui.DuelConfirmMenu;
import com.meowduels.gui.KitPickMenu;
import com.meowduels.gui.MatchSummaryMenu;
import com.meowduels.util.Items;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickCallback;
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
        int pages = Math.max(1, kitPicker
            ? (kits.size() + PAGE_SIZE - 1) / PAGE_SIZE
            : (controls.size() + CONTROL_PAGE_SIZE - 1) / CONTROL_PAGE_SIZE);
        int safePage = Math.max(0, Math.min(page, pages - 1));
        List<ActionButton> actions = new ArrayList<>();
        if (kitPicker) {
            int start = safePage * PAGE_SIZE;
            for (int i = start; i < Math.min(kits.size(), start + PAGE_SIZE); i++) {
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
        if (actions.isEmpty()) {
            actions.add(ActionButton.builder(Component.text("Close")).width(150).build());
        }

        Dialog dialog = Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(view.title()).canCloseWithEscape(true).pause(false).build())
            .type(menu instanceof MatchSummaryMenu && actions.size() == 1
                ? DialogType.notice(actions.get(0))
                : DialogType.multiAction(actions).columns(2).build()));
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

    private static ActionButton button(MeowDuels plugin, Player player, Menu menu, InventoryView view, ButtonEntry entry, int page) {
        ItemMeta meta = entry.item.getItemMeta();
        Component label = meta != null && meta.hasDisplayName() ? meta.displayName() : Component.translatable(entry.item.getType().translationKey());
        List<Component> lore = meta == null || meta.lore() == null ? List.of() : meta.lore();
        Component tooltip = lore.isEmpty() ? null : Component.join(JoinConfiguration.separator(Component.newline()), lore);
        final int slot = entry.slot;
        return ActionButton.builder(label)
            .tooltip(tooltip)
            .width(180)
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
        return ActionButton.builder(Component.text(label)).width(180)
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
