package com.meowduels.commands;

import com.meowduels.MeowDuels;
import com.meowduels.gui.ArenaMenu;
import com.meowduels.gui.DuelConfirmMenu;
import com.meowduels.gui.EditKitEffectsMenu;
import com.meowduels.gui.EditKitListMenu;
import com.meowduels.gui.EventMapMenu;
import com.meowduels.gui.GuiEditorMenu;
import com.meowduels.gui.KitEditorMenu;
import com.meowduels.gui.KitItemsEditMenu;
import com.meowduels.gui.KitPickMenu;
import com.meowduels.gui.PartyMenu;
import com.meowduels.gui.QueuePickMenu;
import com.meowduels.gui.TrimKitMenu;
import com.meowduels.gui.TrimMenu;
import com.meowduels.managers.ArenaManager;
import com.meowduels.managers.CategoryManager;
import com.meowduels.managers.EventManager;
import com.meowduels.managers.GuiLayoutManager;
import com.meowduels.model.Arena;
import com.meowduels.model.DuelRequest;
import com.meowduels.model.Kit;
import com.meowduels.util.GoldenHead;
import com.meowduels.util.Sounds;
import com.meowduels.util.Text;
import com.meowduels.util.Trims;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class MeowDuelsCommand
implements CommandExecutor,
TabCompleter {
    private final MeowDuels plugin;

    public MeowDuelsCommand(MeowDuels meowDuels) {
        this.plugin = meowDuels;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        switch (command.getName().toLowerCase(Locale.ROOT)) {
            case "createarena": {
                this.createArena(commandSender, stringArray);
                break;
            }
            case "arena": {
                this.openArena(commandSender, stringArray);
                break;
            }
            case "deletearena": {
                this.deleteArena(commandSender, stringArray);
                break;
            }
            case "kitcreate": {
                this.createKit(commandSender, stringArray);
                break;
            }
            case "deletekit": {
                this.deleteKit(commandSender, stringArray);
                break;
            }
            case "kiticon": {
                this.kitIcon(commandSender, stringArray);
                break;
            }
            case "kitdisplayname": {
                this.kitDisplayName(commandSender, stringArray);
                break;
            }
            case "editgui": {
                this.editGui(commandSender, stringArray);
                break;
            }
            case "category": {
                this.categoryCommand(commandSender, stringArray, this.plugin.getCategoryManager(), "category");
                break;
            }
            case "categoryqueue": {
                this.categoryCommand(commandSender, stringArray, this.plugin.getQueueCategoryManager(), "categoryqueue");
                break;
            }
            case "queue": {
                this.queue(commandSender, stringArray);
                break;
            }
            case "editkit": {
                this.editKit(commandSender, stringArray);
                break;
            }
            case "spectate": {
                this.spectate(commandSender, stringArray);
                break;
            }
            case "duel": {
                this.duel(commandSender, stringArray);
                break;
            }
            case "leave": {
                this.leave(commandSender);
                break;
            }
            case "party": {
                this.party(commandSender, stringArray);
                break;
            }
            case "meowduelsspawnitems": {
                if (!this.requirePlayer(commandSender)) break;
                this.plugin.giveSpawnItems((Player)commandSender);
                break;
            }
            case "ff": {
                this.forfeit(commandSender);
                break;
            }
            case "event": {
                this.event(commandSender, stringArray);
                break;
            }
            case "eventspec": {
                this.eventSpectate(commandSender);
                break;
            }
            case "eventleave": {
                this.eventLeave(commandSender);
                break;
            }
            case "scoreboardip": {
                this.scoreboardIp(commandSender, stringArray);
                break;
            }
            case "starduels":
            case "sd":
            case "md":
            case "meowduels": {
                this.root(commandSender, stringArray);
                break;
            }
            case "meowduelssetspawn": {
                this.setDuelSpawn(commandSender);
                break;
            }
            case "kiteditor": {
                this.openKitEditor(commandSender);
                break;
            }
            case "givegoldenhead": {
                this.giveGoldenHead(commandSender, stringArray);
                break;
            }
            case "resetconfig": {
                this.resetConfig(commandSender);
                break;
            }
            case "meowduelsserver": {
                this.setServerName(commandSender, stringArray);
                break;
            }
            case "meowduelstrims": {
                this.openTrims(commandSender, stringArray);
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    private void createArena(CommandSender commandSender, String[] stringArray) {
        if (!this.requireAdmin(commandSender)) {
            return;
        }
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("hungergames")) {
            if (!this.requirePlayer(commandSender)) return;
            String arenaName = stringArray.length >= 2 ? stringArray[1] : "HungerGames";
            if (stringArray.length < 2 && this.plugin.getArenaManager().exists(arenaName)) {
                int suffix = 2;
                while (this.plugin.getArenaManager().exists(arenaName + suffix)) suffix++;
                arenaName += suffix;
            }
            this.plugin.getHungerGamesManager().beginSetup((Player)commandSender, arenaName);
            return;
        }
        if (stringArray.length < 1) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-createarena-name", new String[0]));
            return;
        }
        String string = stringArray[0];
        if (this.plugin.getArenaManager().exists(string)) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.arena-named-already-exists", "name", string));
            return;
        }
        this.plugin.getArenaManager().create(string);
        commandSender.sendMessage(this.plugin.messages().get("notice.admin.arena-created-open-arena", "name", string, "name2", string));
    }

    private void openArena(CommandSender commandSender, String[] stringArray) {
        Arena arena;
        if (!this.requireAdmin(commandSender) || !this.requirePlayer(commandSender)) {
            return;
        }
        if (stringArray.length < 1) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-arena-name-tp", new String[0]));
            return;
        }
        if (stringArray.length >= 2) {
            String string = stringArray[1].toLowerCase(Locale.ROOT);
            if (string.equals("copy") || string.equals("paste")) {
                this.arenaClone(commandSender, stringArray[0], string);
                return;
            }
            if (string.equals("regen")) {
                this.arenaRegen(commandSender, stringArray[0]);
                return;
            }
            if (string.equals("snapshot")) {
                this.arenaSnapshot(commandSender, stringArray[0]);
                return;
            }
            if (string.equals("tp") || string.equals("teleport")) {
                this.arenaTeleport(commandSender, stringArray[0], stringArray.length >= 3 ? stringArray[2] : "1");
                return;
            }
        }
        if ((arena = this.plugin.getArenaManager().get(stringArray[0])) == null) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-arena-named-2", "name", stringArray[0]));
            return;
        }
        new ArenaMenu(this.plugin, arena).open((Player)commandSender);
    }

    private void arenaClone(CommandSender commandSender, String string, String string2) {
        if (!this.requireAdmin(commandSender) || !this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        Arena arena = this.plugin.getArenaManager().get(string);
        if (arena == null || !arena.isConfigured()) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-configured-arena-named-4", "arenaname", string));
            return;
        }
        if (!arena.isCloningEnabled()) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.cloning-disabled-arena-enable", "arenaname", string));
            return;
        }
        if (string2.equalsIgnoreCase("copy")) {
            if (!this.plugin.getArenaManager().copyToClipboard(player.getUniqueId(), arena)) {
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-configured-arena-named-4", "arenaname", string));
                Sounds.deny(player);
                return;
            }
            Sounds.select(player);
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.copied-arena-clipboard-walk", "arena", arena.getName(), "arenaname", string));
        } else {
            if (!this.plugin.getArenaManager().hasClipboard(player.getUniqueId())) {
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.clipboard-empty-run-arena-2", "arenaname", string));
                return;
            }
            ArenaManager.PasteResult pasteResult = this.plugin.getArenaManager().pasteAsNewArena(player.getUniqueId(), player.getLocation());
            if (pasteResult == null) {
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.clipboard-empty-run-arena", "arenaname", string));
                Sounds.deny(player);
                return;
            }
            Sounds.accept(player);
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.pasted-block-s-registered", "value", String.valueOf(pasteResult.blocksWritten), "value2", String.valueOf(pasteResult.arenaName)));
        }
    }

    private void arenaRegen(CommandSender commandSender, String string) {
        if (!this.requireAdmin(commandSender)) {
            return;
        }
        Arena arena = this.plugin.getArenaManager().get(string);
        if (arena == null || !arena.isConfigured()) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-configured-arena-named-3", "arenaname", string));
            return;
        }
        int n = this.plugin.getArenaManager().regenArena(arena);
        if (n < 0) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-snapshot-saved-yet", "arenaname", string, "arenaname2", string));
        } else {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.regenerated-block-s", "written", String.valueOf(n), "arenaname", string));
        }
    }

    private void arenaSnapshot(CommandSender commandSender, String string) {
        if (!this.requireAdmin(commandSender)) {
            return;
        }
        Arena arena = this.plugin.getArenaManager().get(string);
        if (arena == null || !arena.isConfigured()) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-configured-arena-named-2", "arenaname", string));
            return;
        }
        this.plugin.getArenaManager().replaceSnapshotArena(arena);
        commandSender.sendMessage(this.plugin.messages().get("notice.admin.snapshot-saved-now-regen", "arenaname", string));
    }

    private void arenaTeleport(CommandSender commandSender, String string, String string2) {
        String string3;
        Location location;
        if (!this.requireAdmin(commandSender) || !this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        Arena arena = this.plugin.getArenaManager().get(string);
        if (arena == null || !arena.isConfigured()) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-configured-arena-named", "arenaname", string));
            return;
        }
        if (string2.equalsIgnoreCase("2")) {
            location = arena.getSpawn2();
            string3 = "Player 2 spawn";
        } else if (string2.equalsIgnoreCase("event")) {
            location = arena.getEventSpawn();
            string3 = "event spawn";
        } else {
            location = arena.getSpawn1();
            string3 = "Player 1 spawn";
        }
        if (location == null) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.isn-t-set", "label", string3, "arenaname", string));
            return;
        }
        player.teleport(location);
        commandSender.sendMessage(this.plugin.messages().get("notice.admin.teleported", "label", string3, "arenaname", string));
    }

    private void setDuelSpawn(CommandSender commandSender) {
        if (!this.requireAdmin(commandSender) || !this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        this.plugin.setDuelSpawn(player.getLocation());
        commandSender.sendMessage(this.plugin.messages().get("notice.admin.duel-spawn-set-current", new String[0]));
    }

    private void openKitEditor(CommandSender commandSender) {
        if (!this.requirePlayer(commandSender)) {
            return;
        }
        new KitEditorMenu(this.plugin, true).open((Player)commandSender);
    }

    private void deleteArena(CommandSender commandSender, String[] stringArray) {
        if (!this.requireAdmin(commandSender)) {
            return;
        }
        if (stringArray.length < 1) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-deletearena-name", new String[0]));
            return;
        }
        if (!this.plugin.getArenaManager().exists(stringArray[0])) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-arena-named", "name", stringArray[0]));
            return;
        }
        this.plugin.getArenaManager().delete(stringArray[0]);
        this.plugin.getHungerGamesManager().removeArena(stringArray[0]);
        commandSender.sendMessage(this.plugin.messages().get("notice.admin.arena-deleted", "name", stringArray[0]));
    }

    private void createKit(CommandSender commandSender, String[] stringArray) {
        if (!this.requireAdmin(commandSender) || !this.requirePlayer(commandSender)) {
            return;
        }
        if (stringArray.length < 1) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-kitcreate-name", new String[0]));
            return;
        }
        String string = stringArray[0];
        if (this.plugin.getKitManager().exists(string)) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.kit-named-already-exists", "name", string));
            return;
        }
        Kit kit = new Kit(string);
        kit.setGuiVisible(true);
        if (stringArray.length >= 2) {
            if (stringArray[1].equalsIgnoreCase("hidden") || stringArray[1].equalsIgnoreCase("false")) kit.setGuiVisible(false);
            else if (stringArray[1].equalsIgnoreCase("gui") || stringArray[1].equalsIgnoreCase("true")) kit.setGuiVisible(true);
        }
        if (stringArray.length >= 3 && stringArray[1].equalsIgnoreCase("prefix")) {
            kit.setPrefix(this.joinFrom(stringArray, 2));
        } else if (stringArray.length >= 4 && stringArray[2].equalsIgnoreCase("prefix")) {
            kit.setPrefix(this.joinFrom(stringArray, 3));
        }
        kit.captureFrom((Player)commandSender);
        this.plugin.getKitManager().put(kit);
        commandSender.sendMessage(this.plugin.messages().get("notice.admin.kit-saved-from-current", "name", string));
    }

    private void deleteKit(CommandSender commandSender, String[] stringArray) {
        if (!this.requireAdmin(commandSender)) {
            return;
        }
        if (stringArray.length < 1) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-deletekit-name", new String[0]));
            return;
        }
        if (!this.plugin.getKitManager().exists(stringArray[0])) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-kit-named-6", "name", stringArray[0]));
            return;
        }
        this.plugin.getKitManager().delete(stringArray[0]);
        commandSender.sendMessage(this.plugin.messages().get("notice.admin.kit-deleted", "name", stringArray[0]));
    }

    private void kitIcon(CommandSender commandSender, String[] stringArray) {
        if (!this.requireAdmin(commandSender) || !this.requirePlayer(commandSender)) {
            return;
        }
        if (stringArray.length < 1) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-kiticon-name", new String[0]));
            return;
        }
        Kit kit = this.plugin.getKitManager().get(stringArray[0]);
        if (kit == null) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-kit-named-5", "name", stringArray[0]));
            return;
        }
        ItemStack itemStack = ((Player)commandSender).getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.hold-item-want-use", new String[0]));
            return;
        }
        kit.setIcon(itemStack.getType());
        this.plugin.getKitManager().save();
        commandSender.sendMessage(this.plugin.messages().get("notice.admin.kit-icon-set", "kit", kit.getName(), "value", String.valueOf(itemStack.getType().name())));
    }

    private void kitDisplayName(CommandSender commandSender, String[] stringArray) {
        if (!this.requireAdmin(commandSender)) {
            return;
        }
        if (stringArray.length < 2) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-kitdisplayname-name-display", new String[0]));
            return;
        }
        Kit kit = this.plugin.getKitManager().get(stringArray[0]);
        if (kit == null) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-kit-named-4", "name", stringArray[0]));
            return;
        }
        String string = this.joinFrom(stringArray, 1);
        kit.setDisplayName(string);
        this.plugin.getKitManager().save();
        commandSender.sendMessage(this.plugin.messages().get("notice.admin.kit-display-name-set", "kit", kit.getName()));
    }

    private void editGui(CommandSender commandSender, String[] stringArray) {
        if (!this.requireAdmin(commandSender) || !this.requirePlayer(commandSender)) {
            return;
        }
        if (stringArray.length < 1 || !GuiLayoutManager.isValidMenu(stringArray[0].toLowerCase(Locale.ROOT))) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-editgui-duelconfirm-mapselect", new String[0]));
            return;
        }
        String string = stringArray[0].toLowerCase(Locale.ROOT);
        if (stringArray.length >= 2 && stringArray[1].equalsIgnoreCase("reset")) {
            if (!this.plugin.getGuiLayoutManager().has(string)) {
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-saved-layout-gui", "menuid", string, "menuid2", string));
                return;
            }
            this.plugin.getGuiLayoutManager().clear(string);
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.cleared-saved-layout-slots", "menuid", string, "menuid2", string));
            return;
        }
        new GuiEditorMenu(this.plugin, string).open((Player)commandSender);
    }

    private void scoreboardIp(CommandSender commandSender, String[] stringArray) {
        if (!this.requireAdmin(commandSender)) {
            return;
        }
        if (stringArray.length < 1) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.current-scoreboard-ip", "scoreboardip", String.valueOf(this.plugin.getScoreboardIp())));
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-scoreboardip-ip-color", new String[0]));
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.color-minimessage-e-g", new String[0]));
            return;
        }
        this.plugin.setScoreboardIp(stringArray[0]);
        if (stringArray.length >= 2) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 1; i < stringArray.length; ++i) {
                if (i > 1) {
                    stringBuilder.append(' ');
                }
                stringBuilder.append(stringArray[i]);
            }
            this.plugin.getConfig().set("scoreboard-ip-color", (Object)stringBuilder.toString());
            this.plugin.saveConfig();
            this.plugin.getScoreboardService().reload();
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.scoreboard-ip-set-2", "name", stringArray[0], "value", String.valueOf(stringBuilder)));
        } else {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.scoreboard-ip-set", "name", stringArray[0]));
        }
    }

    private void openTrims(CommandSender commandSender, String[] stringArray) {
        if (!this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        if (!Trims.canEdit(player)) {
            player.sendMessage(this.plugin.messages().get("notice.admin.don-t-have-permission-2", new String[0]));
            return;
        }
        if (!Trims.available()) {
            player.sendMessage(this.plugin.messages().get("notice.admin.armor-trims-aren-t", new String[0]));
            return;
        }
        if (stringArray.length >= 1) {
            if (!this.plugin.getKitManager().exists(stringArray[0])) {
                player.sendMessage(this.plugin.messages().get("notice.admin.no-kit-named-3", "name", stringArray[0]));
                return;
            }
            new TrimMenu(this.plugin, this.plugin.getKitManager().get(stringArray[0]).getName(), player.getUniqueId()).open(player);
            return;
        }
        new TrimKitMenu(this.plugin).open(player);
    }

    private void setServerName(CommandSender commandSender, String[] stringArray) {
        if (!this.requireAdmin(commandSender)) {
            return;
        }
        if (stringArray.length < 1) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.current-scoreboard-server-name", "servername", String.valueOf(this.plugin.getServerName())));
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-meowduelsserver-name", new String[0]));
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stringArray.length; ++i) {
            if (i > 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(stringArray[i]);
        }
        this.plugin.setServerName(stringBuilder.toString());
        this.plugin.getScoreboardService().reload();
        commandSender.sendMessage(this.plugin.messages().get("notice.admin.scoreboard-server-name-set", "value", String.valueOf(stringBuilder)));
    }

    private void duel(CommandSender commandSender, String[] stringArray) {
        if (!this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        if (stringArray.length == 0) {
            commandSender.sendMessage(this.msg("duel.usage", new String[0]));
            return;
        }
        if (this.checkBusy(player, true, true, true)) {
            return;
        }
        if (stringArray[0].equalsIgnoreCase("accept")) {
            this.handleAccept(player, stringArray);
            return;
        }
        if (this.plugin.getKitManager().isEmpty()) {
            commandSender.sendMessage(this.msg("duel.no-kits", new String[0]));
            return;
        }
        Player player2 = this.plugin.getServer().getPlayerExact(stringArray[0]);
        if (player2 == null) {
            commandSender.sendMessage(this.msg("duel.target-offline", "name", stringArray[0]));
            return;
        }
        if (player2.equals((Object)player)) {
            commandSender.sendMessage(this.msg("duel.cannot-duel-self", new String[0]));
            return;
        }
        if (stringArray.length >= 2) {
            Kit kit = this.plugin.getKitManager().get(stringArray[1]);
            if (kit == null) {
                commandSender.sendMessage(this.msg("duel.kit-gone", new String[0]));
                return;
            }
            int n = Math.max(1, this.plugin.getConfig().getInt("duel.default-rounds", 1));
            if (stringArray.length >= 3) {
                try {
                    n = Integer.parseInt(stringArray[2]);
                }
                catch (NumberFormatException numberFormatException) {
                    commandSender.sendMessage(this.plugin.messages().get("notice.admin.rounds-must-number-e", "target", player2.getName(), "kit", kit.getName()));
                    return;
                }
                if (n < 1) {
                    n = 1;
                }
                if (n > 99) {
                    n = 99;
                }
            }
            this.plugin.getDuelManager().sendRequest(player, player2, kit.getName(), n, null);
            return;
        }
        DuelConfirmMenu duelConfirmMenu = new DuelConfirmMenu(this.plugin, player, player2);
        new KitPickMenu(this.plugin, duelConfirmMenu).open(player);
    }

    private boolean checkBusy(Player player, boolean bl, boolean bl2, boolean bl3) {
        UUID uUID = player.getUniqueId();
        if (this.plugin.getPartyManager().busy(player)) {
            return true;
        }
        if (this.plugin.getEventManager().isInvolved(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.admin.re-event-type-leave", new String[0]));
            return true;
        }
        if (bl && this.plugin.getSpectateManager().isSpectating(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.admin.re-spectating-type-leave", new String[0]));
            return true;
        }
        if (bl2 && this.plugin.getQueueManager().isQueued(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.admin.re-queue-type-leave-2", new String[0]));
            return true;
        }
        if (bl3 && this.plugin.getDuelManager().isInDuel(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.admin.re-duel-type-leave-2", new String[0]));
            return true;
        }
        return false;
    }

    private void event(CommandSender commandSender, String[] stringArray) {
        if (!this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        if (stringArray.length == 0) {
            this.plugin.getEventManager().join(player);
            return;
        }
        if (stringArray[0].equalsIgnoreCase("host")) {
            int n;
            if (!player.hasPermission("meowduels.event.host")) {
                player.sendMessage(this.msg("event.no-permission", new String[0]));
                return;
            }
            if (stringArray.length >= 2 && stringArray[1].equalsIgnoreCase("force_start")) {
                this.plugin.getEventManager().forceStart(player);
                return;
            }
            if (stringArray.length >= 2 && stringArray[1].equalsIgnoreCase("force_end")) {
                this.plugin.getEventManager().forceEnd(player);
                return;
            }
            if (stringArray.length < 3) {
                player.sendMessage(this.msg("event.host-usage", new String[0]));
                return;
            }
            if (!this.plugin.getKitManager().exists(stringArray[1])) {
                player.sendMessage(this.msg("event.no-kit", new String[0]));
                return;
            }
            try {
                n = Integer.parseInt(stringArray[2]);
            }
            catch (NumberFormatException numberFormatException) {
                player.sendMessage(this.plugin.messages().get("notice.admin.minutes-must-number-e", "name", stringArray[1]));
                return;
            }
            if (n < 1) {
                n = 1;
            }
            int n2 = 0;
            if (stringArray.length >= 4) {
                try {
                    n2 = Integer.parseInt(stringArray[3]);
                }
                catch (NumberFormatException numberFormatException) {
                    player.sendMessage(this.plugin.messages().get("notice.admin.slots-must-number-e", "name", stringArray[1]));
                    return;
                }
                if (n2 < 0) {
                    n2 = 0;
                }
            }
            if (this.plugin.getEventManager().getState() != EventManager.State.NONE) {
                player.sendMessage(this.msg("event.already-running", new String[0]));
                return;
            }
            new EventMapMenu(this.plugin, player, stringArray[1], n, n2).open(player);
            return;
        }
        this.plugin.getEventManager().join(player);
    }

    private void eventSpectate(CommandSender commandSender) {
        if (!this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        UUID uUID = player.getUniqueId();
        if (this.plugin.getDuelManager().isInDuel(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.admin.re-duel-type-leave", new String[0]));
            return;
        }
        if (this.plugin.getSpectateManager().isSpectating(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.admin.re-already-spectating-type", new String[0]));
            return;
        }
        if (this.plugin.getQueueManager().isQueued(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.admin.re-queue-type-leave", new String[0]));
            return;
        }
        this.plugin.getEventManager().spectate(player);
    }

    private void eventLeave(CommandSender commandSender) {
        if (!this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        if (this.plugin.getEventManager().isInvolved(player.getUniqueId())) {
            this.plugin.getEventManager().leave(player);
        } else {
            player.sendMessage(this.plugin.messages().get("notice.admin.aren-t-event", new String[0]));
        }
    }

    private void forfeit(CommandSender commandSender) {
        if (!this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        if (this.plugin.getDuelManager().isInDuel(player.getUniqueId())) {
            this.plugin.getDuelManager().leave(player);
        } else {
            player.sendMessage(this.plugin.messages().get("notice.admin.aren-t-duel", new String[0]));
        }
    }

    private List<String> partyComplete(CommandSender commandSender, Command command, String[] stringArray) {
        ArrayList<String> arrayList;
        block14: {
            String string;
            Player player;
            block13: {
                if (!command.getName().equalsIgnoreCase("party")) {
                    return null;
                }
                arrayList = new ArrayList<String>();
                if (stringArray.length <= 1) {
                    String string2 = stringArray.length == 0 ? "" : stringArray[0];
                    for (String string3 : new String[]{"create", "invite", "join", "decline", "duel", "spectate", "force_end", "leave", "disband", "public", "promote"}) {
                        if (!this.startsWith(string3, string2)) continue;
                        arrayList.add(string3);
                    }
                    return arrayList;
                }
                if (!(commandSender instanceof Player)) {
                    return arrayList;
                }
                player = (Player)commandSender;
                string = stringArray[0].toLowerCase(Locale.ROOT);
                if (string.equals("duel")) {
                    if (stringArray.length == 2) {
                        for (String string4 : new String[]{"accept", "decline"}) {
                            if (!this.startsWith(string4, stringArray[1])) continue;
                            arrayList.add(string4);
                        }
                    } else if (stringArray.length == 3) {
                        for (String string5 : this.plugin.getPartyManager().duelChallengers(player.getUniqueId())) {
                            if (!this.startsWith(string5, stringArray[2])) continue;
                            arrayList.add(string5);
                        }
                    }
                    return arrayList;
                }
                if (stringArray.length != 2) {
                    return arrayList;
                }
                if (!string.equals("invite")) break block13;
                for (Player player2 : this.plugin.getServer().getOnlinePlayers()) {
                    if (player2.getUniqueId().equals(player.getUniqueId()) || this.plugin.getPartyManager().inParty(player2.getUniqueId()) || !this.startsWith(player2.getName(), stringArray[1])) continue;
                    arrayList.add(player2.getName());
                }
                break block14;
            }
            if (!string.equals("join") && !string.equals("decline")) break block14;
            ArrayList<String> partyTargets = new ArrayList<String>(this.plugin.getPartyManager().invitersOf(player.getUniqueId()));
            if (string.equals("join")) {
                for (String leader : this.plugin.getPartyManager().publicPartyLeaders()) {
                    if (partyTargets.stream().anyMatch(name -> name.equalsIgnoreCase(leader))) continue;
                    partyTargets.add(leader);
                }
            }
            for (String string6 : partyTargets) {
                if (!this.startsWith(string6, stringArray[1])) continue;
                arrayList.add(string6);
            }
        }
        return arrayList;
    }

    private void party(CommandSender commandSender, String[] stringArray) {
        String string;
        if (!this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        String string2 = string = stringArray.length > 0 ? stringArray[0].toLowerCase(Locale.ROOT) : "";
        if (string.equals("hungergames") || string.equals("hg")) {
            this.plugin.getHungerGamesManager().openHostMenu(player);
            return;
        }
        if (string.equals("create")) {
            this.plugin.getPartyManager().create(player);
            return;
        }
        if (string.equals("public")) {
            this.plugin.getPartyManager().togglePublic(player);
            return;
        }
        if (string.equals("promote")) {
            this.plugin.getPartyManager().promote(player);
            return;
        }
        if (string.equals("leave") || string.equals("disband")) {
            this.plugin.getPartyManager().leave(player);
            return;
        }
        if (string.equals("duel")) {
            String string3;
            String string4 = string3 = stringArray.length > 1 ? stringArray[1].toLowerCase(Locale.ROOT) : "";
            if (stringArray.length < 3 || !string3.equals("accept") && !string3.equals("decline")) {
                player.sendMessage(this.plugin.messages().get("notice.admin.usage-party-duel-accept", new String[0]));
                return;
            }
            Player player2 = this.plugin.getServer().getPlayerExact(stringArray[2]);
            if (player2 == null) {
                player.sendMessage(this.plugin.messages().get("notice.admin.isn-t-online-2", "name", stringArray[2]));
                return;
            }
            if (string3.equals("accept")) {
                this.plugin.getPartyManager().acceptDuels(player, player2);
            } else {
                this.plugin.getPartyManager().declineDuels(player, player2);
            }
            return;
        }
        if (string.equals("force_end") || string.equals("forceend")) {
            this.plugin.getPartyManager().forceEnd(player);
            return;
        }
        if (string.equals("join") && stringArray.length < 2) {
            List<String> publicParties = this.plugin.getPartyManager().publicPartyLeaders();
            if (publicParties.size() == 1) {
                Player leader = this.plugin.getServer().getPlayerExact(publicParties.get(0));
                if (leader != null) {
                    this.plugin.getPartyManager().accept(player, leader);
                    return;
                }
            }
            if (publicParties.isEmpty()) {
                player.sendMessage(Text.color("&eThere are no public parties available to join."));
            } else {
                player.sendMessage(Text.color("&ePublic parties: &f" + String.join("&7, &f", publicParties)));
                player.sendMessage(Text.color("&7Use &f/party join <leader> &7to choose one."));
            }
            return;
        }
        if (string.equals("invite") || string.equals("join") || string.equals("accept") || string.equals("decline")) {
            if (stringArray.length < 2) {
                player.sendMessage(this.plugin.messages().get("notice.admin.usage-party-player", "sub", string));
                return;
            }
            Player player3 = this.plugin.getServer().getPlayerExact(stringArray[1]);
            if (player3 == null) {
                player.sendMessage(this.plugin.messages().get("notice.admin.isn-t-online", "name", stringArray[1]));
                return;
            }
            if (string.equals("invite")) {
                this.plugin.getPartyManager().invite(player, player3);
            } else if (string.equals("decline")) {
                this.plugin.getPartyManager().decline(player, player3);
            } else {
                this.plugin.getPartyManager().accept(player, player3);
            }
            return;
        }
        if (string.equals("spectate")) {
            this.plugin.getPartyManager().spectate(player);
            return;
        }
        if (this.plugin.getPartyManager().inParty(player.getUniqueId())) {
            new PartyMenu(this.plugin).openFor(player);
            return;
        }
        player.sendMessage(this.plugin.messages().get("notice.admin.aren-t-party-party", new String[0]));
    }

    private void leave(CommandSender commandSender) {
        if (!this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        UUID uUID = player.getUniqueId();
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (player.isOnline() && !this.plugin.getDuelManager().isInDuel(player.getUniqueId())) {
                this.plugin.giveSpawnItems(player);
            }
        }, 2L);
        if (this.plugin.getPartyManager().leaveMatch(player)) {
            return;
        }
        if (this.plugin.getEventManager().isInvolved(uUID)) {
            this.plugin.getEventManager().leave(player);
            return;
        }
        if (this.plugin.getDuelManager().isInDuel(uUID)) {
            this.plugin.getDuelManager().leave(player);
            return;
        }
        if (this.plugin.getSpectateManager().isSpectating(uUID)) {
            this.plugin.getSpectateManager().stop(player);
            return;
        }
        if (this.plugin.getQueueManager().isQueued(uUID)) {
            this.plugin.getQueueManager().leaveAll(player);
            return;
        }
        if (this.plugin.getPartyManager().inParty(uUID)) {
            player.sendMessage(this.plugin.messages().get("notice.admin.re-party-use-leave", new String[0]));
            return;
        }
        player.sendMessage(this.plugin.messages().get("notice.admin.aren-t-event-duel", new String[0]));
    }

    private void editKit(CommandSender commandSender, String[] stringArray) {
        if (!this.requireAdmin(commandSender) || !this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        if (stringArray.length >= 1) {
            Kit kit = this.plugin.getKitManager().get(stringArray[0]);
            if (kit == null) {
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-kit-named-2", "name", stringArray[0]));
                return;
            }
            if (stringArray.length >= 2) {
                String option = stringArray[1].toLowerCase(Locale.ROOT);
                if (option.equals("gui") || option.equals("visible")) {
                    if (stringArray.length < 3 || !(stringArray[2].equalsIgnoreCase("true") || stringArray[2].equalsIgnoreCase("false") || stringArray[2].equalsIgnoreCase("on") || stringArray[2].equalsIgnoreCase("off"))) {
                        player.sendMessage(Text.color("&cUsage: /starduels editkit <kit> gui <true|false>"));
                        return;
                    }
                    kit.setGuiVisible(stringArray[2].equalsIgnoreCase("true") || stringArray[2].equalsIgnoreCase("on"));
                    this.plugin.getKitManager().save();
                    player.sendMessage(Text.color("&a" + kit.getName() + " is now " + (kit.isGuiVisible() ? "visible" : "hidden") + " in kit GUIs."));
                    return;
                }
                if (option.equals("prefix")) {
                    if (stringArray.length < 3) {
                        player.sendMessage(Text.color("&cUsage: /starduels editkit <kit> prefix <text|clear>"));
                        return;
                    }
                    String prefix = this.joinFrom(stringArray, 2);
                    kit.setPrefix(prefix.equalsIgnoreCase("clear") ? "" : prefix);
                    this.plugin.getKitManager().save();
                    player.sendMessage(Text.color("&aUpdated the queue prefix for " + kit.getName() + "."));
                    return;
                }
                if (option.equals("name") || option.equals("displayname")) {
                    if (stringArray.length < 3) {
                        player.sendMessage(Text.color("&cUsage: /starduels editkit <kit> name <text>"));
                        return;
                    }
                    kit.setDisplayName(this.joinFrom(stringArray, 2));
                    this.plugin.getKitManager().save();
                    player.sendMessage(Text.color("&aUpdated the display name for " + kit.getName() + "."));
                    return;
                }
                if (option.equals("nameicon") || option.equals("name-icon")) {
                    if (stringArray.length < 3) {
                        player.sendMessage(Text.color("&cUsage: /starduels editkit <kit> nameicon <item/tnt_minecart|head:Name|none|auto>"));
                        return;
                    }
                    String value = stringArray[2];
                    kit.setNameIcon(value.equalsIgnoreCase("auto") || value.equalsIgnoreCase("default") ? null : value);
                    this.plugin.getKitManager().save();
                    player.sendMessage(Text.color("&aUpdated the name icon for " + kit.getName() + ". It now reads:"));
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(kit.withIcon(kit.getDisplayName() == null || kit.getDisplayName().isBlank() ? kit.getName() : kit.getDisplayName())));
                    return;
                }
                if (option.equals("icon")) {
                    if (stringArray.length < 3) {
                        player.sendMessage(Text.color("&cUsage: /starduels editkit <kit> icon <material>"));
                        return;
                    }
                    Material icon = Material.matchMaterial(stringArray[2]);
                    if (icon == null || !icon.isItem()) {
                        player.sendMessage(Text.color("&cUnknown item material: " + stringArray[2]));
                        return;
                    }
                    kit.setIcon(icon);
                    this.plugin.getKitManager().save();
                    player.sendMessage(Text.color("&aUpdated the GUI icon for " + kit.getName() + " to " + icon.name() + "."));
                    return;
                }
            }
            new KitItemsEditMenu(this.plugin, kit).open(player);
            return;
        }
        if (this.plugin.getKitManager().isEmpty()) {
            commandSender.sendMessage(this.msg("duel.no-kits", new String[0]));
            return;
        }
        new EditKitListMenu(this.plugin).open(player);
    }

    private void spectate(CommandSender commandSender, String[] stringArray) {
        if (!this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        if (stringArray.length == 0) {
            this.plugin.getSpectateManager().stop(player);
            return;
        }
        if (this.checkBusy(player, false, true, true)) {
            return;
        }
        Player player2 = this.plugin.getServer().getPlayerExact(stringArray[0]);
        if (player2 == null) {
            commandSender.sendMessage(this.msg("spectate.not-found", "name", stringArray[0]));
            return;
        }
        this.plugin.getSpectateManager().spectate(player, player2);
    }

    private void queue(CommandSender commandSender, String[] stringArray) {
        if (!this.requirePlayer(commandSender)) {
            return;
        }
        Player player = (Player)commandSender;
        if (stringArray.length >= 1 && stringArray[0].equalsIgnoreCase("leave")) {
            this.plugin.getQueueManager().leaveAll(player);
            return;
        }
        if (this.checkBusy(player, true, false, true)) {
            return;
        }
        if (stringArray.length >= 1) {
            Kit kit = this.plugin.getKitManager().get(stringArray[0]);
            if (kit == null) {
                commandSender.sendMessage(this.msg("queue.kit-gone", new String[0]));
                return;
            }
            this.plugin.getQueueManager().join(player, kit.getName());
            return;
        }
        if (this.plugin.getDuelManager().isInDuel(player.getUniqueId())) {
            commandSender.sendMessage(this.msg("duel.already-in-duel", new String[0]));
            return;
        }
        if (this.plugin.getKitManager().isEmpty()) {
            commandSender.sendMessage(this.msg("duel.no-kits", new String[0]));
            return;
        }
        new QueuePickMenu(this.plugin).open(player);
    }

    private void handleAccept(Player player, String[] stringArray) {
        UUID uUID;
        if (stringArray.length >= 2) {
            Player player2 = this.plugin.getServer().getPlayerExact(stringArray[1]);
            if (player2 == null) {
                player.sendMessage(this.msg("accept.target-offline", new String[0]));
                return;
            }
            uUID = player2.getUniqueId();
        } else {
            DuelRequest duelRequest = this.plugin.getDuelManager().getMostRecentRequest(player);
            if (duelRequest == null) {
                player.sendMessage(this.msg("accept.no-requests", new String[0]));
                return;
            }
            uUID = duelRequest.getSender();
        }
        this.plugin.getDuelManager().acceptRequest(player, uUID);
    }

    private void categoryCommand(CommandSender commandSender, String[] stringArray, CategoryManager categoryManager, String string2) {
        if (!this.requireAdmin(commandSender)) {
            return;
        }
        String string3 = stringArray.length >= 1 ? stringArray[0].toLowerCase(Locale.ROOT) : "list";
        switch (string3) {
            case "list": {
                if (categoryManager.all().isEmpty()) {
                    commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-categories-yet-create", "cmd", string2));
                    return;
                }
                commandSender.sendMessage(Text.color("&eKit categories:"));
                for (CategoryManager.Category category : categoryManager.all()) {
                    commandSender.sendMessage(Text.color("&e" + category.getId() + " &8- header &f" + category.getHeader() + " &8- kits: &f" + (category.getKits().isEmpty() ? "all" : String.join((CharSequence)", ", category.getKits()))));
                }
                break;
            }
            case "create": {
                if (stringArray.length < 2) {
                    commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-create-id-header", "cmd", string2));
                    return;
                }
                String string4 = stringArray.length >= 3 ? this.joinFrom(stringArray, 2) : stringArray[1].toUpperCase(Locale.ROOT);
                String string5 = string4;
                if (categoryManager.create(stringArray[1], string4)) {
                    commandSender.sendMessage(this.plugin.messages().get("notice.admin.created-category", "name", stringArray[1]));
                    break;
                }
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.category-id-already-exists", new String[0]));
                break;
            }
            case "delete": {
                if (stringArray.length < 2) {
                    commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-delete-id", "cmd", string2));
                    return;
                }
                categoryManager.delete(stringArray[1]);
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.deleted-category", "name", stringArray[1]));
                break;
            }
            case "header": {
                if (stringArray.length < 3) {
                    commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-header-id-text", "cmd", string2));
                    return;
                }
                CategoryManager.Category category = categoryManager.get(stringArray[1]);
                if (category == null) {
                    commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-category-named-2", "name", stringArray[1]));
                    return;
                }
                category.setHeader(this.joinFrom(stringArray, 2));
                categoryManager.save();
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.header-set", "id", String.valueOf(category.getId()), "header", category.getHeader()));
                break;
            }
            case "addkit": 
            case "removekit": {
                if (stringArray.length < 3) {
                    commandSender.sendMessage(this.plugin.messages().get("notice.admin.usage-id-kit", "cmd", string2, "sub", string3));
                    return;
                }
                CategoryManager.Category category = categoryManager.get(stringArray[1]);
                if (category == null) {
                    commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-category-named", "name", stringArray[1]));
                    return;
                }
                boolean bl = string3.equals("addkit");
                if (bl) {
                    if (!this.plugin.getKitManager().exists(stringArray[2])) {
                        commandSender.sendMessage(this.plugin.messages().get("notice.admin.no-kit-named", "name", stringArray[2]));
                        return;
                    }
                    this.addKitDedup(category, stringArray[2]);
                    commandSender.sendMessage(this.plugin.messages().get("notice.admin.added", "name", stringArray[2], "id", String.valueOf(category.getId())));
                } else {
                    category.getKits().removeIf(string -> string.equalsIgnoreCase(stringArray[2]));
                    commandSender.sendMessage(this.plugin.messages().get("notice.admin.removed-from", "name", stringArray[2], "id", String.valueOf(category.getId())));
                }
                categoryManager.save();
                CategoryManager categoryManager2 = categoryManager == this.plugin.getCategoryManager() ? this.plugin.getQueueCategoryManager() : this.plugin.getCategoryManager();
                this.syncKit(categoryManager2, category.getId(), category.getHeader(), stringArray[2], bl);
                break;
            }
            default: {
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.sub-commands-list-create", new String[0]));
            }
        }
    }

    private void addKitDedup(CategoryManager.Category category, String string) {
        if (category.getKits().stream().noneMatch(string2 -> string2.equalsIgnoreCase(string))) {
            category.getKits().add(string);
        }
    }

    private void syncKit(CategoryManager categoryManager, String string, String string3, String string4, boolean bl) {
        CategoryManager.Category category = categoryManager.get(string);
        if (category == null) {
            if (!bl) {
                return;
            }
            categoryManager.create(string, string3);
            category = categoryManager.get(string);
            if (category == null) {
                return;
            }
        }
        if (bl) {
            this.addKitDedup(category, string4);
        } else {
            category.getKits().removeIf(string2 -> string2.equalsIgnoreCase(string4));
        }
        categoryManager.save();
    }

    private String joinFrom(String[] stringArray, int n) {
        return String.join((CharSequence)" ", Arrays.copyOfRange(stringArray, n, stringArray.length));
    }

    private void root(CommandSender commandSender, String[] stringArray) {
        if (stringArray.length >= 1 && stringArray[0].equalsIgnoreCase("setspawn")) {
            this.setDuelSpawn(commandSender);
            return;
        }
        if (stringArray.length >= 1 && stringArray[0].equalsIgnoreCase("reload")) {
            if (!this.requireAdmin(commandSender)) {
                return;
            }
            this.plugin.reloadAll();
            commandSender.sendMessage(this.msg("general.reloaded", new String[0]));
            return;
        }
        if (stringArray.length >= 1 && stringArray[0].equalsIgnoreCase("scoreboardip")) {
            this.scoreboardIp(commandSender, Arrays.copyOfRange(stringArray, 1, stringArray.length));
            return;
        }
        if (stringArray.length >= 1 && stringArray[0].equalsIgnoreCase("createkit")) {
            this.createKit(commandSender, Arrays.copyOfRange(stringArray, 1, stringArray.length));
            return;
        }
        if (stringArray.length >= 1 && stringArray[0].equalsIgnoreCase("editkit")) {
            this.editKit(commandSender, Arrays.copyOfRange(stringArray, 1, stringArray.length));
            return;
        }
        if (stringArray.length >= 1 && stringArray[0].equalsIgnoreCase("viewkits")) {
            if (!this.requireAdmin(commandSender)) return;
            commandSender.sendMessage(Text.color("&6&lStarDuels kits &7(GUI-visible kits are listed in duel and queue menus)"));
            for (Kit kit : this.plugin.getKitManager().all()) {
                String label = kit.getDisplayName() == null ? kit.getName() : kit.getDisplayName();
                commandSender.sendMessage(Text.color((kit.isGuiVisible() ? "&a[GUI] " : "&8[HIDDEN] ") + "&f" + kit.getName() + " &7- " + label + " &8| prefix: " + (kit.getPrefix().isEmpty() ? "(none)" : kit.getPrefix())));
            }
            commandSender.sendMessage(Text.color("&7Create: /starduels createkit <name> [gui|hidden] [prefix <text>]"));
            commandSender.sendMessage(Text.color("&7Edit: /starduels editkit <name> [gui <true|false>|prefix <text>|name <text>|icon <material>]"));
            return;
        }
        if (stringArray.length >= 2 && (stringArray[1].equalsIgnoreCase("copy") || stringArray[1].equalsIgnoreCase("paste"))) {
            this.arenaClone(commandSender, stringArray[0], stringArray[1]);
            return;
        }
        this.sendHelp(commandSender);
    }

    private void sendHelp(CommandSender commandSender) {
        commandSender.sendMessage(Text.color("&8&m----------------------------------------"));
        commandSender.sendMessage(Text.color("&e&lStarDuels &f- commands"));
        commandSender.sendMessage(Text.color("&e/duel <player> &f- challenge a player"));
        commandSender.sendMessage(Text.color("&e/leave &f- forfeit your current duel"));
        commandSender.sendMessage(Text.color("&e/queue &f- open the 1v1 queue menu"));
        commandSender.sendMessage(Text.color("&e/starduels viewkits &f- list kits and their GUI visibility"));
        if (commandSender.hasPermission("meowduels.admin")) {
            commandSender.sendMessage(Text.color("&e/starduels createkit <name> [gui|hidden] &f- create a kit from your inventory"));
            commandSender.sendMessage(Text.color("&e/starduels editkit <name> &f- edit kit contents/effects, GUI visibility, prefix, name, or icon"));
            commandSender.sendMessage(Text.color("&e/createarena <name> &f- create an arena"));
            commandSender.sendMessage(Text.color("&e/arena <name> &f- setup / settings GUI"));
            commandSender.sendMessage(Text.color("&e/arena <name> copy|paste &f- clone the arena; the copy"));
            commandSender.sendMessage(Text.color("  &fauto-registers itself, ready to use immediately"));
            commandSender.sendMessage(Text.color("&e/arena <name> regen|snapshot &f- manual regen tools"));
            commandSender.sendMessage(Text.color("&e/deletearena <name> &f- delete an arena"));
            commandSender.sendMessage(Text.color("&e/starduels setspawn &f- set where duels send players when they end"));
            commandSender.sendMessage(Text.color("&e/kitcreate <name> &f- save a kit from inventory"));
            commandSender.sendMessage(Text.color("&e/kiteditor &f- GUI item editor for every kit/gamemode"));
            commandSender.sendMessage(Text.color("&e/deletekit <name> &f- delete a kit"));
            commandSender.sendMessage(Text.color("&e/kiticon <name> &f- set a kit's icon to your held item"));
            commandSender.sendMessage(Text.color("&e/editgui <menu> [reset] &f- arrange a GUI, or clear it back to gui/<menu>.yml"));
            commandSender.sendMessage(Text.color("&e/category ... &f- manage kit-menu categories"));
            commandSender.sendMessage(Text.color("&e/scoreboardip <ip> &f- set scoreboard IP"));
        }
        commandSender.sendMessage(Text.color("&8&m----------------------------------------"));
    }

    private boolean requireAdmin(CommandSender commandSender) {
        if (!commandSender.hasPermission("meowduels.admin")) {
            commandSender.sendMessage(this.msg("general.no-permission", new String[0]));
            return false;
        }
        return true;
    }

    private boolean requirePlayer(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(this.msg("general.players-only", new String[0]));
            return false;
        }
        return true;
    }

    private String msg(String string, String ... stringArray) {
        return this.plugin.messages().get(string, stringArray);
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String string, String[] stringArray) {
        String rootCommand = command.getName().toLowerCase(Locale.ROOT);
        if ((rootCommand.equals("starduels") || rootCommand.equals("sd") || rootCommand.equals("md") || rootCommand.equals("meowduels")) && stringArray.length == 1) {
            ArrayList<String> choices = new ArrayList<String>();
            for (String option : List.of("createkit", "editkit", "viewkits", "reload", "scoreboardip", "setspawn", "help")) if (this.startsWith(option, stringArray[0])) choices.add(option);
            return choices;
        }
        if ((rootCommand.equals("starduels") || rootCommand.equals("sd") || rootCommand.equals("md") || rootCommand.equals("meowduels")) && stringArray.length >= 2) {
            ArrayList<String> choices = new ArrayList<String>();
            if (stringArray[0].equalsIgnoreCase("editkit") && stringArray.length == 2) {
                for (Kit kit : this.plugin.getKitManager().all()) if (this.startsWith(kit.getName(), stringArray[1])) choices.add(kit.getName());
                return choices;
            }
            if (stringArray[0].equalsIgnoreCase("editkit") && stringArray.length == 3) {
                for (String option : List.of("gui", "prefix", "name", "icon", "nameicon")) if (this.startsWith(option, stringArray[2])) choices.add(option);
                return choices;
            }
            if (stringArray[0].equalsIgnoreCase("editkit") && stringArray.length == 4 && stringArray[2].equalsIgnoreCase("gui")) {
                for (String option : List.of("true", "false")) if (this.startsWith(option, stringArray[3])) choices.add(option);
                return choices;
            }
        }
        ArrayList<String> arrayList;
        block40: {
            String string2;
            block46: {
                block44: {
                    CategoryManager categoryManager;
                    block45: {
                        block42: {
                            block43: {
                                block41: {
                                    block39: {
                                        List<String> list = this.partyComplete(commandSender, command, stringArray);
                                        if (list != null) {
                                            return list;
                                        }
                                        String object = command.getName().toLowerCase(Locale.ROOT);
                                        if (object.equals("queue") && stringArray.length == 1) {
                                            ArrayList<String> arrayList2 = new ArrayList<String>();
                                            if (this.startsWith("leave", stringArray[0])) {
                                                arrayList2.add("leave");
                                            }
                                            for (Kit kit : this.plugin.getKitManager().all()) {
                                                if (!this.startsWith(kit.getName(), stringArray[0])) continue;
                                                arrayList2.add(kit.getName());
                                            }
                                            return arrayList2;
                                        }
                                        if (object.equals("event")) {
                                            ArrayList<String> category = new ArrayList<String>();
                                            if (stringArray.length == 1) {
                                                if (this.startsWith("host", stringArray[0])) {
                                                    category.add("host");
                                                }
                                            } else if (stringArray.length == 2 && stringArray[0].equalsIgnoreCase("host")) {
                                                if (this.startsWith("force_start", stringArray[1])) {
                                                    category.add("force_start");
                                                }
                                                if (this.startsWith("force_end", stringArray[1])) {
                                                    category.add("force_end");
                                                }
                                                for (Kit kit : this.plugin.getKitManager().all()) {
                                                    if (!this.startsWith(kit.getName(), stringArray[1])) continue;
                                                    category.add(kit.getName());
                                                }
                                            } else if (stringArray.length == 3 && stringArray[0].equalsIgnoreCase("host")) {
                                                for (String string3 : List.of("1", "3", "5", "10")) {
                                                    if (!string3.startsWith(stringArray[2])) continue;
                                                    category.add(string3);
                                                }
                                            }
                                            return category;
                                        }
                                        if (object.equals("meowduelstrims") && stringArray.length == 1) {
                                            ArrayList<String> arrayList3 = new ArrayList<String>();
                                            for (Kit kit : this.plugin.getKitManager().all()) {
                                                if (!this.startsWith(kit.getName(), stringArray[0])) continue;
                                                arrayList3.add(kit.getName());
                                            }
                                            return arrayList3;
                                        }
                                        if (object.equals("givegoldenhead") && stringArray.length == 1) {
                                            ArrayList<String> arrayList4 = new ArrayList<String>();
                                            for (Player player : this.plugin.getServer().getOnlinePlayers()) {
                                                if (!this.startsWith(player.getName(), stringArray[0])) continue;
                                                arrayList4.add(player.getName());
                                            }
                                            return arrayList4;
                                        }
                                        if (object.equals("givegoldenhead") && stringArray.length == 2) {
                                            ArrayList<String> arrayList5 = new ArrayList<String>();
                                            for (String string4 : List.of("1", "8", "16", "32", "64")) {
                                                if (!string4.startsWith(stringArray[1])) continue;
                                                arrayList5.add(string4);
                                            }
                                            return arrayList5;
                                        }
                                        if (object.equals("duel") && stringArray.length == 2) {
                                            ArrayList<String> arrayList6 = new ArrayList<String>();
                                            for (Kit kit : this.plugin.getKitManager().all()) {
                                                if (!this.startsWith(kit.getName(), stringArray[1])) continue;
                                                arrayList6.add(kit.getName());
                                            }
                                            return arrayList6;
                                        }
                                        if (object.equals("duel") && stringArray.length == 3) {
                                            ArrayList<String> arrayList7 = new ArrayList<String>();
                                            for (String string5 : List.of("1", "3", "5")) {
                                                if (!string5.startsWith(stringArray[2])) continue;
                                                arrayList7.add(string5);
                                            }
                                            return arrayList7;
                                        }
                                        string2 = command.getName().toLowerCase(Locale.ROOT);
                                        arrayList = new ArrayList<String>();
                                        if (!string2.equals("arena") && !string2.equals("deletearena") || stringArray.length != 1) break block39;
                                        for (Arena arena : this.plugin.getArenaManager().all()) {
                                            if (!this.startsWith(arena.getName(), stringArray[0])) continue;
                                            arrayList.add(arena.getName());
                                        }
                                        break block40;
                                    }
                                    if (!string2.equals("deletekit") && !string2.equals("kiticon") && !string2.equals("kitdisplayname") && !string2.equals("editkit") || stringArray.length != 1) break block41;
                                    for (Kit kit : this.plugin.getKitManager().all()) {
                                        if (!this.startsWith(kit.getName(), stringArray[0])) continue;
                                        arrayList.add(kit.getName());
                                    }
                                    break block40;
                                }
                                if (!string2.equals("editgui")) break block42;
                                if (stringArray.length != 1) break block43;
                                for (String string6 : List.of("duelconfirm", "mapselect")) {
                                    if (!this.startsWith(string6, stringArray[0])) continue;
                                    arrayList.add(string6);
                                }
                                break block40;
                            }
                            if (stringArray.length != 2 || !this.startsWith("reset", stringArray[1])) break block40;
                            arrayList.add("reset");
                            break block40;
                        }
                        if (!string2.equals("category") && !string2.equals("categoryqueue")) break block44;
                        CategoryManager categoryManager2 = categoryManager = string2.equals("categoryqueue") ? this.plugin.getQueueCategoryManager() : this.plugin.getCategoryManager();
                        if (stringArray.length != 1) break block45;
                        for (String string7 : List.of("list", "create", "delete", "header", "addkit", "removekit")) {
                            if (!this.startsWith(string7, stringArray[0])) continue;
                            arrayList.add(string7);
                        }
                        break block40;
                    }
                    if (stringArray.length != 2 || stringArray[0].equalsIgnoreCase("create")) break block40;
                    for (CategoryManager.Category category : categoryManager.all()) {
                        if (!this.startsWith(category.getId(), stringArray[1])) continue;
                        arrayList.add(category.getId());
                    }
                    break block40;
                }
                if (!string2.equals("queue") || stringArray.length != 1) break block46;
                if (!this.startsWith("leave", stringArray[0])) break block40;
                arrayList.add("leave");
                break block40;
            }
            if ((string2.equals("duel") || string2.equals("spectate")) && stringArray.length == 1) {
                for (Player player : this.plugin.getServer().getOnlinePlayers()) {
                    if (!this.startsWith(player.getName(), stringArray[0])) continue;
                    arrayList.add(player.getName());
                }
            } else if (string2.equals("meowduels")) {
                if (stringArray.length == 1) {
                    for (Arena arena : this.plugin.getArenaManager().all()) {
                        if (!this.startsWith(arena.getName(), stringArray[0])) continue;
                        arrayList.add(arena.getName());
                    }
                } else if (stringArray.length == 2) {
                    for (String string8 : List.of("copy", "paste")) {
                        if (!this.startsWith(string8, stringArray[1])) continue;
                        arrayList.add(string8);
                    }
                }
            }
        }
        return arrayList;
    }

    private void resetConfig(CommandSender commandSender) {
        if (!this.requireAdmin(commandSender)) {
            return;
        }
        File file = this.plugin.getDataFolder();
        File file2 = new File(file, "config.yml");
        if (file2.exists()) {
            file2.delete();
        }
        this.plugin.saveResource("config.yml", true);
        File file3 = new File(file, "arenas.yml");
        if (file3.exists()) {
            file3.delete();
        }
        this.plugin.getArenaManager().load();
        this.plugin.reloadAll();
        commandSender.sendMessage(this.plugin.messages().get("notice.admin.config-reset-defaults-all", "value", String.valueOf(this.plugin.getArenaManager().all().size())));
    }

    private void giveGoldenHead(CommandSender commandSender, String[] stringArray) {
        int n;
        Player player;
        if (!commandSender.hasPermission("uhc.admin.givehead")) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.don-t-have-permission", new String[0]));
            return;
        }
        if (stringArray.length >= 1) {
            player = this.plugin.getServer().getPlayerExact(stringArray[0]);
            if (player == null) {
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.player-not-online", "name", stringArray[0]));
                return;
            }
        } else if (commandSender instanceof Player) {
            player = (Player)commandSender;
        } else {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.console-must-specify-player", new String[0]));
            return;
        }
        int n2 = 1;
        if (stringArray.length >= 2) {
            try {
                n2 = Integer.parseInt(stringArray[1]);
            }
            catch (NumberFormatException numberFormatException) {
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.invalid-amount", "name", stringArray[1]));
                return;
            }
            if (n2 < 1) {
                commandSender.sendMessage(this.plugin.messages().get("notice.admin.amount-must-at-least", new String[0]));
                return;
            }
        }
        String string = this.plugin.getConfig().getString("golden-head.texture", "");
        for (int i = n2; i > 0; i -= n) {
            n = Math.min(64, i);
            player.getInventory().addItem(new ItemStack[]{GoldenHead.create((Plugin)this.plugin, n, string)});
        }
        String string2 = n2 == 1 ? "" : "s";
        player.sendMessage(this.plugin.messages().get("notice.admin.received-golden-head", "amount", String.valueOf(n2), "plural", string2));
        if (commandSender != player) {
            commandSender.sendMessage(this.plugin.messages().get("notice.admin.gave-golden-head", "amount", String.valueOf(n2), "plural", string2, "target", player.getName()));
        }
    }

    private boolean startsWith(String string, String string2) {
        return string.toLowerCase(Locale.ROOT).startsWith(string2.toLowerCase(Locale.ROOT));
    }
}

