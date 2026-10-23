package com.meowduels.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/** Central MiniMessage entry point used by titles and configurable text. */
public final class MiniMessageUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MiniMessageUtil() {
    }

    /** Convert legacy ampersand formatting to MiniMessage before parsing. */
    public static String toMiniMessage(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == '&' || c == '\u00a7') && i + 1 < input.length()) {
                char code = Character.toLowerCase(input.charAt(i + 1));
                if (code == 'x' && i + 13 < input.length()) {
                    StringBuilder hex = new StringBuilder(6);
                    int p = i + 2;
                    boolean valid = true;
                    for (int n = 0; n < 6; n++) {
                        if (p >= input.length() || input.charAt(p) != '&' || p + 1 >= input.length()) {
                            valid = false;
                            break;
                        }
                        char digit = input.charAt(p + 1);
                        if (Character.digit(digit, 16) < 0) {
                            valid = false;
                            break;
                        }
                        hex.append(digit);
                        p += 2;
                    }
                    if (valid) {
                        out.append("<#").append(hex).append('>');
                        i += 13;
                        continue;
                    }
                }
                String tag = legacyTag(code);
                if (tag != null) {
                    out.append(tag);
                    i++;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    public static Component deserialize(String input) {
        return MINI_MESSAGE.deserialize(toMiniMessage(input));
    }

    private static String legacyTag(char code) {
        return switch (code) {
            case '0' -> "<black>";
            case '1' -> "<dark_blue>";
            case '2' -> "<dark_green>";
            case '3' -> "<dark_aqua>";
            case '4' -> "<dark_red>";
            case '5' -> "<dark_purple>";
            case '6' -> "<gold>";
            case '7' -> "<gray>";
            case '8' -> "<dark_gray>";
            case '9' -> "<blue>";
            case 'a' -> "<green>";
            case 'b' -> "<aqua>";
            case 'c' -> "<red>";
            case 'd' -> "<light_purple>";
            case 'e' -> "<yellow>";
            case 'f' -> "<white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> null;
        };
    }
}
