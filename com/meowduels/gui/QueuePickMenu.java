package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.dialog.QueueDialog;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.util.Items;
import com.meowduels.util.KitCatalog;
import com.meowduels.util.Text;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public final class QueuePickMenu extends Menu {
    private static final Set<QueuePickMenu> OPEN = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final int[] KIT_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private static final String MENU = "queuepick";
    private final MeowDuels plugin;
    private Player viewer;

    public QueuePickMenu(MeowDuels plugin) {
        this.plugin = plugin;
    }

    /** Kept for source compatibility with older menu callers; categories are no longer used here. */
    public QueuePickMenu(MeowDuels plugin, String ignoredCategory) {
        this(plugin);
    }

    public static void refreshAll() {
        for (QueuePickMenu menu : OPEN) {
            if (menu.inventory != null) {
                menu.populate();
            }
        }
    }

    @Override
    public void open(Player player) {
        // Queue is always a native Paper dialog; the chest menu is only a fallback
        // if the dialog API fails at runtime.
        try {
            if (QueueDialog.open(this.plugin, player, 0)) return;
        } catch (LinkageError | RuntimeException error) {
            this.plugin.getLogger().warning("Could not open the queue dialog; using the chest menu: " + error.getMessage());
        }
        this.viewer = player;
        this.build();
        player.openInventory(this.inventory);
        OPEN.add(this);
    }

    @Override
    public void onClose(Player player, InventoryCloseEvent event) {
        OPEN.remove(this);
    }

    @Override
    public void build() {
        GuiConfigManager gui = this.plugin.gui();
        this.createRaw(4, gui.title(MENU, "<gradient:#FFF1B3:#FFFFFF:#E6A700><bold>ǫᴜᴇᴜᴇ</bold></gradient>"));
        this.populate();
    }

    private void populate() {
        if (this.inventory == null) {
            return;
        }
        GuiConfigManager gui = this.plugin.gui();
        gui.frame(MENU, this.inventory, 4);
        List<KitCatalog.Entry> kits = KitCatalog.available(this.plugin);
        for (int i = 0; i < KIT_SLOTS.length; i++) {
            int slot = KIT_SLOTS[i];
            this.inventory.setItem(slot, i < kits.size() ? this.kitIcon(gui, kits.get(i)) : null);
        }
    }

    private ItemStack kitIcon(GuiConfigManager gui, KitCatalog.Entry entry) {
        Kit kit = this.plugin.getKitManager().get(entry.kit());
        if (kit == null) {
            return null;
        }
        int fights = this.plugin.getQueueManager().dueling(entry.kit());
        int queued = this.plugin.getQueueManager().queued(entry.kit());
        String label = kit.withIcon(kit.getDisplayName() == null || kit.getDisplayName().isBlank()
            ? "<yellow><bold>" + Text.smallCaps(entry.label()) + "</bold></yellow>"
            : kit.getQueueDisplayName());
        HashMap<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("kit", label);
        placeholders.put("queued", String.valueOf(queued));
        placeholders.put("fights", String.valueOf(fights));
        boolean isQueued = this.viewer != null && this.plugin.getQueueManager().isQueuedFor(this.viewer.getUniqueId(), entry.kit());
        int amount = Math.max(1, Math.min(64, fights));
        Items item = gui.toggle(MENU, "kit", isQueued, placeholders,
            GuiConfigManager.Face.of(gui.material(MENU, "kit", kit.getIcon()), "{kit}", "<white>ᴅᴜᴇʟɪɴɢ <yellow>{fights}", "<white>ɪɴ ǫᴜᴇᴜᴇ <yellow>{queued}", "", "<yellow>➜ ᴄʟɪᴄᴋ ᴛᴏ ǫᴜᴇᴜᴇ").amount(amount),
            GuiConfigManager.Face.of(gui.material(MENU, "kit", kit.getIcon()), "{kit}", "<white>ᴅᴜᴇʟɪɴɢ <yellow>{fights}", "<white>ɪɴ ǫᴜᴇᴜᴇ <yellow>{queued}", "", "<yellow>➜ ʟᴇᴀᴠᴇ ǫᴜᴇᴜᴇ").amount(amount))
            .tag(this.plugin.keyKit(), entry.kit()).glint(Boolean.FALSE);
        item.miniName(label);
        item.rawLore("<gray>ᴅᴜᴇʟɪɴɢ <yellow>" + fights,
            "<gray>ɪɴ ǫᴜᴇᴜᴇ <yellow>" + queued,
            "", isQueued ? "<yellow>➜ ʟᴇᴀᴠᴇ ǫᴜᴇᴜᴇ" : "<yellow>➜ ᴄʟɪᴄᴋ ᴛᴏ ǫᴜᴇᴜᴇ");
        return item.build();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        String kit = Items.readTag(event.getCurrentItem(), this.plugin.keyKit());
        if (kit != null && this.plugin.getKitManager().exists(kit)) {
            this.plugin.getQueueManager().toggle(player, kit);
            if (this.plugin.getDuelManager().isInDuel(player.getUniqueId())) {
                player.closeInventory();
            } else {
                this.populate();
            }
        }
    }
}
