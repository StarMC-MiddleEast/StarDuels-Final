package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.PartyKitMenu;
import com.meowduels.gui.PartyMenu;
import com.meowduels.gui.PartyOpponentMenu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Party;
import com.meowduels.model.PartyMode;
import com.meowduels.util.Items;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PartyModeMenu
extends Menu {
    private static final String MENU = "partymode";
    private static final int ROWS = 3;
    private static final int SIZE = 27;
    private static final int SLOT_FFA = 11;
    private static final int SLOT_SPLIT = 13;
    private static final int SLOT_DUELS = 15;
    private static final int SLOT_BACK = 18;
    private static final String VALUE = "<#E6E8EB>";
    private static final String LABEL = "<#8E959D>";
    private static final String MUTED = "<#6B7079>";
    private static final String HINT = "<dark_gray>\u25b8 <#8E959D>";
    private final MeowDuels plugin;

    public PartyModeMenu(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    static String accent(String string) {
        return "<gradient:#7DE2FF:#4B7BFF>" + string + "</gradient>";
    }

    @Override
    public void build() {
    }

    @Override
    public void open(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-not-party-2", new String[0]));
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-start-2", new String[0]));
            return;
        }
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 3);
        this.createRaw(n, guiConfigManager.title(MENU, "<dark_gray>\u258f <gradient:#7DE2FF:#4B7BFF>\u1d18\u1d00\u0280\u1d1b\u028f \u1d0d\u1d00\u1d1b\u1d04\u029c</gradient>"));
        guiConfigManager.frame(MENU, this.inventory, n);
        this.put(guiConfigManager, n, PartyMode.FFA, guiConfigManager.slot(MENU, "ffa", 11, n), guiConfigManager.toggle(MENU, "ffa", PartyMode.FFA.isReady(), this.tokens(PartyMode.FFA), PartyModeMenu.ready(Material.DIAMOND_SWORD), PartyModeMenu.unready()));
        this.put(guiConfigManager, n, PartyMode.SPLIT, guiConfigManager.slot(MENU, "split", 13, n), guiConfigManager.toggle(MENU, "split", PartyMode.SPLIT.isReady(), this.tokens(PartyMode.SPLIT), PartyModeMenu.ready(Material.SHIELD), PartyModeMenu.unready()));
        this.put(guiConfigManager, n, PartyMode.DUELS, guiConfigManager.slot(MENU, "duels", 15, n), guiConfigManager.toggle(MENU, "duels", PartyMode.DUELS.isReady(), this.tokens(PartyMode.DUELS), PartyModeMenu.ready(Material.IRON_SWORD), PartyModeMenu.unready()));
        this.inventory.setItem(Math.min(n * 9 - 1, guiConfigManager.slot(MENU, "back", 18, n)), guiConfigManager.back(MENU, "back").tag(this.plugin.keyButton(), "mode-back").build());
        player.openInventory(this.inventory);
    }

    private static GuiConfigManager.Face ready(Material material) {
        return GuiConfigManager.Face.of(material, "<gradient:#7DE2FF:#4B7BFF>{mode}</gradient>", "", "{label}{description}", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d04\u029c\u1d0f\u1d0f\ua731\u1d07").glow();
    }

    private static GuiConfigManager.Face unready() {
        return GuiConfigManager.Face.of(Material.GRAY_DYE, "{muted}{mode}", "", "{muted}{description}", "", "{label}\u1d04\u1d0f\u1d0d\u026a\u0274\u0262 \ua731\u1d0f\u1d0f\u0274");
    }

    private Map<String, String> tokens(PartyMode partyMode) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("mode", partyMode.getLabel());
        hashMap.put("description", partyMode.getDescription());
        return hashMap;
    }

    private void put(GuiConfigManager guiConfigManager, int n, PartyMode partyMode, int n2, Items items) {
        items.hideTooltip();
        if (partyMode.isReady()) {
            items.tag(this.plugin.keyButton(), "mode-" + partyMode.name());
        }
        this.inventory.setItem(Math.max(0, Math.min(n * 9 - 1, n2)), items.build());
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        PartyMode partyMode;
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        if ("mode-back".equals(string)) {
            new PartyMenu(this.plugin).openFor(player);
            return;
        }
        if (!string.startsWith("mode-")) {
            return;
        }
        try {
            partyMode = PartyMode.valueOf(string.substring(5));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return;
        }
        if (!partyMode.isReady()) {
            return;
        }
        if (partyMode == PartyMode.DUELS) {
            new PartyOpponentMenu(this.plugin).open(player);
            return;
        }
        new PartyKitMenu(this.plugin, partyMode).open(player);
    }
}

