package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.PartyConfirmMenu;
import com.meowduels.gui.PartyInviteMenu;
import com.meowduels.gui.PartyModeMenu;
import com.meowduels.gui.PartySettingsMenu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Kit;
import com.meowduels.model.Party;
import com.meowduels.util.Items;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PartyMenu
extends Menu {
    private static final int ROWS = 4;
    private static final int SIZE = 36;
    private static final int SLOT_CARD = 4;
    private static final int SLOT_KIT = 11;
    private static final int SLOT_INVITE = 13;
    private static final int SLOT_SETTINGS = 15;
    private static final int MEMBER_ROW = 19;
    private static final int MEMBER_SLOTS = 7;
    private static final int SLOT_START = 29;
    private static final int SLOT_LEAVE_LEADER = 33;
    private static final int SLOT_LEAVE_MEMBER = 31;
    private static final String VALUE = "<#E6E8EB>";
    private static final String LABEL = "<#8E959D>";
    private static final String MUTED = "<#6B7079>";
    private static final String SEP = "<dark_gray>\u203a ";
    private static final String HINT = "<dark_gray>\u25b8 <#8E959D>";
    private final MeowDuels plugin;
    private static final String MENU = "party";

    public PartyMenu(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    private static String accent(String string) {
        return "<gradient:#FF8AD0:#B04BD6>" + string + "</gradient>";
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 4);
        this.createRaw(n, guiConfigManager.title(MENU, "<dark_gray>\u258f <gray><bold>\u1d0d\u1d00\u0274\u1d00\u0262\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f</bold></gray>"));
        guiConfigManager.frame(MENU, this.inventory, n);
    }

    public void openFor(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null) {
            player.sendMessage(this.plugin.messages().get("notice.party.re-not-party", new String[0]));
            return;
        }
        this.build();
        boolean bl = party.isLeader(player.getUniqueId());
        this.inventory.setItem(4, this.card(party));
        this.addMembers(party);
        if (bl) {
            this.addLeaderButtons(party);
        }
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = this.inventory.getSize() / 9;
        int n2 = bl ? guiConfigManager.slot(MENU, "leave-leader", 33, n) : guiConfigManager.slot(MENU, "leave-member", 31, n);
        this.inventory.setItem(Math.min(this.inventory.getSize() - 1, n2), (bl ? guiConfigManager.face(MENU, "leave-leader", null, GuiConfigManager.Face.of(Material.RED_DYE, "<gradient:{no-from}:{no-to}>{cross}\u1d05\u026a\ua731\u0299\u1d00\u0274\u1d05 \u1d18\u1d00\u0280\u1d1b\u028f</gradient>", "", "{label}\u1d07\u0274\u1d05\ua731 \u1d1b\u029c\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f \ua730\u1d0f\u0280 \u1d07\u1d20\u1d07\u0280\u028f\u1d0f\u0274\u1d07", "", "{hint}\u028f\u1d0f\u1d1c'\u029f\u029f \u0299\u1d07 \u1d00\ua731\u1d0b\u1d07\u1d05 \u1d1b\u1d0f \u1d04\u1d0f\u0274\ua730\u026a\u0280\u1d0d")) : guiConfigManager.face(MENU, "leave-member", null, GuiConfigManager.Face.of(Material.RED_DYE, "<gradient:{no-from}:{no-to}>{cross}\u029f\u1d07\u1d00\u1d20\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f</gradient>", "", "{label}\u1d1b\u029c\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f \u1d04\u1d00\u0280\u0280\u026a\u1d07\ua731 \u1d0f\u0274 \u1d21\u026a\u1d1b\u029c\u1d0f\u1d1c\u1d1b \u028f\u1d0f\u1d1c", "", "{hint}\u028f\u1d0f\u1d1c'\u029f\u029f \u0299\u1d07 \u1d00\ua731\u1d0b\u1d07\u1d05 \u1d1b\u1d0f \u1d04\u1d0f\u0274\ua730\u026a\u0280\u1d0d"))).hideTooltip().tag(this.plugin.keyButton(), "party-leave-open").build());
        player.openInventory(this.inventory);
    }

    private ItemStack card(Party party) {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        boolean bl = party.isFighting();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("leader", this.nameOf(party.getLeader()));
        hashMap.put("size", String.valueOf(party.size()));
        hashMap.put("kit", this.kitLabel(party));
        hashMap.put("alive", String.valueOf(party.getAlive().size()));
        hashMap.put("mode", bl ? guiConfigManager.line(MENU, "card", "mode-fighting", "{value}{mode-label}", Collections.singletonMap("mode-label", party.getMode().getLabel())) : guiConfigManager.line(MENU, "card", "mode-idle", "{muted}\u0274\u1d0f\u1d1b \u1d18\u026a\u1d04\u1d0b\u1d07\u1d05 \u028f\u1d07\u1d1b", null));
        hashMap.put("status", bl ? guiConfigManager.line(MENU, "card", "status-fighting", "{good}\ua730\u026a\u0262\u029c\u1d1b\u026a\u0274\u0262 <dark_gray>\u00b7 {value}{alive} \ua731\u1d1b\u026a\u029f\u029f \u026a\u0274", Collections.singletonMap("alive", String.valueOf(party.getAlive().size()))) : guiConfigManager.line(MENU, "card", "status-idle", "{muted}\u1d21\u1d00\u026a\u1d1b\u026a\u0274\u0262 \ua730\u1d0f\u0280 \u1d1b\u029c\u1d07 \u029f\u1d07\u1d00\u1d05\u1d07\u0280", null));
        return guiConfigManager.face(MENU, "card", hashMap, GuiConfigManager.Face.of(Material.PAPER, "<gradient:#FF8AD0:#B04BD6>\u1d18\u1d00\u0280\u1d1b\u028f</gradient>", "", "{label}\u029f\u1d07\u1d00\u1d05\u1d07\u0280 {sep}{value}{leader}", "{label}\u1d0d\u1d07\u1d0d\u0299\u1d07\u0280\ua731 {sep}{value}{size}", "{label}\u1d0b\u026a\u1d1b {sep}{kit}", "{label}\u1d0d\u1d0f\u1d05\u1d07 {sep}{mode}", "", "{label}\ua731\u1d1b\u1d00\u1d1b\u1d1c\ua731 {sep}{status}")).hideTooltip().build();
    }

    private void addLeaderButtons(Party party) {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = this.inventory.getSize() / 9;
        int n2 = this.inventory.getSize() - 1;
        Kit kit = this.kitOf(party);
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("kit", this.kitLabel(party));
        hashMap.put("size", String.valueOf(party.size()));
        Material material = guiConfigManager.material(MENU, "kit", kit != null ? kit.getIcon() : Material.GOLDEN_APPLE);
        this.inventory.setItem(Math.min(n2, guiConfigManager.slot(MENU, "kit", 11, n)), guiConfigManager.toggle(MENU, "kit", kit != null, hashMap, GuiConfigManager.Face.of(material, "{value}\u1d0b\u026a\u1d1b", "", "{label}\ua731\u1d07\u029f\u1d07\u1d04\u1d1b\u1d07\u1d05 {sep}{kit}", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d18\u026a\u1d04\u1d0b \u1d00 \u1d0b\u026a\u1d1b").glow(), GuiConfigManager.Face.of(material, "{value}\u1d0b\u026a\u1d1b", "", "{label}\ua731\u1d07\u029f\u1d07\u1d04\u1d1b\u1d07\u1d05 {sep}{kit}", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d18\u026a\u1d04\u1d0b \u1d00 \u1d0b\u026a\u1d1b")).hideTooltip().tag(this.plugin.keyButton(), "party-kit").build());
        this.inventory.setItem(Math.min(n2, guiConfigManager.slot(MENU, "invite", 13, n)), guiConfigManager.face(MENU, "invite", hashMap, GuiConfigManager.Face.of(Material.PLAYER_HEAD, "{value}\u026a\u0274\u1d20\u026a\u1d1b\u1d07", "", "{label}\u1d00\u1d05\u1d05 \ua731\u1d0f\u1d0d\u1d07\u1d0f\u0274\u1d07 \u1d1b\u1d0f \u1d1b\u029c\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d18\u026a\u1d04\u1d0b \u1d21\u029c\u1d0f")).hideTooltip().tag(this.plugin.keyButton(), "party-invite").build());
        this.inventory.setItem(Math.min(n2, guiConfigManager.slot(MENU, "settings", 15, n)), guiConfigManager.face(MENU, "settings", hashMap, GuiConfigManager.Face.of(Material.GRINDSTONE, "{value}\ua731\u1d07\u1d1b\u1d1b\u026a\u0274\u0262\ua731", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d0f\u1d18\u1d07\u0274")).hideTooltip().tag(this.plugin.keyButton(), "party-settings-open").build());
        hashMap.put("why", this.startHint(party));
        this.inventory.setItem(Math.min(n2, guiConfigManager.slot(MENU, "start", 29, n)), guiConfigManager.toggle(MENU, "start", !party.isFighting() && party.size() >= 2, hashMap, GuiConfigManager.Face.of(Material.LIME_DYE, "<gradient:{go-from}:{go-to}>{tick}\ua731\u1d1b\u1d00\u0280\u1d1b \u1d0d\u1d00\u1d1b\u1d04\u029c</gradient>", "", "{why}").glow(), GuiConfigManager.Face.of(Material.GRAY_DYE, "{muted}{cross}\ua731\u1d1b\u1d00\u0280\u1d1b \u1d0d\u1d00\u1d1b\u1d04\u029c", "", "{why}")).hideTooltip().tag(this.plugin.keyButton(), "party-start").build());
    }

    private void addMembers(Party party) {
        int n = Math.min(7, party.size());
        int n2 = 19 + (7 - n) / 2;
        int n3 = 0;
        for (UUID uUID : party.getMembers()) {
            if (n3 == n) break;
            if (n3 == 6 && party.size() > 7) {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("count", String.valueOf(party.size() - 7 + 1));
                this.inventory.setItem(n2, this.plugin.gui().face(MENU, "overflow", hashMap, GuiConfigManager.Face.of(Material.PAPER, "{value}+{count}", "", "{label}\u1d0d\u1d0f\u0280\u1d07 \u1d0d\u1d07\u1d0d\u0299\u1d07\u0280\ua731 \u026a\u0274 \u1d1b\u029c\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f")).hideTooltip().build());
                break;
            }
            this.inventory.setItem(n2++, this.head(party, uUID));
            ++n3;
        }
    }

    private ItemStack head(Party party, UUID uUID) {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("player", this.nameOf(uUID));
        hashMap.put("state", this.memberState(party, uUID));
        return guiConfigManager.toggle(MENU, "member", party.isLeader(uUID), hashMap, GuiConfigManager.Face.of(Material.PLAYER_HEAD, "<#FFD65C>\u2605 {value}{player}", "", "<#FFD65C>\u029f\u1d07\u1d00\u1d05\u1d07\u0280", "{state}").glow(), GuiConfigManager.Face.of(Material.PLAYER_HEAD, "<#C79BFF>\u25c6 {value}{player}", "", "{label}\u1d0d\u1d07\u1d0d\u0299\u1d07\u0280", "{state}")).skull(Bukkit.getOfflinePlayer((UUID)uUID)).hideTooltip().build();
    }

    private String memberState(Party party, UUID uUID) {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        if (party.isFighting()) {
            return party.getWatching().contains(uUID) ? guiConfigManager.line(MENU, "member", "state-out", "<dark_gray>\u2620 {muted}\u1d07\u029f\u026a\u1d0d\u026a\u0274\u1d00\u1d1b\u1d07\u1d05", null) : guiConfigManager.line(MENU, "member", "state-alive", "{good}\u26a1 \u1d00\u029f\u026a\u1d20\u1d07", null);
        }
        if (Bukkit.getPlayer((UUID)uUID) == null) {
            return guiConfigManager.line(MENU, "member", "state-offline", "{muted}\u1d0f\ua730\ua730\u029f\u026a\u0274\u1d07", null);
        }
        return guiConfigManager.line(MENU, "member", "state-idle", "", null);
    }

    private Kit kitOf(Party party) {
        return party.getKit() == null ? null : this.plugin.getKitManager().get(party.getKit());
    }

    private String kitLabel(Party party) {
        Kit kit = this.kitOf(party);
        if (kit == null) {
            return "<#6B7079>\u0274\u1d0f\u1d1b \u1d04\u029c\u1d0f\ua731\u1d07\u0274";
        }
        return kit.withIcon(kit.getDisplayName() == null || kit.getDisplayName().isEmpty() ? VALUE + kit.getName() : kit.getDisplayName());
    }

    private String startHint(Party party) {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        if (party.isFighting()) {
            return guiConfigManager.line(MENU, "start", "why-fighting", "{muted}\u1d00\u029f\u0280\u1d07\u1d00\u1d05\u028f \ua730\u026a\u0262\u029c\u1d1b\u026a\u0274\u0262", null);
        }
        if (party.size() < 2) {
            return guiConfigManager.line(MENU, "start", "why-too-few", "<#FF8A93>\u0274\u1d07\u1d07\u1d05\ua731 \u1d00\u1d1b \u029f\u1d07\u1d00\ua731\u1d1b 2 \u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731", null);
        }
        return guiConfigManager.line(MENU, "start", "why-ready", "{label}\ua730\ua730\u1d00, \ua731\u1d18\u029f\u026a\u1d1b \u1d0f\u0280 \u1d05\u1d1c\u1d07\u029f\ua731", null);
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
        if (party == null) {
            player.closeInventory();
            return;
        }
        if (!"party-leave-open".equals(string) && !party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-do", new String[0]));
            player.closeInventory();
            return;
        }
        if ("party-kit".equals(string)) {
            new PartyModeMenu(this.plugin).open(player);
        } else if ("party-invite".equals(string)) {
            new PartyInviteMenu(this.plugin).open(player);
        } else if ("party-settings-open".equals(string)) {
            new PartySettingsMenu(this.plugin).open(player);
        } else if ("party-start".equals(string)) {
            new PartyModeMenu(this.plugin).open(player);
        } else if ("party-leave-open".equals(string)) {
            new PartyConfirmMenu(this.plugin, player).open(player);
        }
    }
}

