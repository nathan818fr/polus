package com.nathan818.polus.protocol.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GameVersionUtil {
    public static String gameVersionToString(int gameVersion) {
        long v = Integer.toUnsignedLong(gameVersion);
        int y = (int) (v / 25000);
        int m = (int) ((v %= 25000) / 1800);
        int d = (int) ((v %= 1800) / 50);
        int r = (int) (v % 50);
        return "v" + y + "." + m + "." + d + encodeRevision(r);
    }

    private static String encodeRevision(int r) {
        if (r < 26) {
            return String.valueOf((char) ((int) 'a' + r));
        }
        if (r < 50) {
            return String.valueOf((char) ((int) 'A' - 26 + r));
        }
        throw new IllegalArgumentException();
    }
}
