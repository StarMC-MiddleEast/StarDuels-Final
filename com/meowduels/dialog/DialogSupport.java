package com.meowduels.dialog;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Runtime checks for Paper dialogs and protocol-translated legacy clients. */
public final class DialogSupport {
    private static final int MIN_DIALOG_PROTOCOL = 771; // Minecraft 1.21.6

    private DialogSupport() {}

    public static boolean isAvailable(Plugin plugin, Player player) {
        if (player == null || !paperDialogApiPresent(plugin)) return false;

        // VulkanMod is a client-side Fabric renderer, so the server cannot
        // detect that mod by name. Fabric/Quilt clients can fail to render
        // Paper's native dialog screen; use the equivalent inventory menu.
        String brand = player.getClientBrandName();
        if (brand != null) {
            String normalizedBrand = brand.toLowerCase(Locale.ROOT);
            if (normalizedBrand.contains("fabric") || normalizedBrand.contains("quilt")
                || normalizedBrand.contains("vulkan")) return false;
        }

        Plugin via = plugin.getServer().getPluginManager().getPlugin("ViaVersion");
        if (via == null || !via.isEnabled()) return true;

        try {
            ClassLoader loader = via.getClass().getClassLoader();
            Class<?> viaClass = Class.forName("com.viaversion.viaversion.api.Via", true, loader);
            Object api = viaClass.getMethod("getAPI").invoke(null);
            Method getVersion = api.getClass().getMethod("getPlayerVersion", UUID.class);
            int protocol = ((Number) getVersion.invoke(api, player.getUniqueId())).intValue();
            return protocol >= MIN_DIALOG_PROTOCOL;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            // If ViaVersion is present but its API cannot be queried, prefer the
            // inventory menu over sending a dialog to a client that may not read it.
            return false;
        }
    }

    private static boolean paperDialogApiPresent(Plugin plugin) {
        try {
            Class.forName("io.papermc.paper.dialog.Dialog", false, plugin.getClass().getClassLoader());
            Class.forName("io.papermc.paper.registry.data.dialog.DialogBase", false, plugin.getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }
}
