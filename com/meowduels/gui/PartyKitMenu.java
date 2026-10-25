package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.model.Party;
import com.meowduels.model.PartyMode;
import com.meowduels.util.Items;
import com.meowduels.util.KitCatalog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PartyKitMenu extends Menu {
    private static final String MENU = "partykit";
    private static final int[] KIT_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private final MeowDuels plugin;
    private final PartyMode mode;
    private int page;

    public PartyKitMenu(MeowDuels plugin, PartyMode mode) {
        this.plugin = plugin;
        this.mode = mode;
    }

    private List<Page> pages() {
        ArrayList<String> kits = new ArrayList<String>();
        for (KitCatalog.Entry entry : KitCatalog.available(this.plugin)) {
            kits.add(entry.kit());
        }
        ArrayList<String> unique = new ArrayList<String>(new LinkedHashSet<String>(kits));
        ArrayList<Page> pages = new ArrayList<Page>();
        for (int start = 0; start < unique.size(); start += KIT_SLOTS.length) {
            pages.add(new Page(unique.subList(start, Math.min(unique.size(), start + KIT_SLOTS.length))));
        }
        return pages;
    }

    @Override
    public void build() {
    }

    @Override
    public void open(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-pick", new String[0]));
            return;
        }
        List<Page> pages = this.pages();
        if (pages.isEmpty()) {
            player.sendMessage(this.plugin.messages().get("notice.party.no-kits-category-yet", new String[0]));
            player.sendMessage(this.plugin.messages().get("notice.party.kits-outside-category-aren", new String[0]));
            return;
        }
        this.page = Math.max(0, Math.min(this.page, pages.size() - 1));
        Page current = pages.get(this.page);
        GuiConfigManager gui = this.plugin.gui();
        int rows = gui.rows(MENU, 4);
        int lastSlot = rows * 9 - 1;
        HashMap<String, String> tokens = new HashMap<String, String>();
        tokens.put("mode", this.mode.getLabel());
        this.createRaw(rows, gui.title(MENU, "<dark_gray>▏ {mode}", tokens));
        gui.frame(MENU, this.inventory, rows);

        String selectedKit = party.getKit();
        for (int i = 0; i < KIT_SLOTS.length && i < current.kits.size() && KIT_SLOTS[i] <= lastSlot; i++) {
            ItemStack icon = this.kitIcon(gui, current.kits.get(i), selectedKit);
            if (icon != null) this.inventory.setItem(KIT_SLOTS[i], icon);
        }

        this.inventory.setItem(Math.min(lastSlot, gui.slot(MENU, "back", 27, rows)),
            gui.back(MENU, "back").tag(this.plugin.keyButton(), "kit-back").build());
        if (pages.size() > 1) {
            HashMap<String, String> pageTokens = new HashMap<String, String>();
            pageTokens.put("page", String.valueOf(this.page + 1));
            pageTokens.put("pages", String.valueOf(pages.size()));
            this.inventory.setItem(Math.min(lastSlot, gui.slot(MENU, "prev-page", 28, rows)),
                gui.item(MENU, "prev-page", pageTokens, Material.ARROW, "<yellow>Previous page", "<gray>Page {page} of {pages}")
                    .hideTooltip().tag(this.plugin.keyButton(), "page-prev").build());
            this.inventory.setItem(Math.min(lastSlot, gui.slot(MENU, "next-page", 35, rows)),
                gui.item(MENU, "next-page", pageTokens, Material.ARROW, "<yellow>Next page", "<gray>Page {page} of {pages}")
                    .hideTooltip().tag(this.plugin.keyButton(), "page-next").build());
        }
        player.openInventory(this.inventory);
    }

    private ItemStack kitIcon(GuiConfigManager gui, String kitName, String selectedKit) {
        Kit kit = this.plugin.getKitManager().get(kitName);
        if (kit == null) return null;
        String queueLabel = kit.withIcon(kit.getQueueDisplayName());
        HashMap<String, String> tokens = new HashMap<String, String>();
        tokens.put("kit", queueLabel);
        Items item = gui.toggle(MENU, "kit", kit.getName().equalsIgnoreCase(selectedKit), tokens,
            GuiConfigManager.Face.of(gui.material(MENU, "kit", kit.getIcon()), "{kit}", new String[0]).glow(),
            GuiConfigManager.Face.of(gui.material(MENU, "kit", kit.getIcon()), "{kit}", new String[0]))
            .hideTooltip().tag(this.plugin.keyKit(), kit.getName());
        // Match the queue picker exactly: configured queue prefix and kit icon.
        item.miniName(queueLabel);
        return item.build();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        String button = Items.readTag(item, this.plugin.keyButton());
        if ("kit-back".equals(button)) {
            if (this.mode == PartyMode.DUELS) new PartyOpponentMenu(this.plugin).open(player);
            else new PartyModeMenu(this.plugin).open(player);
            return;
        }
        if ("page-prev".equals(button) || "page-next".equals(button)) {
            int count = this.pages().size();
            if (count > 0) {
                this.page = "page-prev".equals(button) ? (this.page - 1 + count) % count : (this.page + 1) % count;
                this.open(player);
            }
            return;
        }
        String kitName = Items.readTag(item, this.plugin.keyKit());
        if (kitName == null || !this.plugin.getKitManager().exists(kitName)) return;
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.closeInventory();
            return;
        }
        party.setKit(kitName);
        if (this.mode == PartyMode.SPLIT) {
            party.getTeams().clear();
            new PartyTeamMenu(this.plugin).open(player);
        } else if (this.mode == PartyMode.DUELS) {
            new PartyDuelsConfirmMenu(this.plugin).open(player);
        } else {
            new PartyStartConfirmMenu(this.plugin, this.mode).open(player);
        }
    }

    private static final class Page {
        private final List<String> kits;

        private Page(List<String> kits) {
            this.kits = kits;
        }
    }
}
