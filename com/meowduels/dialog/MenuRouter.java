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
        "EventMapMenu", "MatchSummaryMenu", "SettingsMenu", "KitEditorMenu"
    );

    private MenuRouter() {}

    public static boolean usesDialog(MeowDuels plugin, Player player, Menu menu) {
        String name = menu.getClass().getSimpleName();
        if (!PLAYER_MENUS.contains(name)) return false;
        if (name.equals("SettingsMenu") || name.equals("PartySettingsMenu")) return true;
        return plugin.getPlayerSettings().isButtonsGui(player.getUniqueId());
    }
}
