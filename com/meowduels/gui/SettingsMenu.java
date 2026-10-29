package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import com.meowduels.gui.TrimKitMenu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.util.Items;
import com.meowduels.util.Trims;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SettingsMenu
extends Menu {
    private static final int ROWS = 5;
    private static final String DUEL = "set-duelreq";
    private static final String BOARD = "set-scoreboard";
    private static final String INVITES = "set-invites";
    private static final String SPECTATORS = "set-spectators";
    private static final String SOUNDS = "set-sounds";
    private static final String CHAT = "set-chat";
    private static final String TRIMS = "set-trims";
    private static final String BUTTONS_GUI = "set-buttons-gui";
    private static final int SLOT_DUEL = 10;
    private static final int SLOT_BOARD = 14;
    private static final int SLOT_INVITES = 19;
    private static final int SLOT_SPECTATORS = 23;
    private static final int SLOT_CHAT = 28;
    private static final int SLOT_SOUNDS = 32;
    private static final int SLOT_TRIMS = 37;
    private static final int SLOT_BUTTONS_GUI = 41;
    private final MeowDuels plugin;
    private final UUID owner;
    private static final String MENU = "settings";
    private static final String OFF_HINT = "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d1b\u1d1c\u0280\u0274 \u1d0f\u0274";
    private static final String ON_HINT = "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d1b\u1d1c\u0280\u0274 \u1d0f\ua730\ua730";

    public SettingsMenu(MeowDuels meowDuels, Player player) {
        this.plugin = meowDuels;
        this.owner = player.getUniqueId();
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        int n = 5;
        int n2 = n * 9;
        String string = guiConfigManager.title(MENU, "<dark_gray>\u258f <gradient:#FFF1B3:#E6A700>\ua731\u1d07\u1d1b\u1d1b\u026a\u0274\u0262\ua731</gradient>").replace("#A0E9FF", "#FFF1B3").replace("#4C7DF0", "#E6A700");
        this.createRaw(n, string);
        guiConfigManager.frame(MENU, this.inventory, n);
        this.put(n, 10, DUEL, guiConfigManager.toggle(MENU, "duel-requests", this.plugin.getPlayerSettings().isDuelRequests(this.owner), null, GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{good}{tick} \u2694 \u1d05\u1d1c\u1d07\u029f \u0280\u1d07\u01eb\u1d1c\u1d07\ua731\u1d1b\ua731", "", "{good}{tick}\u1d00\u0274\u028f\u1d0f\u0274\u1d07 \u1d04\u1d00\u0274 \u1d04\u029c\u1d00\u029f\u029f\u1d07\u0274\u0262\u1d07 \u028f\u1d0f\u1d1c", "", ON_HINT).glow(), GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{bad}{cross} \u2694 \u1d05\u1d1c\u1d07\u029f \u0280\u1d07\u01eb\u1d1c\u1d07\ua731\u1d1b\ua731", "", "{bad}{cross}\u1d04\u029c\u1d00\u029f\u029f\u1d07\u0274\u0262\u1d07\ua731 \u1d00\u0280\u1d07 \u0299\u029f\u1d0f\u1d04\u1d0b\u1d07\u1d05", "", OFF_HINT)));
        this.put(n, 19, INVITES, guiConfigManager.toggle(MENU, "party-invites", this.plugin.getPlayerSettings().isPartyInvites(this.owner), null, GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{good}{tick} \u265f \u1d18\u1d00\u0280\u1d1b\u028f \u026a\u0274\u1d20\u026a\u1d1b\u1d07\ua731", "", "{good}{tick}\u1d00\u0274\u028f\u1d0f\u0274\u1d07 \u1d04\u1d00\u0274 \u026a\u0274\u1d20\u026a\u1d1b\u1d07 \u028f\u1d0f\u1d1c", "", ON_HINT).glow(), GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{bad}{cross} \u265f \u1d18\u1d00\u0280\u1d1b\u028f \u026a\u0274\u1d20\u026a\u1d1b\u1d07\ua731", "", "{bad}{cross}\u026a\u0274\u1d20\u026a\u1d1b\u1d07\ua731 \u1d00\u0280\u1d07 \u0299\u029f\u1d0f\u1d04\u1d0b\u1d07\u1d05", "", OFF_HINT)));
        this.put(n, 23, SPECTATORS, guiConfigManager.toggle(MENU, "spectators", this.plugin.getPlayerSettings().isSpectators(this.owner), null, GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{good}{tick} \u25c9 \ua731\u1d18\u1d07\u1d04\u1d1b\u1d00\u1d1b\u1d0f\u0280\ua731", "", "{good}{tick}\u1d0f\u1d1b\u029c\u1d07\u0280\ua731 \u1d04\u1d00\u0274 \u1d21\u1d00\u1d1b\u1d04\u029c \u028f\u1d0f\u1d1c\u0280 \u1d05\u1d1c\u1d07\u029f\ua731", "", ON_HINT).glow(), GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{bad}{cross} \u25c9 \ua731\u1d18\u1d07\u1d04\u1d1b\u1d00\u1d1b\u1d0f\u0280\ua731", "", "{bad}{cross}\u028f\u1d0f\u1d1c\u0280 \u1d05\u1d1c\u1d07\u029f\ua731 \u1d00\u0280\u1d07 \u1d18\u0280\u026a\u1d20\u1d00\u1d1b\u1d07", "", OFF_HINT)));
        this.put(n, 28, CHAT, guiConfigManager.toggle(MENU, "isolated-chat", this.plugin.getPlayerSettings().isIsolatedChat(this.owner), null, GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{good}{tick} \u2709 \u026a\ua731\u1d0f\u029f\u1d00\u1d1b\u1d07\u1d05 \u1d04\u029c\u1d00\u1d1b", "", "{good}{tick}\u026a\u0274 \u1d00 \u1d0d\u1d00\u1d1b\u1d04\u029c, \u1d0f\u0274\u029f\u028f \u1d1b\u029c\u1d07 \u1d0d\u1d00\u1d1b\u1d04\u029c \u1d1b\u1d00\u029f\u1d0b\ua731", "", ON_HINT).glow(), GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{bad}{cross} \u2709 \u026a\ua731\u1d0f\u029f\u1d00\u1d1b\u1d07\u1d05 \u1d04\u029c\u1d00\u1d1b", "", "{bad}{cross}\u028f\u1d0f\u1d1c \ua731\u1d07\u1d07 \u1d00\u0274\u1d05 \u1d0a\u1d0f\u026a\u0274 \u1d1b\u029c\u1d07 \u1d21\u029c\u1d0f\u029f\u1d07 \ua731\u1d07\u0280\u1d20\u1d07\u0280", "", OFF_HINT)));
        this.put(n, 14, BOARD, guiConfigManager.toggle(MENU, "scoreboard", this.plugin.getPlayerSettings().isScoreboard(this.owner), null, GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{good}{tick} \u25a4 \ua731\u1d04\u1d0f\u0280\u1d07\u0299\u1d0f\u1d00\u0280\u1d05", "", "{good}{tick}\u1d1b\u029c\u1d07 \ua731\u026a\u1d05\u1d07\u0299\u1d00\u0280 \u026a\ua731 \u1d0f\u0274", "", ON_HINT).glow(), GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{bad}{cross} \u25a4 \ua731\u1d04\u1d0f\u0280\u1d07\u0299\u1d0f\u1d00\u0280\u1d05", "", "{bad}{cross}\u1d1b\u029c\u1d07 \ua731\u026a\u1d05\u1d07\u0299\u1d00\u0280 \u026a\ua731 \u029c\u026a\u1d05\u1d05\u1d07\u0274", "", OFF_HINT)));
        this.put(n, 32, SOUNDS, guiConfigManager.toggle(MENU, "sounds", this.plugin.getPlayerSettings().isSounds(this.owner), null, GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{good}{tick} \u266a \u1d0d\u1d07\u0274\u1d1c \ua731\u1d0f\u1d1c\u0274\u1d05\ua731", "", "{good}{tick}\u1d04\u029f\u026a\u1d04\u1d0b\ua731, \u1d04\u1d0f\u1d1c\u0274\u1d1b\u1d05\u1d0f\u1d21\u0274\ua731 \u1d00\u0274\u1d05 \u1d04\u1d1c\u1d07\ua731", "", ON_HINT).glow(), GuiConfigManager.Face.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "{bad}{cross} \u266a \u1d0d\u1d07\u0274\u1d1c \ua731\u1d0f\u1d1c\u0274\u1d05\ua731", "", "{bad}{cross}\u1d0d\u1d07\u1d0f\u1d21\u1d05\u1d1c\u1d07\u029f\ua731 \u1d0d\u1d00\u1d0b\u1d07\ua731 \u0274\u1d0f \ua731\u1d0f\u1d1c\u0274\u1d05", "", OFF_HINT)));
        Player player = Bukkit.getPlayer((UUID)this.owner);
        if (Trims.canEdit(player)) {
            this.put(n, 37, TRIMS, guiConfigManager.item(MENU, "trims", null, Material.LEATHER_CHESTPLATE, "{value}\u1d0b\u026a\u1d1b \u1d1b\u0280\u026a\u1d0d\ua731", "", "{label}\ua731\u1d07\u1d1b \u028f\u1d0f\u1d1c\u0280 \u1d00\u0280\u1d0d\u1d0f\u1d1c\u0280 \u1d1b\u0280\u026a\u1d0d \u1d18\u1d07\u0280 \u1d0b\u026a\u1d1b", "", "{hint}\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d0f\u1d18\u1d07\u0274"));
        }
        boolean buttonsGui = this.plugin.getPlayerSettings().isButtonsGui(this.owner);
        GuiConfigManager.Face buttonsOn = GuiConfigManager.Face.of(Material.COMPARATOR, "<green>✓ Buttons GUI", "", "<gray>Other menus use Paper Dialogs", "", "<dark_gray>Settings always stays a dialog", "<yellow>Click to switch to chest menus");
        GuiConfigManager.Face buttonsOff = GuiConfigManager.Face.of(Material.COMPARATOR, "<red>✗ Buttons GUI", "", "<gray>Other menus use chest menus", "", "<dark_gray>Settings always stays a dialog", "<yellow>Click to switch to Paper Dialogs");
        this.put(n, SLOT_BUTTONS_GUI, BUTTONS_GUI, guiConfigManager.toggle(MENU, "buttons-gui", buttonsGui, null, buttonsOn, buttonsOff));
    }

    private void put(int n, int n2, String string, Items items) {
        int n3 = Math.max(0, Math.min(n * 9 - 1, n2));
        ItemStack itemStack = items.tag(this.plugin.keyButton(), string).build();
        itemStack.setType(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        this.inventory.setItem(n3, itemStack);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        String string = Items.readTag(inventoryClickEvent.getCurrentItem(), this.plugin.keyButton());
        if (string == null) {
            return;
        }
        if (TRIMS.equals(string)) {
            new TrimKitMenu(this.plugin).open(player);
            return;
        }
        if (BUTTONS_GUI.equals(string)) {
            this.plugin.getPlayerSettings().toggle("buttons-gui", this.owner);
            this.build();
            player.openInventory(this.inventory);
            return;
        }
        String string2 = SettingsMenu.keyFor(string);
        if (string2 == null) {
            return;
        }
        this.plugin.getPlayerSettings().toggle(string2, this.owner);
        if (BOARD.equals(string)) {
            this.plugin.getScoreboardService().handleJoin(player);
        }
        this.build();
        player.openInventory(this.inventory);
    }

    private static String keyFor(String string) {
        if (DUEL.equals(string)) {
            return "duel-requests";
        }
        if (BOARD.equals(string)) {
            return "scoreboard";
        }
        if (INVITES.equals(string)) {
            return "party-invites";
        }
        if (SPECTATORS.equals(string)) {
            return "spectators";
        }
        if (SOUNDS.equals(string)) {
            return "sounds";
        }
        if (CHAT.equals(string)) {
            return "isolated-chat";
        }
        return null;
    }
}

