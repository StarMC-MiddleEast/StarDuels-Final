package com.meowduels.managers;

import com.meowduels.MeowDuels;
import com.meowduels.util.Colors;
import com.meowduels.util.MiniMessageUtil;
import com.meowduels.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessageManager {
    private static final Map<String, String> DEFAULTS = new LinkedHashMap<String, String>();
    private static final String COUNTDOWN_TITLE = "<gradient:#FFD65C:#FFB02E>{seconds}</gradient>";
    private static final String COUNTDOWN_SUBTITLE = "<#6B7079>\u0262\u1d07\u1d1b \u0280\u1d07\u1d00\u1d05\u028f</#6B7079>";
    private static final String PREFIX = "<dark_gray>[<yellow>ⓘ</yellow>]</dark_gray> <gray>";
    private static final String FIGHT_TITLE = "<bold><gradient:#FFF500:#FFD000>FIGHT</gradient></bold>";
    private static final String OLD_COUNTDOWN_TITLE = "&e{seconds}";
    private static final String OLD_COUNTDOWN_SUBTITLE = "";
    private static final String OLD_FIGHT_TITLE = "&eFIGHT \u2694 !";
    private static final String OLD_PARTY_COUNTDOWN = "&d{seconds}";
    private static final String OLD_PARTY_GO = "&a&lFIGHT!";
    private static final String CHAT_NOTICE = "<dark_gray>\u25b8 <#8E959D>{target} is in a <#E6E8EB>{kind}<#8E959D> - they only see lines that name them. <#E6E8EB>/msg {target}<#8E959D> to talk properly.";
    private static final String OLD_CHAT_NOTICE = "<dark_gray>\u25b8 <#8E959D>{target} is in a <#E6E8EB>{kind}<#8E959D> - your chat can't reach them.";
    private final MeowDuels plugin;
    private final File file;
    private YamlConfiguration config;
    private String prefix;

    public MessageManager(MeowDuels meowDuels) {
        this.plugin = meowDuels;
        this.file = new File(meowDuels.getDataFolder(), "messages.yml");
        this.reload();
    }

    public void reload() {
        if (!this.file.exists()) {
            try {
                this.plugin.saveResource("messages.yml", false);
            } catch (IllegalArgumentException exception) {
                this.writeDefaults();
            }
        }
        this.config = YamlConfiguration.loadConfiguration((File)this.file);
        this.fillMissing();
        this.retireOldTitles();
        this.prefix = PREFIX;
    }

    private void writeDefaults() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        for (Map.Entry<String, String> entry : DEFAULTS.entrySet()) {
            if (MessageManager.isTitleKey(entry.getKey())) continue;
            yamlConfiguration.set(entry.getKey(), (Object)entry.getValue());
        }
        try {
            yamlConfiguration.save(this.file);
        }
        catch (IOException iOException) {
            this.plugin.getLogger().severe("Failed to write messages.yml: " + iOException.getMessage());
        }
    }

    private void fillMissing() {
        if (this.config == null) {
            return;
        }
        boolean bl = false;
        for (Map.Entry<String, String> entry : DEFAULTS.entrySet()) {
            if (MessageManager.isTitleKey(entry.getKey())) continue;
            if (this.config.contains(entry.getKey())) continue;
            this.config.set(entry.getKey(), (Object)entry.getValue());
            bl = true;
        }
        if (!bl) {
            return;
        }
        try {
            this.config.save(this.file);
        }
        catch (IOException iOException) {
            this.plugin.getLogger().warning("Failed to update messages.yml: " + iOException.getMessage());
        }
    }

    private void retireOldTitles() {
        if (this.config == null) {
            return;
        }
        boolean bl = MessageManager.replaceIfUntouched(this.config, "titles.countdown.title", OLD_COUNTDOWN_TITLE, COUNTDOWN_TITLE);
        bl |= MessageManager.replaceIfUntouched(this.config, "titles.countdown.subtitle", OLD_COUNTDOWN_SUBTITLE, COUNTDOWN_SUBTITLE);
        bl |= MessageManager.replaceIfUntouched(this.config, "titles.fight.title", OLD_FIGHT_TITLE, FIGHT_TITLE);
        bl |= MessageManager.replaceIfUntouched(this.config, "titles.victory.title", "&a&lVICTORY", "<bold><#FFFF00>VICTORY</#FFFF00></bold>");
        bl |= MessageManager.replaceIfUntouched(this.config, "titles.victory.subtitle", "&7Score &8\u00bb &a{yourScore} &7- &c{theirScore}", "<white>Score: <bold><#FFFF00>{yourScore}</#FFFF00></bold> <dark_gray>-</dark_gray> <bold><#FF3030>{theirScore}</#FF3030></bold>");
        bl |= MessageManager.replaceIfUntouched(this.config, "titles.defeat.title", "&c&lDEFEAT", "<bold><#FF2020>DEFEAT</#FF2020></bold>");
        bl |= MessageManager.replaceIfUntouched(this.config, "titles.defeat.subtitle", "&7Score &8\u00bb &a{yourScore} &7- &c{theirScore}", "<white>Score: <bold><#FF3030>{yourScore}</#FF3030></bold> <dark_gray>-</dark_gray> <bold><#FF3030>{theirScore}</#FF3030></bold>");
        bl |= MessageManager.replaceIfUntouched(this.config, "party.countdown-title", OLD_PARTY_COUNTDOWN, COUNTDOWN_TITLE);
        bl |= MessageManager.replaceIfUntouched(this.config, "party.countdown-go", OLD_PARTY_GO, FIGHT_TITLE);
        bl |= MessageManager.replaceIfUntouched(this.config, "duel.start.header", "&f&lDuel:", DEFAULTS.get("duel.start.header"));
        bl |= MessageManager.replaceIfUntouched(this.config, "duel.start.header", "&cDuel Info:", DEFAULTS.get("duel.start.header"));
        bl |= MessageManager.replaceIfUntouched(this.config, "duel.start.header", "&c&lDuel Info:", DEFAULTS.get("duel.start.header"));
        bl |= MessageManager.replaceIfUntouched(this.config, "duel.start.opponent", "&a\u2694 &7Opponent: &f{opponent}", "<gray>Opponent <dark_gray>\u00bb</dark_gray> <white>{opponent}");
        bl |= MessageManager.replaceIfUntouched(this.config, "duel.start.kit", "&a\u270e &7Kit: &d{kit}", "<gray>Kit <dark_gray>\u00bb</dark_gray> <white>{kit}");
        bl |= MessageManager.replaceIfUntouched(this.config, "duel.start.rounds", "&a\u2605 &7Rounds: &f{rounds}", "<gray>First to <yellow>{rounds}");
        bl |= MessageManager.replaceIfUntouched(this.config, "duel.start.leave", "{prefix}&7Use &a/leave &7to leave the duel.", DEFAULTS.get("duel.start.leave"));
        bl |= MessageManager.replaceIfUntouched(this.config, "queue.joined-text", "You have been queued to ", DEFAULTS.get("queue.joined-text"));
        bl |= MessageManager.replaceIfUntouched(this.config, "queue.joined-text", "You have joined the queue for ", DEFAULTS.get("queue.joined-text"));
        bl |= MessageManager.replaceIfUntouched(this.config, "queue.joined-text", "<yellow>\u2726 <white>Queued for ", DEFAULTS.get("queue.joined-text"));
        bl |= MessageManager.replaceIfUntouched(this.config, "queue.left-text", "You have left the queue for ", DEFAULTS.get("queue.left-text"));
        bl |= MessageManager.replaceIfUntouched(this.config, "queue.left-text", "<yellow>\u2726 <white>Left queue for ", DEFAULTS.get("queue.left-text"));
        bl |= MessageManager.replaceIfUntouched(this.config, "party.kill-pvp", "<#FF3B57>\u2620 <#FF8A93>{victim} <#6B7079>was killed by <#7CFF6B>{killer} <dark_gray>\u2022 <#E6E8EB>{alive} <#6B7079>left", DEFAULTS.get("party.kill-pvp"));
        bl |= MessageManager.replaceIfUntouched(this.config, "party.kill-generic", "<#FF3B57>\u2620 <#FF8A93>{victim} <#6B7079>died <dark_gray>\u2022 <#E6E8EB>{alive} <#6B7079>left", DEFAULTS.get("party.kill-generic"));
        bl |= MessageManager.replaceIfUntouched(this.config, "party.winner", "<#FFD65C>\u2605 <gradient:#FFE9A3:#FFB02E>{winner}</gradient> <#6B7079>won the party match!", DEFAULTS.get("party.winner"));
        bl |= MessageManager.replaceIfUntouched(this.config, "chat.isolated-notice", OLD_CHAT_NOTICE, CHAT_NOTICE);
        bl |= MessageManager.replaceIfUntouched(this.config, "prefix", "&d\u24d8 ", PREFIX);
        bl |= MessageManager.replaceIfUntouched(this.config, "prefix", "<gradient:#FFF1B3:#E6A700>\u2726</gradient> <yellow>STAR</yellow> <dark_gray>\u00bb</dark_gray> ", PREFIX);
        bl |= MessageManager.replaceIfUntouched(this.config, "notice.admin.copied-arena-clipboard-walk", "&7Copied arena {arena} to your clipboard. Walk to a new spot and run /arena {arenaname} paste.", "&e\u2726 &fCopied arena &e{arena}&f. Its aligned clone will appear to the right when you run &e/arena {arenaname} paste&f.");
        if (!(bl |= MessageManager.replaceIfUntouched(this.config, "notice.admin.pasted-block-s-registered", "&7Pasted {value} block(s) and registered the new arena {value2} - it's ready to use immediately.", "&a\u2714 &fPlaced &e{value}&f blocks and registered &e{value2}&f beside the source arena."))) {
            return;
        }
        try {
            this.config.save(this.file);
            this.plugin.getLogger().info("Updated legacy message defaults. Your custom wording was kept; edit match title text in titles.yml.");
        }
        catch (IOException iOException) {
            this.plugin.getLogger().warning("Failed to update messages.yml: " + iOException.getMessage());
        }
    }

    private static boolean replaceIfUntouched(YamlConfiguration yamlConfiguration, String string, String string2, String string3) {
        String string4 = yamlConfiguration.getString(string);
        if (string4 == null || !string4.equals(string2)) {
            return false;
        }
        yamlConfiguration.set(string, (Object)string3);
        return true;
    }

    public String raw(String string) {
        if (MessageManager.isTitleKey(string) && this.plugin.titles() != null) {
            return this.plugin.titles().raw(string);
        }
        String string2 = this.config == null ? null : this.config.getString(string);
        String string3 = string2;
        if (string2 == null) {
            string2 = DEFAULTS.get(string);
        }
        return string2 == null ? "&cmissing message: " + string : string2;
    }

    public String get(String string, String ... stringArray) {
        String string2 = this.raw(string).replace("{prefix}", this.prefix == null ? PREFIX : this.prefix);
        int n = 0;
        while (n + 1 < stringArray.length) {
            string2 = string2.replace("{" + stringArray[n] + "}", stringArray[n + 1]);
            n += 2;
        }
        if (!MessageManager.isTitleKey(string) && !MessageManager.isResultKey(string)
            && !string2.contains("ⓘ")) {
            string2 = "<dark_gray>[<yellow>ⓘ</yellow>]</dark_gray> <gray>" + string2;
        }
        if (!string.startsWith("titles.")) {
            string2 = MessageManager.yellowWhite(string2);
        }
        if (MessageManager.isTitleKey(string)) {
            return MiniMessageUtil.toMiniMessage(string2);
        }
        if (string.startsWith("request.")) {
            return string2.indexOf(60) >= 0 ? Colors.toSection(string2) : Text.color(string2);
        }
        if (string.startsWith("notice.")) {
            return Colors.toSection(string2);
        }
        if (string.startsWith("party.") || string.startsWith("event.") || string.startsWith("chat.")) {
            return Colors.toSection(string2);
        }
        if (string.startsWith("match.") || string.startsWith("round.") || string.startsWith("death.")) {
            return Text.colorNormal(string2);
        }
        return Colors.toSection(string2);
    }

    static boolean isTitleKey(String key) {
        return key.startsWith("titles.") || key.endsWith("-title") || key.endsWith("-subtitle")
            || key.equals("party.countdown-go") || key.equals("party.countdown-title")
            || key.equals("party.countdown-subtitle-split") || key.equals("party.countdown-subtitle-split-noteam")
            || key.equals("party.win-subtitle-split");
    }

    static Map<String, String> defaultMessages() {
        return DEFAULTS;
    }

    private static boolean isResultKey(String key) {
        return key.startsWith("titles.victory") || key.startsWith("titles.defeat")
            || key.startsWith("duel.victory") || key.startsWith("duel.defeat")
            || key.startsWith("match.result") || key.startsWith("match.over");
    }

    private static String yellowWhite(String string) {
        return string.replace("&7", "&f").replace("&a", "&f").replace("&2", "&f").replace("&c", "&e").replace("&4", "&e").replace("&e", "&e").replace("&d", "&e").replace("<gray>", "<white>").replace("<#8E959D>", "<white>").replace("<yellow>", "<yellow>").replace("<gold>", "<yellow>").replace("<red>", "<yellow>").replace("<dark_red>", "<yellow>").replace("<green>", "<white>").replace("<#FF2E55>", "<yellow>").replace("<#FF7FC4>", "<yellow>").replace("<#FF5FB0>", "<yellow>").replace("<#FF2020>", "<yellow>").replace("<#FF3030>", "<yellow>").replace("<#FF3B57>", "<yellow>").replace("<#FF8A8A>", "<yellow>").replace("<#FF8A93>", "<yellow>").replace("<#C0392B>", "<yellow>").replace("<#8B0000>", "<yellow>").replace("<#7CFF6B>", "<white>").replace("<#1FA32F>", "<white>").replace("<#6B7079>", "<white>").replace("<#E6E8EB>", "<white>");
    }

    static {
        DEFAULTS.put("prefix", PREFIX);
        DEFAULTS.put("general.no-permission", "{prefix}&cYou don't have permission to do that.");
        DEFAULTS.put("general.reloaded", "{prefix}&aConfig reloaded.");
        DEFAULTS.put("general.players-only", "{prefix}&cThis command can only be used by a player.");
        DEFAULTS.put("duel.usage", "{prefix}&cUsage: /duel <player>");
        DEFAULTS.put("duel.cannot-duel-self", "{prefix}&cYou cannot duel yourself.");
        DEFAULTS.put("duel.already-in-duel", "{prefix}&cYou are already in a duel.");
        DEFAULTS.put("duel.target-in-duel", "{prefix}&c{target} is already in a duel.");
        DEFAULTS.put("duel.kit-gone", "{prefix}&cThat kit no longer exists.");
        DEFAULTS.put("duel.arena-booked", "{prefix}&cSorry, the Arena was booked.");
        DEFAULTS.put("duel.arena-world-unavailable", "{prefix}&cArena &e{arena}&c has a missing or unloaded spawn world. An admin must repair its world or spawn points; players were returned to spawn.");
        DEFAULTS.put("duel.requests-off", "{prefix}&c{target} has duel requests turned off.");
        DEFAULTS.put("duel.no-arenas", "{prefix}&cNo free arena is available right now - try again shortly.");
        DEFAULTS.put("duel.no-kits", "{prefix}&cNo kits have been created yet.");
        DEFAULTS.put("duel.target-offline", "{prefix}&cPlayer {name} is not online.");
        DEFAULTS.put("duel.sent", "{prefix}&aChallenge sent to &e{target} &a(&e{kit}&a, first to &e{rounds}&a).");
        DEFAULTS.put("duel.starting", "{prefix}&aDuel starting against &e{opponent}&a!");
        DEFAULTS.put("duel.victory-opponent-left", "{prefix}&aYour opponent left - you win.");
        DEFAULTS.put("duel.victory-opponent-disconnected", "{prefix}&aYour opponent disconnected - you win.");
        DEFAULTS.put("duel.left-confirm", "{prefix}&cYou left the duel.");
        DEFAULTS.put("duel.round", "{prefix}&fRound &d{round} &f- first to {roundsToWin}");
        DEFAULTS.put("duel.start.header", "<yellow><bold>DUEL</bold> <dark_gray>\u00b7</dark_gray> <white>{opponent}</white>");
        DEFAULTS.put("duel.start.info", "<gray>Kit <dark_gray>\u00bb</dark_gray> <white>{kit}</white> <dark_gray>\u00b7</dark_gray> <gray>First to <yellow>{rounds}</yellow>");
        DEFAULTS.put("duel.start.leave", "<dark_gray>Type <yellow>/leave</yellow> to leave.</dark_gray>");
        DEFAULTS.put("leave.not-in-duel", "{prefix}&cYou are not in a duel.");
        DEFAULTS.put("spectate.not-found", "{prefix}&cPlayer {name} is not online.");
        DEFAULTS.put("spectate.in-duel", "{prefix}&cYou can't spectate while in a duel.");
        DEFAULTS.put("spectate.self", "{prefix}&cYou can't spectate yourself.");
        DEFAULTS.put("spectate.target-not-in-fight", "{prefix}&c{target} is not in a fight right now.");
        DEFAULTS.put("spectate.not-allowed", "{prefix}&cSpectating is disabled in this arena.");
        DEFAULTS.put("spectate.now", "{prefix}&aNow spectating &e{target}&a. Type &e/spectate&a to stop.");
        DEFAULTS.put("spectate.stopped", "{prefix}&aStopped spectating.");
        DEFAULTS.put("spectate.not-spectating", "{prefix}&cYou are not spectating anyone.");
        DEFAULTS.put("spectate.fight-ended", "{prefix}&eThe fight ended - returning you back.");
        DEFAULTS.put("spectate.started-watching", "{name} has started spectating you.");
        DEFAULTS.put("spectate.stopped-watching", "{name} has stopped spectating you.");
        DEFAULTS.put("queue.in-duel", "{prefix}&cYou are already in a duel.");
        DEFAULTS.put("queue.kit-gone", "{prefix}&cThat kit no longer exists.");
        DEFAULTS.put("queue.left", "{prefix}&aYou left every queue.");
        DEFAULTS.put("queue.not-queued", "{prefix}&cYou are not in a queue.");
        DEFAULTS.put("queue.no-arena", "{prefix}&cNo free arena is available for that kit right now.");
        DEFAULTS.put("queue.joined-text", "<white>Queued for ");
        DEFAULTS.put("queue.left-text", "<white>Left queue for ");
        DEFAULTS.put("duel.victory", "{prefix}&a&lVICTORY! &fYou won the duel (&e{yourScore} &f- &e{theirScore}&f).");
        DEFAULTS.put("duel.defeat", "{prefix}&c&lDEFEAT. &f{winner} won the duel.");
        DEFAULTS.put("duel.match-result", "<yellow>\u2620 <white>{killer} killed <yellow>{victim}</white>\n\n<yellow><bold>Match Result:</bold></yellow>\n<yellow>\u2694 <white>Winner: <yellow>{winner}</yellow>\n<yellow>\u2692 <white>Kit: <gradient:#FFF1B3:#E6A700>{kit}</gradient>\n<yellow>\u2605 <white>Score: <yellow>{score}");
        DEFAULTS.put("accept.no-requests", "{prefix}&cYou have no pending duel requests.");
        DEFAULTS.put("accept.target-offline", "{prefix}&cThat player is not online.");
        DEFAULTS.put("accept.expired", "{prefix}&cThat duel request has expired.");
        DEFAULTS.put("accept.sender-offline", "{prefix}&cThat player is no longer online.");
        DEFAULTS.put("accept.one-in-duel", "{prefix}&cOne of you is already in a duel.");
        DEFAULTS.put("accept.no-arena", "{prefix}&cNo free arena is available for that kit right now.");
        DEFAULTS.put("request.header", "&6DUEL REQUEST FROM &e&l{sender}");
        DEFAULTS.put("request.kit", "&eKit: &e&l{kit}");
        DEFAULTS.put("request.rounds", "&eRounds: &f{rounds}");
        DEFAULTS.put("request.click", "&6&l[CLICK HERE]");
        DEFAULTS.put("request.click-hover", "&aClick to accept the duel from &f{sender}");
        DEFAULTS.put("menu.select-kit-first", "{prefix}&cSelect a kit first.");
        DEFAULTS.put("menu.target-offline", "{prefix}&c{target} is no longer online.");
        DEFAULTS.put("menu.arena-incompatible", "{prefix}&cThat arena doesn't support the selected kit.");
        DEFAULTS.put("arena.cannot-edit", "{prefix}&cYou cannot edit an arena here.");
        DEFAULTS.put("chat.isolated-notice", CHAT_NOTICE);
        DEFAULTS.put("chat.kind-duel", "duel");
        DEFAULTS.put("chat.kind-party", "party match");
        DEFAULTS.put("titles.match-found.title", "&cMATCH FOUND!");
        DEFAULTS.put("titles.match-found.subtitle", "&7Preparing your arena...");
        DEFAULTS.put("titles.match-found.progress-1", "&c● &8● &8●");
        DEFAULTS.put("titles.match-found.progress-2", "&c● &c● &8●");
        DEFAULTS.put("titles.match-found.progress-3", "&c● &c● &c●");
        DEFAULTS.put("titles.match-found.progress-4", "&8● &c● &c●");
        DEFAULTS.put("titles.countdown.title", COUNTDOWN_TITLE);
        DEFAULTS.put("titles.countdown.subtitle", COUNTDOWN_SUBTITLE);
        DEFAULTS.put("titles.fight.title", FIGHT_TITLE);
        DEFAULTS.put("titles.fight.subtitle", "<white>⚔ <bold><#FFFF00>{yourScore}</#FFFF00></bold> <gray>-</gray> <bold><#FF3030>{theirScore}</#FF3030></bold>");
        DEFAULTS.put("titles.round-won.title", "&a&lVICTORY");
        DEFAULTS.put("titles.round-won.subtitle", "&7Score &8\u00bb &a{yourScore} &7- &c{theirScore}");
        DEFAULTS.put("titles.round-lost.title", "&c&lDEFEAT");
        DEFAULTS.put("titles.round-lost.subtitle", "&7Score &8\u00bb &a{yourScore} &7- &c{theirScore}");
        DEFAULTS.put("round.won-chat", "&6\u2604 &aRound {round} won &8\u00bb &f{score} &7- &f{opponent_score}");
        DEFAULTS.put("round.lost-chat", "&4\u2620 &cRound {round} lost &8\u00bb &f{score} &7- &f{opponent_score}");
        DEFAULTS.put("event.announce", "&a{host} is hosting an event of &e{kit}&a. Do &f/event &ato join. &7Players: &f{slots} &8| &7Time Left: &c{time}");
        DEFAULTS.put("event.started", "&a&lThe Event has started!");
        DEFAULTS.put("event.full", "&cThe event is full ({slots} slots).");
        DEFAULTS.put("event.spectate-start", "&7You are now spectating the event. &f/eventleave &7to stop.");
        DEFAULTS.put("event.spectate-playing", "&cYou're playing in the event - you can't spectate it.");
        DEFAULTS.put("event.already-spectating", "&cYou're already spectating the event.");
        DEFAULTS.put("party.invite-header", "  &d\u2605 &f&lPARTY INVITE");
        DEFAULTS.put("party.invite-from", "  &7{leader} &7wants you in their party.");
        DEFAULTS.put("party.invite-info", "  &8Members: &f{members}");
        DEFAULTS.put("party.invite-accept", "  &a&l[ JOIN ]");
        DEFAULTS.put("party.invite-gap", "   ");
        DEFAULTS.put("party.invite-decline", "&c&l[ DECLINE ]");
        DEFAULTS.put("party.invite-accept-hover", "&aJoin {leader}'s party");
        DEFAULTS.put("party.invite-decline-hover", "&cTurn down {leader}'s invite");
        DEFAULTS.put("party.invite-sent", "&aInvited &f{player}&a - waiting for them to accept.");
        DEFAULTS.put("party.invite-declined", "&7You turned down &f{leader}&7's invite.");
        DEFAULTS.put("party.invite-declined-by", "&7{player} &7turned down your party invite.");
        DEFAULTS.put("party.match-started", "&aParty match started &8\u2022 &f{count} &7fighting on &f{arena}");
        DEFAULTS.put("party.countdown-title", COUNTDOWN_TITLE);
        DEFAULTS.put("party.countdown-subtitle", "&7Free-for-all \u2022 last one standing");
        DEFAULTS.put("party.countdown-go", FIGHT_TITLE);
        DEFAULTS.put("party.countdown-subtitle-split", "<#8E959D>\u1d1b\u1d07\u1d00\u1d0d \u1d20\ua731 \u1d1b\u1d07\u1d00\u1d0d <dark_gray>\u2022 <#8E959D>\u028f\u1d0f\u1d1c'\u0280\u1d07 \u1d0f\u0274 {team}");
        DEFAULTS.put("party.countdown-subtitle-split-noteam", "<#8E959D>\u1d1b\u1d07\u1d00\u1d0d \u1d20\ua731 \u1d1b\u1d07\u1d00\u1d0d <dark_gray>\u2022 <#8E959D>\u1d21\u026a\u1d18\u1d07 \u1d1b\u029c\u1d07 \u1d0f\u1d1b\u029c\u1d07\u0280 \ua731\u026a\u1d05\u1d07");
        DEFAULTS.put("party.kill-pvp", "<yellow>\u2620 <white>{victim} <white>was killed by <yellow>{killer} <dark_gray>\u2022 <white>{alive} left");
        DEFAULTS.put("party.kill-generic", "<yellow>\u2620 <white>{victim} <white>died <dark_gray>\u2022 <white>{alive} left");
        DEFAULTS.put("party.team-aqua", "<#7DE2FF>\u1d1b\u1d07\u1d00\u1d0d \u1d00\ua7af\u1d1c\u1d00");
        DEFAULTS.put("party.team-red", "<#FF8A8A>\u1d1b\u1d07\u1d00\u1d0d \u0280\u1d07\u1d05");
        DEFAULTS.put("party.teams-line", "<#8E959D>\u1d1b\u1d07\u1d00\u1d0d\ua731 <dark_gray>\u00bb <#7DE2FF>{aqua} <dark_gray>\u1d20\ua731 <#FF8A8A>{red}");
        DEFAULTS.put("party.team-winner", "<#FFD65C>\u2605 {team} <#6B7079>won the party match!");
        DEFAULTS.put("party.winner", "<yellow>\u2605 <gradient:#FFF1B3:#E6A700>{winner}</gradient> <white>won the party match!");
        DEFAULTS.put("party.no-winner", "&7The party match ended with no winner.");
        DEFAULTS.put("party.force-ended", "<#FF8A93>\u2716 <#6B7079>{leader} ended the party match.");
        DEFAULTS.put("party.duel-header", "  <#7DE2FF>\u2694 <bold>\u1d18\u1d00\u0280\u1d1b\u028f \u1d05\u1d1c\u1d07\u029f \u1d04\u029c\u1d00\u029f\u029f\u1d07\u0274\u0262\u1d07</bold>");
        DEFAULTS.put("party.duel-from", "  <#6B7079>\ua730\u0280\u1d0f\u1d0d <#E6E8EB>{leader}<#6B7079>'\ua731 \u1d18\u1d00\u0280\u1d1b\u028f <dark_gray>\u2022 <#E6E8EB>{members} <#6B7079>\u1d0d\u1d07\u1d0d\u0299\u1d07\u0280\ua731");
        DEFAULTS.put("party.duel-terms", "  <#6B7079>\u1d0b\u026a\u1d1b <dark_gray>\u00bb <#E6E8EB>{kit} <dark_gray>\u2022 <#6B7079>\ua730\u026a\u0280\ua731\u1d1b \u1d1b\u1d0f <#E6E8EB>{rounds} <dark_gray>\u2022 <#E6E8EB>{pairs} <#6B7079>\u1d20\ua731 \u028f\u1d0f\u1d1c");
        DEFAULTS.put("party.duel-accept", "  &a&l[ ACCEPT ]");
        DEFAULTS.put("party.duel-decline", "&c&l[ DECLINE ]");
        DEFAULTS.put("party.duel-accept-hover", "&aFight {leader}'s party");
        DEFAULTS.put("party.duel-decline-hover", "&cTurn down {leader}'s party duel");
        DEFAULTS.put("party.duel-sent", "<#7DE2FF>\u2694 <#6B7079>\u1d04\u029c\u1d00\u029f\u029f\u1d07\u0274\u0262\u1d07\u1d05 <#E6E8EB>{party}<#6B7079>'\ua731 \u1d18\u1d00\u0280\u1d1b\u028f <dark_gray>\u2022 <#E6E8EB>{kit} <dark_gray>\u2022 <#6B7079>\ua730\u026a\u0280\ua731\u1d1b \u1d1b\u1d0f <#E6E8EB>{rounds}");
        DEFAULTS.put("party.round-won", "<#FFD65C>\u2605 {team} <#6B7079>\u1d1b\u1d00\u1d0b\u1d07\ua731 \u1d1b\u029c\u1d07 \u0280\u1d0f\u1d1c\u0274\u1d05 <dark_gray>\u2022 <#7DE2FF>{aqua} <dark_gray>- <#FF8A8A>{red} <dark_gray>\u2022 <#6B7079>\ua730\u026a\u0280\ua731\u1d1b \u1d1b\u1d0f <#E6E8EB>{rounds}");
        DEFAULTS.put("party.round-win-title", "<gradient:#7CFF6B:#1FA32F>\u0280\u1d0f\u1d1c\u0274\u1d05 \u1d21\u1d0f\u0274</gradient>");
        DEFAULTS.put("party.round-lose-title", "<gradient:#FF8A8A:#C0392B>\u0280\u1d0f\u1d1c\u0274\u1d05 \u029f\u1d0f\ua731\u1d1b</gradient>");
        DEFAULTS.put("party.round-subtitle", "<#7DE2FF>{aqua} <#6B7079>- <#FF8A8A>{red}");
        DEFAULTS.put("party.round-start", "<#8E959D>\u0280\u1d0f\u1d1c\u0274\u1d05 <#E6E8EB>{round}<dark_gray>/<#8E959D>{rounds}");
        DEFAULTS.put("party.win-title", "&6&lVICTORY");
        DEFAULTS.put("party.win-subtitle", "&7Last one standing");
        DEFAULTS.put("party.win-subtitle-split", "<#8E959D>\u028f\u1d0f\u1d1c\u0280 \u1d1b\u1d07\u1d00\u1d0d \u1d21\u026a\u1d18\u1d07\u1d05 \u1d1b\u029c\u1d07\u1d0d \u1d0f\u1d1c\u1d1b");
        DEFAULTS.put("event.kill.pvp", "<#FF3B57>\u2620 <#FF8A93>{victim} <#6B7079>was eliminated by <#7CFF6B>{killer} <dark_gray>\u2022 <#E6E8EB>{alive} <#6B7079>left");
        DEFAULTS.put("event.kill.generic", "<#FF3B57>\u2620 <#FF8A93>{victim} <#6B7079>was eliminated <dark_gray>\u2022 <#E6E8EB>{alive} <#6B7079>left");
        DEFAULTS.put("event.win", "&a&l{winner} has won the event! &4\u2620");
        DEFAULTS.put("event.joined", "&aYou joined the event! Wait for it to start.");
        DEFAULTS.put("event.left", "&7You left the event.");
        DEFAULTS.put("event.no-permission", "&cYou don't have permission to host events.");
        DEFAULTS.put("event.already-running", "&cAn event is already running.");
        DEFAULTS.put("event.no-kit", "&cNo kit named that exists.");
        DEFAULTS.put("event.no-arena", "&cNo free arena is available to host an event right now.");
        DEFAULTS.put("event.no-event-spawn", "&cArena {arena} has no event spawn set - set it in /arena first.");
        DEFAULTS.put("event.arena-busy", "&cArena {arena} is in use right now.");
        DEFAULTS.put("event.none", "&cThere's no event to join right now.");
        DEFAULTS.put("event.already-joined", "&cYou already joined the event.");
        DEFAULTS.put("event.busy", "&cLeave your duel, queue, or spectate first (&f/leave&c) to join the event.");
        DEFAULTS.put("event.started-cannot-join", "&cThe event already started - you can't join now.");
        DEFAULTS.put("event.host-usage", "&cUsage: &f/event host <kit> <minutes> [slots]&c, &f/event host force_start&c or &f/event host force_end");
        DEFAULTS.put("event.force-ended", "&cThe event has been ended by a host.");
        DEFAULTS.put("event.countdown-title", "<gradient:#FFD65C:#FF8A2E>{seconds}</gradient>");
        DEFAULTS.put("event.countdown-subtitle", "<#8E959D>\u1d1b\u029c\u1d07 \u1d07\u1d20\u1d07\u0274\u1d1b \u026a\ua731 \u1d00\u0299\u1d0f\u1d1c\u1d1b \u1d1b\u1d0f \ua731\u1d1b\u1d00\u0280\u1d1b");
        DEFAULTS.put("event.start-title", "<gradient:#7CFF6B:#1FA32F>\ua730\u026a\u0262\u029c\u1d1b</gradient>");
        DEFAULTS.put("event.start-subtitle", "<#8E959D>\u029f\u1d00\ua731\u1d1b \u1d0f\u0274\u1d07 \ua731\u1d1b\u1d00\u0274\u1d05\u026a\u0274\u0262 \u1d21\u026a\u0274\ua731");
        DEFAULTS.put("event.win-title", "<gradient:#FFE9A3:#FFB02E>\u1d20\u026a\u1d04\u1d1b\u1d0f\u0280\u028f</gradient>");
        DEFAULTS.put("event.win-subtitle", "<#8E959D>\u029f\u1d00\ua731\u1d1b \u1d0f\u0274\u1d07 \ua731\u1d1b\u1d00\u0274\u1d05\u026a\u0274\u0262");
        DEFAULTS.put("event.out-title", "<gradient:#FF8A8A:#C0392B>\u1d07\u029f\u026a\u1d0d\u026a\u0274\u1d00\u1d1b\u1d07\u1d05</gradient>");
        DEFAULTS.put("event.out-subtitle", "<#8E959D>\u028f\u1d0f\u1d1c\u0280 \u1d0b\u026a\u029f\u029f\ua731 <dark_gray>\u00bb <#E6E8EB>{kills}");
        DEFAULTS.put("countdown.player-ready", "&7{player} is Ready !");
        DEFAULTS.put("countdown.forced", "&7\u267b Duel Started Forcefully !");
        DEFAULTS.put("duel.forfeit-broadcast", "&7Someone got ragebaited! &4\u2620");
        DEFAULTS.put("match.toxic.1", "&6\u2604 &a{winner} &7obliterated &c{loser} &4\u2620 &8\u2022 &f{winner_score}&7-&f{loser_score}");
        DEFAULTS.put("match.toxic.2", "&4\u2620 &c{loser} &7got sent to spawn by &6\u2604 &a{winner} &8(&f{winner_score}&7-&f{loser_score}&8)");
        DEFAULTS.put("match.toxic.3", "&6\u2604 &a{winner} &7ended &c{loser}&7's whole career &4\u2620 &8\u2022 &f{winner_score}&7-&f{loser_score}");
        DEFAULTS.put("match.toxic.4", "&4\u2620 &c{loser} &7just got ratio'd by &6\u2604 &a{winner} &8[&f{winner_score}&7-&f{loser_score}&8]");
        DEFAULTS.put("match.toxic.5", "&6\u2604 &a{winner} &7made &c{loser} &4\u2620 &7hit the respawn button &8\u2022 &f{winner_score}&7-&f{loser_score}");
        DEFAULTS.put("match.toxic.6", "&4\u2620 &c{loser} &7got absolutely clapped by &6\u2604 &a{winner} &8(&f{winner_score}&7-&f{loser_score}&8)");
        DEFAULTS.put("match.toxic.7", "&6\u2604 &a{winner} &7turned &c{loser} &4\u2620 &7into a corpse &8\u2022 &f{winner_score}&7-&f{loser_score}");
        DEFAULTS.put("match.toxic.8", "&4\u2620 &c{loser} &7should uninstall \u2014 &6\u2604 &a{winner} &7said gg &8[&f{winner_score}&7-&f{loser_score}&8]");
        DEFAULTS.put("match.toxic.9", "&6\u2604 &a{winner} &7dropped &c{loser} &4\u2620 &7like a bad habit &8\u2022 &f{winner_score}&7-&f{loser_score}");
        DEFAULTS.put("match.toxic.10", "&4\u2620 &c{loser} &7got humbled by &6\u2604 &a{winner} &8(&f{winner_score}&7-&f{loser_score}&8)");
        DEFAULTS.put("death.duel.1", "&4\u2620 &c{victim} &7was killed by &a{killer} &8\u2022 &f{killer_score}&7-&f{victim_score}");
        DEFAULTS.put("death.duel.2", "&4\u2620 &c{victim} &7got dropped by &a{killer} &8\u2022 &f{killer_score}&7-&f{victim_score}");
        DEFAULTS.put("death.duel.3", "&a\u2694 &a{killer} &7cut down &c{victim} &8\u2022 &f{killer_score}&7-&f{victim_score}");
        DEFAULTS.put("death.duel.4", "&4\u2620 &c{victim} &7got sent flying by &a{killer} &8\u2022 &f{killer_score}&7-&f{victim_score}");
        DEFAULTS.put("death.duel.5", "&a\u2694 &a{killer} &7clapped &c{victim} &8\u2022 &f{killer_score}&7-&f{victim_score}");
        DEFAULTS.put("death.normal.pvp.1", "&4\u2620 &c{victim} &7was slain by &a{killer}");
        DEFAULTS.put("death.normal.pvp.2", "&4\u2620 &c{victim} &7got dropped by &a{killer}");
        DEFAULTS.put("death.normal.pvp.3", "&a\u2694 &a{killer} &7ended &c{victim}");
        DEFAULTS.put("death.normal.pvp.4", "&4\u2620 &c{victim} &7couldn't handle &a{killer}");
        DEFAULTS.put("death.normal.generic", "&4\u2620 &c{victim} &7died");
        DEFAULTS.put("titles.victory.title", "<bold><#FFFF00>VICTORY</#FFFF00></bold>");
        DEFAULTS.put("titles.victory.subtitle", "<white>Score: <bold><#FFFF00>{yourScore}</#FFFF00></bold> <dark_gray>-</dark_gray> <bold><#FF3030>{theirScore}</#FF3030></bold>");
        DEFAULTS.put("titles.defeat.title", "<bold><#FF2020>DEFEAT</#FF2020></bold>");
        DEFAULTS.put("titles.defeat.subtitle", "<white>Score: <bold><#FF3030>{yourScore}</#FF3030></bold> <dark_gray>-</dark_gray> <bold><#FF3030>{theirScore}</#FF3030></bold>");
        DEFAULTS.put("notice.admin.gave-golden-head", "&7Gave {amount} Golden Head{plural} to {target}.");
        DEFAULTS.put("notice.admin.received-golden-head", "&7You received {amount} Golden Head{plural}.");
        DEFAULTS.put("notice.admin.amount-must-at-least", "&7Amount must be at least 1.");
        DEFAULTS.put("notice.admin.invalid-amount", "&7Invalid amount: {name}");
        DEFAULTS.put("notice.admin.console-must-specify-player", "&7Console must specify a player: /givegoldenhead <player> [amount]");
        DEFAULTS.put("notice.admin.player-not-online", "&7Player {name} is not online.");
        DEFAULTS.put("notice.admin.don-t-have-permission", "&7You don't have permission to do that.");
        DEFAULTS.put("notice.admin.config-reset-defaults-all", "&7Config reset to defaults and all arenas wiped. ({value} arenas remain)");
        DEFAULTS.put("notice.admin.sub-commands-list-create", "&7Sub-commands: list, create, delete, header, addkit, removekit");
        DEFAULTS.put("notice.admin.removed-from", "&7Removed {name} from {id}.");
        DEFAULTS.put("notice.admin.added", "&7Added {name} to {id}.");
        DEFAULTS.put("notice.admin.no-kit-named", "&7No kit named {name}.");
        DEFAULTS.put("notice.admin.no-category-named", "&7No category named {name}.");
        DEFAULTS.put("notice.admin.usage-id-kit", "&7Usage: /{cmd} {sub} <id> <kit>");
        DEFAULTS.put("notice.admin.header-set", "&7Header for {id} set to {header}.");
        DEFAULTS.put("notice.admin.no-category-named-2", "&7No category named {name}.");
        DEFAULTS.put("notice.admin.usage-header-id-text", "&7Usage: /{cmd} header <id> <text>");
        DEFAULTS.put("notice.admin.deleted-category", "&7Deleted category {name}.");
        DEFAULTS.put("notice.admin.usage-delete-id", "&7Usage: /{cmd} delete <id>");
        DEFAULTS.put("notice.admin.category-id-already-exists", "&7A category with that id already exists.");
        DEFAULTS.put("notice.admin.created-category", "&7Created category {name}.");
        DEFAULTS.put("notice.admin.usage-create-id-header", "&7Usage: /{cmd} create <id> [header]");
        DEFAULTS.put("notice.admin.no-categories-yet-create", "&7No categories yet. Create one with /{cmd} create <id>.");
        DEFAULTS.put("notice.admin.no-kit-named-2", "&7No kit named {name}.");
        DEFAULTS.put("notice.admin.aren-t-event-duel", "&7You aren't in an event, duel, queue, or spectating.");
        DEFAULTS.put("notice.admin.re-party-use-leave", "&7You're in a party - use the Leave Party item, or /party leave.");
        DEFAULTS.put("notice.admin.aren-t-party-party", "&7You aren't in a party. /party create to start one.");
        DEFAULTS.put("notice.admin.isn-t-online", "&7{name} isn't online.");
        DEFAULTS.put("notice.admin.usage-party-player", "&7Usage: /party {sub} <player>");
        DEFAULTS.put("notice.admin.isn-t-online-2", "&7{name} isn't online.");
        DEFAULTS.put("notice.admin.usage-party-duel-accept", "&7Usage: /party duel <accept|decline> <leader>");
        DEFAULTS.put("notice.admin.aren-t-duel", "&7You aren't in a duel.");
        DEFAULTS.put("notice.admin.aren-t-event", "&7You aren't in an event.");
        DEFAULTS.put("notice.admin.re-queue-type-leave", "&7You're in a queue - type /leave first.");
        DEFAULTS.put("notice.admin.re-already-spectating-type", "&7You're already spectating - type /leave first.");
        DEFAULTS.put("notice.admin.re-duel-type-leave", "&7You're in a duel - type /leave or /ff first.");
        DEFAULTS.put("notice.admin.slots-must-number-e", "&7Slots must be a number, e.g. /event host {name} 5 8");
        DEFAULTS.put("notice.admin.minutes-must-number-e", "&7Minutes must be a number, e.g. /event host {name} 5 8");
        DEFAULTS.put("notice.admin.re-duel-type-leave-2", "&7You're in a duel - type /leave or /ff first.");
        DEFAULTS.put("notice.admin.re-queue-type-leave-2", "&7You're in a queue - type /leave first.");
        DEFAULTS.put("notice.admin.re-spectating-type-leave", "&7You're spectating - type /leave first.");
        DEFAULTS.put("notice.admin.re-event-type-leave", "&7You're in an event - type /leave first.");
        DEFAULTS.put("notice.admin.rounds-must-number-e", "&7Rounds must be a number, e.g. /duel {target} {kit} 3");
        DEFAULTS.put("notice.admin.scoreboard-server-name-set", "&7Scoreboard server name set to {value}.");
        DEFAULTS.put("notice.admin.usage-meowduelsserver-name", "&7Usage: /meowduelsserver <name...>");
        DEFAULTS.put("notice.admin.current-scoreboard-server-name", "&7Current scoreboard server name: {servername}");
        DEFAULTS.put("notice.admin.no-kit-named-3", "&7No kit named {name}.");
        DEFAULTS.put("notice.admin.armor-trims-aren-t", "&7Armor trims aren't supported on this server version.");
        DEFAULTS.put("notice.admin.don-t-have-permission-2", "&7You don't have permission to do that.");
        DEFAULTS.put("notice.admin.scoreboard-ip-set", "&7Scoreboard IP set to {name}.");
        DEFAULTS.put("notice.admin.scoreboard-ip-set-2", "&7Scoreboard IP set to {name} ({value}).");
        DEFAULTS.put("notice.admin.color-minimessage-e-g", "&7[color] is MiniMessage, e.g. <dark_red> or <#8B0000>");
        DEFAULTS.put("notice.admin.usage-scoreboardip-ip-color", "&7Usage: /scoreboardip <ip> [color]");
        DEFAULTS.put("notice.admin.current-scoreboard-ip", "&7Current scoreboard IP: {scoreboardip}");
        DEFAULTS.put("notice.admin.cleared-saved-layout-slots", "&7Cleared the saved {menuid} layout. The slots in gui/{menuid2}.yml are in charge again.");
        DEFAULTS.put("notice.admin.no-saved-layout-gui", "&7No saved layout for {menuid} - gui/{menuid2}.yml is already in charge.");
        DEFAULTS.put("notice.admin.usage-editgui-duelconfirm-mapselect", "&7Usage: /editgui <duelconfirm|mapselect> [reset]");
        DEFAULTS.put("notice.admin.kit-display-name-set", "&7Kit {kit} display name set.");
        DEFAULTS.put("notice.admin.no-kit-named-4", "&7No kit named {name}.");
        DEFAULTS.put("notice.admin.usage-kitdisplayname-name-display", "&7Usage: /kitdisplayname <name> <display...>");
        DEFAULTS.put("notice.admin.kit-icon-set", "&7Kit {kit} icon set to {value}.");
        DEFAULTS.put("notice.admin.hold-item-want-use", "&7Hold the item you want to use as the icon.");
        DEFAULTS.put("notice.admin.no-kit-named-5", "&7No kit named {name}.");
        DEFAULTS.put("notice.admin.usage-kiticon-name", "&7Usage: /kiticon <name>");
        DEFAULTS.put("notice.admin.kit-deleted", "&7Kit {name} deleted.");
        DEFAULTS.put("notice.admin.no-kit-named-6", "&7No kit named {name}.");
        DEFAULTS.put("notice.admin.usage-deletekit-name", "&7Usage: /deletekit <name>");
        DEFAULTS.put("notice.admin.kit-saved-from-current", "&7Kit {name} saved from your current inventory.");
        DEFAULTS.put("notice.admin.kit-named-already-exists", "&7A kit named {name} already exists.");
        DEFAULTS.put("notice.admin.usage-kitcreate-name", "&7Usage: /kitcreate <name>");
        DEFAULTS.put("notice.admin.arena-deleted", "&7Arena {name} deleted.");
        DEFAULTS.put("notice.admin.no-arena-named", "&7No arena named {name}.");
        DEFAULTS.put("notice.admin.usage-deletearena-name", "&7Usage: /deletearena <name>");
        DEFAULTS.put("notice.admin.duel-spawn-set-current", "&7Duel spawn set to your current location. Players will be teleported here whenever a duel finishes.");
        DEFAULTS.put("notice.admin.teleported", "&7Teleported to the {label} of {arenaname}.");
        DEFAULTS.put("notice.admin.isn-t-set", "&7The {label} of {arenaname} isn't set.");
        DEFAULTS.put("notice.admin.no-configured-arena-named", "&7No configured arena named {arenaname}.");
        DEFAULTS.put("notice.admin.snapshot-saved-now-regen", "&7Snapshot saved for {arenaname}. This is now the regen baseline.");
        DEFAULTS.put("notice.admin.no-configured-arena-named-2", "&7No configured arena named {arenaname}.");
        DEFAULTS.put("notice.admin.regenerated-block-s", "&7Regenerated {written} block(s) in {arenaname}.");
        DEFAULTS.put("notice.admin.no-snapshot-saved-yet", "&7No snapshot saved yet for {arenaname}. Run /arena {arenaname2} snapshot first.");
        DEFAULTS.put("notice.admin.no-configured-arena-named-3", "&7No configured arena named {arenaname}.");
        DEFAULTS.put("notice.admin.pasted-block-s-registered", "&a\u2714 &fPlaced &e{value}&f blocks and registered &e{value2}&f beside the source arena.");
        DEFAULTS.put("notice.admin.clipboard-empty-run-arena", "&7Your clipboard is empty. Run /arena {arenaname} copy first.");
        DEFAULTS.put("notice.admin.clipboard-empty-run-arena-2", "&7Your clipboard is empty. Run /arena {arenaname} copy first.");
        DEFAULTS.put("notice.admin.copied-arena-clipboard-walk", "&e\u2726 &fCopied arena &e{arena}&f. Its aligned clone will appear to the right when you run &e/arena {arenaname} paste&f.");
        DEFAULTS.put("notice.admin.cloning-disabled-arena-enable", "&7Cloning is disabled for this arena. Enable it in /arena {arenaname}.");
        DEFAULTS.put("notice.admin.no-configured-arena-named-4", "&7No configured arena named {arenaname}.");
        DEFAULTS.put("notice.admin.no-arena-named-2", "&7No arena named {name}.");
        DEFAULTS.put("notice.admin.usage-arena-name-tp", "&7Usage: /arena <name> [tp|copy|paste|regen|snapshot]");
        DEFAULTS.put("notice.admin.arena-created-open-arena", "&7Arena {name} created. Open /arena {name2} to set it up.");
        DEFAULTS.put("notice.admin.arena-named-already-exists", "&7An arena named {name} already exists.");
        DEFAULTS.put("notice.admin.usage-createarena-name", "&7Usage: /createarena <name>");
        DEFAULTS.put("notice.arena.teleported", "&7Teleported to the {label} of {arena}.");
        DEFAULTS.put("notice.arena.isn-t-set-yet", "&7The {label} isn't set yet.");
        DEFAULTS.put("notice.arena.snapshot-saved", "&7Snapshot saved for {arena}.");
        DEFAULTS.put("notice.arena.regenerated-block-s", "&7Regenerated {written} block(s).");
        DEFAULTS.put("notice.arena.no-snapshot-saved-yet", "&7No snapshot saved yet - click Snapshot Now first.");
        DEFAULTS.put("notice.kit.kit-now-inventory-everyone", "&7The {kit} kit is now your inventory - for everyone.");
        DEFAULTS.put("notice.kit.shift-click-replace-kit", "&7Shift-click to copy inventory, armor, and offhand into the editor. Press Save there to apply the {kit} kit.");
        DEFAULTS.put("notice.kit.saved-s-items-everyone", "&7Saved {kit}'s items - for everyone.");
        DEFAULTS.put("notice.kit.saved-own-layout", "&7Saved your own {kit} layout.");
        DEFAULTS.put("notice.kit.isn-t-same-set", "&7That isn't the same set of items. You can rearrange the kit, but not change what's in it.");
        DEFAULTS.put("notice.kit.reset-kit-s-saved", "&7Reset to the kit's saved layout.");
        DEFAULTS.put("notice.kit.layout-was-reset-server", "&7Your {kit} layout was reset to the server default.");
        DEFAULTS.put("notice.event.map-use-right-now", "&7That map is in use right now - pick another.");
        DEFAULTS.put("notice.editgui.saved-menu-layout", "&7Saved the {value} menu layout.");
        DEFAULTS.put("notice.editgui.reset-default", "&7Reset {value} to default.");
        DEFAULTS.put("notice.editgui.added-decoration-panes-inventory", "&7Added decoration panes to your inventory.");
        DEFAULTS.put("notice.editgui.editor-closed-without-saving", "&7Editor closed without saving.");
        DEFAULTS.put("notice.editgui.saved-menu-layout-2", "&7Saved the {value} menu layout.");
        DEFAULTS.put("notice.party.party-gone-pick-another", "&7That party is gone - pick another.");
        DEFAULTS.put("notice.party.only-party-leader-start", "&7Only the party leader can start a match.");
        DEFAULTS.put("notice.party.only-party-leader-invite", "&7Only the party leader can invite.");
        DEFAULTS.put("notice.party.kits-outside-category-aren", "&7Kits outside a category aren't offered - add them with /category.");
        DEFAULTS.put("notice.party.no-kits-category-yet", "&7No kits are in a category yet.");
        DEFAULTS.put("notice.party.only-party-leader-pick", "&7Only the party leader can pick the kit.");
        DEFAULTS.put("notice.party.only-party-leader-do", "&7Only the party leader can do that.");
        DEFAULTS.put("notice.party.re-not-party", "&7You're not in a party.");
        DEFAULTS.put("notice.party.only-party-leader-start-2", "&7Only the party leader can start a match.");
        DEFAULTS.put("notice.party.re-not-party-2", "&7You're not in a party.");
        DEFAULTS.put("notice.party.party-isn-t-available", "&7That party isn't available any more.");
        DEFAULTS.put("notice.party.only-party-leader-start-3", "&7Only the party leader can start a match.");
        DEFAULTS.put("notice.party.only-party-leader-change", "&7Only the party leader can change party settings.");
        DEFAULTS.put("notice.party.only-party-leader-start-4", "&7Only the party leader can start a match.");
        DEFAULTS.put("notice.party.only-party-leader-set", "&7Only the party leader can set the teams.");
        DEFAULTS.put("notice.party.re-queue-type-queue", "&7You're in a queue - type /queue leave first.");
        DEFAULTS.put("notice.party.re-spectating-type-leave", "&7You're spectating - type /leave first.");
        DEFAULTS.put("notice.party.re-event-type-leave", "&7You're in an event - type /leave first.");
        DEFAULTS.put("notice.party.re-duel-finish-first", "&7You're in a duel - finish it first.");
        DEFAULTS.put("notice.party.try-again-after", "&7{target} is {what} - try again after.");
        DEFAULTS.put("notice.party.re-party-leave-first", "&7You're in a party - leave it first to do that.");
        DEFAULTS.put("notice.party.re-party-match-type", "&7You're in a party match - type /leave first.");
        DEFAULTS.put("notice.party.left-party-match", "&7You left the party match.");
        DEFAULTS.put("notice.party.no-free-arena-supports", "&7No free arena supports that kit right now.");
        DEFAULTS.put("notice.party.both-parties-need-someone", "&7Both parties need someone online.");
        DEFAULTS.put("notice.party.kit-no-longer-exists", "&7That kit no longer exists.");
        DEFAULTS.put("notice.party.one-parties-already-match", "&7One of the parties is already in a match.");
        DEFAULTS.put("notice.party.their-party-gone", "&7Their party is gone.");
        DEFAULTS.put("notice.party.need-leading-party-accept", "&7You need to be leading a party to accept.");
        DEFAULTS.put("notice.party.challenge-expired", "&7That challenge has expired.");
        DEFAULTS.put("notice.party.turned-down-party-duel", "&7{leader} turned down your party duel.");
        DEFAULTS.put("notice.party.turned-down-s-party", "&7You turned down {from}'s party duel.");
        DEFAULTS.put("notice.party.no-challenge-from", "&7No challenge from {from}.");
        DEFAULTS.put("notice.party.wait-s-before-challenging", "&7Wait {value}s before challenging again.");
        DEFAULTS.put("notice.party.they-already-have-challenge", "&7They already have your challenge - give them a moment.");
        DEFAULTS.put("notice.party.their-leader-offline", "&7Their leader is offline.");
        DEFAULTS.put("notice.party.both-parties-need-someone-2", "&7Both parties need someone online.");
        DEFAULTS.put("notice.party.pick-kit-first", "&7Pick a kit first.");
        DEFAULTS.put("notice.party.party-just-started-match", "&7That party just started a match of their own.");
        DEFAULTS.put("notice.party.party-gone-pick-another-2", "&7That party is gone - pick another.");
        DEFAULTS.put("notice.party.party-already-fighting", "&7Your party is already fighting.");
        DEFAULTS.put("notice.party.only-party-leader-start-5", "&7Only the party leader can start a match.");
        DEFAULTS.put("notice.party.party-isn-t-match", "&7Your party isn't in a match.");
        DEFAULTS.put("notice.party.only-party-leader-end", "&7Only the party leader can end the match.");
        DEFAULTS.put("notice.party.re-not-party-3", "&7You're not in a party.");
        DEFAULTS.put("notice.party.spectating-party-match-leave", "&7Spectating the party match. /leave to stop.");
        DEFAULTS.put("notice.party.re-already-watching-leave", "&7You're already watching. /leave to stop.");
        DEFAULTS.put("notice.party.re-match-t-spectate", "&7You're in the match - you can't spectate it.");
        DEFAULTS.put("notice.party.party-isn-t-fighting", "&7Your party isn't fighting right now.");
        DEFAULTS.put("notice.party.re-not-party-4", "&7You're not in a party.");
        DEFAULTS.put("notice.party.re-out-watching-rest", "&7You're out - watching the rest. /leave to stop.");
        DEFAULTS.put("notice.party.both-teams-need-at", "&7Both teams need at least one online player.");
        DEFAULTS.put("notice.party.message", "&7{value}");
        DEFAULTS.put("notice.party.no-free-arena-supports-2", "&7No free arena supports that kit.");
        DEFAULTS.put("notice.party.not-enough-party-members", "&7Not enough party members are online.");
        DEFAULTS.put("notice.party.pick-kit-first-2", "&7Pick a kit first.");
        DEFAULTS.put("notice.party.need-at-least-2", "&7You need at least 2 players in the party.");
        DEFAULTS.put("notice.party.party-already-fighting-2", "&7Your party is already fighting.");
        DEFAULTS.put("notice.party.only-party-leader-start-6", "&7Only the party leader can start a match.");
        DEFAULTS.put("notice.party.message-2", "&7{reason}");
        DEFAULTS.put("notice.party.left-party", "&7You left the party.");
        DEFAULTS.put("notice.party.re-not-party-5", "&7You're not in a party.");
        DEFAULTS.put("notice.party.party-run-by-they", "&7The party is run by {leader} - they pick the kit and start the match.");
        DEFAULTS.put("notice.party.party-mid-match-try", "&7That party is mid-match - try again when it ends.");
        DEFAULTS.put("notice.party.haven-t-been-invited", "&7You haven't been invited to that party.");
        DEFAULTS.put("notice.party.leave-current-party-first", "&7Leave your current party first.");
        DEFAULTS.put("notice.party.party-no-longer-exists", "&7That party no longer exists.");
        DEFAULTS.put("notice.party.wait-s-before-inviting", "&7Wait {value}s before inviting again.");
        DEFAULTS.put("notice.party.party-invites-turned-off", "&7{target} has party invites turned off.");
        DEFAULTS.put("notice.party.already-invite", "&7{target} already has your invite.");
        DEFAULTS.put("notice.party.already-party", "&7{target} is already in a party.");
        DEFAULTS.put("notice.party.they-re-already-party", "&7They're already in your party.");
        DEFAULTS.put("notice.party.t-invite-during-party", "&7You can't invite during a party match.");
        DEFAULTS.put("notice.party.only-party-leader-invite-2", "&7Only the party leader can invite.");
        DEFAULTS.put("notice.party.re-not-party-6", "&7You're not in a party.");
        DEFAULTS.put("notice.party.party-created-invite-someone", "&7Party created. Invite someone with /party invite <player>.");
        DEFAULTS.put("notice.party.re-already-party", "&7You're already in a party.");
        DEFAULTS.put("notice.party.have-no-invite-from", "&7You have no invite from {leader}.");
        DEFAULTS.put("notice.trim.cleared-trims", "&7Cleared your trims for {value}.");
        DEFAULTS.put("notice.trim.applied-trim-all-armor", "&7Applied the trim to all your armor pieces (only you will wear it).");
        DEFAULTS.put("notice.trim.no-armor-trim-trims", "&7No armor to trim (or trims unsupported).");
        DEFAULTS.put("notice.trim.applied-trim-only-will", "&7Applied {value} {value2} trim (only you will wear it).");
        DEFAULTS.put("notice.trim.could-not-apply-trim", "&7Could not apply the trim (this server may not support armor trims).");
        DEFAULTS.put("notice.trim.kit-no-armor-slot", "&7That kit has no armor in that slot.");
        DEFAULTS.put("notice.guard.use-get-out-first", "&7Use {value} to get out first.");
        DEFAULTS.put("notice.guard.t-use-commands", "&7You can't use commands {what}.");
        DEFAULTS.put("notice.duel.no-potions-until-fight", "&7No potions until the fight starts.");
        DEFAULTS.put("notice.duel.no-potions-until-fight-2", "&7No potions until the fight starts.");
        DEFAULTS.put("notice.duel.t-duel-while-re", "&7You can't duel while you're in the event.");
        DEFAULTS.put("notice.duel.re-party-leave-first", "&7You're in a party - leave it first, or start a party match.");
        DEFAULTS.put("notice.duel.they-re-already-duel", "&7They're already in a duel.");
        DEFAULTS.put("notice.duel.player-no-longer-online", "&7That player is no longer online.");
        DEFAULTS.put("notice.duel.no-recent-opponent-rematch", "&7No recent opponent to rematch.");
        DEFAULTS.put("notice.duel.message", "&7{value}");
        DEFAULTS.put("notice.duel.duel-t-start-someone", "&7That duel can't start - someone is in the event.");
        DEFAULTS.put("notice.duel.wait-s-before-sending", "&7Wait {value}s before sending another challenge.");
        DEFAULTS.put("notice.duel.already-challenge-give-them", "&7{target} already has your challenge - give them a moment.");
        DEFAULTS.put("notice.duel.party-right-now", "&7{target} is in a party right now.");
        DEFAULTS.put("notice.duel.re-party-leave-first-2", "&7You're in a party - leave it first, or start a party match.");
        DEFAULTS.put("notice.duel.event-right-now", "&7{target} is in the event right now.");
        DEFAULTS.put("notice.duel.t-duel-while-re-2", "&7You can't duel while you're in the event.");
        DEFAULTS.put("notice.spawn.re-event-type-leave", "&7You're in an event - type /leave first.");
        DEFAULTS.put("notice.spawn.re-spectating-type-leave", "&7You're spectating - type /leave first.");
        DEFAULTS.put("notice.spawn.re-already-duel", "&7You're already in a duel.");
        DEFAULTS.put("notice.spawn.only-party-leader-do", "&7Only the party leader can do that.");
        DEFAULTS.put("notice.spawn.re-not-party", "&7You're not in a party.");
        DEFAULTS.put("notice.queue.t-queue-while-re", "&7You can't queue while you're in the event.");
        DEFAULTS.put("notice.setup.tip-after-decorating-arena", "&7Tip: after decorating the arena, run /arena {arena} snapshot to update the baseline.");
        DEFAULTS.put("notice.setup.arena-now-set-up", "&7Arena {arena} is now set up! A regen snapshot was captured automatically.");
        DEFAULTS.put("notice.setup.message", "&7{prompt}");
        DEFAULTS.put("notice.setup.corner-2-saved", "&7Corner 2 saved.");
        DEFAULTS.put("notice.setup.corner-1-saved", "&7Corner 1 saved.");
        DEFAULTS.put("notice.setup.player-2-spawn-saved", "&7Player 2 spawn saved.");
        DEFAULTS.put("notice.setup.player-1-spawn-saved", "&7Player 1 spawn saved.");
        DEFAULTS.put("notice.setup.type-done-when-standing", "&7Type done when you are standing in position, or cancel.");
        DEFAULTS.put("notice.setup.setup-cancelled", "&7Setup cancelled.");
        DEFAULTS.put("notice.setup.message-2", "&7{prompt}");
        DEFAULTS.put("notice.setup.type-cancel-at-any", "&7Type cancel at any time to abort.");
        DEFAULTS.put("notice.setup.setup-started-arena", "&7Setup started for arena {arena}.");
        DEFAULTS.put("notice.setup.event-border-area-saved", "&7Event border area saved for {arena}.");
        DEFAULTS.put("notice.setup.now-go-second-border", "&7Now go to the second border corner and type done.");
        DEFAULTS.put("notice.setup.first-border-corner-saved", "&7First border corner saved.");
        DEFAULTS.put("notice.setup.type-done-where-re", "&7Type done where you're standing, or cancel.");
        DEFAULTS.put("notice.setup.border-setup-cancelled", "&7Border setup cancelled.");
        DEFAULTS.put("notice.setup.type-cancel-abort", "&7Type cancel to abort.");
        DEFAULTS.put("notice.setup.go-first-border-corner", "&7Go to the first border corner and type done in chat.");
        DEFAULTS.put("notice.setup.event-spawn-saved", "&7Event spawn saved for {arena}.");
        DEFAULTS.put("notice.setup.type-done-where-re-2", "&7Type done where you're standing, or cancel.");
        DEFAULTS.put("notice.setup.event-spawn-setup-cancelled", "&7Event spawn setup cancelled.");
        DEFAULTS.put("notice.setup.type-cancel-abort-2", "&7Type cancel to abort.");
        DEFAULTS.put("notice.setup.go-event-arena-spawn", "&7Go to the event arena spawn (where event players start) and type done in chat.");
        DEFAULTS.put("notice.spectate.spectators-turned-off", "&7{targetplayer} has spectators turned off.");
        DEFAULTS.put("notice.spectate.t-spectate-while-re", "&7You can't spectate while you're in a party match.");
        DEFAULTS.put("notice.spectate.t-spectate-duel-while", "&7You can't spectate a duel while you're in the event.");
        DEFAULTS.put("notice.tab.tab-updated", "&7Tab {value} updated.");
        DEFAULTS.put("notice.tab.cancelled", "&7Cancelled.");
        DEFAULTS.put("notice.tab.type-cancel-abort", "&7Type cancel to abort.");
        DEFAULTS.put("notice.tab.type-new-chat", "&7Type the new {label} in chat.");
    }
}

