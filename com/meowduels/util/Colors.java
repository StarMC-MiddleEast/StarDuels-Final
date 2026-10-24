package com.meowduels.util;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Colors {
    private static final String CODES = "0123456789abcdefklmnor";
    private static final String[] NAMES = new String[]{"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"};
    private static final int[] RGB = new int[]{0, 170, 43520, 43690, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA, 0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF};

    private Colors() {
    }

    public static String toSection(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        string = MiniMessageUtil.toMiniMessage(string);
        // Validate with Adventure's official parser before producing the
        // legacy string required by older Bukkit API call sites.
        MiniMessage.miniMessage().deserialize(string);
        StringBuilder stringBuilder = new StringBuilder(string.length() + 16);
        int n = string.length();
        int n2 = 0;
        while (n2 < n) {
            int n3;
            int n4;
            char c = string.charAt(n2);
            if ((c == '&' || c == '\u00a7') && n2 + 1 < n && (n4 = Colors.legacyAt(string, n2, stringBuilder)) > 0) {
                n2 += n4;
                continue;
            }
            if (c == '<' && (n3 = string.indexOf(62, n2)) > n2) {
                String string2 = string.substring(n2 + 1, n3);
                if (string2.regionMatches(true, 0, "gradient", 0, 8) && (string2.length() == 8 || string2.charAt(8) == ':')) {
                    int n5 = Colors.indexOfIgnoreCase(string, "</gradient>", n3 + 1);
                    String string3 = string.substring(n3 + 1, n5 < 0 ? n : n5);
                    stringBuilder.append(Colors.gradient(string2, string3));
                    n2 = n5 < 0 ? n : n5 + 11;
                    continue;
                }
                // A kit icon or player head has no colour-code form. Drop the
                // tag and the space after it, leaving just the name.
                String low = string2.toLowerCase(java.util.Locale.ROOT);
                if (low.startsWith("sprite:") || low.startsWith("head:")) {
                    n2 = n3 + 1;
                    if (n2 + 8 <= n && string.regionMatches(true, n2, "</white>", 0, 8)) {
                        n2 += 8;
                    }
                    if (n2 < n && string.charAt(n2) == ' ') {
                        ++n2;
                    }
                    continue;
                }
                String string4 = Colors.tagCode(string2);
                if (string4 != null) {
                    stringBuilder.append(string4);
                }
                n2 = n3 + 1;
                continue;
            }
            stringBuilder.append(c);
            ++n2;
        }
        return stringBuilder.toString();
    }

    public static String hex(int n) {
        String string = String.format("%06x", n & 0xFFFFFF);
        StringBuilder stringBuilder = new StringBuilder(14).append('\u00a7').append('x');
        for (int i = 0; i < 6; ++i) {
            stringBuilder.append('\u00a7').append(string.charAt(i));
        }
        return stringBuilder.toString();
    }

    private static int legacyAt(String string, int n, StringBuilder stringBuilder) {
        if (Character.toLowerCase(string.charAt(n + 1)) == 'x' && n + 13 < string.length()) {
            StringBuilder stringBuilder2 = new StringBuilder(6);
            for (int i = n + 2; i < n + 14; i += 2) {
                char c = string.charAt(i);
                if (c != '&' && c != '\u00a7' || Character.digit(string.charAt(i + 1), 16) < 0) {
                    stringBuilder2 = null;
                    break;
                }
                stringBuilder2.append(string.charAt(i + 1));
            }
            if (stringBuilder2 != null) {
                stringBuilder.append(Colors.hex(Integer.parseInt(stringBuilder2.toString(), 16)));
                return 14;
            }
        }
        if (string.charAt(n + 1) == '#' && Colors.isHex(string, n + 2, 6)) {
            stringBuilder.append(Colors.hex(Integer.parseInt(string.substring(n + 2, n + 8), 16)));
            return 8;
        }
        char c = Character.toLowerCase(string.charAt(n + 1));
        if (CODES.indexOf(c) >= 0) {
            stringBuilder.append('\u00a7').append(c);
            return 2;
        }
        return 0;
    }

    private static String tagCode(String string) {
        Integer n;
        String string2 = string.trim().toLowerCase();
        if (string2.startsWith("/")) {
            return null;
        }
        if (string2.startsWith("color:") || string2.startsWith("colour:") || string2.startsWith("c:")) {
            string2 = string2.substring(string2.indexOf(58) + 1);
        }
        if ((n = Colors.colorOf(string2)) != null) {
            return Colors.hex(n);
        }
        if (string2.equals("b") || string2.equals("bold")) {
            return "\u00a7l";
        }
        if (string2.equals("i") || string2.equals("italic") || string2.equals("em")) {
            return "\u00a7o";
        }
        if (string2.equals("u") || string2.equals("underlined")) {
            return "\u00a7n";
        }
        if (string2.equals("st") || string2.equals("strikethrough")) {
            return "\u00a7m";
        }
        if (string2.equals("obf") || string2.equals("obfuscated")) {
            return "\u00a7k";
        }
        if (string2.equals("reset") || string2.equals("r")) {
            return "\u00a7r";
        }
        return null;
    }

    private static Integer colorOf(String string) {
        if (string.length() == 7 && string.charAt(0) == '#' && Colors.isHex(string, 1, 6)) {
            return Integer.parseInt(string.substring(1), 16);
        }
        for (int i = 0; i < NAMES.length; ++i) {
            if (!NAMES[i].equals(string)) continue;
            return RGB[i];
        }
        return null;
    }

    private static String gradient(String string, String string2) {
        int n;
        Integer comparable;
        String[] stringArray = string.split(":");
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (int i = 1; i < stringArray.length; ++i) {
            comparable = Colors.colorOf(stringArray[i].trim().toLowerCase());
            if (comparable == null) continue;
            arrayList.add(comparable);
        }
        String string3 = Colors.toSection(string2);
        if (arrayList.size() < 2) {
            return string3;
        }
        StringBuilder decorations = new StringBuilder();
        StringBuilder stringBuilder = new StringBuilder();
        int n2 = 0;
        for (int i = 0; i < string3.length(); ++i) {
            n = string3.charAt(i);
            if (n == 167 && i + 1 < string3.length()) {
                char c = Character.toLowerCase(string3.charAt(i + 1));
                if (c == 'x' && i + 13 < string3.length()) {
                    i += 13;
                    continue;
                }
                if ("klmno".indexOf(c) >= 0) {
                    decorations.append('\u00a7').append(c);
                    ++i;
                    continue;
                }
                if (c == 'r') {
                    decorations.setLength(0);
                    ++i;
                    continue;
                }
                ++i;
                continue;
            }
            stringBuilder.append((char)n);
            ++n2;
        }
        if (n2 == 0) {
            return "";
        }
        StringBuilder stringBuilder2 = new StringBuilder(n2 * 16);
        for (n = 0; n < n2; ++n) {
            double d = n2 <= 1 ? 0.0 : (double)n / (double)(n2 - '\u0001');
            stringBuilder2.append(Colors.hex(Colors.lerp(arrayList, d))).append(decorations).append(stringBuilder.charAt(n));
        }
        return stringBuilder2.toString();
    }

    private static int lerp(List<Integer> list, double d) {
        int n = list.size() - 1;
        double d2 = d * (double)n;
        int n2 = (int)Math.floor(d2);
        if (n2 >= n) {
            n2 = n - 1;
        }
        double d3 = d2 - (double)n2;
        int n3 = list.get(n2);
        int n4 = list.get(n2 + 1);
        int n5 = (int)Math.round((double)(n3 >> 16 & 0xFF) + (double)((n4 >> 16 & 0xFF) - (n3 >> 16 & 0xFF)) * d3);
        int n6 = (int)Math.round((double)(n3 >> 8 & 0xFF) + (double)((n4 >> 8 & 0xFF) - (n3 >> 8 & 0xFF)) * d3);
        int n7 = (int)Math.round((double)(n3 & 0xFF) + (double)((n4 & 0xFF) - (n3 & 0xFF)) * d3);
        return n5 << 16 | n6 << 8 | n7;
    }

    private static int indexOfIgnoreCase(String string, String string2, int n) {
        int n2 = string.length() - string2.length();
        for (int i = Math.max(0, n); i <= n2; ++i) {
            if (!string.regionMatches(true, i, string2, 0, string2.length())) continue;
            return i;
        }
        return -1;
    }

    private static boolean isHex(String string, int n, int n2) {
        if (n + n2 > string.length()) {
            return false;
        }
        for (int i = n; i < n + n2; ++i) {
            if (Character.digit(string.charAt(i), 16) >= 0) continue;
            return false;
        }
        return true;
    }
}

