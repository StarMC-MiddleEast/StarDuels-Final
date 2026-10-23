package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.PartyDuelsConfirmMenu;
import com.meowduels.gui.PartyModeMenu;
import com.meowduels.gui.PartyOpponentMenu;
import com.meowduels.gui.PartyStartConfirmMenu;
import com.meowduels.gui.PartyTeamMenu;
import com.meowduels.managers.CategoryManager;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.model.Party;
import com.meowduels.model.PartyMode;
import com.meowduels.util.Items;
import com.meowduels.util.KitCatalog;
import com.meowduels.util.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PartyKitMenu
extends Menu {
    private static final String MENU = "partykit";
    private static final int ROWS = 4;
    private static final int SIZE = 36;
    private static final int[] KIT_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private static final int SLOT_ARROW = 35;
    private static final int SLOT_BACK = 27;
    private final MeowDuels plugin;
    private final PartyMode mode;
    private int step = 0;

    public PartyKitMenu(MeowDuels meowDuels, PartyMode partyMode) {
        this.plugin = meowDuels;
        this.mode = partyMode;
    }

    private List<Page> pages() {
        ArrayList<Page> arrayList = new ArrayList<Page>();
        List<CategoryManager.Category> list = this.plugin.getCategoryManager().all();
        List<KitCatalog.Entry> available = KitCatalog.available(this.plugin);
        if (list.isEmpty()) {
            ArrayList<String> arrayList2 = new ArrayList<String>();
            for (KitCatalog.Entry entry : available) {
                arrayList2.add(entry.label());
            }
            this.slice(arrayList, "\u1d0b\u026a\u1d1b\ua731", arrayList2);
            return arrayList;
        }
        Set<String> added = new HashSet<String>();
        ArrayList<String> unassigned = new ArrayList<String>();
        for (CategoryManager.Category category : list) {
            ArrayList<String> categoryKits = new ArrayList<String>();
            for (String kitName : this.plugin.getCategoryManager().kitsFor(category)) {
                KitCatalog.Entry entry = findEntry(available, kitName);
                if (entry == null) continue;
                categoryKits.add(entry.label());
                added.add(entry.kit().toLowerCase(Locale.ROOT));
            }
            this.slice(arrayList, PartyKitMenu.smallCaps(category.getId()), categoryKits);
        }
        for (KitCatalog.Entry entry : available) {
            if (!added.contains(entry.kit().toLowerCase(Locale.ROOT))) {
                unassigned.add(entry.label());
            }
        }
        if (!unassigned.isEmpty()) {
            this.slice(arrayList, PartyKitMenu.smallCaps(list.get(0).getId()), unassigned);
        }
        return arrayList;
    }

    private static KitCatalog.Entry findEntry(List<KitCatalog.Entry> entries, String kitName) {
        for (KitCatalog.Entry entry : entries) {
            if (entry.kit().equalsIgnoreCase(kitName)) return entry;
        }
        return null;
    }

    private void slice(List<Page> list, String string, List<String> list2) {
        ArrayList<String> arrayList = new ArrayList<String>(new LinkedHashSet<String>(list2));
        if (arrayList.isEmpty()) {
            return;
        }
        for (int i = 0; i < arrayList.size(); i += KIT_SLOTS.length) {
            list.add(new Page(string, arrayList.subList(i, Math.min(arrayList.size(), i + KIT_SLOTS.length))));
        }
    }

    private static String smallCaps(String string) {
        return Text.smallCaps(string == null ? "" : string.replace('_', ' '));
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
        List<Page> list = this.pages();
        if (list.isEmpty()) {
            player.sendMessage(this.plugin.messages().get("notice.party.no-kits-category-yet", new String[0]));
            player.sendMessage(this.plugin.messages().get("notice.party.kits-outside-category-aren", new String[0]));
            return;
        }
        if (this.step >= list.size() || this.step < 0) {
            this.step = 0;
        }
        Page page = list.get(this.step);
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 4);
        int n2 = n * 9 - 1;
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("mode", this.mode.getLabel());
        hashMap.put("category", page.title);
        this.createRaw(n, guiConfigManager.title(MENU, "<dark_gray>\u258f {mode} {sep}{value}{category}", hashMap));
        guiConfigManager.frame(MENU, this.inventory, n);
        String string = party.getKit();
        for (int i = 0; i < KIT_SLOTS.length && i < page.kits.size() && KIT_SLOTS[i] <= n2; ++i) {
            ItemStack itemStack = this.kitIcon(guiConfigManager, page.kits.get(i), string);
            if (itemStack == null) continue;
            this.inventory.setItem(KIT_SLOTS[i], itemStack);
        }
        this.inventory.setItem(Math.min(n2, guiConfigManager.slot(MENU, "back", 27, n)), guiConfigManager.back(MENU, "back").tag(this.plugin.keyButton(), "kit-back").build());
        if (list.size() > 1) {
            this.inventory.setItem(Math.min(n2, guiConfigManager.slot(MENU, "next-category", 35, n)), guiConfigManager.face(MENU, "next-category", null, GuiConfigManager.Face.of(Material.ARROW, "{value}\u0274\u1d07x\u1d1b \u1d04\u1d00\u1d1b\u1d07\u0262\u1d0f\u0280\u028f", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d04\u028f\u1d04\u029f\u1d07")).hideTooltip().tag(this.plugin.keyButton(), "next-cat").build());
        }
        player.openInventory(this.inventory);
    }

    private ItemStack kitIcon(GuiConfigManager guiConfigManager, String string, String string2) {
        KitCatalog.Entry entry = findEntry(KitCatalog.available(this.plugin), string);
        if (entry == null) return null;
        Kit kit = this.plugin.getKitManager().get(entry.kit());
        if (kit == null) {
            return null;
        }
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("kit", entry.label());
        Items items = guiConfigManager.toggle(MENU, "kit", kit.getName().equalsIgnoreCase(string2), hashMap, GuiConfigManager.Face.of(guiConfigManager.material(MENU, "kit", kit.getIcon()), "&e{kit}", new String[0]).glow(), GuiConfigManager.Face.of(guiConfigManager.material(MENU, "kit", kit.getIcon()), "&e{kit}", new String[0])).hideTooltip().tag(this.plugin.keyKit(), kit.getName());
        if (guiConfigManager.name(MENU, "kit", hashMap) == null && kit.getDisplayName() != null && !kit.getDisplayName().isEmpty()) {
            items.miniName(kit.getDisplayName());
        }
        return items.build();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
        if ("kit-back".equals(Items.readTag(itemStack, this.plugin.keyButton()))) {
            if (this.mode == PartyMode.DUELS) {
                new PartyOpponentMenu(this.plugin).open(player);
            } else {
                new PartyModeMenu(this.plugin).open(player);
            }
            return;
        }
        if ("next-cat".equals(Items.readTag(itemStack, this.plugin.keyButton()))) {
            int n = this.pages().size();
            if (n > 0) {
                this.step = (this.step + 1) % n;
                this.open(player);
            }
            return;
        }
        String string = Items.readTag(itemStack, this.plugin.keyKit());
        if (string == null || !this.plugin.getKitManager().exists(string)) {
            return;
        }
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.closeInventory();
            return;
        }
        party.setKit(string);
        if (this.mode == PartyMode.SPLIT) {
            party.getTeams().clear();
            new PartyTeamMenu(this.plugin).open(player);
            return;
        }
        if (this.mode == PartyMode.DUELS) {
            new PartyDuelsConfirmMenu(this.plugin).open(player);
            return;
        }
        new PartyStartConfirmMenu(this.plugin, this.mode).open(player);
    }

    private static final class Page {
        private final String title;
        private final List<String> kits;

        private Page(String string, List<String> list) {
            this.title = string;
            this.kits = list;
        }
    }
}

