package com.meowduels.util;

import com.meowduels.model.Kit;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Puts a kit's item icon in front of its name.
 *
 * <p>The icon is a sprite text component - the same thing as
 * {"atlas":"minecraft:items","sprite":"item/ender_pearl"} in /tellraw - written
 * as a MiniMessage tag, so it shows anywhere the name is rendered as a
 * component: menu titles and item names, chat, titles, the sidebar. Where a
 * name is turned into a legacy colour-code string instead, Colors.toSection
 * drops the tag (and the space after it), so those places show the plain name
 * rather than a broken one.
 *
 * <p>Per kit, {@code name-icon} in kits.yml picks the icon:
 * <ul>
 *   <li>left out - the kit's menu icon, when that item has a flat sprite</li>
 *   <li>{@code item/tnt_minecart} or just {@code tnt_minecart} - an item sprite</li>
 *   <li>{@code block/stone} - a block texture</li>
 *   <li>{@code head:PlayerName} - that player's head</li>
 *   <li>{@code none} - no icon for this kit</li>
 * </ul>
 * config.yml {@code kit-names.icons} turns them all off, and
 * {@code kit-names.format} decides where the icon sits.
 */
public final class KitIcons {
    private static boolean enabled = true;
    private static String format = "{icon} {name}";
    private static boolean supported = true;

    private KitIcons() {
    }

    /** Reads the kit-names section of config.yml; null means all defaults. */
    public static void load(ConfigurationSection config) {
        enabled = config == null || config.getBoolean("icons", true);
        String f = config == null ? null : config.getString("format", "{icon} {name}");
        format = f == null || !f.contains("{name}") ? "{icon} {name}" : f;
        supported = detectSupport();
    }

    /**
     * Sprites need Adventure 4.25 (Paper 1.21.9+). On anything older MiniMessage
     * would print the tag as text, so icons switch themselves off there.
     */
    private static boolean detectSupport() {
        try {
            Class.forName("net.kyori.adventure.text.object.SpriteObjectContents");
            Component probe = MiniMessage.miniMessage().deserialize("<sprite:items:item/stone>");
            return !PlainTextComponentSerializer.plainText().serialize(probe).contains("sprite");
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    /** The label with the kit's icon placed per kit-names.format, or the label unchanged. */
    public static String decorate(Kit kit, String label) {
        if (label == null) {
            return null;
        }
        String icon = kit == null ? "" : tag(kit);
        if (icon.isEmpty()) {
            return label;
        }
        return format.replace("{icon}", icon).replace("{name}", label);
    }

    /** The MiniMessage tag for this kit's icon, or "" for none. */
    public static String tag(Kit kit) {
        if (!enabled || !supported) {
            return "";
        }
        String spec = kit.getNameIcon();
        if (spec == null || spec.isBlank()) {
            spec = auto(kit.getIcon());
            if (spec == null) {
                return "";
            }
        }
        spec = spec.trim();
        String lower = spec.toLowerCase(Locale.ROOT);
        if (lower.equals("none") || lower.equals("off") || lower.equals("false")) {
            return "";
        }
        // White keeps the sprite's own colours; a parent gradient would tint it.
        if (lower.startsWith("head:")) {
            String who = spec.substring(5).trim();
            return who.isEmpty() || !who.matches("[A-Za-z0-9_\\-]+") ? "" : "<white><head:" + who + "></white>";
        }
        if (lower.startsWith("block/")) {
            return lower.matches("block/[a-z0-9_./\\-]+") ? "<white><sprite:blocks:" + lower + "></white>" : "";
        }
        if (lower.startsWith("minecraft:")) {
            lower = lower.substring(10);
        }
        if (!lower.contains("/")) {
            lower = "item/" + lower;
        }
        return lower.matches("[a-z0-9_./\\-]+") ? "<white><sprite:items:" + lower + "></white>" : "";
    }

    /**
     * The sprite for a menu icon material, or null when the item has no flat
     * picture of its own - blocks, heads and shields are drawn as 3D models,
     * and their sprite would show as the purple missing texture.
     */
    private static String auto(Material material) {
        if (material == null) {
            return null;
        }
        switch (material.name()) {
            case "SHIELD":
            case "PLAYER_HEAD":
            case "AIR":
                return null;
            case "COMPASS":
                return "item/compass_16";
            case "RECOVERY_COMPASS":
                return "item/recovery_compass_16";
            case "CLOCK":
                return "item/clock_00";
            case "CROSSBOW":
                return "item/crossbow_standby";
            default:
                break;
        }
        try {
            if (material.isBlock()) {
                return null;
            }
        }
        catch (Throwable throwable) {
            return null;
        }
        return "item/" + material.name().toLowerCase(Locale.ROOT);
    }
}
