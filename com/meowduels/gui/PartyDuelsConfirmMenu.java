package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.PartyKitMenu;
import com.meowduels.gui.PartyOpponentMenu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.model.Party;
import com.meowduels.model.PartyMode;
import com.meowduels.util.Items;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PartyDuelsConfirmMenu
extends Menu {
    private static final String MENU = "partyduelsconfirm";
    private static final int ROWS = 4;
    private static final int SLOT_INFO = 4;
    private static final int[] ROUND_SLOTS = new int[]{11, 13, 15};
    private static final int[] ROUND_VALUES = new int[]{1, 3, 5};
    private static final int SLOT_BACK = 27;
    private static final int SLOT_START = 22;
    private static final String VALUE = "<#E6E8EB>";
    private static final String LABEL = "<#8E959D>";
    private static final String MUTED = "<#6B7079>";
    private static final String SEP = "<dark_gray>\u203a ";
    private static final String HINT = "<dark_gray>\u25b8 <#8E959D>";
    private final MeowDuels plugin;

    public PartyDuelsConfirmMenu(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @Override
    public void build() {
    }

    @Override
    public void open(Player player) {
        int n;
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-start", new String[0]));
            return;
        }
        Party party2 = this.plugin.getPartyManager().partyLedBy(party.getDuelTarget());
        if (party2 == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.party-gone-pick-another", new String[0]));
            new PartyOpponentMenu(this.plugin).open(player);
            return;
        }
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n2 = guiConfigManager.rows(MENU, 4);
        int n3 = n2 * 9 - 1;
        int n4 = this.onlineCount(party);
        int n5 = this.onlineCount(party2);
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("opponent", this.nameOf(party2.getLeader()));
        hashMap.put("kit", this.kitLabel(party));
        hashMap.put("ours", String.valueOf(n4));
        hashMap.put("theirs", String.valueOf(n5));
        hashMap.put("rounds", String.valueOf(party.getDuelRounds()));
        hashMap.put("description", PartyMode.DUELS.getDescription());
        this.createRaw(n2, guiConfigManager.title(MENU, "<dark_gray>\u258f <gradient:#7DE2FF:#4B7BFF>\u1d18\u1d00\u0280\u1d1b\u028f \u1d05\u1d1c\u1d07\u029f\ua731</gradient> {sep}{value}{opponent}", hashMap));
        guiConfigManager.frame(MENU, this.inventory, n2);
        this.inventory.setItem(Math.min(n3, guiConfigManager.slot(MENU, "info", 4, n2)), guiConfigManager.face(MENU, "info", hashMap, GuiConfigManager.Face.of(Material.PLAYER_HEAD, "<gradient:#7DE2FF:#4B7BFF>{opponent}</gradient>{label}'\ua731 \u1d18\u1d00\u0280\u1d1b\u028f", "", "{label}\u1d0b\u026a\u1d1b {sep}{kit}", "{label}\ua731\u026a\u1d05\u1d07\ua731 {sep}{value}{ours} {label}\u1d20\ua731 {value}{theirs}", "{label}\u0280\u1d0f\u1d1c\u0274\u1d05\ua731 {sep}{value}\ua730\u026a\u0280\ua731\u1d1b \u1d1b\u1d0f {rounds}", "", "{label}{description}")).skull(Bukkit.getOfflinePlayer((UUID)party2.getLeader())).hideTooltip().build());
        for (n = 0; n < ROUND_SLOTS.length; ++n) {
            int n6 = ROUND_VALUES[n];
            HashMap<String, String> hashMap2 = new HashMap<String, String>();
            hashMap2.put("rounds", String.valueOf(n6));
            this.inventory.setItem(ROUND_SLOTS[n], guiConfigManager.toggle(MENU, "rounds", party.getDuelRounds() == n6, hashMap2, GuiConfigManager.Face.of(Material.PAPER, "{good}\ua730\u026a\u0280\ua731\u1d1b \u1d1b\u1d0f {rounds}", "", "{good}\ua731\u1d07\u029f\u1d07\u1d04\u1d1b\u1d07\u1d05").amount(n6).glow(), GuiConfigManager.Face.of(Material.PAPER, "{value}\ua730\u026a\u0280\ua731\u1d1b \u1d1b\u1d0f {rounds}", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d18\u026a\u1d04\u1d0b").amount(n6)).hideTooltip().tag(this.plugin.keyButton(), "pd-rounds:" + n6).build());
        }
        this.inventory.setItem(Math.min(n3, guiConfigManager.slot(MENU, "back", 27, n2)), guiConfigManager.back(MENU, "back").tag(this.plugin.keyButton(), "pd-back").build());
        n = n4 > 0 && n5 > 0 && party.getKit() != null ? 1 : 0;
        this.inventory.setItem(Math.min(n3, guiConfigManager.slot(MENU, "start", 22, n2)), guiConfigManager.toggle(MENU, "start", n != 0, hashMap, GuiConfigManager.Face.of(Material.LIME_DYE, "<gradient:{go-from}:{go-to}>{tick}\ua731\u1d1b\u1d00\u0280\u1d1b</gradient>", "", "{label}\u1d1b\u1d07\u1d00\u1d0d \u1d20\ua731 \u1d1b\u1d07\u1d00\u1d0d \u1d1c\u0274\u1d1b\u026a\u029f \u1d0f\u0274\u1d07 \ua731\u026a\u1d05\u1d07 \u026a\ua731 \u1d0f\u1d1c\u1d1b").glow(), GuiConfigManager.Face.of(Material.GRAY_DYE, "{muted}{cross}\u0274\u1d0f\u1d1b \u0280\u1d07\u1d00\u1d05\u028f", "", "{bad}\u0299\u1d0f\u1d1b\u029c \u1d18\u1d00\u0280\u1d1b\u026a\u1d07\ua731 \u0274\u1d07\u1d07\u1d05 \ua731\u1d0f\u1d0d\u1d07\u1d0f\u0274\u1d07 \u1d0f\u0274\u029f\u026a\u0274\u1d07")).hideTooltip().tag(this.plugin.keyButton(), n != 0 ? "pd-go" : "pd-none").build());
        player.openInventory(this.inventory);
    }

    private int onlineCount(Party party) {
        int n = 0;
        for (UUID uUID : party.getMembers()) {
            if (Bukkit.getPlayer((UUID)uUID) == null) continue;
            ++n;
        }
        return n;
    }

    private String kitLabel(Party party) {
        Kit kit;
        Kit kit2 = kit = party.getKit() == null ? null : this.plugin.getKitManager().get(party.getKit());
        if (kit == null) {
            return "<#6B7079>\u0274\u1d0f\u1d1b \u1d04\u029c\u1d0f\ua731\u1d07\u0274";
        }
        return kit.withIcon(kit.getDisplayName() == null || kit.getDisplayName().isEmpty() ? VALUE + kit.getName() : kit.getDisplayName());
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
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.closeInventory();
            return;
        }
        if ("pd-back".equals(string)) {
            new PartyKitMenu(this.plugin, PartyMode.DUELS).open(player);
            return;
        }
        if (string.startsWith("pd-rounds:")) {
            try {
                party.setDuelRounds(Integer.parseInt(string.substring(10)));
            }
            catch (NumberFormatException numberFormatException) {
                return;
            }
            this.open(player);
            return;
        }
        if ("pd-go".equals(string)) {
            player.closeInventory();
            this.plugin.getPartyManager().challengeDuels(player);
        }
    }
}

