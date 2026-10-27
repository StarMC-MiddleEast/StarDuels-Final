package com.meowduels;

import com.meowduels.commands.LeaderboardCommand;
import com.meowduels.commands.MeowDuelsCommand;
import com.meowduels.gui.QueuePickMenu;
import com.meowduels.gui.Style;
import com.meowduels.hook.MeowDuelsPlaceholders;
import com.meowduels.hook.TabHook;
import com.meowduels.listeners.ArenaProtectionListener;
import com.meowduels.listeners.ChatListener;
import com.meowduels.listeners.CommandGuardListener;
import com.meowduels.listeners.DuelListener;
import com.meowduels.listeners.EventListener;
import com.meowduels.listeners.GoldenHeadListener;
import com.meowduels.listeners.GuiListener;
import com.meowduels.listeners.PvpOverrideListener;
import com.meowduels.dialog.DialogListener;
import com.meowduels.listeners.SetupChatListener;
import com.meowduels.listeners.SpawnItemsListener;
import com.meowduels.managers.ArenaManager;
import com.meowduels.managers.CategoryManager;
import com.meowduels.managers.DuelManager;
import com.meowduels.managers.EventManager;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.managers.GuiLayoutManager;
import com.meowduels.managers.KitLayoutManager;
import com.meowduels.managers.KitManager;
import com.meowduels.managers.MessageManager;
import com.meowduels.managers.PartyManager;
import com.meowduels.managers.PlayerSettingsManager;
import com.meowduels.managers.QueueManager;
import com.meowduels.managers.ScoreboardService;
import com.meowduels.managers.SetupManager;
import com.meowduels.managers.SpectateManager;
import com.meowduels.managers.StatsManager;
import com.meowduels.managers.TabService;
import com.meowduels.managers.TipService;
import com.meowduels.managers.TrimPreferenceManager;
import com.meowduels.managers.TitlesManager;
import com.meowduels.util.Cooldowns;
import com.meowduels.util.Sounds;
import com.meowduels.util.SpawnItems;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class MeowDuels
extends JavaPlugin {
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private DuelManager duelManager;
    private SetupManager setupManager;
    private GuiLayoutManager guiLayoutManager;
    private GuiConfigManager guiConfigManager;
    private ScoreboardService scoreboardService;
    private MessageManager messageManager;
    private TitlesManager titlesManager;
    private CategoryManager categoryManager;
    private CategoryManager queueCategoryManager;
    private QueueManager queueManager;
    private TabService tabService;
    private SpectateManager spectateManager;
    private StatsManager statsManager;
    private EventManager eventManager;
    private TrimPreferenceManager trimPreferenceManager;
    private KitLayoutManager kitLayoutManager;
    private PlayerSettingsManager playerSettingsManager;
    private TipService tipService;
    private TabHook tabHook;
    private PartyManager partyManager;
    private NamespacedKey keyKit;
    private NamespacedKey keyButton;
    private NamespacedKey keyArena;
    private String scoreboardIp = "play.starpvp.fun";
    private String serverName = "server";
    private Location duelSpawn;
    private static final String OLD_BRAND_TITLE = "<bold><red>\u0299\u029f\u1d0f\u1d0f\u1d05\u1d1b\u029c\u026a\u0280\ua731\u1d1b";

    public void onEnable() {
        this.migrateLegacyDataFolder();
        this.saveDefaultConfig();
        this.mergeConfigDefaults();
        this.migrateDuelMarker();
        this.migrateBoardMarker();
        this.migrateTips();
        this.migrateBrandTitle();
        this.migrateYellowTheme();
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }
        this.keyKit = new NamespacedKey((Plugin)this, "kit");
        this.keyButton = new NamespacedKey((Plugin)this, "button");
        this.keyArena = new NamespacedKey((Plugin)this, "arena");
        this.scoreboardIp = this.getConfig().getString("scoreboard-ip", "play.starpvp.fun");
        this.serverName = this.getConfig().getString("server-name", "server");
        this.loadDuelSpawn();
        this.messageManager = new MessageManager(this);
        this.titlesManager = new TitlesManager(this);
        this.arenaManager = new ArenaManager(this);
        this.arenaManager.recoverDirtyArenas();
        this.kitManager = new KitManager(this);
        this.categoryManager = new CategoryManager(this);
        this.queueCategoryManager = new CategoryManager(this, "queuecategories.yml");
        this.guiLayoutManager = new GuiLayoutManager(this);
        this.guiConfigManager = new GuiConfigManager(this);
        this.setupManager = new SetupManager(this);
        this.scoreboardService = new ScoreboardService(this);
        this.duelManager = new DuelManager(this);
        this.queueManager = new QueueManager(this);
        this.partyManager = new PartyManager(this);
        this.tabService = new TabService(this);
        this.spectateManager = new SpectateManager(this);
        this.statsManager = new StatsManager(this);
        this.eventManager = new EventManager(this);
        this.trimPreferenceManager = new TrimPreferenceManager(this);
        this.kitLayoutManager = new KitLayoutManager(this);
        this.playerSettingsManager = new PlayerSettingsManager(this);
        Sounds.mutedWhen(player -> !this.playerSettingsManager.isSounds(player.getUniqueId()));
        Sounds.load(this.getConfig().getConfigurationSection("sounds"));
        Style.load(this.getConfig().getConfigurationSection("theme"));
        com.meowduels.util.KitIcons.load(this.getConfig().getConfigurationSection("kit-names"));
        this.tipService = new TipService(this);
        this.tabHook = new TabHook();
        if (this.tabHook.isAvailable()) {
            this.getLogger().info("Hooked into TAB - ranks hidden and health shown on nametags during duels.");
        } else {
            this.getLogger().warning("TAB API not found - if you use TAB, the in-duel tab/nametag override won't work. Check the TAB version.");
        }
        this.registerCommands();
        this.registerListeners();
        this.hookPlaceholderAPI();
        this.getServer().getScheduler().runTaskTimer((Plugin)this, () -> {
            this.scoreboardService.tick();
            this.spectateManager.tick();
            this.duelManager.tickWorldLocks();
            this.duelManager.tickArenaSafety();
            this.duelManager.tickCounts();
            this.duelManager.tickArenaReservations();
            this.queueManager.tickMatch();
            this.partyManager.tick();
            if (this.eventManager.isRunning() && this.eventManager.getArena() != null) {
                DuelManager.applyWorldLocks(this.eventManager.getArena());
                this.eventManager.tickBorderDamage();
            }
            for (Player player : this.getServer().getOnlinePlayers()) {
                Cooldowns.tick(player);
            }
            this.tipService.tick();
            QueuePickMenu.refreshAll();
        }, 20L, 20L);
        this.getServer().getScheduler().runTaskTimer((Plugin)this, () -> this.tabService.reassertBubbles(), TabService.REASSERT_PERIOD, TabService.REASSERT_PERIOD);
        this.getServer().getScheduler().runTaskTimer((Plugin)this, () -> this.scoreboardService.updateHealthBelowName(), 4L, 4L);
        this.getLogger().info("StarDuels enabled.");
    }

    private void migrateLegacyDataFolder() {
        File file = this.getDataFolder();
        File file2 = new File(file.getParentFile(), "MeowDuels");
        if (!file2.isDirectory() || file2.equals(file)) {
            return;
        }
        try {
            if (!file.exists()) {
                Files.move(file2.toPath(), file.toPath(), new CopyOption[0]);
                this.getLogger().info("Moved existing MeowDuels data to the StarDuels folder.");
                return;
            }
            Path path = file2.toPath();
            Path path2 = file.toPath();
            try (Stream<Path> stream = Files.walk(path, new FileVisitOption[0]);){
                for (Path path3 : (Iterable<Path>)stream::iterator) {
                    Path path4 = path2.resolve(path.relativize(path3));
                    if (Files.isDirectory(path3, new LinkOption[0])) {
                        Files.createDirectories(path4, new FileAttribute[0]);
                        continue;
                    }
                    if (Files.exists(path4, new LinkOption[0])) continue;
                    Files.createDirectories(path4.getParent(), new FileAttribute[0]);
                    Files.copy(path3, path4, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
            this.getLogger().info("Copied missing files from the existing MeowDuels data folder.");
        }
        catch (IOException iOException) {
            this.getLogger().severe("Could not migrate MeowDuels data to StarDuels: " + iOException.getMessage());
        }
    }

    public void onDisable() {
        if (this.eventManager != null) {
            this.eventManager.shutdown();
        }
        if (this.partyManager != null) {
            this.partyManager.shutdown();
        }
        if (this.spectateManager != null) {
            this.spectateManager.shutdown();
        }
        if (this.duelManager != null) {
            this.duelManager.shutdown();
        }
        if (this.arenaManager != null) {
            this.arenaManager.save();
            this.arenaManager.shutdown();
        }
        if (this.kitManager != null) {
            this.kitManager.save();
        }
        if (this.statsManager != null) {
            this.statsManager.save();
        }
    }

    private void registerCommands() {
        MeowDuelsCommand meowDuelsCommand = new MeowDuelsCommand(this);
        PluginCommand pluginCommand = this.getCommand("leaderboard");
        if (pluginCommand != null) {
            pluginCommand.setExecutor((CommandExecutor)new LeaderboardCommand(this));
        } else {
            this.getLogger().warning("Command 'leaderboard' is missing from plugin.yml.");
        }
        for (String string : new String[]{"starduels", "createarena", "arena", "deletearena", "kitcreate", "deletekit", "kiticon", "kitdisplayname", "editgui", "category", "categoryqueue", "scoreboardip", "duel", "leave", "queue", "editkit", "spectate", "meowduelssetspawn", "kiteditor", "party", "givegoldenhead", "resetconfig", "meowduelsserver", "meowduelstrims", "ff", "event", "eventspec", "eventleave", "meowduelsspawnitems"}) {
            PluginCommand pluginCommand2 = this.getCommand(string);
            if (pluginCommand2 != null) {
                pluginCommand2.setExecutor((CommandExecutor)meowDuelsCommand);
                pluginCommand2.setTabCompleter((TabCompleter)meowDuelsCommand);
                continue;
            }
            this.getLogger().warning("Command '" + string + "' is missing from plugin.yml.");
        }
    }

    private void hookPlaceholderAPI() {
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }
        try {
            new MeowDuelsPlaceholders(this).register();
            this.getLogger().info("Hooked into PlaceholderAPI - %meowduels_...% placeholders registered.");
        }
        catch (Throwable throwable) {
            this.getLogger().warning("Could not register PlaceholderAPI expansion: " + String.valueOf(throwable));
        }
    }

    private void mergeConfigDefaults() {
        InputStream inputStream = this.getResource("config.yml");
        if (inputStream == null) {
            return;
        }
        File file = new File(this.getDataFolder(), "config.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration((File)file);
        YamlConfiguration yamlConfiguration2 = YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        boolean bl = false;
        for (String string : yamlConfiguration2.getKeys(true)) {
            Object object = yamlConfiguration2.get(string);
            if (object instanceof ConfigurationSection || yamlConfiguration.contains(string)) continue;
            yamlConfiguration.set(string, object);
            bl = true;
        }
        if (bl) {
            try {
                yamlConfiguration.save(file);
                this.getLogger().info("config.yml updated with new options (existing settings kept).");
            }
            catch (IOException iOException) {
                this.getLogger().warning("Could not merge config.yml defaults: " + iOException.getMessage());
            }
        }
    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents((Listener)new GuiListener(), (Plugin)this);
        this.getServer().getPluginManager().registerEvents(new DialogListener(this), this);
        this.getServer().getPluginManager().registerEvents((Listener)new SetupChatListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new ArenaProtectionListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new DuelListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new EventListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new PvpOverrideListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new CommandGuardListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new ChatListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new GoldenHeadListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new SpawnItemsListener(this), (Plugin)this);
    }

    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }

    public KitManager getKitManager() {
        return this.kitManager;
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public KitLayoutManager getKitLayouts() {
        return this.kitLayoutManager;
    }

    public TrimPreferenceManager getTrimPreferences() {
        return this.trimPreferenceManager;
    }

    public TipService getTipService() {
        return this.tipService;
    }

    public PlayerSettingsManager getPlayerSettings() {
        return this.playerSettingsManager;
    }

    public String getDuelMarker() {
        String string = this.getConfig().getString("duel-marker", "\u2694");
        return string == null ? "" : string;
    }

    private void migrateBoardMarker() {
        boolean bl = MeowDuels.replaceLine(this.getConfig(), "scoreboard.ffa.lines", "<yellow>\u26a1 <white>{player}", "<yellow>\u2691 <white>{player}");
        if (!(bl |= MeowDuels.replaceLine(this.getConfig(), "scoreboard.duel.lines", "{team_color}\u2694 <white>{player}", "{team_color}\u2691 <white>{player}"))) {
            return;
        }
        this.saveConfig();
        this.getLogger().info("Scoreboard player line switched from the old bolt to the flag, to match tab. Edit scoreboard.*.lines in config.yml to change it back.");
    }

    private void migrateBrandTitle() {
        String string = this.getConfig().getString("scoreboard.global.title");
        if (string == null || !string.equals(OLD_BRAND_TITLE)) {
            return;
        }
        this.getConfig().set("scoreboard.global.title", (Object)"<bold>{server_name}");
        this.saveConfig();
        this.getLogger().info("The sidebar title was still the old shipped default, which was another server's name. It now follows server-name in config.yml.");
    }

    private void migrateYellowTheme() {
        List<String> list;
        List<String> list2;
        String string;
        boolean bl = false;
        if ("example.net".equals(this.getConfig().getString("scoreboard-ip"))
                || "play.starmc.fun".equals(this.getConfig().getString("scoreboard-ip"))) {
            this.getConfig().set("scoreboard-ip", (Object)"play.starpvp.fun");
            bl = true;
        }
        if ("<#FF78C1>".equals(this.getConfig().getString("scoreboard-ip-color"))) {
            this.getConfig().set("scoreboard-ip-color", (Object)"<gradient:#FFF1B3:#E6A700>");
            bl = true;
        }
        if ("<#8E959D>".equals(this.getConfig().getString("theme.label"))) {
            this.getConfig().set("theme.label", (Object)"<white>");
            bl = true;
        }
        if ("#FF2E55".equals(this.getConfig().getString("theme.accent-from"))) {
            this.getConfig().set("theme.accent-from", (Object)"#FFF1B3");
            bl = true;
        }
        if ("#FF7FC4".equals(this.getConfig().getString("theme.accent-to"))) {
            this.getConfig().set("theme.accent-to", (Object)"#E6A700");
            bl = true;
        }
        bl |= MeowDuels.replaceLine(this.getConfig(), "scoreboard.global.title", "<bold><gradient:#FFF1B3:#E6A700>PRACTICE</gradient></bold>", "<bold><gradient:#FFF1B3:#E6A700>\u1d18\u0280\u1d00\u1d04\u1d1b\u026a\u1d04\u1d07</gradient></bold>");
        bl |= MeowDuels.replaceLine(this.getConfig(), "scoreboard.party-ffa.title", "<bold><gradient:#FFF1B3:#E6A700>\u0493\u0493\u1d00</gradient></bold>", "<bold><gradient:#FFF1B3:#E6A700>\u1d18\u1d00\u0280\u1d1b\u028f</gradient></bold>");
        bl |= MeowDuels.replaceLine(this.getConfig(), "scoreboard.party-ffa.title", "<bold><gradient:#FFD65C:#FF8A2E>\u0493\u0493\u1d00</gradient></bold>", "<bold><gradient:#FFF1B3:#E6A700>\u1d18\u1d00\u0280\u1d1b\u028f</gradient></bold>");
        bl |= MeowDuels.replaceLine(this.getConfig(), "scoreboard.party-ffa.title", "<bold><gradient:#FFF1B3:#E6A700>\u1d18\u1d00\u0280\u1d1b\u028f\u1d00</gradient></bold>", "<bold><gradient:#FFF1B3:#E6A700>\u1d18\u1d00\u0280\u1d1b\u028f</gradient></bold>");
        if ("<bold>{server_name}".equals(this.getConfig().getString("scoreboard.global.title"))) {
            this.getConfig().set("scoreboard.global.title", (Object)"<bold><gradient:#FFF1B3:#E6A700>PRACTICE</gradient></bold>");
            bl = true;
        }
        List<String> list9 = Arrays.asList("<gray><strikethrough>                    </strikethrough>", "<#FF2E55>\u2694 \u1d0b\u026a\u029f\u029f\ua731 <dark_gray>: <red>{kills}", "<#FF2E55>\u2620 \u1d05\u1d07\u1d00\u1d1b\u029c\ua731 <dark_gray>: <red>{deaths}", "<#FF2E55>\u231a \u1d18\u029f\u1d00\u028f\u1d1b\u026a\u1d0d\u1d07 <dark_gray>: <red>{playtime}", "<gray><strikethrough>                    </strikethrough>", "{ip_color}{server_ip}");
        if (list9.equals(this.getConfig().getStringList("scoreboard.global.lines"))) {
            this.getConfig().set("scoreboard.global.lines", Arrays.asList("", "<yellow>\u2694 <white>\u1d0b\u026a\u029f\u029f\ua731 <dark_gray>\u00bb <white>{kills}", "<yellow>\u2620 <white>\u1d05\u1d07\u1d00\u1d1b\u029c\ua731 <dark_gray>\u00bb <white>{deaths}", "<yellow>\u2726 <white>\ua731\u1d1b\u0280\u1d07\u1d00\u1d0b <dark_gray>\u00bb <white>{streak}", "", "<yellow>\u25c6 <white>\u1d0b\u1d05\u0280 <dark_gray>\u00bb <white>{kdr}", "<yellow>\u2605 <white>\u1d21\u026a\u0274\ua731 <dark_gray>\u00bb <white>{wins}", "<yellow>\u2301 <white>\u1d18\u026a\u0274\u0262 <dark_gray>\u00bb <white>{ping}ms", "", "<gradient:#FFF1B3:#E6A700>{server_ip}</gradient>"));
            bl = true;
        }
        if ((string = "<bold><gradient:#FF2D95:#FF8AD0>\u1d05\u1d1c\u1d07\u029f</gradient></bold>").equals(this.getConfig().getString("scoreboard.duel.title"))) {
            this.getConfig().set("scoreboard.duel.title", (Object)"<bold><gradient:#FFF1B3:#E6A700>\u1d05\u1d1c\u1d07\u029f</gradient></bold>");
            bl = true;
        }
        if ((list2 = Arrays.asList("<gray><strikethrough>                    </strikethrough>", "{team_color}\u2691 <white>{player}", "<#FF5FB0>\u25c6 <white>{score} <dark_gray>-</dark_gray> <white>{opponent_score}  <dark_gray>\u00bb <red>{opponent}", "", "<#FF5FB0>\u2726 <gray>\u1d0b\u026a\u1d1b <dark_gray>\u00bb <white>{kit}", "<#FF5FB0>\u2666 <gray>\u0280\u1d0f\u1d1c\u0274\u1d05 <dark_gray>\u00bb <white>{round}<dark_gray>/<gray>{rounds_to_win}", "<#FF5FB0>\u25c8 <gray>\u1d00\u0280\u1d07\u0274\u1d00 <dark_gray>\u00bb <white>{arena}", "<#FF5FB0>\u231a <gray>\u1d1b\u026a\u1d0d\u1d07 <dark_gray>\u00bb <white>{time}", "<gray><strikethrough>                    </strikethrough>", "{ip_color}{server_ip}")).equals(this.getConfig().getStringList("scoreboard.duel.lines"))) {
            this.getConfig().set("scoreboard.duel.lines", Arrays.asList("<yellow>\u2691 <white>{player}", "<yellow>\u2605 <white>{score} <dark_gray>-</dark_gray> <white>{opponent_score} <dark_gray>\u00bb <yellow>{opponent}", "", "<yellow>\u2694 <white>\u1d0b\u026a\u1d1b <dark_gray>\u00bb <yellow>{kit}", "<yellow>\u25c6 <white>\u0280\u1d0f\u1d1c\u0274\u1d05 <dark_gray>\u00bb <yellow>{round}<dark_gray>/<white>{rounds_to_win}", "<yellow>\u25c7 <white>\u1d00\u0280\u1d07\u0274\u1d00 <dark_gray>\u00bb <yellow>{arena}", "<yellow>\u231a <white>\u1d1b\u026a\u1d0d\u1d07 <dark_gray>\u00bb <yellow>{time}", "<gradient:#FFF1B3:#E6A700>{server_ip}</gradient>"));
            bl = true;
        }
        if ((list = Arrays.asList("<yellow>\u2726 <white>{party_leader}", "<yellow>\u263b <white>\u1d0d\u1d07\u1d0d\u0299\u1d07\u0280\ua731 <dark_gray>\u00bb <yellow>{party_size}", "", "<yellow>\u2620 <white>\u029f\u1d0f\ua731\ua731\u1d07\ua731 <dark_gray>\u00bb <yellow>{losses}", "<yellow>\u2605 <white>\u1d21\u026a\u0274\ua731 <dark_gray>\u00bb <yellow>{wins}", "<yellow>\u2691 <white>\ua731\u1d1b\u1d00\u1d1b\u1d07 <dark_gray>\u00bb <yellow>{party_access}", "", "<yellow>\u2691 <white>\u1d00\u029f\u026a\u1d20\u1d07 <dark_gray>\u00bb <yellow>{party_alive}<dark_gray>/<white>{party_size}", "<yellow>\u25c9 <white>\ua731\u1d18\u1d07\u1d04\u1d1b\u1d00\u1d1b\u026a\u0274\u0262 <dark_gray>\u00bb <yellow>{party_spectators}", "", "<yellow>\u2694 <white>\u1d0b\u026a\u029f\u029f\ua731 <dark_gray>\u00bb <yellow>{party_kills}", "<yellow>\u2620 <white>\u1d05\u1d07\u1d00\u1d1b\u029c\ua731 <dark_gray>\u00bb <yellow>{deaths}", "<yellow>\u2726 <white>\ua731\u1d1b\u0280\u1d07\u1d00\u1d0b <dark_gray>\u00bb <yellow>{streak}", "<gradient:#FFF1B3:#E6A700>{server_ip}</gradient>")).equals(this.getConfig().getStringList("scoreboard.party.lines"))) {
            this.getConfig().set("scoreboard.party.lines", Arrays.asList("<yellow>\u2726 <white>{party_leader}", "<yellow>\u263b <white>\u1d0d\u1d07\u1d0d\u0299\u1d07\u0280\ua731 <dark_gray>\u00bb <yellow>{party_size}", "<yellow>\u2620 <white>\u029f\u1d0f\ua731\ua731\u1d07\ua731 <dark_gray>\u00bb <yellow>{losses}", "<yellow>\u2605 <white>\u1d21\u026a\u0274\ua731 <dark_gray>\u00bb <yellow>{wins}", "<yellow>\u2691 <white>\ua731\u1d1b\u1d00\u1d1b\u1d07 <dark_gray>\u00bb <yellow>{party_access}", "<gradient:#FFF1B3:#E6A700>{server_ip}</gradient>"));
            bl = true;
        }
        if (bl) {
            this.saveConfig();
        }
    }

    private void migrateTips() {
        boolean bl = MeowDuels.replaceLine(this.getConfig(), "tips.global", "<dark_gray>\u25b8 <#8E959D>Chat inside a duel is isolated. Type someone's <#E6E8EB>full name<#8E959D> to reach them anyway.", "<dark_gray>\u25b8 <#8E959D>Turn on <#E6E8EB>isolated chat<#8E959D> in <#E6E8EB>/settings<#8E959D> to keep the lobby out of your fights.");
        if (!(bl |= MeowDuels.replaceLine(this.getConfig(), "tips.match", "<dark_gray>\u25b8 <#8E959D>Your chat is isolated - only this match reads it.", "<dark_gray>\u25b8 <#8E959D>With <#E6E8EB>isolated chat<#8E959D> on, only this match reads what you type here."))) {
            return;
        }
        this.saveConfig();
        this.getLogger().info("Reworded the isolated-chat tips: the setting is opt-in, so they no longer tell everyone it is already on. Edit tips.* in config.yml to change them.");
    }

    private static boolean replaceLine(FileConfiguration fileConfiguration, String string, String string2, String string3) {
        List list = fileConfiguration.getStringList(string);
        boolean bl = false;
        for (int i = 0; i < list.size(); ++i) {
            if (!string2.equals(list.get(i))) continue;
            list.set(i, string3);
            bl = true;
        }
        if (bl) {
            fileConfiguration.set(string, (Object)list);
        }
        return bl;
    }

    private void migrateDuelMarker() {
        if (!"\ud83d\udde1".equals(this.getConfig().getString("duel-marker"))) {
            return;
        }
        this.getConfig().set("duel-marker", (Object)"\u2694");
        this.saveConfig();
        this.getLogger().info("duel-marker was the old dagger emoji, which needs a resource pack to render. Switched it to crossed swords; set it back in config.yml if you had a pack for it.");
    }

    public String getPartyMarker() {
        String string = this.getConfig().getString("party-marker", "\u2694");
        return string == null ? "" : string;
    }

    public void giveSpawnItems(Player player) {
        SpawnItems.give(this, player);
    }

    public DuelManager getDuelManager() {
        return this.duelManager;
    }

    public SetupManager getSetupManager() {
        return this.setupManager;
    }

    public GuiLayoutManager getGuiLayoutManager() {
        return this.guiLayoutManager;
    }

    public GuiConfigManager gui() {
        return this.guiConfigManager;
    }

    public ScoreboardService getScoreboardService() {
        return this.scoreboardService;
    }

    public MessageManager messages() {
        return this.messageManager;
    }

    public TitlesManager titles() {
        return this.titlesManager;
    }

    public CategoryManager getCategoryManager() {
        return this.categoryManager;
    }

    public CategoryManager getQueueCategoryManager() {
        return this.queueCategoryManager;
    }

    public QueueManager getQueueManager() {
        return this.queueManager;
    }

    public TabService getTabService() {
        return this.tabService;
    }

    public StatsManager getStatsManager() {
        return this.statsManager;
    }

    public TabHook getTabHook() {
        return this.tabHook;
    }

    public void reloadAll() {
        this.reloadConfig();
        this.mergeConfigDefaults();
        // mergeConfigDefaults can add keys to disk, so refresh Bukkit's in-memory view.
        this.reloadConfig();
        this.migrateYellowTheme();
        // Reload the editable YAML-backed plugin settings as well as config.yml.
        // Player and match data files are deliberately left to their managers;
        // reloading those during a live match could replace in-memory state.
        if (this.kitManager != null) {
            this.kitManager.load();
        }
        if (this.categoryManager != null) {
            this.categoryManager.reload();
        }
        if (this.queueCategoryManager != null) {
            this.queueCategoryManager.reload();
        }
        if (this.guiLayoutManager != null) {
            this.guiLayoutManager.load();
        }
        if (this.kitLayoutManager != null) {
            this.kitLayoutManager.reload();
        }
        this.scoreboardIp = this.getConfig().getString("scoreboard-ip", "play.starpvp.fun");
        this.serverName = this.getConfig().getString("server-name", "server");
        this.loadDuelSpawn();
        if (this.messageManager != null) {
            this.messageManager.reload();
        }
        if (this.titlesManager != null) {
            this.titlesManager.reload();
        }
        if (this.guiConfigManager != null) {
            this.guiConfigManager.reload();
        }
        if (this.scoreboardService != null) {
            this.scoreboardService.reload();
        }
        if (this.tipService != null) {
            this.tipService.reload();
        }
        Sounds.load(this.getConfig().getConfigurationSection("sounds"));
        Style.load(this.getConfig().getConfigurationSection("theme"));
        com.meowduels.util.KitIcons.load(this.getConfig().getConfigurationSection("kit-names"));
    }

    public SpectateManager getSpectateManager() {
        return this.spectateManager;
    }

    public PartyManager getPartyManager() {
        return this.partyManager;
    }

    public NamespacedKey keyKit() {
        return this.keyKit;
    }

    public NamespacedKey keyButton() {
        return this.keyButton;
    }

    public NamespacedKey keyArena() {
        return this.keyArena;
    }

    public String getScoreboardIp() {
        return this.scoreboardIp;
    }

    public void setScoreboardIp(String string) {
        this.scoreboardIp = string;
        this.getConfig().set("scoreboard-ip", (Object)string);
        this.saveConfig();
    }

    public String getServerName() {
        return this.serverName;
    }

    public void setServerName(String string) {
        this.serverName = string;
        this.getConfig().set("server-name", (Object)string);
        this.saveConfig();
    }

    public Location getDuelSpawn() {
        return this.duelSpawn;
    }

    public void setDuelSpawn(Location location) {
        this.duelSpawn = location.clone();
        this.getConfig().set("duel-spawn.world", (Object)location.getWorld().getName());
        this.getConfig().set("duel-spawn.x", (Object)location.getX());
        this.getConfig().set("duel-spawn.y", (Object)location.getY());
        this.getConfig().set("duel-spawn.z", (Object)location.getZ());
        this.getConfig().set("duel-spawn.yaw", (Object)Float.valueOf(location.getYaw()));
        this.getConfig().set("duel-spawn.pitch", (Object)Float.valueOf(location.getPitch()));
        this.saveConfig();
    }

    private void loadDuelSpawn() {
        World world;
        if (!this.getConfig().isSet("duel-spawn.world")) {
            this.duelSpawn = null;
            return;
        }
        String string = this.getConfig().getString("duel-spawn.world");
        World world2 = world = string == null ? null : Bukkit.getWorld((String)string);
        if (world == null) {
            this.duelSpawn = null;
            return;
        }
        double d = this.getConfig().getDouble("duel-spawn.x");
        double d2 = this.getConfig().getDouble("duel-spawn.y");
        double d3 = this.getConfig().getDouble("duel-spawn.z");
        float f = (float)this.getConfig().getDouble("duel-spawn.yaw");
        float f2 = (float)this.getConfig().getDouble("duel-spawn.pitch");
        this.duelSpawn = new Location(world, d, d2, d3, f, f2);
    }
}

