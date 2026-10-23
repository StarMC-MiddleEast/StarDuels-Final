package com.meowduels.gui;

import com.meowduels.MeowDuels;
import com.meowduels.gui.KitSelectMenu;
import com.meowduels.gui.Menu;
import com.meowduels.managers.GuiConfigManager;
import com.meowduels.model.Arena;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ArenaMenu
extends Menu {
    private final MeowDuels plugin;
    private final Arena arena;
    private static final String MENU = "arena";
    private static final Material ON_MATERIAL = Material.PINK_STAINED_GLASS_PANE;
    private static final Material OFF_MATERIAL = Material.GRAY_STAINED_GLASS_PANE;
    private static final String TOGGLE_ON = "&d\u1d07\u0274\u1d00\u0299\u029f\u1d07\u1d05 &8(\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d05\u026a\ua731\u1d00\u0299\u029f\u1d07)";
    private static final String TOGGLE_OFF = "&7\u1d05\u026a\ua731\u1d00\u0299\u029f\u1d07\u1d05 &8(\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d07\u0274\u1d00\u0299\u029f\u1d07)";
    private static final String NUMBER_HINT = "&7\u029f\u1d07\ua730\u1d1b-\u1d04\u029f\u026a\u1d04\u1d0b: &a+1 &8| &7\u0280\u026a\u0262\u029c\u1d1b-\u1d04\u029f\u026a\u1d04\u1d0b: &c-1";

    public ArenaMenu(MeowDuels meowDuels, Arena arena) {
        this.plugin = meowDuels;
        this.arena = arena;
    }

    @Override
    public void build() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put(MENU, this.arena.getName());
        this.createRaw(6, guiConfigManager.title(MENU, "&d&l\u1d00\u0280\u1d07\u0274\u1d00: &5{arena}", hashMap));
        ItemStack itemStack = guiConfigManager.pane(MENU);
        for (int i = 0; i < 54; ++i) {
            this.inventory.setItem(i, itemStack);
        }
        if (!this.arena.isConfigured()) {
            this.inventory.setItem(31, guiConfigManager.face(MENU, "setup-first", null, GuiConfigManager.Face.of(Material.MAGENTA_STAINED_GLASS_PANE, "&d&l\ua731\u1d07\u1d1b\u1d1c\u1d18", "&7\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \ua731\u1d1b\u1d00\u0280\u1d1b \u1d00\u0280\u1d07\u0274\u1d00 \ua731\u1d07\u1d1b\u1d1c\u1d18.", "&7\u028f\u1d0f\u1d1c'\u029f\u029f \u0299\u1d07 \u1d00\ua731\u1d0b\u1d07\u1d05 \u1d1b\u1d0f \u1d21\u1d00\u029f\u1d0b \u1d1b\u1d0f \u1d07\u1d00\u1d04\u029c", "&7\ua731\u1d18\u1d00\u1d21\u0274 \u1d00\u0274\u1d05 \u1d04\u1d0f\u0280\u0274\u1d07\u0280 \u1d00\u0274\u1d05 \u1d1b\u028f\u1d18\u1d07 &e\u1d05\u1d0f\u0274\u1d07&7.")).build());
            this.inventory.setItem(44, this.saturationToggle(guiConfigManager));
            return;
        }
        Map<String, String> map = this.tokens();
        this.inventory.setItem(10, guiConfigManager.toggle(MENU, "auto-regenerate", this.arena.isAutoRegenerate(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u1d00\u1d1c\u1d1b\u1d0f-\u0280\u1d07\u0262\u1d07\u0274\u1d07\u0280\u1d00\u1d1b\u1d07", "&7\u0280\u1d07\ua731\u1d1b\u1d0f\u0280\u1d07 \u1d1b\u029c\u1d07 \u1d00\u0280\u1d07\u0274\u1d00 \u1d00\ua730\u1d1b\u1d07\u0280 \u1d07\u1d00\u1d04\u029c \ua730\u026a\u0262\u029c\u1d1b.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u1d00\u1d1c\u1d1b\u1d0f-\u0280\u1d07\u0262\u1d07\u0274\u1d07\u0280\u1d00\u1d1b\u1d07", "&7\u0280\u1d07\ua731\u1d1b\u1d0f\u0280\u1d07 \u1d1b\u029c\u1d07 \u1d00\u0280\u1d07\u0274\u1d00 \u1d00\ua730\u1d1b\u1d07\u0280 \u1d07\u1d00\u1d04\u029c \ua730\u026a\u0262\u029c\u1d1b.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(13, guiConfigManager.toggle(MENU, "enabled", this.arena.isEnabled(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u1d07\u0274\u1d00\u0299\u029f\u1d07\u1d05 (\u026a\u0274 \u0280\u1d0f\u1d1b\u1d00\u1d1b\u026a\u1d0f\u0274)", "&7\u1d1b\u1d1c\u0280\u0274 \u1d0f\ua730\ua730 \u1d1b\u1d0f \u029c\u026a\u1d05\u1d07 \ua730\u0280\u1d0f\u1d0d \u1d0d\u1d00\u1d1b\u1d04\u029c\u1d0d\u1d00\u1d0b\u026a\u0274\u0262", "&7\u1d21\u026a\u1d1b\u029c\u1d0f\u1d1c\u1d1b \u1d05\u1d07\u029f\u1d07\u1d1b\u026a\u0274\u0262 \u1d1b\u029c\u1d07 \u1d00\u0280\u1d07\u0274\u1d00.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u1d07\u0274\u1d00\u0299\u029f\u1d07\u1d05 (\u026a\u0274 \u0280\u1d0f\u1d1b\u1d00\u1d1b\u026a\u1d0f\u0274)", "&7\u1d1b\u1d1c\u0280\u0274 \u1d0f\ua730\ua730 \u1d1b\u1d0f \u029c\u026a\u1d05\u1d07 \ua730\u0280\u1d0f\u1d0d \u1d0d\u1d00\u1d1b\u1d04\u029c\u1d0d\u1d00\u1d0b\u026a\u0274\u0262", "&7\u1d21\u026a\u1d1b\u029c\u1d0f\u1d1c\u1d1b \u1d05\u1d07\u029f\u1d07\u1d1b\u026a\u0274\u0262 \u1d1b\u029c\u1d07 \u1d00\u0280\u1d07\u0274\u1d00.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(18, guiConfigManager.toggle(MENU, "allow-building", this.arena.isAllowBuild(), null, GuiConfigManager.Face.of(Material.GREEN_STAINED_GLASS_PANE, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u0299\u1d1c\u026a\u029f\u1d05\u026a\u0274\u0262", "&7\u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731 \u1d04\u1d00\u0274 \u1d18\u029f\u1d00\u1d04\u1d07 \u0299\u029f\u1d0f\u1d04\u1d0b\ua731", "&7\u1d00\u0274\u028f\u1d21\u029c\u1d07\u0280\u1d07 \u026a\u0274\ua731\u026a\u1d05\u1d07 \u1d1b\u029c\u1d07 \u1d00\u0280\u1d07\u0274\u1d00.", "", TOGGLE_ON), GuiConfigManager.Face.of(Material.RED_STAINED_GLASS_PANE, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u0299\u1d1c\u026a\u029f\u1d05\u026a\u0274\u0262", "&7\u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731 \u1d04\u1d00\u0274 \u1d18\u029f\u1d00\u1d04\u1d07 \u0299\u029f\u1d0f\u1d04\u1d0b\ua731", "&7\u1d00\u0274\u028f\u1d21\u029c\u1d07\u0280\u1d07 \u026a\u0274\ua731\u026a\u1d05\u1d07 \u1d1b\u029c\u1d07 \u1d00\u0280\u1d07\u0274\u1d00.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(19, guiConfigManager.toggle(MENU, "allow-breaking", this.arena.isAllowBreak(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u0299\u0280\u1d07\u1d00\u1d0b\u026a\u0274\u0262", "&7\u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731 \u1d04\u1d00\u0274 \u0299\u0280\u1d07\u1d00\u1d0b \u0299\u029f\u1d0f\u1d04\u1d0b\ua731", "&7\u1d00\u0274\u028f\u1d21\u029c\u1d07\u0280\u1d07 \u026a\u0274\ua731\u026a\u1d05\u1d07 \u1d1b\u029c\u1d07 \u1d00\u0280\u1d07\u0274\u1d00.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u0299\u0280\u1d07\u1d00\u1d0b\u026a\u0274\u0262", "&7\u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731 \u1d04\u1d00\u0274 \u0299\u0280\u1d07\u1d00\u1d0b \u0299\u029f\u1d0f\u1d04\u1d0b\ua731", "&7\u1d00\u0274\u028f\u1d21\u029c\u1d07\u0280\u1d07 \u026a\u0274\ua731\u026a\u1d05\u1d07 \u1d1b\u029c\u1d07 \u1d00\u0280\u1d07\u0274\u1d00.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(20, guiConfigManager.toggle(MENU, "break-self-placed", this.arena.isAllowRemoveAdded(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u0299\u0280\u1d07\u1d00\u1d0b \ua731\u1d07\u029f\ua730-\u1d18\u029f\u1d00\u1d04\u1d07\u1d05 \u1d0f\u0274\u029f\u028f", "&7\u1d07\u1d20\u1d07\u0274 \u1d21\u026a\u1d1b\u029c \u0299\u0280\u1d07\u1d00\u1d0b\u026a\u0274\u0262 \u1d0f\ua730\ua730, \u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731", "&7\u1d04\u1d00\u0274 \ua731\u1d1b\u026a\u029f\u029f \u0299\u0280\u1d07\u1d00\u1d0b \u0299\u029f\u1d0f\u1d04\u1d0b\ua731 \u1d1b\u029c\u1d07\u028f", "&7\u1d18\u029f\u1d00\u1d04\u1d07\u1d05 \u1d1b\u029c\u026a\ua731 \u1d0d\u1d00\u1d1b\u1d04\u029c.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u0299\u0280\u1d07\u1d00\u1d0b \ua731\u1d07\u029f\ua730-\u1d18\u029f\u1d00\u1d04\u1d07\u1d05 \u1d0f\u0274\u029f\u028f", "&7\u1d07\u1d20\u1d07\u0274 \u1d21\u026a\u1d1b\u029c \u0299\u0280\u1d07\u1d00\u1d0b\u026a\u0274\u0262 \u1d0f\ua730\ua730, \u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731", "&7\u1d04\u1d00\u0274 \ua731\u1d1b\u026a\u029f\u029f \u0299\u0280\u1d07\u1d00\u1d0b \u0299\u029f\u1d0f\u1d04\u1d0b\ua731 \u1d1b\u029c\u1d07\u028f", "&7\u1d18\u029f\u1d00\u1d04\u1d07\u1d05 \u1d1b\u029c\u026a\ua731 \u1d0d\u1d00\u1d1b\u1d04\u029c.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(26, guiConfigManager.toggle(MENU, "cloning", this.arena.isCloningEnabled(), hashMap, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u1d04\u029f\u1d0f\u0274\u026a\u0274\u0262 \u1d07\u0274\u1d00\u0299\u029f\u1d07\u1d05", "&7\u1d00\u029f\u029f\u1d0f\u1d21 &e/\u1d00\u0280\u1d07\u0274\u1d00 {arena} \u1d04\u1d0f\u1d18\u028f&7 \u1d00\u0274\u1d05", "&e/\u1d00\u0280\u1d07\u0274\u1d00 {arena} \u1d18\u1d00\ua731\u1d1b\u1d07&7.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u1d04\u029f\u1d0f\u0274\u026a\u0274\u0262 \u1d07\u0274\u1d00\u0299\u029f\u1d07\u1d05", "&7\u1d00\u029f\u029f\u1d0f\u1d21 &e/\u1d00\u0280\u1d07\u0274\u1d00 {arena} \u1d04\u1d0f\u1d18\u028f&7 \u1d00\u0274\u1d05", "&e/\u1d00\u0280\u1d07\u0274\u1d00 {arena} \u1d18\u1d00\ua731\u1d1b\u1d07&7.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(30, guiConfigManager.toggle(MENU, "allow-spectators", this.arena.isAllowSpectators(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \ua731\u1d18\u1d07\u1d04\u1d1b\u1d00\u1d1b\u1d0f\u0280\ua731", "&7\u1d04\u1d00\u0274 \u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731 /\ua731\u1d18\u1d07\u1d04\u1d1b\u1d00\u1d1b\u1d07 \u1d1b\u029c\u026a\ua731 \ua730\u026a\u0262\u029c\u1d1b?", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \ua731\u1d18\u1d07\u1d04\u1d1b\u1d00\u1d1b\u1d0f\u0280\ua731", "&7\u1d04\u1d00\u0274 \u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731 /\ua731\u1d18\u1d07\u1d04\u1d1b\u1d00\u1d1b\u1d07 \u1d1b\u029c\u026a\ua731 \ua730\u026a\u0262\u029c\u1d1b?", "", TOGGLE_OFF)).build());
        this.inventory.setItem(36, guiConfigManager.toggle(MENU, "allow-explosions", this.arena.isAllowExplosions(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u1d07x\u1d18\u029f\u1d0f\ua731\u026a\u1d0f\u0274\ua731", "&7\u1d1b\u0274\u1d1b / \u1d04\u0280\u1d07\u1d07\u1d18\u1d07\u0280 \u1d07x\u1d18\u029f\u1d0f\ua731\u026a\u1d0f\u0274\ua731.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u1d07x\u1d18\u029f\u1d0f\ua731\u026a\u1d0f\u0274\ua731", "&7\u1d1b\u0274\u1d1b / \u1d04\u0280\u1d07\u1d07\u1d18\u1d07\u0280 \u1d07x\u1d18\u029f\u1d0f\ua731\u026a\u1d0f\u0274\ua731.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(37, guiConfigManager.toggle(MENU, "allow-fire-spread", this.arena.isAllowFireSpread(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \ua730\u026a\u0280\u1d07 \ua731\u1d18\u0280\u1d07\u1d00\u1d05", "&7\ua730\u026a\u0280\u1d07 \ua731\u1d18\u0280\u1d07\u1d00\u1d05\u026a\u0274\u0262 / \u0299\u1d1c\u0280\u0274\u026a\u0274\u0262 \u0299\u029f\u1d0f\u1d04\u1d0b\ua731.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \ua730\u026a\u0280\u1d07 \ua731\u1d18\u0280\u1d07\u1d00\u1d05", "&7\ua730\u026a\u0280\u1d07 \ua731\u1d18\u0280\u1d07\u1d00\u1d05\u026a\u0274\u0262 / \u0299\u1d1c\u0280\u0274\u026a\u0274\u0262 \u0299\u029f\u1d0f\u1d04\u1d0b\ua731.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(38, guiConfigManager.toggle(MENU, "allow-liquid-flow", this.arena.isAllowLiquidFlow(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u029f\u026a\ua7af\u1d1c\u026a\u1d05 \ua730\u029f\u1d0f\u1d21", "&7\u1d21\u1d00\u1d1b\u1d07\u0280 / \u029f\u1d00\u1d20\u1d00 \ua730\u029f\u1d0f\u1d21\u026a\u0274\u0262.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u029f\u026a\ua7af\u1d1c\u026a\u1d05 \ua730\u029f\u1d0f\u1d21", "&7\u1d21\u1d00\u1d1b\u1d07\u0280 / \u029f\u1d00\u1d20\u1d00 \ua730\u029f\u1d0f\u1d21\u026a\u0274\u0262.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(39, guiConfigManager.toggle(MENU, "allow-mob-spawns", this.arena.isAllowMobSpawns(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u1d0d\u1d0f\u0299 \ua731\u1d18\u1d00\u1d21\u0274\ua731", "&7\u0274\u1d00\u1d1b\u1d1c\u0280\u1d00\u029f \u1d0d\u1d0f\u0299 \ua731\u1d18\u1d00\u1d21\u0274\u026a\u0274\u0262.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u1d0d\u1d0f\u0299 \ua731\u1d18\u1d00\u1d21\u0274\ua731", "&7\u0274\u1d00\u1d1b\u1d1c\u0280\u1d00\u029f \u1d0d\u1d0f\u0299 \ua731\u1d18\u1d00\u1d21\u0274\u026a\u0274\u0262.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(40, guiConfigManager.toggle(MENU, "allow-fall-damage", this.arena.isAllowFallDamage(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \ua730\u1d00\u029f\u029f \u1d05\u1d00\u1d0d\u1d00\u0262\u1d07", "&7\ua730\u1d00\u029f\u029f \u1d05\u1d00\u1d0d\u1d00\u0262\u1d07 \u1d21\u029c\u026a\u029f\u1d07 \ua730\u026a\u0262\u029c\u1d1b\u026a\u0274\u0262.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \ua730\u1d00\u029f\u029f \u1d05\u1d00\u1d0d\u1d00\u0262\u1d07", "&7\ua730\u1d00\u029f\u029f \u1d05\u1d00\u1d0d\u1d00\u0262\u1d07 \u1d21\u029c\u026a\u029f\u1d07 \ua730\u026a\u0262\u029c\u1d1b\u026a\u0274\u0262.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(41, guiConfigManager.toggle(MENU, "allow-hunger-loss", this.arena.isAllowHungerLoss(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u029c\u1d1c\u0274\u0262\u1d07\u0280 \u029f\u1d0f\ua731\ua731", "&7\u029c\u1d1c\u0274\u0262\u1d07\u0280 \u1d05\u0280\u1d00\u026a\u0274\ua731 \u1d05\u1d1c\u0280\u026a\u0274\u0262 \u1d00 \ua730\u026a\u0262\u029c\u1d1b.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u1d00\u029f\u029f\u1d0f\u1d21 \u029c\u1d1c\u0274\u0262\u1d07\u0280 \u029f\u1d0f\ua731\ua731", "&7\u029c\u1d1c\u0274\u0262\u1d07\u0280 \u1d05\u0280\u1d00\u026a\u0274\ua731 \u1d05\u1d1c\u0280\u026a\u0274\u0262 \u1d00 \ua730\u026a\u0262\u029c\u1d1b.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(42, guiConfigManager.toggle(MENU, "lock-clear-weather", this.arena.isLockClearWeather(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u029f\u1d0f\u1d04\u1d0b \u1d04\u029f\u1d07\u1d00\u0280 \u1d21\u1d07\u1d00\u1d1b\u029c\u1d07\u0280", "&7\u1d0b\u1d07\u1d07\u1d18 \u1d1b\u029c\u1d07 \ua731\u1d0b\u028f \u1d04\u029f\u1d07\u1d00\u0280 \u1d05\u1d1c\u0280\u026a\u0274\u0262 \ua730\u026a\u0262\u029c\u1d1b\ua731.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u029f\u1d0f\u1d04\u1d0b \u1d04\u029f\u1d07\u1d00\u0280 \u1d21\u1d07\u1d00\u1d1b\u029c\u1d07\u0280", "&7\u1d0b\u1d07\u1d07\u1d18 \u1d1b\u029c\u1d07 \ua731\u1d0b\u028f \u1d04\u029f\u1d07\u1d00\u0280 \u1d05\u1d1c\u0280\u026a\u0274\u0262 \ua730\u026a\u0262\u029c\u1d1b\ua731.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(43, guiConfigManager.toggle(MENU, "lock-day-time", this.arena.isLockDayTime(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u029f\u1d0f\u1d04\u1d0b \u1d05\u1d00\u028f \u1d1b\u026a\u1d0d\u1d07", "&7\u1d0b\u1d07\u1d07\u1d18 \u026a\u1d1b \u1d05\u1d00\u028f\u1d1b\u026a\u1d0d\u1d07 \u1d05\u1d1c\u0280\u026a\u0274\u0262 \ua730\u026a\u0262\u029c\u1d1b\ua731.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u029f\u1d0f\u1d04\u1d0b \u1d05\u1d00\u028f \u1d1b\u026a\u1d0d\u1d07", "&7\u1d0b\u1d07\u1d07\u1d18 \u026a\u1d1b \u1d05\u1d00\u028f\u1d1b\u026a\u1d0d\u1d07 \u1d05\u1d1c\u0280\u026a\u0274\u0262 \ua730\u026a\u0262\u029c\u1d1b\ua731.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(44, this.saturationToggle(guiConfigManager));
        this.inventory.setItem(46, guiConfigManager.toggle(MENU, "border-particles", this.arena.isBorderParticles(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u0299\u1d0f\u0280\u1d05\u1d07\u0280 \u1d18\u1d00\u0280\u1d1b\u026a\u1d04\u029f\u1d07\ua731", "&7\u1d04\u1d0f\ua731\u1d0d\u1d07\u1d1b\u026a\u1d04 \u1d18\u1d00\u0280\u1d1b\u026a\u1d04\u029f\u1d07 \u1d0f\u1d1c\u1d1b\u029f\u026a\u0274\u1d07 (\u1d04\u1d0f\u1d0d\u026a\u0274\u0262 \ua731\u1d0f\u1d0f\u0274).", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u0299\u1d0f\u0280\u1d05\u1d07\u0280 \u1d18\u1d00\u0280\u1d1b\u026a\u1d04\u029f\u1d07\ua731", "&7\u1d04\u1d0f\ua731\u1d0d\u1d07\u1d1b\u026a\u1d04 \u1d18\u1d00\u0280\u1d1b\u026a\u1d04\u029f\u1d07 \u1d0f\u1d1c\u1d1b\u029f\u026a\u0274\u1d07 (\u1d04\u1d0f\u1d0d\u026a\u0274\u0262 \ua731\u1d0f\u1d0f\u0274).", "", TOGGLE_OFF)).build());
        this.inventory.setItem(47, guiConfigManager.toggle(MENU, "glowing-opponent", this.arena.isGlowingOpponent(), null, GuiConfigManager.Face.of(ON_MATERIAL, "&d\u0262\u029f\u1d0f\u1d21\u026a\u0274\u0262 \u1d0f\u1d18\u1d18\u1d0f\u0274\u1d07\u0274\u1d1b", "&7\u0299\u1d0f\u1d1b\u029c \ua730\u026a\u0262\u029c\u1d1b\u1d07\u0280\ua731 \u0262\u029f\u1d0f\u1d21 \u1d05\u1d1c\u0280\u026a\u0274\u0262 \u1d1b\u029c\u1d07 \u1d0d\u1d00\u1d1b\u1d04\u029c.", "", TOGGLE_ON), GuiConfigManager.Face.of(OFF_MATERIAL, "&d\u0262\u029f\u1d0f\u1d21\u026a\u0274\u0262 \u1d0f\u1d18\u1d18\u1d0f\u0274\u1d07\u0274\u1d1b", "&7\u0299\u1d0f\u1d1b\u029c \ua730\u026a\u0262\u029c\u1d1b\u1d07\u0280\ua731 \u0262\u029f\u1d0f\u1d21 \u1d05\u1d1c\u0280\u026a\u0274\u0262 \u1d1b\u029c\u1d07 \u1d0d\u1d00\u1d1b\u1d04\u029c.", "", TOGGLE_OFF)).build());
        this.inventory.setItem(14, guiConfigManager.face(MENU, "regen-delay", this.tokens(this.arena.getRegenDelayTicks()), GuiConfigManager.Face.of(Material.CLOCK, "&d\u0280\u1d07\u0262\u1d07\u0274 \u1d05\u1d07\u029f\u1d00\u028f", "&7\u1d05\u1d07\u029f\u1d00\u028f \u0299\u1d07\ua730\u1d0f\u0280\u1d07 \u0280\u1d07\u0262\u1d07\u0274\u1d07\u0280\u1d00\u1d1b\u026a\u0274\u0262.", "", "&f\u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b: &d{value} \u1d1b\u026a\u1d04\u1d0b\ua731", NUMBER_HINT)).build());
        this.inventory.setItem(15, guiConfigManager.face(MENU, "countdown-override", this.tokens(this.arena.getCountdownOverride()), GuiConfigManager.Face.of(Material.CLOCK, "&d\u1d04\u1d0f\u1d1c\u0274\u1d1b\u1d05\u1d0f\u1d21\u0274 \u1d0f\u1d20\u1d07\u0280\u0280\u026a\u1d05\u1d07", "&7\u1d04\u1d1c\ua731\u1d1b\u1d0f\u1d0d \ua730\u026a\u0262\u029c\u1d1b \u1d04\u1d0f\u1d1c\u0274\u1d1b\u1d05\u1d0f\u1d21\u0274 \u029f\u1d07\u0274\u0262\u1d1b\u029c.", "", "&f\u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b: &d{value} \ua731 (-1 = \u1d05\u1d07\ua730\u1d00\u1d1c\u029f\u1d1b)", NUMBER_HINT)).build());
        this.inventory.setItem(23, guiConfigManager.face(MENU, "event-border-end", this.tokens(this.arena.getEventBorderEnd()), GuiConfigManager.Face.of(Material.CLOCK, "&e\u1d07\u1d20\u1d07\u0274\u1d1b \u0299\u1d0f\u0280\u1d05\u1d07\u0280 \u1d07\u0274\u1d05", "&7\u1d1b\u029c\u1d07 \u0299\u1d0f\u0280\u1d05\u1d07\u0280 \ua731\u1d1b\u1d0f\u1d18\ua731 \ua731\u029c\u0280\u026a\u0274\u1d0b\u026a\u0274\u0262 \u029c\u1d07\u0280\u1d07.", "", "&f\u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b: &d{value} \u0299\u029f\u1d0f\u1d04\u1d0b\ua731", NUMBER_HINT)).build());
        this.inventory.setItem(24, guiConfigManager.face(MENU, "event-shrink-interval", this.tokens(this.arena.getEventBorderInterval()), GuiConfigManager.Face.of(Material.CLOCK, "&e\u1d07\u1d20\u1d07\u0274\u1d1b \ua731\u029c\u0280\u026a\u0274\u1d0b \u026a\u0274\u1d1b\u1d07\u0280\u1d20\u1d00\u029f", "&7\u1d1b\u026a\u1d0d\u1d07 \u0299\u1d07\u1d1b\u1d21\u1d07\u1d07\u0274 \u1d07\u1d00\u1d04\u029c \ua731\u029c\u0280\u026a\u0274\u1d0b \ua731\u1d1b\u1d07\u1d18.", "", "&f\u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b: &d{value} \ua731\u1d07\u1d04\u1d0f\u0274\u1d05\ua731", NUMBER_HINT)).build());
        this.inventory.setItem(25, guiConfigManager.face(MENU, "event-shrink-step", this.tokens(this.arena.getEventBorderStep()), GuiConfigManager.Face.of(Material.CLOCK, "&e\u1d07\u1d20\u1d07\u0274\u1d1b \ua731\u029c\u0280\u026a\u0274\u1d0b \ua731\u1d1b\u1d07\u1d18", "&7\u029c\u1d0f\u1d21 \u1d0d\u1d1c\u1d04\u029c \ua731\u1d0d\u1d00\u029f\u029f\u1d07\u0280 \u1d07\u1d00\u1d04\u029c \ua731\u029c\u0280\u026a\u0274\u1d0b.", "", "&f\u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b: &d{value} \u0299\u029f\u1d0f\u1d04\u1d0b\ua731", NUMBER_HINT)).build());
        this.inventory.setItem(9, guiConfigManager.face(MENU, "setup", null, GuiConfigManager.Face.of(Material.MAGENTA_STAINED_GLASS_PANE, "&d&l\ua731\u1d07\u1d1b\u1d1c\u1d18", "&7\u0280\u1d07-\u0280\u1d1c\u0274 \u1d1b\u029c\u1d07 \u1d18\u1d0f\ua731\u026a\u1d1b\u026a\u1d0f\u0274 \ua731\u1d07\u1d1b\u1d1c\u1d18 \u1d21\u026a\u1d22\u1d00\u0280\u1d05.")).build());
        this.inventory.setItem(11, guiConfigManager.face(MENU, "regen-now", null, GuiConfigManager.Face.of(Material.NETHER_STAR, "&d&l\u0280\u1d07\u0262\u1d07\u0274 \u0274\u1d0f\u1d21", "&7\ua730\u1d0f\u0280\u1d04\u1d07-\u0280\u1d07\ua731\u1d1b\u1d0f\u0280\u1d07 \u1d1b\u029c\u1d07 \u1d00\u0280\u1d07\u0274\u1d00 \u1d1b\u1d0f \u026a\u1d1b\ua731", "&7\ua731\u1d00\u1d20\u1d07\u1d05 \ua731\u0274\u1d00\u1d18\ua731\u029c\u1d0f\u1d1b \u0280\u026a\u0262\u029c\u1d1b \u0274\u1d0f\u1d21.")).build());
        this.inventory.setItem(12, guiConfigManager.face(MENU, "snapshot-now", null, GuiConfigManager.Face.of(Material.ENDER_EYE, "&d&l\ua731\u0274\u1d00\u1d18\ua731\u029c\u1d0f\u1d1b \u0274\u1d0f\u1d21", "&7\ua731\u1d00\u1d20\u1d07 \u1d1b\u029c\u1d07 \u1d00\u0280\u1d07\u0274\u1d00'\ua731 \u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b \u0299\u029f\u1d0f\u1d04\u1d0b\ua731", "&7\u1d00\ua731 \u1d1b\u029c\u1d07 \u0274\u1d07\u1d21 \u0280\u1d07\u0262\u1d07\u0274 \u0299\u1d00\ua731\u1d07\u029f\u026a\u0274\u1d07.")).build());
        this.inventory.setItem(16, guiConfigManager.face(MENU, "kits", map, GuiConfigManager.Face.of(Material.CHEST, "&e&l\u1d04\u1d0f\u1d0d\u1d18\u1d00\u1d1b\u026a\u0299\u029f\u1d07 \u1d0b\u026a\u1d1b\ua731", "&7\u1d04\u029c\u1d0f\u1d0f\ua731\u1d07 \u1d21\u029c\u026a\u1d04\u029c \u1d0b\u026a\u1d1b\ua731 \u1d04\u1d00\u0274 \u0299\u1d07 \u1d1c\ua731\u1d07\u1d05 \u029c\u1d07\u0280\u1d07.", "&7\u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b\u029f\u028f: &f{kits} \u1d0b\u026a\u1d1b(\ua731)", "&8(\u0274\u1d0f\u0274\u1d07 = \u1d00\u029f\u029f \u1d0b\u026a\u1d1b\ua731 \u1d00\u029f\u029f\u1d0f\u1d21\u1d07\u1d05)")).build());
        this.inventory.setItem(17, guiConfigManager.face(MENU, "info", map, GuiConfigManager.Face.of(Material.BOOK, "&7\u1d00\u0280\u1d07\u0274\u1d00 \u026a\u0274\ua730\u1d0f", "&7\u1d21\u1d0f\u0280\u029f\u1d05: &f{world}", "&7\u0280\u1d07\u0262\u026a\u1d0f\u0274: &f{region}", "&8\u1d04\u029c\u1d00\u0274\u0262\u1d07\ua731 \ua731\u1d00\u1d20\u1d07 \u1d00\u1d1c\u1d1b\u1d0f\u1d0d\u1d00\u1d1b\u026a\u1d04\u1d00\u029f\u029f\u028f.")).build());
        Map<String, String> map2 = this.tokens();
        map2.put("state", this.arena.getEventSpawn() != null ? guiConfigManager.line(MENU, "event-spawn", "state-set", "&a\ua731\u1d07\u1d1b", null) : guiConfigManager.line(MENU, "event-spawn", "state-unset", "&c\u0274\u1d0f\u1d1b \ua731\u1d07\u1d1b", null));
        this.inventory.setItem(21, guiConfigManager.face(MENU, "event-spawn", map2, GuiConfigManager.Face.of(Material.BEACON, "&e&l\ua730\ua730\u1d00 / \u1d07\u1d20\u1d07\u0274\u1d1b \ua731\u1d18\u1d00\u1d21\u0274", "&7\u1d21\u029c\u1d07\u0280\u1d07 \ua730\ua730\u1d00 \u1d07\u1d20\u1d07\u0274\u1d1b \u1d00\u0274\u1d05 \u1d18\u1d00\u0280\u1d1b\u028f \u1d0d\u1d00\u1d1b\u1d04\u029c", "&7\u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731 \u1d00\u0280\u1d07 \u1d1b\u1d07\u029f\u1d07\u1d18\u1d0f\u0280\u1d1b\u1d07\u1d05 \u026a\u0274.", "&7\u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b: {state}", "", "&c\u0280\u1d07\ua7af\u1d1c\u026a\u0280\u1d07\u1d05&7 - \u1d00\u0274 \u1d00\u0280\u1d07\u0274\u1d00 \u1d21\u026a\u1d1b\u029c\u1d0f\u1d1c\u1d1b \u1d1b\u029c\u026a\ua731", "&7\u1d04\u1d00\u0274'\u1d1b \u029c\u1d0f\ua731\u1d1b \ua730\ua730\u1d00 \u1d0f\u0280 \u1d18\u1d00\u0280\u1d1b\u028f \u1d0d\u1d00\u1d1b\u1d04\u029c\u1d07\ua731.", "", "&e\u1d04\u029f\u026a\u1d04\u1d0b&7, \u1d21\u1d00\u029f\u1d0b \u1d1b\u1d0f \u1d1b\u029c\u1d07 \ua731\u1d18\u1d0f\u1d1b, \u1d1b\u028f\u1d18\u1d07 &e\u1d05\u1d0f\u0274\u1d07&7.")).build());
        Map<String, String> map3 = this.tokens();
        Map<String, String> map4 = this.tokens();
        map4.put("blocks", String.valueOf((int)this.arena.getBorderStartSize()));
        map3.put("state", this.arena.hasBorderRegion() ? guiConfigManager.line(MENU, "border-area", "state-set", "&a\ua731\u1d07\u1d1b &7({blocks} \u0299\u029f\u1d0f\u1d04\u1d0b\ua731)", map4) : guiConfigManager.line(MENU, "border-area", "state-unset", "&c\u0274\u1d0f\u1d1b \ua731\u1d07\u1d1b &7(\u1d00\u1d1c\u1d1b\u1d0f-\ua730\u026a\u1d1b)", null));
        this.inventory.setItem(22, guiConfigManager.face(MENU, "border-area", map3, GuiConfigManager.Face.of(Material.FILLED_MAP, "&e&l\u1d07\u1d20\u1d07\u0274\u1d1b \u0299\u1d0f\u0280\u1d05\u1d07\u0280 \u1d00\u0280\u1d07\u1d00", "&7\u1d1b\u029c\u1d07 \ua731\u1d1b\u1d00\u0280\u1d1b\u026a\u0274\u0262 \u0299\u1d0f\u0280\u1d05\u1d07\u0280 \u0299\u1d0fx.", "&7\u1d04\u1d1c\u0280\u0280\u1d07\u0274\u1d1b: {state}", "", "&e\u1d04\u029f\u026a\u1d04\u1d0b&7, \u1d21\u1d00\u029f\u1d0b \u1d1b\u1d0f &f\u1d04\u1d0f\u0280\u0274\u1d07\u0280 1&7 \u1d1b\u028f\u1d18\u1d07 &e\u1d05\u1d0f\u0274\u1d07&7,", "&7\u1d1b\u029c\u1d07\u0274 &f\u1d04\u1d0f\u0280\u0274\u1d07\u0280 2&7 \u1d1b\u028f\u1d18\u1d07 &e\u1d05\u1d0f\u0274\u1d07&7.")).build());
        this.inventory.setItem(45, guiConfigManager.face(MENU, "void-rescue", null, GuiConfigManager.Face.of(Material.BLUE_STAINED_GLASS_PANE, "&b\u1d20\u1d0f\u026a\u1d05 \u0280\u1d07\ua731\u1d04\u1d1c\u1d07", "&7\ua730\u1d00\u029f\u029f\u026a\u0274\u0262 \u026a\u0274\u1d1b\u1d0f \u1d1b\u029c\u1d07 \u1d20\u1d0f\u026a\u1d05 \u1d1b\u1d07\u029f\u1d07\u1d18\u1d0f\u0280\u1d1b\ua731", "&7\u1d18\u029f\u1d00\u028f\u1d07\u0280\ua731 \u0299\u1d00\u1d04\u1d0b \u1d1b\u1d0f \u1d1b\u029c\u1d07\u026a\u0280 \ua731\u1d18\u1d00\u1d21\u0274.", "&8\u1d00\u029f\u1d21\u1d00\u028f\ua731 \u1d0f\u0274.")).build());
        this.inventory.setItem(52, guiConfigManager.face(MENU, "description", map, GuiConfigManager.Face.of(Material.NAME_TAG, "&7\u1d05\u1d07\ua731\u1d04\u0280\u026a\u1d18\u1d1b\u026a\u1d0f\u0274", "&7{description}")).build());
        Map<String, String> map5 = this.tokens();
        map5.put("state", this.arena.getSpawn1() != null ? guiConfigManager.line(MENU, "tp-spawn1", "state-set", "&7\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d1b\u1d07\u029f\u1d07\u1d18\u1d0f\u0280\u1d1b \u1d1b\u029c\u1d07\u0280\u1d07.", null) : guiConfigManager.line(MENU, "tp-spawn1", "state-unset", "&8\u0274\u1d0f\u1d1b \ua731\u1d07\u1d1b.", null));
        this.inventory.setItem(48, guiConfigManager.face(MENU, "tp-spawn1", map5, GuiConfigManager.Face.of(Material.ENDER_PEARL, "&b&l\u1d1b\u1d18 \u2192 \u1d18\u029f\u1d00\u028f\u1d07\u0280 1 \ua731\u1d18\u1d00\u1d21\u0274", "{state}")).build());
        Map<String, String> map6 = this.tokens();
        map6.put("state", this.arena.getSpawn2() != null ? guiConfigManager.line(MENU, "tp-spawn2", "state-set", "&7\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d1b\u1d07\u029f\u1d07\u1d18\u1d0f\u0280\u1d1b \u1d1b\u029c\u1d07\u0280\u1d07.", null) : guiConfigManager.line(MENU, "tp-spawn2", "state-unset", "&8\u0274\u1d0f\u1d1b \ua731\u1d07\u1d1b.", null));
        this.inventory.setItem(49, guiConfigManager.face(MENU, "tp-spawn2", map6, GuiConfigManager.Face.of(Material.ENDER_PEARL, "&b&l\u1d1b\u1d18 \u2192 \u1d18\u029f\u1d00\u028f\u1d07\u0280 2 \ua731\u1d18\u1d00\u1d21\u0274", "{state}")).build());
        Map<String, String> map7 = this.tokens();
        map7.put("state", this.arena.getEventSpawn() != null ? guiConfigManager.line(MENU, "tp-event", "state-set", "&7\u1d04\u029f\u026a\u1d04\u1d0b \u1d1b\u1d0f \u1d1b\u1d07\u029f\u1d07\u1d18\u1d0f\u0280\u1d1b \u1d1b\u029c\u1d07\u0280\u1d07.", null) : guiConfigManager.line(MENU, "tp-event", "state-unset", "&8\u0274\u1d0f\u1d1b \ua731\u1d07\u1d1b.", null));
        this.inventory.setItem(50, guiConfigManager.face(MENU, "tp-event", map7, GuiConfigManager.Face.of(Material.ENDER_EYE, "&b&l\u1d1b\u1d18 \u2192 \u1d07\u1d20\u1d07\u0274\u1d1b \ua731\u1d18\u1d00\u1d21\u0274", "{state}")).build());
    }

    private Map<String, String> tokens() {
        GuiConfigManager guiConfigManager = this.plugin.gui();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put(MENU, this.arena.getName());
        hashMap.put("kits", String.valueOf(this.arena.getKits().size()));
        hashMap.put("world", this.arena.getWorldName());
        hashMap.put("region", this.describeRegion());
        hashMap.put("description", this.arena.getDescription().isEmpty() ? guiConfigManager.line(MENU, "description", "none-set", "&8(\u0274\u1d0f\u0274\u1d07 \ua731\u1d07\u1d1b)", null) : this.arena.getDescription());
        return hashMap;
    }

    private ItemStack saturationToggle(GuiConfigManager guiConfigManager) {
        return guiConfigManager.toggle(MENU, "saturation-on-eat", this.arena.isSaturationOnEat(), null, GuiConfigManager.Face.of(Material.GREEN_STAINED_GLASS_PANE, "&a&lSaturation refill enabled", "&7Matches start at zero saturation.", "&7Eating food refills saturation to full.", "", "&aEnabled &8(click to disable)"), GuiConfigManager.Face.of(Material.RED_STAINED_GLASS_PANE, "&c&lSaturation refill disabled", "&7Matches start at zero saturation.", "&7Eating uses normal food saturation.", "", "&cDisabled &8(click to enable)")).build();
    }

    private Map<String, String> tokens(int n) {
        Map<String, String> map = this.tokens();
        map.put("value", String.valueOf(n));
        return map;
    }

    private String describeRegion() {
        if (this.arena.getMin() == null) {
            return "not set";
        }
        return this.arena.getMin().getBlockX() + "," + this.arena.getMin().getBlockY() + "," + this.arena.getMin().getBlockZ() + " \u2192 " + this.arena.getMax().getBlockX() + "," + this.arena.getMax().getBlockY() + "," + this.arena.getMax().getBlockZ();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent inventoryClickEvent) {
        int n = inventoryClickEvent.getRawSlot();
        if (n < 0 || n >= 54) {
            return;
        }
        if (!this.arena.isConfigured()) {
            if (n == 31) {
                player.closeInventory();
                this.plugin.getSetupManager().begin(player, this.arena);
            } else if (n == 44) {
                this.arena.setSaturationOnEat(!this.arena.isSaturationOnEat());
                this.plugin.getArenaManager().save();
                this.build();
                player.openInventory(this.inventory);
            }
            return;
        }
        boolean bl = inventoryClickEvent.isRightClick();
        switch (n) {
            case 9: {
                player.closeInventory();
                this.plugin.getSetupManager().begin(player, this.arena);
                return;
            }
            case 10: {
                this.arena.setAutoRegenerate(!this.arena.isAutoRegenerate());
                break;
            }
            case 11: {
                int n2 = this.plugin.getArenaManager().regenArena(this.arena);
                if (n2 < 0) {
                    player.sendMessage(this.plugin.messages().get("notice.arena.no-snapshot-saved-yet", new String[0]));
                } else {
                    player.sendMessage(this.plugin.messages().get("notice.arena.regenerated-block-s", "written", String.valueOf(n2)));
                }
                return;
            }
            case 12: {
                this.plugin.getArenaManager().snapshotArena(this.arena);
                player.sendMessage(this.plugin.messages().get("notice.arena.snapshot-saved", MENU, this.arena.getName()));
                return;
            }
            case 13: {
                this.arena.setEnabled(!this.arena.isEnabled());
                break;
            }
            case 14: {
                this.arena.setRegenDelayTicks(this.arena.getRegenDelayTicks() + (bl ? -10 : 10));
                break;
            }
            case 15: {
                this.arena.setCountdownOverride(ArenaMenu.clampCountdown(this.arena.getCountdownOverride() + (bl ? -1 : 1)));
                break;
            }
            case 16: {
                new KitSelectMenu(this.plugin, this.arena).open(player);
                return;
            }
            case 18: {
                this.arena.setAllowBuild(!this.arena.isAllowBuild());
                break;
            }
            case 19: {
                this.arena.setAllowBreak(!this.arena.isAllowBreak());
                break;
            }
            case 20: {
                this.arena.setAllowRemoveAdded(!this.arena.isAllowRemoveAdded());
                break;
            }
            case 21: {
                player.closeInventory();
                this.plugin.getSetupManager().beginEventSpawn(player, this.arena);
                return;
            }
            case 22: {
                player.closeInventory();
                this.plugin.getSetupManager().beginBorder(player, this.arena);
                return;
            }
            case 23: {
                this.arena.setEventBorderEnd(this.arena.getEventBorderEnd() + (bl ? -5 : 5));
                break;
            }
            case 24: {
                this.arena.setEventBorderInterval(this.arena.getEventBorderInterval() + (bl ? -10 : 10));
                break;
            }
            case 25: {
                this.arena.setEventBorderStep(this.arena.getEventBorderStep() + (bl ? -5 : 5));
                break;
            }
            case 26: {
                this.arena.setCloningEnabled(!this.arena.isCloningEnabled());
                break;
            }
            case 30: {
                this.arena.setAllowSpectators(!this.arena.isAllowSpectators());
                break;
            }
            case 36: {
                this.arena.setAllowExplosions(!this.arena.isAllowExplosions());
                break;
            }
            case 37: {
                this.arena.setAllowFireSpread(!this.arena.isAllowFireSpread());
                break;
            }
            case 38: {
                this.arena.setAllowLiquidFlow(!this.arena.isAllowLiquidFlow());
                break;
            }
            case 39: {
                this.arena.setAllowMobSpawns(!this.arena.isAllowMobSpawns());
                break;
            }
            case 40: {
                this.arena.setAllowFallDamage(!this.arena.isAllowFallDamage());
                break;
            }
            case 41: {
                this.arena.setAllowHungerLoss(!this.arena.isAllowHungerLoss());
                break;
            }
            case 42: {
                this.arena.setLockClearWeather(!this.arena.isLockClearWeather());
                break;
            }
            case 43: {
                this.arena.setLockDayTime(!this.arena.isLockDayTime());
                break;
            }
            case 44: {
                this.arena.setSaturationOnEat(!this.arena.isSaturationOnEat());
                break;
            }
            case 46: {
                this.arena.setBorderParticles(!this.arena.isBorderParticles());
                break;
            }
            case 47: {
                this.arena.setGlowingOpponent(!this.arena.isGlowingOpponent());
                break;
            }
            case 48: {
                this.teleportTo(player, this.arena.getSpawn1(), "Player 1 spawn");
                return;
            }
            case 49: {
                this.teleportTo(player, this.arena.getSpawn2(), "Player 2 spawn");
                return;
            }
            case 50: {
                this.teleportTo(player, this.arena.getEventSpawn(), "event spawn");
                return;
            }
            default: {
                return;
            }
        }
        this.plugin.getArenaManager().save();
        this.build();
        player.openInventory(this.inventory);
    }

    private void teleportTo(Player player, Location location, String string) {
        if (location == null) {
            player.sendMessage(this.plugin.messages().get("notice.arena.isn-t-set-yet", "label", string));
            return;
        }
        player.closeInventory();
        player.teleport(location);
        player.sendMessage(this.plugin.messages().get("notice.arena.teleported", "label", string, MENU, this.arena.getName()));
    }

    private static int clampCountdown(int n) {
        if (n < -1) {
            return -1;
        }
        return n;
    }
}

