package com.meowduels.util;

import java.util.Map;

public final class Tokens {
    private Tokens() {
    }

    public static String apply(String string, Map<String, String> map) {
        if (string == null || map == null || map.isEmpty() || string.indexOf(123) < 0) {
            return string;
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String string2 = entry.getValue();
            string = string.replace("{" + entry.getKey() + "}", string2 == null ? "" : string2);
        }
        return string;
    }
}

