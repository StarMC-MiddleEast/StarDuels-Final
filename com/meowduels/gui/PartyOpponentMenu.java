package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.PartyKitMenu;
import com.meowduels.gui.PartyModeMenu;
import com.meowduels.gui.Style;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Party;
import com.meowduels.model.PartyMode;
import com.meowduels.util.Items;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PartyOpponentMenu
extends Menu {
    private static final int MAX_LIST_ROWS = 4;
    private static final String VALUE = "<#E6E8EB>";
    private static final String LABEL = "<#8E959D>";
    private static final String MUTED = "<#6B7079>";
    private static final String HINT = "<dark_gray>\u25b8 <#8E959D>";
    private static final String MENU = "partyopponent";
    private final MeowDuels plugin;

    public PartyOpponentMenu(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @Override
    public void build() {
    }

    @Override
    public void open(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-start-3", new String[0]));
            return;
        }
        GuiConfigManager guiConfigManager = this.plugin.gui();
        List<Party> list = this.plugin.getPartyManager().opponentParties(party);
        int n = Style.gridRows(list.size(), guiConfigManager.rows(MENU, 4));
        this.createRaw(n, guiConfigManager.title(MENU, "<dark_gray>\u258f <gradient:#7DE2FF:#4B7BFF>\u1d18\u1d00\u0280\u1d1b\u028f \u1d05\u1d1c\u1d07\u029f\ua731</gradient> {sep}{value}\u1d18\u026a\u1d04\u1d0b \u1d00 \u1d18\u1d00\u0280\u1d1b\u028f"));
        guiConfigManager.frame(MENU, this.inventory, n);
        int n2 = (n - 2) * 7;
        for (int i = 0; i < list.size() && i < n2; ++i) {
            Party party2 = list.get(i);
            this.inventory.setItem(Style.gridSlot(i), this.card(guiConfigManager, party, party2));
        }
        if (list.isEmpty()) {
            this.inventory.setItem(Style.gridSlot(3), guiConfigManager.face(MENU, "empty", null, GuiConfigManager.Face.of(Material.BARRIER, "<#FF8A93>\u0274\u1d0f \u1d18\u1d00\u0280\u1d1b\u026a\u1d07\ua731 \u1d1b\u1d0f \ua730\u026a\u0262\u029c\u1d1b", "", "{label}\u0274\u1d0f\u0299\u1d0f\u1d05\u028f \u1d07\u029f\ua731\u1d07 \u029c\u1d00\ua731 \u1d00 \u1d18\u1d00\u0280\u1d1b\u028f \u0280\u026a\u0262\u029c\u1d1b \u0274\u1d0f\u1d21", "", "{muted}\u1d1b\u029c\u1d07\u028f \u0274\u1d07\u1d07\u1d05 \u1d1b\u1d0f \u0299\u1d07 \u1d0f\u1d1c\u1d1b \u1d0f\ua730 \u1d00 \u1d0d\u1d00\u1d1b\u1d04\u029c")).hideTooltip().build());
        }
        this.inventory.setItem(Style.backSlot(n), guiConfigManager.back(MENU, "back").tag(this.plugin.keyButton(), "opp-back").build());
        player.openInventory(this.inventory);
    }

    private ItemStack card(GuiConfigManager guiConfigManager, Party party, Party party2) {
        UUID uUID = party2.getLeader();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("leader", this.nameOf(uUID));
        hashMap.put("size", String.valueOf(party2.size()));
        ArrayList<String> arrayList = new ArrayList<String>();
        int n = 0;
        for (UUID uUID2 : party2.getMembers()) {
            HashMap<String, String> hashMap2;
            if (n >= 8) {
                hashMap2 = new HashMap<String, String>();
                hashMap2.put("count", String.valueOf(party2.size() - n));
                arrayList.add(guiConfigManager.line(MENU, "party", "more-line", "{muted}  +{count} \u1d0d\u1d0f\u0280\u1d07", hashMap2));
                break;
            }
            hashMap2 = new HashMap();
            hashMap2.put("mark", uUID2.equals(uUID) ? "<#FFD65C>\u2605 " : "<dark_gray>\u2022 ");
            hashMap2.put("colour", Bukkit.getPlayer((UUID)uUID2) != null ? Style.VALUE : Style.MUTED);
            hashMap2.put("player", this.nameOf(uUID2));
            arrayList.add(guiConfigManager.line(MENU, "party", "member-line", "  {mark}{colour}{player}", hashMap2));
            ++n;
        }
        HashMap hashMap3 = new HashMap();
        hashMap3.put("members", arrayList);
        return guiConfigManager.toggle(MENU, "party", uUID.equals(party.getDuelTarget()), hashMap, hashMap3, PartyOpponentMenu.card("{good}\ua731\u1d07\u029f\u1d07\u1d04\u1d1b\u1d07\u1d05").glow(), PartyOpponentMenu.card("{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \ua730\u026a\u0262\u029c\u1d1b \u1d1b\u029c\u1d07\u1d0d")).skull(Bukkit.getOfflinePlayer((UUID)uUID)).hideTooltip().tag(this.plugin.keyButton(), "opp:" + String.valueOf(uUID)).build();
    }

    private static GuiConfigManager.Face card(String string) {
        return GuiConfigManager.Face.of(Material.PLAYER_HEAD, "<gradient:#7DE2FF:#4B7BFF>{leader}</gradient>{label}'\ua731 \u1d18\u1d00\u0280\u1d1b\u028f", "", "{label}\u1d0d\u1d07\u1d0d\u0299\u1d07\u0280\ua731 <dark_gray>\u00bb {value}{size}", "", "{members}", "", string);
    }

    private String nameOf(UUID uUID) {
        Player player = Bukkit.getPlayer((UUID)uUID);
        if (player != null) {
            return player.getName();
        }
        String string = Bukkit.getOfflinePlayer((UUID)uUID).getName();
        return string == null ? "?" : string;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        UUID uUID;
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        if ("opp-back".equals(string)) {
            new PartyModeMenu(this.plugin).open(player);
            return;
        }
        if (!string.startsWith("opp:")) {
            return;
        }
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.closeInventory();
            return;
        }
        try {
            uUID = UUID.fromString(string.substring(4));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return;
        }
        Party party2 = this.plugin.getPartyManager().partyLedBy(uUID);
        if (party2 == null || party2 == party || party2.isFighting()) {
            player.sendMessage(this.plugin.messages().get("notice.party.party-isn-t-available", new String[0]));
            this.open(player);
            return;
        }
        party.setDuelTarget(uUID);
        new PartyKitMenu(this.plugin, PartyMode.DUELS).open(player);
    }
}

