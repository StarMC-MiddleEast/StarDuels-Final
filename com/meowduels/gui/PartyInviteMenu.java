package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.PartyMenu;
import com.meowduels.gui.Style;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Party;
import com.meowduels.util.Items;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PartyInviteMenu
extends Menu {
    private static final int MAX_LIST_ROWS = 4;
    private static final String VALUE = "<#E6E8EB>";
    private static final String LABEL = "<#8E959D>";
    private static final String HINT = "<dark_gray>\u25b8 <#8E959D>";
    private static final String MENU = "partyinvite";
    private final MeowDuels plugin;

    public PartyInviteMenu(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    private List<Player> candidates(Player player) {
        ArrayList<Player> arrayList = new ArrayList<Player>();
        for (Player player2 : Bukkit.getOnlinePlayers()) {
            if (player2.getUniqueId().equals(player.getUniqueId()) || this.plugin.getPartyManager().inParty(player2.getUniqueId())) continue;
            arrayList.add(player2);
        }
        return arrayList;
    }

    @Override
    public void build() {
    }

    @Override
    public void open(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-invite", new String[0]));
            return;
        }
        GuiConfigManager guiConfigManager = this.plugin.gui();
        List<Player> list = this.candidates(player);
        int n = Style.gridRows(list.size(), guiConfigManager.rows(MENU, 4));
        this.createRaw(n, guiConfigManager.title(MENU, "<dark_gray>\u258f <gradient:#FF8AD0:#B04BD6>\u026a\u0274\u1d20\u026a\u1d1b\u1d07 \u1d1b\u1d0f \u1d18\u1d00\u0280\u1d1b\u028f</gradient>"));
        guiConfigManager.frame(MENU, this.inventory, n);
        int n2 = (n - 2) * 7;
        for (int i = 0; i < list.size() && i < n2; ++i) {
            Player player2 = list.get(i);
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("player", player2.getName());
            this.inventory.setItem(Style.gridSlot(i), guiConfigManager.toggle(MENU, "player", party.isInvited(player2.getUniqueId()), hashMap, GuiConfigManager.Face.of(Material.PLAYER_HEAD, "{value}{player}", "", "{good}\u026a\u0274\u1d20\u026a\u1d1b\u1d07\u1d05 - \u1d21\u1d00\u026a\u1d1b\u026a\u0274\u0262 \ua730\u1d0f\u0280 \u1d1b\u029c\u1d07\u1d0d").glow(), GuiConfigManager.Face.of(Material.PLAYER_HEAD, "{value}{player}", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u026a\u0274\u1d20\u026a\u1d1b\u1d07")).skull((OfflinePlayer)player2).hideTooltip().tag(this.plugin.keyButton(), "invite:" + player2.getName()).build());
        }
        if (list.isEmpty()) {
            this.inventory.setItem(Style.gridSlot(3), guiConfigManager.face(MENU, "empty", null, GuiConfigManager.Face.of(Material.BARRIER, "<#FF8A93>\u0274\u1d0f\u0299\u1d0f\u1d05\u028f \u1d1b\u1d0f \u026a\u0274\u1d20\u026a\u1d1b\u1d07", "", "{label}\u1d07\u1d20\u1d07\u0280\u028f\u1d0f\u0274\u1d07 \u1d0f\u0274\u029f\u026a\u0274\u1d07 \u026a\ua731 \u1d00\u029f\u0280\u1d07\u1d00\u1d05\u028f \u026a\u0274 \u1d00 \u1d18\u1d00\u0280\u1d1b\u028f")).hideTooltip().build());
        }
        this.inventory.setItem(Style.backSlot(n), guiConfigManager.back(MENU, "back").tag(this.plugin.keyButton(), "party-back").build());
        player.openInventory(this.inventory);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        if ("party-back".equals(string)) {
            new PartyMenu(this.plugin).openFor(player);
            return;
        }
        if (!string.startsWith("invite:")) {
            return;
        }
        Player player2 = this.plugin.getServer().getPlayerExact(string.substring(7));
        if (player2 == null) {
            this.open(player);
            return;
        }
        this.plugin.getPartyManager().invite(player, player2);
        this.open(player);
    }
}

