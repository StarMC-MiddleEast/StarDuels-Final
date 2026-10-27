package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.KitPickMenu;
import com.meowduels.gui.MapSelectMenu;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Arena;
import com.meowduels.model.Kit;
import com.meowduels.util.Items;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class DuelConfirmMenu
extends Menu {
    private static final int[] ROUND_OPTIONS = new int[]{1, 2, 3, 5};
    private static final String MENU = "duelconfirm";
    private static final int ROWS = 3;
    private static final int SLOT_INFO = 10;
    private static final int SLOT_ROUNDS = 11;
    private static final int SLOT_MAP = 12;
    private static final int SLOT_SEND = 14;
    private static final int SLOT_BACK = 22;
    private final MeowDuels plugin;
    private final Player sender;
    private final Player target;
    private String selectedKit;
    private String selectedArena;
    private int roundsIndex = 0;

    public DuelConfirmMenu(MeowDuels meowDuels, Player player, Player player2) {
        this.plugin = meowDuels;
        this.sender = player;
        this.target = player2;
    }

    public Player getTarget() {
        return this.target;
    }

    public Player getSender() {
        return this.sender;
    }

    public String getSelectedKit() {
        return this.selectedKit;
    }

    public void setSelectedKit(String string) {
        Arena arena;
        this.selectedKit = string;
        if (!(this.selectedArena == null || (arena = this.plugin.getArenaManager().get(this.selectedArena)) != null && arena.supportsKit(string))) {
            this.selectedArena = null;
        }
    }

    public String getSelectedArena() {
        return this.selectedArena;
    }

    public void setSelectedArena(String string) {
        this.selectedArena = string;
    }

    public void reopen(Player player) {
        this.build();
        player.openInventory(this.inventory);
    }

    private int currentRounds() {
        return ROUND_OPTIONS[this.roundsIndex];
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = guiConfigManager.rows(MENU, 3);
        Object object = guiConfigManager.title(MENU, "<bold><gradient:#FF465E:#FFAA55>\u1d05\u1d1c\u1d07\u029f \u1d04\u1d0f\u0274\ua730\u026a\u0280\u1d0d</gradient></bold> <dark_gray>\u203a <white>" + this.target.getName());
        if ("Round Selection".equals(object)) {
            object = "<bold><gradient:#FF465E:#FFAA55>\u1d05\u1d1c\u1d07\u029f \u1d04\u1d0f\u0274\ua730\u026a\u0280\u1d0d</gradient></bold> <dark_gray>\u203a <white>" + this.target.getName();
        }
        this.createRaw(n, (String)object);
        guiConfigManager.frame(MENU, this.inventory, n);
        HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
        if (this.plugin.getGuiLayoutManager().has(MENU)) {
            for (Map.Entry<Integer, ItemStack> entry : this.plugin.getGuiLayoutManager().get(MENU).entrySet()) {
                if (entry.getKey() >= n * 9) continue;
                String string = Items.readTag(entry.getValue(), this.plugin.keyButton());
                if (string != null) {
                    hashMap.put(string, entry.getKey());
                    continue;
                }
                this.inventory.setItem(entry.getKey().intValue(), entry.getValue());
            }
        }
        this.place(hashMap, "info", guiConfigManager.slot(MENU, "info", 10, n), n);
        this.place(hashMap, "clock", guiConfigManager.slot(MENU, "clock", 11, n), n);
        this.place(hashMap, "map", guiConfigManager.slot(MENU, "map", 12, n), n);
        this.place(hashMap, "confirm", guiConfigManager.slot(MENU, "confirm", 14, n), n);
        this.place(hashMap, "cancel", guiConfigManager.slot(MENU, "cancel", 22, n), n);
    }

    private void place(Map<String, Integer> map, String string, int n, int n2) {
        int n3 = map.getOrDefault(string, n);
        if (n3 < 0 || n3 >= n2 * 9) {
            n3 = Math.max(0, Math.min(n2 * 9 - 1, n3));
        }
        this.inventory.setItem(n3, this.renderButton(string));
    }

    private String gamemodeLabel() {
        Kit kit;
        Kit kit2 = kit = this.selectedKit == null ? null : this.plugin.getKitManager().get(this.selectedKit);
        if (kit == null) {
            return "<yellow>None";
        }
        String string = kit.getDisplayName();
        return kit.withIcon(string == null || string.isEmpty() ? "<yellow>" + kit.getName() : string);
    }

    private Map<String, String> tokens() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("rounds", String.valueOf(this.currentRounds()));
        hashMap.put("gamemode", this.gamemodeLabel());
        hashMap.put("opponent", this.target.getName());
        hashMap.put("sender", this.sender.getName());
        return hashMap;
    }

    private ItemStack renderButton(String string) {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        Map<String, String> map = this.tokens();
        switch (string) {
            case "clock": {
                return guiConfigManager.item(MENU, "clock", map, Material.CLOCK, "<bold><gradient:#FFE55B:#FFAA00>\u2726 \u0280\u1d0f\u1d1c\u0274\u1d05\ua731 \u2726</gradient></bold>", "", "<yellow>Current <white>{rounds} Rounds", "", "<yellow>LMB <white>Increase Rounds", "<yellow>RMB <white>Decrease Rounds").hideTooltip().tag(this.plugin.keyButton(), "clock").build();
            }
            case "map": {
                return guiConfigManager.item(MENU, "map", map, Material.PAPER, "<bold><gradient:#69F0FF:#248BFF>\u21c4 \u1d0d\u1d00\u1d18 \ua731\u1d07\u029f\u1d07\u1d04\u1d1b\u026a\u1d0f\u0274 \u21c4</gradient></bold>", "<white>Click to Open Menu!").hideTooltip().tag(this.plugin.keyButton(), "map").build();
            }
            case "confirm": {
                return guiConfigManager.item(MENU, "confirm", map, Material.LIME_DYE, "<bold><gradient:#7CFF6B:#1FA32F>\u2714 \ua731\u1d07\u0274\u1d05 \u1d05\u1d1c\u1d07\u029f \u2714</gradient></bold>", "<white>Click to Send Duel!").hideTooltip().tag(this.plugin.keyButton(), "confirm").build();
            }
            case "cancel": {
                return guiConfigManager.item(MENU, "cancel", map, Material.BARRIER, "<bold><gradient:#FF7777:#D52345>\u2716 \u0262\u1d0f \u0299\u1d00\u1d04\u1d0b \u2716</gradient></bold>", "<white>Go Back").hideTooltip().tag(this.plugin.keyButton(), "cancel").build();
            }
        }
        return guiConfigManager.item(MENU, "info", map, Material.ITEM_FRAME, "<bold><gradient:#69F0FF:#248BFF>\u25c6 \u026a\u0274\ua730\u1d0f\u0280\u1d0d\u1d00\u1d1b\u026a\u1d0f\u0274 \u25c6</gradient></bold>", "", "<white>Gamemode: {gamemode}", "<white>Rounds: <yellow>{rounds}", "<white>Opponent: <yellow>{opponent}", "<white>Sender: <yellow>{sender}").hideTooltip().tag(this.plugin.keyButton(), "info").build();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        switch (string) {
            case "info": {
                break;
            }
            case "map": {
                new MapSelectMenu(this.plugin, this).open(player);
                break;
            }
            case "clock": {
                int n = this.roundsIndex + (inventoryClickEvent.isRightClick() ? -1 : 1);
                this.roundsIndex = Math.max(0, Math.min(ROUND_OPTIONS.length - 1, n));
                this.reopen(player);
                break;
            }
            case "cancel": {
                new KitPickMenu(this.plugin, this).open(player);
                break;
            }
            case "confirm": {
                if (this.selectedKit == null) {
                    player.sendMessage(this.plugin.messages().get("menu.select-kit-first", new String[0]));
                    return;
                }
                Player player2 = this.plugin.getServer().getPlayer(this.target.getUniqueId());
                if (player2 == null) {
                    player.sendMessage(this.plugin.messages().get("menu.target-offline", "target", this.target.getName()));
                    player.closeInventory();
                    return;
                }
                if (!this.plugin.getDuelManager().hasUsableArena(this.selectedKit)) {
                    player.sendMessage(this.plugin.messages().get("duel.no-arenas", new String[0]));
                    player.closeInventory();
                    return;
                }
                this.plugin.getDuelManager().sendRequest(player, player2, this.selectedKit, this.currentRounds(), this.selectedArena);
                player.closeInventory();
                break;
            }
        }
    }
}

