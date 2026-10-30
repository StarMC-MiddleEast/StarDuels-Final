package com.meowduels.dialog;

import com.meowduels.MeowDuels;
import com.meowduels.gui.Menu;
import java.util.Set;
import org.bukkit.entity.Player;

/** Central policy for which existing menus use the native Buttons GUI. */
public final class MenuRouter {
    private static final Set<String> PLAYER_MENUS = Set.of(
        "QueuePickMenu", "QueueCategoriesMenu", "KitPickMenu", "MapSelectMenu", "DuelConfirmMenu",
        "PartyConfirmMenu", "PartyDuelsConfirmMenu", "PartyInviteMenu", "PartyKitMenu", "PartyMenu", "PartyModeMenu",
        "PartyOpponentMenu", "PartySettingsMenu", "PartyStartConfirmMenu", "PartyTeamMenu", "TrimKitMenu", "TrimMenu",
        "EventMapMenu", "SettingsMenu", "KitEditorMenu"
    );

    private MenuRouter() {}

    public static boolean usesDialog(MeowDuels plugin, Player player, Menu menu) {
        String name = menu.getClass().getSimpleName();
        if (!PLAYER_MENUS.contains(name)) return false;
        // Native dialogs require Paper 1.21.6+ and a client protocol that can
        // display them. Older server/client versions keep using the chest GUI.
        if (!DialogSupport.isAvailable(plugin, player)) return false;
        // Keep the switch reachable even when the player chooses chest menus.
        if ("SettingsMenu".equals(name)) return true;
        return plugin.getPlayerSettings().isButtonsGui(player.getUniqueId());
    }
}
