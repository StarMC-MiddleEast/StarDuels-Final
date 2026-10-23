package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.PartyMenu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Party;
import com.meowduels.util.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PartySettingsMenu
extends Menu {
    private static final int ROWS = 3;
    private static final int SLOT_OPEN = 11;
    private static final int SLOT_FF = 13;
    private static final int SLOT_BACK = 18;
    private static final String LABEL = "<#8E959D>";
    private static final String MUTED = "<#6B7079>";
    private static final String HINT = "<dark_gray>\u25b8 <#8E959D>";
    private final MeowDuels plugin;
    private static final String MENU = "partysettings";
    private static final String ON_STATE = "{good}{tick}\u1d0f\u0274";
    private static final String OFF_STATE = "{bad}{cross}\u1d0f\ua730\ua730";
    private static final String TOGGLE_HINT = "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d1b\u1d0f\u0262\u0262\u029f\u1d07";

    public PartySettingsMenu(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 3);
        this.createRaw(n, guiConfigManager.title(MENU, "<dark_gray>\u258f <gradient:#FF8AD0:#B04BD6>\u1d18\u1d00\u0280\u1d1b\u028f \ua731\u1d07\u1d1b\u1d1b\u026a\u0274\u0262\ua731</gradient>"));
        guiConfigManager.frame(MENU, this.inventory, n);
    }

    @Override
    public void open(Player player) {
        this.build();
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null) {
            return;
        }
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-change", new String[0]));
            return;
        }
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = this.inventory.getSize() / 9;
        int n2 = this.inventory.getSize();
        this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "open", 11, n)), guiConfigManager.toggle(MENU, "open", party.isOpenToAll(), null, GuiConfigManager.Face.of(Material.ENDER_EYE, "{value}\u1d0f\u1d18\u1d07\u0274 \u1d1b\u1d0f \u1d00\u029f\u029f", "", "{label}\u1d00\u0274\u028f\u1d0f\u0274\u1d07 \u1d04\u1d00\u0274 \u1d0a\u1d0f\u026a\u0274 \u1d21\u026a\u1d1b\u029c\u1d0f\u1d1c\u1d1b \u1d00\u0274 \u026a\u0274\u1d20\u026a\u1d1b\u1d07", "", ON_STATE, "", TOGGLE_HINT).glow(), GuiConfigManager.Face.of(Material.ENDER_EYE, "{value}\u1d0f\u1d18\u1d07\u0274 \u1d1b\u1d0f \u1d00\u029f\u029f", "", "{label}\u1d00\u0274\u028f\u1d0f\u0274\u1d07 \u1d04\u1d00\u0274 \u1d0a\u1d0f\u026a\u0274 \u1d21\u026a\u1d1b\u029c\u1d0f\u1d1c\u1d1b \u1d00\u0274 \u026a\u0274\u1d20\u026a\u1d1b\u1d07", "", OFF_STATE, "", TOGGLE_HINT)).tag(this.plugin.keyButton(), "party-set-open").build());
        this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "friendly-fire", 13, n)), guiConfigManager.toggle(MENU, "friendly-fire", party.isFriendlyFire(), null, GuiConfigManager.Face.of(Material.IRON_SWORD, "{value}\ua730\u0280\u026a\u1d07\u0274\u1d05\u029f\u028f \ua730\u026a\u0280\u1d07", "", "{label}\u1d0d\u1d07\u1d0d\u0299\u1d07\u0280\ua731 \u1d04\u1d00\u0274 \u029c\u1d1c\u0280\u1d1b \u1d07\u1d00\u1d04\u029c \u1d0f\u1d1b\u029c\u1d07\u0280 \u1d0f\u1d1c\u1d1b\ua731\u026a\u1d05\u1d07 \u1d00 \u1d0d\u1d00\u1d1b\u1d04\u029c", "", ON_STATE, "", TOGGLE_HINT).glow(), GuiConfigManager.Face.of(Material.IRON_SWORD, "{value}\ua730\u0280\u026a\u1d07\u0274\u1d05\u029f\u028f \ua730\u026a\u0280\u1d07", "", "{label}\u1d0d\u1d07\u1d0d\u0299\u1d07\u0280\ua731 \u1d04\u1d00\u0274 \u029c\u1d1c\u0280\u1d1b \u1d07\u1d00\u1d04\u029c \u1d0f\u1d1b\u029c\u1d07\u0280 \u1d0f\u1d1c\u1d1b\ua731\u026a\u1d05\u1d07 \u1d00 \u1d0d\u1d00\u1d1b\u1d04\u029c", "", OFF_STATE, "", TOGGLE_HINT)).tag(this.plugin.keyButton(), "party-set-ff").build());
        this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "back", 18, n)), guiConfigManager.back(MENU, "back").tag(this.plugin.keyButton(), "party-back").build());
        player.openInventory(this.inventory);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.closeInventory();
            return;
        }
        if ("party-set-open".equals(string)) {
            party.setOpenToAll(!party.isOpenToAll());
            this.open(player);
        } else if ("party-set-ff".equals(string)) {
            party.setFriendlyFire(!party.isFriendlyFire());
            this.open(player);
        } else if ("party-back".equals(string)) {
            new PartyMenu(this.plugin).openFor(player);
        }
    }
}

