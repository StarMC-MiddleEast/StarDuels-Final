package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.PartyKitMenu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.model.Party;
import com.meowduels.model.PartyMode;
import com.meowduels.util.Items;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PartyStartConfirmMenu
extends Menu {
    private static final String MENU = "partystartconfirm";
    private static final int ROWS = 3;
    private static final int SLOT_BACK = 18;
    private static final int SLOT_WHAT = 12;
    private static final int SLOT_START = 14;
    private static final String VALUE = "<#E6E8EB>";
    private static final String LABEL = "<#8E959D>";
    private static final String MUTED = "<#6B7079>";
    private static final String SEP = "<dark_gray>\u203a ";
    private final MeowDuels plugin;
    private final PartyMode mode;

    public PartyStartConfirmMenu(MeowDuels meowDuels, PartyMode partyMode) {
        this.plugin = meowDuels;
        this.mode = partyMode;
    }

    @Override
    public void build() {
    }

    @Override
    public void open(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-start-4", new String[0]));
            return;
        }
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 3);
        int n2 = n * 9;
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("mode", this.mode.getLabel());
        hashMap.put("description", this.mode.getDescription());
        hashMap.put("kit", this.kitLabel(party));
        hashMap.put("players", String.valueOf(party.size()));
        this.createRaw(n, guiConfigManager.title(MENU, "<dark_gray>\u258f <gradient:#7DE2FF:#4B7BFF>\ua731\u1d1b\u1d00\u0280\u1d1b \u1d0d\u1d00\u1d1b\u1d04\u029c</gradient>", hashMap));
        guiConfigManager.frame(MENU, this.inventory, n);
        this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "back", 18, n)), guiConfigManager.back(MENU, "back").tag(this.plugin.keyButton(), "start-back").build());
        this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "what", 12, n)), guiConfigManager.face(MENU, "what", hashMap, GuiConfigManager.Face.of(Material.PAPER, "<gradient:#7DE2FF:#4B7BFF>{mode}</gradient>", "", "{label}\u1d0b\u026a\u1d1b {sep}{kit}", "{label}\u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731 {sep}{value}{players}", "", "{label}{description}")).hideTooltip().build());
        this.inventory.setItem(Math.min(n2 - 1, guiConfigManager.slot(MENU, "start", 14, n)), guiConfigManager.face(MENU, "start", hashMap, GuiConfigManager.Face.of(Material.LIME_DYE, "<gradient:{go-from}:{go-to}>{tick}\ua731\u1d1b\u1d00\u0280\u1d1b</gradient>", "", "{label}\ua731\u1d07\u0274\u1d05 \u1d07\u1d20\u1d07\u0280\u028f\u1d0f\u0274\u1d07 \u026a\u0274 \u1d00\u0274\u1d05 \u0299\u1d07\u0262\u026a\u0274 \u1d1b\u029c\u1d07 \u1d04\u1d0f\u1d1c\u0274\u1d1b\u1d05\u1d0f\u1d21\u0274").glow()).hideTooltip().tag(this.plugin.keyButton(), "start-go").build());
        player.openInventory(this.inventory);
    }

    private String kitLabel(Party party) {
        Kit kit;
        Kit kit2 = kit = party.getKit() == null ? null : this.plugin.getKitManager().get(party.getKit());
        if (kit == null) {
            return "<#6B7079>\u0274\u1d0f\u1d1b \u1d04\u029c\u1d0f\ua731\u1d07\u0274";
        }
        return kit.withIcon(kit.getDisplayName() == null || kit.getDisplayName().isEmpty() ? VALUE + kit.getName() : kit.getDisplayName());
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if ("start-back".equals(string)) {
            new PartyKitMenu(this.plugin, this.mode).open(player);
        } else if ("start-go".equals(string)) {
            player.closeInventory();
            this.plugin.getPartyManager().startMatch(player, this.mode);
        }
    }
}

