package com.nathan818.polus.protocol.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GameCodeUtil {
    public static int gameCodeFromString(String value) throws IllegalArgumentException {
        if (value.length() == 6) {
            return gameCodeV2FromString(value);
        }
        return gameCodeV1FromString(value);
    }

    public static String gameCodeToString(int gameCode) throws IllegalArgumentException {
        if ((gameCode & V2_BIT) == V2_BIT) {
            return gameCodeV2ToString(gameCode);
        }
        return gameCodeV1ToString(gameCode);
    }

    public static boolean isValidGameCode(int gameCode) {
        if ((gameCode & V2_BIT) == V2_BIT) {
            return isValidGameCodeV2(gameCode);
        }
        return isValidGameCodeV1(gameCode);
    }

    public static int getGameCodeVersion(int gameCode) {
        if ((gameCode & V2_BIT) == V2_BIT) {
            return isValidGameCodeV2(gameCode) ? 2 : 0;
        }
        return isValidGameCodeV1(gameCode) ? 1 : 0;
    }

    public static int randomGameCode() {
        return randomGameCodeV2();
    }

    public static int randomGameCode(Random random) {
        return randomGameCodeV2(random);
    }

    /*
     * Version 1 (4-chars codes)
     */

    public static final int V1_INDEX_MAX_VALUE = 456975; // 26^4 - 1

    public static int gameCodeV1FromString(String value) {
        if (value.length() != 4) {
            throw invalidGameCodeString(value);
        }
        return (v1decodeChar(value.charAt(0)) << 24)
                | (v1decodeChar(value.charAt(1)) << 16)
                | (v1decodeChar(value.charAt(2)) << 8)
                | v1decodeChar(value.charAt(3));
    }

    public static String gameCodeV1ToString(int gameCode) {
        char[] chars = new char[4];
        chars[0] = v1encodeChar(gameCode, 3);
        chars[1] = v1encodeChar(gameCode, 2);
        chars[2] = v1encodeChar(gameCode, 1);
        chars[3] = v1encodeChar(gameCode, 0);
        return new String(chars);
    }

    public static boolean isValidGameCodeV1(int gameCode) {
        return v1isValidDigit((gameCode >> 24) & 0xFF)
                && v1isValidDigit((gameCode >> 16) & 0xFF)
                && v1isValidDigit((gameCode >> 8) & 0xFF)
                && v1isValidDigit(gameCode & 0xFF);
    }

    public static int gameCodeV1FromIndex(int index) throws IllegalArgumentException {
        if (!isValidGameCodeV1Index(index)) {
            throw invalidGameCodeIndex(index);
        }
        return gameCodeV1FromIndexUnchecked(index);
    }

    private static int gameCodeV1FromIndexUnchecked(int index) {
        return (((int) 'A' + (index % 26)) << 24)
                | (((int) 'A' + ((index /= 26) % 26)) << 16)
                | (((int) 'A' + ((index /= 26) % 26)) << 8)
                | ((int) 'A' + (index / 26) % 26);
    }

    public static int gameCodeV1ToIndex(int gameCode) throws IllegalArgumentException {
        return (((gameCode >> 24) & 0xFF) - (int) 'A')
                + (((gameCode >> 16) & 0xFF) - (int) 'A') * 26
                + (((gameCode >> 8) & 0xFF) - (int) 'A') * 676
                + ((gameCode & 0xFF) - (int) 'A') * 17576;
    }

    public static boolean isValidGameCodeV1Index(int index) {
        return index >= 0 && index <= V1_INDEX_MAX_VALUE;
    }

    public static int randomGameCodeV1() {
        return randomGameCodeV1(ThreadLocalRandom.current());
    }

    public static int randomGameCodeV1(Random random) {
        return gameCodeV1FromIndexUnchecked(random.nextInt(V1_INDEX_MAX_VALUE + 1));
    }

    private static int v1decodeChar(char c) {
        if (c < 'A' || c > 'Z') {
            throw invalidGameCodeCharacter(c);
        }
        return c;
    }

    private static char v1encodeChar(int gameCode, int index) {
        int digit = (gameCode >> (index * 8)) & 0xFF;
        if (digit < (int) 'A' || digit > (int) 'Z') {
            throw invalidGameCode(gameCode);
        }
        return (char) digit;
    }

    private static boolean v1isValidDigit(int digit) {
        return digit >= (int) 'A' && digit <= (int) 'Z';
    }

    /*
     * Version 2 (6-chars codes)
     */

    private static final String V2_ALPHABET = "QWXRTYLPESDFGHUJKZOCVBINMA";
    private static final int[] V2_ALPHABET_INDEX = new int[]{25, 21, 19, 10, 8, 11, 12, 13, 22, 15, 16, 6, 24, 23, 18, 7, 0, 3, 9, 4, 14, 20, 1, 2, 5, 17};
    private static final int V2_BIT = 0x80000000;
    private static final int V2_LSB_MAX_VALUE = 675;
    private static final int V2_MSB_MAX_VALUE = 456975;

    public static final int V2_INDEX_MAX_VALUE = ((V2_LSB_MAX_VALUE + 1) * (V2_MSB_MAX_VALUE + 1)) - 1;

    public static int gameCodeV2FromString(String value) throws IllegalArgumentException {
        if (value.length() != 6) {
            throw invalidGameCodeString(value);
        }
        int c0 = v2decodeChar(value.charAt(0));
        int c1 = v2decodeChar(value.charAt(1));
        int c2 = v2decodeChar(value.charAt(2));
        int c3 = v2decodeChar(value.charAt(3));
        int c4 = v2decodeChar(value.charAt(4));
        int c5 = v2decodeChar(value.charAt(5));
        int lsb = (c0 + 26 * c1);
        int msb = (c2 + 26 * (c3 + 26 * (c4 + 26 * c5)));
        return v2pack(lsb, msb);
    }

    public static String gameCodeV2ToString(int gameCode) throws IllegalArgumentException {
        int lsb = v2unpackLsb(gameCode);
        int msb = v2unpackMsb(gameCode);
        if (!v2isValid(gameCode, lsb, msb)) {
            throw invalidGameCode(gameCode);
        }
        return new String(new char[]{
                V2_ALPHABET.charAt(lsb % 26),
                V2_ALPHABET.charAt(lsb / 26 % 26),
                V2_ALPHABET.charAt(msb % 26),
                V2_ALPHABET.charAt(msb / 26 % 26),
                V2_ALPHABET.charAt(msb / (26 * 26) % 26),
                V2_ALPHABET.charAt(msb / (26 * 26 * 26) % 26)
        });
    }

    public static boolean isValidGameCodeV2(int gameCode) {
        int lsb = v2unpackLsb(gameCode);
        int msb = v2unpackMsb(gameCode);
        return v2isValid(gameCode, lsb, msb);
    }

    public static int gameCodeV2FromIndex(int index) throws IllegalArgumentException {
        if (!isValidGameCodeV2Index(index)) {
            throw invalidGameCodeIndex(index);
        }
        return gameCodeV2FromIndexUnchecked(index);
    }

    private static int gameCodeV2FromIndexUnchecked(int index) {
        int lsb = index % (V2_LSB_MAX_VALUE + 1);
        int msb = index / (V2_LSB_MAX_VALUE + 1);
        return v2pack(lsb, msb);
    }

    public static int gameCodeV2ToIndex(int gameCode) throws IllegalArgumentException {
        int lsb = v2unpackLsb(gameCode);
        int msb = v2unpackMsb(gameCode);
        if (!v2isValid(gameCode, lsb, msb)) {
            throw invalidGameCode(gameCode);
        }
        return msb * V2_LSB_MAX_VALUE + lsb;
    }

    public static boolean isValidGameCodeV2Index(int index) {
        return index >= 0 && index <= V2_INDEX_MAX_VALUE;
    }

    public static int randomGameCodeV2() {
        return randomGameCodeV2(ThreadLocalRandom.current());
    }

    public static int randomGameCodeV2(Random random) {
        return gameCodeV2FromIndexUnchecked(random.nextInt(V2_INDEX_MAX_VALUE + 1));
    }

    private static int v2pack(int lsb, int msb) {
        return V2_BIT | (msb << 10) | lsb;
    }

    private static int v2unpackLsb(int gameCode) {
        return gameCode & 0x3FF;
    }

    private static int v2unpackMsb(int gameCode) {
        return (gameCode >> 10) & 0x1FFFFE;
    }

    private static boolean v2isValid(int gameCode, int lsb, int msb) {
        return lsb >= 0 && lsb <= V2_LSB_MAX_VALUE
                && msb >= 0 && msb <= V2_MSB_MAX_VALUE
                && (gameCode & V2_BIT) == V2_BIT;
    }

    private static int v2decodeChar(char c) {
        int i = c - 65;
        if (i < 0 || i >= V2_ALPHABET_INDEX.length) {
            throw invalidGameCodeCharacter(c);
        }
        return V2_ALPHABET_INDEX[i];
    }

    /*
     * ----------
     */

    private static IllegalArgumentException invalidGameCode(int gameCode) {
        return new IllegalArgumentException(String.format("Invalid GameCode: 0x%08X", gameCode));
    }

    private static IllegalArgumentException invalidGameCodeString(String str) {
        return new IllegalArgumentException("Invalid GameCode string: " + str);
    }

    private static IllegalArgumentException invalidGameCodeCharacter(char c) {
        return new IllegalArgumentException("Invalid GameCode character: " + c);
    }

    private static IllegalArgumentException invalidGameCodeIndex(int index) {
        return new IllegalArgumentException("Invalid GameCode index: " + index);
    }

    /* // CODE: Compute the V2_ALPHABET_INDEX array from the V2_ALPHABET string
    public static void main(String[] args) {
        int[] V2_ALPHABET_INDEX = new int[V2_ALPHABET.length()];
        for (int i = 0; i < V2_ALPHABET.length(); ++i) {
            V2_ALPHABET_INDEX[i] = V2_ALPHABET.indexOf('A' + i);
            if (V2_ALPHABET_INDEX[i] == -1) {
                throw new RuntimeException("V2_ALPHABET must contains only uppercase latin letters");
            }
        }
        System.out.println("int[] V2_ALPHABET_INDEX = new int[]{"
                + Arrays.stream(V2_ALPHABET_INDEX).mapToObj(Integer::toString).collect(Collectors.joining(", ")) + "};");
    } */
}
