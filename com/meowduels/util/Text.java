package com.meowduels.util;

public final class Text {
    private static final char[] SMALL = new char[]{'\u1d00', '\u0299', '\u1d04', '\u1d05', '\u1d07', '\ua730', '\u0262', '\u029c', '\u026a', '\u1d0a', '\u1d0b', '\u029f', '\u1d0d', '\u0274', '\u1d0f', '\u1d18', '\ua7af', '\u0280', '\ua731', '\u1d1b', '\u1d1c', '\u1d20', '\u1d21', 'x', '\u028f', '\u1d22'};
    public static final String PREFIX = Colors.toSection("<gold>\u2726</gold> <yellow>STAR</yellow> <dark_gray>\u00bb</dark_gray> <white>");

    private Text() {
    }

    public static String color(String string) {
        if (string == null) {
            return "";
        }
        return Colors.toSection(MiniMessageUtil.toMiniMessage(Text.smallCaps(string)));
    }

    public static String prefixed(String string) {
        return Text.plain(string);
    }

    public static String plain(String string) {
        if (string == null) {
            return "";
        }
        String string2 = string.replaceAll("(?i)[&\u00a7][0-9a-fk-or]", "");
        string2 = string2.replaceAll("[^\\x20-\\x7e]", "");
        string2 = string2.replaceAll(" {2,}", " ").trim();
        return Colors.toSection(MiniMessageUtil.toMiniMessage("<gray>" + string2));
    }

    public static String colorNormal(String string) {
        if (string == null) {
            return "";
        }
        return Colors.toSection(MiniMessageUtil.toMiniMessage(string));
    }

    public static String grayEmoji(String string) {
        if (string == null) {
            return "";
        }
        String string2 = string.replaceAll("(?i)[&\u00a7][0-9a-fk-or]", "");
        string2 = string2.replaceAll(" {2,}", " ").trim();
        return Colors.toSection(MiniMessageUtil.toMiniMessage("<gray>" + string2));
    }

    /** Where the tag opening at i ends, or -1 if this '<' does not start one. */
    private static int tagEnd(String string, int i) {
        int close = string.indexOf('>', i);
        if (close <= i + 1) {
            return -1;
        }
        int next = string.indexOf('<', i + 1);
        return next >= 0 && next < close ? -1 : close;
    }

    public static String smallCaps(String string) {
        StringBuilder stringBuilder = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if ((c == '&' || c == '\u00a7') && i + 1 < string.length()) {
                stringBuilder.append(c).append(string.charAt(i + 1));
                ++i;
                continue;
            }
            // MiniMessage tags pass through untouched: re-casing <gradient:...>
            // or a kit icon's <sprite:items:item/...> would break the tag.
            int close = c == '<' ? Text.tagEnd(string, i) : -1;
            if (close > i) {
                stringBuilder.append(string, i, close + 1);
                i = close;
                continue;
            }
            if (c >= 'a' && c <= 'z') {
                stringBuilder.append(SMALL[c - 97]);
                continue;
            }
            if (c >= 'A' && c <= 'Z') {
                stringBuilder.append(SMALL[c - 65]);
                continue;
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
}

