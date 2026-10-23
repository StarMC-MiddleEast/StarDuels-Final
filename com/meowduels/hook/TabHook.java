package com.meowduels.hook;

import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.entity.Player;

public final class TabHook {
    private final boolean available;
    private Object api;
    private Method getPlayer;
    private Object tablistManager;
    private Method tablistSetPrefix;
    private Object nametagManager;
    private Method nametagSetPrefix;
    private Method nametagSetSuffix;

    public TabHook() {
        boolean bl = false;
        try {
            Class<?> clazz = Class.forName("me.neznamy.tab.api.TabAPI");
            this.api = clazz.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            Class<?> clazz2 = Class.forName("me.neznamy.tab.api.TabPlayer");
            this.getPlayer = clazz.getMethod("getPlayer", UUID.class);
            if (this.api != null) {
                this.tablistManager = this.tryGet(clazz, "getTabListFormatManager");
                this.tablistSetPrefix = this.resolveMethod(this.tablistManager, "setPrefix", clazz2);
                this.nametagManager = this.tryGet(clazz, "getNameTagManager");
                this.nametagSetPrefix = this.resolveMethod(this.nametagManager, "setPrefix", clazz2);
                this.nametagSetSuffix = this.resolveMethod(this.nametagManager, "setSuffix", clazz2);
                bl = this.getPlayer != null && (this.tablistSetPrefix != null || this.nametagSetPrefix != null);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.available = bl;
    }

    private Object tryGet(Class<?> clazz, String string) {
        try {
            return clazz.getMethod(string, new Class[0]).invoke(this.api, new Object[0]);
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private Method resolveMethod(Object object, String string, Class<?> clazz) {
        if (object == null) {
            return null;
        }
        try {
            Method method = object.getClass().getMethod(string, clazz, String.class);
            method.setAccessible(true);
            return method;
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    public boolean isAvailable() {
        return this.available;
    }

    private Object tabPlayer(Player player) {
        try {
            return this.getPlayer.invoke(this.api, player.getUniqueId());
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    public void hideRank(Player player) {
        this.setPrefix(player, "");
    }

    public void setRankPrefix(Player player, String string) {
        if (!this.available || this.nametagSetPrefix == null || player == null) {
            return;
        }
        Object object = this.tabPlayer(player);
        if (object == null) {
            return;
        }
        try {
            this.nametagSetPrefix.invoke(this.nametagManager, object, string);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void setTablistPrefix(Player player, String string) {
        if (!this.available || this.tablistSetPrefix == null || player == null) {
            return;
        }
        Object object = this.tabPlayer(player);
        if (object == null) {
            return;
        }
        try {
            this.tablistSetPrefix.invoke(this.tablistManager, object, string);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void applyDuel(Player player, String string, String string2) {
        this.setPrefix(player, string);
        this.setNametagSuffix(player, string2);
    }

    public void clearDuel(Player player) {
        this.setPrefix(player, null);
        this.setNametagSuffix(player, null);
    }

    public void restoreRank(Player player) {
        this.setPrefix(player, null);
    }

    private void setPrefix(Player player, String string) {
        if (!this.available || player == null) {
            return;
        }
        Object object = this.tabPlayer(player);
        if (object == null) {
            return;
        }
        if (this.tablistSetPrefix != null) {
            try {
                this.tablistSetPrefix.invoke(this.tablistManager, object, string);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (this.nametagSetPrefix != null) {
            try {
                this.nametagSetPrefix.invoke(this.nametagManager, object, string);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public void setNametagSuffix(Player player, String string) {
        if (!this.available || this.nametagSetSuffix == null || player == null) {
            return;
        }
        Object object = this.tabPlayer(player);
        if (object == null) {
            return;
        }
        try {
            this.nametagSetSuffix.invoke(this.nametagManager, object, string);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

