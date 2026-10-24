package com.meowduels.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/** Shared Adventure message helpers for player-facing plugin notices. */
public final class MessageUtils {
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Component INFO_PREFIX = MINI.deserialize("<dark_gray>[<yellow>ⓘ</yellow>]</dark_gray> <gray>");

    private MessageUtils() {}

    public static void send(Component prefix, Audience audience, String miniMessageString) {
        if (audience != null) audience.sendMessage(prefix.append(MINI.deserialize(miniMessageString)));
    }

    public static void send(Audience audience, String miniMessageString) {
        send(INFO_PREFIX, audience, miniMessageString);
    }

    public static Component infoPrefix() {
        return INFO_PREFIX;
    }
}
