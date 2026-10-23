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
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PartyTeamMenu
extends Menu {
    private static final String MENU = "partyteam";
    private static final int ROWS = 6;
    private static final int SIZE = 54;
    private static final int DIVIDER_COL = 4;
    private static final int[] AQUA_COLS = new int[]{0, 1, 2, 3};
    private static final int[] RED_COLS = new int[]{5, 6, 7, 8};
    private static final int HEAD_ROWS = 4;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_SHUFFLE = 47;
    private static final int SLOT_INFO = 49;
    private static final int SLOT_START = 51;
    private static final String VALUE = "<#E6E8EB>";
    private static final String LABEL = "<#8E959D>";
    private static final String MUTED = "<#6B7079>";
    private static final String SEP = "<dark_gray>\u203a ";
    private static final String HINT = "<dark_gray>\u25b8 <#8E959D>";
    private static final String AQUA = "<gradient:#7DE2FF:#2BB9E8>";
    private static final String RED = "<gradient:#FF8A8A:#D63232>";
    private final MeowDuels plugin;

    public PartyTeamMenu(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    @Override
    public void build() {
    }

    @Override
    public void open(Player player) {
        Party party = this.plugin.getPartyManager().partyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.sendMessage(this.plugin.messages().get("notice.party.only-party-leader-set", new String[0]));
            return;
        }
        if (party.getTeams().isEmpty()) {
            party.shuffleTeams();
        }
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 6);
        int n2 = n * 9 - 1;
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("kit", this.kitLabel(party));
        hashMap.put("size", String.valueOf(party.size()));
        hashMap.put("mode", PartyMode.SPLIT.getLabel());
        this.createRaw(n, guiConfigManager.title(MENU, "<dark_gray>\u258f <gradient:#7DE2FF:#4B7BFF>\u1d18\u1d00\u0280\u1d1b\u028f \ua731\u1d18\u029f\u026a\u1d1b - \u1d1b\u1d07\u1d00\u1d0d\ua731</gradient>"));
        guiConfigManager.frame(MENU, this.inventory, n);
        ItemStack itemStack = guiConfigManager.face(MENU, "divider", null, GuiConfigManager.Face.of(Material.IRON_BARS, " ", new String[0])).build();
        for (int i = 0; i < n - 1; ++i) {
            this.inventory.setItem(i * 9 + 4, itemStack);
        }
        HashMap<String, String> hashMap2 = new HashMap<String, String>();
        hashMap2.put("size", String.valueOf(party.teamMembers(Party.Team.AQUA).size()));
        HashMap<String, String> hashMap3 = new HashMap<String, String>();
        hashMap3.put("size", String.valueOf(party.teamMembers(Party.Team.RED).size()));
        this.inventory.setItem(2, guiConfigManager.face(MENU, "banner-aqua", hashMap2, GuiConfigManager.Face.of(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "<gradient:#7DE2FF:#2BB9E8>\u1d1b\u1d07\u1d00\u1d0d \u1d00\ua7af\u1d1c\u1d00</gradient>", "", "{label}\u1d0f\u0274 \u1d1b\u029c\u026a\ua731 \ua731\u026a\u1d05\u1d07 {sep}{value}{size}")).hideTooltip().build());
        this.inventory.setItem(6, guiConfigManager.face(MENU, "banner-red", hashMap3, GuiConfigManager.Face.of(Material.RED_STAINED_GLASS_PANE, "<gradient:#FF8A8A:#D63232>\u1d1b\u1d07\u1d00\u1d0d \u0280\u1d07\u1d05</gradient>", "", "{label}\u1d0f\u0274 \u1d1b\u029c\u026a\ua731 \ua731\u026a\u1d05\u1d07 {sep}{value}{size}")).hideTooltip().build());
        this.fill(party, Party.Team.AQUA, AQUA_COLS);
        this.fill(party, Party.Team.RED, RED_COLS);
        boolean bl = party.aliveOrMembers(Party.Team.AQUA) > 0 && party.aliveOrMembers(Party.Team.RED) > 0;
        this.inventory.setItem(Math.min(n2, guiConfigManager.slot(MENU, "back", 45, n)), guiConfigManager.back(MENU, "back").tag(this.plugin.keyButton(), "team-back").build());
        this.inventory.setItem(Math.min(n2, guiConfigManager.slot(MENU, "shuffle", 47, n)), guiConfigManager.face(MENU, "shuffle", hashMap, GuiConfigManager.Face.of(Material.ENDER_PEARL, "<gradient:#7DE2FF:#4B7BFF>\ua731\u029c\u1d1c\ua730\ua730\u029f\u1d07</gradient>", "", "{label}\ua731\u1d18\u029f\u026a\u1d1b \u1d1b\u029c\u1d07 \u1d18\u1d00\u0280\u1d1b\u028f \u1d00\u1d1b \u0280\u1d00\u0274\u1d05\u1d0f\u1d0d \u1d00\u0262\u1d00\u026a\u0274")).hideTooltip().tag(this.plugin.keyButton(), "team-shuffle").build());
        this.inventory.setItem(Math.min(n2, guiConfigManager.slot(MENU, "info", 49, n)), guiConfigManager.face(MENU, "info", hashMap, GuiConfigManager.Face.of(Material.PAPER, "<gradient:#7DE2FF:#4B7BFF>{mode}</gradient>", "", "{label}\u1d0b\u026a\u1d1b {sep}{kit}", "{label}\u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731 {sep}{value}{size}", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d00 \u029c\u1d07\u1d00\u1d05 \u1d1b\u1d0f \u1d0d\u1d0f\u1d20\u1d07 \u026a\u1d1b \u1d00\u1d04\u0280\u1d0f\ua731\ua731")).hideTooltip().build());
        this.inventory.setItem(Math.min(n2, guiConfigManager.slot(MENU, "start", 51, n)), guiConfigManager.toggle(MENU, "start", bl, hashMap, GuiConfigManager.Face.of(Material.LIME_DYE, "<gradient:{go-from}:{go-to}>\ua731\u1d1b\u1d00\u0280\u1d1b \u1d0d\u1d00\u1d1b\u1d04\u029c</gradient>", "", "{label}\ua731\u1d07\u0274\u1d05 \u0299\u1d0f\u1d1b\u029c \u1d1b\u1d07\u1d00\u1d0d\ua731 \u026a\u0274").glow(), GuiConfigManager.Face.of(Material.GRAY_DYE, "{muted}\ua731\u1d1b\u1d00\u0280\u1d1b \u1d0d\u1d00\u1d1b\u1d04\u029c", "", "<#FF8A93>\u0299\u1d0f\u1d1b\u029c \u1d1b\u1d07\u1d00\u1d0d\ua731 \u0274\u1d07\u1d07\u1d05 \u1d00 \u1d18\u029f\u1d00\u028f\u1d07\u0280")).hideTooltip().tag(this.plugin.keyButton(), "team-start").build());
        player.openInventory(this.inventory);
    }

    private void fill(Party party, Party.Team team, int[] nArray) {
        List<UUID> list = party.teamMembers(team);
        for (int i = 0; i < list.size() && i < 4 * nArray.length; ++i) {
            int n = (1 + i / nArray.length) * 9 + nArray[i % nArray.length];
            this.inventory.setItem(n, this.head(party, list.get(i), team));
        }
    }

    private ItemStack head(Party party, UUID uUID, Party.Team team) {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("player", this.nameOf(uUID));
        hashMap.put("state", Bukkit.getPlayer((UUID)uUID) != null ? "" : guiConfigManager.line(MENU, "head-aqua", "state-offline", "{muted}\u1d0f\ua730\ua730\u029f\u026a\u0274\u1d07", null));
        Items items = team == Party.Team.AQUA ? guiConfigManager.face(MENU, "head-aqua", hashMap, GuiConfigManager.Face.of(Material.PLAYER_HEAD, "<gradient:#7DE2FF:#2BB9E8>{player}</gradient>", "", "{label}\u1d1b\u1d07\u1d00\u1d0d {sep}<gradient:#7DE2FF:#2BB9E8>\u1d1b\u1d07\u1d00\u1d0d \u1d00\ua7af\u1d1c\u1d00</gradient>", "{state}", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d0d\u1d0f\u1d20\u1d07 \u1d1b\u1d0f \u1d1b\u029c\u1d07 \u1d0f\u1d1b\u029c\u1d07\u0280 \u1d1b\u1d07\u1d00\u1d0d")) : guiConfigManager.face(MENU, "head-red", hashMap, GuiConfigManager.Face.of(Material.PLAYER_HEAD, "<gradient:#FF8A8A:#D63232>{player}</gradient>", "", "{label}\u1d1b\u1d07\u1d00\u1d0d {sep}<gradient:#FF8A8A:#D63232>\u1d1b\u1d07\u1d00\u1d0d \u0280\u1d07\u1d05</gradient>", "{state}", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d0d\u1d0f\u1d20\u1d07 \u1d1b\u1d0f \u1d1b\u029c\u1d07 \u1d0f\u1d1b\u029c\u1d07\u0280 \u1d1b\u1d07\u1d00\u1d0d"));
        return items.skull(Bukkit.getOfflinePlayer((UUID)uUID)).hideTooltip().tag(this.plugin.keyButton(), "team-move:" + uUID.toString()).build();
    }

    private String kitLabel(Party party) {
        Kit kit;
        Kit kit2 = kit = party.getKit() == null ? null : this.plugin.getKitManager().get(party.getKit());
        if (kit == null) {
            return "<#6B7079>\u0274\u1d0f\u1d1b \u1d04\u029c\u1d0f\ua731\u1d07\u0274";
        }
        return kit.getDisplayName() == null || kit.getDisplayName().isEmpty() ? VALUE + kit.getName() : kit.getDisplayName();
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
        if ("team-back".equals(string)) {
            new PartyKitMenu(this.plugin, PartyMode.SPLIT).open(player);
            return;
        }
        if ("team-shuffle".equals(string)) {
            party.shuffleTeams();
            this.open(player);
            return;
        }
        if ("team-start".equals(string)) {
            player.closeInventory();
            this.plugin.getPartyManager().startMatch(player, PartyMode.SPLIT);
            return;
        }
        if (string.startsWith("team-move:")) {
            UUID uUID;
            try {
                uUID = UUID.fromString(string.substring(10));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                return;
            }
            Party.Team team = party.teamOf(uUID);
            if (team != null) {
                party.setTeam(uUID, team == Party.Team.AQUA ? Party.Team.RED : Party.Team.AQUA);
            }
            this.open(player);
        }
    }
}

