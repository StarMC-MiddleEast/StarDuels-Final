package com.meowduels.util;

public final class Marks {
    public static final String FLAG = "\u2691";
    public static final String SKULL = "\u2620";
    public static final char BLUE = '9';
    /** Team Aqua's colour - light blue, matching the team's name in messages. */
    public static final char AQUA = 'b';
    public static final char RED = 'c';
    public static final char EVENT = 'e';
    public static final char ALIVE = 'a';
    public static final char OUT = '8';

    private Marks() {
    }

    public static String tab(char c) {
        return "\u00a7" + c + " \u2691 \u00a7" + c;
    }

    public static String tag(char c) {
        return "\u00a7" + c + "\u2691 \u00a7" + c;
    }

    public static String tabOut() {
        return "\u00a78 \u2620 \u00a78";
    }

    public static String tagOut() {
        return "\u00a78\u2620 \u00a78";
    }
}

