package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Party;
import com.meowduels.util.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PartyConfirmMenu
extends Menu {
    private static final int ROWS = 3;
    private static final int SLOT_NO = 11;
    private static final int SLOT_WHAT = 13;
    private static final int SLOT_YES = 15;
    private final MeowDuels plugin;
    private final boolean leader;
    private static final String MENU = "partyconfirm";

    public PartyConfirmMenu(MeowDuels meowDuels, Player player) {
        this.plugin = meowDuels;
        Party party = meowDuels.getPartyManager().partyOf(player.getUniqueId());
        this.leader = party != null && party.isLeader(player.getUniqueId());
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 3);
        int n2 = n * 9;
        this.createRaw(n, this.leader ? guiConfigManager.titleOf(MENU, "leader", "<dark_gray>\u258f <gradient:#FF6B6B:#A01028>\u1d05\u026a\ua731\u0299\u1d00\u0274\u1d05 \u1d18\u1d00\u0280\u1d1b\u028f</gradient>", null) : guiConfigManager.titleOf(MENU, "member", "<dark_gray>\u258f <gradient:#FF6B6B:#A01028>\u029f\u1d07\u1d00\u1d20\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f</gradient>", null));
        guiConfigManager.frame(MENU, this.inventory, n);
        this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "stay", 11, n)), guiConfigManager.face(MENU, "stay", null, GuiConfigManager.Face.of(Material.LIME_DYE, "<gradient:{go-from}:{go-to}>{tick}\ua731\u1d1b\u1d00\u028f</gradient>", "", "{label}\u0274\u1d0f\u1d1b\u029c\u026a\u0274\u0262 \u1d04\u029c\u1d00\u0274\u0262\u1d07\ua731").glow()).tag(this.plugin.keyButton(), "party-no").build());
        int n3 = this.leader ? guiConfigManager.slot(MENU, "what-leader", 13, n) : guiConfigManager.slot(MENU, "what-member", 13, n);
        this.inventory.setItem(Math.min(n2 - 1, n3), (this.leader ? guiConfigManager.face(MENU, "what-leader", null, GuiConfigManager.Face.of(Material.PAPER, "{value}\u1d05\u026a\ua731\u0299\u1d00\u0274\u1d05 \u1d18\u1d00\u0280\u1d1b\u028f", "", "<#FF8A93>\u1d07\u1d20\u1d07\u0280\u028f\u1d0f\u0274\u1d07 \u026a\ua731 \u0280\u1d07\u1d0d\u1d0f\u1d20\u1d07\u1d05 \ua730\u0280\u1d0f\u1d0d \u1d1b\u029c\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f")) : guiConfigManager.face(MENU, "what-member", null, GuiConfigManager.Face.of(Material.PAPER, "{value}\u029f\u1d07\u1d00\u1d20\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f", "", "{label}\u1d1b\u029c\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f \u1d04\u1d00\u0280\u0280\u026a\u1d07\ua731 \u1d0f\u0274 \u1d21\u026a\u1d1b\u029c\u1d0f\u1d1c\u1d1b \u028f\u1d0f\u1d1c"))).hideTooltip().build());
        int n4 = this.leader ? guiConfigManager.slot(MENU, "leave-leader", 15, n) : guiConfigManager.slot(MENU, "leave-member", 15, n);
        this.inventory.setItem(Math.min(n2 - 1, n4), (this.leader ? guiConfigManager.face(MENU, "leave-leader", null, GuiConfigManager.Face.of(Material.RED_DYE, "<gradient:{no-from}:{no-to}>{cross}\u1d05\u026a\ua731\u0299\u1d00\u0274\u1d05 \u1d18\u1d00\u0280\u1d1b\u028f</gradient>", "", "{label}\u1d1b\u029c\u026a\ua731 \u1d04\u1d00\u0274\u0274\u1d0f\u1d1b \u0299\u1d07 \u1d1c\u0274\u1d05\u1d0f\u0274\u1d07")) : guiConfigManager.face(MENU, "leave-member", null, GuiConfigManager.Face.of(Material.RED_DYE, "<gradient:{no-from}:{no-to}>{cross}\u029f\u1d07\u1d00\u1d20\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f</gradient>", "", "{label}\u1d1b\u029c\u026a\ua731 \u1d04\u1d00\u0274\u0274\u1d0f\u1d1b \u0299\u1d07 \u1d1c\u0274\u1d05\u1d0f\u0274\u1d07"))).hideTooltip().tag(this.plugin.keyButton(), "party-yes").build());
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if ("party-yes".equals(string)) {
            player.closeInventory();
            this.plugin.getPartyManager().leave(player);
        } else if ("party-no".equals(string)) {
            player.closeInventory();
        }
    }
}

