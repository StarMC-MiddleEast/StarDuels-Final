package com.meowduels.util;

import java.util.Locale;

public final class Health {
    private static final double EXACT_BELOW = 20.0;

    private Health() {
    }

    public static String nametag(double d) {
        if (d < 0.0) {
            d = 0.0;
        }
        return String.format(Locale.US, "%.1f", d);
    }

    public static String text(double d) {
        if (d < 0.0) {
            d = 0.0;
        }
        if (d < 20.0) {
            return String.format(Locale.US, "%.2f", d);
        }
        return String.valueOf((int)Math.round(d));
    }
}

